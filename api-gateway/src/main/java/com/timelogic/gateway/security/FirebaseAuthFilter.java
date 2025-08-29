package com.timelogic.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class FirebaseAuthFilter implements GlobalFilter, Ordered {

    private final FirebaseTokenVerifier verifier;
    private final AntPathMatcher matcher = new AntPathMatcher();

    // ajusta si tienes paths públicos
    private boolean isPublic(String path) {
        return matcher.match("/actuator/**", path)
            || matcher.match("/swagger-ui/**", path)
            || matcher.match("/v3/api-docs/**", path)
            || matcher.match("/public/**", path);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var req = exchange.getRequest();

        if (HttpMethod.OPTIONS.equals(req.getMethod())) {
            return chain.filter(exchange); // CORS preflight fuera
        }
        if (isPublic(req.getPath().value())) {
            return chain.filter(exchange);
        }

        String auth = req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing Authorization: Bearer <token>");
        }

        String idToken = auth.substring(7);

        return Mono.fromCallable(() -> verifier.verify(idToken))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(decoded -> {
                    // comparte el token con el siguiente filtro
                    exchange.getAttributes().put("firebaseToken", decoded);

                    var mutated = req.mutate().headers(h -> {
                        h.set("X-User-Id", notNull(decoded.getUid()));
                        h.set("X-User-Email", notNull(decoded.getEmail()));
                        h.set("X-User-Name", notNull(decoded.getName()));
                        Object roles = decoded.getClaims().get("roles");
                        h.set("X-User-Roles", roles == null ? "" : roles.toString());
                    }).build();

                    return chain.filter(exchange.mutate().request(mutated).build());
                })
                .onErrorResume(ex -> unauthorized(exchange, "Invalid or expired token"));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var resp = exchange.getResponse();
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
        var data = resp.bufferFactory()
                .wrap(("{\"error\":\"unauthorized\",\"message\":\"" + message + "\"}").getBytes());
        return resp.writeWith(Mono.just(data));
    }

    private static String notNull(String v) { return v == null ? "" : v; }

    @Override
    public int getOrder() {
        // Debe ir antes del relay interno
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
