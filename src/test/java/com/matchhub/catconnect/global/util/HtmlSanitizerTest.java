package com.matchhub.catconnect.global.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HtmlSanitizer 단위 테스트
 */
@DisplayName("HtmlSanitizer 테스트")
class HtmlSanitizerTest {

    private static final Logger log = LoggerFactory.getLogger(HtmlSanitizerTest.class);

    private HtmlSanitizer htmlSanitizer;

    @BeforeEach
    void setUp() {
        htmlSanitizer = new HtmlSanitizer();
    }

    @Nested
    @DisplayName("XSS 공격 방어 테스트")
    class XssPreventionTests {

        @Test
        @DisplayName("script 태그 제거")
        void testRemoveScriptTag() {
            log.debug("script 태그 제거 테스트 시작");

            String malicious = "<p>안녕하세요</p><script>alert('XSS')</script>";
            String sanitized = htmlSanitizer.sanitize(malicious);

            assertFalse(sanitized.contains("<script>"));
            assertFalse(sanitized.contains("alert"));
            assertTrue(sanitized.contains("<p>안녕하세요</p>"));

            log.debug("script 태그 제거 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("onclick 이벤트 핸들러 제거")
        void testRemoveOnclickHandler() {
            log.debug("onclick 이벤트 핸들러 제거 테스트 시작");

            String malicious = "<button onclick=\"alert('XSS')\">클릭</button>";
            String sanitized = htmlSanitizer.sanitize(malicious);

            assertFalse(sanitized.contains("onclick"));
            assertFalse(sanitized.contains("alert"));

            log.debug("onclick 이벤트 핸들러 제거 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("onerror 이벤트 핸들러 제거")
        void testRemoveOnerrorHandler() {
            log.debug("onerror 이벤트 핸들러 제거 테스트 시작");

            String malicious = "<img src=\"invalid\" onerror=\"alert('XSS')\">";
            String sanitized = htmlSanitizer.sanitize(malicious);

            assertFalse(sanitized.contains("onerror"));
            assertFalse(sanitized.contains("alert"));

            log.debug("onerror 이벤트 핸들러 제거 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("javascript: URL 스킴 제거")
        void testRemoveJavascriptUrl() {
            log.debug("javascript: URL 스킴 제거 테스트 시작");

            String malicious = "<a href=\"javascript:alert('XSS')\">링크</a>";
            String sanitized = htmlSanitizer.sanitize(malicious);

            assertFalse(sanitized.contains("javascript:"));

            log.debug("javascript: URL 스킴 제거 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("iframe 태그 제거")
        void testRemoveIframeTag() {
            log.debug("iframe 태그 제거 테스트 시작");

            String malicious = "<iframe src=\"http://malicious.com\"></iframe>";
            String sanitized = htmlSanitizer.sanitize(malicious);

            assertFalse(sanitized.contains("<iframe"));

            log.debug("iframe 태그 제거 테스트 완료: sanitized={}", sanitized);
        }
    }

    @Nested
    @DisplayName("허용된 태그 보존 테스트")
    class AllowedTagsTests {

        @Test
        @DisplayName("기본 서식 태그 허용")
        void testAllowBasicFormatting() {
            log.debug("기본 서식 태그 허용 테스트 시작");

            String html = "<p><strong>굵은 글씨</strong> <em>기울임</em> <u>밑줄</u></p>";
            String sanitized = htmlSanitizer.sanitize(html);

            assertTrue(sanitized.contains("<strong>"));
            assertTrue(sanitized.contains("<em>"));
            assertTrue(sanitized.contains("<u>"));

            log.debug("기본 서식 태그 허용 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("목록 태그 허용")
        void testAllowListTags() {
            log.debug("목록 태그 허용 테스트 시작");

            String html = "<ul><li>항목 1</li><li>항목 2</li></ul>";
            String sanitized = htmlSanitizer.sanitize(html);

            assertTrue(sanitized.contains("<ul>"));
            assertTrue(sanitized.contains("<li>"));

            log.debug("목록 태그 허용 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("테이블 태그 허용")
        void testAllowTableTags() {
            log.debug("테이블 태그 허용 테스트 시작");

            String html = "<table><thead><tr><th>제목</th></tr></thead><tbody><tr><td>내용</td></tr></tbody></table>";
            String sanitized = htmlSanitizer.sanitize(html);

            assertTrue(sanitized.contains("<table>"));
            assertTrue(sanitized.contains("<thead>"));
            assertTrue(sanitized.contains("<tbody>"));
            assertTrue(sanitized.contains("<th>"));
            assertTrue(sanitized.contains("<td>"));

            log.debug("테이블 태그 허용 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("코드 블록 태그 허용")
        void testAllowCodeTags() {
            log.debug("코드 블록 태그 허용 테스트 시작");

            String html = "<pre><code class=\"language-java\">System.out.println(\"Hello\");</code></pre>";
            String sanitized = htmlSanitizer.sanitize(html);

            assertTrue(sanitized.contains("<pre>"));
            assertTrue(sanitized.contains("<code"));

            log.debug("코드 블록 태그 허용 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("안전한 링크 허용")
        void testAllowSafeLinks() {
            log.debug("안전한 링크 허용 테스트 시작");

            String html = "<a href=\"https://example.com\" target=\"_blank\">링크</a>";
            String sanitized = htmlSanitizer.sanitize(html);

            assertTrue(sanitized.contains("<a"));
            assertTrue(sanitized.contains("href=\"https://example.com\""));

            log.debug("안전한 링크 허용 테스트 완료: sanitized={}", sanitized);
        }

        @Test
        @DisplayName("이미지 태그 허용")
        void testAllowImageTags() {
            log.debug("이미지 태그 허용 테스트 시작");

            String html = "<img src=\"https://example.com/image.jpg\" alt=\"이미지\">";
            String sanitized = htmlSanitizer.sanitize(html);

            assertTrue(sanitized.contains("<img"));
            assertTrue(sanitized.contains("src=\"https://example.com/image.jpg\""));

            log.debug("이미지 태그 허용 테스트 완료: sanitized={}", sanitized);
        }
    }

    @Nested
    @DisplayName("빈 입력 처리 테스트")
    class EmptyInputTests {

        @Test
        @DisplayName("null 입력 처리")
        void testNullInput() {
            log.debug("null 입력 처리 테스트 시작");

            String result = htmlSanitizer.sanitize(null);

            assertNull(result);

            log.debug("null 입력 처리 테스트 완료");
        }

        @Test
        @DisplayName("빈 문자열 입력 처리")
        void testEmptyInput() {
            log.debug("빈 문자열 입력 처리 테스트 시작");

            String result = htmlSanitizer.sanitize("");

            assertEquals("", result);

            log.debug("빈 문자열 입력 처리 테스트 완료");
        }

        @Test
        @DisplayName("공백만 있는 입력 처리")
        void testBlankInput() {
            log.debug("공백만 있는 입력 처리 테스트 시작");

            String result = htmlSanitizer.sanitize("   ");

            assertEquals("   ", result);

            log.debug("공백만 있는 입력 처리 테스트 완료");
        }
    }
}
