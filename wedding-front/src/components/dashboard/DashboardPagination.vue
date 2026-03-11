<template>
  <div
    v-if="totalPages > 1"
    class="dashboard-pagination"
  >
    <q-btn
      round
      flat
      :icon="leftIcon"
      :disable="pageComputed === 1 || loading"
      @click="goPrev"
    />

    <div class="dashboard-page-numbers">
      <q-btn
        v-for="p in totalPages"
        :key="p"
        :label="p"
        :outline="p !== pageComputed"
        :unelevated="p === pageComputed"
        rounded
        :color="color"
        :text-color="p === pageComputed ? 'white' : color"
        class="dashboard-page-number"
        :class="{ 'dashboard-page-number--active': p === pageComputed }"
        :disable="loading"
        @click="goTo(p)"
      />
    </div>

    <q-btn
      round
      flat
      :icon="rightIcon"
      :disable="pageComputed >= totalPages || loading"
      @click="goNext"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  modelValue: number
  totalPages: number
  loading?: boolean
  color?: string
  leftIcon?: string
  rightIcon?: string
}>(), {
  loading: false,
  color: 'brown-5',
  leftIcon: 'chevron_left',
  rightIcon: 'chevron_right'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
}>()

const pageComputed = computed({
  get: () => props.modelValue || 1,
  set: (val: number) => {
    if (val < 1 || val > props.totalPages || props.loading) return
    emit('update:modelValue', val)
  }
})

function goPrev () {
  pageComputed.value = pageComputed.value - 1
}

function goNext () {
  pageComputed.value = pageComputed.value + 1
}

function goTo (p: number) {
  pageComputed.value = p
}
</script>

<style scoped lang="scss">
.dashboard-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.dashboard-page-numbers {
  display: flex;
  gap: 4px;
}

.dashboard-page-number {
  min-width: 32px;
  padding: 0 6px;
  font-size: 0.78rem;
}

.dashboard-page-number--active {
  font-weight: 600;
  border-radius: 999px;
}
</style>

