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
