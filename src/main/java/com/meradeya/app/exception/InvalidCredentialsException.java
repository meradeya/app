package com.meradeya.app.exception;

/**
 * Thrown when credentials are invalid (wrong email or password).
 */
public class InvalidCredentialsException extends RuntimeException {

  public InvalidCredentialsException() {
    super("Invalid email or password.");
  }
}

