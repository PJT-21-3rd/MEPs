<script setup>
import { computed } from 'vue';
import { ArrowLeft, Scale, FolderHeart, Heart } from '@lucide/vue';
import FavoriteCard from '@/components/mypage/FavoriteCard.vue';
import { useRouter } from 'vue-router';
import SidebarHeader from '../layout/SidebarHeader.vue';

const props = defineProps({
  buildings: Array,
  selectedIds: Array,
});

const emit = defineEmits(['toggle', 'unlike', 'diagnosed']);

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

const router = useRouter();

function goBack() {
  if (window.history.state && window.history.state.back) {
    router.back();
  } else {
    router.push('/');
  }
}
</script>

<template>
  <aside class="w-[350px] shrink-0 flex flex-col h-full">
    <SidebarHeader />
    <div class="px-4">
      <header class="flex items-center gap-3 pb-4 pt-2">
        <button
          class="flex items-center gap-1 rounded-full px-2 py-1.5 text-[15px] text-text-main hover:bg-surface-base transition-colors"
          @click="goBack"
          aria-label="뒤로가기"
        >
          <ArrowLeft :size="20" />
        </button>
        <div>
          <p className="text-[19px] tracking-tight flex items-center">
            마이페이지&nbsp;<FolderHeart :size="17" />
          </p>
          <p className="text-[13px] text-text-sub">찜한 매물 {{ buildings.length }}개</p>
        </div>
      </header>

      <div
        v-if="buildings.length > 0"
        class="flex items-center gap-1.5 px-2 pb-3 text-[13px] text-primary"
      >
        <Scale :size="18" />
        <span>{{ guideText }}</span>
      </div>
    </div>

    <ul
      v-if="buildings.length > 0"
      class="list-none px-4 m-0 flex flex-col gap-2.5 flex-1 overflow-y-auto"
    >
      <FavoriteCard
        v-for="building in buildings"
        :key="building.buildingId"
        :building="building"
        :order="selectOrder(building.buildingId)"
        @toggle="emit('toggle', building.buildingId)"
        @unlike="emit('unlike', $event)"
        @diagnosed="emit('diagnosed', $event)"
      />
    </ul>
    <!-- 찜 목록 없으면 (빈 상태) -->
    <div v-else class="flex-1 flex flex-col items-center justify-center text-center px-6">
      <div class="w-16 h-16 rounded-full bg-surface-blue flex items-center justify-center mb-4">
        <Heart :size="30" class="text-primary" />
      </div>
      <p class="text-[15px] font-bold text-text-main mb-1.5">아직 찜한 매물이 없어요</p>
      <p class="text-[13px] text-text-sub mb-5 leading-relaxed">
        관심 있는 매물을 찜하고<br />한눈에 비교해보세요
      </p>
      <button
        @click="goBack"
        class="px-5 py-2 bg-primary text-white text-[13px] font-semibold rounded-full hover:bg-primary/90 transition-colors cursor-pointer"
      >
        매물 보러 가기
      </button>
    </div>
  </aside>
</template>
