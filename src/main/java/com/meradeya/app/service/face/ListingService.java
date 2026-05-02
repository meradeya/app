package com.meradeya.app.service.face;

import com.meradeya.app.domain.entity.ListingStatus;
import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.CreateListingRequest;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.dto.listing.ListingSummary;
import com.meradeya.app.dto.listing.UpdateListingRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ListingService {
  
  Page<ListingSummary> getFeed(Pageable pageable);

  Page<ListingSummary> getOwnListings(UUID callerId, ListingStatus status, Pageable pageable);

  ListingDetail getListing(UUID listingId, UUID callerId);
  
  ListingDetail createListing(UUID sellerId, CreateListingRequest req);

  ListingDetail updateListing(UUID listingId, UUID callerId, UpdateListingRequest req);

  ListingDetail publish(UUID listingId, UUID callerId);

  ListingDetail archive(UUID listingId, UUID callerId);

  ListingDetail deleteListing(UUID listingId, UUID callerId);

  ListingDetail uploadPhoto(UUID listingId, MultipartFile file);

  ListingDetail deletePhoto(UUID listingId, UUID photoId);

  List<CategoryTreeDto> getCategoryTree();
}
