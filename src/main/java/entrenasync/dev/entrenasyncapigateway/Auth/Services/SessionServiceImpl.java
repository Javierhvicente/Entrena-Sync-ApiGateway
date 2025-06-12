package entrenasync.dev.entrenasyncapigateway.Auth.Services;

import com.fasterxml.jackson.databind.JsonNode;
import entrenasync.dev.entrenasyncapigateway.Auth.Config.KeycloakProperties;
import entrenasync.dev.entrenasyncapigateway.Auth.Exceptions.SessionExceptions;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.UserAuthResponse;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

    private final WebClient webClient;
    private final KeycloakProperties keycloakProperties;

    @Autowired
    public SessionServiceImpl(WebClient.Builder webClientBuilder, KeycloakProperties keycloakProperties) {
        this.keycloakProperties = keycloakProperties;
        this.webClient = webClientBuilder
                .baseUrl(keycloakProperties.getUrl())
                .build();
    }
    @Override
    public Mono<LoginResponse> login(LoginRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        log.info("request user: " + request.getUsername());
        log.info("request user pas: " + request.getPassword());
        return webClient.post()
                .uri("/protocol/openid-connect/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                        .fromFormData("grant_type", "password")
                        .with("client_id", keycloakProperties.getClientId())
                        .with("username", request.getUsername())
                        .with("password", request.getPassword())
                        .with("client_secret", keycloakProperties.getClientSecret())
                        .with("scope", "openid")
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

    @Override
    public Mono<UserAuthResponse> getUserInfo(String accessToken) {
        return webClient.get()
                .uri("/protocol/openid-connect/userinfo")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.warn("Error when reading user token.");
                            return Mono.error(new SessionExceptions.noTokenOnRequest());
                        })
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.warn("Error when reading user token, server error");
                            return Mono.error(new SessionExceptions.noTokenOnRequest());
                        }))
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    DecodedJWT jwt = JWT.decode(accessToken);
                    List<String> roles = new ArrayList<>();
                    Map<String, Object> realmAccess = jwt.getClaim("realm_access").asMap();
                    if (realmAccess != null && realmAccess.containsKey("roles")) {
                        roles = (List<String>) realmAccess.get("roles");
                    }

                    return new UserAuthResponse(
                            json.path("sub").asText(),
                            json.path("preferred_username").asText(),
                            json.path("email").asText(),
                            json.path("given_name").asText(),
                            json.path("family_name").asText(),
                            json.path("Type").asText(),
                            roles
                    );
                });
    }

}
