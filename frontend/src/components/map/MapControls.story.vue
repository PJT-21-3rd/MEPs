<template>
  <Story title="MapControls" group="map">
    <Variant title="기본 (Default)">
      <div
        class="relative w-full h-[600px] bg-neutral-200 overflow-hidden rounded-xl border border-neutral-300"
      >
        <div
          class="absolute inset-0 flex items-center justify-center text-neutral-400 font-bold text-2xl"
        >
          가상의 지도 영역
        </div>

        <MapControls />
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import MapControls from './MapControls.vue';
</script>
