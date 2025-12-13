package com.matchhub.catconnect.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 검색 결과 페이지 뷰 컨트롤러
 * 검색 결과를 표시하는 HTML 페이지로 라우팅함
 */
@Controller
public class SearchViewController {

    private static final Logger log = LoggerFactory.getLogger(SearchViewController.class);

    /**
     * 검색 결과 페이지 요청 처리
     * @param keyword 검색 키워드
     * @param type 검색 타입 (ALL, BOARD, COMMENT, USER)
     * @param model Spring MVC 모델
     * @return 검색 결과 뷰 템플릿
     */
    @GetMapping("/search")
    public String searchResults(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            Model model) {
        log.debug("GET /search 요청: keyword={}, type={}", keyword, type);
        // View에서 사용할 검색 키워드와 타입을 모델에 추가
        model.addAttribute("keyword", keyword);
        model.addAttribute("type", type);
        model.addAttribute("currentPage", "search");
        return "common/search-results"; // templates/common/search-results.html
    }
}
