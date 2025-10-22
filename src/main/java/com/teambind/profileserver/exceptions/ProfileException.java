package com.teambind.profileserver.exceptions;

import org.springframework.http.HttpStatus;

public class ProfileException extends RuntimeException {
  private final ProfileErrorCode errorcode;

  public ProfileException(ProfileErrorCode errorcode) {

    super(errorcode.toString());
    this.errorcode = errorcode;
  }

  public HttpStatus getStatus() {
    return errorcode.getStatus();
  }

  public ProfileErrorCode getErrorCode() {
    return errorcode;
  }
}
