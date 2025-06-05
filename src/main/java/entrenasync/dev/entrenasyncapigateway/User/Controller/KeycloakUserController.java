package entrenasync.dev.entrenasyncapigateway.User.Controller;

import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserUpdateRequest;
import entrenasync.dev.entrenasyncapigateway.User.Services.KeycloakUserService;
import entrenasync.dev.entrenasyncapigateway.Utils.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/keycloak/user")
public class KeycloakUserController {

    private final KeycloakUserService keycloakUserService;

    public KeycloakUserController(KeycloakUserService keycloakUserService) {
        this.keycloakUserService = keycloakUserService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type) {

        PagedResponse<UserResponse> users = keycloakUserService.getAllUsers(page, size, type);
        return ResponseEntity.ok(users);
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username){
        return ResponseEntity.ok(keycloakUserService.getUserByUsername(username));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest){
        return ResponseEntity.ok(keycloakUserService.createUser(userRequest));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest userRequest){
        return ResponseEntity.ok(keycloakUserService.updateUser(userRequest, userId));
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username){
        keycloakUserService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
