package entrenasync.dev.entrenasyncapigateway.Auth.Services;

import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import reactor.core.publisher.Mono;

public interface SessionService {
    Mono<LoginResponse> login(LoginRequest request);
}
