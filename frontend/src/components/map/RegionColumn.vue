<template>
  <div class="h-[240px] overflow-y-auto px-2" :class="{ 'border-l border-surface-gray': divider }">
    <button
      v-for="item in items"
      :key="item"
      @click="$emit('pick', item)"
      class="mb-1 flex w-full items-center rounded-lg px-3 py-2.5 text-left text-[13px] font-semibold tracking-tight transition-colors"
      :class="
        item === selected
          ? 'bg-surface-blue text-primary font-bold'
          : 'text-text-secondary hover:bg-surface-base'
      "
    >
      {{ item }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  items: { type: Array, required: true },
  selected: { type: String, default: '' },
  divider: { type: Boolean, default: false },
});

defineEmits(['pick']);
</script>
