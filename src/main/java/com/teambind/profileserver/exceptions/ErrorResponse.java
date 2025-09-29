package com.teambind.profileserver.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  private LocalDateTime timestamp;

  private int status;
  private String code;
  private String message;
  private String path;

  public static ErrorResponse of(int status, String code, String message, String path) {
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status)
        .code(code)
        .message(message)
        .path(path)
        .build();
  }
}
