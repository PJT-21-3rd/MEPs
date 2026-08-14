package org.meps.common.exception;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 모든 도메인 예외는 {@link BusinessException}을 상속하고 자신의 {@link ErrorCode}를 실어 던진다.
 * 여기서는 BusinessException으로 표현되지 않는 프레임워크/외부 라이브러리 예외만 매핑한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        if (errorCode.getStatus().is5xxServerError()) {
            log.error("[{}] {}", errorCode, e.getMessage(), e);
        } else {
            log.warn("[{}] {}", errorCode, e.getMessage());
        }
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.of(errorCode));
    }

    /** 400
     * 입력값 검증 실패(@Valid), 필수 파라미터 누락/타입 오류 */
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE));
    }

    /** 401
     * 토큰 없음/형식 오류/만료/위조 */
    @ExceptionHandler({
            JwtException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<ErrorResponse> handleUnauthorized(Exception e) {
        log.warn("인증 실패: {}", e.getMessage());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getStatus())
                .body(ErrorResponse.of(ErrorCode.UNAUTHORIZED));
    }

    /** 404
     * 매핑되지 않은 URL */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("존재하지 않는 경로 요청: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(ErrorCode.NOT_FOUND_URL.getStatus())
                .body(ErrorResponse.of(ErrorCode.NOT_FOUND_URL));
    }

    /** 500
     * 그 외 미처리 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
