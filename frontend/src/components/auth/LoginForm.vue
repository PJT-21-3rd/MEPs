<script setup>
import { ref } from 'vue';
import { login } from '@/api/auth';
import { useAuthStore } from '@/stores/authStore';

const authStore = useAuthStore();

const email = ref('');
const password = ref('');
const errorMessage = ref('');

const emit = defineEmits(['signup', 'success']);

async function handleLogin() {
  errorMessage.value = '';
  if (!email.value || !password.value) {
    errorMessage.value = '아이디와 비밀번호를 입력해주세요';
    return;
  }
  try {
    const data = await login({ email: email.value, password: password.value });
    authStore.setToken(data.accessToken, email.value);
    emit('success');
  } catch (error) {
    console.log('로그인 에러:', error);
    if (error.response?.status === 401) {
      errorMessage.value = '아이디 또는 비밀번호가 올바르지 않습니다';
    } else if (error.response?.status === 400) {
      errorMessage.value = '아이디와 비밀번호를 입력해주세요';
    } else {
      errorMessage.value = '로그인에 실패했습니다';
    }
  }
}

function goSignup() {
  emit('signup'); // 회원가입으로 가라고 부모에게 알림
}
</script>

<template>
  <div>
    <!-- 아이디 -->
    <label class="text-[13px] font-medium">아이디</label>
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

    <!-- 에러 메시지 -->
    <p v-if="errorMessage" class="text-[13px] text-status-danger mb-3">
      {{ errorMessage }}
    </p>

    <!-- 로그인 버튼 -->
    <button @click="handleLogin" class="w-full py-3 bg-primary text-white font-bold rounded-lg">
      로그인
    </button>

    <!-- 회원가입 링크 -->
    <p class="text-center text-[13px] text-text-sub mt-4">
      아직 회원이 아니신가요?
      <button @click="goSignup" class="text-primary font-medium underline">회원가입</button>
    </p>
  </div>
</template>
