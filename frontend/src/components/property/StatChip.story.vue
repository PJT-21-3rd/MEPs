<!-- src/components/property/StatChip.story.vue -->
<template>
  <Story title="StatChip" group="property">
    <Variant title="기본 상태 (Default)">
      <div class="p-5 bg-surface-gray/30 w-[200px]">
        <StatChip
          label="일 유동인구"
          :icon="ChartNetwork"
          :value="formatPopulation(125000)"
          :sub="formatRate(10.24)"
        />
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import { ChartNetwork, Users, Store, Building, Droplets } from '@lucide/vue';
import StatChip from './StatChip.vue';
import { formatPopulation, formatRate } from '@/utils/formatters.js';
</script>
