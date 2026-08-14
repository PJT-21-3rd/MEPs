package org.meps.user.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class PasswordMismatchException extends BusinessException {
    public PasswordMismatchException() {
        super(ErrorCode.PASSWORD_MISMATCH);
    }
}