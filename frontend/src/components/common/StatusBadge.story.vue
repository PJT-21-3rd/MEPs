<script setup>
import StatusBadge from '@/components/common/StatusBadge.vue';
</script>

<template>
  <Story title="공통 컴포넌트/StatusBadge" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전 (md)">
      <StatusBadge status="safe" />
    </Variant>

    <Variant title="양호 (md)">
      <StatusBadge status="good" />
    </Variant>

    <Variant title="주의 (md)">
      <StatusBadge status="warning" />
    </Variant>

    <Variant title="위험 (md)">
      <StatusBadge status="danger" />
    </Variant>
  </Story>
</template>
