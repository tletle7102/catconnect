package com.matchhub.catconnect.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidEmailDomainValidator implements ConstraintValidator<ValidEmailDomain, String> {

    private static final Logger log = LoggerFactory.getLogger(ValidEmailDomainValidator.class);
    private static final String[] ALLOWED_DOMAINS = {
            "email.com",
            "example.com",
            "gmail.com",
            "naver.com",
            "daum.net",
            "hanmail.net",
            "kakao.com",
            "nate.com"
    }; // 테스트용, 실제로는 설정 파일로 관리 가능

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // null은 @NotBlank로 처리
        }
        String domain = value.substring(value.indexOf("@") + 1);
        boolean isValid = false;
        for (String allowedDomain : ALLOWED_DOMAINS) {
            if (domain.equals(allowedDomain)) {
                isValid = true;
                break;
            }
        }
        if (!isValid) {
            log.warn("허용되지 않은 이메일 도메인: value={}", value);
        }
        return isValid;
    }
}
