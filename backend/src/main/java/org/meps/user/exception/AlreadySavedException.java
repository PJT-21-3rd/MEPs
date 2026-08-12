package org.meps.user.exception;

public class AlreadySavedException extends RuntimeException {
    public AlreadySavedException(String buildingId) {
        super("이미 찜한 건물입니다: " + buildingId);
    }
}