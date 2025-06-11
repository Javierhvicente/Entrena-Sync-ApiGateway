package entrenasync.dev.entrenasyncapigateway.Auth.Controller;

import entrenasync.dev.entrenasyncapigateway.Auth.Services.SessionService;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.UserAuthResponse;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/session")
public class SessionController {
    @Value("${cookies.secure}")
    private boolean secureCookies;
    private final SessionService sessionService;
    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> login(
            @Valid @RequestBody LoginRequest request,
            ServerHttpResponse response
    ) {
        return sessionService.login(request).map(loginResponse -> {

            // access token
            ResponseCookie accessToken = ResponseCookie.from("access_token", loginResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(secureCookies)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(loginResponse.getExpiresIn())
                    .build();

            // refresh token
            ResponseCookie refreshToken = ResponseCookie.from("refresh_token", loginResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(secureCookies)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(loginResponse.getRefreshExpiresIn())
                    .build();

            response.addCookie(accessToken);
            response.addCookie(refreshToken);
            return ResponseEntity.ok("Login successful");
        });
    }
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerHttpResponse response){
        ResponseCookie accessCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(secureCookies) // true si es https
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(secureCookies) // true si es https
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
        return Mono.just(ResponseEntity.noContent().build());
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserAuthResponse>> getSessionInfo(@CookieValue(value = "access_token", required = false) String accesToken){
        if(accesToken == null || accesToken.isEmpty()){
            log.error("No acces token provided");
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return sessionService.getUserInfo(accesToken)
                .map(ResponseEntity::ok)
                .onErrorResume(e ->{
                    log.error("Error obtaining the user info on session", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
                });
    }
}