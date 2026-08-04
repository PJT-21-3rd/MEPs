package org.meps.common.geocoding;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class GeocodeResult {
    private final double lat;
    private final double lng;
    /** 지번(PARCEL) 매칭 시 V-World 응답의 PNU 19자리(level4LC). 도로명 매칭 등은 null */
    private final String pnu;
}
