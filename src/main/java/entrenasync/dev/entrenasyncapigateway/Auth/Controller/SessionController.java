package entrenasync.dev.entrenasyncapigateway.Auth.Controller;

import entrenasync.dev.entrenasyncapigateway.Auth.Services.ISessionService;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginRequest;
import entrenasync.dev.entrenasyncapigateway.Auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/session")
public class SessionController {
    private final ISessionService sessionService;

    public SessionController(ISessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest){
        return sessionService.login(loginRequest);
    }
}
