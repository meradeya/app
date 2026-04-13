package com.meradeya.app.security;

import com.meradeya.app.domain.entity.User;
import com.meradeya.app.domain.entity.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Immutable authenticated principal used by Spring Security.
 *
 * <p>The principal is a security projection of {@link User}; it intentionally contains only fields
 * required for authentication and authorization decisions.
 *
 * @apiNote This type avoids leaking full JPA entities into the security context.
 */
@Getter
public class AppUserPrincipal implements UserDetails {

  private static final List<GrantedAuthority> DEFAULT_AUTHORITIES =
      List.of(new SimpleGrantedAuthority("ROLE_USER"));

  private final UUID userId;
  private final String email;
  private final String passwordHash;
  private final UserStatus status;

  private AppUserPrincipal(UUID userId, String email, String passwordHash, UserStatus status) {
    this.userId = userId;
    this.email = email;
    this.passwordHash = passwordHash;
    this.status = status;
  }

  /**
   * Creates a principal snapshot from a persisted user.
   *
   * @param user source user entity
   * @return corresponding security principal
   * @implSpec Captures the user state at load time; callers should reload per request to observe
   * status changes.
   */
  public static AppUserPrincipal from(User user) {
    return new AppUserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(),
        user.getStatus());
  }

  /**
   * Returns authorities granted to the current principal.
   *
   * @return non-null authority collection
   * @implSpec A single {@code ROLE_USER} authority is currently assigned.
   */
  @Override
  public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
    return DEFAULT_AUTHORITIES; // TODO: Implement RBAC (#5)
  }

  /**
   * Returns the encoded password hash used for credential checks.
   *
   * @return encoded password hash
   */
  @Override
  public @NonNull String getPassword() {
    return passwordHash;
  }

  /**
   * Returns the principal username value.
   *
   * @return principal username used by Spring Security
   * @implNote The current implementation uses email as the username field.
   */
  @Override
  public @NonNull String getUsername() {
    return email;
  }

  /**
   * Indicates whether the account is not locked.
   *
   * @return {@code true} unless the user is suspended
   * @implSpec Suspended users are treated as locked for authentication purposes.
   */
  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.SUSPENDED;
  }


  /**
   * Indicates whether the account is enabled.
   *
   * @return {@code true} unless the user is deleted
   * @implSpec Deleted users are rejected by authentication checks even if a token still exists.
   */
  @Override
  public boolean isEnabled() {
    return status != UserStatus.DELETED;
  }

}
