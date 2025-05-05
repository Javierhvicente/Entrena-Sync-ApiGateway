package entrenasync.dev.entrenasyncapigateway.Auth.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak")
@Getter
@Setter
public class KeycloakProperties {
    private String clientId;
    private String clientSecret;
    private String realm;
    private String url;
    private String grantType;
    private String redirectUri;
    private String username;
    private String password;
}
