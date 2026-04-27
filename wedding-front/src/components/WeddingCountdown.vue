<template>
  <div class="countdown" role="timer" aria-live="polite">
    <p v-if="label" class="countdown-label">{{ label }}</p>

    <div v-if="!isPast" class="countdown-grid">
      <div class="countdown-cell">
        <span class="countdown-number">{{ parts.days }}</span>
        <span class="countdown-unit">{{ parts.days === 1 ? 'dia' : 'dias' }}</span>
      </div>

      <span class="countdown-separator">:</span>

      <div class="countdown-cell">
        <span class="countdown-number">{{ pad(parts.hours) }}</span>
        <span class="countdown-unit">horas</span>
      </div>

      <span class="countdown-separator">:</span>

      <div class="countdown-cell">
        <span class="countdown-number">{{ pad(parts.minutes) }}</span>
        <span class="countdown-unit">min</span>
      </div>

      <span class="countdown-separator">:</span>

      <div class="countdown-cell">
        <span class="countdown-number">{{ pad(parts.seconds) }}</span>
        <span class="countdown-unit">seg</span>
      </div>
    </div>

    <p v-else class="countdown-arrived">
      Hoje é o nosso grande dia!
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

interface WeddingCountdownProps {
  target?: Date | string
  label?: string
}

const props = withDefaults(defineProps<WeddingCountdownProps>(), {
  target: '2026-10-03T15:00:00',
  label: 'Faltam para o nosso sim'
})

const targetDate = computed(() => new Date(props.target))
const now = ref(new Date())
let timerId: number | undefined

function tick () {
  now.value = new Date()
}

function pad (value: number) {
  return String(value).padStart(2, '0')
}

onMounted(() => {
  tick()
  timerId = window.setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (timerId !== undefined) {
    window.clearInterval(timerId)
    timerId = undefined
  }
})

const diffMs = computed(() => targetDate.value.getTime() - now.value.getTime())
const isPast = computed(() => diffMs.value <= 0)

const parts = computed(() => {
  const ms = Math.max(diffMs.value, 0)
  const days = Math.floor(ms / 86_400_000)
  const hours = Math.floor((ms % 86_400_000) / 3_600_000)
  const minutes = Math.floor((ms % 3_600_000) / 60_000)
  const seconds = Math.floor((ms % 60_000) / 1000)
  return { days, hours, minutes, seconds }
})
</script>

<style scoped lang="scss">
.countdown {
  margin: 18px auto 28px;
  padding: 16px 18px;
  max-width: 460px;
  background: rgba(255, 243, 235, 0.12);
  border: 1px solid rgba(248, 213, 192, 0.35);
  border-radius: 14px;
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  text-align: center;
  color: #fff3eb;
}

.countdown-label {
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 0.7rem;
  margin-bottom: 12px;
  color: #f3d5c4;
}

.countdown-grid {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 8px;
}

.countdown-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 52px;
}

.countdown-number {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 2rem;
  line-height: 1.1;
  color: #fff3eb;
  font-variant-numeric: tabular-nums;
}

.countdown-unit {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.65rem;
  color: #f3d5c4;
  margin-top: 4px;
}

.countdown-separator {
  font-family: 'Playfair Display', serif;
  font-size: 1.6rem;
  color: rgba(248, 213, 192, 0.55);
  line-height: 1.1;
  padding-top: 2px;
}

.countdown-arrived {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 1.4rem;
  color: #fff3eb;
  margin: 0;
}

@media (min-width: 768px) {
  .countdown {
    margin: 22px auto 32px;
    padding: 20px 24px;
  }

  .countdown-grid {
    gap: 14px;
  }

  .countdown-cell {
    min-width: 64px;
  }

  .countdown-number {
    font-size: 2.6rem;
  }

  .countdown-unit {
    font-size: 0.72rem;
  }

  .countdown-separator {
    font-size: 2rem;
  }

  .countdown-arrived {
    font-size: 1.7rem;
  }
}
</style>
