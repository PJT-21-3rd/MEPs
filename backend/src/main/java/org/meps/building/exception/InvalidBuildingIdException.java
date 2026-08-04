package org.meps.building.exception;

public class InvalidBuildingIdException extends RuntimeException {
    public InvalidBuildingIdException(String buildingId) {
        super("buildingId 형식이 올바르지 않습니다: " + buildingId);
    }
}