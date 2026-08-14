package org.meps.building.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

public class InvalidBuildingIdException extends BusinessException {
    public InvalidBuildingIdException(String buildingId) {
        super(ErrorCode.INVALID_BUILDING_ID, "buildingId 형식이 올바르지 않습니다: " + buildingId);
    }
}
