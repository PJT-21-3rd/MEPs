package org.meps.building.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class InvalidBoundsException extends BusinessException {
    public InvalidBoundsException(String reason, double swLat, double swLng, double neLat, double neLng) {
        super(ErrorCode.INVALID_BOUNDS, String.format("%s (swLat=%.6f, swLng=%.6f, neLat=%.6f, neLng=%.6f)",
                reason, swLat, swLng, neLat, neLng));
    }
}
