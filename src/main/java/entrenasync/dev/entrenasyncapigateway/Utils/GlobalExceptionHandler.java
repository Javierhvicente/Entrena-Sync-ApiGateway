package entrenasync.dev.entrenasyncapigateway.Utils;

import entrenasync.dev.entrenasyncapigateway.Auth.Exceptions.SessionExceptions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionExceptions.loginBadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(SessionExceptions.loginBadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Credenciales inválidas para el usuario: Email o contraseña incorrectos" );
    }

}
