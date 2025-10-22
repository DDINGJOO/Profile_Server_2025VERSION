package com.teambind.profileserver.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

public class NickNameValidator implements ConstraintValidator<NickName, String> {

  @Value("${nickname.validation.regex:^[a-zA-Z0-9_]{3,15}$}")
  private String regex;

  // TODO : 부적절한 닉네임 필터 구현
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    if (!value.matches(regex) || value.isEmpty()) {
      return false;
    }
    ;
    return true;
  }
}
