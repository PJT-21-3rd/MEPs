package org.meps.common.util;

import java.util.regex.Pattern;

/** 주소 표시용 포맷팅 유틸 */
public final class AddressUtils {

    private static final Pattern TRAILING_PARENTHESES = Pattern.compile("\\s*\\([^)]*\\)$");

    private AddressUtils() {
    }

    /**
     * 지번주소 표시용 포맷팅: 끝의 "번지"를 제거한다.
     * "서울특별시 광진구 중곡동 143-130번지" → "서울특별시 광진구 중곡동 143-130"
     */
    public static String formatJibunAddr(String jibunAddr) {
        if (jibunAddr == null) {
            return null;
        }
        String trimmed = jibunAddr.trim();
        if (trimmed.endsWith("번지")) {
            return trimmed.substring(0, trimmed.length() - 2).trim();
        }
        return trimmed;
    }

    /**
     * 도로명주소 표시용 포맷팅: 끝의 참고항목 괄호를 제거한다.
     * "서울특별시 광진구 용마산로 160 (중곡동)" → "서울특별시 광진구 용마산로 160"
     */
    public static String formatRoadAddr(String roadAddr) {
        if (roadAddr == null) {
            return null;
        }
        return TRAILING_PARENTHESES.matcher(roadAddr.trim()).replaceFirst("").trim();
    }
}
