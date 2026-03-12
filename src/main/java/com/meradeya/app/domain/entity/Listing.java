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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "listings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Listing {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @Setter(AccessLevel.PACKAGE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "seller_id")
  private User seller;

  @NotNull
  @Setter(AccessLevel.PACKAGE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @NotBlank
  @Size(max = 200)
  @Setter
  @Column
  private String title;

  @Lob
  @Setter
  @Column(columnDefinition = "text")
  private String description;

  @NotNull
  @Digits(integer = 10, fraction = 2)
  @Setter
  @Column
  private BigDecimal price;

  @NotBlank
  @Size(min = 3, max = 3)
  @Pattern(regexp = "[A-Z]{3}")
  @Setter
  @Column
  private String currency = "MDL";

  @Enumerated(EnumType.STRING)
  @NotNull
  @Setter
  @Column(name = "condition")
  private ListingCondition condition;

  @Enumerated(EnumType.STRING)
  @NotNull
  @Setter
  @Column
  private ListingStatus status = ListingStatus.DRAFT;

  @Size(max = 200)
  @Setter
  @Column
  private String location;

  @JdbcTypeCode(SqlTypes.JSON)
  @NotNull
  @Setter
  @Column
  private Map<String, Object> attributes = new HashMap<>();

  @NotNull
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @NotNull
  @Column(name = "updated_at")
  private Instant updatedAt;

  @Version
  private long version;

  @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Getter(AccessLevel.NONE)
  private List<ListingPhoto> photos = new ArrayList<>();

  public List<ListingPhoto> getPhotos() {
    return Collections.unmodifiableList(photos);
  }

  public void addPhoto(ListingPhoto photo) {
    photos.add(photo);
    photo.setListing(this);
  }

  public void removePhoto(ListingPhoto photo) {
    photos.remove(photo);
    photo.setListing(null);
  }

  @PrePersist
  void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
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
    if (!(o instanceof Listing that)) {
      return false;
    }
    return id != null && id.equals(that.id);
  }

}
