package entrenasync.dev.entrenasyncapigateway.User.Mappers;

import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import org.keycloak.representations.idm.UserRepresentation;

public final class UserMappers {

    private UserMappers() {
        // evitar instancias
    }

    public static UserResponse toUserResponse(UserRepresentation user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedTimestamp()
        );
    }
}
