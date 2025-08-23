package com.matchhub.catconnect.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("AppException 테스트") // 테스트 클래스 설명
class AppExceptionTest {

    private static final Logger log = LoggerFactory.getLogger(AppExceptionTest.class);

    // AppException의 생성자 동작 및 속성 확인 테스트
    @Nested
    @DisplayName("AppException 속성 검증")
    class PropertyTests{

        @Test
        @DisplayName("기본 생성자 속성 검증")
        void testDefaultConstructor() {
            log.debug("기본 생성자 테스트 시작");

            // 메시지를 생략한 기본 생성자 사용 시 ErrorCode의 기본 메시지가 설정되어야 함
            AppException ex =new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND);

            // 도메인, 에러코드, 메시지가 올바르게 설정되었는지 확인
            assertEquals(Domain.USER, ex.getDomain());
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
            assertEquals("사용자를 찾을 수 없습니다.", ex.getMessage());

            log.debug("기본 생성자 테스트 완료");
        }

        @Test
        @DisplayName("커스텀 메시지 생성자 속성 검증")
        void testCustomMessageConstructor() {
            log.debug("커스텀 메시지 테스트 시작");

            // 커스텀 메시지를 전달했을 때, 해당 메시지가 그대로 사용되는지 검증
            AppException ex = new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND, "게시글 ID: 999 없음");

            assertEquals(Domain.BOARD, ex.getDomain());
            assertEquals(ErrorCode.BOARD_NOT_FOUND, ex.getErrorCode());
            assertEquals("게시글 ID: 999 없음", ex.getMessage());

            log.debug("커스텀 메시지 테스트 완료");
        }
    }

    // 실제 도메인과 에러코드가 일치하는지 테스트
    @Nested
    @DisplayName("AppException 데이터 일치 테스트")
    class DataMatchingTests {

        @Test
        @DisplayName("USER 도메인과 에러 코드 검증")
        void testUserDomainAndErrorCode() {
            log.debug("USER 도메인-에러 코드 테스트 시작");

            AppException ex = new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND);

            assertEquals(Domain.USER, ex.getDomain());
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
            assertEquals("사용자를 찾을 수 없습니다.", ex.getMessage());

            log.debug("USER 도메인-에러 코드 테스트 완료");
        }

        @Test
        @DisplayName("BOARD 도메인과 에러 코드 검증")
        void testBoardDomainAndErrorCode() {
            log.debug("BOARD 도메인-에러 코드 테스트 시작");

            AppException ex = new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND);

            assertEquals(Domain.BOARD, ex.getDomain());
            assertEquals(ErrorCode.BOARD_NOT_FOUND, ex.getErrorCode());
            assertEquals("게시글을 찾을 수 없습니다.", ex.getMessage());

            log.debug("BOARD 도메인-에러 코드 테스트 완료");
        }

        @Test
        @DisplayName("커스텀 메시지 일관성 검증")
        void testCustomMessageConsistency() {
            log.debug("커스텀 메시지 테스트 시작");

            // 사용자가 전달한 커스텀 메시지가 그대로 들어가는지 확인
            String customMessage = "사용자 ID: 123 없음";
            AppException ex = new AppException(Domain.COMMENT, ErrorCode.COMMENT_NOT_FOUND, customMessage);

            assertEquals(customMessage, ex.getMessage());
            assertEquals(ErrorCode.COMMENT_NOT_FOUND, ex.getErrorCode());

            log.debug("커스텀 메시지 테스트 완료");
        }
    }
}