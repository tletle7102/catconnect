package com.matchhub.catconnect.global.exception;

// 커스텀 예외 클래스. 런타임 시 발생할 수 있는 예외를 표현하며, 도메인과 에러 코드 포함
public class AppException extends RuntimeException {

    // 예외가 발생한 도메인 (예: USER, BOARD 등)
    private final Domain domain;

    // 예외의 상세 정보가 담긴 ErrorCode (예: USER_NOT_FOUND, INTERNAL_SERVER_ERROR 등)
    private final ErrorCode errorCode;

    /**
     * 에러 메시지를 직접 전달받는 생성자
     *
     * @param domain     예외가 발생한 도메인
     * @param errorCode  에러 코드 (에러 메시지, 상태 코드 포함)
     * @param message    사용자 정의 에러 메시지 (null이면 ErrorCode에 정의된 메시지 사용)
     */
    public AppException(Domain domain, ErrorCode errorCode, String message) {
        // 부모 클래스(RuntimeException)에 메시지 전달
        // 전달된 message가 null이면 ErrorCode에서 기본 메시지 사용
        super(message != null ? message : errorCode.getMessage());
        this.domain = domain;
        this.errorCode = errorCode;
    }

    /**
     * 메시지를 전달하지 않을 때 사용하는 생성자
     * ErrorCode의 기본 메시지 사용
     */
    public AppException(Domain domain, ErrorCode errorCode) {
        // 위의 생성자를 호출하며 message는 null로 전달
        this(domain, errorCode, null);
    }

    public Domain getDomain() {
        return domain;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
