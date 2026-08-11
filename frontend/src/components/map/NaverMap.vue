<template>
  <div
    id="map"
    ref="mapContainer"
    class="w-full h-full bg-surface-gray"
    :class="{ 'cursor-pointer': mapStore.isRoadViewMode }"
  ></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useMapStore } from '@/stores/mapStore';
import { useUiStore } from '@/stores/uiStore';
import { fetchReverseGeocoding } from '@/api/map';
import { fetchHjdBriefing } from '@/api/aiBrief';
import { fetchNearbyBuildings } from '@/api/building';

const mapContainer = ref(null);
const mapStore = useMapStore();
const uiStore = useUiStore();

let timeOut = null; // 재검색 타이머
let currentPolygons = []; // 폴리곤 객체
let streetLayer = null; // 거리뷰 선 객체

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
      fillOpacity: 0.3,
      strokeColor: '#0071AC',
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

// 행정동 브리핑 호출
const updateHjdBriefing = async (lat, lng) => {
  try {
    const hjdCode = await fetchReverseGeocoding(lat, lng);
    if (!hjdCode) return;

    const briefingData = await fetchHjdBriefing(hjdCode);
    if (briefingData) {
      uiStore.setHjdBriefingData(briefingData);
      console.log(`#${briefingData.hjdName} AI 브리핑 데이터 업데이트 완료`);
    }
  } catch (error) {
    console.error('브리핑 업데이트 실패');
  }
};

// 건물리스트 업데이트
const updateNearbyBuildings = async () => {
  const map = mapStore.mapInstance;
  if (!map) return;

  const bounds = map.getBounds();
  const sw = bounds.getSW();
  const ne = bounds.getNE();
  const currentZoom = map.getZoom();

  try {
    const data = await fetchNearbyBuildings(sw.lat(), sw.lng(), ne.lat(), ne.lng(), currentZoom);

    uiStore.setBuildingsData(data);
    console.log(data);

    if (!data.zoomRequired && data.buildings) {
      // drawMarkers(data.buildings);
    } else {
      currentMarkers.forEach((m) => m.setMap(null));
      currentMarkers = [];
      console.log('줌 아웃 상태: 마커를 표시하려면 지도를 확대해 주세요.');
    }
  } catch (error) {
    console.error('주변 건물 데이터를 불러오는 데 실패했습니다.', error);
  }
};

// 로드뷰 토글 감지 & 거리뷰레이어 표시
watch(
  () => mapStore.isRoadViewMode,
  (isActive) => {
    const map = mapStore.mapInstance;
    if (!map) return;

    if (isActive) {
      if (!streetLayer) streetLayer = new window.naver.maps.StreetLayer();
      streetLayer.setMap(map);
      map.setCursor('crosshair');
    } else {
      if (streetLayer) streetLayer.setMap(null);
      map.setCursor('grab');
    }
  },
);

// 폴리곤 그리기
watch(
  () => uiStore.currentBuildingDetail,
  (newData) => {
    if (newData) {
      drawBuildingPolygons(newData);
    } else {
      currentPolygons.forEach((polygon) => polygon.setMap(null));
      currentPolygons = [];
    }
  },
);

// todo: 마커 그리기

// 지도 초기화, 이벤트 등록
onMounted(() => {
  if (!window.naver || !window.naver.maps) {
    console.error('네이버 지도 API를 불러올 수 없습니다.');
    return;
  }

  const initialLat = 37.5445;
  const initialLng = 127.0716;

  // 지도 초기 옵션 설정
  const mapOptions = {
    center: new window.naver.maps.LatLng(initialLat, initialLng),
    zoom: 17,
    zoomControl: false,
  };

  const map = new window.naver.maps.Map(mapContainer.value, mapOptions);
  mapStore.setMapInstance(map);

  window.naver.maps.Event.once(map, 'init', () => {
    const center = map.getCenter();
    updateHjdBriefing(center.lat(), center.lng());
    updateNearbyBuildings();
  });

  const handleMapStart = () => {
    if (timeOut) clearTimeout(timeOut);
    mapStore.setMapMoved(false);
  };

  // 지도의 모든 움직임이 완전히 멈췄을 때 (마우스, 줌, 빠른 이동 모두 포함)
  const handleMapIdle = () => {
    if (timeOut) clearTimeout(timeOut);

    timeOut = setTimeout(() => {
      mapStore.setMapMoved(true); // 800ms이후 맵움직임 true

      const currentCenter = map.getCenter();
      updateHjdBriefing(currentCenter.lat(), currentCenter.lng());
      // updateNearbyBuildings();
    }, 800);
  };

  window.naver.maps.Event.addListener(map, 'dragstart', handleMapStart);
  window.naver.maps.Event.addListener(map, 'idle', handleMapIdle);

  // 1. 지도에서 건물 클릭시
  window.naver.maps.Event.addListener(map, 'click', async (e) => {
    const lat = e.coord.lat();
    const lng = e.coord.lng();

    // 로드뷰 모드라면 건물클릭 x
    if (mapStore.isRoadViewMode) {
      uiStore.openRoadViewModal(lat, lng);
      return;
    }
  });
});
</script>
