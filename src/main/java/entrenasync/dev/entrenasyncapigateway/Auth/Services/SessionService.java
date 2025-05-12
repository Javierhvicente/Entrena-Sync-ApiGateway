package entrenasync.dev.entrenasyncapigateway.Auth.Services;

import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.UserAuthResponse;
import entrenasync.dev.entrenasyncapigateway.User.Dto.UserResponse;
import reactor.core.publisher.Mono;

public interface SessionService {
    Mono<LoginResponse> login(LoginRequest request);
    Mono<UserAuthResponse> getUserInfo(String accesToken);
}
