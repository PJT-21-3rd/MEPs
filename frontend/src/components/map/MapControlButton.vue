<template>
  <button
    :title="title"
    :class="[
      'flex h-10 w-10 items-center justify-center rounded-xl  shadow-md ring-1 ring-black/5 transition-colors',
      active ? 'bg-primary text-white font-bold' : 'bg-white text-text-main hover:bg-neutral-50',
    ]"
  >
    <slot />
  </button>
</template>

<script setup>
defineProps({
  active: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '',
  },
});
</script>
