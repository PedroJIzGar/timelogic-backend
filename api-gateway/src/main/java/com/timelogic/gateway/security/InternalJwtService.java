package com.timelogic.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class InternalJwtService {

  @Value("${auth.internal.issuer}")   private String issuer;
  @Value("${auth.internal.audience}") private String audience;
  @Value("${auth.internal.ttl-seconds:600}") private long ttlSeconds;
  @Value("${auth.internal.secret}")   private String secret;

  private SecretKey key;

  @PostConstruct
  void init() {
    byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      throw new IllegalStateException("auth.internal.secret must be at least 32 bytes for HS256");
    }
    this.key = Keys.hmacShaKeyFor(bytes);
  }

  public String issue(String subject, String email, String name, List<String> roles) {
    Instant now = Instant.now();
    return Jwts.builder()
        .id(UUID.randomUUID().toString())
        .issuer(issuer)
        .audience().add(audience).and()
        .subject(subject != null ? subject : email)
        .claim("email", email)
        .claim("name", name)
        .claim("roles", roles)
        .issuedAt(Date.from(now))
        .notBefore(Date.from(now.minusSeconds(5)))
        .expiration(Date.from(now.plusSeconds(ttlSeconds)))
        .signWith(key) // ← sin alg, desaparecen las deprecations
        .compact();
  }
}
