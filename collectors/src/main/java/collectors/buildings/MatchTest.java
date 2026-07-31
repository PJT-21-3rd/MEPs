package collectors.buildings;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * 다동 필지 매칭 검증 (일회성 테스트)
 *
 * 검증 대상: 한 필지에 건물 2동이 있을 때, juso 다리를 통해
 *          각 건물이 서로 다른(올바른) 표제부에 매칭되는지.
 *
 * 테스트 필지: 광진구 중곡동 245-2 — A동 / B동
 *   두 동의 도로명주소 건물본번이 101 / 103으로 달라서
 *   주소키 매칭(1단)이 작동해야 하고, 동명(2단)으로도 교차 확인 가능.
 *
 * 검증 통과 조건: 두 건물의 매칭 결과 mgmBldrgstPk가 서로 달라야 함.
 *              (같으면 = 두 건물이 같은 표제부를 가리킴 = 매칭 실패)
 */
public class MatchTest {

    private static final String JUSO_FILE = "/Users/home/Desktop/build_seoul.txt";

    private static final String PNU = "1121510100102450002";
    private static final String[] TARGETS = {
            "1121510100102450002004406",   // A동
            "1121510100102450002004407"    // B동
    };

    public static void main(String[] args) {
        JusoBuildingIndex.load(JUSO_FILE);

        List<JsonNode> titles = BrTitleClient.fetch(PNU);
        System.out.println("표제부 후보: " + titles.size() + "건 (pnu=" + PNU + ")");
        for (JsonNode t : titles) {
            System.out.println("   PK=" + BrTitleClient.mgmBldrgstPk(t)
                    + " 도로키=" + t.path("naRoadCd").asText() + "/" + t.path("naUgrndCd").asText()
                    + "/" + t.path("naMainBun").asText() + "/" + t.path("naSubBun").asText()
                    + " 동명=[" + t.path("dongNm").asText() + "]"
                    + " 주부속=" + t.path("mainAtchGbCdNm").asText()
                    + " 건축면적=" + t.path("archArea").asText());
        }
        System.out.println("---------------------------");

        String[] matchedPk = new String[TARGETS.length];

        for (int i = 0; i < TARGETS.length; i++) {
            String bdMgtSn = TARGETS[i];
            JusoBuildingIndex.Row juso = JusoBuildingIndex.find(bdMgtSn);

            if (juso == null) {
                System.out.println(bdMgtSn + " → juso 다리 없음");
                continue;
            }
            System.out.println(bdMgtSn);
            System.out.println("   juso  도로키=" + juso.roadCd + "/" + juso.undgrndYn
                    + "/" + juso.mainNo + "/" + juso.subNo
                    + " 동명=[" + juso.dongNm + "]");

            JsonNode matched = BrTitleClient.match(titles, juso);
            if (matched == null) {
                System.out.println("   → 매칭 실패");
                continue;
            }
            matchedPk[i] = BrTitleClient.mgmBldrgstPk(matched);
            System.out.println("   → 매칭 PK=" + matchedPk[i]
                    + " 용도=" + BrTitleClient.mainPurps(matched)
                    + " 지상" + BrTitleClient.grndFlr(matched)
                    + "/지하" + BrTitleClient.ugrndFlr(matched)
                    + " 승인=" + BrTitleClient.useAprDay(matched));
        }

        System.out.println("---------------------------");
        boolean distinct = matchedPk[0] != null && matchedPk[1] != null
                && !matchedPk[0].equals(matchedPk[1]);
        System.out.println(distinct
                ? "PASS — 두 건물이 서로 다른 표제부에 매칭됨"
                : "FAIL — 같은 표제부를 가리키거나 매칭 실패");
    }
}