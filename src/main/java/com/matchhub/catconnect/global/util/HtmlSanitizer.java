package com.matchhub.catconnect.global.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * HTML 콘텐츠 XSS 방어를 위한 Sanitizer
 */
@Component
public class HtmlSanitizer {

    private final Safelist safelist;

    public HtmlSanitizer() {
        // Toast UI Editor에서 생성하는 HTML 태그들을 허용
        this.safelist = Safelist.relaxed()
                // 기본 서식
                .addTags("div", "span", "br", "hr")
                // 코드 블록
                .addTags("pre", "code")
                .addAttributes("pre", "class")
                .addAttributes("code", "class")
                // 테이블
                .addTags("table", "thead", "tbody", "tr", "th", "td")
                .addAttributes("table", "class")
                .addAttributes("th", "class", "colspan", "rowspan")
                .addAttributes("td", "class", "colspan", "rowspan")
                // 체크리스트
                .addTags("input")
                .addAttributes("input", "type", "checked", "disabled")
                // 링크
                .addAttributes("a", "href", "target", "rel")
                .addProtocols("a", "href", "http", "https", "mailto")
                // 이미지
                .addAttributes("img", "src", "alt", "title", "width", "height")
                .addProtocols("img", "src", "http", "https", "data")
                // 일반 속성
                .addAttributes(":all", "class", "id", "style");
    }

    /**
     * HTML 콘텐츠를 sanitize하여 XSS 공격 방지
     * @param html 원본 HTML
     * @return sanitize된 HTML
     */
    public String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }
        return Jsoup.clean(html, safelist);
    }
}
