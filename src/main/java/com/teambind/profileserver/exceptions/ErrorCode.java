package com.teambind.profileserver.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  NICKNAME_ALREADY_EXISTS("PROFILE_001", "Nickname already exists", HttpStatus.CONFLICT),
  HISTORY_UPDATE_FAILED(
      "PROFILE_002", "Failed to update history", HttpStatus.INTERNAL_SERVER_ERROR),
  GENRE_SIZE_INVALID("PROFILE_003", "Genre size not valid", HttpStatus.BAD_REQUEST),
  NOT_ALLOWED_GENRE_ID_AND_NAME(
      "PROFILE_004", "GenreId and Name are not allowed", HttpStatus.BAD_REQUEST),
  INSTRUMENT_SIZE_INVALID("PROFILE_005", "Instrument size not valid", HttpStatus.BAD_REQUEST),
  NOT_ALLOWED_INSTRUMENTS_ID_AND_NAME(
      "PROFILE_006", "InstrumentId and Name are not allowed", HttpStatus.BAD_REQUEST),
  USER_NOT_FOUND("PROFILE_007", "User not found", HttpStatus.NOT_FOUND),
  NICKNAME_INVALID("PROFILE_008", "Nickname is invalid", HttpStatus.BAD_REQUEST),
  GENRE_INVALID("PROFILE_009", "GenreId and Name are invalid", HttpStatus.BAD_REQUEST),
  ;
  private final String errCode;
  private final String message;
  private final HttpStatus status;

  ErrorCode(String errCode, String message, HttpStatus status) {

    this.status = status;
    this.errCode = errCode;
    this.message = message;
  }

  @Override
  public String toString() {
    return "ErrorCode{"
        + " status='"
        + status
        + '\''
        + "errCode='"
        + errCode
        + '\''
        + ", message='"
        + message
        + '\''
        + '}';
  }
}
