package org.meps.common.exception;

public record ErrorResponse(String code) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name());
    }
}
