package com.matchhub.catconnect.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidUsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private static final Logger log = LoggerFactory.getLogger(ValidUsernameValidator.class);
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{4,20}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null은 @NotBlank로 처리
        }
        boolean isValid = value.matches(USERNAME_PATTERN);
        if (!isValid) {
            log.warn("유효하지 않은 사용자 이름: value={}", value);
        }
        return isValid;
    }
}
