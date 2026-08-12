package org.meps.common.auth;

/** 인증 필수 API에 비로그인(토큰 부재·무효) 접근 → 401 */
public class LoginRequiredException extends RuntimeException {
    public LoginRequiredException(String message) {
        super(message);
    }
}
