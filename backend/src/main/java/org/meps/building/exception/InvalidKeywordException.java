package org.meps.building.exception;

public class InvalidKeywordException extends RuntimeException {
    public InvalidKeywordException() {
        super("keyword는 비어 있을 수 없습니다.");
    }
}
