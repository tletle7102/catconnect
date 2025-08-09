package com.matchhub.catconnect.global.exception;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // @Order 어노테이션 기준으로 테스트 실행 순서 결정
@DisplayName("Response 통합 테스트")
class ResponseTest {

    private static final Logger log = LoggerFactory.getLogger(ResponseTest.class); // 로깅 설정

    @BeforeAll
    static void beforeAllInit() {
        // 모든 테스트 전에 한 번만 실행
        System.out.println("=== Response 테스트 시작 ===");
        log.info("테스트 초기화");
    }

    @Nested
    @DisplayName("Response 성공 응답 테스트")
    class ResponseSuccessTests {

        @ParameterizedTest // 여러 input 값으로 반복 테스트
        @Order(1)
        @ValueSource(strings = {"테스트 데이터", "다른 데이터"}) // 각 문자열을 테스트에 주입
        @DisplayName("Response 성공 데이터 및 result 검증")
        void testResponseSuccessData(String data) {
            log.debug("성공 데이터 테스트: data={}", data);

            // 성공 응답 생성
            Response<String> response = Response.success(data, "성공");

            // 결과 필드들이 정확한지 확인
            assertEquals("SUCCESS", response.getResult());
            assertEquals(data, response.getData());
            assertEquals("성공", response.getMessage());
            assertEquals(200, response.getStatus());

            log.debug("성공 데이터 테스트 완료");
        }

        @ParameterizedTest
        @Order(2)
        @CsvSource({
                "게시글, 요청 성공",
                "댓글, 처리 완료"
        }) // (data, message) 형태로 테스트 반복
        @DisplayName("Response 성공 메시지 및 데이터 일치 여부")
        void testResponseSuccessCsvSource(String data, String message) {
            log.debug("성공 메시지 테스트: data={}", data);
            Response<String> response = Response.success(data, message);

            assertEquals("SUCCESS", response.getResult());
            assertEquals(data, response.getData());
            assertEquals(message, response.getMessage());
            assertEquals(200, response.getStatus());
            log.debug("성공 메시지 테스트 완료");
        }

        @Test
        @Order(3)
        @DisplayName("Response 기본 성공 응답 검증")
        void testDefaultSuccessResponse() {
            log.debug("기본 성공 응답 테스트 시작");
            // 메시지를 생략하고 기본 메시지를 사용하는 테스트
            Response<String> response = Response.success("기본 데이터");

            assertEquals("SUCCESS", response.getResult());
            assertEquals("기본 데이터", response.getData());
            assertEquals("요청이 성공적으로 처리되었습니다.", response.getMessage());
            assertEquals(200, response.getStatus());
            log.debug("기본 성공 응답 테스트 완료");
        }
    }

    @Nested
    @DisplayName("Response 에러 응답 테스트")
    class ResponseErrorTests {

        @ParameterizedTest
        @Order(4)
        @CsvSource({
                "잘못된 요청, 400",
                "서버 오류, 500"
        }) // (message, status) 조합 테스트
        @DisplayName("Response 에러 메시지 및 상태 코드 검증")
        void testResponseError(String message, int status) {
            log.debug("에러 응답 테스트: message={}", message);
            Response<?> response = Response.error(message, HttpStatus.valueOf(status));

            assertEquals("ERROR", response.getResult());
            assertNull(response.getData()); // 에러 응답은 데이터가 없음
            assertEquals(message, response.getMessage());
            assertEquals(status, response.getStatus());
            log.debug("에러 응답 테스트 완료");
        }

        @Test
        @Order(5)
        @DisplayName("Response 에러 응답 기본 속성 검증")
        void testResponseErrorProperties() {
            log.debug("에러 속성 테스트 시작");
            Response<?> response = Response.error("접근 권한 없음", HttpStatus.FORBIDDEN);

            assertEquals("ERROR", response.getResult());
            assertNull(response.getData());
            assertEquals("접근 권한 없음", response.getMessage());
            assertEquals(403, response.getStatus());
            log.debug("에러 속성 테스트 완료");
        }
    }

    @Nested
    @DisplayName("시간 제한 테스트")
    class TimeoutTests {

        @Test
        @Order(6)
        @DisplayName("Response 에러 응답 생성 시간 제한 검증")
        @Timeout(1) // 테스트가 1초 넘게 걸리면 실패 처리
        void testResponseErrorTimeout() {
            log.debug("시간 제한 테스트 시작");
            Response<?> response = Response.error("시간 제한 테스트", HttpStatus.BAD_REQUEST);

            assertEquals("ERROR", response.getResult());
            assertEquals("시간 제한 테스트", response.getMessage());
            log.debug("시간 제한 테스트 완료");
        }
    }

    @AfterAll
    static void afterAll() {
        // 모든 테스트가 끝난 후 한 번만 실행
        System.out.println("=== Response 테스트 종료 ===");
        log.info("테스트 종료");
    }
}
