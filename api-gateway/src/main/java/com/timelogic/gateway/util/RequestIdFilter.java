package com.timelogic.gateway.util;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class RequestIdFilter implements GlobalFilter, Ordered {

    public static final String HEADER = "X-Request-Id";
    public static final String ATTR   = "requestId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String existing = exchange.getRequest().getHeaders().getFirst(HEADER);
        String reqId = (existing == null || existing.isBlank())
                ? UUID.randomUUID().toString()
                : existing;

        exchange.getAttributes().put(ATTR, reqId);

        var mutated = exchange.getRequest().mutate()
                .headers(h -> h.set(HEADER, reqId)) // set: sobrescribe valores previos
                .build();

        // Opcional: MDC local (no garantiza propagación entre hilos sin puente Reactor)
        try (var ignored = MDC.putCloseable(HEADER, reqId)) {
            return chain.filter(exchange.mutate().request(mutated).build());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
