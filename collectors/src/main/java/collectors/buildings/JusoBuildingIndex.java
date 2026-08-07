package collectors.buildings;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * juso.go.kr 도로명주소 건물DB(build_seoul.txt) 메모리 인덱스
 *
 * 역할: WFS 건물과 건축물대장 표제부를 잇는 "다리"
 *
 * 문제 배경:
 *   WFS는 bd_mgt_sn(건물관리번호 25자리)로 건물을 식별하지만,
 *   건축물대장 표제부 응답에는 이 값이 없다(내부 PK인 mgmBldrgstPk만 존재).
 *   한 필지에 여러 동이 있으면 어느 표제부가 그 건물인지 가릴 수 없다.
 *
 * 해결:
 *   juso 건물DB에는 bd_mgt_sn과 도로명주소 키가 함께 들어 있고,
 *   표제부에도 같은 도로명주소 키(naRoadCd/naUgrndCd/naMainBun/naSubBun)가 있다.
 *   → bd_mgt_sn으로 도로명주소 키를 얻어 표제부와 대조하면 동을 특정할 수 있다.
 *
 * 파일 스펙 ('|' 구분, CP949, juso.go.kr 건물DB 레이아웃 기준):
 *   9번째  도로명코드(12)      → 표제부 naRoadCd
 *   11번째 지하여부(1)         → naUgrndCd
 *   12번째 건물본번(5)         → naMainBun
 *   13번째 건물부번(5)         → naSubBun
 *   15번째 상세건물명(동명)    → 다동 필지 2차 구분용
 *   16번째 건물관리번호(25) PK → bd_mgt_sn
 *   23번째 이동사유코드        → 63(폐지)은 인덱스에서 제외
 *
 * 한계: 대장·도로명주소 미등재 건물(철거 미반영, 무허가 등)은 이 인덱스에 없다.
 *      그 경우 find()가 null을 반환하고, 단일동 필지면 매칭이 그대로 성립한다.
 */

public class JusoBuildingIndex {

    public static class Row {
        public final String roadCd;     // 도로명코드 12자리
        public final String undgrndYn;  // 지하여부 0/1/2/3
        public final int mainNo;        // 건물본번
        public final int subNo;         // 건물부번
        public final String dongNm;     // 상세건물명(동명), 없으면 null
        public final String hjdCd;      // 행정동코드 (참고용)

        Row(String roadCd, String undgrndYn, int mainNo, int subNo, String dongNm, String hjdCd) {
            this.roadCd = roadCd;
            this.undgrndYn = undgrndYn;
            this.mainNo = mainNo;
            this.subNo = subNo;
            this.dongNm = dongNm;
            this.hjdCd = hjdCd;
        }
    }

    private static final Map<String, Row> INDEX = new HashMap<>(1_000_000);
    private static boolean loaded = false;

    public static void load(String filePath) {
        if (loaded) return;

        long start = System.currentTimeMillis();
        int total = 0, skipped = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath), Charset.forName("CP949")))) {

            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String[] c = line.split("\\|", -1);
                if (c.length < 27) { skipped++; continue; }

                String bdMgtSn = c[15].trim();
                if (bdMgtSn.length() != 25) { skipped++; continue; }

                if ("63".equals(c[22].trim())) { skipped++; continue; }

                INDEX.put(bdMgtSn, new Row(
                        c[8].trim(),               // 도로명코드
                        c[10].trim(),              // 지하여부
                        parseIntOr(c[11], -1),     // 건물본번
                        parseIntOr(c[12], -1),     // 건물부번
                        emptyToNull(c[14]),        // 상세건물명(동명)
                        emptyToNull(c[17])         // 행정동코드
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("juso 건물DB 로드 실패: " + filePath, e);
        }

        loaded = true;
        System.out.printf("juso 인덱스 로드: %,d건 (제외 %,d건, %.1f초)%n",
                INDEX.size(), skipped, (System.currentTimeMillis() - start) / 1000.0);
    }

    public static Row find(String bdMgtSn) {
        if (!loaded) throw new IllegalStateException("load()를 먼저 호출하세요");
        return INDEX.get(bdMgtSn);
    }

    private static int parseIntOr(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private JusoBuildingIndex() {}
}