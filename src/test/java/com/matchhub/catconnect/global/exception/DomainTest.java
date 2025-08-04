package com.matchhub.catconnect.global.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainTest {

    // Domain enum의 값과 각 설명(description)이 올바르게 설정되어 있는지 테스트
    @Test
    void testDomainValues() {
        // Domain enum에 총 4개의 값이 있는지 확인
        assertEquals(4, Domain.values().length);

        // 각 enum 상수의 설명이 올바른지 확인
        assertEquals("사용자", Domain.USER.getDescription());
        assertEquals("게시글", Domain.BOARD.getDescription());
        assertEquals("댓글", Domain.COMMENT.getDescription());
        assertEquals("좋아요", Domain.LIKE.getDescription());
    }

    // Domain enum의 동작을 테스트
    @Test
    void testDomainEnum() {
        // 문자열 "USER"를 이용해서 Domain.USER 값을 가져올 수 있는지 확인
        assertEquals(Domain.USER, Domain.valueOf("USER"));

        // 존재하지 않는 문자열로 enum을 찾으면 예외가 발생하는지 확인
        assertThrows(IllegalArgumentException.class, () -> Domain.valueOf("INVALID"));
    }

}