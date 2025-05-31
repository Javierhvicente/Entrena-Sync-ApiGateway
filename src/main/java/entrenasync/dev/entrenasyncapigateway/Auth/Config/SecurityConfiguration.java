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
                        .pathMatchers(HttpMethod.PUT,"/keycloak/user/**").hasRole("admin")
                        .pathMatchers(HttpMethod.GET, "/keycloak/user/**").hasRole("admin")
                        .pathMatchers(HttpMethod.POST, "/keycloak/user").permitAll()
                        .pathMatchers("/workers/**").hasRole("admin")
                        .pathMatchers("/Workouts/**").hasAnyRole("admin", "Client")
                        .pathMatchers("/Exercises/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/Exercises").permitAll()
                        .pathMatchers( HttpMethod.PUT,"/Clients").hasAnyRole("admin", "Client")
                        .pathMatchers(HttpMethod.POST, "/Clients").permitAll()
                        .pathMatchers( HttpMethod.PATCH,"/Clients/**").hasAnyRole("admin", "Client")
                        .pathMatchers( HttpMethod.GET,"/Clients/user/{id}").hasAnyRole("admin", "Client")
                        .pathMatchers( HttpMethod.GET,"/Clients").hasRole("admin")
                        .pathMatchers( HttpMethod.GET,"/Clients/{id}").hasRole("admin")
                        .pathMatchers( HttpMethod.DELETE,"/Clients/**").hasAnyRole("admin", "Client")
                        .pathMatchers("/storage/**").permitAll()
                        .pathMatchers("session/**").permitAll()
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
