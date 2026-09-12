<script setup>
import { X } from '@lucide/vue';
import mepsLogo from '@/assets/images/MEPS_LOGO.png';
import { useRouter } from 'vue-router';
import LoginForm from './LoginForm.vue';

const emit = defineEmits(['close']);
const router = useRouter();

function handleSignup() {
  emit('close'); // 모달 닫고
  router.push('/signup'); // 회원가입 페이지로
}
</script>

<template>
  <!-- 배경 어둡게 (오버레이) -->
  <div class="fixed inset-0 bg-black/40 flex items-center justify-center z-1000">
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

      <p class="text-[13px] text-text-sub mb-3 mt-2">
        안전 진단 점수 및 근거 설명은 로그인 후 확인하실 수 있어요
      </p>
      <!-- 로그인 폼 (공통) -->
      <LoginForm @signup="handleSignup" @success="emit('close')" />
    </div>
  </div>
</template>
