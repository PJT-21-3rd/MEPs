package collectors.buildings;

import collectors.common.Config;
import collectors.buildings.PnuParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 건축물대장 층별개요 조회 (공공데이터포털)
 * https://apis.data.go.kr/1613000/BldRgstHubService/getBrFlrOulnInfo
 *
 * 담당 컬럼: floor_info (JSON)
 * API 명세 응답의 floors 배열 형식과 동일하게 저장:
 * [{ "flrGbNm": "지상", "flrNoNm": "1층", "mainPurpsNm": "...", "etcPurps": "..." }, ...]
 *
 * 주의: 층별개요는 "층당 1건"이 아니라 "층×용도당 1건".
 *      예) 1층에 음식점+소매점이 있으면 1층 레코드가 2건.
 */
public class BrFloorClient {

    private static final String BASE_URL =
            "https://apis.data.go.kr/1613000/BldRgstHubService/getBrFlrOulnInfo";
    private static final ObjectMapper OM = new ObjectMapper();

    private static final int PAGE_SIZE = 100;

    public static List<JsonNode> fetch(String pnu) {
        PnuParser p = new PnuParser(pnu);

        String url = BASE_URL
                + "?serviceKey=" + Config.get("datago.key")
                + "&sigunguCd=" + p.sigunguCd()
                + "&bjdongCd=" + p.bjdongCd()
                + "&platGbCd=" + p.platGbCd()
                + "&bun=" + p.bun()
                + "&ji=" + p.ji()
                + "&numOfRows=" + PAGE_SIZE
                + "&pageNo=1"
                + "&_type=json";

        try {
            JsonNode root = OM.readTree(DataGoClient.get(url));
            checkResultCode(root);
            List<JsonNode> result = items(root);

            // 100건 초과 필지(대형 복합건물)는 데이터가 조용히 잘리므로 경고
            int totalCount = root.path("response").path("body").path("totalCount").asInt(0);
            if (totalCount > result.size()) {
                System.err.println("[경고] 층별개요 일부 누락: " + result.size() + "/" + totalCount
                        + "건 (pnu=" + pnu + ") — 페이징 필요");
            }
            return result;

        } catch (ApiLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("층별개요 파싱 실패 (pnu=" + pnu + "): " + e.getMessage(), e);
        }
    }

    public static String toFloorInfoJson(List<JsonNode> items, String mgmBldrgstPk) {
        List<JsonNode> mine = new ArrayList<>();
        for (JsonNode item : items) {
            if (mgmBldrgstPk != null && mgmBldrgstPk.equals(text(item, "mgmBldrgstPk"))) {
                mine.add(item);
            }
        }

        mine.sort((a, b) -> Integer.compare(sortKey(a), sortKey(b)));

        ArrayNode arr = OM.createArrayNode();
        for (JsonNode item : mine) {
            ObjectNode floor = OM.createObjectNode();
            floor.put("flrGbNm", text(item, "flrGbCdNm"));
            floor.put("flrNoNm", text(item, "flrNoNm"));
            floor.put("mainPurpsNm", text(item, "mainPurpsCdNm"));
            floor.put("etcPurps", text(item, "etcPurps"));
            arr.add(floor);
        }
        return arr.toString();
    }

    private static int sortKey(JsonNode item) {
        JsonNode flrNo = item.get("flrNo");
        int no = (flrNo == null || flrNo.isNull()) ? 0 : flrNo.asInt(0);

        String gb = text(item, "flrGbCdNm");
        if (gb == null) return no;
        if (gb.contains("지하")) return -no;
        if (gb.contains("옥탑")) return 1000 + no;
        return no;
    }

    // ---- 공통 파싱 ----

    private static void checkResultCode(JsonNode root) {
        JsonNode header = root.path("response").path("header");
        String code = header.path("resultCode").asText("");
        String msg = header.path("resultMsg").asText("");

        if (code.isEmpty() || "00".equals(code) || "03".equals(code)) return;

        // 22: 일일 한도 초과, 30~32: 키 미등록/미승인/기한만료
        if ("22".equals(code) || code.startsWith("3")) {
            throw new ApiLimitException("API 호출 중단 (resultCode=" + code + ", " + msg + ")");
        }
        throw new RuntimeException("API 오류 (resultCode=" + code + ", " + msg + ")");
    }

    private static List<JsonNode> items(JsonNode root) {
        List<JsonNode> result = new ArrayList<>();
        JsonNode item = root.path("response").path("body").path("items").path("item");

        if (item.isMissingNode() || item.isNull()) return result;

        if (item.isArray()) {
            item.forEach(result::add);
        } else {
            result.add(item);
        }
        return result;
    }

    private static String text(JsonNode item, String field) {
        JsonNode v = item.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText().trim();
        return s.isEmpty() ? null : s;
    }

    private BrFloorClient() {}
}