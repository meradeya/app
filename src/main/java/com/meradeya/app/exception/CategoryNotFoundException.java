package com.meradeya.app.exception;

import com.meradeya.app.exception.face.AppException;

public class CategoryNotFoundException extends AppException {

  public static final String TITLE = "Category Not Found";
  public static final String MESSAGE = "Category not found.";

  public CategoryNotFoundException() {
    super(MESSAGE);
  }

  @Override
  public String getTitle() {
    return TITLE;
  }
}
