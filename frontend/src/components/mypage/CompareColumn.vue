<script setup>
import ScoreGauge from '@/components/report/ScoreGauge.vue';
import AiBriefingCard from '@/components/report/AiBriefingCard.vue';
import DiagnosticFactorList from '@/components/report/DiagnosticFactorList.vue';
import { Sparkles, Info, LandPlot, Building2, Layers } from '@lucide/vue';
import BuildingInfoPannel from '../detail/BuildingInfoPannel.vue';
import RoadViewImage from '../detail/RoadViewImage.vue';
const props = defineProps({
  building: Object,
  activeSections: Array,
});

function isActive(key) {
  return props.activeSections.includes(key);
}

function formatDate(yyyymmdd) {
  if (!yyyymmdd || yyyymmdd.length !== 8) return yyyymmdd;
  return `${yyyymmdd.slice(0, 4)}-${yyyymmdd.slice(4, 6)}-${yyyymmdd.slice(6, 8)}`;
}
</script>

<template>
  <div class="flex-1 min-w-0 border border-surface-gray rounded-xl p-4">
    <div class="mb-4 border border-surface-gray rounded-2xl">
      <!-- 로드뷰 사진 -->
      <div class="mb-3">
        <RoadViewImage :lat="building.lat" :lng="building.lng" />
      </div>

      <!-- 건물 기본정보 -->
      <div class="mb-4 px-4">
        <BuildingInfoPannel :buildingData="building" />
      </div>
    </div>

    <!-- AI 안전진단 섹션 -->
    <div v-if="isActive('report')" class="mb-4">
      <h4 class="flex items-center gap-1 text-[15px] font-bold text-primary mb-2">
        <Sparkles :size="17" />
        AI 안전진단
      </h4>
      <div class="p-3 border border-surface-gray rounded-lg">
        <div class="flex items-center gap-4">
          <div class="scale-75 origin-center shrink-0 -mx-6 -my-6">
            <ScoreGauge :score="building.score" />
          </div>
          <div class="flex-1 min-w-0">
            <AiBriefingCard :loading="false" :briefing="building.briefing" />
          </div>
        </div>
        <DiagnosticFactorList :items="building.diagnosis" mode="summary" class="mt-2" />
      </div>
    </div>

    <!-- 기본 정보 섹션 -->
    <div v-if="isActive('basic')" class="mb-4">
      <h4 class="flex items-center gap-1 text-[15px] font-bold text-primary mb-2">
        <Info :size="17" />기본 정보
      </h4>
      <div class="p-3 border border-surface-gray rounded-lg">
        <dl class="text-[13px]">
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">주용도</dt>
            <dd>{{ building.mainPurps }}</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">대지면적</dt>
            <dd>{{ building.platArea.toLocaleString() }}㎡</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">연면적</dt>
            <dd>{{ building.totArea.toLocaleString() }}㎡</dd>
          </div>
          <div class="flex justify-between py-1">
            <dt class="text-text-sub">호수</dt>
            <dd>{{ building.hoCnt }}세대</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- 토지 정보 섹션 -->
    <div v-if="isActive('land')" class="mb-4">
      <h4 class="flex items-center gap-1 text-[15px] font-bold text-primary mb-2">
        <LandPlot :size="17" />토지 정보
      </h4>
      <div class="p-3 border border-surface-gray rounded-lg">
        <dl class="text-[13px]">
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">지목</dt>
            <dd>{{ building.land.lndcgrCodeNm }}</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">용도지역</dt>
            <dd>{{ building.land.prposAreaNm }}</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">도로조건</dt>
            <dd>{{ building.land.roadSideCodeNm }}</dd>
          </div>
          <div class="flex justify-between py-1">
            <dt class="text-text-sub">공시지가</dt>
            <dd>{{ building.land.pbIntfPcInd.toLocaleString() }}원/㎡</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- 건축물 정보 섹션 -->
    <div v-if="isActive('building')" class="mb-4">
      <h4 class="flex items-center gap-1 text-[15px] font-bold text-primary mb-2">
        <Building2 :size="17" />건축물 정보
      </h4>
      <div class="p-3 border border-surface-gray rounded-lg">
        <dl class="text-[13px]">
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">주구조</dt>
            <dd>{{ building.detail.strctCdNm }}</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">층수</dt>
            <dd>지상 {{ building.detail.grndFlr }}층 / 지하 {{ building.detail.ugrndFlr }}층</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">사용승인일</dt>
            <dd>{{ formatDate(building.detail.useAprDay) }}</dd>
          </div>
          <div class="flex justify-between py-1 border-b border-surface-gray">
            <dt class="text-text-sub">경과연차</dt>
            <dd>{{ building.detail.elapsedYear }}년</dd>
          </div>
          <div class="flex justify-between py-1">
            <dt class="text-text-sub">위반건축물</dt>
            <dd>{{ building.detail.violBdYn === 'Y' ? '해당' : '해당없음' }}</dd>
          </div>
        </dl>
      </div>
    </div>

    <!-- 층별 정보 섹션 -->
    <div v-if="isActive('floor')" class="mb-4">
      <h4 class="flex items-center gap-1 text-[15px] font-bold text-primary mb-2">
        <Layers :size="17" />층별 정보
      </h4>
      <div class="p-3 border border-surface-gray rounded-lg">
        <table class="w-full text-[12px]">
          <thead>
            <tr class="text-text-sub border-b border-surface-gray">
              <th class="text-left py-1 font-medium">층</th>
              <th class="text-left py-1 font-medium">용도</th>
              <th class="text-left py-1 font-medium">기타</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(floor, index) in building.floors"
              :key="index"
              class="border-b border-surface-gray"
            >
              <td class="py-1">{{ floor.flrGbNm }} {{ floor.flrNoNm }}</td>
              <td class="py-1">{{ floor.mainPurpsNm }}</td>
              <td class="py-1">{{ floor.etcPurps }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
