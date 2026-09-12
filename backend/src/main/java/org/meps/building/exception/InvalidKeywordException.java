package org.meps.building.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class InvalidKeywordException extends BusinessException {
    public InvalidKeywordException() {
        super(ErrorCode.INVALID_KEYWORD);
    }
}
