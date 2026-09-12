<script setup>
import DetailedReportDisclaimer from './DetailedReportDisclaimer.vue';
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/DetailedReportDisclaimer"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="기본">
      <div style="width: 400px" class="p-4 bg-white">
        <DetailedReportDisclaimer />
      </div>
    </Variant>
  </Story>
</template>
