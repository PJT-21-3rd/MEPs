<script setup>
import OfflineAgentCard from './OfflineAgentCard.vue';

function handleConsult() {
  console.log('상담받고 싶어요 클릭됨');
}
</script>

<template>
  <Story title="AI 리포트 컴포넌트/OfflineAgentCard" :layout="{ type: 'single', iframe: false }">
    <Variant title="기본">
      <div style="width: 600px; height: 400px; background: #e5e7eb" class="relative p-6">
        <OfflineAgentCard
          class="absolute bottom-6 left-6"
          dong-name="역삼동"
          :agent="{
            name: '김민준',
            company: 'KB부동산개발법인(주)',
            phone: '010-1234-5678',
          }"
          @consult="handleConsult"
        />
      </div>
    </Variant>
  </Story>
</template>
