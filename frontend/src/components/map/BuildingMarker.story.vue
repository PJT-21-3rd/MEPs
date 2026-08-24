<template>
  <Story title="지도/BuildingMarker">
    <!-- 1. 기본 상태 -->
    <Variant title="1. 기본 마커">
      <div class="p-12 bg-surface-base flex items-center justify-center">
        <BuildingMarker purpose="제2종근린생활시설" area="3,840㎡" />
      </div>
    </Variant>

    <!-- 2. 찜한 곳 상태 (is-saved) -->
    <Variant title="2. 찜한 상가 (isSaved)">
      <div class="p-12 bg-surface-base flex items-center justify-center">
        <BuildingMarker purpose="일반음식점" area="1,200㎡" :is-saved="true" />
      </div>
    </Variant>

    <!-- 3. 최근 본 곳 상태 (is-recent) -->
    <Variant title="3. 최근 본 상가 (isRecent)">
      <div class="p-12 bg-surface-base flex items-center justify-center">
        <BuildingMarker purpose="판매시설" area="5,420㎡" :is-recent="true" />
      </div>
    </Variant>

    <!-- 4. 찜 + 최근 동시 적용 (우선순위 테스트) -->
    <Variant title="4. 찜 & 최근 (우선순위 테스트)">
      <div class="p-12 bg-surface-base flex items-center justify-center">
        <BuildingMarker purpose="숙박시설" area="890㎡" :is-saved="true" :is-recent="true" />
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import BuildingMarker from './BuildingMarker.vue';
</script>
