<template>
  <div class="py-4">
    <a
      href="https://docs.google.com/forms/d/e/1FAIpQLSe2GekcfspJoQOoqOcFNWlnicIWpblZ1so4LVelJuQ62X2-yA/viewform?usp=publish-editor"
      target="_blank"
      rel="noopener noreferrer"
      class="group relative flex w-full items-center justify-between overflow-hidden rounded-2xl bg-gradient-to-br from-surface-blue to-[#E8F3FF] p-4 text-left transition-all hover:shadow-md"
    >
      <MessageSquarePlus
        :size="64"
        class="absolute -right-2 -bottom-3 text-white/40 transition-transform group-hover:scale-110 group-hover:-rotate-6"
      />

      <div class="relative z-10 flex flex-col gap-1">
        <div class="flex items-center gap-1.5">
          <span
            class="rounded bg-button-primary/10 px-1.5 py-0.5 text-[10px] font-bold text-button-primary"
          >
            의견 남기기
          </span>
        </div>
        <h3 class="text-[14px] font-bold text-text-main mt-0.5">MEPS에게 바라는 점이 있나요?</h3>
        <p class="text-[12px] text-text-secondary">서비스 개선 사항이나 오류를 알려주세요.</p>
      </div>

      <div
        class="relative z-10 flex h-8 w-8 items-center justify-center rounded-full bg-white shadow-sm transition-transform group-hover:translate-x-1"
      >
        <ChevronRight :size="16" class="text-text-main" />
      </div>
    </a>
  </div>
</template>

<script setup>
import { MessageSquarePlus, ChevronRight } from '@lucide/vue';
</script>
