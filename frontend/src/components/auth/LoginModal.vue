<script setup>
import { ref } from 'vue';
import { X } from '@lucide/vue';
import mepsLogo from '@/assets/images/MEPS_LOGO.png';

const emit = defineEmits(['close']);

const email = ref('');
const password = ref('');

function handleLogin() {
  console.log('로그인 시도: ', email.value, password.value);
  //이곳에 백엔드 API 호출
}
</script>

<template>
  <!-- 배경 어둡게 (오버레이) -->
  <div
    class="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
    @click="emit('close')"
  >
    <!-- 모달 카드 -->
    <div class="bg-white rounded-2xl p-6 w-[360px]" @click.stop>
      <!-- 헤더 -->
      <div class="flex items-center justify-between mb-1">
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-surface-gray flex items-center justify-center">
            <img :src="mepsLogo" alt="MEPS" class="h-6" />
          </div>
          <span class="font-bold">MEPS 로그인</span>
        </div>
        <button @click="emit('close')"><X :size="20" class="text-text-sub" /></button>
      </div>
      <!-- 안전 진단 점수 및 근거 설명은 로그인 후 확인하실 수 있습니다. -->
      <p class="text-[13px] text-text-sub mb-5">
        안전 진단 점수 및 근거 설명은 로그인 후 확인하실 수 있습니다.
      </p>
      <!-- 아이디 -->
      <label class="text-[13px] font-medium">아이디</label>
      <input
        v-model="email"
        type="text"
        placeholder="아이디를 입력하세요"
        class="w-full px-3 py-2.5 mt-1 mb-3 border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />
      <!-- 비밀번호 -->
      <label class="text-[13px] font-medium">비밀번호</label>
      <input
        v-model="password"
        type="password"
        placeholder="비밀번호를 입력하세요"
        class="w-full px-3 py-2.5 mt-1 mb-5 border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />
      <!-- 로그인버튼 -->
      <button @click="handleLogin" class="w-full py-3 bg-primary text-white font-bold rounded-lg">
        로그인
      </button>
      <!-- 회원가입 링크 -->
      <p class="text-center text-[13px] text-text-sub mt-4">
        아직 회원이 아니신가요?
        <button class="text-primary font-medium">회원가입</button>
      </p>
    </div>
  </div>
</template>
