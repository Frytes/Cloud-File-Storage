package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = StoragePathValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidStoragePath {
    String message() default "Недопустимый формат пути";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}