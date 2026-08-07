package org.meps.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.report.dto.SafetyBriefingDto;
import org.meps.report.dto.SafetyReportRowDto;

@Mapper
public interface SafetyReportMapper {

    SafetyReportRowDto findByBdMgtSn(
            @Param("bdMgtSn") String bdMgtSn
    );

    /**
     * 점수 upsert. 점수가 바뀌었을 때만 호출되는 전제라, 기존 행 갱신 시
     * 무효해진 brief·report 문장을 같은 문장 안에서 NULL로 리셋한다 (원자적)
     */
    void upsertScores(
            @Param("bdMgtSn") String bdMgtSn,
            @Param("aiModelNm") String aiModelNm,
            @Param("totalScore") int totalScore,
            @Param("floodScore") int floodScore,
            @Param("sinkScore") int sinkScore,
            @Param("fireScore") int fireScore,
            @Param("structScore") int structScore
    );

    void updateBriefs(
            @Param("bdMgtSn") String bdMgtSn,
            @Param("aiModelNm") String aiModelNm,
            @Param("briefs") SafetyBriefingDto briefs
    );
}
