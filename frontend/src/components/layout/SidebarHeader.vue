<template>
  <header class="flex items-center justify-between p-6 pb-3">
    <div class="flex items-center gap-2">
      <div class="bg-surface-gray rounded-xl">
        <img src="@/assets/images/MEPS_LOGO.png" alt="meps_logo" class="w-10 h-10 object-contain" />
      </div>

      <h1 class="text-xl font-semibold">MEPS</h1>
    </div>

    <div class="flex items-center gap-1 text-text-sub">
      <button class="p-2.5 rounded-full hover:bg-surface-gray hover:text-text-main/70">
        <FolderHeart class="w-5.5 h-5.5" />
      </button>
      <button class="p-2.5 rounded-full hover:bg-surface-gray hover:text-text-main/70">
        <UserRound class="w-5.5 h-5.5" />
      </button>
    </div>
  </header>
</template>

<script setup>
import { FolderHeart, User, UserRound } from '@lucide/vue'

// 버튼
</script>
