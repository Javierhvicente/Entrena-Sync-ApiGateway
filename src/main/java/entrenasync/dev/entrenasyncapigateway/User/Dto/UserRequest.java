package entrenasync.dev.entrenasyncapigateway.User.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
    @NotBlank(message = "The username is required")
    @Size(min = 3, max = 50, message = "The username must be between 3 and 50 characters")
    private String username;

    @Email(message = "The email must be a valid email address")
    private String email;

    @NotBlank(message = "The first name is mandatory")
    private String firstName;

    @NotBlank(message = "The last name")
    private String lastName;

    @NotNull(message = "The user type is required")
    private Type type;

    @NotBlank(message = "The password is required")
    private String password;

    @NotBlank(message = "The password confirmation is required")
    private String passwordConfirmation;

    private List<String> roles;

    public enum Type {
        admin,
        client,
        worker,
    }
}
