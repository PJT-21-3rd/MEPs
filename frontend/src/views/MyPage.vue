<script setup>
import { favoriteBuildings } from '@/mocks/favorites'

function gradeClass(grade) {
  if (grade === '안전') return 'bg-status-safe text-text-safe'
  if (grade === '양호') return 'bg-status-good text-text-good'
  if (grade === '주의') return 'bg-status-warn text-text-warn'
  return ''
}
</script>

<template>
  <div class="mypage">
    <header class="page-header">
      <div class="title-row">
        <span class="back">‹</span>
        <h1>마이페이지</h1>
      </div>
      <p class="count">찜한 매물 {{ favoriteBuildings.length }}개</p>
    </header>

    <p class="guide">⊕ 비교할 매물 2개를 선택하세요</p>

    <ul class="card-list">
      <li v-for="building in favoriteBuildings" :key="building.id" class="card">
        <span class="select-circle"></span>

        <div class="card-info">
          <div class="card-title">
            <span class="name">{{ building.name }}</span>
            <span class="grade" :class="gradeClass(building.grade)">
              {{ building.grade }}
            </span>
          </div>
          <p class="address">{{ building.address }}</p>
        </div>

        <div class="card-right">
          <span class="score">{{ building.score }}</span>
          <span class="heart">♥</span>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.mypage {
  padding: 20px 16px;
  max-width: 380px;
}

.page-header {
  margin-bottom: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.back {
  font-size: 22px;
  color: var(--color-text-main);
}

.page-header h1 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.count {
  font-size: 13px;
  color: var(--color-text-sub);
  margin: 2px 0 0 26px;
}

.guide {
  font-size: 13px;
  color: var(--color-primary);
  margin: 0 0 12px;
}

.card-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--color-surface-gray);
  border-radius: 12px;
}

.select-circle {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border: 2px solid var(--color-surface-gray);
  border-radius: 50%;
}

.card-info {
  flex: 1;
  min-width: 0;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.name {
  font-size: 15px;
  font-weight: 700;
}

.grade {
  font-size: 11px;
  padding: 2px 7px;
  border-radius: 4px;
}

.address {
  font-size: 12px;
  color: var(--color-text-sub);
  margin: 3px 0 0;
}

.card-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.score {
  font-size: 17px;
  font-weight: 700;
  color: var(--color-text-sub);
}

.heart {
  font-size: 18px;
  color: var(--color-status-like);
}
</style>
