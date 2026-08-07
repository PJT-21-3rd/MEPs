<script setup>
import { X, Check, ShieldCheck } from '@lucide/vue';

// 11번 - 보험 연결 모달 (풍수해/사업장종합 재사용)
defineProps({
  // 문구 틀은 고정("이 상가의 취약점을 보완하는 / 맞춤형 [highlight] 추천")
  // highlight만 상황에 맞게 교체: 'KB손해보험' | '풍수해보험'
  highlight: {
    type: String,
    required: true,
  },
  subtitle: {
    type: String,
    default: '',
  },
  items: {
    type: Array,
    required: true,
    // { name: String, description: String, badge?: '필수' }
  },
  ctaText: {
    type: String,
    required: true,
  },
});

defineEmits(['close', 'submit']);
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 z-40 flex items-center justify-center"
      style="background-color: rgba(245, 245, 245, 0.95)"
      @click.self="$emit('close')"
    >
      <div
        class="w-[760px] h-[944px] max-h-[90vh] bg-white rounded-[24px] overflow-hidden flex flex-col"
      >
        <!-- 헤더 -->
        <div class="bg-secondary px-8 py-5 flex items-center gap-2.5">
          <span class="w-8 h-8 rounded-[10px] bg-primary flex items-center justify-center shrink-0">
            <ShieldCheck class="w-4.5 h-4.5 text-secondary" />
          </span>
          <span class="flex-1 text-[15px] font-regular text-primary">MEPS &times; KB금융그룹</span>
          <button type="button" aria-label="닫기" @click="$emit('close')">
            <X class="w-5 h-5 text-primary" />
          </button>
        </div>

        <!-- 본문 -->
        <div class="flex-1 overflow-y-auto p-5 flex flex-col gap-4">
          <div class="flex flex-col gap-1">
            <p class="text-[24px] font-medium text-text-main leading-tight">
              이 상가의 취약점을 보완하는
            </p>
            <p class="text-[24px] font-medium text-text-main leading-tight">
              맞춤형 <span class="text-secondary">{{ highlight }}</span> 추천
            </p>
            <p v-if="subtitle" class="text-[15px] font-regular text-text-modal">{{ subtitle }}</p>
          </div>

          <div class="flex flex-col gap-3">
            <div
              v-for="item in items"
              :key="item.name"
              class="p-4 rounded-[16px] border border-surface-gray flex items-start gap-3"
            >
              <span
                class="w-8 h-8 rounded-full bg-badge-check-light flex items-center justify-center shrink-0"
              >
                <Check class="w-4 h-4 text-badge-check" />
              </span>
              <div class="flex-1 flex flex-col gap-0.5">
                <div class="flex items-center gap-2">
                  <span class="text-[16px] font-regular text-text-main">{{ item.name }}</span>
                  <span
                    v-if="item.badge"
                    class="px-2 py-0.5 rounded-full text-[12px] font-regular bg-status-danger text-white"
                  >
                    {{ item.badge }}
                  </span>
                </div>
                <p class="text-[14px] text-text-modal">{{ item.description }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- CTA -->
        <div class="p-4 border-t border-surface-gray">
          <button
            type="button"
            class="w-full py-3.5 rounded-xl bg-secondary text-primary text-sm font-bold flex items-center justify-center gap-1.5"
            @click="$emit('submit')"
          >
            <ShieldCheck class="w-4 h-4 shrink-0" />
            {{ ctaText }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>
