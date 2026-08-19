<template>
  <div class="flex flex-col flex-1 overflow-hidden bg-white h-full w-[400px]">
    <div class="flex-1 overflow-y-auto">
      <!-- 로드뷰 -->
      <div class="px-4">
        <div class="h-[180px] w-full rounded-2xl bg-surface-gray animate-pulse"></div>
      </div>
      <!-- 주소이름 -->
      <div class="px-5 pt-5">
        <div class="mt-1 h-6 w-2/3 rounded-md bg-surface-gray animate-pulse"></div>
        <div class="mt-1.5 space-y-1">
          <div class="flex items-center gap-1.5">
            <div class="h-5 w-8 px-1.5 py-0.5 rounded bg-surface-gray animate-pulse"></div>
            <div class="h-5 w-3/4 rounded bg-surface-gray animate-pulse"></div>
          </div>
          <div class="flex items-center gap-1.5">
            <div class="h-4 w-10 px-1.5 py-0.5 rounded bg-surface-gray animate-pulse"></div>
            <div class="h-4 w-1/2 rounded bg-surface-gray animate-pulse"></div>
          </div>
        </div>
      </div>
      <!-- 칩 -->
      <div class="px-5 mt-4">
        <div class="flex gap-2">
          <div class="h-[55px] flex-1 rounded-xl bg-surface-gray animate-pulse"></div>
          <div class="h-[55px] flex-1 rounded-xl bg-surface-gray animate-pulse"></div>
        </div>
        <div class="flex justify-end mt-1.5">
          <div class="h-3 w-16 rounded bg-surface-gray animate-pulse"></div>
        </div>
      </div>
      <!-- 토지건물상세정보 -->
      <div class="px-5 mt-6">
        <div class="flex gap-4 border-b border-surface-base pb-2 mb-4">
          <div class="h-6 w-15 rounded bg-surface-gray animate-pulse"></div>
          <div class="h-6 w-15 rounded bg-surface-gray animate-pulse"></div>
          <div class="h-6 w-15 rounded bg-surface-gray animate-pulse"></div>
        </div>
        <div class="space-y-4">
          <div v-for="i in 5" :key="i" class="flex justify-between items-center py-1">
            <div class="h-4 w-16 rounded bg-surface-gray animate-pulse"></div>
            <div class="h-4 w-24 rounded bg-surface-gray animate-pulse"></div>
          </div>
        </div>
      </div>
    </div>
    <!-- 버튼 -->
    <div class="shrink-0 bg-white px-5 py-4 pb-6">
      <div class="h-[52px] w-full rounded-xl bg-surface-gray animate-pulse"></div>
    </div>
  </div>
</template>
