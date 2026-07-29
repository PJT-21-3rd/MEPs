package collectors.fire;

import collectors.common.Db;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 화재 2단계: fire 테이블 적재
 * 입력: data/fire.json ({gu, dong, year, cnt})
 * - adstrd(adstrd_cd, adstrd_nm)에서 "구 동" 이름 → 행정동코드(hjd_cd) 매핑
 * - 이름 비교 시 공백/구분자(·.,) 제거로 표기 차이 흡수
 * - adstrd는 '창신제1동'처럼 숫자 앞에 '제'를 쓰는 동이 있어 원형 키와 '제' 제거 키를 함께 등록
 *   ('홍제제1동' → '홍제1동'처럼 기본 이름이 '제'로 끝나는 동이 있어 통계표 쪽 이름은 변형하지 않음)
 * - 통계표에만 있는 동은 ALIAS 로 현재 adstrd 코드에 귀속 (adstrd 기준 적재)
 *   상일동(2020)은 현 강일동+상일제1·2동 세 지역으로 분할되어 귀속 불가 → 제외 (스킵 목록에 표시됨)
 * - (year, hjd_cd) 유니크 키가 없으면 생성 후 ON DUPLICATE KEY UPDATE 로 멱등 적재
 * - adstrd에 없는 동(분동 전 옛 동 등)은 적재하지 않고 유사 이름 후보와 함께 출력 → 수작업 판단
 */
public class FireLoader {

    private static final Map<String, String> ALIAS = Map.of(
            "강남구 개포3동", "강남구 일원2동",     // 2022년 일원2동 → 개포3동 개명, adstrd는 개명 전 기준
            "동대문구 신설동", "동대문구 용신동",   // 2025년 용신동에서 분동, 용신동으로 합산
            "동대문구 용두동", "동대문구 용신동",
            "중랑구 면목본동", "중랑구 면목동단동"); // adstrd 표기 그대로 사용

    private static final List<String> adstrdNames = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        JsonNode list = new ObjectMapper().readTree(
                Files.readString(Path.of("data", "fire.json")));

        try (Connection conn = Db.connect()) {
            ensureUniqueKey(conn);

            Map<String, String> adstrd = loadAdstrdMap(conn);
            Map<String, Integer> unmatched = new LinkedHashMap<>();

            String sql = """
                    INSERT INTO fire (year, cnt, hjd_cd)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE cnt = VALUES(cnt)
                    """;

            // 신설동·용두동 → 용신동처럼 두 동이 한 코드로 귀속되면 합산해야 하므로 먼저 집계
            Map<String, Integer> agg = new LinkedHashMap<>();  // key = year|hjd_cd
            for (JsonNode item : list) {
                String name = item.path("gu").asText() + " " + item.path("dong").asText();
                String cd = adstrd.get(normalize(ALIAS.getOrDefault(name, name)));
                if (cd == null) {
                    unmatched.merge(name, 1, Integer::sum);
                    continue;
                }
                agg.merge(item.path("year").asText() + "|" + cd, item.path("cnt").asInt(), Integer::sum);
            }

            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int count = 0;
                for (Map.Entry<String, Integer> e : agg.entrySet()) {
                    String[] key = e.getKey().split("\\|");
                    ps.setString(1, key[0]);
                    ps.setInt(2, e.getValue());
                    ps.setString(3, key[1]);

                    ps.addBatch();
                    if (++count % 500 == 0) ps.executeBatch();
                }
                ps.executeBatch();
                conn.commit();
                System.out.printf("적재 완료: %d건%n", count);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

            if (!unmatched.isEmpty()) {
                System.out.printf("adstrd 미매칭으로 스킵된 동 %d개:%n", unmatched.size());
                unmatched.forEach((name, cnt) ->
                        System.out.printf("  %s (%d건) / adstrd 후보: %s%n", name, cnt, candidates(name)));
            }
        }
    }

    private static void ensureUniqueKey(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("""
                    SELECT COUNT(*) FROM information_schema.statistics
                    WHERE table_schema = DATABASE() AND table_name = 'fire'
                      AND index_name = 'uk_fire_year_hjd'
                    """);
            rs.next();
            if (rs.getInt(1) == 0) {
                st.executeUpdate("ALTER TABLE fire ADD UNIQUE KEY uk_fire_year_hjd (year, hjd_cd)");
                System.out.println("유니크 키 생성: uk_fire_year_hjd (year, hjd_cd)");
            }
        }
    }

    private static Map<String, String> loadAdstrdMap(Connection conn) throws Exception {
        Map<String, String> map = new HashMap<>();
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT adstrd_cd, adstrd_nm FROM adstrd");
            while (rs.next()) {
                String nm = rs.getString("adstrd_nm");
                String cd = rs.getString("adstrd_cd");
                adstrdNames.add(nm);

                String key = normalize(nm);
                String prev = map.put(key, cd);
                if (prev != null) {
                    throw new IllegalStateException("정규화 후 동 이름 충돌: " + nm);
                }
                // '제N동' 표기 차이 흡수용 보조 키. 원형 키가 우선하도록 putIfAbsent
                String stripped = key.replaceAll("제(?=\\d)", "");
                if (!stripped.equals(key)) map.putIfAbsent(stripped, cd);
            }
        }
        if (map.isEmpty()) throw new IllegalStateException("adstrd 테이블이 비어 있음");
        return map;
    }

    /** 공백·구분자 표기 차이 흡수 (예: 종로1.2.3.4가동 / 종로1·2·3·4가동) */
    private static String normalize(String name) {
        return name.replaceAll("[\\s.·,]", "");
    }

    /** 미매칭 동에 대해 같은 구의 비슷한 이름(동 이름 앞 두 글자 일치)을 찾아준다 */
    private static String candidates(String name) {
        String[] parts = name.split(" ", 2);
        String prefix = parts[1].substring(0, Math.min(2, parts[1].length()));
        List<String> found = adstrdNames.stream()
                .filter(n -> n.startsWith(parts[0]) && n.substring(parts[0].length()).replace(" ", "").startsWith(prefix))
                .toList();
        return found.isEmpty() ? "(없음)" : String.join(", ", found);
    }
}
