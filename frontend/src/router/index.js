import { createRouter, createWebHistory } from 'vue-router'
import MapView from '@/views/MapView.vue'

const routes = [
  {
    path: '/',
    name: 'Map',
    component: MapView,
  },
  {
    path: '/mypage',
    name: 'mypage',
    component: () => import('../views/MyPage.vue'),
  },
  // {
  //   path: '/login',
  //   name: 'Login',
  //   component: () => import('@/views/LoginView.vue'),
  // },
  // {
  //   path: '/mypage',
  //   name: 'MyPage',
  //   component: () => import('@/views/MyPageView.vue'),
  //   meta: { requiresAuth: true }, // 로그인 가드
  // },
  // {
  //   path: '/:pathMatch(.*)*',
  //   name: 'NotFound',
  //   component: () => import('@/views/NotFoundView.vue'),
  // },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

// 로그인 가드 (Protected Route)
// router.beforeEach((to, from, next) => {
//   const isAuthenticated = !!localStorage.getItem('accessToken')
//   if (to.meta.requiresAuth && !isAuthenticated) {
//     alert('로그인이 필요한 서비스입니다.')
//     next({ name: 'Login' })
//   } else {
//     next()
//   }
// })

export default router
