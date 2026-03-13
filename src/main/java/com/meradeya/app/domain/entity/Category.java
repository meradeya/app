package com.meradeya.app.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Setter(AccessLevel.PACKAGE)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Category parent;

  @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
  @Getter(AccessLevel.NONE)
  private List<Category> children = new ArrayList<>();

  @NotBlank
  @Size(max = 100)
  @Setter
  @Column
  private String name;

  @NotBlank
  @Size(max = 100)
  @Setter
  @Column
  private String slug;

  @Version
  private long version;

  @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
  @Getter(AccessLevel.NONE)
  private List<Listing> listings = new ArrayList<>();

  public List<Category> getChildren() {
    return Collections.unmodifiableList(children);
  }

  public List<Listing> getListings() {
    return Collections.unmodifiableList(listings);
  }

  public void addChild(Category child) {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(Category child) {
    children.remove(child);
    child.setParent(null);
  }

  public void addListing(Listing listing) {
    listings.add(listing);
    listing.setCategory(this);
  }

  public void removeListing(Listing listing) {
    listings.remove(listing);
    listing.setCategory(null);
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
    if (!(o instanceof Category that)) {
      return false;
    }
    return id != null && id.equals(that.id);
  }

}
