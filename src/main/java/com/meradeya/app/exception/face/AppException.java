package com.meradeya.app.exception.face;

/**
 * Marker interface for application-level exceptions that carry a
 * human-readable {@link #getTitle()}.
 *
 * <p>All implementing classes must extend {@link RuntimeException} so that
 * {@link #getMessage()} is always available.
 */
public interface AppException {

  /** Short, human-readable title for the problem (e.g. {@code "Email Already Exists"}). */
  String getTitle();

  /**
   * Human-readable detail message describing this specific occurrence.
   * Satisfied automatically by any {@link RuntimeException} subclass.
   */
  String getMessage();
}
