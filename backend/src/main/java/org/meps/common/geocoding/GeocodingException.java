package org.meps.common.geocoding;

/** 지오코딩/외부 API 실패 → 502 */
public class GeocodingException extends RuntimeException {
    public GeocodingException(String message) {
        super(message);
    }

    public GeocodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
