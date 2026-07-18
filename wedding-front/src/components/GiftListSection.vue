<template>
  <section
    ref="giftSection"
    class="gift-section"
  >
    <div
      class="gift-header"
      data-reveal
    >
      <p class="gift-kicker">Lista de presentes</p>
      <h2 class="gift-title">Escolha um mimo especial</h2>

      <div class="gift-divider">
        <span class="line"></span>
        <span class="heart">✧</span>
        <span class="line"></span>
      </div>

      <p class="gift-intro">
        Se preferir presentear com algo específico, escolha um item da nossa lista.
        Você também pode deixar seu nome ou reservar anonimamente.
      </p>
    </div>

    <div v-if="loading" class="text-center q-my-xl">
      <q-spinner color="primary" size="2.5em" />
    </div>

    <div
      v-else-if="!gifts.length"
      class="gift-empty"
    >
      Em breve publicaremos nossa lista de presentes por aqui.
    </div>

    <template v-else>
      <div class="gift-toolbar">
        <p class="gift-stats">
          {{ availableCount }} disponíveis · {{ reservedCount }} reservados
        </p>

        <div class="gift-sort">
          <button
            v-for="option in sortOptions"
            :key="option.value"
            type="button"
            class="gift-sort-btn"
            :class="{ 'gift-sort-btn--active': sortMode === option.value }"
            @click="sortMode = option.value"
          >
            {{ option.label }}
          </button>
        </div>
      </div>

      <div class="gift-grid">
        <article
          v-for="gift in visibleGifts"
          :key="gift.id"
          class="gift-card"
          :class="{ 'gift-card--reserved': gift.reserved }"
        >
          <div class="gift-image-wrap">
            <img
              v-if="gift.imageUrl"
              :src="gift.imageUrl"
              :alt="gift.title"
              class="gift-image"
            >
            <div
              v-else
              class="gift-image-placeholder"
            >
              🎁
            </div>
            <span
              v-if="gift.reserved"
              class="gift-badge"
            >
              Reservado
            </span>
          </div>

          <div class="gift-body">
            <h3 class="gift-card-title">{{ gift.title }}</h3>
            <p
              v-if="gift.description"
              class="gift-card-desc"
            >
              {{ gift.description }}
            </p>
            <div
              v-if="gift.price != null"
              class="gift-price"
            >
              R$ {{ formatPrice(gift.price) }}
            </div>

            <div class="gift-actions">
              <q-btn
                v-if="!gift.reserved"
                unelevated
                label="Quero presentear"
                class="gift-reserve-btn"
                @click="openReserveDialog(gift)"
              />
              <q-btn
                v-if="gift.productUrl"
                flat
                dense
                label="Ver na loja"
                class="gift-link-btn"
                @click="openProduct(gift.productUrl)"
              />
            </div>
          </div>
        </article>
      </div>

      <div
        v-if="hasMoreGifts"
        class="gift-load-more"
      >
        <q-btn
          flat
          no-caps
          label="Ver mais"
          class="gift-load-more-btn"
          @click="showMoreGifts"
        />
        <p class="gift-load-more-hint">
          Mostrando {{ visibleGifts.length }} de {{ sortedGifts.length }}
        </p>
      </div>
    </template>

    <q-dialog
      v-model="reserveDialog"
      backdrop-filter="blur(6px) saturate(150%)"
    >
      <q-card class="reserve-card q-pa-lg">
        <h3 class="reserve-title">Reservar presente</h3>
        <p class="reserve-subtitle">
          {{ selectedGift?.title }}
        </p>

        <q-input
          v-model="reservedBy"
          label="Seu nome (opcional — deixe em branco para anônimo)"
          outlined
          dense
          class="q-mt-md"
        />

        <div
          v-if="reserveError"
          class="reserve-error q-mt-sm"
        >
          {{ reserveError }}
        </div>

        <div class="reserve-actions q-mt-lg">
          <q-btn
            flat
            label="Cancelar"
            @click="reserveDialog = false"
          />
          <q-btn
            unelevated
            label="Confirmar reserva"
            class="gift-reserve-btn"
            :loading="reserving"
            @click="confirmReserve"
          />
        </div>
      </q-card>
    </q-dialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Notify } from 'quasar'
import { api } from 'src/boot/axios'

type SortMode = 'default' | 'price-asc' | 'price-desc'

interface GiftItem {
  id: number
  title: string
  description: string | null
  imageUrl: string | null
  productUrl: string | null
  price: number | null
  reserved: boolean
  reservedBy: string | null
  sortOrder: number
}

const sortOptions: { value: SortMode; label: string }[] = [
  { value: 'default', label: 'Ordem' },
  { value: 'price-asc', label: 'Menor preço' },
  { value: 'price-desc', label: 'Maior preço' }
]

const PAGE_SIZE = 8

const giftSection = ref<HTMLElement | null>(null)
const gifts = ref<GiftItem[]>([])
const loading = ref(true)
const sortMode = ref<SortMode>('default')
const visibleLimit = ref(PAGE_SIZE)
const reserveDialog = ref(false)
const selectedGift = ref<GiftItem | null>(null)
const reservedBy = ref('')
const reserving = ref(false)
const reserveError = ref('')

const availableCount = computed(() => gifts.value.filter(g => !g.reserved).length)
const reservedCount = computed(() => gifts.value.filter(g => g.reserved).length)

function compareByMode (a: GiftItem, b: GiftItem, mode: SortMode) {
  if (mode === 'default') {
    return a.sortOrder - b.sortOrder || a.id - b.id
  }

  const priceA = a.price
  const priceB = b.price

  if (priceA == null && priceB == null) {
    return a.sortOrder - b.sortOrder || a.id - b.id
  }
  if (priceA == null) return 1
  if (priceB == null) return -1

  if (mode === 'price-asc') return priceA - priceB
  return priceB - priceA
}

function sortPartition (items: GiftItem[], mode: SortMode) {
  return [...items].sort((a, b) => compareByMode(a, b, mode))
}

const sortedGifts = computed(() => {
  const available = sortPartition(gifts.value.filter(g => !g.reserved), sortMode.value)
  const reserved = sortPartition(gifts.value.filter(g => g.reserved), sortMode.value)
  return [...available, ...reserved]
})

const visibleGifts = computed(() => sortedGifts.value.slice(0, visibleLimit.value))

const hasMoreGifts = computed(() => visibleLimit.value < sortedGifts.value.length)

function showMoreGifts () {
  visibleLimit.value += PAGE_SIZE
}

watch(sortMode, () => {
  visibleLimit.value = PAGE_SIZE
})

function formatPrice (value: number) {
  return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadGifts () {
  loading.value = true
  try {
    const { data } = await api.get<GiftItem[]>('/gifts')
    gifts.value = data
  } catch (e) {
    console.error('Erro ao carregar lista de presentes', e)
  } finally {
    loading.value = false
  }
}

function openReserveDialog (gift: GiftItem) {
  selectedGift.value = gift
  reservedBy.value = ''
  reserveError.value = ''
  reserveDialog.value = true
}

function openProduct (url: string) {
  window.open(url, '_blank', 'noopener,noreferrer')
}

async function confirmReserve () {
  if (!selectedGift.value) return

  reserving.value = true
  reserveError.value = ''

  try {
    await api.post(`/gifts/${selectedGift.value.id}/reserve`, {
      reservedBy: reservedBy.value.trim() || null
    })
    reserveDialog.value = false
    Notify.create({
      type: 'positive',
      message: 'Presente reservado!',
      position: 'top'
    })
    await loadGifts()
  } catch {
    reserveError.value = 'Não foi possível reservar este presente. Talvez alguém acabou de escolhê-lo.'
  } finally {
    reserving.value = false
  }
}

function scrollToSection () {
  giftSection.value?.scrollIntoView({ behavior: 'smooth' })
}

defineExpose({
  scrollToSection
})

onMounted(() => {
  void loadGifts()
})
</script>

<style scoped lang="scss">
.gift-section {
  background: #f7f5ec;
  padding: 64px 16px 72px;
}

@media (min-width: 1024px) {
  .gift-section {
    padding-inline: 40px;
  }
}

.gift-header {
  text-align: center;
  margin-bottom: 36px;
}

.gift-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #6b7a3a;
  margin-bottom: 8px;
}

.gift-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 2.1rem;
  color: #5a332d;
  margin-bottom: 10px;
}

.gift-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 14px;
}

.gift-divider .line {
  width: 52px;
  height: 1px;
  background: linear-gradient(to right, #e0b79c, #6b7a3a);
}

.gift-divider .heart {
  font-size: 0.9rem;
  color: #6b7a3a;
}

.gift-intro {
  max-width: 640px;
  margin: 0 auto;
  color: #7b5a4c;
  line-height: 1.6;
}

.gift-toolbar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.gift-stats {
  margin: 0;
  font-size: 0.85rem;
  color: #7b5a4c;
  letter-spacing: 0.02em;
}

.gift-sort {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.gift-sort-btn {
  appearance: none;
  border: 1px solid rgba(200, 107, 90, 0.28);
  background: rgba(255, 250, 245, 0.95);
  color: #7b5a4c;
  border-radius: 999px;
  padding: 8px 14px;
  font-size: 0.78rem;
  letter-spacing: 0.04em;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.gift-sort-btn:hover {
  border-color: rgba(107, 122, 58, 0.45);
  color: #5a332d;
}

.gift-sort-btn--active {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  border-color: transparent;
  color: #fdfaf4;
}

.gift-empty {
  text-align: center;
  color: #7b5a4c;
  padding: 32px 16px;
}

.gift-load-more {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: 28px;
}

.gift-load-more-btn {
  color: #6b7a3a;
  border: 1px solid rgba(107, 122, 58, 0.35);
  border-radius: 999px;
  padding: 6px 22px;
  font-size: 0.85rem;
  letter-spacing: 0.04em;
}

.gift-load-more-hint {
  margin: 0;
  font-size: 0.78rem;
  color: #9a7a6c;
}

.gift-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  width: 100%;
  max-width: 1320px;
  margin: 0 auto;
}

@media (min-width: 640px) {
  .gift-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px;
  }
}

@media (min-width: 1024px) {
  .gift-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
  }
}

.gift-card {
  background: rgba(255, 250, 245, 0.98);
  border-radius: 16px;
  overflow: hidden;
  border: 1px solid rgba(200, 107, 90, 0.18);
  box-shadow: 0 12px 28px rgba(90, 51, 45, 0.1);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.gift-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 32px rgba(90, 51, 45, 0.16);
}

.gift-card--reserved {
  opacity: 0.72;
}

.gift-card--reserved:hover {
  transform: none;
  box-shadow: 0 12px 28px rgba(90, 51, 45, 0.1);
}

.gift-image-wrap {
  position: relative;
  aspect-ratio: 1 / 1;
  background: #fff3eb;
}

.gift-image,
.gift-image-placeholder {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gift-image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.gift-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(90, 51, 45, 0.88);
  color: #fdfaf4;
  font-size: 0.68rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  padding: 5px 9px;
  border-radius: 999px;
}

.gift-body {
  padding: 14px;
}

.gift-card-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.05rem;
  color: #5a332d;
  margin-bottom: 6px;
}

.gift-card-desc {
  color: #7b5a4c;
  font-size: 0.85rem;
  line-height: 1.45;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.gift-price {
  font-weight: 600;
  font-size: 0.95rem;
  color: #6b7a3a;
  margin-bottom: 10px;
}

.gift-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.gift-reserve-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  padding-inline: 16px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.72rem;
}

.gift-link-btn {
  color: #a2503b;
}

.reserve-card {
  width: min(100%, 420px);
  min-width: unset;
  max-width: 420px;
  border-radius: 18px;
  background: #fff8f4;
  border: 1px solid rgba(200, 107, 90, 0.25);
}

@media (max-width: 400px) {
  .reserve-card {
    width: calc(100vw - 32px);
  }
}

.reserve-title {
  font-family: 'Playfair Display', serif;
  color: #5a332d;
  font-size: 1.3rem;
}

.reserve-subtitle {
  color: #7b5a4c;
}

.reserve-error {
  color: #8a1c1c;
  background: #fdecea;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 0.85rem;
}

.reserve-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
