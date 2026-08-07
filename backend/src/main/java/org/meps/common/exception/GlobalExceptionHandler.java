package org.meps.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.meps.building.exception.BuildingNotFoundException;
import org.meps.building.exception.InvalidBoundsException;
import org.meps.building.exception.InvalidBuildingIdException;
import org.meps.building.exception.InvalidKeywordException;
import org.meps.common.geocoding.GeocodingException;
import org.meps.hjd.exception.AiBriefingNotAvailableException;
import org.meps.hjd.exception.HjdNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 필수 파라미터 누락/타입 오류, 좌표 범위 오류, keyword 공백 → 400 */
    @ExceptionHandler({
            InvalidBoundsException.class,
            InvalidBuildingIdException.class,
            InvalidKeywordException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<Void> handleBadRequest(Exception e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().build();
    }

    /** 좌표에 대응하는 행정동 없음 → 404 */
    @ExceptionHandler(HjdNotFoundException.class)
    public ResponseEntity<Void> handleHjdNotFound(HjdNotFoundException e) {
        log.warn("조회 결과 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /** 지오코딩/외부 API 실패 → 502 */
    @ExceptionHandler(GeocodingException.class)
    public ResponseEntity<Void> handleGeocodingFailure(GeocodingException e) {
        log.error("지오코딩/외부 API 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

    /** 매핑되지 않은 URL → 404 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Void> handleNoHandlerFound(NoHandlerFoundException e) {
        log.warn("존재하지 않는 경로 요청: {} {}", e.getHttpMethod(), e.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }


    /** 존재하지 않는 건물 → 404 */
    @ExceptionHandler(BuildingNotFoundException.class)
    public ResponseEntity<Void> handleBuildingNotFound(BuildingNotFoundException e) {
        log.warn("건물 조회 결과 없음: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /** 그 외 미처리 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /** 행정동은 존재하나 AI 브리핑이 아직 생성되지 않음 (배치 미처리) → 502 */
    @ExceptionHandler(AiBriefingNotAvailableException.class)
    public ResponseEntity<Void> handleAiBriefingNotAvailable(AiBriefingNotAvailableException e) {
        log.warn("AI 브리핑 미생성: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
}
