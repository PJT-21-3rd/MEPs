package org.meps.building.exception;

public class InvalidBoundsException extends RuntimeException {
    public InvalidBoundsException(String reason, double swLat, double swLng, double neLat, double neLng) {
        super(String.format("%s (swLat=%.6f, swLng=%.6f, neLat=%.6f, neLng=%.6f)",
                reason, swLat, swLng, neLat, neLng));
    }
}
