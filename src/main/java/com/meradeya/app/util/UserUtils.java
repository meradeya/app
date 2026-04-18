package com.meradeya.app.util;

import java.util.Locale;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserUtils {

  /**
   * Normalizes email-like credentials for case-insensitive lookup.
   *
   * @param email raw credential value
   * @return lower-case normalized value
   */
  public static String normalizeEmail(String email) {
    return email.toLowerCase(Locale.ROOT).trim();
  }

}
