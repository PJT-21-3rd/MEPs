package org.meps.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** BusinessException이 가리키는 HTTP 상태·기본 메시지 */
@Getter
public enum ErrorCode {

    // common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "요청 파라미터가 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),
    NOT_FOUND_URL(HttpStatus.NOT_FOUND, "존재하지 않는 경로입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // building
    BUILDING_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 건물입니다."),
    INVALID_BOUNDS(HttpStatus.BAD_REQUEST, "좌표 범위가 올바르지 않습니다."),
    INVALID_BUILDING_ID(HttpStatus.BAD_REQUEST, "buildingId 형식이 올바르지 않습니다."),
    INVALID_KEYWORD(HttpStatus.BAD_REQUEST, "keyword는 비어 있을 수 없습니다."),

    // hjd
    HJD_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 행정동입니다."),
    AI_BRIEFING_NOT_AVAILABLE(HttpStatus.BAD_GATEWAY, "AI 브리핑이 아직 생성되지 않았습니다."),

    // sgg
    SGG_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 구(시군구) 코드입니다."),

    // insurance
    INVALID_FACTOR(HttpStatus.BAD_REQUEST, "유효하지 않은 진단 요소입니다."),

    // user / auth
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    ALREADY_SAVED(HttpStatus.CONFLICT, "이미 찜한 건물입니다."),

    // external / infra
    GEOCODING_FAILED(HttpStatus.BAD_GATEWAY, "지오코딩/외부 API 호출에 실패했습니다."),
    LLM_CALL_FAILED(HttpStatus.BAD_GATEWAY, "AI 응답 생성에 실패했습니다."),

    // flood
    INVALID_FLOOD_GRADE(HttpStatus.INTERNAL_SERVER_ERROR, "정의되지 않은 침수 등급 데이터입니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
