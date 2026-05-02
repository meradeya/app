package com.meradeya.app.domain.repository;

import com.meradeya.app.domain.entity.Listing;
import com.meradeya.app.domain.entity.ListingStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

  Page<Listing> findBySellerId(UUID sellerId, Pageable pageable);

  Page<Listing> findBySellerIdAndStatus(UUID sellerId, ListingStatus status, Pageable pageable);

  Page<Listing> findByStatus(ListingStatus status, Pageable pageable);
}
