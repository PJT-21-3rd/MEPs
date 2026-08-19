import http from '.';

// 회원가입 API (POST/api/users/signup)
export async function signup({ email, password, passwordConfirm }) {
  const { data } = await http.post('/api/users/signup', {
    email,
    password,
    passwordConfirm,
  });
  return data;
}

// 로그인
export async function login({ email, password }) {
  const { data } = await http.post('/api/users/login', { email, password });
  return data;
}

// api/auth.js
export async function refreshApi() {
  try {
    const refreshToken = localStorage.getItem('refreshToken');
    const { data } = await http.post('/api/users/refresh', { refreshToken });
    return data;
  } catch (error) {
    console.error('토큰 재발급 실패:', error);
    throw error;
  }
}

// 계정 삭제
export async function deleteAccount() {
  const { data } = await http.delete('/api/users/me');
  return data;
}
