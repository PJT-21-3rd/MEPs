package collectors.buildings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 수집 대상 건물 판정 필터
 *
 * 기준: 요식업이 입점할 수 있는 건물
 *   건축물대장 용도는 등재 시점의 법정 용도라 현재 영업 업종과 다를 수 있다.
 *   (제조업소로 등재된 자리에 음식점이 영업 중인 경우 등)
 *
 * 방식: 블랙리스트
 *   상업 용도를 나열하면 빠뜨린 업종이 영구 누락되므로,
 *   명백한 비대상(주거·설비·학교·종교)만 제외하고 나머지는 모두 포함한다.
 *
 * 판정 단위: 건물.
 *   층 하나라도 제외 목록에 없는 용도가 있으면 수집 대상.
 *   (1층 소매점 + 2~3층 단독주택 건물은 포함, 전층 주거만 제외)
 */
public class BuildingFilter {

    private static final ObjectMapper OM = new ObjectMapper();

    // 주거
    private static final Set<String> RESIDENTIAL = new HashSet<>(Arrays.asList(
            "단독주택", "다가구주택", "다세대주택", "아파트", "연립주택",
            "도시형생활주택", "다중주택", "기숙사",
            "오피스텔", "고시원", "다중생활시설"
    ));

    // 설비
    private static final Set<String> FACILITY = new HashSet<>(Arrays.asList(
            "주차장", "차고", "창고", "일반창고", "기타창고시설",
            "부대시설", "대피소", "공중화장실", "계단실", "승강기",
            "기계실", "전기실", "물탱크실"
    ));

    // 기타
    private static final Set<String> NON_LEASABLE = new HashSet<>(Arrays.asList(
            "초등학교", "중학교", "고등학교", "대학교", "유치원",
            "교육(연수)원", "직업훈련소",
            "교회", "성당", "기타종교집회장", "기타종교시설"
    ));

    public static boolean isTarget(String floorInfoJson, String mainPurps) {
        // 층별현황이 없는 건물은 주용도로 판단
        if (floorInfoJson == null || "[]".equals(floorInfoJson)) {
            return mainPurps != null && !isExcluded(mainPurps);
        }

        try {
            JsonNode floors = OM.readTree(floorInfoJson);
            for (JsonNode floor : floors) {
                JsonNode purps = floor.get("mainPurpsNm");
                if (purps == null || purps.isNull()) continue;
                if (!isExcluded(purps.asText().trim())) {
                    return true;   // 대상 용도 층 발견
                }
            }
            return false;   // 모든 층이 제외 대상

        } catch (Exception e) {
            // 파싱 실패 시 주용도로 판단 (데이터를 잃지 않는 쪽)
            return mainPurps != null && !isExcluded(mainPurps);
        }
    }

    private static boolean isExcluded(String purps) {
        return RESIDENTIAL.contains(purps)
                || FACILITY.contains(purps)
                || NON_LEASABLE.contains(purps);
    }

    private BuildingFilter() {}
}