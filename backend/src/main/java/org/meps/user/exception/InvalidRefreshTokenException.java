package org.meps.user.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

/** 리프레시 토큰이 없거나 만료/무효 → 401 */
public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException(String message) {
        super(ErrorCode.INVALID_REFRESH_TOKEN, message);
    }
}