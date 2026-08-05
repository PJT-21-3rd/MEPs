<script setup>
import ScoreGauge from '@/components/report/ScoreGauge.vue';
import AiBriefingCard from '@/components/report/AiBriefingCard.vue';
import DiagnosticFactorList from '@/components/report/DiagnosticFactorList.vue';
import { Sparkles, Info, LandPlot, Building2, Layers } from '@lucide/vue';

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
    <!-- 인범 님 기본정보 컴포넌트 자리 (사진 + 건물정보) -->
    <!-- TODO: <BuildingBasicInfo :building="building" /> 로 교체 -->
    <div
      class="mb-4 flex items-center justify-center h-32 bg-surface-gray rounded-lg text-text-sub text-[13px]"
    >
      사진+기본정보 (인범 님 컴포넌트 예정)
    </div>

    <!-- 건물명/주소 (인범 컴포넌트에 포함되면 제거) -->
    <h3 class="text-base font-bold">{{ building.roadAddr }}</h3>
    <p class="text-[13px] text-text-sub mt-1 mb-4">{{ building.bldNm }}</p>

    <!-- AI 안전진단 섹션 -->
    <div v-if="isActive('report')" class="mb-4 p-3 border border-surface-gray rounded-lg">
      <h4 class="flex items-center gap-1 text-[13px] font-bold text-primary mb-2">
        <Sparkles :size="15" />
        AI 안전진단
      </h4>
      <div class="flex justify-center py-2">
        <ScoreGauge :score="building.score" />
      </div>
      <AiBriefingCard :loading="false" :briefing="building.briefing" />
      <DiagnosticFactorList :items="building.diagnosis" mode="summary" class="mt-2" />
    </div>

    <!-- 기본 정보 섹션 -->
    <div v-if="isActive('basic')" class="mb-4 p-3 border border-surface-gray rounded-lg">
      <h4 class="flex items-center gap-1 text-[13px] font-bold text-primary mb-2">
        <Info :size="15" />기본 정보
      </h4>
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

    <!-- 토지 정보 섹션 -->
    <div v-if="isActive('land')" class="mb-4 p-3 border border-surface-gray rounded-lg">
      <h4 class="flex items-center gap-1 text-[13px] font-bold text-primary mb-2">
        <LandPlot :size="15" />토지 정보
      </h4>
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

    <!-- 건축물 정보 섹션 -->
    <div v-if="isActive('building')" class="mb-4 p-3 border border-surface-gray rounded-lg">
      <h4 class="flex items-center gap-1 text-[13px] font-bold text-primary mb-2">
        <Building2 :size="15" />건축물 정보
      </h4>
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

    <!-- 층별 정보 섹션 -->
    <div v-if="isActive('floor')" class="mb-4 p-3 border border-surface-gray rounded-lg">
      <h4 class="flex items-center gap-1 text-[13px] font-bold text-primary mb-2">
        <Layers :size="15" />층별 현황
      </h4>
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
</template>
