package com.timelogic.gateway.security;

import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InternalJwtRelayFilter implements GlobalFilter, Ordered {

    private final InternalJwtService tokens;
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Value("${auth.internal.enabled:true}")
    private boolean enabled;

    private boolean isProtectedApi(String path) {
        return matcher.match("/api/**", path);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) return chain.filter(exchange);

        String path = exchange.getRequest().getPath().value();
        if (!isProtectedApi(path)) return chain.filter(exchange);

        // 1) Preferimos el token de Firebase depositado por el filtro anterior
        FirebaseToken t = exchange.getAttribute("firebaseToken");
        if (t != null) {
            String sub   = t.getUid();
            String email = t.getEmail();
            String name  = t.getName();
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) t.getClaims().getOrDefault("roles", Collections.emptyList());

            String internal = tokens.issue(sub, email, name, roles);
            var mutated = exchange.mutate()
                    .request(req -> req.headers(h -> {
                        h.set(HttpHeaders.AUTHORIZATION, "Bearer " + internal);
                        h.set("X-User-Email", notNull(email));
                        h.set("X-User-Name",  notNull(name));
                        h.set("X-User-Roles", String.join(",", roles));
                    }))
                    .build();
            return chain.filter(mutated);
        }

        // 2) Fallback a SecurityContext (por si decides poblar Authentication en el futuro)
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .defaultIfEmpty(null)
                .flatMap(auth -> {
                    if (auth == null || !auth.isAuthenticated()) {
                        return chain.filter(exchange);
                    }
                    String sub   = auth.getName();
                    String email = extractEmail(auth);
                    String name  = extractName(auth);
                    List<String> roles = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .map(a -> a.replace("ROLE_", ""))
                            .collect(Collectors.toList());

                    String internal = tokens.issue(sub, email, name, roles);
                    var mutated = exchange.mutate()
                            .request(req -> req.headers(h -> {
                                h.set(HttpHeaders.AUTHORIZATION, "Bearer " + internal);
                                h.set("X-User-Email", notNull(email));
                                h.set("X-User-Name",  notNull(name));
                                h.set("X-User-Roles", String.join(",", roles));
                            }))
                            .build();
                    return chain.filter(mutated);
                });
    }

    private static String extractEmail(Authentication auth) {
        Object p = auth.getPrincipal();
        try { return (String) p.getClass().getMethod("getEmail").invoke(p); }
        catch (Exception ignore) { return auth.getName(); }
    }

    private static String extractName(Authentication auth) {
        Object p = auth.getPrincipal();
        try { return (String) p.getClass().getMethod("getName").invoke(p); }
        catch (Exception ignore) { return null; }
    }

    private static String notNull(String v) { return v == null ? "" : v; }

    @Override
    public int getOrder() {
        // después de FirebaseAuthFilter y antes de filtros de routing
        return Ordered.LOWEST_PRECEDENCE - 10;
    }
}
