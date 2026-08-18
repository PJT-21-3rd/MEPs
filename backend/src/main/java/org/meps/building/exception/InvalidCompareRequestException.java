package org.meps.building.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class InvalidCompareRequestException extends BusinessException {
    public InvalidCompareRequestException(String message) {
        super(ErrorCode.INVALID_COMPARE_REQUEST, message);
    }
}
