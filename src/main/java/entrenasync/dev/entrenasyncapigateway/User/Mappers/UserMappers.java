package entrenasync.dev.entrenasyncapigateway.User.Mappers;

import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public final class UserMappers {

    private UserMappers() {
        // evitar instancias
    }

    public static UserResponse toUserResponse(UserRepresentation user) {
        UserRequest.Type type = extractTypeFromAttributes(user);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                type != null ? type.name() : null,
                user.getFirstName(),
                user.getLastName()
        );
    }

    private static UserRequest.Type extractTypeFromAttributes(UserRepresentation user) {
        if (user.getAttributes() == null) return null;

        List<String> typeValues = user.getAttributes().get("Type");
        if (typeValues == null || typeValues.isEmpty()) return null;

        try {
            return UserRequest.Type.valueOf(typeValues.get(0));
        } catch (IllegalArgumentException e) {
            return null; // o lanza una excepción si prefieres
        }
    }
}
