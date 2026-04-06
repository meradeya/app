package com.meradeya.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserProfile {

  @Id
  @Column(name = "user_id")
  private UUID userId;

  @MapsId
  @NotNull
  @Setter
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @NotBlank
  @Size(max = 100)
  @Setter
  @Column(name = "display_name")
  private String displayName;

  @Size(max = 500)
  @Setter
  @Column(name = "avatar_url")
  private String avatarUrl;

  @Size(max = 200)
  @Setter
  @Column
  private String location;

  @Lob
  @Setter
  @Column(columnDefinition = "text")
  private String bio;

  @NotNull
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Version
  private long version;

  
  public UserProfile(User user, String displayName) {
    this.user = user;
    this.displayName = displayName;
  }


  @PrePersist
  @PreUpdate
  void touchTimestamp() {
    updatedAt = Instant.now();
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UserProfile that)) {
      return false;
    }
    return userId != null && userId.equals(that.userId);
  }

}
