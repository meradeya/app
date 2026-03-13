package com.meradeya.app.exception;

/**
 * Thrown when an auth/refresh token is invalid, expired, already used, or revoked.
 */
public class InvalidTokenException extends RuntimeException {

  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException() {
    super("The token is invalid or has expired.");
  }
}

