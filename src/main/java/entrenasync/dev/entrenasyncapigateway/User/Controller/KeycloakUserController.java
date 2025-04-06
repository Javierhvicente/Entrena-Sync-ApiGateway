package entrenasync.dev.entrenasyncapigateway.User.Controller;

import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import entrenasync.dev.entrenasyncapigateway.User.Services.IKeycloakUserService;
import jakarta.validation.Valid;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasRole;

@RestController
@RequestMapping("/keycloak/user")
public class KeycloakUserController {

    private IKeycloakUserService keycloakUserService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(keycloakUserService.getAllUsers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserRepresentation> getUserByUsername(@PathVariable String username){
        return ResponseEntity.ok(keycloakUserService.getUserByUsername(username));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity<UserRepresentation> createUser(@RequestBody @Valid UserRequest userRequest){
        return ResponseEntity.ok(keycloakUserService.createUser(userRequest));
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public ResponseEntity<UserRepresentation> updateUser(@PathVariable String userId, @RequestBody @Valid UserRequest userRequest){
        return ResponseEntity.ok(keycloakUserService.updateUser(userRequest, userId));
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteUser(@PathVariable String username){
        keycloakUserService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
