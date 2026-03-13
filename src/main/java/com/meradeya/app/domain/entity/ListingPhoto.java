package com.meradeya.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Min;
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
@Table(name = "listing_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ListingPhoto {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @Setter(AccessLevel.PACKAGE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "listing_id")
  private Listing listing;

  @NotBlank
  @Size(max = 500)
  @Setter
  @Column
  private String url;

  @Min(0)
  @Setter
  @Column(name = "display_order")
  private short displayOrder = 0;

  @NotNull
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Version
  private long version;

  @PrePersist
  void onCreate() {
    createdAt = Instant.now();
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
    if (!(o instanceof ListingPhoto that)) {
      return false;
    }
    return id != null && id.equals(that.id);
  }

}
