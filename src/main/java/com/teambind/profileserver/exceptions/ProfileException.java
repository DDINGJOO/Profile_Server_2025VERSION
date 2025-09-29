package com.teambind.profileserver.exceptions;

import org.springframework.http.HttpStatus;

public class ProfileException extends RuntimeException {
  private final ErrorCode errorcode;

  public ProfileException(ErrorCode errorcode) {

    super(errorcode.toString());
    this.errorcode = errorcode;
  }

  public HttpStatus getStatus() {
    return errorcode.getStatus();
  }

  public ErrorCode getErrorCode() {
    return errorcode;
  }
}
