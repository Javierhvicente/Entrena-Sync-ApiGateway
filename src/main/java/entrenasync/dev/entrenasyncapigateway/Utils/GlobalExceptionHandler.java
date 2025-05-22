package entrenasync.dev.entrenasyncapigateway.Utils;

import entrenasync.dev.entrenasyncapigateway.Auth.Exception.SessionExceptions;
import entrenasync.dev.entrenasyncapigateway.User.Exceptions.KeyCloakUserExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionExceptions.loginBadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(SessionExceptions.loginBadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(SessionExceptions.noTokenOnRequest.class)
    public ResponseEntity<String> handleNoToken(SessionExceptions.noTokenOnRequest ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(KeyCloakUserExceptions.UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExists(KeyCloakUserExceptions.UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ex.getMessage());
    }

    @ExceptionHandler(KeyCloakUserExceptions.UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(KeyCloakUserExceptions.UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler(KeyCloakUserExceptions.InvalidRoleException.class)
    public ResponseEntity<String> handleInvalidRole(KeyCloakUserExceptions.InvalidRoleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(KeyCloakUserExceptions.InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentials(KeyCloakUserExceptions.InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ex.getMessage());
    }

    @ExceptionHandler(KeyCloakUserExceptions.KeycloakOperationException.class)
    public ResponseEntity<String> handleKeycloakOperation(KeyCloakUserExceptions.KeycloakOperationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(KeyCloakUserExceptions.KeycloakNotMatchingPasswords.class)
    public ResponseEntity<String> handleKeycloakNotMatchingPasswords(KeyCloakUserExceptions.KeycloakNotMatchingPasswords ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

}
