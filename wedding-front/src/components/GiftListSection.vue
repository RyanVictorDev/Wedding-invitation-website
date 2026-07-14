<template>
  <section
    ref="giftSection"
    class="gift-section"
    data-reveal
  >
    <div class="gift-header">
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

    <div
      v-else
      class="gift-grid"
    >
      <article
        v-for="gift in gifts"
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
import { onMounted, ref } from 'vue'
import { api } from 'src/boot/axios'

interface GiftItem {
  id: number
  title: string
  description: string | null
  imageUrl: string | null
  productUrl: string | null
  price: number | null
  reserved: boolean
  reservedBy: string | null
}

const giftSection = ref<HTMLElement | null>(null)
const gifts = ref<GiftItem[]>([])
const loading = ref(true)
const reserveDialog = ref(false)
const selectedGift = ref<GiftItem | null>(null)
const reservedBy = ref('')
const reserving = ref(false)
const reserveError = ref('')

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

.gift-empty {
  text-align: center;
  color: #7b5a4c;
  padding: 32px 16px;
}

.gift-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 24px;
  max-width: 1080px;
  margin: 0 auto;
}

.gift-card {
  background: rgba(255, 250, 245, 0.98);
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid rgba(200, 107, 90, 0.18);
  box-shadow: 0 18px 40px rgba(90, 51, 45, 0.14);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.gift-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 22px 44px rgba(90, 51, 45, 0.2);
}

.gift-card--reserved {
  opacity: 0.88;
}

.gift-image-wrap {
  position: relative;
  aspect-ratio: 4 / 3;
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
  font-size: 2.4rem;
}

.gift-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(90, 51, 45, 0.88);
  color: #fdfaf4;
  font-size: 0.72rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  padding: 6px 10px;
  border-radius: 999px;
}

.gift-body {
  padding: 18px;
}

.gift-card-title {
  font-family: 'Playfair Display', serif;
  font-size: 1.15rem;
  color: #5a332d;
  margin-bottom: 8px;
}

.gift-card-desc {
  color: #7b5a4c;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 10px;
}

.gift-price {
  font-weight: 600;
  color: #6b7a3a;
  margin-bottom: 12px;
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
  padding-inline: 18px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.75rem;
}

.gift-link-btn {
  color: #a2503b;
}

.reserve-card {
  min-width: 320px;
  max-width: 420px;
  border-radius: 18px;
  background: #fff8f4;
  border: 1px solid rgba(200, 107, 90, 0.25);
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
