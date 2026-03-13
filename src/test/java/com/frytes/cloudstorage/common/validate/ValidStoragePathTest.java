package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidStoragePathTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    record TestRecord(@ValidStoragePath String path) {}

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "folder/",
            "folder/subfolder/",
            "file.txt",
            "my file with spaces.txt",
            "папка/файл (1).txt",
            "folder/subfolder/file.txt",
            "my-document_v2.1.txt"
    })
    void validPathShouldPass(String validPath) {
        var violations = validator.validate(new TestRecord(validPath));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../etc/passwd",
            "folder/../file.txt",
            "folder//file.txt",
            "/absolute/path"
    })
    void invalidPathShouldFail(String invalidPath) {
        var violations = validator.validate(new TestRecord(invalidPath));
        assertThat(violations).isNotEmpty();
    }
}