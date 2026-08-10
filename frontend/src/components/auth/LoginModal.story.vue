<script setup>
import LoginModal from './LoginModal.vue';
</script>

<template>
  <Story title="인증/LoginModal" :layout="{ type: 'single', iframe: false }">
    <Variant title="기본">
      <LoginModal @close="() => {}" />
    </Variant>
  </Story>
</template>
