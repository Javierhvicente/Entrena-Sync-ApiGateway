package entrenasync.dev.entrenasyncapigateway.User.Services;

import entrenasync.dev.entrenasyncapigateway.Auth.Config.KeycloakProvider;
import entrenasync.dev.entrenasyncapigateway.Auth.Exceptions.KeyCloakUserExceptions;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserUpdateRequest;
import entrenasync.dev.entrenasyncapigateway.User.Mappers.UserMappers;
import entrenasync.dev.entrenasyncapigateway.Utils.PagedResponse;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static entrenasync.dev.entrenasyncapigateway.User.Mappers.UserMappers.toUserResponse;

@Service
@Slf4j
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final KeycloakProvider keycloakProvider;
    @Autowired
    public KeycloakUserServiceImpl(KeycloakProvider keycloakProvider){
        this.keycloakProvider = keycloakProvider;
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(int page, int size, String type) {
        log.info("Getting users - page: {}, size: {}, type filter: {}", page, size, type);

        // Obtener todos los usuarios
        List<UserRepresentation> allUsers = keycloakProvider.realmResource()
                .users()
                .list();

        List<UserRepresentation> filteredUsers;

        // Aplicar filtro solo si se especifica un tipo
        if (type != null && !type.trim().isEmpty()) {
            log.info("Applying type filter: {}", type);

            filteredUsers = allUsers.stream()
                    .filter(user -> {
                        if (user.getAttributes() == null) return false;

                        List<String> typeValues = user.getAttributes().get("type");
                        if (typeValues == null || typeValues.isEmpty()) {
                            // También buscar con "Type" (mayúscula) por compatibilidad
                            typeValues = user.getAttributes().get("Type");
                        }

                        return typeValues != null &&
                                typeValues.stream().anyMatch(t -> t.equalsIgnoreCase(type));
                    })
                    .toList();

            log.info("Found {} users of type '{}' out of {} total users",
                    filteredUsers.size(), type, allUsers.size());
        } else {
            log.info("No type filter applied, returning all users");
            filteredUsers = allUsers;
        }

        // Aplicar paginación manual
        int totalElements = filteredUsers.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = page * size;
        int end = Math.min(start + size, totalElements);

        // Validar que start no sea mayor que el total de elementos
        if (start >= totalElements) {
            return new PagedResponse<>(List.of(), page, size, totalElements, totalPages);
        }

        List<UserRepresentation> pagedUsers = filteredUsers.subList(start, end);

        // Convertir a UserResponse
        List<UserResponse> content = pagedUsers.stream()
                .map(UserMappers::toUserResponse)
                .toList();

        return new PagedResponse<>(content, page, size, totalElements, totalPages);
    }

    private String extractAttributeValue(UserRepresentation user, String attributeName) {
        if (user.getAttributes() == null) {
            log.debug("No attributes found for user: {}", user.getUsername());
            return null;
        }

        List<String> attributeValues = user.getAttributes().get(attributeName);
        if (attributeValues == null || attributeValues.isEmpty()) {
            log.debug("Attribute '{}' not found for user: {}", attributeName, user.getUsername());
            return null;
        }

        String value = attributeValues.get(0);
        log.debug("Found attribute '{}' = '{}' for user: {}", attributeName, value, user.getUsername());
        return value;
    }


    @Override
    public UserResponse getUserByUsername(String username) {
        UserRepresentation user = keycloakProvider.realmResource()
                .users()
                .searchByUsername(username, true)
                .stream()
                .findFirst()
                .orElseThrow(() -> new KeyCloakUserExceptions.UserNotFoundException(username));

        return toUserResponse(user);
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        var usersResource = keycloakProvider.realmResource().users();

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

        UsersResource userResource = keycloakProvider.usersResource(keycloakProvider.realmResource());
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEnabled(true);


        Map<String, List<String>> attributes = new HashMap<>();
        attributes.put("Type", List.of(userRequest.getType().name()));
        user.setAttributes(attributes);
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
            RealmResource realmResource = keycloakProvider.realmResource();

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
    public UserResponse updateUser(UserUpdateRequest userRequest, String userId) {
        UserResource userResource = keycloakProvider.usersResource(keycloakProvider.realmResource()).get(userId);
        UserRepresentation user = userResource.toRepresentation();

        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
            user.setUsername(userRequest.getEmail());
        }

        if (userRequest.getFirstName() != null) {
            user.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
        }

        if (userRequest.getPassword() != null) {
            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userRequest.getPassword());

            user.setCredentials(Collections.singletonList(credentialRepresentation));
        }

        if (userRequest.getType() != null) {
            Map<String, List<String>> attributes = user.getAttributes();
            if (attributes == null) {
                attributes = new HashMap<>();
            }
            attributes.put("Type", List.of(userRequest.getType().name()));
            user.setAttributes(attributes);
        }

        userResource.update(user);

        UserRepresentation updated = userResource.toRepresentation();
        return toUserResponse(updated);
    }


    @Override
    public void deleteUser(String userId) {
        keycloakProvider.usersResource(keycloakProvider.realmResource()).get(userId).remove();
    }


}