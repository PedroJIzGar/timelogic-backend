package com.timelogic.gateway.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.cloud.gateway.config.HttpClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Ajustes del HttpClient Netty usado por Spring Cloud Gateway:
 * timeouts razonables y compresión.
 */
@Configuration
public class HttpClientConfig {

  @Bean
  public HttpClientCustomizer httpClientCustomizer() {
    return httpClient -> httpClient
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
        .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS)))
        .compress(true);
  }
}
