package com.frytes.cloudstorage.common.validate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidUsernameTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    record TestRecord(@ValidUsername String username) {}

    @Test
    void validUsernameShouldPass() {
        var violations = validator.validate(new TestRecord("john_doe123"));
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "a",
            "user<name>",
            "user name",
            "юзер",
            "very_long_username_that_exceeds_fifty_characters_1234567890"
    })
    void invalidUsernameShouldFail(String invalidUsername) {
        var violations = validator.validate(new TestRecord(invalidUsername));
        assertThat(violations).isNotEmpty();
    }
}