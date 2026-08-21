<template>
  <div class="flex flex-col gap-3">
    <div v-for="i in 1" :key="i" class="flex w-full gap-3.5 rounded-2xl p-3.5 bg-surface-base">
      <div class="h-[76px] w-[76px] shrink-0 rounded-xl bg-surface-gray animate-pulse" />

      <div class="min-w-0 flex-1 flex flex-col justify-around py-0.5">
        <div class="min-w-0">
          <div class="flex items-start justify-between gap-2">
            <div class="h-4 w-[70%] rounded bg-surface-gray animate-pulse mt-0.5" />
            <div class="h-6 w-6 shrink-0 rounded-full bg-surface-gray animate-pulse -mr-1 -mt-1" />
          </div>
          <div class="h-3 w-[40%] rounded bg-surface-gray animate-pulse mt-2" />
        </div>

        <div class="flex items-center gap-2.5 mt-2.5">
          <div class="h-3 w-10 rounded bg-surface-gray animate-pulse" />
          <div class="h-3 w-14 rounded bg-surface-gray animate-pulse" />
          <div class="h-3 w-12 rounded bg-surface-gray animate-pulse" />
        </div>
      </div>
    </div>
  </div>
</template>
