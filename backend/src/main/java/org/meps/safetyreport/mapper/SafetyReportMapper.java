package org.meps.safetyreport.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.SafetyReportRowDto;

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

    /** 브리핑 저장 + brief_status DONE 전이를 한 문장으로 수행 (원자적) */
    void updateBriefs(
            @Param("bdMgtSn") String bdMgtSn,
            @Param("aiModelNm") String aiModelNm,
            @Param("briefs") BasicBriefingDto briefs
    );

    /**
     * 브리핑 생성 권한 클레임. WHERE 조건 + affected rows로 원자적 리더 선출 —
     * 1을 받은 호출자만 리더다 (SELECT 후 UPDATE 분리 금지)
     */
    int tryClaimBriefGeneration(
            @Param("bdMgtSn") String bdMgtSn
    );

    /** 리더의 생성 실패 시 클레임 반납 — brief_status를 NULL로 되돌려 다음 요청의 재시도를 연다 */
    void releaseBriefClaim(
            @Param("bdMgtSn") String bdMgtSn
    );

    /** ai_model_nm은 건드리지 않는다 — 그 값은 점수 upsert 시점(기본 리포트 조회)에만 기록된다 */
    void updateReports(
            @Param("bdMgtSn") String bdMgtSn,
            @Param("totalReport") String totalReport,
            @Param("floodReport") String floodReport,
            @Param("sinkReport") String sinkReport,
            @Param("fireReport") String fireReport,
            @Param("structReport") String structReport
    );
}
