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

@DisplayName("ValidUsernameValidator 테스트")
class ValidUsernameValidatorTest {

    private static final Logger log = LoggerFactory.getLogger(ValidUsernameValidatorTest.class);
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        log.debug("Validator 초기화");
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 사용자 이름 검증 성공")
    void testValidUsername() {
        log.debug("유효한 사용자 이름 테스트 시작");
        class TestClass {
            @ValidUsername
            String username = "test_user1";
        }
        TestClass test = new TestClass();
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertTrue(violations.isEmpty(), "유효한 사용자 이름은 검증에 성공해야 합니다.");
        log.debug("유효한 사용자 이름 테스트 완료");
    }

    @Test
    @DisplayName("유효하지 않은 사용자 이름 검증 실패")
    void testInvalidUsername() {
        log.debug("유효하지 않은 사용자 이름 테스트 시작");
        class TestClass {
            @ValidUsername
            String username = "test_@user!1";
        }
        TestClass test = new TestClass();
        Set<ConstraintViolation<TestClass>> violations = validator.validate(test);
        assertEquals(1, violations.size(), "유효하지 않은 사용자 이름은 검증에 실패해야 합니다.");
        assertEquals("사용자 이름은 4~20자의 알파벳, 숫자, 밑줄(_)만 포함해야 합니다.", violations.iterator().next().getMessage());
        log.debug("유효하지 않은 사용자 이름 테스트 완료");
    }

}