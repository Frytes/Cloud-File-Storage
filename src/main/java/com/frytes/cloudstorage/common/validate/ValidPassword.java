package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@NotBlank(message = "Пароль не может быть пустым")
@Size(min = 8, max = 100, message = "Пароль должен состоять минимум из 8 и максимум из 100 символов")
@Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-]+$", message = "Пароль содержит недопустимые символы")
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidPassword {
    String message() default "Недопустимый формат пароля";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}