package org.meps.common.geocoding;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

/** 지오코딩/외부 API 실패 → 502 */
public class GeocodingException extends BusinessException {
    public GeocodingException(String message) {
        super(ErrorCode.GEOCODING_FAILED, message);
    }

    public GeocodingException(String message, Throwable cause) {
        super(ErrorCode.GEOCODING_FAILED, message, cause);
    }
}
