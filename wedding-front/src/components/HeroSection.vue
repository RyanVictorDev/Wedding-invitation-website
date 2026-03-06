<template>
  <div class="wedding-hero">
    <div class="overlay">
      <div class="hero-content">
        <p class="hero-subtitle" data-reveal data-reveal-order="1">
          Convite de casamento
        </p>

        <h1 class="hero-title" data-reveal data-reveal-order="2">
          Clara &amp; Ryan
        </h1>

        <p class="hero-date" data-reveal data-reveal-order="3">
          3 de Outubro, às 15h00
        </p>

        <p class="hero-location" data-reveal data-reveal-order="4">
          Igreja Matriz de Pacatuba
        </p>

        <p class="hero-phrase" data-reveal data-reveal-order="5">
          “O amor é a melhor de todas as viagens.
          Queremos você ao nosso lado nesse dia.”
        </p>

        <q-btn
          class="hero-cta"
          label="Confirmar presença"
          unelevated
          data-reveal
          data-reveal-order="7"
          @click="$emit('open-presence')"
        />
      </div>
    </div>

    <button
      class="scroll-indicator"
      aria-label="Rolar para detalhes"
      @click="$emit('scroll-details')"
    >
      <span class="scroll-indicator__circle">
        <span class="scroll-indicator__arrow"></span>
      </span>

      <span class="scroll-indicator__text">
        Role para ver mais
      </span>
    </button>
  </div>
</template>

<script setup lang="ts">
defineEmits([
  'open-presence',
  'scroll-details'
])
</script>

<style scoped lang="scss">
.wedding-hero {
  position: relative;
  min-height: 100vh;
  background-image: url('../assets/background.png');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  margin-top: -64px;
  padding-top: 64px;
}

.overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    rgba(40, 20, 15, 0.65),
    rgba(40, 20, 15, 0.85)
  );
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 48px 16px;
}

.hero-content {
  max-width: 640px;
  text-align: center;
  color: #fff3eb;
}

.hero-subtitle {
  text-transform: uppercase;
  letter-spacing: 0.18em;
  font-size: 0.8rem;
  margin-bottom: 12px;
  color: #f3d5c4;
}

.hero-title {
  font-family: 'Playfair Display', serif;
  font-size: 2.8rem;
  letter-spacing: 0.08em;
  margin-bottom: 12px;
}

.hero-date {
  font-size: 1.1rem;
  margin-bottom: 4px;
  color: #fbe1cf;
}

.hero-location {
  font-size: 1rem;
  margin-bottom: 20px;
  color: #f8d5c0;
}

.hero-phrase {
  font-size: 0.95rem;
  margin-bottom: 32px;
  color: #ffece0;
  font-style: italic;
}

.hero-cta {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4 !important;
  padding: 10px 28px;
  border-radius: 999px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.hero-cta:hover {
  background: linear-gradient(135deg, #5a6a30, #76853e);
  transform: translateY(-1px);
}

.scroll-indicator {
  position: absolute;
  left: 50%;
  bottom: 32px;
  transform: translateX(-50%);
  background: transparent;
  border: none;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #fbe1cf;
  cursor: pointer;
  font-size: 0.78rem;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.scroll-indicator__circle {
  width: 32px;
  height: 48px;
  border-radius: 999px;
  border: 1px solid rgba(248, 213, 192, 0.8);
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 8px;
}

.scroll-indicator__arrow {
  align-self: end;
  margin-bottom: 8px;
  width: 8px;
  height: 14px;
  border-radius: 999px;
  background-color: rgba(248, 213, 192, 0.9);
  animation: scrollDot 1.4s ease-in-out infinite;
}

@keyframes scrollDot {
  0% { transform: translateY(0); opacity: 0.3; }
  40% { transform: translateY(-10px); opacity: 1; }
  80% { transform: translateY(-16px); opacity: 0; }
  100% { transform: translateY(0); opacity: 0; }
}
</style>
