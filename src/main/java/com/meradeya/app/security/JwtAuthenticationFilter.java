package com.meradeya.app.security;

import com.meradeya.app.prop.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final AuthProperties authProperties;
  private final AppUserDetailsService appUserDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    try {
      String jwt = parseJwt(request);
      if (jwt != null) {
        SecretKey key = Keys.hmacShaKeyFor(
            authProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8));

        Jws<Claims> claimsJws = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(jwt);

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
        }
      }
    } catch (Exception e) {
      log.warn("Cannot set user authentication: {}", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }

}
