<script setup>
import { ref } from 'vue';
import { Sparkles, Info, LandPlot, Building2, Layers } from '@lucide/vue';

const sections = [
  { key: 'report', label: 'AI 안전진단', icon: Sparkles },
  { key: 'basic', label: '기본 정보', icon: Info },
  { key: 'land', label: '토지 정보', icon: LandPlot },
  { key: 'building', label: '건축물 정보', icon: Building2 },
  { key: 'floor', label: '층별 정보', icon: Layers },
];

const activeSections = ref(['report', 'basic', 'land', 'building', 'floor']);

function toggleSection(key) {
  const index = activeSections.value.indexOf(key);
  if (index !== -1) {
    if (activeSections.value.length === 1) return;
    activeSections.value.splice(index, 1);
  } else {
    activeSections.value.push(key);
  }
}

function isActive(key) {
  return activeSections.value.includes(key);
}
</script>

<template>
  <section class="flex-1 min-w-0">
    <div class="flex flex-wrap gap-2 mb-4">
      <button
        v-for="section in sections"
        :key="section.key"
        @click="toggleSection(section.key)"
        class="flex items-center gap-1.5 px-3 py-2 rounded-full text-[13px] font-medium border transition-colors"
        :class="
          isActive(section.key)
            ? 'bg-primary text-white border-primary'
            : 'bg-surface-gray text-text-sub border-surface-gray'
        "
      >
        <component :is="section.icon" :size="15" />
        {{ section.label }}
      </button>
    </div>
  </section>
</template>
