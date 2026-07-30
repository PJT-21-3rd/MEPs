<!-- src/components/property/CommercialAiBriefing.vue -->
<script setup>
import { Sparkles, Users, Store, Building2, ChartNetwork } from '@lucide/vue';
import StatChip from './StatChip.vue';
import { onMounted, onUnmounted, ref } from 'vue';
import { formatPopulation, formatRate } from '@/utils/formatters';

const apiData = ref({
  sggName: '강남구',
  adstrdName: '대치동',
  dailyFlpop: 125000, // 120000 이상 숫자
  flpopChangeRate: 10.24, // 증감률 (Float)
  topIndustryName: '일반음식점', // 주요업종
  topIndustryEtcCnt: 3, // 기타 업종 수
  avgBuildingAge: 11.2, // 노후도
  majorAgeGroup: '20대', // 주요 연령층
  majorAgeRatio: 58.02, // 비율
  overallBriefing:
    '일 평균 유동인구 3.8만 명의 활발한 상권으로, 음식점·카페 창업 수요가 특히 높아요. 주 소비층은 20~30대 직장인이 58%로, 트렌디한 업종이 유리해요. 평균 노후도 15년으로 건물 상태는 양호하지만, 최근 3년간 저지대 침수가 2건 있었으니 1층 매장은 주의가 필요해요.',
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
  <div class="rounded-2xl bg-gradient-to-br from-surface-blue to-[#f5fbff] p-5">
    <!-- 헤더 -->
    <div class="mb-3 flex items-center gap-2">
      <span
        class="flex items-center gap-1 rounded-full bg-button-primary px-2.5 py-1 text-[12px] text-white"
      >
        <Sparkles :size="12" /> AI 브리핑
      </span>
      <p class="text-[18px] tracking-tight text-neutral-900">대치동</p>
      <p class="text-[14px] text-neutral-400">강남구</p>
    </div>

    <!-- 카드 섹션 -->
    <div class="mb-3.5 grid grid-cols-2 gap-2">
      <StatChip
        label="일 유동인구"
        :icon="ChartNetwork"
        :value="formatPopulation(apiData.dailyFlpop)"
        :sub="formatRate(apiData.flpopChangeRate)"
      />
      <StatChip
        label="주요 업종"
        :icon="Store"
        :value="apiData.topIndustryName"
        :sub="`외 ${apiData.topIndustryEtcCnt}종`"
      />
      <StatChip label="평균 노후도" :icon="Building2" :value="`${apiData.avgBuildingAge}년`" />
      <StatChip
        label="주요 연령층"
        :icon="Users"
        :value="apiData.majorAgeGroup"
        :sub="`${apiData.majorAgeRatio}%`"
      />
    </div>

    <!-- 헤드 뉴스 라인 -->
    <div class="mb-3 flex items-center gap-2 rounded-lg bg-red-50 px-3 py-2 h-10">
      <span class="shrink-0 rounded bg-red-500 px-1.5 py-0.5 text-[10px] font-bold text-white z-10">
        NEWS
      </span>

      <div class="relative flex-1 h-6 overflow-hidden">
        <Transition name="slide-up">
          <div :key="currentNewsIndex" class="absolute inset-0 flex items-center">
            <p
              class="w-full truncate text-[13px] font-medium text-red-600 hover:underline cursor-pointer"
              title="클릭하여 상세 보기"
            >
              {{ mockNews[currentNewsIndex] }}
            </p>
          </div>
        </Transition>
      </div>
    </div>

    <!-- 동에 대한 설명 -->
    <p class="text-[15px] leading-[1.65] text-neutral-700">
      {{ apiData.overallBriefing }}
    </p>
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
