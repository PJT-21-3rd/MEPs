package org.meps.sgg.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class SggNotFoundException extends BusinessException {
    public SggNotFoundException(String sggCd) {
        super(ErrorCode.SGG_NOT_FOUND, String.format("존재하지 않는 구(시군구) 코드입니다: %s", sggCd));
    }
}
