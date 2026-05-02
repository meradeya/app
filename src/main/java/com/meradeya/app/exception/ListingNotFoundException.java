package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class ListingNotFoundException extends AppException {

  public static final String TITLE = "Listing Not Found";
  public static final String MESSAGE = "Listing not found.";

  public ListingNotFoundException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
