package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;


public class EmailAlreadyExistsException extends RuntimeException implements AppException {

  public static final String TITLE = "Email Already Exists";
  public static final String MESSAGE = "An account with this email already exists.";

  public EmailAlreadyExistsException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
