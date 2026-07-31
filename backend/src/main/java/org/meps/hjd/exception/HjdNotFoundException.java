package org.meps.hjd.exception;

public class HjdNotFoundException extends RuntimeException {
    public HjdNotFoundException(double lat, double lng) {
        super(String.format("해당 좌표(lat=%.6f, lng=%.6f)에 대응하는 행정동을 찾을 수 없습니다.", lat, lng));
    }
}