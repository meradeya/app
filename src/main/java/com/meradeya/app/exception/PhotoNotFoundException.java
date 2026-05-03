package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class PhotoNotFoundException extends AppException {

  public static final String TITLE = "Photo Not Found";
  public static final String MESSAGE = "Photo not found.";

  public PhotoNotFoundException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
