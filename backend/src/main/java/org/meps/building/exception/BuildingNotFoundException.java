package org.meps.building.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class BuildingNotFoundException extends BusinessException {
    public BuildingNotFoundException(String buildingId) {
        super(ErrorCode.BUILDING_NOT_FOUND, "존재하지 않는 건물입니다: " + buildingId);
    }
}
