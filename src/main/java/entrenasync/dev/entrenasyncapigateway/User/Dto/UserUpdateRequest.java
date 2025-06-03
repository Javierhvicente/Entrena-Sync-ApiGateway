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
public class UserUpdateRequest {
    @Email(message = "The email must be a valid email address")
    private String email;

    private String firstName;

    private String lastName;

    private String password;

    private String passwordConfirmation;

    private List<String> roles;
}
