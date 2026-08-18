package org.meps.building.service;

import lombok.RequiredArgsConstructor;
import org.meps.building.dto.BuildingCompareDetailDto;
import org.meps.building.dto.BuildingCompareItemDto;
import org.meps.building.dto.BuildingCompareResponseDto;
import org.meps.building.exception.BuildingNotSavedException;
import org.meps.building.exception.InvalidCompareRequestException;
import org.meps.safetyreport.dto.BasicReportResponseDto;
import org.meps.safetyreport.service.BasicReportService;
import org.meps.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 찜한 매물 비교
 * 매물별로 기존 상세 조회(BuildingService)와 AI 안심 진단 기본 리포트(BasicReportService)를 재사용하여 조립한다.
 * 요청한 buildingIds는 모두 해당 사용자가 찜한 매물이어야 한다.
 */
@Service
@RequiredArgsConstructor
public class BuildingCompareService {

    private static final int MIN_COUNT = 2;
    private static final int MAX_COUNT = 3;

    private final BuildingService buildingService;
    private final BasicReportService basicReportService;
    private final UserMapper userMapper;

    public BuildingCompareResponseDto compare(List<String> buildingIds, Integer userId) {
        validate(buildingIds, userId);

        List<BuildingCompareItemDto> items = buildingIds.stream()
                .map(this::buildItem)
                .toList();

        return BuildingCompareResponseDto.builder().buildings(items).build();
    }

    private BuildingCompareItemDto buildItem(String buildingId) {
        BuildingCompareDetailDto building = buildingService.getBuildingCompareDetail(buildingId);

        BasicReportResponseDto safetyReport = basicReportService.getBasicReport(buildingId, true);
        return BuildingCompareItemDto.builder()
                .building(building)
                .safetyReport(safetyReport)
                .build();
    }

    private void validate(List<String> buildingIds, Integer userId) {
        if (buildingIds == null || buildingIds.size() < MIN_COUNT || buildingIds.size() > MAX_COUNT) {
            throw new InvalidCompareRequestException(
                    "비교할 매물은 " + MIN_COUNT + "~" + MAX_COUNT + "개여야 합니다: " + buildingIds);
        }
        if (buildingIds.stream().distinct().count() != buildingIds.size()) {
            throw new InvalidCompareRequestException("중복된 buildingId가 포함되어 있습니다: " + buildingIds);
        }

        List<String> savedIds = userMapper.findSavedBuildingIds(userId);
        if (!savedIds.containsAll(buildingIds)) {
            throw new BuildingNotSavedException(buildingIds);
        }
    }
}
