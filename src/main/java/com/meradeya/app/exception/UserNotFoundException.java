package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class UserNotFoundException extends AppException {

  public static final String TITLE = "User Not Found";
  public static final String MESSAGE = "User not found.";

  public UserNotFoundException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}

