package org.meps.user.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String email) {
        super(ErrorCode.DUPLICATE_EMAIL, "이미 가입된 이메일입니다: " + email);
    }
}
