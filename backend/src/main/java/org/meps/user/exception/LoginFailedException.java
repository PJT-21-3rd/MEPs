package org.meps.user.exception;

/** 로그인 실패 → 401 */
public class LoginFailedException extends RuntimeException {
    public LoginFailedException() {
        super("이메일 또는 비밀번호가 올바르지 않습니다.");
    }
}