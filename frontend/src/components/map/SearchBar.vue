<script setup>
import { Clock, Search, X } from '@lucide/vue';
import { useUiStore } from '@/stores/uiStore';
import { useMapStore } from '@/stores/mapStore';
import { onMounted, ref } from 'vue';
import { useClickOutside } from '@/hooks/useClickOutside';
import api from '@/api/axios';
import axios from 'axios';

const uiStore = useUiStore();
const mapStore = useMapStore();

// dropdwon 관련
const isDropdownOpen = ref(false);
const searchContainerRef = ref(null);
const recentSearches = ref([]); // 최근검색어s
const autoCompletes = ref([]); // 자동완성
let debounceTimeout = null;

// 최근 검색어 관련 로직
const loadRecentSearches = () => {
  const saved = localStorage.getItem('meps-recent-searches');
  if (saved) {
    recentSearches.value = JSON.parse(saved);
  }
};

const saveRecentSearches = () => {
  localStorage.setItem('meps-recent-searches', JSON.stringify(recentSearches.value));
};

const removeRecentSearch = (keyword) => {
  recentSearches.value = recentSearches.value.filter((item) => item !== keyword);
  saveRecentSearches();
};

const clearAllRecentSearches = () => {
  recentSearches.value = [];
  localStorage.removeItem('meps-recent-searches');
};

const clearSearch = () => {
  uiStore.searchQuery = '';
  autoCompletes.value = [];
};

// 검색 실행로직
const executeSearch = async (keyword) => {
  if (!keyword || !keyword.trim()) return;

  const validKeyword = keyword.trim();
  uiStore.searchQuery = validKeyword;
  isDropdownOpen.value = false;

  recentSearches.value = recentSearches.value.filter((item) => item !== validKeyword);
  recentSearches.value.unshift(validKeyword);
  if (recentSearches.value.length > 5) recentSearches.value.pop();
  saveRecentSearches();

  const map = mapStore.mapInstance;
  if (!map) return;

  try {
    console.log(`백엔드 자체 DB 검색 요청 중... (/api/buildings/search)`);
    const response = await api.get('/api/buildings/search', {
      params: { keyword: validKeyword },
    });
    const responseData = response.data;
    const data = Array.isArray(responseData) ? responseData[0] : responseData;

    // 응답 분기 처리
    if (data && data.searchType === 'BUILDING') {
      console.log(`백엔드 DB 검색 성공`);
      console.log(`    분기: BUILDING (건물)`);
      console.log(`    데이터:`, data);

      const targetLoc = new window.naver.maps.LatLng(data.targetLat, data.targetLng);
      map.panTo(targetLoc, { duration: 300 });
      map.setZoom(17, true);
      uiStore.openBuildingDetail(data.targetBuildingId);
      return; // 검색 종료
    } else if (data && data.searchType === 'REGION') {
      console.log(`백엔드 DB 검색 성공!`);
      console.log(`    분기: REGION (행정구역/동네)`);
      console.log(`    데이터:`, data);

      const bounds = new window.naver.maps.LatLngBounds(
        new window.naver.maps.LatLng(data.swLat, data.swLng),
        new window.naver.maps.LatLng(data.neLat, data.neLng),
      );
      map.panToBounds(bounds);
      uiStore.closeBuildingDetail();
      return; // 검색 종료
    }

    console.log(`[Fallback] 백엔드 DB에 결과 없음(NONE).`);
    console.log(`Ncloud 지역 검색 API로 넘어갑니다... (/api-hub/search/v1/local)`);

    // track2: naver api
    const ncloudRes = await axios.get('/api-hub/search/v1/local', {
      params: {
        query: validKeyword,
        display: 1,
        format: 'json',
      },
      headers: {
        'X-NCP-APIGW-API-KEY-ID': import.meta.env.VITE_NCLOUD_CLIENT_ID,
        'X-NCP-APIGW-API-KEY': import.meta.env.VITE_NCLOUD_CLIENT_SECRET,
        Accept: 'application/json',
      },
    });

    const items = ncloudRes.data.items;

    if (items && items.length > 0) {
      const firstPlace = items[0];

      console.log(`Ncloud 지역 검색 API 검색 성공!`);
      console.log(`    분기: POI (장소/지하철역 등)`);
      console.log(`    데이터:`, firstPlace);

      // 지역 검색 API는 TM128 좌표계를 사용
      // (문서에 'WGS84 좌표계 기준'이라고 적혀있지만 실제 mapx, mapy 값은 보통 정수형 TM128로 옵니다.
      // 만약 mapx값이 127.xxx 형태라면 변환이 필요 없으니 아래 코드를 수정해야 함.)
      const rawX = firstPlace.mapx;
      const rawY = firstPlace.mapy;

      let latLng;
      // 좌표가 120, 30 대의 WGS84 형태인지, TM128 형태인지
      if (rawX.indexOf('.') === -1 && rawX.length >= 9) {
        // 10^7 곱해진 WGS84
        latLng = new window.naver.maps.LatLng(parseInt(rawY) / 10000000, parseInt(rawX) / 10000000);
      } else if (rawX.indexOf('.') !== -1) {
        // WGS84 형태 (예: 127.1054328)
        latLng = new window.naver.maps.LatLng(parseFloat(rawY), parseFloat(rawX));
      } else {
        // TM128 형태 (예: 314567, 543210)
        const tm128Point = new window.naver.maps.Point(parseInt(rawX), parseInt(rawY));
        latLng = window.naver.maps.TransCoord.fromTM128ToLatLng(tm128Point);
      }

      console.log(`   변환된 좌표: lat=${latLng.lat()}, lng=${latLng.lng()}`);

      map.panTo(latLng, { duration: 300 });
      map.setZoom(16, true);
      uiStore.closeBuildingDetail(); // 상세창 닫기
      return;
    }
  } catch (error) {
    console.error('검색 API 호출 오류:', error);
    if (error.response && error.response.data) {
      console.error('API HUB 오류:', error.response.data);
    }
    alert('검색 중 문제가 발생했습니다.');
  }
};

// 외부 클릭 감지(드롭다운 닫기)
useClickOutside(searchContainerRef, () => {
  isDropdownOpen.value = false;
});

onMounted(() => {
  loadRecentSearches();
});
</script>

<template>
  <div class="relative" ref="searchContainerRef">
    <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-sub" />
    <input
      type="text"
      v-model="uiStore.searchQuery"
      @input="isDropdownOpen = true"
      @keyup.enter="executeSearch(uiStore.searchQuery)"
      @focus="isDropdownOpen = true"
      placeholder="주소, 지역 또는 건물명 검색"
      class="w-full h-14 rounded-2xl border-0 bg-neutral-100 pl-12 pr-11 text-[16px] placeholder:text-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-secondary/40"
      :class="{ 'bg-white': uiStore.isDetailOpen || isDropdownOpen }"
    />
    <button
      v-if="uiStore.searchQuery"
      @click="clearSearch"
      class="absolute right-3 top-1/2 -translate-y-1/2 p-1 bg-neutral-300/70 rounded-full text-white hover:bg-neutral-400"
    >
      <X size="14px" />
    </button>

    <!-- 최근 검색 드롭다운 -->
    <div
      v-if="isDropdownOpen"
      class="absolute left-0 right-0 top-full z-40 mt-2 overflow-hidden rounded-2xl border border-neutral-100 bg-white shadow-xl"
    >
      <template v-if="!uiStore.searchQuery">
        <div class="flex items-center justify-between px-4 pb-1.5 pt-3">
          <p class="text-text-sub text-[12px]">최근 검색</p>
        </div>
        <ul v-if="recentSearches.length > 0" class="pb-1">
          <li v-for="(item, index) in recentSearches" :key="index" @click="executeSearch(item)">
            <div class="group flex items-center gap-2.5 px-4 py-2.5 hover:bg-neutral-50">
              <Clock class="shrink-0 text-text-sub" size="16px" />
              <span class="min-w-0 flex-1 truncate text-left text-[15px] text-text-main">
                {{ item }}
              </span>
              <button
                @click.stop="removeRecentSearch(item)"
                class="rounded-full p-1 text-neutral-300 hover:bg-neutral-200 hover:text-neutral-500"
              >
                <X size="14px" />
              </button>
            </div>
          </li>
        </ul>
        <div v-else class="flex items-center gap-2.5 px-4 py-2.5">
          <p class="min-w-0 flex-1 truncate text-center text-[15px] text-text-sub font-semibold">
            최근 검색 내역이 없습니다.
          </p>
        </div>
        <div class="flex items-center justify-between border-t border-neutral-100 px-4 py-2.5">
          <button
            @click="clearAllRecentSearches"
            class="text-[13px] text-text-sub hover:text-text-main"
          >
            전체삭제
          </button>
          <button
            @click="isDropdownOpen = false"
            class="text-[13px] text-neutral-500 hover:text-neutral-800"
          >
            닫기
          </button>
        </div>
      </template>

      <template v-else>
        <ul class="py-1">
          <li
            @click="executeSearch(uiStore.searchQuery)"
            class="flex items-center gap-2.5 px-4 py-3 hover:bg-neutral-50 cursor-pointer border-b border-neutral-100"
          >
            <div class="flex items-center justify-center w-6 h-6 rounded-full bg-blue-50">
              <Search />
            </div>
            <span class="min-w-0 flex-1 truncate text-left text-[15px] font-bold text-[#0071AC]">
              "{{ uiStore.searchQuery }}" 검색
            </span>
          </li>
        </ul>
      </template>
    </div>
  </div>
</template>
