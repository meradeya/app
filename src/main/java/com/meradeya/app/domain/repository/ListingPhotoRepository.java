package com.meradeya.app.domain.repository;

import com.meradeya.app.domain.entity.ListingPhoto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, UUID> {

  List<ListingPhoto> findByListingId(UUID listingId);

  /**
   * Count photos to enforce the per-listing photo limit.
   */
  long countByListingId(UUID listingId);
}
