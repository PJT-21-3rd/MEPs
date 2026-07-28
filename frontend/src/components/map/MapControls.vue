<template>
  <div class="absolute bottom-10 right-6 z-20 flex flex-col gap-2">
    <button
      @click="handleMyLocation"
      class="flex h-10 w-10 items-center justify-center rounded-xl bg-white shadow-md ring-1 ring-black/5"
    >
      <Navigation size="18px" />
    </button>

    <div className="overflow-hidden rounded-xl bg-white shadow-md ring-1 ring-black/5">
      <button
        @click="handleZoomIn"
        className="flex h-10 w-10 items-center justify-center hover:bg-neutral-50"
      >
        <Plus size="18px" />
      </button>
      <div className="mx-auto h-px w-6 bg-neutral-200" />
      <button
        @click="handleZoomOut"
        className="flex h-10 w-10 items-center justify-center hover:bg-neutral-50"
      >
        <Minus size="18px" />
      </button>
    </div>

    <button
      @click="handleLayer"
      class="flex h-10 w-10 items-center justify-center rounded-xl bg-white shadow-md ring-1 ring-black/5"
    >
      <Layers size="18px" />
    </button>
    <button
      @click="handleRoadView"
      class="flex h-10 w-10 items-center justify-center rounded-xl bg-white shadow-md ring-1 ring-black/5"
    >
      <Webcam size="18px" />
    </button>
  </div>
</template>

<script setup>
import { Layers, Minus, Navigation, Plus, Webcam } from '@lucide/vue';

const handleMyLocation = () => {
  console.log('내 위치로 이동');
};

const handleZoomIn = () => {
  console.log('지도 확대');
};

const handleZoomOut = () => {
  console.log('지도 축소');
};

const handleLayer = () => {
  console.log('지도 레이어');
};

const handleRoadView = () => {
  console.log('로드뷰');
};
</script>
