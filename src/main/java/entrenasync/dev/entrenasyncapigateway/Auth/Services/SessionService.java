package entrenasync.dev.entrenasyncapigateway.Auth.Services;

import entrenasync.dev.entrenasyncapigateway.Auth.Config.KeycloakProperties;
import entrenasync.dev.entrenasyncapigateway.Auth.Exception.SessionExceptions;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.security.auth.login.LoginException;

@Service
public class SessionService implements ISessionService {

    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    @Autowired
    public SessionService(WebClient.Builder webClientBuilder, KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.webClient = webClientBuilder
                .baseUrl(keycloakProperties.getUrl())
                .build();
    }
    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        return webClient.post()
                .uri("/protocol/openid-connect/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData("grant_type", "password")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("username", request.getUsername())
                        .with("password", request.getPassword())
                        .with("client_secret", keycloakProperties.getClientSecret())
                )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(
                                new SessionExceptions.loginBadCredentialsException(
                                        request.getUsername(),
                                        request.getPassword()
                                )
                        )
                )
                .bodyToMono(LoginResponse.class);

    }

}
