package com.meradeya.app.domain.entity;

import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public enum ListingStatus {
  DRAFT, ACTIVE, PAUSED, SOLD, ARCHIVED;

  public static ListingStatus from(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    try {
      return ListingStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Invalid listing status: " + status, ex);
    }
  }

}

