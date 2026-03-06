<template>
  <q-page class="wedding-page">
    <HeroSection
      @open-presence="openPresenceModal"
      @scroll-details="scrollToDetails"
    />

    <WeddingDetails ref="detailsComponent" />
  </q-page>
  <PresenceConfirmation ref="presenceModal" />
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';
import PresenceConfirmation from 'src/components/presenceConfirmation.vue';
import WeddingDetails from 'src/components/WeddingDetails.vue';
import HeroSection from 'src/components/HeroSection.vue'

const detailsComponent = ref<InstanceType<typeof WeddingDetails> | null>(null)
const revealElements = ref<HTMLElement[]>([]);
let observer: IntersectionObserver | null = null;

const presenceModal = ref<InstanceType<typeof PresenceConfirmation> | null>(null)

function openPresenceModal () {
  presenceModal.value?.open()
}

function scrollToDetails () {
  detailsComponent.value?.scrollToSection()
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
