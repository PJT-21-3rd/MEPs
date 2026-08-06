<template>
  <div id="map" ref="mapContainer" class="w-full h-full bg-surface-gray"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useMapStore } from '@/stores/mapStore';
import { useUiStore } from '@/stores/uiStore';

const mapContainer = ref(null);
const mapStore = useMapStore();
const uiStore = useUiStore();

let timeOut = null; // 재검색 타이머
let currentPolygons = []; // 폴리곤 객체

// geoJSON 좌표 파싱
const parseMultiPolygon = (multiPolygon) => {
  if (!multiPolygon || !multiPolygon.coordinates) return [];
  const paths = [];

  multiPolygon.coordinates.forEach((polygon) => {
    polygon.forEach((ring) => {
      const path = ring.map((coord) => new window.naver.maps.LatLng(coord[1], coord[0]));
      paths.push(path);
    });
  });
  return paths;
};

// 폴리곤 데이터 레이어 표시
const drawBuildingPolygons = (data) => {
  const map = mapStore.mapInstance; // 현재 지도 객체
  if (!map || !data) return;

  // 초기화
  currentPolygons.forEach((polygon) => polygon.setMap(null));
  currentPolygons = [];

  // 토지 (지적도) 폴리곤 그리기
  if (data.parcelGeom) {
    const landPaths = parseMultiPolygon(data.parcelGeom);
    const landPolygon = new window.naver.maps.Polygon({
      map: map,
      paths: landPaths,
      fillColor: '#0071AC',
      fillOpacity: 0.3, // 투명 배경
      strokeColor: '#0071AC', // 빨간색
      strokeWeight: 2,
      strokeStyle: 'shortdash', // 점선
    });
    currentPolygons.push(landPolygon);
  }

  // 건축물 (바닥면적) 폴리곤 그리기
  if (data.footprint) {
    const buildingPaths = parseMultiPolygon(data.footprint);
    const buildingPolygon = new window.naver.maps.Polygon({
      map: map,
      paths: buildingPaths,
      fillColor: '#0071AC',
      fillOpacity: 0,
      strokeColor: '#0071AC',
      strokeWeight: 2,
      strokeStyle: 'solid', // 실선
    });
    currentPolygons.push(buildingPolygon);
  }

  // 지도 중심 이동 (선택사항 - 클릭 시에만 이동하게 할 수도 있음)
  if (data.center && data.center.coordinates) {
    const centerLatLng = new window.naver.maps.LatLng(
      data.center.coordinates[1], // lat
      data.center.coordinates[0], // lng
    );
    map.panTo(centerLatLng, { duration: 300 });
  }
};

// TODO: API 호출 함수 - 임시
const fetchPolygonData = async (buildingId) => {
  // TODO: 실제 API 호출 (const res = await fetch(`/api/buildings/${buildingId}`);)
  console.log(`[API 요청] 건물 ID: ${buildingId}의 폴리곤 데이터 패칭...`);

  return {
    buildingId: buildingId,
    center: { type: 'Point', coordinates: [127.0366742, 37.5006373] },
    footprint: {
      type: 'MultiPolygon',
      coordinates: [
        [
          [
            [127.0365, 37.5007],
            [127.0368, 37.5007],
            [127.0368, 37.5005],
            [127.0365, 37.5005],
            [127.0365, 37.5007],
          ],
        ],
      ],
    },
    parcelGeom: {
      type: 'MultiPolygon',
      coordinates: [
        [
          [
            [127.0364, 37.5008],
            [127.0369, 37.5008],
            [127.0369, 37.5004],
            [127.0364, 37.5004],
            [127.0364, 37.5008],
          ],
        ],
      ],
    },
  };
};

const fetchPolygonByCoord = async (lat, lng) => {
  // TODO: 실제 API 호출 (const res = await fetch(`/api/buildings/search?lat=${lat}&lng=${lng}`);)
  console.log(`[API 요청] 좌표 검색: lat=${lat}, lng=${lng}`);
  return await fetchPolygonData('1168010100102160000'); // 가짜 데이터 반환
};

// 지도 초기화, 이벤트 등록
onMounted(() => {
  if (!window.naver || !window.naver.maps) {
    console.error('네이버 지도 API를 불러올 수 없습니다.');
    return;
  }

  // 지도 초기 옵션 설정
  const mapOptions = {
    center: new window.naver.maps.LatLng(37.4979, 127.0276),
    zoom: 15,
    zoomControl: false,
  };

  const map = new window.naver.maps.Map(mapContainer.value, mapOptions);
  mapStore.setMapInstance(map);

  const handleMapStart = () => {
    if (timeOut) clearTimeout(timeOut);
    mapStore.setMapMoved(false);
  };

  // 지도의 모든 움직임이 완전히 멈췄을 때 (마우스, 줌, 빠른 이동 모두 포함)
  const handleMapIdle = () => {
    if (timeOut) clearTimeout(timeOut);

    timeOut = setTimeout(() => {
      mapStore.setMapMoved(true); // 800ms이후 맵움직임 true
    }, 800);
  };

  window.naver.maps.Event.addListener(map, 'dragstart', handleMapStart);
  window.naver.maps.Event.addListener(map, 'idle', handleMapIdle);

  // 1. 지도에서 건물 클릭시
  window.naver.maps.Event.addListener(map, 'click', async (e) => {
    const lat = e.coord.lat();
    const lng = e.coord.lng();
    const data = await fetchPolygonByCoord(lat, lng); // 해당 좌표의 건물 정보 가져오기

    if (data && data.buildingId) {
      uiStore.openBuildingDetail(data.buildingId);
      drawBuildingPolygons(data);
    }
  });

  // 2. 리스트에서 선택시
  watch(
    () => uiStore.selectedBuildingId,
    async (newId) => {
      if (newId) {
        const data = await fetchPolygonData(newId);
        drawBuildingPolygons(data);
      } else {
        currentPolygons.forEach((polygon) => polygon.setMap(null));
        currentPolygons = [];
      }
    },
  );
});
</script>
