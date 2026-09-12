<template>
  <div class="flex items-center justify-between border-b border-surface-base py-2.5">
    <span class="text-[14px] text-text-sub">{{ label }}</span>
    <span class="text-[15px]">{{ value }}</span>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String },
  value: { type: [String, Number], default: '-' },
});
</script>
