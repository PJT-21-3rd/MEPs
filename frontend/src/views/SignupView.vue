<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import mepsLogo from '@/assets/images/MEPS_LOGO.png';

const router = useRouter();

const email = ref('');
const password = ref('');
const passwordConfirm = ref('');

function handleSignup() {
  // 입력값만 확인
  console.log('회원가입 시도:', email.value, password.value, passwordConfirm.value);
  // 백엔드 API 호출
}

function goLogin() {
  // 로그인으로 이동 (나중에 연결)
  console.log('로그인으로 이동');
}
</script>

<template>
  <div
    class="relative min-h-screen flex flex-col items-center justify-center bg-primary px-4 overflow-hidden"
  >
    <div class="absolute top-[15%] left-[10%] w-12 h-12 rounded-lg bg-white/5 rotate-12"></div>
    <div class="absolute top-[20%] right-[12%] w-16 h-16 rounded-lg bg-white/5 -rotate-6"></div>
    <div class="absolute bottom-[25%] left-[15%] w-10 h-10 rounded-lg bg-white/5 rotate-45"></div>
    <div class="absolute bottom-[20%] right-[18%] w-14 h-14 rounded-lg bg-white/5 rotate-12"></div>

    <div class="flex items-center gap-2 mb-6">
      <div class="w-8 h-8 rounded-lg bg-white flex items-center justify-center">
        <img :src="mepsLogo" alt="MEPS" class="h-8" />
      </div>
      <span class="text-white font-semibold">MEPS</span>
    </div>

    <!-- 회원가입 카드 -->
    <div class="relative bg-white rounded-2xl p-6 w-[380px]">
      <!-- 헤더 -->
      <div class="flex items-center gap-2 mb-1">
        <div class="w-8 h-8 rounded-lg bg-surface-gray flex items-center justify-center">
          <img :src="mepsLogo" alt="" class="h-8" />
        </div>
        <span class="font-bold">MEPS 회원가입</span>
      </div>
      <p class="text-[13px] text-text-sub mb-5">
        안전 진단 점수 및 근거 설명은 회원가입 후 확인하실 수 있습니다.
      </p>

      <!-- 이메일 -->
      <label class="text-[13px] font-medium">이메일</label>
      <input
        v-model="email"
        type="text"
        placeholder="아이디를 입력하세요"
        class="w-full px-3 py-2.5 mt-1 mb-3 bg-surface-gray border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />

      <!-- 비밀번호 -->
      <label class="text-[13px] font-medium">비밀번호</label>
      <input
        v-model="password"
        type="password"
        placeholder="비밀번호를 입력하세요"
        class="w-full px-3 py-2.5 mt-1 mb-3 bg-surface-gray border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />

      <!-- 비밀번호 확인 -->
      <label class="text-[13px] font-medium">비밀번호 확인</label>
      <input
        v-model="passwordConfirm"
        type="password"
        placeholder="비밀번호를 한번 더 입력하세요"
        class="w-full px-3 py-2.5 mt-1 mb-5 bg-surface-gray border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />

      <!-- 회원가입 버튼 -->
      <button @click="handleSignup" class="w-full py-3 bg-primary text-white font-bold rounded-lg">
        회원가입
      </button>

      <!-- 로그인 링크 -->
      <p class="text-center text-[13px] text-text-sub mt-4">
        이미 계정이 있으신가요?
        <button @click="goLogin" class="text-primary font-medium">로그인</button>
      </p>
    </div>
  </div>
</template>
