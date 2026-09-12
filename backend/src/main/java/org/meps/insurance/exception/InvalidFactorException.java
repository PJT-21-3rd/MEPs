package org.meps.insurance.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class InvalidFactorException extends BusinessException {
    public InvalidFactorException(String message) {
        super(ErrorCode.INVALID_FACTOR, message);
    }
}