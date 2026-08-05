// src/mocks/compareData.js
// 매물 비교 상세 목 데이터 — 백엔드 건물 상세/비교 API 응답 명세를 그대로 반영
// 실제 API 나오면 이 파일 대신 응답을 사용 (필드명이 같아서 화면 코드는 그대로)
//
// 필드명 출처: 건물 기본정보 조회 API 명세
//   기본: buildingId, bldNm, roadAddr, jibunAddr, mainPurps, platArea, totArea, hoCnt
//   land.*   : 지목 / 용도지역 / 도로조건 / 공시지가
//   detail.* : 주구조 / 지상·지하층수 / 호수 / 사용승인일 / 경과연차 / 위반건축물여부
//   floors[] : 지상지하구분 / 층번호 / 주용도 / 기타용도

export const compareData = {
  // id를 키로 두면 selectedIds로 바로 꺼내 쓰기 편함
  1: {
    buildingId: '1121510700102020000025797',
    bldNm: '역삼 스타빌딩',
    score: 92,
    briefing:
      '구조·화재·지반침하·침수 4개 항목 모두 안전 범위로 평가되었습니다. 안심하고 검토하셔도 좋습니다.',
    diagnosis: {
      structure: { status: 'safe', summary: '구조 안전성에 특이사항이 없습니다.' },
      fire: { status: 'safe', summary: '최근 3년간 화재 이력이 없습니다.' },
      sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
      flood: { status: 'good', summary: '경미한 침수 이력이 있으나 안전 범위입니다.' },
    },
    roadAddr: '서울특별시 강남구 태헤란로 152',
    jibunAddr: '서울특별시 강남구 역삼동 123-4',
    mainPurps: '제2종근린생활시설',
    platArea: 512.0, // 대지면적(㎡)
    totArea: 3840.0, // 연면적(㎡)
    hoCnt: 48, // 호수

    land: {
      lndcgrCodeNm: '대', // 지목
      prposAreaNm: '일반상업지역', // 용도지역
      roadSideCodeNm: '광대소각', // 도로조건
      pbIntfPcInd: 18500000, // 공시지가(원/㎡)
    },

    detail: {
      strctCdNm: '철근콘크리트구조', // 주구조
      grndFlr: 8, // 지상층수
      ugrndFlr: 2, // 지하층수
      hoCnt: 48, // 호수
      useAprDay: '20150722', // 사용승인일
      elapsedYear: 10, // 경과연차
      violBdYn: 'N', // 위반건축물 여부
    },

    floors: [
      { flrGbNm: '지상', flrNoNm: '1층', mainPurpsNm: '소매점', etcPurps: '편의점' },
      { flrGbNm: '지상', flrNoNm: '2층', mainPurpsNm: '일반음식점', etcPurps: '카페' },
      { flrGbNm: '지상', flrNoNm: '3층', mainPurpsNm: '사무소', etcPurps: '-' },
      { flrGbNm: '지하', flrNoNm: '1층', mainPurpsNm: '주차장', etcPurps: '-' },
    ],
  },

  3: {
    buildingId: '1121510700102020000025798',
    bldNm: '센트럴프라자',
    score: 96,
    briefing:
      '전 항목에서 우수한 안전성을 보입니다. 최근 준공된 건물로 구조적 안정성이 특히 뛰어납니다.',
    diagnosis: {
      structure: { status: 'safe', summary: '최신 내진 설계가 적용되었습니다.' },
      fire: { status: 'safe', summary: '화재 이력이 없습니다.' },
      sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
      flood: { status: 'safe', summary: '침수 이력이 없습니다.' },
    },
    roadAddr: '서울특별시 강남구 태헤란로 210',
    jibunAddr: '서울특별시 강남구 역삼동 210-9',
    mainPurps: '업무시설',
    platArea: 680.0,
    totArea: 5120.0,
    hoCnt: 62,

    land: {
      lndcgrCodeNm: '대',
      prposAreaNm: '일반상업지역',
      roadSideCodeNm: '광대한면',
      pbIntfPcInd: 21200000,
    },

    detail: {
      strctCdNm: '철골철근콘크리트구조',
      grndFlr: 12,
      ugrndFlr: 3,
      hoCnt: 62,
      useAprDay: '20180315',
      elapsedYear: 7,
      violBdYn: 'N',
    },

    floors: [
      { flrGbNm: '지상', flrNoNm: '1층', mainPurpsNm: '소매점', etcPurps: '은행' },
      { flrGbNm: '지상', flrNoNm: '2층', mainPurpsNm: '업무시설', etcPurps: '사무소' },
      { flrGbNm: '지하', flrNoNm: '1층', mainPurpsNm: '주차장', etcPurps: '-' },
      { flrGbNm: '지하', flrNoNm: '2층', mainPurpsNm: '주차장', etcPurps: '-' },
    ],
  },

  6: {
    buildingId: '1121510700102020000025799',
    bldNm: '메가시티타워',
    score: 88,
    briefing: '위반건축물 이력이 있으나 안전 지표는 매우 양호합니다. 세부 사항 확인을 권장합니다.',
    diagnosis: {
      structure: { status: 'good', summary: '위반건축물 이력이 있어 확인이 필요합니다.' },
      fire: { status: 'safe', summary: '화재 이력이 없습니다.' },
      sinkhole: { status: 'safe', summary: '지반침하 이력이 없습니다.' },
      flood: { status: 'safe', summary: '침수 이력이 없습니다.' },
    },
    roadAddr: '서울특별시 강남구 태헤란로 301',
    jibunAddr: '서울특별시 강남구 역삼동 301-2',
    mainPurps: '제1종근린생활시설',
    platArea: 445.0,
    totArea: 2980.0,
    hoCnt: 33,

    land: {
      lndcgrCodeNm: '대',
      prposAreaNm: '제3종일반주거지역',
      roadSideCodeNm: '중로한면',
      pbIntfPcInd: 12800000,
    },

    detail: {
      strctCdNm: '철근콘크리트구조',
      grndFlr: 6,
      ugrndFlr: 1,
      hoCnt: 33,
      useAprDay: '20200110',
      elapsedYear: 5,
      violBdYn: 'Y',
    },

    floors: [
      { flrGbNm: '지상', flrNoNm: '1층', mainPurpsNm: '소매점', etcPurps: '-' },
      { flrGbNm: '지상', flrNoNm: '2층', mainPurpsNm: '미용실', etcPurps: '-' },
      { flrGbNm: '지상', flrNoNm: '3층', mainPurpsNm: '사무소', etcPurps: '-' },
    ],
  },
};
