<template>
  <div class="flex-1 flex flex-col gap-5 px-1 pb-2 pt-5 animate-pulse">
    <!-- DetailedReportSummary 자리 -->
    <div class="flex flex-col gap-2 px-4">
      <div class="h-5 rounded bg-surface-gray w-1/3"></div>
      <div class="h-16 rounded-2xl bg-surface-gray"></div>
    </div>

    <!-- DiagnosticFactorList(detail 모드) 4개 항목 자리 -->
    <div class="flex flex-col gap-2 px-4">
      <div v-for="n in 4" :key="n" class="rounded-xl bg-surface-gray h-28"></div>
    </div>

    <!-- Disclaimer 자리 -->
    <div class="px-4">
      <div class="h-10 rounded-lg bg-surface-gray"></div>
    </div>
  </div>
</template>
