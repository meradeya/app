package com.meradeya.app.config;

/**
 * Central registry of cache names used by the application.
 *
 * <p>Keep these names stable to avoid breaking cached data during deployments.
 */
public final class CacheNames {

  /** Cache for listing detail views keyed by listing id. */
  public static final String LISTINGS = "listings";

  /** Cache for the category tree payload. */
  public static final String CATEGORY_TREE = "categoryTree";

  private CacheNames() {
  }
}
