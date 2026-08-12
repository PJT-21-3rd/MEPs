<script setup>
import { useRoute, useRouter } from 'vue-router';
import LoginForm from '@/components/auth/LoginForm.vue';
import mepsLogo from '@/assets/images/MEPS_LOGO.png';

const router = useRouter();
const route = useRoute();

function handleSignup() {
  router.push('/signup'); // 회원가입 페이지로
}
function handleSuccess() {
  const redirect = route.query.redirect || '/';
  router.push(redirect);
}
function goHome() {
  router.push('/');
}
</script>

<template>
  <div
    class="relative min-h-screen flex flex-col items-center justify-center bg-primary px-4 overflow-hidden"
  >
    <!-- 배경 도형 (회원가입이랑 같음) -->
    <div class="absolute top-[15%] left-[10%] w-12 h-12 rounded-lg bg-white/5 rotate-12"></div>
    <div class="absolute top-[20%] right-[12%] w-16 h-16 rounded-lg bg-white/5 -rotate-6"></div>
    <div class="absolute bottom-[25%] left-[15%] w-10 h-10 rounded-lg bg-white/5 rotate-45"></div>
    <div class="absolute bottom-[20%] right-[18%] w-14 h-14 rounded-lg bg-white/5 rotate-12"></div>

    <!-- 로고 -->
    <div class="flex items-center gap-2 mb-6 cursor-pointer" @click="goHome">
      <div class="w-8 h-8 rounded-lg bg-white flex items-center justify-center">
        <img :src="mepsLogo" alt="MEPS" class="h-8" />
      </div>
      <span class="text-white font-semibold">MEPS</span>
    </div>

    <!-- 로그인 카드 -->
    <div class="relative bg-white rounded-2xl p-6 w-[380px]">
      <div class="flex items-center gap-2 mb-1">
        <div class="w-8 h-8 rounded-lg bg-surface-gray flex items-center justify-center">
          <img :src="mepsLogo" alt="" class="h-8" />
        </div>
        <span class="font-bold">MEPS 로그인</span>
      </div>
      <p class="text-[13px] text-text-sub mb-5">
        안전 진단 점수 및 근거 설명은 로그인 후 확인하실 수 있습니다.
      </p>

      <!-- 로그인 폼 (공통) -->
      <LoginForm @signup="handleSignup" @success="handleSuccess" />
    </div>
  </div>
</template>
