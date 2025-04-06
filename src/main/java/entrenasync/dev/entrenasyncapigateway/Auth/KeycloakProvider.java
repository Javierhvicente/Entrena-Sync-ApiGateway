package entrenasync.dev.entrenasyncapigateway.Auth;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakProvider {
    public static RealmResource getRealmResource(){
         Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:9090/")
                .realm("Entrena-Sync-realm")
                .clientId("entrena-sync-client")
                .clientSecret("7nqgYCDk2HR3J5k8h9EhnJt9AO7Qng8w")
                .username("deventrenasync")
                .password("EntrenaSync#Test")
                .resteasyClient(new ResteasyClientBuilderImpl().connectionPoolSize(10).build())
                .build();
         return keycloak.realm("Entrena-Sync-realm");
    }

    public static UsersResource getUserResource(){
       RealmResource realmResource = getRealmResource();
       return realmResource.users();
    }
}
