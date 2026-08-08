import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueDevTools from 'vite-plugin-vue-devtools';
import tailwindcss from '@tailwindcss/vite';
import { HstVue } from '@histoire/plugin-vue';

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools(), tailwindcss()],
  server: {
    proxy: {
      // '/api-hub'로 시작하는 요청을 Ncloud API HUB로 우회
      '/api-hub': {
        target: 'https://naverapihub.apigw.ntruss.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api-hub/, ''),
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  histoire: {
    plugins: [HstVue()],
    setupFile: 'src/histoire.setup.js',
    tree: {
      groups: [
        { id: 'top', title: '' },
        { id: 'common', title: '공통 컴포넌트' },
        { id: 'map', title: '지도/탐색 컴포넌트' },
        { id: 'report', title: 'AI 리포트 컴포넌트' },
        { id: 'property', title: '상가 관련 컴포넌트' },
      ],
    },
  },
});
