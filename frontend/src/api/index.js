// 공통
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';
import { refreshApi } from '@/api/auth';

const api = axios.create({
  baseURL: '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: JWT 토큰 자동 첨부
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    const status = error.response?.status;
    const url = error.config?.url || '';

    // 로그인/회원가입 요청의 401은 제외(ex)비번오류)
    const isAuthRequest =
      url.includes('/login') || url.includes('/signup') || url.includes('/refresh');

    if (status === 401 && !isAuthRequest && !originalRequest._retry) {
      originalRequest._retry = true; // 무한 루프 방지

      try {
        const { accessToken } = await refreshApi(); // 재발급
        localStorage.setItem('accessToken', accessToken); // 저장
        originalRequest.headers.Authorization = `Bearer ${accessToken}`; // 새 토큰
        return api(originalRequest); // 원래 요청 재시도
      } catch (refreshError) {
        // 재발급 실패 (refreshToken도 만료 등) → 로그아웃
        console.warn('토큰 재발급 실패, 로그아웃합니다.');
        const authStore = useAuthStore();
        authStore.logout();
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  },
);

export default api;
