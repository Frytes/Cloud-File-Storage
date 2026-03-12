package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Pattern(regexp = "^(?!.*\\.\\.).*", message = "Недопустимый путь: нельзя использовать '../'")
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidStoragePath {
    String message() default "Недопустимый формат пути";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}