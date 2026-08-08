<script setup>
import { useRouter } from 'vue-router';
import mepsLogo from '@/assets/images/MEPS_LOGO.png';
import { Home } from '@lucide/vue';

const router = useRouter();

function goHome() {
  router.push('/');
}
</script>

<template>
  <div
    class="relative min-h-screen flex flex-col items-center justify-center bg-primary text-center px-4 overflow-hidden"
  >
    <!-- 배경 장식 도형들 -->
    <div class="absolute top-[15%] left-[10%] w-12 h-12 rounded-lg bg-white/5 rotate-12"></div>
    <div class="absolute top-[20%] right-[12%] w-16 h-16 rounded-lg bg-white/5 -rotate-6"></div>
    <div class="absolute bottom-[25%] left-[15%] w-10 h-10 rounded-lg bg-white/5 rotate-45"></div>
    <div class="absolute bottom-[20%] right-[18%] w-14 h-14 rounded-lg bg-white/5 rotate-12"></div>
    <div class="absolute top-[45%] left-[8%] w-8 h-8 rounded-lg bg-white/5 -rotate-12"></div>
    <!-- 로고 -->
    <div class="flex items-center gap-2 mb-6">
      <div class="w-8 h-8 rounded-lg bg-white flex items-center justify-center">
        <img :src="mepsLogo" alt="MEPS" class="h-8" />
      </div>
      <span class="text-white">MEPS</span>
    </div>

    <!-- 404 -->
    <h1 class="text-[100px] font-bold text-secondary leading-none">404</h1>

    <!-- 구분선 -->
    <div class="w-10 h-px bg-white/30 my-4"></div>

    <!-- 안내 문구 -->
    <h2 class="text-lg font-bold text-white mb-2">해당 상가를 찾을 수 없어요</h2>
    <p class="text-[13px] text-white/70 leading-relaxed">
      요청하신 페이지가 사라졌거나 주소가 잘못 입력됐어요.<br />
      지도로 돌아가 안전한 상가를 다시 찾아볼까요?
    </p>

    <!-- 버튼 -->

    <button
      @click="goHome"
      class="flex items-center gap-1.5 px-4 py-2.5 bg-secondary text-primary font-bold text-[14px] rounded-lg mt-6"
    >
      <Home :size="16" />
      홈으로 돌아가기
    </button>
  </div>
</template>
