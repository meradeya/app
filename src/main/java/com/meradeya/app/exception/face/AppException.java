package com.meradeya.app.exception.face;

/**
 * Base class for all application-level exceptions.
 *
 * <p>Subclasses must provide a {@link #getTitle()} implementation that returns
 * a short, human-readable title for the problem.
 */
public abstract class AppException extends RuntimeException {

  protected AppException(String message) {
    super(message);
  }

  /**
   * Short, human-readable title for the problem.
   */
  public abstract String getTitle();
}
