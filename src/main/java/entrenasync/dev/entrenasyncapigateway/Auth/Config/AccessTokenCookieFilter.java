package entrenasync.dev.entrenasyncapigateway.Auth.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
@Slf4j
@Component
public class AccessTokenCookieFilter implements WebFilter, Ordered {
    private static final String TOKEN_COOKIE_NAME = "access_token";
    @Override
    public int getOrder(){
        return -100;
    }
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var cookie = exchange.getRequest().getCookies().getFirst(TOKEN_COOKIE_NAME);
        if(cookie != null){
            log.info("cookies: " + cookie.getValue());
            ServerHttpRequest mutateRequest =  exchange.getRequest().mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + cookie.getValue())
                    .build();
            return chain.filter(exchange.mutate().request(mutateRequest).build());
        }
        log.info("hey");
        return chain.filter(exchange);
    }
}
