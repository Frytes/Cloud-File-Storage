package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidPasswordTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    record TestRecord(@ValidPassword String password) {}

    @Test
    void validPasswordShouldPass() {
        var violations = validator.validate(new TestRecord("Secret123!"));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "short",
            "русский123!",
            "Secret\n123!",
            "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901" // 101 символ
    })
    void invalidPasswordShouldFail(String invalidPassword) {
        var violations = validator.validate(new TestRecord(invalidPassword));
        assertThat(violations).isNotEmpty();
    }
}