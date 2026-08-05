<script setup>
import { ref, computed } from 'vue';
import { Scale, Sparkles, Info, LandPlot, Building2, Layers } from '@lucide/vue';
import { compareData } from '@/mocks/compareData';
import ScoreGauge from '@/components/report/ScoreGauge.vue';
import AiBriefingCard from '@/components/report/AiBriefingCard.vue';
import DiagnosticFactorList from '@/components/report/DiagnosticFactorList.vue';

const compareBuildings = computed(() => {
  return props.selectedIds.map((id) => compareData[id]);
});

const props = defineProps({
  selectedIds: Array,
});

const sections = [
  { key: 'report', label: 'AI 안전진단', icon: Sparkles },
  { key: 'basic', label: '기본 정보', icon: Info },
  { key: 'land', label: '토지 정보', icon: LandPlot },
  { key: 'building', label: '건축물 정보', icon: Building2 },
  { key: 'floor', label: '층별 정보', icon: Layers },
];

const activeSections = ref(['report', 'basic', 'land', 'building', 'floor']);

function toggleSection(key) {
  const index = activeSections.value.indexOf(key);
  if (index !== -1) {
    if (activeSections.value.length === 1) return;
    activeSections.value.splice(index, 1);
  } else {
    activeSections.value.push(key);
  }
}

function isActive(key) {
  return activeSections.value.includes(key);
}

function formatDate(yyyymmdd) {
  if (!yyyymmdd || yyyymmdd.length !== 8) return yyyymmdd;
  return `${yyyymmdd.slice(0, 4)}-${yyyymmdd.slice(4, 6)}-${yyyymmdd.slice(6, 8)}`;
}
</script>

<template>
  <section class="flex-1 min-w-0 flex flex-col h-full">
    <!-- 헤더: 제목 + 섹션 표시 개수 + 토글 -->
    <div class="pb-4 border-b border-surface-gray">
      <div class="flex items-center justify-between mb-3">
        <h2 class="flex items-center gap-2 text-base font-bold">
          <Scale :size="18" class="text-primary" />
          찜한 매물 비교
        </h2>
        <span class="text-[13px] text-text-sub">
          {{ activeSections.length }}/{{ sections.length }}개 섹션 표시
        </span>
      </div>

      <div class="flex flex-wrap gap-2">
        <button
          v-for="section in sections"
          :key="section.key"
          @click="toggleSection(section.key)"
          class="flex items-center gap-1.5 px-3 py-2 rounded-full text-[13px] font-medium border transition-colors"
          :class="
            isActive(section.key)
              ? 'bg-primary text-white border-primary'
              : 'bg-surface-gray text-text-sub border-surface-gray'
          "
        >
          <component :is="section.icon" :size="15" />
          {{ section.label }}
        </button>
      </div>
    </div>
    <div class="flex-1 overflow-y-auto pt-4">
      <!-- 2개 미만: 안내 문구 -->
      <div
        v-if="compareBuildings.length < 2"
        class="flex-1 flex flex-col items-center justify-center py-20 text-text-sub"
      >
        <Building2 :size="40" class="mb-3 opacity-40" />
        <p class="text-[15px]">
          {{ compareBuildings.length === 0 ? '비교할 매물을 선택하세요' : '하나 더 선택해주세요' }}
        </p>
        <p class="text-[13px] mt-1 opacity-70">
          왼쪽 찜 목록에서 매물을 선택하면 상세 조건이 비교됩니다
        </p>
      </div>

      <!-- 2개 이상: 비교 뷰 -->
      <div v-else class="pt-4 flex gap-4">
        <div
          v-for="building in compareBuildings"
          :key="building.buildingId"
          class="flex-1 min-w-0 border border-surface-gray rounded-xl p-4"
        >
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
          <div v-if="isActive('report')" class="mb-4">
            <h4 class="text-[13px] font-bold text-primary mb-2">AI 안전진단</h4>
            <div class="flex justify-center py-2">
              <ScoreGauge :score="building.score" />
            </div>
            <AiBriefingCard :loading="false" :briefing="building.briefing" />
            <DiagnosticFactorList :items="building.diagnosis" mode="summary" class="mt-2" />
          </div>

          <!-- 기본 정보 섹션 -->
          <div v-if="isActive('basic')" class="mb-4">
            <h4 class="text-[13px] font-bold text-primary mb-2">기본 정보</h4>
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
          <div v-if="isActive('land')" class="mb-4">
            <h4 class="text-[13px] font-bold text-primary mb-2">토지 정보</h4>
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
          <div v-if="isActive('building')" class="mb-4">
            <h4 class="text-[13px] font-bold text-primary mb-2">건축물 정보</h4>
            <dl class="text-[13px]">
              <div class="flex justify-between py-1 border-b border-surface-gray">
                <dt class="text-text-sub">주구조</dt>
                <dd>{{ building.detail.strctCdNm }}</dd>
              </div>
              <div class="flex justify-between py-1 border-b border-surface-gray">
                <dt class="text-text-sub">층수</dt>
                <dd>
                  지상 {{ building.detail.grndFlr }}층 / 지하 {{ building.detail.ugrndFlr }}층
                </dd>
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
          <div v-if="isActive('floor')" class="mb-4">
            <h4 class="text-[13px] font-bold text-primary mb-2">층별 현황</h4>
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
    </div>
  </section>
</template>
