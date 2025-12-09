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

@DisplayName("RestrictedStringValidator 테스트")
class RestrictedStringValidatorTest {

    private static final Logger log = LoggerFactory.getLogger(RestrictedStringValidatorTest.class);
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        log.debug("Validator 초기화");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("허용된 문자열 유효성 검증 성공")
    void testValidString() {
        log.debug("허용된 문자열 테스트 시작");
        class TestClass {
            @RestrictedString
            String value = "유효성 테스트용 문자열";
        }
        TestClass test = new TestClass();
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertTrue(violations.isEmpty(), "허용된 문자열은 유효해야 합니다.");
        log.debug("허용된 문자열 테스트 완료");
    }

    @Test
    @DisplayName("제한된 특수 문자 포함 시 유효서 검증 실패")
    void testInvalidString() {
        log.debug("제한된 문자열 테스트 시작");
        class TestClass {
            @RestrictedString
            String value = "안녕하세요<script>";
        }
        TestClass test =  new TestClass();
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertEquals(1, violations.size(), "제한된 특수 문자가 포함되면 유효성 검증이 실패해야 합니다.");
        assertEquals("허용되지 않은 특수 문자가 포함되어 있습니다.", violations.iterator().next().getMessage());
        log.debug("제한된 문자열 테스트 완료");
    }
}