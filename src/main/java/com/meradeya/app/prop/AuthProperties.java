package com.meradeya.app.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

  private String jwtSecret;
  private long accessTokenExpirySeconds;
  private long refreshTokenExpirySeconds;
  private long emailVerifyTokenExpirySeconds;
  private long passwordResetTokenExpirySeconds;

}

