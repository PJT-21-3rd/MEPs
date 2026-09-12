package org.meps.common.auth;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

/** 인증 필수 API에 비로그인(토큰 부재·무효) 접근 → 401 */
public class LoginRequiredException extends BusinessException {
    public LoginRequiredException(String message) {
        super(ErrorCode.LOGIN_REQUIRED, message);
    }
}
