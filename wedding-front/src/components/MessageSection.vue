<template>
  <section ref="messageSection" class="message-section">
    <div class="message-header" data-reveal>
      <p class="message-kicker">Recado</p>
      <h2 class="message-title">Deixe um recado</h2>

      <div class="message-divider">
        <span class="line"></span>
        <span class="heart">✧</span>
        <span class="line"></span>
      </div>
    </div>

    <div class="d-flex">
      <div class="q-px-xl">
        <q-editor v-model="editor" min-height="20rem" toolbar-rounded flat/>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const detailsSection = ref<HTMLElement | null>(null)
const editor = ref('')

function scrollToSection () {
  detailsSection.value?.scrollIntoView({ behavior: 'smooth' })
}

defineExpose({
  scrollToSection
})
</script>

<style scoped lang="scss">
.message-section {
  background: linear-gradient(to top, rgba(236, 207, 192, 0.98), #fdf3ec);
  padding: 64px 16px 88px;
}

.message-header {
  text-align: center;
  margin-bottom: 40px;
}

.message-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #c86b5a;
  margin-bottom: 10px;
}

.message-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 2.1rem;
  color: #5a332d;
  margin-bottom: 12px;
}

.message-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.message-divider .line {
  width: 52px;
  height: 1px;
  background: linear-gradient(to right, #e0b79c, #c86b5a);
}

.message-divider .heart {
  font-size: 0.9rem;
  color: #c86b5a;
}

.message-grid {
  display: flex;
  align-content: center;
}

@media (min-width: 768px) {
  .message-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
