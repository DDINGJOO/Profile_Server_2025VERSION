package com.teambind.profileserver.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AttributeValidator.class)
@Documented
public @interface NickName {
	String message() default "적절하지 않은 닉네임 형식입니다.";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
