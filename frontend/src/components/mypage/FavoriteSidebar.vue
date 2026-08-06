<script setup>
import { computed } from 'vue';
import { ArrowLeft, Scale, FolderHeart } from '@lucide/vue';
import FavoriteCard from '@/components/mypage/FavoriteCard.vue';

const props = defineProps({
  buildings: Array,
  selectedIds: Array,
});

const emit = defineEmits(['toggle']);

const guideText = computed(() => {
  const count = props.selectedIds.length;
  if (count === 0) return '비교할 매물을 선택하세요';
  if (count === 1) return '하나 더 선택해주세요';
  return '비교할 매물이 선택되었어요';
});

function selectOrder(id) {
  const index = props.selectedIds.indexOf(id);
  return index === -1 ? null : index + 1;
}
</script>

<template>
  <aside class="w-[330px] shrink-0 flex flex-col h-full pt-4 px-4">
    <div class="shrink-0">
      <header class="flex items-start gap-2 mb-4">
        <ArrowLeft :size="22" class="mt-0.5" />
        <div>
          <h1 class="flex items-center gap-1 text-lg font-bold m-0">
            마이페이지
            <FolderHeart :size="17" />
          </h1>
          <p class="text-[13px] text-text-sub mt-0.5">찜한 매물 {{ buildings.length }}개</p>
        </div>
      </header>

      <p class="flex items-center gap-2 text-[15px] text-primary mt-8 mb-2">
        <Scale :size="18" />
        {{ guideText }}
      </p>
    </div>

    <ul class="list-none p-0 m-0 flex flex-col gap-2.5 flex-1 overflow-y-auto">
      <FavoriteCard
        v-for="building in buildings"
        :key="building.id"
        :building="building"
        :order="selectOrder(building.id)"
        @toggle="emit('toggle', building.id)"
      />
    </ul>
  </aside>
</template>
