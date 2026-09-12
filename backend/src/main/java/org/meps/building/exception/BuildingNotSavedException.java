package org.meps.building.exception;

import org.meps.common.exception.BusinessException;
import org.meps.common.exception.ErrorCode;

import java.util.List;

public class BuildingNotSavedException extends BusinessException {
    public BuildingNotSavedException(List<String> buildingIds) {
        super(ErrorCode.BUILDING_NOT_SAVED, "찜하지 않은 매물이 포함되어 있습니다: " + buildingIds);
    }
}
