package com.meradeya.app.config;

import com.meradeya.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures HTTP security for the application.
 *
 * <p>The configuration is stateless and JWT-based: requests are authenticated through
 * {@link JwtAuthenticationFilter} and no server-side HTTP session is used.
 *
 * @apiNote Public endpoints are explicitly whitelisted; every other request requires
 * authentication.
 * @implNote Method security is enabled so endpoint-level rules can be complemented with
 * {@code @PreAuthorize}/{@code @PostAuthorize} checks.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Builds the application security filter chain.
   *
   * <p>The resulting chain is stateless and expects bearer JWT authentication for protected
   * routes.
   * Request authorization is evaluated after matcher rules and, for protected endpoints, after
   * {@link JwtAuthenticationFilter} attempts principal resolution.
   *
   * @param http mutable Spring Security HTTP configuration
   * @return configured {@link SecurityFilterChain}
   * @implSpec CSRF is disabled for stateless APIs, session creation policy is
   * {@link SessionCreationPolicy#STATELESS}, and JWT authentication runs before
   * {@link UsernamePasswordAuthenticationFilter}.
   * @implNote Public matcher list includes authentication lifecycle endpoints and operational
   * endpoints (Swagger/Actuator/error) needed without JWT.
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Auth endpoints are public because credentials are supplied in the payload.
            // Logout remains public so clients can revoke refresh tokens even with an expired access JWT.
            .requestMatchers(
                "/**/auth/register",
                "/**/auth/login",
                "/**/auth/refresh",
                "/**/auth/logout",
                "/**/auth/verify-email",
                "/**/auth/password-reset/request",
                "/**/auth/password-reset/confirm",
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/actuator/health",
                "/actuator/info",
                "/error"
            ).permitAll()
            .anyRequest()
            .authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  /**
   * Creates the password encoder used for password hashing and verification.
   *
   * @return BCrypt-based {@link PasswordEncoder}
   * @implNote Cost factor 12 is used as the default security/performance tradeoff.
   * @implSpec The returned encoder is used for both new password hashing and login-time password
   * verification.
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

}
