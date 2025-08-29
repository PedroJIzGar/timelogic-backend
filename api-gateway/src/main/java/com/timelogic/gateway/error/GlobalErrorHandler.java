package com.timelogic.gateway.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    HttpStatusCode status = HttpStatus.INTERNAL_SERVER_ERROR;
    String message = ex.getMessage();

    if (ex instanceof ResponseStatusException rse) {
      status = rse.getStatusCode();
      if (rse.getReason() != null) message = rse.getReason();
    } else if (ex instanceof ServerWebInputException) {
      status = HttpStatus.BAD_REQUEST;
    } else if (ex instanceof AccessDeniedException) {
      status = HttpStatus.FORBIDDEN;
    }

    String reason =
        (status instanceof HttpStatus hs) ? hs.getReasonPhrase()
                                          : HttpStatus.valueOf(status.value()).getReasonPhrase();

    log.error("Request failed: {} {} -> {} {}",
        exchange.getRequest().getMethod(),
        exchange.getRequest().getPath(),
        status.value(), reason, ex);

    var resp = exchange.getResponse();
    resp.setStatusCode(status);
    resp.getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
    resp.getHeaders().set(HttpHeaders.CACHE_CONTROL, "no-store");

    String body = """
      {"timestamp":"%s","status":%d,"error":"%s","message":%s,"path":"%s"}
      """.formatted(
        Instant.now(),
        status.value(),
        escape(reason),
        message == null ? "null" : "\"" + escape(message) + "\"",
        escape(exchange.getRequest().getPath().value())
      );

    var buf = resp.bufferFactory().wrap(body.getBytes());
    return resp.writeWith(Mono.just(buf));
  }

  private static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
