package com.meradeya.app.advice;

import com.meradeya.app.dto.common.ExceptionDetail;
import com.meradeya.app.exception.AccountSuspendedException;
import com.meradeya.app.exception.EmailAlreadyExistsException;
import com.meradeya.app.exception.InvalidCredentialsException;
import com.meradeya.app.exception.InvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ExceptionDetail> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.CONFLICT, ex));
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<Void> handleInvalidCredentials() {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
  }

  @ExceptionHandler(AccountSuspendedException.class)
  public ResponseEntity<ExceptionDetail> handleAccountSuspended(AccountSuspendedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.FORBIDDEN, ex));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ExceptionDetail> handleInvalidToken(InvalidTokenException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ExceptionDetail.forStatusAndException(HttpStatus.BAD_REQUEST, ex));
  }

}
