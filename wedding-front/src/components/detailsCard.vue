<template>
  <div :class="['details-card', { 'details-card--highlight': isOlive }]">
    <div class="details-icon">
      <span :class="['icon-circle', { 'icon-circle--olive': isOlive }]">{{ icon }}</span>
    </div>
    <h3>{{ title }}</h3>
    <p class="details-highlight">{{ highlight }}</p>
    <p class="details-text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

  interface DetailsCardProps {
    isOlive?: boolean;
    title: string;
    highlight: string;
    text: string;
    icon: string;
  }

  const props = withDefaults(defineProps<DetailsCardProps>(), {
    isOlive: false,
    title: '',
    highlight: '',
    text: ''
  });

  const title = ref(props.title);
  const highlight = ref(props.highlight);
  const text = ref(props.text);
  const icon = ref(props.icon);
  const isOlive = ref(props.isOlive);
</script>

<style scoped lang="scss">
.details-card {
  background-color: #ffffff;
  border-radius: 18px;
  padding: 22px 20px;
  box-shadow: 0 10px 24px rgba(88, 43, 34, 0.1);
  border: 1px solid rgba(200, 107, 90, 0.18);
  position: relative;
  overflow: hidden;
}

.details-card--highlight {
  border-color: rgba(107, 122, 58, 0.6);
  background: linear-gradient(135deg, #ffffff, #f4f7ec);
}

.details-icon {
  margin-bottom: 10px;
}

.details-title {
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

.details-card--highlight {
  border-color: rgba(107, 122, 58, 0.6);
  background: linear-gradient(135deg, #ffffff, #f4f7ec);
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
</style>
