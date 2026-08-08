<script setup>
import { LandPlot, Building2, Layers } from '@lucide/vue';
import { formatDate } from '@/utils/formatters';
const props = defineProps({
  buildings: Array, // 비교할 매물들 (compareBuildings)
  activeSections: Array, // 켜진 섹션
});
import { getBuildingCoverageRatio } from '@/utils/formatters';

function isActive(key) {
  return props.activeSections.includes(key);
}

// 토지 정보 항목
const landRows = [
  { label: '면적', get: (b) => `${b.platArea.toLocaleString()}㎡` },
  { label: '지목', get: (b) => b.land.lndcgrCodeNm },
  { label: '용도지역', get: (b) => b.land.prposAreaNm },
  { label: '도로접면', get: (b) => b.land.roadSideCodeNm },
  { label: '공시지가', get: (b) => `${b.land.pbIntfPcInd.toLocaleString()}원/㎡` },
];

// 건축물 정보 항목
const buildingRows = [
  { label: '건물이름', get: (b) => b.bldNm },
  { label: '주용도', get: (b) => b.mainPurps },
  { label: '주구조', get: (b) => b.detail.strctCdNm },
  { label: '높이', get: (b) => `${b.detail.heit}m` },
  { label: '지상/지하', get: (b) => `지상 ${b.detail.grndFlr}층 / 지하 ${b.detail.ugrndFlr}층` },
  { label: '대지면적', get: (b) => `${b.platArea.toLocaleString()}㎡` },
  { label: '연면적', get: (b) => `${b.totArea.toLocaleString()}㎡` },
  {
    label: '건축면적',
    get: (b) =>
      `${b.detail.archArea.toLocaleString()}㎡ (건폐율 ${getBuildingCoverageRatio(b.detail.archArea, b.platArea)}%)`,
  },
  { label: '호수', get: (b) => `${b.detail.hoCnt}호` },
  { label: '사용승인일', get: (b) => formatDate(b.detail.useAprDay) },
  { label: '위반건축물', get: (b) => (b.detail.violBdYn === 'Y' ? '해당' : '해당없음') },
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
            <tr v-for="row in landRows" :key="row.label" class="odd:bg-white even:bg-surface-gray">
              <td class="pl-4 py-3 pr-4 text-text-sub w-[140px] align-top">{{ row.label }}</td>
              <td
                v-for="building in buildings"
                :key="building.buildingId"
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
              class="odd:bg-white even:bg-surface-gray"
            >
              <td class="pl-4 py-3 pr-4 text-text-sub w-[140px] align-top">{{ row.label }}</td>
              <td
                v-for="building in buildings"
                :key="building.buildingId"
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
          :key="building.buildingId"
          class="flex-1 min-w-0 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm px-4 py-1.5"
        >
          <table class="w-full text-[12px] table-fixed">
            <thead>
              <tr class="text-text-sub border-b border-surface-gray">
                <th class="text-left py-1.5 font-medium">층</th>
                <th class="text-left py-1.5 font-medium">용도</th>
                <th class="text-left py-1.5 font-medium">기타</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(floor, index) in building.floors"
                :key="index"
                class="border-b border-surface-gray"
              >
                <td class="py-1.5">{{ floor.flrGbNm }} {{ floor.flrNoNm }}</td>
                <td class="py-1.5">{{ floor.mainPurpsNm }}</td>
                <td class="py-1.5">{{ floor.etcPurps }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>
