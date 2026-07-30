<script setup>
import ScoreGauge from './ScoreGauge.vue'
</script>

<template>
  <Story title="AI 리포트 컴포넌트/ScoreGauge" :layout="{ type: 'single', iframe: false }">
    <Variant title="안전 등급 (95점)">
      <ScoreGauge :score="95" />
    </Variant>

    <Variant title="양호 등급 (85점)">
      <ScoreGauge :score="85" />
    </Variant>

    <Variant title="주의 등급 (72점)">
      <ScoreGauge :score="72" />
    </Variant>
  </Story>
</template>
