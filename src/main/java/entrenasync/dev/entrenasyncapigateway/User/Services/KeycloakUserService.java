package entrenasync.dev.entrenasyncapigateway.User.Services;

import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;

import java.util.List;
public interface KeycloakUserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserByUsername(String username);
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(UserRequest userRequest, String userId);
    void deleteUser(String userId);
}
