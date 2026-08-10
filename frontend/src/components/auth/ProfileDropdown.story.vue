<script setup>
import ProfileDropdown from './ProfileDropdown.vue';
</script>

<template>
  <Story title="인증/ProfileDropdown">
    <Variant title="기본">
      <div class="p-8 bg-surface-gray flex justify-end">
        <ProfileDropdown @logout="() => {}" @delete-account="() => {}" />
      </div>
    </Variant>
  </Story>
</template>
