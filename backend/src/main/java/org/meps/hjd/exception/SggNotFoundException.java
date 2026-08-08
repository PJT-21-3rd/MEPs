package org.meps.hjd.exception;

public class SggNotFoundException extends RuntimeException {
    public SggNotFoundException(String sggCd) {
        super(String.format("존재하지 않는 구(시군구) 코드입니다: %s", sggCd));
    }
}
