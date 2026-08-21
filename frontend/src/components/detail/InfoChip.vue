<template>
  <div v-if="label && value" class="flex-1 rounded-xl bg-neutral-50 px-3.5 py-2.5">
    <p className="text-[12px] text-neutral-400">{{ label }}</p>
    <p className="mt-0.5 text-[16px] text-neutral-900">{{ value }}</p>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String },
  value: { type: [String, Number], default: '-' },
});
</script>
