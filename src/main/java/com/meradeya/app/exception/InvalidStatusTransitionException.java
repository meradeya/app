package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class InvalidStatusTransitionException extends AppException {

  public static final String TITLE = "Invalid Status Transition";

  public InvalidStatusTransitionException(String from, String to) {
    super("Cannot transition from " + from + " to " + to + ".");
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
