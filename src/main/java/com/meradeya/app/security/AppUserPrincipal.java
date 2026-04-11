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

  public static AppUserPrincipal from(User user) {
    return new AppUserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash(),
        user.getStatus());
  }

  @Override
  public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
    return DEFAULT_AUTHORITIES;
  }

  @Override
  public @NonNull String getPassword() {
    return passwordHash;
  }

  @Override
  public @NonNull String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonLocked() {
    return status != UserStatus.SUSPENDED;
  }


  @Override
  public boolean isEnabled() {
    return status != UserStatus.DELETED;
  }
}


