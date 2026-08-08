import axios from 'axios';

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
    // 예: 토큰이 만료되어 401 에러
    if (error.response && error.response.status === 401) {
      console.warn('토큰이 만료되었습니다. 다시 로그인해주세요.');
    }
    return Promise.reject(error);
  },
);

export default api;
