package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class InvalidTokenException extends RuntimeException implements AppException {

  public static final String TITLE = "Invalid Token";
  public static final String MESSAGE = "The token is invalid or has expired.";

  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
