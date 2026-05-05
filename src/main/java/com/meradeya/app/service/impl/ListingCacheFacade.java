package com.meradeya.app.service.impl;

import com.meradeya.app.config.CacheNames;
import com.meradeya.app.dto.category.CategoryTreeDto;
import com.meradeya.app.dto.listing.ListingDetail;
import com.meradeya.app.service.face.ListingService;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache facade that isolates cacheable read methods from self-invocation issues.
 *
 * <p>All cacheable reads are routed through this bean to ensure cache
 * interception happens on a proxied method call.
 */
@Service
public class ListingCacheFacade {

  private final ListingService delegate;

  public ListingCacheFacade(@Qualifier("defaultListingService") ListingService delegate) {
    this.delegate = delegate;
  }

  /**
   * Cache-aside read for listing detail without visibility checks.
   *
   * @param listingId listing id
   * @return listing detail
   */
  @Cacheable(cacheNames = CacheNames.LISTINGS, key = "#listingId", sync = true)
  public ListingDetail getListingDetailRaw(UUID listingId) {
    return delegate.getListingDetailRaw(listingId);
  }

  /**
   * Cache-aside read for the category tree.
   *
   * @return cached category tree
   */
  @Cacheable(cacheNames = CacheNames.CATEGORY_TREE, sync = true)
  public List<CategoryTreeDto> getCategoryTree() {
    return delegate.getCategoryTree();
  }
}
