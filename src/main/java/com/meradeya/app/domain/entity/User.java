package com.meradeya.app.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Email
  @NotBlank
  @Size(max = 255)
  @Setter
  @Column
  private String email;

  @NotBlank
  @Size(max = 255)
  @Setter
  @Column(name = "password_hash")
  private String passwordHash;

  @Setter
  @Column(name = "email_verified")
  private boolean emailVerified = false;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Setter
  @Column
  private UserStatus status = UserStatus.ACTIVE;

  @NotNull
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @NotNull
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Version
  private long version;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private UserProfile profile;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Getter(AccessLevel.NONE)
  private List<RefreshToken> refreshTokens = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  @Getter(AccessLevel.NONE)
  private List<AuthToken> authTokens = new ArrayList<>();

  @OneToMany(mappedBy = "seller", fetch = FetchType.LAZY)
  @Getter(AccessLevel.NONE)
  private List<Listing> listings = new ArrayList<>();

  public List<RefreshToken> getRefreshTokens() {
    return Collections.unmodifiableList(refreshTokens);
  }

  public List<AuthToken> getAuthTokens() {
    return Collections.unmodifiableList(authTokens);
  }

  public List<Listing> getListings() {
    return Collections.unmodifiableList(listings);
  }

  public void addRefreshToken(RefreshToken token) {
    refreshTokens.add(token);
    token.setUser(this);
  }

  public void removeRefreshToken(RefreshToken token) {
    refreshTokens.remove(token);
    token.setUser(null);
  }

  public void addAuthToken(AuthToken token) {
    authTokens.add(token);
    token.setUser(this);
  }

  public void removeAuthToken(AuthToken token) {
    authTokens.remove(token);
    token.setUser(null);
  }

  public void addListing(Listing listing) {
    listings.add(listing);
    listing.setSeller(this);
  }

  public void removeListing(Listing listing) {
    listings.remove(listing);
    listing.setSeller(null);
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
    if (email != null) {
      email = email.toLowerCase();
    }
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
    if (email != null) {
      email = email.toLowerCase();
    }
  }

}
