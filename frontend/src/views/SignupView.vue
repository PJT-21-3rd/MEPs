<script setup>
import { ref, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import mepsLogo from '@/assets/images/MEPS_LOGO.png';
import { ChevronRight } from '@lucide/vue';
import { signup, login } from '@/api/auth';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const step = ref('terms');

// 약관 체크 상태
const agreeService = ref(false);
const agreeLocation = ref(false);
const agreePrivacy = ref(false);
const agreeAge = ref(false);
// 회원가입 입력
const email = ref('');
const password = ref('');
const passwordConfirm = ref('');
const errorMessage = ref('');
const termsError = ref('');

const agreeAll = computed({
  get() {
    return agreeService.value && agreeLocation.value && agreePrivacy.value && agreeAge.value;
  },
  set(value) {
    agreeService.value = value;
    agreeLocation.value = value;
    agreePrivacy.value = value;
    agreeAge.value = value;
  },
});

// 약관 → 폼으로
function goToForm() {
  // 필수 약관 체크 확인
  if (!agreeService.value || !agreeLocation.value || !agreePrivacy.value || !agreeAge.value) {
    termsError.value = '필수 약관에 모두 동의해주세요';
    return;
  }
  step.value = 'form';
}

async function handleSignup() {
  errorMessage.value = '';
  // 빈 칸 검사
  if (!email.value || !password.value || !passwordConfirm.value) {
    errorMessage.value = '모든 항목을 입력해주세요';
    return;
  }
  // 비밀번호 형식 검사 (명세: 8~20자, 영문+숫자+특수문자)
  const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,20}$/;
  if (!pwRegex.test(password.value)) {
    errorMessage.value = '비밀번호는 8~20자, 영문·숫자·특수문자를 모두 포함해야 합니다';
    return;
  }
  // 비밀번호 일치 검사
  if (password.value !== passwordConfirm.value) {
    errorMessage.value = '비밀번호가 일치하지 않습니다';
    return;
  }
  // API 호출
  try {
    // 회원가입
    await signup({
      email: email.value,
      password: password.value,
      passwordConfirm: passwordConfirm.value,
    });
    // 자동로그인
    const data = await login({ email: email.value, password: password.value });
    authStore.setToken(data.accessToken, email.value);
    // 리다이렉트
    const redirect = route.query.redirect || '/';
    router.push(redirect);
  } catch (error) {
    if (error.response?.status === 409) {
      errorMessage.value = '이미 사용 중인 이메일입니다';
    } else if (error.response?.status === 400) {
      errorMessage.value = '입력값을 확인해주세요';
    } else {
      errorMessage.value = '회원가입에 실패했습니다';
    }
  }
}

function goLogin() {
  router.push({ name: 'Login', query: route.query });
}

// 약관 상세 (지금은 자리만)
function showTerms(type) {
  // TODO: 약관 텍스트 준비되면 상세 모달/페이지 연결
  console.log('약관 상세:', type);
}

function goHome() {
  router.push('/');
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

    <div class="flex items-center gap-2 mb-6 cursor-pointer" @click="goHome">
      <div class="w-8 h-8 rounded-lg bg-white flex items-center justify-center">
        <img :src="mepsLogo" alt="MEPS" class="h-8" />
      </div>
      <span class="text-white font-semibold">MEPS</span>
    </div>

    <!-- 1단계: 이용약관 -->
    <div v-if="step === 'terms'" class="relative bg-white rounded-2xl p-6 w-[380px]">
      <div class="flex items-center gap-2 mb-1">
        <div class="w-8 h-8 rounded-lg bg-surface-gray flex items-center justify-center">
          <img :src="mepsLogo" alt="" class="h-8" />
        </div>
        <span class="font-bold">MEPS 이용약관</span>
      </div>
      <p class="text-[13px] text-text-sub mb-5">
        안전 진단 점수 및 근거 설명은 회원가입 후 확인하실 수 있습니다.
      </p>

      <!-- 모두 동의 -->
      <label
        class="flex items-center gap-2 py-2.5 text-[14px] font-semibold border-b border-surface-gray mb-2"
      >
        <input type="checkbox" v-model="agreeAll" />
        모두 확인, 동의합니다
      </label>

      <!-- 약관 체크박스들 -->
      <label class="flex items-center gap-2 py-2 text-[14px]">
        <input type="checkbox" v-model="agreeService" />
        <span class="flex-1">(필수) MEPS 서비스 이용약관 동의</span>
        <button @click.prevent="showTerms('service')" class="text-text-sub">
          <ChevronRight :size="16" />
        </button>
      </label>
      <label class="flex items-center gap-2 py-2 text-[14px]">
        <input type="checkbox" v-model="agreeLocation" />
        <span class="flex-1">(필수) 위치기반 서비스 이용약관 동의</span>
        <button @click.prevent="showTerms('location')" class="text-text-sub">
          <ChevronRight :size="16" />
        </button>
      </label>
      <label class="flex items-center gap-2 py-2 text-[14px]">
        <input type="checkbox" v-model="agreePrivacy" />
        <span class="flex-1">(필수) 개인정보처리방침 동의</span>
        <button @click.prevent="showTerms('privacy')" class="text-text-sub">
          <ChevronRight :size="16" />
        </button>
      </label>
      <label class="flex items-center gap-2 py-2 text-[14px]">
        <input type="checkbox" v-model="agreeAge" />
        <span class="flex-1">(필수) 만 14세 이상</span>
      </label>

      <p v-if="termsError" class="text-[13px] text-status-danger mt-2">
        {{ termsError }}
      </p>

      <button @click="goToForm" class="w-full py-3 mt-4 bg-primary text-white font-bold rounded-lg">
        다음
      </button>

      <!-- 로그인 링크 -->
      <p class="text-center text-[13px] text-text-sub mt-4">
        이미 계정이 있으신가요?
        <button @click="goLogin" class="text-primary font-medium underline">로그인</button>
      </p>
    </div>

    <!-- 2단계: 회원가입 폼 -->
    <div v-if="step === 'form'" class="relative bg-white rounded-2xl p-6 w-[380px]">
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
        class="w-full px-3 py-2.5 mt-1 mb-1 bg-surface-gray border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />
      <p class="text-[12px] text-text-sub mt-1 mb-3">8~20자, 영문·숫자·특수문자 포함</p>

      <!-- 비밀번호 확인 -->
      <label class="text-[13px] font-medium">비밀번호 확인</label>
      <input
        v-model="passwordConfirm"
        type="password"
        placeholder="비밀번호를 한번 더 입력하세요"
        class="w-full px-3 py-2.5 mt-1 mb-5 bg-surface-gray border border-surface-gray rounded-lg text-[14px] outline-none focus:border-primary"
      />

      <!-- 에러 메시지 -->
      <p v-if="errorMessage" class="text-[13px] text-status-danger mb-3">
        {{ errorMessage }}
      </p>

      <!-- 회원가입 버튼 -->
      <button @click="handleSignup" class="w-full py-3 bg-primary text-white font-bold rounded-lg">
        회원가입
      </button>

      <!-- 로그인 링크 -->
      <p class="text-center text-[13px] text-text-sub mt-4">
        이미 계정이 있으신가요?
        <button @click="goLogin" class="text-primary font-medium underline">로그인</button>
      </p>
    </div>
  </div>
</template>
