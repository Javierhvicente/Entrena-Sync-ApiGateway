package entrenasync.dev.entrenasyncapigateway.Exceptions;

public class KeyCloakUserExceptions extends RuntimeException {

    public KeyCloakUserExceptions(String message) {
        super(message);
    }

    public static class UserAlreadyExistsException extends KeyCloakUserExceptions {
        public UserAlreadyExistsException(String username) {
            super("The user " + username + " already exists");
        }
    }

    public static class UserNotFoundException extends KeyCloakUserExceptions {
        public UserNotFoundException(String userId) {
            super("User not found with id " + userId);
        }
    }

    public static class InvalidRoleException extends KeyCloakUserExceptions {
        public InvalidRoleException(String roleName) {
            super("The role " + roleName + "is not valid");
        }
    }

    public static class InvalidCredentialsException extends KeyCloakUserExceptions {
        public InvalidCredentialsException() {
            super("The credentials introduce aren´t valid");
        }
    }

    public static class KeycloakOperationException extends KeyCloakUserExceptions {
        public KeycloakOperationException(String message) {
            super("Error during the keycloak operation" + message);
        }
    }

    public static class KeycloakNotMatchingPasswords extends KeyCloakUserExceptions {
        public KeycloakNotMatchingPasswords(String message) {
            super(message);
        }
    }
}
