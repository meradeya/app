package com.meradeya.app.service.impl;

import com.meradeya.app.config.CacheNames;
import com.meradeya.app.domain.entity.ListingStatus;
import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.CreateListingRequest;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.dto.listing.ListingSummary;
import com.meradeya.app.dto.listing.UpdateListingRequest;
import com.meradeya.app.exception.ListingNotFoundException;
import com.meradeya.app.service.face.ListingService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Caching decorator for {@link ListingService}.
 *
 * <p>Wraps the default implementation to provide cache-aside reads and
 * cache updates/evictions for mutating operations.
 */
@Service
@Primary
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "app.cache.listings.enabled", havingValue = "true")
public class CachedListingServiceImpl implements ListingService {

  private final ListingService delegate;
  private final ListingCacheFacade cacheFacade;

  public CachedListingServiceImpl(
      @Qualifier("defaultListingService") ListingService delegate,
      ListingCacheFacade cacheFacade) {
    this.delegate = delegate;
    this.cacheFacade = cacheFacade;
  }

  /**
   * Delegates to the default service without caching because feed is paginated and personalized in
   * future iterations.
   */
  @Override
  public Page<ListingSummary> getFeed(Pageable pageable) {
    return delegate.getFeed(pageable);
  }

  /**
   * Delegates to the default service; own listings are not cached due to user-specific visibility
   * and frequent updates.
   */
  @Override
  public Page<ListingSummary> getOwnListings(UUID callerId, ListingStatus status,
      Pageable pageable) {
    return delegate.getOwnListings(callerId, status, pageable);
  }

  /**
   * Reads listing detail from the cache facade.
   */
  @Override
  public ListingDetail getListingDetailRaw(UUID listingId) {
    return cacheFacade.getListingDetailRaw(listingId);
  }

  /**
   * Applies visibility rules on top of cached raw listing detail.
   */
  @Override
  public ListingDetail getListing(UUID listingId, UUID callerId) {
    ListingDetail listing = getListingDetailRaw(listingId);
    boolean isOwner = callerId != null && callerId.equals(listing.sellerId());
    if (!isOwner && !listing.status().equals(ListingStatus.ACTIVE.name())) {
      throw new ListingNotFoundException();
    }
    return listing;
  }

  /**
   * Creates a listing and returns the delegate result (no cache entry yet).
   */
  @Override
  public ListingDetail createListing(UUID sellerId, CreateListingRequest req) {
    return delegate.createListing(sellerId, req);
  }

  /**
   * Updates the listing and refreshes the cache entry.
   */
  @Override
  @CachePut(cacheNames = CacheNames.LISTINGS, key = "#listingId")
  public ListingDetail updateListing(UUID listingId, UUID callerId, UpdateListingRequest req) {
    return delegate.updateListing(listingId, callerId, req);
  }

  /**
   * Publishes the listing and refreshes the cache entry.
   */
  @Override
  @CachePut(cacheNames = CacheNames.LISTINGS, key = "#listingId")
  public ListingDetail publish(UUID listingId, UUID callerId) {
    return delegate.publish(listingId, callerId);
  }

  /**
   * Archives the listing and refreshes the cache entry.
   */
  @Override
  @CachePut(cacheNames = CacheNames.LISTINGS, key = "#listingId")
  public ListingDetail archive(UUID listingId, UUID callerId) {
    return delegate.archive(listingId, callerId);
  }

  /**
   * Deletes the listing and evicts the cache entry.
   */
  @Override
  @CacheEvict(cacheNames = CacheNames.LISTINGS, key = "#listingId")
  public ListingDetail deleteListing(UUID listingId, UUID callerId) {
    return delegate.deleteListing(listingId, callerId);
  }

  /**
   * Uploads a photo and refreshes the cache entry.
   */
  @Override
  @CachePut(cacheNames = CacheNames.LISTINGS, key = "#listingId")
  public ListingDetail uploadPhoto(UUID listingId, MultipartFile file) {
    return delegate.uploadPhoto(listingId, file);
  }

  /**
   * Deletes a photo and refreshes the cache entry.
   */
  @Override
  @CachePut(cacheNames = CacheNames.LISTINGS, key = "#listingId")
  public ListingDetail deletePhoto(UUID listingId, UUID photoId) {
    return delegate.deletePhoto(listingId, photoId);
  }

  /**
   * Reads the cached category tree from the cache facade.
   */
  @Override
  public List<CategoryTreeDto> getCategoryTree() {
    return cacheFacade.getCategoryTree();
  }
}
