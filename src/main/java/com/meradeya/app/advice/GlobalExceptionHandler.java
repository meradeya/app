package com.meradeya.app.advice;

import com.meradeya.app.exception.AccountSuspendedException;
import com.meradeya.app.exception.EmailAlreadyExistsException;
import com.meradeya.app.exception.InvalidCredentialsException;
import com.meradeya.app.exception.InvalidTokenException;
import com.meradeya.app.generated.api.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  //TODO someone should revert this exception handling   
  
  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<Error> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new Error("EMAIL_ALREADY_EXISTS", ex.getMessage()));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Void> handleInvalidCredentials(InvalidCredentialsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @ExceptionHandler(AccountSuspendedException.class)
  public ResponseEntity<Error> handleAccountSuspended(AccountSuspendedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(new Error("ACCOUNT_SUSPENDED", ex.getMessage()));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<Error> handleInvalidToken(InvalidTokenException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new Error("TOKEN_INVALID", ex.getMessage()));
  }
}

