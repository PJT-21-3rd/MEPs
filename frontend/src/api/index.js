// 공통
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

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
  (error) => {
    const status = error.response?.status;
    const url = error.config?.url || '';

    // 로그인/회원가입 요청의 401은 제외(ex)비번오류)
    const isAuthRequest = url.includes('/login') || url.includes('/signup');

    if (status === 401 && !isAuthRequest) {
      console.warn('토큰이 만료되었습니다. 다시 로그인해주세요.');
      const authStore = useAuthStore();
      authStore.logout();
    }
    return Promise.reject(error);
  },
);

export default api;
