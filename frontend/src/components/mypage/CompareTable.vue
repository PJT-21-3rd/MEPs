<script setup>
import { LandPlot, Building2, Layers } from '@lucide/vue';
import {
  formatArea,
  formatPrice,
  formatDate,
  formatFloor,
  formatFloorName,
  formatBuildingAge,
  formatViolation,
} from '@/utils/formatters';
const props = defineProps({
  buildings: Array, // 비교할 매물들 (compareBuildings)
  activeSections: Array, // 켜진 섹션
});
import { getBuildingCoverageRatio } from '@/utils/formatters';

function isActive(key) {
  return props.activeSections.includes(key);
}

// 토지 정보
const landRows = [
  { label: '면적', get: (b) => `${formatArea(b.building.platArea)}` },
  { label: '지목', get: (b) => b.building.land.lndcgrCodeNm },
  { label: '용도지역', get: (b) => b.building.land.prposAreaNm },
  { label: '도로접면', get: (b) => b.building.land.roadSideCodeNm },
  { label: '공시지가', get: (b) => `${formatPrice(b.building.land.pblntfPclnd)}` },
];

// 건축물 정보
const buildingRows = [
  { label: '건물이름', get: (b) => b.building.bldNm || '-' },
  { label: '주용도', get: (b) => b.building.mainPurpsNm },
  { label: '주구조', get: (b) => b.building.detail.strctCdNm },
  { label: '높이', get: (b) => `${b.building.detail.heit}m` },
  {
    label: '지상/지하',
    get: (b) => `${formatFloor(b.building.detail.grndFlr, b.building.detail.ugrndFlr)}`,
  },
  { label: '대지면적', get: (b) => `${formatArea(b.building.platArea)}` },
  {
    label: '건축면적',
    get: (b) =>
      `${formatArea(b.building.detail.archArea)} (건폐율 ${getBuildingCoverageRatio(b.building.detail.archArea, b.building.platArea)}%)`,
  },
  { label: '연면적', get: (b) => `${formatArea(b.building.totArea)}` },
  { label: '호수', get: (b) => `${b.building.detail.hoCnt}호` },
  {
    label: '사용승인일',
    get: (b) =>
      `${formatDate(b.building.detail.useAprDay)} (${formatBuildingAge(b.building.detail.useAprDay)})`,
  },
  { label: '위반건축물여부', get: (b) => formatViolation(b.building.detail.violBdYn) },
];
</script>

<template>
  <div>
    <!-- 토지 정보 섹션 -->
    <div v-if="isActive('land')" class="mt-4">
      <h4 class="flex items-center gap-1.5 text-[17px] font-bold text-primary mb-1">
        <LandPlot :size="17" />
        토지 정보
      </h4>
      <div class="bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm">
        <table class="w-full text-[13px] table-fixed">
          <tbody>
            <tr v-for="row in landRows" :key="row.label" class="odd:bg-white even:bg-surface-base">
              <td class="pl-4 py-3 pr-4 text-text-sub w-[140px] align-top">{{ row.label }}</td>
              <td
                v-for="building in buildings"
                :key="building.building.buildingId"
                class="py-3 px-4 align-top border-l border-surface-gray"
              >
                {{ row.get(building) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 건축물 정보 섹션 -->
    <div v-if="isActive('building')" class="mt-4">
      <h4 class="flex items-center gap-1.5 text-[17px] font-bold text-primary mb-1">
        <Building2 :size="17" />
        건축물 정보
      </h4>
      <div class="bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm">
        <table class="w-full text-[13px] table-fixed">
          <tbody>
            <tr
              v-for="row in buildingRows"
              :key="row.label"
              class="odd:bg-white even:bg-surface-base"
            >
              <td class="pl-4 py-3 pr-4 text-text-sub w-[140px] align-top">{{ row.label }}</td>
              <td
                v-for="building in buildings"
                :key="building.building.buildingId"
                class="py-3 px-4 align-top border-l border-surface-gray"
              >
                {{ row.get(building) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 층별 현황 섹션 -->
    <div v-if="isActive('floor')" class="mt-4 mb-4">
      <h4 class="flex items-center gap-1.5 text-[17px] font-bold text-primary mb-1">
        <Layers :size="17" />
        층별 현황
      </h4>
      <div class="flex gap-4">
        <!-- 매물마다 자기 층 목록 표 -->
        <div
          v-for="building in buildings"
          :key="building.building.buildingId"
          class="flex-1 min-w-0 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm"
        >
          <table
            v-if="building.building.floors && building.building.floors.length > 0"
            class="w-full text-[12px] table-fixed"
          >
            <thead class="overflow-hidden border border-surface-gray">
              <tr
                class="flex items-center bg-surface-base px-4 py-2 text-[12px] text-text-sub text-left"
              >
                <th class="w-14">층</th>
                <th class="flex-1">용도</th>
                <th class="w-20 text-right">기타</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(floor, index) in building.building.floors"
                :key="index"
                class="flex items-center border-t border-surface-base px-4 py-2.5"
              >
                <td class="w-14 text-[14px]">
                  {{ formatFloorName(floor.flrGbNm, floor.flrNoNm) }}
                </td>
                <td class="flex-1 text-[14px] text-text-secondary/95">
                  {{ floor.mainPurpsNm || '-' }}
                </td>
                <td class="w-20 text-right text-[14px] text-text-secondary/90">
                  {{ floor.etcPurps || '-' }}
                </td>
              </tr>
            </tbody>
          </table>

          <!-- floors 없으면 정보 없음 -->
          <div v-else class="py-8 text-center text-[13px] text-text-sub">
            등록된 층별 현황이 없어요
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
