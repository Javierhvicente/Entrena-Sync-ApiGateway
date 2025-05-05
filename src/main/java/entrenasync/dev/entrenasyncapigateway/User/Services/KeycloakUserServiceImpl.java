package entrenasync.dev.entrenasyncapigateway.User.Services;

import entrenasync.dev.entrenasyncapigateway.Exceptions.KeyCloakUserExceptions;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import entrenasync.dev.entrenasyncapigateway.User.Services.KeycloakUserService;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static entrenasync.dev.entrenasyncapigateway.User.Mappers.UserMappers.toUserResponse;

@Service
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final RealmResource realmResource;
    private final UsersResource usersResource;

    public KeycloakUserServiceImpl(RealmResource realmResource, UsersResource usersResource) {
        this.realmResource = realmResource;
        this.usersResource = usersResource;
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return usersResource
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
        UserRepresentation user = usersResource
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new KeyCloakUserExceptions.UserNotFoundException(username));

        return toUserResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
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

        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(true);

        Response response = usersResource.create(user);
        var status = response.getStatus();

        if (status == 201) {
            String path = response.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf("/") + 1);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(OAuth2Constants.PASSWORD);
            credential.setValue(userRequest.getPassword());
            credential.setTemporary(false);

            usersResource.get(userId).resetPassword(credential);

            List<RoleRepresentation> roleRepresentations;

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

            return toUserResponse(usersResource.get(userId).toRepresentation());
        } else {
            throw new KeyCloakUserExceptions.KeycloakOperationException("Failed to create user: " + response.readEntity(String.class));
        }
    }

    @Override
    public UserResponse updateUser(UserRequest userRequest, String userId) {
        // Verifica si el usuario existe
        UserRepresentation existingUser = usersResource.get(userId).toRepresentation();
        if (existingUser == null) {
            throw new KeyCloakUserExceptions.UserNotFoundException(userId);
        }

        // Validar si las contraseñas coinciden (si es que vienen)
        if (userRequest.getPassword() != null && !userRequest.getPassword().equals(userRequest.getPasswordConfirmation())) {
            throw new KeyCloakUserExceptions.KeycloakNotMatchingPasswords("Passwords do not match");
        }

        // Actualizar campos
        existingUser.setUsername(userRequest.getUsername());
        existingUser.setEmail(userRequest.getEmail());
        existingUser.setFirstName(userRequest.getFirstName());
        existingUser.setLastName(userRequest.getLastName());

        usersResource.get(userId).update(existingUser);

        // Actualizar contraseña si se proporciona
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userRequest.getPassword());

            usersResource.get(userId).resetPassword(credentialRepresentation);
        }

        // Actualizar roles si vienen
        if (userRequest.getRoles() != null && !userRequest.getRoles().isEmpty()) {
            List<RoleRepresentation> roles = realmResource.roles()
                    .list()
                    .stream()
                    .filter(role -> userRequest.getRoles()
                            .stream()
                            .anyMatch(reqRole -> reqRole.equalsIgnoreCase(role.getName())))
                    .toList();

            usersResource.get(userId).roles().realmLevel().remove(
                    usersResource.get(userId).roles().realmLevel().listAll()
            );
            usersResource.get(userId).roles().realmLevel().add(roles);
        }

        return toUserResponse(usersResource.get(userId).toRepresentation());
    }


    @Override
    public void deleteUser(String userId) {
        try {
            usersResource.get(userId).remove();
        } catch (Exception e) {
            throw new KeyCloakUserExceptions.KeycloakOperationException("Failed to delete user with ID: " + userId);
        }
    }

}
