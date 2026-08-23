<template>
  <div
    class="sticky z-10 flex gap-1 border-b border-surface-gray bg-white/95 px-5 backdrop-blur"
    :style="{ top: `${headerHeight}px` }"
  >
    <button
      v-for="tab in TABS"
      :key="tab.id"
      @click="scrollToSection(tab.id)"
      class="relative px-3 py-3 text-[15px] transition-colors"
      :class="activeTab === tab.id ? 'text-primary' : 'text-text-sub'"
    >
      {{ tab.label }}
      <span
        v-if="activeTab === tab.id"
        class="absolute inset-x-3 bottom-0 h-0.5 rounded-full bg-primary"
      />
    </button>
  </div>

  <!-- 본문 -->
  <div v-if="buildingData" class="px-5 pb-50 pt-2">
    <!-- 토지 -->
    <section ref="sectionLand" id="section-land" class="scroll-mt-14 py-3">
      <div class="mb-2 flex items-center gap-2">
        <LandPlot :size="17" class="text-text-sub" />
        <p class="text-[16px] text-text-main">토지 정보</p>
      </div>
      <InfoRow label="면적" :value="formatArea(buildingData.platArea)" />
      <InfoRow label="지목" :value="buildingData.land?.lndcgrCodeNm" />
      <InfoRow label="용도지역" :value="buildingData.land?.prposAreaNm" />
      <!-- <InfoRow label="이용상황" /> -->
      <InfoRow label="도로접면" :value="buildingData.land?.roadSideCodeNm" />
      <!-- <InfoRow label="지형높이" /> -->
      <!-- <InfoRow label="지형향상" /> -->
      <InfoRow label="공시지가" :value="formatPrice(buildingData.land?.pblntfPclnd)" />
    </section>

    <!-- 건물 -->
    <section ref="sectionBuilding" id="section-building" class="scroll-mt-14 py-3">
      <div class="mb-2 flex items-center gap-2">
        <Building :size="17" class="text-text-sub" />
        <p class="text-[16px] text-text-main">건축물 정보</p>
      </div>
      <InfoRow label="건물이름" :value="buildingData.bldNm || '-'" />
      <InfoRow label="주용도" :value="buildingData.mainPurpsNm" />
      <!-- <InfoRow label="기타용도" /> -->
      <InfoRow label="주구조" :value="buildingData.detail?.strctCdNm" />
      <!-- <InfoRow label="지붕구조" /> -->
      <InfoRow label="높이" :value="`${buildingData.detail?.heit}m`" />
      <InfoRow
        label="지상/지하"
        :value="formatFloor(buildingData.detail?.grndFlr, buildingData.detail?.ugrndFlr)"
      />
      <InfoRow label="대지면적" :value="formatArea(buildingData.platArea)" />
      <InfoRow label="건축면적" :value="formatArea(buildingData.detail?.archArea)" />
      <InfoRow label="연면적" :value="formatArea(buildingData.totArea)" />
      <InfoRow label="호수" :value="`${buildingData.detail.hoCnt}호`" />
      <InfoRow label="사용승인일" :value="formatDate(buildingData.detail?.useAprDay)" />
      <InfoRow label="위반건축여부" :value="formatViolation(buildingData.detail?.violBdYn)" />
    </section>

    <!-- 층별현황 -->
    <section ref="sectionFloor" id="section-floor" class="scroll-mt-14 py-3">
      <div class="mb-2 flex items-center gap-2">
        <Layers :size="17" class="text-text-sub" />
        <p class="text-[16px] text-text-main">층별 현황</p>
      </div>
      <div class="overflow-hidden rounded-xl border border-surface-gray">
        <div class="flex items-center bg-surface-base px-4 py-2 text-[12px] text-text-sub">
          <span class="w-14">층</span>
          <span class="flex-1">주용도</span>
          <span class="w-20 text-right">기타용도</span>
        </div>
        <div
          v-for="(floor, idx) in buildingData.floors"
          :key="idx"
          class="flex items-center border-t border-surface-base px-4 py-2.5"
        >
          <span class="w-14 text-[14px]">
            {{ formatFloorName(floor.flrGbNm, floor.flrNoNm) }}
          </span>
          <span class="flex-1 text-[14px] text-text-secondary/95">
            {{ floor.mainPurpsNm || '-' }}
          </span>
          <span class="w-20 text-right text-[14px] text-text-secondary/90">
            {{ floor.etcPurps || '-' }}
          </span>
        </div>
      </div>
    </section>
    <FeedbackBanner />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Building, LandPlot, Layers } from '@lucide/vue';
import {
  formatArea,
  formatPrice,
  formatDate,
  formatFloor,
  formatFloorName,
  formatViolation,
} from '@/utils/formatters';
import InfoRow from './InfoRow.vue';
import FeedbackBanner from '../common/FeedbackBanner.vue';

const props = defineProps({
  buildingData: { type: Object, default: () => null },
  isStuck: { type: Boolean, default: false },
  headerHeight: { type: Number, default: 0 },
});

const TABS = [
  { id: 'land', label: '토지 정보' },
  { id: 'building', label: '건축물 정보' },
  { id: 'floor', label: '층별 현황' },
];

const activeTab = ref('land');
const sectionLand = ref(null);
const sectionBuilding = ref(null);
const sectionFloor = ref(null);

let observer = null;

const isClickScrolling = ref(false);
let scrollTimeout = null;

const visibleSections = ref({
  land: false,
  building: false,
  floor: false,
});

onMounted(() => {
  const scrollBox = sectionLand.value?.closest('.overflow-y-auto');
  observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        const tabId = entry.target.id.replace('section-', '');
        visibleSections.value[tabId] = entry.isIntersecting;
      });

      if (isClickScrolling.value) return;

      const orderedSections = ['land', 'building', 'floor'];
      for (const id of orderedSections) {
        if (visibleSections.value[id]) {
          activeTab.value = id;
          break;
        }
      }
    },
    {
      root: scrollBox,
      rootMargin: '-130px 0px -50% 0px',
    },
  );
  if (sectionLand.value) observer.observe(sectionLand.value);
  if (sectionBuilding.value) observer.observe(sectionBuilding.value);
  if (sectionFloor.value) observer.observe(sectionFloor.value);
});

onUnmounted(() => {
  if (observer) observer.disconnect();
  if (scrollTimeout) clearTimeout(scrollTimeout);
});

const scrollToSection = (tabId) => {
  const sectionRefs = { land: sectionLand, building: sectionBuilding, floor: sectionFloor };
  const target = sectionRefs[tabId].value;

  if (target) {
    const scrollContainer = target.closest('.overflow-y-auto');

    if (scrollContainer) {
      activeTab.value = tabId;
      isClickScrolling.value = true;

      const targetPosition = target.offsetTop - 125;

      scrollContainer.scrollTo({
        top: targetPosition,
        behavior: 'smooth',
      });

      if (scrollTimeout) clearTimeout(scrollTimeout);
      scrollTimeout = setTimeout(() => {
        isClickScrolling.value = false;
      }, 800);
    }
  }
};
</script>
