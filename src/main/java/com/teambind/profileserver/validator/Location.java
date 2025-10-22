package com.teambind.profileserver.validator;

import jakarta.validation.Constraint;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LocationValidator.class)
@Documented
public @interface Location {
    String message() default "Invalid location";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
}
