package com.frytes.cloudstorage.common.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StoragePathValidator implements ConstraintValidator<ValidStoragePath, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (path.contains("..")) {
            return false;
        }
        if (path.startsWith("/")) {
            return false;
        }
        if (path.contains("//")) {
            return false;
        }

        return path.matches("^[a-zA-Z0-9/._-]+$");
    }
}