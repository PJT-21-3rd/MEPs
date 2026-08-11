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
