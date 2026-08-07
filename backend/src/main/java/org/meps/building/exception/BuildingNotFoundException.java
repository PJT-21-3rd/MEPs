package org.meps.building.exception;

public class BuildingNotFoundException extends RuntimeException {
    public BuildingNotFoundException(String buildingId) {
        super("존재하지 않는 건물입니다: " + buildingId);
    }
}