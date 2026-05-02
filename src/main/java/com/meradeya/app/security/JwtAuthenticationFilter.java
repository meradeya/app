package com.meradeya.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests by parsing bearer JWT tokens and populating Spring Security context.
 *
 * <p>Processing model:
 * <ol>
 *   <li>Extract bearer token from the {@code Authorization} header.</li>
 *   <li>Verify JWT signature with the configured HMAC key.</li>
 *   <li>Read user id from JWT subject claim.</li>
 *   <li>Load current user snapshot and build an authenticated principal.</li>
 *   <li>Store authentication in {@link SecurityContextHolder} for downstream authorization.</li>
 * </ol>
 *
 * <p>The filter is intentionally best-effort: invalid, missing, or non-resolvable tokens do not
 * short-circuit the chain. Authorization is left to endpoint security rules.
 *
 * @apiNote This filter performs authentication only. Access control decisions are enforced by
 * Spring Security request/method authorization configured elsewhere.
 * @implNote User state checks ({@code isEnabled}/{@code isAccountNonLocked}) are evaluated on every
 * request so suspended/deleted accounts stop authenticating even with previously issued tokens.
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AppUserDetailsService appUserDetailsService;
  private final JwtParser jwtParser;

  public JwtAuthenticationFilter(JwtParser jwtParser,
      AppUserDetailsService appUserDetailsService) {
    this.appUserDetailsService = appUserDetailsService;
    this.jwtParser = jwtParser;
  }

  /**
   * Executes JWT extraction, validation, and principal resolution for a request.
   *
   * <p>Detailed behavior:
   * <ol>
   *   <li>If no bearer token is present, the method leaves the security context unchanged.</li>
   *   <li>If a token is present, signature/claims are validated using the configured HMAC secret.</li>
   *   <li>If the token subject can be parsed and no authentication is already set, user details are loaded.</li>
   *   <li>If the resolved account is inactive (disabled or locked), authentication is skipped.</li>
   *   <li>Otherwise, a fully-authenticated token is stored in the current security context.</li>
   * </ol>
   *
   * @param request     current HTTP request
   * @param response    current HTTP response
   * @param filterChain remaining chain
   * @throws ServletException when the servlet container reports a filter failure
   * @throws IOException      when request/response IO fails
   * @implSpec On successful token validation, an authenticated
   * {@link UsernamePasswordAuthenticationToken} is stored in
   * {@link SecurityContextHolder#getContext()}.
   * @implNote Any exception during token parsing/verification/user loading is swallowed and logged
   * as warning; the request continues as unauthenticated and downstream access rules decide
   * outcome.
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    try {
      String jwt = parseJwt(request);
      if (jwt != null) {
        Jws<Claims> claimsJws = jwtParser.parseSignedClaims(jwt);

        String userId = claimsJws.getPayload().getSubject();

        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          UserDetails userDetails = appUserDetailsService.loadByUserId(UUID.fromString(userId));
          if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()) {
            log.warn("Skipping authentication for inactive user subject={}", userId);
            filterChain.doFilter(request, response);
            return;
          }

          // Build stateless authentication from trusted JWT + current user snapshot.
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());

          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } else if (userId == null) {
          log.warn("JWT subject is null, skipping authentication");
        }
      }
    } catch (Exception e) {
      log.warn("Cannot set user authentication: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extracts the bearer token from the {@code Authorization} header.
   *
   * @param request source request
   * @return raw JWT token when present, otherwise {@code null}
   * @implSpec Only the {@code Bearer <token>} scheme is supported.
   * @implNote This method performs format extraction only; cryptographic validation is handled by
   * {@link #doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)}.
   */
  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }

}
