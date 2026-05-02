package com.meradeya.app.config;

import com.meradeya.app.prop.AuthProperties;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

  @Bean
  public JwtParser jwtParser(AuthProperties authProperties) {
    SecretKey secretKey = Keys.hmacShaKeyFor(
        authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8)
    );

    return Jwts.parser()
        .verifyWith(secretKey)
        .build();
  }

}
