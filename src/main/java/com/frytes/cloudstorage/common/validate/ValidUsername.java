package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@NotBlank(message = "Имя пользователя не может быть пустым")
@Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
@Pattern(regexp = "^[a-zA-Z0-9._-]+$",
        message = "Имя может содержать только латинские буквы, цифры, точки, подчеркивания и дефисы")
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidUsername {
    String message() default "Недопустимый формат имени пользователя";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}