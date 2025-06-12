package entrenasync.dev.entrenasyncapigateway.Auth.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfiguration {

    @Bean("securityWebFilterChainEntrenaSync")
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.DELETE,"/keycloak/user/**").hasRole("admin")
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.PUT,"/keycloak/user/**").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.GET, "/keycloak/user/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/keycloak/user").permitAll()
                        .pathMatchers("/Workouts/**").hasAnyRole("admin", "Client")
                        .pathMatchers("/Exercises/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/Exercises").permitAll()
                        .pathMatchers( HttpMethod.PUT,"/Clients/{id}").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.POST, "/Clients").permitAll()
                        .pathMatchers( HttpMethod.PATCH,"/Clients/**").hasAnyRole("admin", "Client")
                        .pathMatchers( HttpMethod.GET,"/Clients/user/{id}").hasAnyRole("admin", "Client")
                        .pathMatchers( HttpMethod.GET,"/Clients").hasAnyRole("admin", "Worker")
                        .pathMatchers( HttpMethod.GET,"/Clients/{id}").hasRole("admin")
                        .pathMatchers( HttpMethod.DELETE,"/Clients/**").hasAnyRole("admin", "Client")
                        .pathMatchers("/storage/**").permitAll()
                        .pathMatchers(HttpMethod.GET,"/workers").permitAll()
                        .pathMatchers(HttpMethod.GET, "/workers/type/{id}").permitAll()
                        .pathMatchers(HttpMethod.GET, "/workers/{id}").permitAll()
                        .pathMatchers(HttpMethod.GET, "/workers/user/{id}").hasAnyRole("admin", "Worker")
                        .pathMatchers(HttpMethod.POST, "/workers").hasRole("admin")
                        .pathMatchers(HttpMethod.PUT, "/workers/{id}").hasAnyRole("admin", "Worker")
                        .pathMatchers(HttpMethod.DELETE, "/workers/{id}").hasRole("admin")
                        .pathMatchers(HttpMethod.GET, "session/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "session/**").permitAll()
                        .pathMatchers(HttpMethod.PUT, "session/**").permitAll()
                        .pathMatchers(HttpMethod.DELETE, "session/**").permitAll()
                        .pathMatchers(HttpMethod.PUT, "services").hasAnyRole("admin", "Worker")
                        .pathMatchers(HttpMethod.GET, "services").permitAll()
                        .pathMatchers(HttpMethod.GET, "services/{id}").permitAll()
                        .pathMatchers(HttpMethod.POST, "services").hasAnyRole("admin", "Worker")
                        .pathMatchers(HttpMethod.DELETE, "services/{id}").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.PUT, "services/plans").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.GET, "services/plans").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.GET, "services/plans/{id}").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.POST, "services/plans").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.DELETE, "services/plans/{id}").hasAnyRole("admin", "Client")
                        .pathMatchers("payments/**").permitAll()
                        .anyExchange().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint()))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwtSpec -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean("jwtAuthenticationConverterEntrenaSync")
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmConverter());
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @Bean("accessDeniedHandlerEntrenaSync")
    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            String message = "error: Acceso denegado, no tienes permisos para usar este servicio.";
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(message.getBytes())));
        };
    }

    @Bean("authenticationEntryPointEntrenaSync")
    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            String message = "error: No estás autenticado para acceder a este servicio.";
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(message.getBytes())));
        };
    }
}
