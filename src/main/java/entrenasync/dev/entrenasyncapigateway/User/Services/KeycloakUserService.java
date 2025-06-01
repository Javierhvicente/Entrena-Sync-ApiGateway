package entrenasync.dev.entrenasyncapigateway.User.Services;

import entrenasync.dev.entrenasyncapigateway.User.Dto.UserRequest;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserUpdateRequest;
import entrenasync.dev.entrenasyncapigateway.Utils.PagedResponse;

import java.util.List;
public interface KeycloakUserService {
    PagedResponse<UserResponse> getAllUsers(int page, int size);
    UserResponse getUserByUsername(String username);
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(UserUpdateRequest userRequest, String userId);
    void deleteUser(String userId);
}
