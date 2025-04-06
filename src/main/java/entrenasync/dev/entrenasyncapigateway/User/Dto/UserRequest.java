package entrenasync.dev.entrenasyncapigateway.User.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "The email is required")
    @Email(message = "The email must be a valid email address")
    private String email;

    @NotBlank(message = "The first name is mandatory")
    private String firstName;

    @NotBlank(message = "The last name")
    private String lastName;

    @NotBlank(message = "The password is required")
    @Size(min = 8, message = "The password must have 8 characters at least")
    private String password;

    @NotBlank(message = "The password confirmation is required")
    @Size(min = 8, message = "The password confirmation must match the password")
    private String passwordConfirmation;

    private List<String> roles;
}
