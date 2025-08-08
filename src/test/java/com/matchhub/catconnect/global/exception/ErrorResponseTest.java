package com.matchhub.catconnect.global.exception;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 테스트 메서드 실행 순서를 @Order 기준으로 설정
@DisplayName("ErrorResponse 통합 테스트") // 전체 테스트 클래스 이름
class ErrorResponseTest {

    private static final Logger log = LoggerFactory.getLogger(ErrorResponseTest.class); // 로그 기록을 위한 Logger

    @BeforeAll
    static void beforeAllInit() {
        // 테스트 시작 전에 한 번만 실행됨
        System.out.println("=== ErrorResponse 테스트 시작 ===");
        log.info("테스트 초기화");
    }

    @Nested
    @DisplayName("ErrorResponse 속성 검증") // 내부 클래스: 속성 테스트용
    class PropertyTests {

        @Test
        @Order(1)
        @DisplayName("기본 생성자 속성 검증") // 생성자에 기본 메시지와 상태코드를 설정하는지 테스트
        void testDefaultConstructorProperties() {
            log.debug("기본 생성자 테스트 시작");
            ErrorResponse response = new ErrorResponse(Domain.USER, ErrorCode.USER_NOT_FOUND);

            // 각 속성이 정확히 설정되었는지 검증
            assertEquals(Domain.USER, response.getDomain());
            assertEquals("USER_001", response.getCode());
            assertEquals("사용자를 찾을 수 없습니다.", response.getMessage());
            assertEquals(404, response.getStatus());

            log.debug("기본 생성자 테스트 완료");
        }

        @Test
        @Order(2)
        @DisplayName("커스텀 메시지 생성자 속성 검증")
        void testCustomMessageConstructor() {
            log.debug("커스텀 메시지 테스트 시작");
            ErrorResponse response = new ErrorResponse(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND, "게시글 ID: 999 없음");
            assertEquals(Domain.BOARD, response.getDomain());
            assertEquals("BOARD_001", response.getCode());
            assertEquals("게시글 ID: 999 없음", response.getMessage());
            assertEquals(404, response.getStatus());
            log.debug("커스텀 메시지 테스트 완료");
        }
    }

    @Nested
    @DisplayName("ErrorResponse 데이터 일치 테스트")
    class DataMatchingTests {

        @ParameterizedTest
        @Order(3)
        @CsvSource({
                "USER, USER_001, 사용자를 찾을 수 없습니다., 404",
                "BOARD, BOARD_001, 게시글을 찾을 수 없습니다., 404",
                "COMMENT, COMMENT_001, 댓글을 찾을 수 없습니다., 404"
        })
        @DisplayName("도메인과 에러 코드 조합 검증")
        void testDomainAndErrorCode(String domain, String code, String message, int status) {
            log.debug("도메인-에러 코드 테스트: domain={}", domain);
            ErrorResponse response = new ErrorResponse(Domain.valueOf(domain), ErrorCode.valueOf(domain + "_NOT_FOUND"));
            assertEquals(Domain.valueOf(domain), response.getDomain());
            assertEquals(code, response.getCode());
            assertEquals(message, response.getMessage());
            assertEquals(status, response.getStatus());
            log.debug("도메인-에러 코드 테스트 완료");
        }

        @ParameterizedTest
        @Order(4)
        @ValueSource(strings = {"특정 사용자 없음", "게시글 조회 실패"})
        @DisplayName("커스텀 메시지 일관성 검증")
        void testCustomMessageConsistency(String customMessage) {
            log.debug("커스텀 메시지 일관성 테스트: message={}", customMessage);
            ErrorResponse response = new ErrorResponse(Domain.COMMENT, ErrorCode.COMMENT_NOT_FOUND, customMessage);
            assertEquals(customMessage, response.getMessage());
            assertEquals("COMMENT_001", response.getCode());
            assertEquals(404, response.getStatus());
            log.debug("커스텀 메시지 일관성 테스트 완료");
        }
    }

    @Nested
    @DisplayName("ErrorResponse Enum 조합 테스트")
    class EnumCombinationTests {

        @ParameterizedTest
        @Order(5)
        @EnumSource(Domain.class)
        @DisplayName("모든 도메인에 대한 에러 응답 검증")
        void testAllDomains(Domain domain) {
            log.debug("도메인 테스트: domain={}", domain);
            ErrorResponse response = new ErrorResponse(domain, ErrorCode.INTERNAL_SERVER_ERROR);
            assertEquals(domain, response.getDomain());
            assertEquals("GLOBAL_001", response.getCode());
            assertEquals("서버 내부 오류가 발생했습니다.", response.getMessage());
            assertEquals(500, response.getStatus());
            log.debug("도메인 테스트 완료");
        }

        @ParameterizedTest
        @Order(6)
        @EnumSource(ErrorCode.class)
        @DisplayName("모든 ErrorCode에 대한 응답 검증")
        void testAllErrorCodes(ErrorCode errorCode) {
            log.debug("ErrorCode 테스트: code={}", errorCode.getCode());
            ErrorResponse response = new ErrorResponse(Domain.LIKE, errorCode);
            assertEquals(Domain.LIKE, response.getDomain());
            assertEquals(errorCode.getCode(), response.getCode());
            assertEquals(errorCode.getMessage(), response.getMessage());
            assertEquals(errorCode.getHttpStatus().value(), response.getStatus());
            log.debug("ErrorCode 테스트 완료");
        }
    }

    @Nested
    @DisplayName("시간 제한 테스트")
    class TimeoutTests {

        @Test
        @Order(7)
        @DisplayName("빠른 생성 시간 제한 검증")
        @Timeout(1)
        void testErrorResponseCreationTimeout() {
            log.debug("시간 제한 테스트 시작");
            ErrorResponse response = new ErrorResponse(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND);
            assertEquals(Domain.BOARD, response.getDomain());
            log.debug("시간 제한 테스트 완료");
        }
    }

    @AfterAll
    static void afterAll() {
        System.out.println("=== ErrorResponse 테스트 종료 ===");
        log.info("테스트 종료");
    }
}
