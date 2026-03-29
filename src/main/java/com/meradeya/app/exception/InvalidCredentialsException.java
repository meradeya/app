package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;


public class InvalidCredentialsException extends AppException {

  public static final String TITLE = "Invalid Credentials";
  public static final String MESSAGE = "Invalid email or password.";

  public InvalidCredentialsException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
