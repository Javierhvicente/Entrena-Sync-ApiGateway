package entrenasync.dev.entrenasyncapigateway.User.Services;

import entrenasync.dev.entrenasyncapigateway.Auth.KeycloakProvider;
import entrenasync.dev.entrenasyncapigateway.Exceptions.KeyCloakUserExceptions;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static entrenasync.dev.entrenasyncapigateway.User.Mappers.UserMappers.toUserResponse;

@Service
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    @Override
    public List<UserResponse> getAllUsers() {
        return KeycloakProvider.getRealmResource()
                .users()
                .list()
                .stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getCreatedTimestamp()
                ))
                .toList();
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        UserRepresentation user = KeycloakProvider.getRealmResource()
                .users()
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new KeyCloakUserExceptions.UserNotFoundException(username));

        return toUserResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        var usersResource = KeycloakProvider.getRealmResource().users();

        var conflictUser = usersResource.search(userRequest.getUsername(), true)
                .stream()
                .findFirst()
                .orElse(null);

        if (conflictUser != null) {
            if (Objects.equals(conflictUser.getUsername(), userRequest.getUsername())) {
                throw new KeyCloakUserExceptions.UserAlreadyExistsException(userRequest.getUsername());
            }
            if (Objects.equals(conflictUser.getEmail(), userRequest.getEmail())) {
                throw new KeyCloakUserExceptions.UserAlreadyExistsException(userRequest.getEmail());
            }
        }

        if (!Objects.equals(userRequest.getPassword(), userRequest.getPasswordConfirmation())) {
            throw new KeyCloakUserExceptions.KeycloakNotMatchingPasswords("Passwords do not match");
        }

        UsersResource userResource = KeycloakProvider.getUserResource();
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(true);

        Response response = userResource.create(user);
        var status = response.getStatus();

        if (status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(OAuth2Constants.PASSWORD);
            credential.setValue(userRequest.getPassword());
            credential.setTemporary(false);

            userResource.get(userId).resetPassword(credential);

            List<RoleRepresentation> roleRepresentations;
            RealmResource realmResource = KeycloakProvider.getRealmResource();

            if (userRequest.getRoles() == null || userRequest.getRoles().isEmpty()) {
                roleRepresentations = List.of(realmResource.roles().get("user").toRepresentation());
            } else {
                roleRepresentations = realmResource.roles()
                        .list()
                        .stream()
                        .filter(role -> userRequest.getRoles()
                                .stream()
                                .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                        .toList();
            }

            realmResource.users().get(userId).roles().realmLevel().add(roleRepresentations);

            return toUserResponse(realmResource.users().get(userId).toRepresentation());
        } else {
            throw new KeyCloakUserExceptions.KeycloakOperationException("Failed to create user: " + response.readEntity(String.class));
        }
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest, String userId) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setType(OAuth2Constants.PASSWORD);
        credentialRepresentation.setValue(userRequest.getPassword());

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(true);
        user.setCredentials(Collections.singletonList(credentialRepresentation));

        KeycloakProvider.getUserResource().get(userId).update(user);

        UserRepresentation updated = KeycloakProvider.getUserResource().get(userId).toRepresentation();
        return toUserResponse(updated);
    }

    @Override
    public void deleteUser(String userId) {
        KeycloakProvider.getUserResource().get(userId).remove();
    }


}
