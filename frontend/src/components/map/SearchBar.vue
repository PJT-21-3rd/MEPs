<script setup>
import { Clock, Search, X } from '@lucide/vue';
import { useUiStore } from '@/stores/uiStore';
import { onMounted, onUnmounted, ref } from 'vue';
import { useClickOutside } from '@/hooks/useClickOutside';

const uiStore = useUiStore();

// dropdwon 관련
const isDropdownOpen = ref(false);
const searchContainerRef = ref(null);
const recentSearches = ref([]); // 최근검색어s

// 로컬에서 불러오기
const loadRecentSearches = () => {
  const saved = localStorage.getItem('meps-recent-searches');
  if (saved) {
    recentSearches.value = JSON.parse(saved);
  }
};

const saveRecentSearches = () => {
  localStorage.setItem('meps-recent-searches', JSON.stringify(recentSearches.value));
};

// 검색
const handleSearch = (q) => {
  const keyword = q?.trim();
  if (!keyword) return;

  uiStore.searchQuery = keyword;

  recentSearches.value = recentSearches.value.filter((item) => item !== keyword); // 중복제거
  recentSearches.value.unshift(keyword);

  if (recentSearches.value.length > 5) {
    recentSearches.value.pop();
  }

  saveRecentSearches();

  isDropdownOpen.value = false;
  uiStore.isDetailOpen = true;
};

const removeRecentSearch = (keyword) => {
  recentSearches.value = recentSearches.value.filter((item) => item !== keyword);
  saveRecentSearches();
};

const clearAllRecentSearches = () => {
  recentSearches.value = [];
  localStorage.removeItem('meps-recent-searches');
};

const clearSearch = () => {
  uiStore.searchQuery = '';
};

// 외부 클릭 감지(드롭다운 닫기)
useClickOutside(searchContainerRef, () => {
  isDropdownOpen.value = false;
});
</script>

<template>
  <div class="relative" ref="searchContainerRef">
    <Search class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-sub" />
    <input
      type="text"
      v-model="uiStore.searchQuery"
      @keyup.enter="handleSearch(uiStore.searchQuery)"
      @focus="isDropdownOpen = true"
      placeholder="주소 또는 건물명 검색"
      class="w-full h-14 rounded-2xl border-0 bg-neutral-100 pl-12 pr-11 text-[16px] placeholder:text-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-secondary/40"
      :class="{ 'bg-white': uiStore.isDetailOpen }"
    />
    <button
      v-if="uiStore.searchQuery"
      @click="clearSearch"
      class="absolute right-3 top-1/2 -translate-y-1/2 p-1 bg-neutral-300/70 rounded-full text-white hover:bg-neutral-400"
    >
      <X size="14px" />
    </button>

    <!-- 최근 검색 드롭다운 -->
    <div
      v-if="isDropdownOpen"
      class="absolute left-0 right-0 top-full z-40 mt-2 overflow-hidden rounded-2xl border border-neutral-100 bg-white shadow-xl"
    >
      <div class="flex items-center justify-between px-4 pb-1.5 pt-3">
        <p class="text-text-sub text-[12px]">최근 검색</p>
      </div>
      <ul v-if="recentSearches.length > 0" class="pb-1">
        <li v-for="(item, index) in recentSearches" :key="index" @click="handleSearch(item)">
          <div class="group flex items-center gap-2.5 px-4 py-2.5 hover:bg-neutral-50">
            <Clock class="shrink-0 text-text-sub" size="16px" />
            <span class="min-w-0 flex-1 truncate text-left text-[15px] text-text-main">
              {{ item }}
            </span>

            <button
              @click.stop="removeRecentSearch(item)"
              class="rounded-full p-1 text-neutral-300 hover:bg-neutral-200 hover:text-neutral-500"
            >
              <X size="14px" />
            </button>
          </div>
        </li>
      </ul>
      <div v-else class="flex items-center gap-2.5 px-4 py-2.5">
        <p class="min-w-0 flex-1 truncate text-center text-[15px] text-text-sub font-semibold">
          최근 검색 내역이 없습니다.
        </p>
      </div>
      <div class="flex items-center justify-between border-t border-neutral-100 px-4 py-2.5">
        <button
          @click="clearAllRecentSearches"
          class="text-[13px] text-text-sub hover:text-text-main"
        >
          전체삭제
        </button>
        <button
          @click="isDropdownOpen = false"
          class="text-[13px] text-neutral-500 hover:text-neutral-800"
        >
          닫기
        </button>
      </div>
    </div>
  </div>
</template>
