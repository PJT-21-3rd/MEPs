<script setup>
import { X, Landmark, MapPin, Smartphone, ChevronRight } from '@lucide/vue';

// 12번 - 대출 연결 모달
defineProps({
  products: {
    type: Array,
    required: true,
    // { category: String, name: String, rateText: String, description: String }
  },
});

defineEmits(['close', 'view-detail', 'find-branch', 'open-app']);
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
            <Landmark class="w-4.5 h-4.5 text-secondary" />
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
              소상공인 사장님을 위한
            </p>
            <p class="text-[24px] font-medium text-text-main leading-tight">
              <span class="text-secondary">MEPS &times; KB국민은행</span> 맞춤 대출 가이드
            </p>
          </div>

          <div class="flex flex-col gap-3">
            <div
              v-for="product in products"
              :key="product.name"
              class="p-4 rounded-[16px] border border-surface-gray flex flex-col gap-2"
            >
              <div class="flex items-start justify-between gap-2">
                <span
                  class="px-2 py-0.5 rounded-full text-[12px] font-regular bg-badge-loan text-text-loan shrink-0"
                >
                  {{ product.category }}
                </span>
                <span class="text-right text-[20px] font-regular text-primary shrink-0">
                  {{ product.rateText }}
                </span>
              </div>
              <p class="text-[16px] font-regular text-text-main">{{ product.name }}</p>
              <p class="text-[14px] text-text-modal">{{ product.description }}</p>
              <button
                type="button"
                class="flex items-center gap-0.5 text-[14px] font-medium text-primary"
                @click="$emit('view-detail', product)"
              >
                상세 보기
                <ChevronRight class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>

          <!-- 영업점 연계 가이드 -->
          <div class="p-4 rounded-[16px] bg-surface-gray flex flex-col gap-3">
            <div>
              <p class="text-[15px] font-regular text-text-main">영업점 연계 가이드</p>
              <p class="text-[13px] text-text-modal">
                비대면 또는 인근 영업점 방문으로 바로 상담을 신청할 수 있어요.
              </p>
            </div>
            <div class="flex gap-2">
              <button
                type="button"
                class="flex-1 py-2.5 rounded-[14px] bg-white border border-surface-gray text-[14px] font-medium text-text-main flex items-center justify-center gap-1"
                @click="$emit('find-branch')"
              >
                <MapPin class="w-3.5 h-3.5 shrink-0" />
                인근 KB국민은행 영업점 찾기
              </button>
              <button
                type="button"
                class="flex-1 py-2.5 rounded-[14px] bg-secondary text-[14px] font-medium text-primary flex items-center justify-center gap-1"
                @click="$emit('open-app')"
              >
                <Smartphone class="w-3.5 h-3.5 shrink-0" />
                KB스타뱅킹 앱으로 신청하기
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>
