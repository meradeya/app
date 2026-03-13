package com.meradeya.app.exception;

/**
 * Thrown when registration is attempted with an already-registered email.
 */
public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException(String email) {
    super("An account with email '" + email + "' already exists.");
  }
}

