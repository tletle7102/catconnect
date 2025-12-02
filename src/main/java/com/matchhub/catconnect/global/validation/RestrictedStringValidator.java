package com.matchhub.catconnect.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestrictedStringValidator implements ConstraintValidator<RestrictedString, String> {

    private static final Logger log = LoggerFactory.getLogger(RestrictedStringValidator.class);
    private static final String RESTRICTED_PATTERN = "[<>&]"; // HTML 태그 관련 문자 제한

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null은 @NotBlank로 처리
        }
        boolean isValid = !value.matches(".*"+ RESTRICTED_PATTERN + ".*");
        if (!isValid) {
            log.warn("제한된 특수 문자 포함: value={}", value);
        }
        return isValid;
    }
}
