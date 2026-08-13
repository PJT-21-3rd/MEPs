package org.meps.hjd.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class HjdNotFoundException extends BusinessException {
    public HjdNotFoundException(double lat, double lng) {
        super(ErrorCode.HJD_NOT_FOUND,
                String.format("해당 좌표(lat=%.6f, lng=%.6f)에 대응하는 행정동을 찾을 수 없습니다.", lat, lng));
    }

    public HjdNotFoundException(String hjdCd) {
        super(ErrorCode.HJD_NOT_FOUND, String.format("존재하지 않는 행정동 코드입니다: %s", hjdCd));
    }
}
