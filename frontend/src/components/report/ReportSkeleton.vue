<template>
  <div class="flex-1 flex flex-col gap-5 px-5 py-2 animate-pulse">
    <div class="flex flex-col gap-2">
      <div class="flex flex-col items-center gap-2 py-2">
        <div class="w-28 h-28 rounded-full bg-surface-gray"></div>
        <div class="w-20 h-4 rounded bg-surface-gray"></div>
      </div>
      <div class="h-16 rounded-2xl bg-surface-gray"></div>
    </div>

    <div class="flex flex-col gap-3">
      <div v-for="n in 4" :key="n" class="h-16 rounded-xl bg-surface-gray"></div>
    </div>

    <div class="h-14 rounded-2xl bg-surface-gray"></div>
    <div class="h-20 rounded-2xl bg-surface-gray"></div>
  </div>
</template>
