<script setup>
import { ref, watch, onUnmounted } from 'vue';

const props = defineProps({
  text: { type: String, required: true },
  active: { type: Boolean, default: false },
  speed: { type: Number, default: 6 }, // 글자당 ms
});

const emit = defineEmits(['done']);

const displayed = ref('');
let timer = null;

function startTyping() {
  clearInterval(timer);
  displayed.value = '';
  let i = 0;
  timer = setInterval(() => {
    displayed.value += props.text[i];
    i += 1;
    if (i >= props.text.length) {
      clearInterval(timer);
      emit('done');
    }
  }, props.speed);
}

watch(
  () => props.active,
  (isActive) => {
    if (isActive) startTyping();
  },
  { immediate: true },
);

onUnmounted(() => clearInterval(timer));
</script>

<template>
  <span>{{ displayed }}</span>
</template>
