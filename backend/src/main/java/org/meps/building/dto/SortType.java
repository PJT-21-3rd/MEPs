package org.meps.building.dto;

/**
 * 위치 기반 매물 리스트 정렬 기준.
 * 모든 정렬의 동률 시 2차 정렬은 거리 오름차순.
 */
public enum SortType {
    DISTANCE,   // 거리순 (기본값) - 뷰포트 중심에서 가까운 순
    LATEST,     // 최신순 - 사용승인일 내림차순, null은 맨 뒤
    POPULAR     // 인기순 - 찜 수 내림차순
}
