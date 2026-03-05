<template>
  <q-layout view="lHh Lpr lFf" class="root-layout">
    <q-header
      elevated
      :class="['header', { 'header--scrolled': isScrolled }]"
    >
      <q-toolbar class="toolbar">
        <q-btn
          flat
          dense
          round
          icon="spa"
          aria-label="Menu"
          @click="toggleLeftDrawer"
        />

        <q-toolbar-title class="title">
          C &amp; R
        </q-toolbar-title>
      </q-toolbar>
    </q-header>
    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue';

const leftDrawerOpen = ref(false);
const isScrolled = ref(false);

function handleScroll () {
  isScrolled.value = window.scrollY > 10;
}

function toggleLeftDrawer () {
  leftDrawerOpen.value = !leftDrawerOpen.value;
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll, { passive: true });
  handleScroll();
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<style lang="scss">
.root-layout {
  background-color: transparent;
}

.header {
  background: transparent;
  color: #fff7f2;
  box-shadow: none;
  transition:
    background 220ms ease-out,
    box-shadow 220ms ease-out,
    backdrop-filter 220ms ease-out;
  backdrop-filter: none;
}

.header--scrolled {
  background: linear-gradient(to right, rgba(75, 43, 40, 0.98), rgba(140, 59, 42, 0.96));
  backdrop-filter: blur(10px);
  box-shadow: none;
}

.toolbar {
  min-height: 64px;
}

.title {
  font-family: 'Playfair Display', Georgia, 'Times New Roman', Times, serif;
  letter-spacing: 0.22em;
  text-transform: uppercase;
  font-size: 0.9rem;
}
</style>
