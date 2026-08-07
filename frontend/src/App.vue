<template>
  <div class="w-screen h-screen overflow-hidden">
    <RouterView />
    <InsuranceModal
      v-if="uiStore.insuranceModalConfig"
      v-bind="uiStore.insuranceModalConfig"
      @close="uiStore.closeInsuranceModal"
      @submit="uiStore.closeInsuranceModal"
    />
    <LoanModal
      v-if="uiStore.loanModalConfig"
      v-bind="uiStore.loanModalConfig"
      @close="uiStore.closeLoanModal"
      @view-detail="(product) => console.log('상세보기', product)"
      @find-branch="() => {}"
      @open-app="() => {}"
    />
  </div>
</template>

<script setup>
import { RouterView } from 'vue-router';
import { useUiStore } from '@/stores/uiStore';
import InsuranceModal from '@/components/finance/InsuranceModal.vue';
import LoanModal from '@/components/finance/LoanModal.vue';

const uiStore = useUiStore();
</script>
