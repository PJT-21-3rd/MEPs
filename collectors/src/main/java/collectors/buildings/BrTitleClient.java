package collectors.buildings;

import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 건축물대장 표제부 조회 (공공데이터포털)
 * https://apis.data.go.kr/1613000/BldRgstHubService/getBrTitleInfo
 *
 * 담당 컬럼: road_addr, jibun_addr, main_purps, bld_nm,
 *           grnd_flr, ugrnd_flr, use_apr_day, ho_cnt, strct_cd_nm
 */
public class BrTitleClient {

    private static final String BASE_URL =
            "https://apis.data.go.kr/1613000/BldRgstHubService/getBrTitleInfo";
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
                + "&pageNo=1"          // 필수 — 누락 시 1건만 반환됨
                + "&_type=json";

        try {
            JsonNode root = OM.readTree(DataGoClient.get(url));
            checkResultCode(root);
            List<JsonNode> result = items(root);

            // 조용한 데이터 손실 방지 — 받은 건수가 전체보다 적으면 경고
            int totalCount = root.path("response").path("body").path("totalCount").asInt(0);
            if (totalCount > result.size()) {
                System.err.println("[경고] 표제부 일부 누락: " + result.size() + "/" + totalCount
                        + "건 (pnu=" + pnu + ") — 페이징 필요");
            }
            return result;

        } catch (ApiLimitException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("표제부 파싱 실패 (pnu=" + pnu + "): " + e.getMessage(), e);
        }
    }

    public static JsonNode match(List<JsonNode> items, JusoBuildingIndex.Row juso) {
        if (items.isEmpty()) return null;
        if (items.size() == 1) return items.get(0);

        List<JsonNode> pool = items;

        // 1. 주소키 대조
        if (juso != null) {
            List<JsonNode> addrMatched = new ArrayList<>();
            for (JsonNode item : items) {
                if (juso.roadCd.equals(text(item, "naRoadCd"))
                        && juso.undgrndYn.equals(text(item, "naUgrndCd"))
                        && juso.mainNo == intOr(item, "naMainBun", -1)
                        && juso.subNo == intOr(item, "naSubBun", -1)) {
                    addrMatched.add(item);
                }
            }
            if (addrMatched.size() == 1) return addrMatched.get(0);
            if (!addrMatched.isEmpty()) pool = addrMatched;

            // 2. 동명 대조
            if (juso.dongNm != null) {
                for (JsonNode item : pool) {
                    if (juso.dongNm.equals(text(item, "dongNm"))) return item;
                }
            }
        }

        // 3. 주건축물 우선
        for (JsonNode item : pool) {
            if ("0".equals(text(item, "mainAtchGbCd"))) return item;
        }
        return pool.get(0);
    }

    public static String mgmBldrgstPk(JsonNode item) {
        return text(item, "mgmBldrgstPk");
    }

    // ---- 값 추출 ----

    public static String roadAddr(JsonNode item)   { return text(item, "newPlatPlc"); }
    public static String jibunAddr(JsonNode item)  { return text(item, "platPlc"); }
    public static String mainPurps(JsonNode item)  { return text(item, "mainPurpsCdNm"); }
    public static String bldNm(JsonNode item)      { return text(item, "bldNm"); }
    public static String strctCdNm(JsonNode item)  { return text(item, "strctCdNm"); }
    public static String useAprDay(JsonNode item)  { return text(item, "useAprDay"); }

    public static Integer grndFlr(JsonNode item)   { return intVal(item, "grndFlrCnt"); }
    public static Integer ugrndFlr(JsonNode item)  { return intVal(item, "ugrndFlrCnt"); }
    public static Integer hoCnt(JsonNode item)     { return intVal(item, "hoCnt"); }

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

    private static Integer intVal(JsonNode item, String field) {
        JsonNode v = item.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText().trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int intOr(JsonNode item, String field, int def) {
        Integer v = intVal(item, field);
        return v == null ? def : v;
    }

    private BrTitleClient() {}
}