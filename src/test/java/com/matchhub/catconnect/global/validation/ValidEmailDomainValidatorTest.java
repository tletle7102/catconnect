package com.matchhub.catconnect.global.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidEmailDomainValidator 테스트")
class ValidEmailDomainValidatorTest {

    private static final Logger log = LoggerFactory.getLogger(ValidEmailDomainValidatorTest.class);
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        log.debug("Validator 초기화");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("허용된 이메일 도메인 검증 성공")
    void testValidEmailDomain() {
        log.debug("허용된 이메일 도메인 테스트 시작");
        class TestClass {
            @ValidEmailDomain
            String email = "test@gmail.com";
        }
        TestClass test = new TestClass();
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertTrue(violations.isEmpty(), "허용된 이메일 도메인은 검증에 성공해야 합니다.");
        log.debug("허용된 이메일 도메인 테스트 완료");
    }

    @Test
    @DisplayName("허용되지 않은 이메일 도메인 검증 성공")
    void testInvalidEmailDomain() {
        log.debug("허용되지 않은 이메일 도메인 테스트 시작");
        class TestClass {
            @ValidEmailDomain
            String email = "test@invalidemaildomain.com";
        }
        TestClass test = new TestClass();
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertEquals(1, violations.size(), "허용되지 않은 이메일 도메인은 검증에 실패해야 합니다.");
        assertEquals("허용되지 않은 이메일 도메인입니다.", violations.iterator().next().getMessage());
        log.debug("허용되지 않은 이메일 도메인 테스트 완료");
    }
}