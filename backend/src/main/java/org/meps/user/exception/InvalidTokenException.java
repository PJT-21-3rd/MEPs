package org.meps.user.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
