package com.meradeya.app.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 * Enumeration of MIME types accepted for listing photo uploads.
 *
 */
@Getter
public enum AllowedPhotoMimeType {

  JPEG("image/jpeg"),
  PNG("image/png"),
  WEBP("image/webp");

  private final String mimeType;

  AllowedPhotoMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /** Returns true if the given MIME type string is in the allowlist. */
  public static boolean isAllowed(String mime) {
    return Arrays.stream(values()).anyMatch(t -> t.mimeType.equals(mime));
  }

  /** Returns the full set of allowed MIME type strings — used in error messages. */
  public static Set<String> allowedMimeTypes() {
    return Arrays.stream(values()).map(AllowedPhotoMimeType::getMimeType)
        .collect(Collectors.toSet());
  }
}

