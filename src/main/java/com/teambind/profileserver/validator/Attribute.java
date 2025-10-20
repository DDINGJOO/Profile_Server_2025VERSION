package com.teambind.profileserver.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/** 필수 동의항목 검증 어노테이션 - 필수 동의항목이 모두 포함되어 있는지 확인 - 필수 항목은 consented = true 여야 함 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AttributeValidator.class)
@Documented
public @interface Attribute {
	String value();
	String message() default "장르와 악기는 최대 {max}개까지 선택 가능하며, 유효한 장르 ID와 이름이어야 합니다";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
