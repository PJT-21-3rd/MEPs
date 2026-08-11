package org.meps.insurance.exception;

/** factors 누락/공백 또는 유효하지 않은 진단 요소 코드 → 400 */
public class InvalidFactorException extends RuntimeException {
    public InvalidFactorException(String message) {
        super(message);
    }
}