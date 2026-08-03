package org.meps.hjd.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** 행정동 경계 bbox (시군구명 매칭 시 소속 행정동 합집합) */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class HjdBboxDto {
    private double swLat;
    private double swLng;
    private double neLat;
    private double neLng;

    public double centerLat() {
        return (swLat + neLat) / 2;
    }

    public double centerLng() {
        return (swLng + neLng) / 2;
    }
}
