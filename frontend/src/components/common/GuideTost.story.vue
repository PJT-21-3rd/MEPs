<script setup>
import GuideToast from './GuideToast.vue';
</script>

<template>
  <Story title="공통/GuideToast">
    <Variant title="기본">
      <div class="p-8 bg-surface-gray">
        <GuideToast @close="() => {}" />
      </div>
    </Variant>
  </Story>
</template>
