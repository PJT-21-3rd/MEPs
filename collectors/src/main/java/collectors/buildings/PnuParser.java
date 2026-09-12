package collectors.buildings;

/**
 * PNU(19자리) → 건축물대장 API 조회 파라미터 분해
 *
 * PNU 구조: 시군구(5) + 법정동(5) + 필지구분(1) + 본번(4) + 부번(4)
 * 예) 1121510700102120000
 *     11215      광진구
 *     10700      화양동
 *     1          대지 (2=산)
 *     0212       본번 212
 *     0000       부번 없음
 */
public class PnuParser {

    private final String pnu;

    public PnuParser(String pnu) {
        if (pnu == null || pnu.length() != 19) {
            throw new IllegalArgumentException("PNU는 19자리여야 합니다: " + pnu);
        }
        this.pnu = pnu;
    }

    /** 시군구코드 5자리 */
    public String sigunguCd() {
        return pnu.substring(0, 5);
    }

    /** 법정동코드 5자리 */
    public String bjdongCd() {
        return pnu.substring(5, 10);
    }

    /**
     * 대지구분코드 — 건축물대장 API용
     * PNU 필지구분 1(대지) → "0", 2(산) → "1"
     */
    public String platGbCd() {
        char gb = pnu.charAt(10);
        if (gb == '1') return "0";
        if (gb == '2') return "1";
        throw new IllegalArgumentException("알 수 없는 필지구분: " + gb + " (pnu=" + pnu + ")");
    }

    /** 본번 4자리 (0채움 유지) */
    public String bun() {
        return pnu.substring(11, 15);
    }

    /** 부번 4자리 (0채움 유지) */
    public String ji() {
        return pnu.substring(15, 19);
    }
}