package entrenasync.dev.entrenasyncapigateway.Auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthResponse {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String type;
    private List<String> roles;
}
