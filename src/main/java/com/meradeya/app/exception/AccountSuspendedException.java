package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;


public class AccountSuspendedException extends RuntimeException implements AppException {

  public static final String TITLE = "Account Suspended";
  public static final String MESSAGE = "Your account has been suspended.";

  public AccountSuspendedException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
