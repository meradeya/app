package com.meradeya.app.config;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import com.meradeya.app.prop.PhotoStorageProperties;
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
  private final PhotoStorageProperties photoStorageProperties;

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
            // Auth lifecycle — credentials are in the payload, no JWT needed.
            // Logout is public so clients can revoke tokens even after JWT expiry.
            .requestMatchers(
                "/**/auth/register",
                "/**/auth/login",
                "/**/auth/refresh",
                "/**/auth/logout",
                "/**/auth/verify-email",
                "/**/auth/password-reset/request",
                "/**/auth/password-reset/confirm"
            ).permitAll()
            // Public listing discovery — ACTIVE listings only, no write access.
            // /listings/own is PROTECTED; it must be matched before the generic
            // GET /listings/{listingId} rule, or it would be accidentally permitted.
            .requestMatchers(GET, "/**/listings/own").authenticated()
            .requestMatchers(GET, 
                "/**/feed", 
                "/**/listings/{listingId}", 
                "/**/categories",
                photoStorageProperties.getMediaUrlRoot() + "/**"
            ).permitAll()
            .requestMatchers(POST, "/**/listings/search").permitAll()
            .requestMatchers(
                "/v3/api-docs/**",
                "/v3/api-docs.yaml",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/actuator/health",
                "/actuator/info",
                "/error"
            ).permitAll()
            // Everything else requires a valid JWT
            .anyRequest().authenticated()
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
