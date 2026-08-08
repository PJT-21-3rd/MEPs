<template>
  <Story title="BuildingCard" group="property">
    <Variant title="1. 기본 형태 (건물명, 도로명 모두 있음)">
      <BuildingCard :building="mockBuilding1" />
    </Variant>

    <Variant title="2. 건물명/도로명 없음 (지번 주소 대체)">
      <div class="w-[360px] p-5 bg-neutral-100 min-h-screen">
        <div
          class="flex flex-col overflow-hidden rounded-xl border border-surface-gray bg-white shadow-sm transition-all hover:border-primary hover:shadow-md cursor-pointer group"
        >
          <div class="relative h-[120px] w-full bg-neutral-100">
            <img
              src="https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=600&auto=format&fit=crop"
              alt="건물 사진"
              class="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
            />
            <div
              class="absolute left-3 top-3 flex items-center gap-1 rounded bg-blue-500 px-2 py-1 shadow-sm"
            >
              <ShieldCheck class="h-3 w-3 text-white" />
              <span class="text-[11px] font-bold text-white">안전 88점</span>
            </div>
          </div>

          <div class="flex flex-col p-4">
            <div class="mb-3">
              <h4
                class="text-base font-extrabold text-neutral-900 line-clamp-1 group-hover:text-primary transition-colors"
              >
                대치 무슨빌딩
              </h4>
              <p class="mt-0.5 text-[13px] text-neutral-500 line-clamp-1">
                서울특별시 역삼동 뭐기시 93
              </p>
            </div>

            <div class="mb-3 h-px w-full bg-neutral-100"></div>

            <div class="flex flex-col gap-2">
              <div class="flex items-center gap-2">
                <Layers class="h-3.5 w-3.5 text-neutral-400" />
                <span class="text-[13px] font-medium text-neutral-700">지하 3층 / 지상 15층</span>
              </div>

              <div class="flex items-center gap-2">
                <CalendarDays class="h-3.5 w-3.5 text-neutral-400" />
                <span class="text-[13px] font-medium text-neutral-700">2012년 준공 14년차</span>
              </div>

              <div class="flex items-center gap-2">
                <Store class="h-3.5 w-3.5 text-neutral-400" />
                <span class="text-[13px] font-medium text-neutral-700 truncate">
                  근린생활시설
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import BuildingCard from './BuildingCard.vue';
import { ShieldCheck, Layers, CalendarDays, Store, Building2, Heart } from '@lucide/vue';

// =========================================
// 백엔드 API 명세서 기반 더미 데이터
// =========================================

// 모든 데이터가 완벽하게 들어온 경우
const mockBuilding1 = {
  buildingId: '1121510700102120000',
  lat: 37.5473051,
  lng: 127.073132,
  jibunAddr: '서울특별시 광진구 화양동 212',
  roadAddr: '서울특별시 광진구 광나루로 392 (화양동)',
  bldNm: '메가타워',
  mainPurpsNm: '제2종근린생활시설',
  grndFlr: 12,
  ugrndFlr: 1,
  useAprDay: '19781229',
};

// 꼬마빌딩 등 건물명과 도로명이 null인 경우
const mockBuilding2 = {
  buildingId: '1121510700102160000',
  lat: 37.5469847,
  lng: 127.0725513,
  jibunAddr: '서울특별시 광진구 화양동 216',
  roadAddr: null,
  bldNm: null,
  mainPurpsNm: '근린생활시설',
  grndFlr: 4,
  ugrndFlr: 1,
  useAprDay: '19950310',
};
</script>
