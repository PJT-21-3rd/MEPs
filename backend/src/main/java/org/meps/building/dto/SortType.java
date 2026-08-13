package org.meps.building.dto;

/**
 * 위치 기반 매물 리스트 정렬 기준.
 * 모든 정렬의 동률 시 2차 정렬은 거리 오름차순.
 */
public enum SortType {
    POPULAR,    // 찜많은순 (기본값) - 찜 수 내림차순
    LATEST,     // 최신순 - 사용승인일 내림차순, null은 맨 뒤
    AREA        // 면적순 - 연면적 내림차순
}
