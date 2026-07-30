<template>
  <div class="rounded-xl bg-white/70 px-3 py-2">
    <div class="flex items-center gap-1.5 text-neutral-400">
      <component v-if="icon" :is="icon" :size="14" />
      <span class="text-[12px]">{{ label }}</span>
    </div>
    <div class="mt-1 flex items-baseline gap-1">
      <span class="text-[15px] tracking-tight text-neutral-900">{{ value }}</span>
      <span v-if="sub" class="text-[12px] text-neutral-400">{{ sub }}</span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon: {
    type: [Object, Function],
    required: false,
  },
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  sub: {
    type: String,
    default: '',
  },
});
</script>
