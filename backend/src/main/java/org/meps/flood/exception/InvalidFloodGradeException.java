package org.meps.flood.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

/** GRADE_WEIGHTS에 없는 침수 등급 값 — 원본 데이터 이상 → 500 */
public class InvalidFloodGradeException extends BusinessException {
    public InvalidFloodGradeException(int grade) {
        super(ErrorCode.INVALID_FLOOD_GRADE, "Unexpected flood grade: " + grade);
    }
}
