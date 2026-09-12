package org.meps.user.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class AlreadySavedException extends BusinessException {
    public AlreadySavedException(String buildingId) {
        super(ErrorCode.ALREADY_SAVED, "이미 찜한 건물입니다: " + buildingId);
    }
}
