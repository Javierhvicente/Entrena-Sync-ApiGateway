package entrenasync.dev.entrenasyncapigateway.Auth.Controller;

import entrenasync.dev.entrenasyncapigateway.Auth.Services.SessionService;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/session")
public class SessionController {
    private final SessionService sessionService;
    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> login(
            @RequestBody LoginRequest request,
            ServerHttpResponse response
    ) {
        return sessionService.login(request).map(loginResponse -> {

            // access token
            ResponseCookie accessToken = ResponseCookie.from("access_token", loginResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(loginResponse.getExpiresIn())
                    .build();

            // refresh token
            ResponseCookie refreshToken = ResponseCookie.from("refresh_token", loginResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(loginResponse.getRefreshExpiresIn())
                    .build();

            response.addCookie(accessToken);
            response.addCookie(refreshToken);

            return ResponseEntity.status(HttpStatus.OK).build();
        });
    }
}
