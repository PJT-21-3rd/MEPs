<template>
  <Story title="MapControlButton" group="map">
    <!-- 기본 -->
    <Variant title="기본 (Default)">
      <div class="flex items-center justify-center p-10 bg-surface-gray/30">
        <MapControlButton title="내 위치">
          <Navigation size="18px" />
        </MapControlButton>
      </div>
    </Variant>

    <!-- 활성화 상태 -->
    <Variant title="활성화 (Active)">
      <div class="flex items-center justify-center p-10 bg-surface-gray/30">
        <MapControlButton title="내 위치" active>
          <Navigation size="18px" />
        </MapControlButton>
      </div>
    </Variant>

    <!-- 텍스트 들어간 버튼 -->
    <Variant title="텍스트 포함 (Text)">
      <div class="flex items-center justify-center p-10 bg-surface-gray/30">
        <MapControlButton title="지적편집도" active>
          <span class="text-[10px] leading-tight text-center">지적<br />편집도</span>
        </MapControlButton>
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import { Navigation } from '@lucide/vue';
import MapControlButton from './MapControlButton.vue';
</script>
