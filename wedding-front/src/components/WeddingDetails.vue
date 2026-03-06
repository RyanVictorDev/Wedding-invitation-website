<template>
  <section ref="detailsSection" class="details-section">
    <div class="details-header" data-reveal>
      <p class="details-kicker">Celebração</p>
      <h2 class="details-title">Detalhes do grande dia</h2>

      <div class="details-divider">
        <span class="line"></span>
        <span class="heart">✧</span>
        <span class="line"></span>
      </div>
    </div>

    <div class="details-grid">
      <DetailsCard
        data-reveal
        title="Data & Horário"
        highlight="3 de Outubro · 15h00"
        text="Chegue alguns minutos antes para se acomodar com calma e aproveitar cada momento."
        icon="⏰"
      />

      <DetailsCard
        data-reveal
        title="Cerimônia"
        highlight="Igreja Matriz de Pacatuba"
        text="Pacatuba - CE | Um cenário especial para testemunhar o nosso “sim”."
        icon="⛪"
      />

      <DetailsCard
        isOlive
        data-reveal
        title="Vestimenta"
        highlight="Tons terrosos & verde oliva"
        text="Traje passeio completo em cores suaves que conversem com terracota, nude e verde oliva."
        icon="👗"
      />

      <DetailsCard
        data-reveal
        title="Confirmação de presença"
        highlight="Em breve"
        text="Em breve colocaremos aqui o link para você confirmar sua presença e deixar tudo ainda mais especial."
        icon="❤"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DetailsCard from './detailsCard.vue'

const detailsSection = ref<HTMLElement | null>(null)

function scrollToSection () {
  detailsSection.value?.scrollIntoView({ behavior: 'smooth' })
}

defineExpose({
  scrollToSection
})
</script>

<style scoped lang="scss">
.details-section {
  background: #fdf3ec;
  padding: 64px 16px 88px;
}

.details-header {
  text-align: center;
  margin-bottom: 40px;
}

.details-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #c86b5a;
  margin-bottom: 10px;
}

.details-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 2.1rem;
  color: #5a332d;
  margin-bottom: 12px;
}

.details-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.details-divider .line {
  width: 52px;
  height: 1px;
  background: linear-gradient(to right, #e0b79c, #c86b5a);
}

.details-divider .heart {
  font-size: 0.9rem;
  color: #c86b5a;
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 22px;
}

@media (min-width: 768px) {
  .details-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
