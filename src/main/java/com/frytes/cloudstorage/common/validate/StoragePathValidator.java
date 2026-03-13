package com.frytes.cloudstorage.common.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StoragePathValidator implements ConstraintValidator<ValidStoragePath, String> {

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (path == null) {
            return false;
        }
        if (path.isEmpty()) {
            return true; // Пустая строка - это корневая папка (валидный путь)
        }
        if (path.contains("..")) {
            return false; // Защита от Path Traversal
        }
        if (path.startsWith("/")) {
            return false; // S3 пути не должны быть абсолютными
        }
        return !path.contains("//"); // Защита от некорректных путей
// Разрешаем пробелы, кириллицу и прочие символы
    }
}