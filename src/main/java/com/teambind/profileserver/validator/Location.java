package com.teambind.profileserver.validator;

import jakarta.validation.Constraint;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NickNameValidator.class)
@Documented
public @interface Location {}
