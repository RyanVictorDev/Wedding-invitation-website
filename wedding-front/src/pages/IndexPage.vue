<template>
  <q-page class="wedding-page">
    <div class="wedding-hero">
      <div class="overlay">
        <div class="hero-content">
          <p class="hero-subtitle" data-reveal data-reveal-order="1">Convite de casamento</p>
          <h1 class="hero-title" data-reveal data-reveal-order="2">Clara &amp; Ryan</h1>
          <p class="hero-date" data-reveal data-reveal-order="3">3 de Outubro, às 15h00</p>
          <p class="hero-location" data-reveal data-reveal-order="4">Igreja Matriz de Pacatuba</p>
          <p class="hero-phrase" data-reveal data-reveal-order="5">
            “O amor é a melhor de todas as viagens. Queremos você ao nosso lado nesse dia.”
          </p>
          <q-btn
            class="hero-cta"
            label="Confirmar presença"
            unelevated
            @click="scrollToDetails"
            data-reveal
            data-reveal-order="6"
          />
        </div>
      </div>
      <button class="scroll-indicator" @click="scrollToDetails" aria-label="Rolar para detalhes">
        <span class="scroll-indicator__circle">
          <span class="scroll-indicator__arrow"></span>
        </span>
        <span class="scroll-indicator__text">Role para ver mais</span>
      </button>
    </div>

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
        <div class="details-card" data-reveal>
          <div class="details-icon">
            <span class="icon-circle">⏰</span>
          </div>
          <h3>Data &amp; Horário</h3>
          <p class="details-highlight">3 de Outubro · 15h00</p>
          <p class="details-text">
            Chegue alguns minutos antes para se acomodar com calma e aproveitar cada momento.
          </p>
        </div>

        <div class="details-card" data-reveal>
          <div class="details-icon">
            <span class="icon-circle">⛪</span>
          </div>
          <h3>Cerimônia</h3>
          <p class="details-highlight">Igreja Matriz de Pacatuba</p>
          <p class="details-text">
            Pacatuba - CE<br />
            Um cenário especial para testemunhar o nosso “sim”.
          </p>
        </div>

        <div class="details-card details-card--highlight" data-reveal>
          <div class="details-icon">
            <span class="icon-circle icon-circle--olive">👗</span>
          </div>
          <h3>Vestimenta</h3>
          <p class="details-highlight">Tons terrosos &amp; verde oliva</p>
          <p class="details-text">
            Traje passeio completo em cores suaves que conversem com terracota, nude e verde oliva.
          </p>
        </div>

        <div class="details-card" data-reveal>
          <div class="details-icon">
            <span class="icon-circle">❤</span>
          </div>
          <h3>Confirmação de presença</h3>
          <p class="details-highlight">Em breve</p>
          <p class="details-text">
            Em breve colocaremos aqui o link para você confirmar sua presença e deixar tudo ainda mais especial.
          </p>
        </div>
      </div>
    </section>
  </q-page>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

const detailsSection = ref<HTMLElement | null>(null);
const revealElements = ref<HTMLElement[]>([]);
let observer: IntersectionObserver | null = null;

function scrollToDetails () {
  detailsSection.value?.scrollIntoView({ behavior: 'smooth' });
}

onMounted(() => {
  revealElements.value = Array.from(
    document.querySelectorAll<HTMLElement>('[data-reveal]')
  );

  observer = new IntersectionObserver(
    entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting && entry.target instanceof Element) {
          entry.target.classList.add('is-visible');
          observer?.unobserve(entry.target);
        }
      });
    },
    {
      threshold: 0.12,
      rootMargin: '0px 0px -10% 0px'
    }
  );

  revealElements.value.forEach(el => observer?.observe(el));
});

onBeforeUnmount(() => {
  revealElements.value.forEach(el => observer?.unobserve(el));
  observer?.disconnect();
  observer = null;
});
</script>

<style lang="scss" scoped>
.wedding-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff8f4;
  color: #4b2b28;
  font-family: 'DM Sans', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@400;500;600&family=DM+Sans:wght@300;400;500&family=Playfair+Display:wght@500;600&display=swap');

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
  box-sizing: border-box;
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
  font-family: 'Playfair Display', 'Cormorant Garamond', 'Times New Roman', serif;
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
  box-shadow: 0 10px 20px rgba(65, 80, 35, 0.35);
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
  box-sizing: border-box;
  position: relative;
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

.scroll-indicator__text {
  font-size: 0.7rem;
  opacity: 0.85;
}

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

.details-card {
  background-color: #ffffff;
  border-radius: 18px;
  padding: 22px 20px;
  box-shadow: 0 10px 24px rgba(88, 43, 34, 0.1);
  border: 1px solid rgba(200, 107, 90, 0.18);
  position: relative;
  overflow: hidden;
}

.details-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top left, rgba(200, 107, 90, 0.11), transparent 55%);
  pointer-events: none;
}

.details-card--highlight {
  border-color: rgba(107, 122, 58, 0.6);
  background: linear-gradient(135deg, #ffffff, #f4f7ec);
}

.details-card h2 {
  font-family: 'Cormorant Garamond', 'Times New Roman', serif;
  font-size: 1.2rem;
  margin-bottom: 4px;
  color: #8c3b2a;
}

.details-card h3 {
  font-family: 'Cormorant Garamond', 'Times New Roman', serif;
  font-size: 1.2rem;
  margin-bottom: 4px;
  color: #8c3b2a;
}

.details-highlight {
  font-size: 0.95rem;
  font-weight: 500;
  color: #a2503b;
  margin-bottom: 6px;
}

.details-text {
  font-size: 0.95rem;
  line-height: 1.6;
  color: #5a332d;
}

.details-icon {
  margin-bottom: 10px;
}

.icon-circle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 999px;
  background-color: #fbe1cf;
  color: #b35743;
  font-size: 1rem;
}

.icon-circle--olive {
  background-color: #dbe3c5;
  color: #6b7a3a;
}

.details-grid .details-card:nth-child(1) {
  transition-delay: 60ms;
}
.details-grid .details-card:nth-child(2) {
  transition-delay: 120ms;
}
.details-grid .details-card:nth-child(3) {
  transition-delay: 180ms;
}
.details-grid .details-card:nth-child(4) {
  transition-delay: 240ms;
}

[data-reveal] {
  opacity: 0;
  transform: translateY(18px);
  transition:
    opacity 700ms ease-out,
    transform 700ms ease-out;
}

[data-reveal].is-visible {
  opacity: 1;
  transform: translateY(0);
}

[data-reveal-order="1"] {
  transition-delay: 40ms;
}

[data-reveal-order="2"] {
  transition-delay: 120ms;
}

[data-reveal-order="3"] {
  transition-delay: 200ms;
}

[data-reveal-order="4"] {
  transition-delay: 260ms;
}

[data-reveal-order="5"] {
  transition-delay: 320ms;
}

[data-reveal-order="6"] {
  transition-delay: 380ms;
}

@keyframes scrollDot {
  0% {
    transform: translateY(0);
    opacity: 0.3;
  }
  40% {
    transform: translateY(-10px);
    opacity: 1;
  }
  80% {
    transform: translateY(-16px);
    opacity: 0;
  }
  100% {
    transform: translateY(0);
    opacity: 0;
  }
}

@media (min-width: 768px) {
  .hero-title {
    font-size: 3.4rem;
  }

  .hero-phrase {
    font-size: 1.05rem;
  }

  .details-section {
    padding-inline: 80px;
  }

  .details-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .details-grid > .details-card:nth-child(3),
  .details-grid > .details-card:nth-child(4) {
    grid-column: span 1;
  }
}
</style>
