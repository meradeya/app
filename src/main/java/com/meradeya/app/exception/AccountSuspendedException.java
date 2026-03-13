package com.meradeya.app.exception;

/**
 * Thrown when a suspended account attempts to log in.
 */
public class AccountSuspendedException extends RuntimeException {

  public AccountSuspendedException() {
    super("Your account has been suspended.");
  }
}

