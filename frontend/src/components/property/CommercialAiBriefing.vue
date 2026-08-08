<script setup>
import { Users, Store, Building2, ChartNetwork, Summary } from '@lucide/vue';
import StatChip from './StatChip.vue';
import { onMounted, onUnmounted, ref } from 'vue';
import { formatPopulation, formatRate } from '@/utils/formatters';

defineProps({
  summary: {
    type: Object,
    required: true,
  },
});

const mockNews = ref([
  "'대치동' 인근 상습 침수 구역 하수관거 정비 사업 착수",
  '강남구, 학원가 및 노후 상가 화재 예방 특별 점검 실시',
  '대치동 꼬마빌딩 거래량 전월 대비 15% 껑충',
]);

const currentNewsIndex = ref(0);
let newsInterval = null;

onMounted(() => {
  newsInterval = setInterval(() => {
    currentNewsIndex.value = (currentNewsIndex.value + 1) % mockNews.value.length;
  }, 5000);
});

onUnmounted(() => {
  if (newsInterval) clearInterval(newsInterval);
});
</script>

<template>
  <!-- 카드 섹션 -->
  <div class="px-6 pb-5">
    <div class="relative overflow-hidden rounded-b-2xl bg-surface-blue px-5 pb-5 pt-1">
      <!-- <div
          className="pointer-events-none absolute -right-10 -top-16 h-40 w-40 rounded-full bg-gradient-to-br from-white/70 to-transparent blur-2xl"
        /> -->

      <div class="relative mb-3.5 grid grid-cols-2 gap-2">
        <StatChip
          label="일 유동인구"
          :icon="ChartNetwork"
          :value="formatPopulation(summary.dailyFlpop)"
          :sub="formatRate(summary.flpopChangeRate)"
        />
        <StatChip
          label="주요 업종"
          :icon="Store"
          :value="summary.topIndustryName"
          :sub="`외 ${summary.topIndustryEtcCnt}종`"
        />
        <StatChip label="평균 노후도" :icon="Building2" :value="`${summary.avgBuildingAge}년`" />
        <StatChip
          label="주요 연령층"
          :icon="Users"
          :value="summary.majorAgeGroup"
          :sub="`${summary.majorAgeRatio}%`"
        />
      </div>

      <!-- 헤드 뉴스 라인 -->
      <div class="mb-3 flex items-center gap-2 rounded-lg bg-surface-red px-2.5 py-1.5 h-10">
        <span
          class="shrink-0 rounded bg-status-like px-1.5 py-0.5 text-[10px] font-bold text-white z-10"
        >
          NEWS
        </span>

        <div class="relative flex-1 h-6 overflow-hidden">
          <Transition name="slide-up">
            <div :key="currentNewsIndex" class="absolute inset-0 flex items-center">
              <p
                class="w-full truncate text-[13px] text-status-like hover:underline cursor-pointer"
                title="클릭하여 상세 보기"
              >
                {{ mockNews[currentNewsIndex] }}
              </p>
            </div>
          </Transition>
        </div>
      </div>

      <!-- 동에 대한 설명 -->
      <p class="text-[15px] leading-[1.65] text-text-secondary">
        {{ summary.overallBriefing }}
      </p>
    </div>
  </div>
</template>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 1s ease-in-out;
}

/* 새로 들어오는 뉴스는 아래에서 시작 */
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(100%);
}

/* 기존에 있던 뉴스는 위로 사라짐 */
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-70%);
}
</style>
