<!-- src/components/property/StatChip.story.vue -->
<template>
  <Story title="StatChip" group="property">
    <Variant title="1. 기본 상태 (Default)">
      <div class="p-5 bg-surface-gray/30 w-[200px]">
        <StatChip label="주요 업종" value="음식점" sub="외 15종" subClass="text-text-sub">
          <template #icon><Store :size="14" /></template>
        </StatChip>
      </div>
    </Variant>

    <Variant title="2. 서브 텍스트 강조 (Primary/Orange)">
      <div class="p-5 bg-surface-gray/30 w-[200px] flex flex-col gap-3">
        <StatChip label="일 유동인구" value="12.5만명" sub="증가추세" subClass="text-primary">
          <template #icon><Users :size="14" /></template>
        </StatChip>

        <StatChip label="평균 노후도" value="24년" sub="양호" subClass="text-orange-500">
          <template #icon><Building :size="14" /></template>
        </StatChip>
      </div>
    </Variant>

    <Variant title="3. 위험/경고 테마 (Danger)">
      <div class="p-5 bg-surface-gray/30 w-[200px]">
        <StatChip
          label="침수 위험"
          value="주의필요"
          sub="침수 이력 2회"
          variant="danger"
          subClass="text-red-500"
        >
          <template #icon><Droplets :size="14" /></template>
        </StatChip>
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import { Users, Store, Building, Droplets } from '@lucide/vue';
import StatChip from './StatChip.vue';
</script>
