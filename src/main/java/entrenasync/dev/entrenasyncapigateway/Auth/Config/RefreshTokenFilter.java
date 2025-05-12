package entrenasync.dev.entrenasyncapigateway.Auth.Config;

import com.fasterxml.jackson.databind.JsonNode;
import entrenasync.dev.entrenasyncapigateway.Auth.Config.KeycloakProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenFilter implements WebFilter, Ordered {

    private final KeycloakProperties keycloakProperties;

    private final WebClient.Builder webClientBuilder;

    @Override
    public int getOrder() {
        return -90;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        HttpCookie accessTokenCookie = exchange.getRequest().getCookies().getFirst("access_token");

        if (accessTokenCookie != null && isTokenExpired(accessTokenCookie.getValue())) {
            HttpCookie refreshTokenCookie = exchange.getRequest().getCookies().getFirst("refresh_token");

            if (refreshTokenCookie != null) {
                return refreshToken(refreshTokenCookie.getValue(), exchange.getResponse())
                        .flatMap(newAccessToken -> {
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken)
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        });
            }
        }

        return chain.filter(exchange);
    }

    private boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.warn("Error al parsear el token, asumiendo expirado: {}", e.getMessage());
            return true;
        }
    }

    private Mono<String> refreshToken(String refreshToken, ServerHttpResponse response) {
        WebClient webClient = webClientBuilder
                .baseUrl(keycloakProperties.getUrl())
                .build();

        return webClient.post()
                .uri("/protocol/openid-connect/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", refreshToken)
                        .with("client_id", keycloakProperties.getClientId())
                        .with("client_secret", keycloakProperties.getClientSecret())
                )
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(json -> {
                    String newAccessToken = json.get("access_token").asText();
                    String newRefreshToken = json.get("refresh_token").asText();
                    int expiresIn = json.get("expires_in").asInt();
                    int refreshExpiresIn = json.get("refresh_expires_in").asInt();

                    // Set new cookies
                    response.addCookie(ResponseCookie.from("access_token", newAccessToken)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Strict")
                            .path("/")
                            .maxAge(Duration.ofSeconds(expiresIn))
                            .build());

                    response.addCookie(ResponseCookie.from("refresh_token", newRefreshToken)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Strict")
                            .path("/")
                            .maxAge(Duration.ofSeconds(refreshExpiresIn))
                            .build());

                    log.info("Refresh token exitoso");
                    return newAccessToken;
                })
                .doOnError(err -> log.error("Error refrescando el token: {}", err.getMessage()));
    }
}

