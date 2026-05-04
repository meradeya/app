package com.meradeya.app.prop;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Typed configuration for application caching.
 *
 * <p>Backed by {@code app.cache.*} properties.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

  /** Global on/off switch for the caching layer. */
  private boolean enabled = true;

  /** Optional Redis key prefix to namespace cache entries. */
  private String keyPrefix = "app::";

  /** Listing-related cache configuration. */
  private Listings listings = new Listings();

  /** Category-related cache configuration. */
  private Categories categories = new Categories();

  @Getter
  @Setter
  public static class Listings {

    /** Toggle for the listings cache in the decorator service. */
    private boolean enabled = true;

    /** Default time-to-live for listing detail entries. */
    private Duration ttl = Duration.ofHours(1);
  }

  @Getter
  @Setter
  public static class Categories {

    /** Time-to-live for the category tree cache. */
    private Duration ttl = Duration.ofHours(24);
  }
}
