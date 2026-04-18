package com.meradeya.app.domain.entity;

import java.util.Locale;
import java.util.Optional;

/**
 * Lifecycle states supported by a {@link Listing}.
 * <p>
 * Use {@link #from(String)} to safely parse external string values into enum constants.
 */
public enum ListingStatus {
  DRAFT, ACTIVE, PAUSED, SOLD, ARCHIVED;

  /**
   * Converts a raw status string to a {@link ListingStatus} value.
   *
   * <p>Behavior:
   * <ul>
   *   <li>Returns {@code null} when input is {@code null} or blank.</li>
   *   <li>Otherwise trims whitespace and normalizes case using {@link Locale#ROOT}.</li>
   *   <li>Resolves the normalized value via {@link ListingStatus#valueOf(String)}.</li>
   * </ul>
   *
   * @param status raw status value
   * @return matching {@link ListingStatus}, or {@code null} when input is {@code null} or blank
   * @throws IllegalArgumentException if normalized value does not match any enum constant
   */
  public static ListingStatus from(String status) {
    return Optional
        .ofNullable(status)
        .filter(s -> !s.isBlank())
        .map(String::trim)
        .map(s -> s.toUpperCase(Locale.ROOT))
        .map(ListingStatus::valueOf)
        .orElse(null);
  }

}
