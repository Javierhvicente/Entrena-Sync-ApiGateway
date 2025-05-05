package entrenasync.dev.entrenasyncapigateway.Auth;

import entrenasync.dev.entrenasyncapigateway.Auth.Config.KeycloakProperties;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakProvider {

    private final KeycloakProperties properties;

    public KeycloakProvider(KeycloakProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RealmResource realmResource() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(properties.getUrl())
                .realm(properties.getRealm())
                .clientId(properties.getClientId())
                .clientSecret(properties.getClientSecret())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
                .build();

        return keycloak.realm(properties.getRealm());
    }

    @Bean
    public UsersResource usersResource(RealmResource realmResource) {
        return realmResource.users();
    }
}
