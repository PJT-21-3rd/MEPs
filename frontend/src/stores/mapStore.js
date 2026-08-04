// 지도 컨트롤

import { defineStore } from 'pinia';
import { ref, shallowRef } from 'vue';

export const useMapStore = defineStore('map', () => {
  const mapInstance = shallowRef(null);
  const isMapMoved = ref(false);

  const setMapInstance = (map) => {
    mapInstance.value = map;
  }; // 지도

  const setMapMoved = (status) => {
    isMapMoved.value = status;
  }; // 맵 변경 상태 업데이트

  const zoomIn = () => {
    if (mapInstance.value) {
      const currentZoom = mapInstance.value.getZoom();
      mapInstance.value.setZoom(currentZoom + 1, true);
    }
  };
  const zoomOut = () => {
    if (mapInstance.value) {
      const currentZoom = mapInstance.value.getZoom();
      mapInstance.value.setZoom(currentZoom - 1, true);
    }
  };

  const moveToMyLocation = () => {
    if (!mapInstance.value) return;

    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;
          const loc = new window.naver.maps.LatLng(lat, lng);

          mapInstance.value.setCenter(loc);
          mapInstance.value.setZoom(16, true);
        },
        (error) => {
          console.error('위치 정보를 가져올 수 없습니다.', error);
          alert('위치 정보를 가져올 수 없습니다. 브라우저의 위치 권한을 허용해주세요.');
        },
      );
    } else {
      alert('이 브라우저에서는 위치 정보(GPS)를 지원하지 않습니다.');
    }
  };
  return {
    mapInstance,
    isMapMoved,
    setMapInstance,
    setMapMoved,
    zoomIn,
    zoomOut,
    moveToMyLocation,
  };
});
