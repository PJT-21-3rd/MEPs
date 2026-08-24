<template>
  <div
    id="map"
    ref="mapContainer"
    class="w-full h-full bg-surface-gray"
    :class="{ 'cursor-pointer': mapStore.isRoadViewMode }"
  ></div>
</template>

<script setup>
import { ref, onMounted, watch, createVNode, render } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useMapStore } from '@/stores/mapStore';
import { useUiStore } from '@/stores/uiStore';
import { useAuthStore } from '@/stores/authStore';
import { useToastStore } from '@/stores/toastStore';
import { fetchHjdBriefing, fetchSggBriefing } from '@/api/aiBrief';
import { fetchNearbyBuildings } from '@/api/building';
import BuildingMarker from '@/components/map/BuildingMarker.vue';

const route = useRoute();
const router = useRouter();
const mapContainer = ref(null);
const mapStore = useMapStore();
const uiStore = useUiStore();
const authStore = useAuthStore();
const toastStore = useToastStore();

let timeOut = null; // 재검색 타이머
let currentPolygons = []; // 폴리곤 객체
let currentMarkers = []; // 마커 객체
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
const drawBuildingPolygons = (data, preventMove = false) => {
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
      strokeWeight: 1.5,
      strokeStyle: 'solid', // 실선
    });
    currentPolygons.push(buildingPolygon);
  }

  // 지도 중심 이동 - 리스트 클릭시만
  if (!preventMove && data.center && data.center.coordinates) {
    const centerLatLng = new window.naver.maps.LatLng(
      data.center.coordinates[1], // lat
      data.center.coordinates[0], // lng
    );
    map.morph(centerLatLng, 19, { duration: 300 });
  }
};

// 마커 표시
const drawMarkers = (buildings) => {
  const map = mapStore.mapInstance;
  if (!map) return;

  // 초기화
  currentMarkers.forEach((marker) => marker.setMap(null));
  currentMarkers = [];

  const allBuildingsMap = new Map();

  buildings.forEach((b) => allBuildingsMap.set(b.buildingId, b)); // api 응답 주변 매물 추가
  if (uiStore.recentBuildings) {
    uiStore.recentBuildings.forEach((b) => allBuildingsMap.set(b.buildingId, b));
  } // 최근 본 매물 추가
  if (uiStore.savedBuildings) {
    uiStore.savedBuildings.forEach((b) => allBuildingsMap.set(b.buildingId, b));
  } // 찜한 매물

  const uniqueBuildings = Array.from(allBuildingsMap.values()); // 중복제거

  uniqueBuildings.forEach((building) => {
    const purposeText = building.mainPurpsNm || '상가';
    const areaText = building.archArea ? `${Math.round(building.archArea)}m²` : '-';
    // const isSelected = uiStore.selectedBuildingId === building.buildingId; // 현재 선택된 건물인가

    // 찜한 매물 및 최근 본 매물 여부 판별
    const isSavedBuilding =
      building.saved ===
      (true || uiStore.savedBuildings?.some((b) => b.buildingId === building.buildingId) || false);
    const isRecentBuilding =
      uiStore.recentBuildings?.some((b) => b.buildingId === building.buildingId) || false;

    const container = document.createElement('div');
    const vnode = createVNode(BuildingMarker, {
      purpose: purposeText,
      area: areaText,
      isSaved: isSavedBuilding,
      isRecent: isRecentBuilding,
      // isSelected: isSelected
    });

    render(vnode, container);

    const marker = new window.naver.maps.Marker({
      position: new window.naver.maps.LatLng(building.lat, building.lng),
      map: map,
      title: building.bldNm || building.jibunAddr,
      icon: {
        content: container.firstElementChild,
        anchor: new window.naver.maps.Point(0, 42),
      },
      zIndex:
        uiStore.selectedBuildingId ===
        (building.buildingId ? 100 : isSavedBuilding ? 50 : isRecentBuilding ? 40 : 10),
    });

    marker.buildingId = building.buildingId;
    marker.setVisible(String(uiStore.selectedBuildingId) !== building.buildingId);

    window.naver.maps.Event.addListener(marker, 'click', () => {
      uiStore.openBuildingDetail(building.buildingId);
      router.push({ query: { ...route.query, buildingId: building.buildingId } });
    });
    currentMarkers.push(marker);
  });
};

// 행정동 브리핑 호출
const updateHjdBriefing = async (lat, lng, zoom) => {
  try {
    window.naver.maps.Service.reverseGeocode(
      {
        coords: new window.naver.maps.LatLng(lat, lng),
        orders: 'admcode', // 행정동 기준
      },
      async (status, response) => {
        if (status === window.naver.maps.Service.Status.OK) {
          const item = response.v2.results[0];
          if (!item) return;
          // const sggName = item.region.area2.name;
          // const hjdName = item.region.area3.name;
          const originalCode = item.code.id;

          const hjdCode8Digits = originalCode.substring(0, 8);
          const sggCode = originalCode.substring(0, 5);

          try {
            if (zoom < 15) {
              const briefingData = await fetchSggBriefing(sggCode);
              if (briefingData) {
                uiStore.setBriefingData(briefingData, 'gu');
                //console.log(`[구 단위] ${briefingData.sggName} 브리핑 업데이트`);
              }
            } else {
              const briefingData = await fetchHjdBriefing(hjdCode8Digits);
              if (briefingData) {
                uiStore.setBriefingData(briefingData, 'dong');
                //console.log(`[동 단위] ${briefingData.hjdName} 브리핑 업데이트`);
              }
            }
          } catch (apiError) {
            console.log('해당 지역의 브리핑 데이터가 없습니다 (404)');
            uiStore.setBriefingData(null, 'dong');
          }
        }
      },
    );
  } catch (error) {
    console.error('브리핑 업데이트 실패');
  }
};

// 건물리스트 업데이트
const updateNearbyBuildings = async () => {
  uiStore.setBuildingsLoading(true);
  const map = mapStore.mapInstance;
  if (!map) return;

  const bounds = map.getBounds();
  const sw = bounds.getSW();
  const ne = bounds.getNE();
  const currentZoom = map.getZoom();

  try {
    const data = await fetchNearbyBuildings(
      sw.lat(),
      sw.lng(),
      ne.lat(),
      ne.lng(),
      currentZoom,
      uiStore.currentSort,
    );
    uiStore.setBuildingsData(data);
    console.log(data);
  } catch (error) {
    uiStore.setBuildingsLoading(false);
    console.error('주변 건물 데이터를 불러오는 데 실패했습니다.', error);
  }
};

// 지도 위치(좌표,줌) 쿼리파라미터에 업데이트
const syncMapStateToUrl = () => {
  const map = mapStore.mapInstance;
  if (!map) return;

  const center = map.getCenter();
  const zoom = map.getZoom();
  const newQuery = {
    ...route.query,
    lat: center.lat().toFixed(6),
    lng: center.lng().toFixed(6),
    zoom: zoom,
  };
  router.replace({ query: newQuery }); // history 오염방지
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

// 건물 상세 데이터 갱신 시 폴리곤 그리기
watch(
  () => uiStore.currentBuildingDetail,
  (newData) => {
    if (newData) {
      drawBuildingPolygons(newData, uiStore.preventMapMove);
    } else {
      currentPolygons.forEach((polygon) => polygon.setMap(null));
      currentPolygons = [];
    }
  },
);

// 주소창 파라미터 동기화
watch(
  () => route.query.buildingId,
  (newId) => {
    if (newId) {
      if (uiStore.selectedBuildingId !== newId) {
        uiStore.openBuildingDetail(newId);
      }
    } else {
      uiStore.closeBuildingDetail();
      const center = mapStore.mapInstance.getCenter();
      updateHjdBriefing(center.lat(), center.lng());
    }
  },
);

// 정렬 순서 변경 -> 건물데이터 업뎃
watch(
  () => uiStore.currentSort,
  () => {
    updateNearbyBuildings();
  },
);

// 재검색시 마커
watch(
  () => uiStore.currentBuildings,
  (newBuildings) => {
    if (!uiStore.isZoomRequired && newBuildings && newBuildings.length > 0) {
      drawMarkers(newBuildings);
    } else {
      currentMarkers.forEach((m) => m.setMap(null));
      currentMarkers = [];
    }
  },
  { deep: true },
);

// 찜/최근 본 매물 리스트 변경
watch(
  () => [uiStore.savedBuildings, uiStore.recentBuildings],
  () => {
    if (!uiStore.isZoomRequired) {
      drawMarkers(uiStore.currentBuildings || []);
    }
  },
  { deep: true },
);

watch(
  () => uiStore.selectedBuildingId,
  (newId) => {
    const selectedIdStr = newId ? String(newId) : null;

    currentMarkers.forEach((marker) => {
      if (marker.buildingId === selectedIdStr) {
        marker.setVisible(false); // 선택된 건물 마커 숨기기
      } else {
        marker.setVisible(true); // 나머지 마커 표시
      }
    });
  },
);

// 선택된 건물 변경시 (추후)
// watch(
//   () => uiStore.selectedBuildingId,
//   () => {
//     if (uiStore.currentBuildings && uiStore.currentBuildings.length > 0) {
//       drawMarkers(uiStore.currentBuildings);
//     }
//   },
// );

// 지도 초기화, 이벤트 등록
onMounted(() => {
  if (!window.naver || !window.naver.maps) {
    console.error('네이버 지도 API를 불러올 수 없습니다.');
    return;
  }

  if (authStore.isLoggedIn && typeof uiStore.loadSavedBuildings === 'function') {
    uiStore.loadSavedBuildings();
  }

  const queryLat = parseFloat(route.query.lat);
  const queryLng = parseFloat(route.query.lng);
  const queryZoom = parseInt(route.query.zoom);

  const initialLat = !isNaN(queryLat) ? queryLat : 37.5445;
  const initialLng = !isNaN(queryLng) ? queryLng : 127.0716;
  const initialZoom = !isNaN(queryZoom) ? queryZoom : 17;

  // 지도 초기 옵션 설정
  const mapOptions = {
    center: new window.naver.maps.LatLng(initialLat, initialLng),
    zoom: initialZoom,
    zoomControl: false,
    minZoom: 10,
    maxZoom: 20,
  };

  const map = new window.naver.maps.Map(mapContainer.value, mapOptions);
  mapStore.setMapInstance(map);

  window.naver.maps.Event.once(map, 'init', () => {
    const queryBuildingId = route.query.buildingId;
    if (queryBuildingId) {
      uiStore.openBuildingDetail(queryBuildingId);
    } else {
      const center = map.getCenter();
      updateHjdBriefing(center.lat(), center.lng());
      updateNearbyBuildings();
    }
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
      const currentZoom = map.getZoom();
      updateHjdBriefing(currentCenter.lat(), currentCenter.lng(), currentZoom);
      updateNearbyBuildings();
      syncMapStateToUrl();
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

    // 클릭 제한 (추후 구별 혹은 동별 폴리곤 지원되면 변경)
    const currentZoom = map.getZoom();
    if (currentZoom < 15) {
      toastStore.showToast('건물을 선택하려면 지도를 더 확대해주세요.', {
        action: {
          label: '확대하기',
          onClick: () => {
            const targetLatLng = new window.naver.maps.LatLng(lat, lng);
            map.morph(targetLatLng, 17, { duration: 300 });
          },
        },
      });
      return;
    }

    const clickedBuildingId = await uiStore.openBuildingDetailByCoord(lat, lng);
    if (clickedBuildingId) {
      router.push({ query: { ...route.query, buildingId: clickedBuildingId } });
    } else {
      const newQuery = { ...route.query };
      delete newQuery.buildingId;
      router.push({ query: newQuery });
    }
  });
});
</script>
