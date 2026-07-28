// src/stores/uiStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUiStore = defineStore('ui', () => {
  const isDetailOpen = ref(false)

  const searchQuery = ref('')

  const toggleDetailPanel = () => {
    isDetailOpen.value = !isDetailOpen.value
  }

  return { isDetailOpen, searchQuery, toggleDetailPanel }
})
