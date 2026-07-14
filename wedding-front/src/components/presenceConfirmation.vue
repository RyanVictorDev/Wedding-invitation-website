<template>
  <q-dialog v-model="dialog" backdrop-filter="blur(6px) saturate(150%)">
    <q-card class="confirmation-card">
      <q-card-section class="title-section">
        <div class="divider">
          <span class="line"></span>
          <span class="heart">✧</span>
          <span class="line"></span>
        </div>
        <h2 class="title">Confirmar presença</h2>
        <p class="subtitle">Busque pelo nome completo e selecione quem irá ao casamento.</p>
      </q-card-section>

      <q-card-section class="select-section">
        <q-input
          v-model="searchTerm"
          label="Buscar convidado (mín. 3 letras)"
          outlined
          dense
          class="guest-input"
          clearable
          :loading="searchLoading"
        />

        <div v-if="searchResults.length" class="search-results q-mt-sm">
          <button
            v-for="guest in searchResults"
            :key="guest.id"
            type="button"
            class="search-result"
            @click="selectGuest(guest)"
          >
            {{ guest.name }}
            <span v-if="guest.godparent" class="search-result-badge">Padrinho/Madrinha</span>
          </button>
        </div>

        <div
          v-else-if="searchTerm.trim().length >= 3 && !searchLoading"
          class="search-empty q-mt-sm"
        >
          Nome não encontrado na lista de convidados.
        </div>
      </q-card-section>

      <q-card-section
        v-if="confirmedGuests.length || notGoingGuests.length"
        class="confirmed-section"
      >
        <div class="lists-container">
          <div
            class="guest-list-column"
            @dragover.prevent
            @drop="handleDrop('yes')"
          >
            <div class="confirmed-title">Confirmados</div>
            <div class="guest-list">
              <span
                v-for="guest in confirmedGuests"
                :key="`yes-${guest.id}`"
                class="guest-chip guest-chip--yes"
                draggable="true"
                @dragstart="handleDragStart(guest, 'yes')"
              >
                <span class="guest-chip__label">{{ guest.name }}</span>
                <button
                  type="button"
                  class="guest-chip__remove"
                  aria-label="Remover convidado"
                  @click.stop="removeGuest('yes', guest.id)"
                >
                  ✕
                </button>
              </span>
            </div>
          </div>

          <div
            class="guest-list-column"
            @dragover.prevent
            @drop="handleDrop('no')"
          >
            <div class="confirmed-title">Não poderão ir</div>
            <div class="guest-list">
              <span
                v-for="guest in notGoingGuests"
                :key="`no-${guest.id}`"
                class="guest-chip guest-chip--no"
                draggable="true"
                @dragstart="handleDragStart(guest, 'no')"
              >
                <span class="guest-chip__label">{{ guest.name }}</span>
                <button
                  type="button"
                  class="guest-chip__remove"
                  aria-label="Remover convidado"
                  @click.stop="removeGuest('no', guest.id)"
                >
                  ✕
                </button>
              </span>
            </div>
          </div>
        </div>
      </q-card-section>

      <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

      <q-card-actions class="actions">
        <q-btn
          unelevated
          :loading="saving"
          :disable="!confirmedGuests.length && !notGoingGuests.length"
          label="Confirmar"
          class="confirm-btn"
          @click="confirmPresence"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { api } from 'src/boot/axios'

interface GuestLookup {
  id: number
  name: string
  godparent: boolean
}

const dialog = ref(false)
const searchTerm = ref('')
const searchResults = ref<GuestLookup[]>([])
const searchLoading = ref(false)
const confirmedGuests = ref<GuestLookup[]>([])
const notGoingGuests = ref<GuestLookup[]>([])
const saving = ref(false)
const errorMessage = ref('')

const draggingGuest = ref<GuestLookup | null>(null)
const draggingFrom = ref<'yes' | 'no' | null>(null)

let searchTimer: number | null = null

watch(searchTerm, (value) => {
  if (searchTimer !== null) {
    window.clearTimeout(searchTimer)
  }

  const term = value.trim()
  if (term.length < 3) {
    searchResults.value = []
    return
  }

  searchTimer = window.setTimeout(() => {
    void lookupGuests(term)
  }, 300)
})

function isSelected (id: number) {
  return confirmedGuests.value.some(g => g.id === id) || notGoingGuests.value.some(g => g.id === id)
}

async function lookupGuests (term: string) {
  searchLoading.value = true
  try {
    const { data } = await api.get<GuestLookup[]>('/guests/lookup', {
      params: { search: term }
    })
    searchResults.value = data.filter(g => !isSelected(g.id))
  } catch (e) {
    console.error('Erro ao buscar convidados', e)
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

function selectGuest (guest: GuestLookup) {
  if (isSelected(guest.id)) return
  confirmedGuests.value.push(guest)
  searchResults.value = searchResults.value.filter(g => g.id !== guest.id)
  searchTerm.value = ''
  errorMessage.value = ''
}

function handleDragStart (guest: GuestLookup, from: 'yes' | 'no') {
  draggingGuest.value = guest
  draggingFrom.value = from
}

function handleDrop (target: 'yes' | 'no') {
  if (!draggingGuest.value || !draggingFrom.value || draggingFrom.value === target) {
    draggingGuest.value = null
    draggingFrom.value = null
    return
  }

  const fromList = draggingFrom.value === 'yes' ? confirmedGuests.value : notGoingGuests.value
  const toList = target === 'yes' ? confirmedGuests.value : notGoingGuests.value

  const idx = fromList.findIndex(g => g.id === draggingGuest.value?.id)
  if (idx !== -1) {
    const [guest] = fromList.splice(idx, 1)
    if (guest && !toList.some(g => g.id === guest.id)) {
      toList.push(guest)
    }
  }

  draggingGuest.value = null
  draggingFrom.value = null
}

function removeGuest (list: 'yes' | 'no', id: number) {
  const targetList = list === 'yes' ? confirmedGuests.value : notGoingGuests.value
  const idx = targetList.findIndex(g => g.id === id)
  if (idx !== -1) {
    targetList.splice(idx, 1)
  }
}

async function confirmPresence () {
  const allGuests = [...confirmedGuests.value, ...notGoingGuests.value]
  if (!allGuests.length) return

  saving.value = true
  errorMessage.value = ''

  try {
    await api.post('/guests/confirm', {
      guests: [
        ...confirmedGuests.value.map(g => ({ id: g.id, willAttend: true })),
        ...notGoingGuests.value.map(g => ({ id: g.id, willAttend: false }))
      ]
    })

    dialog.value = false
    confirmedGuests.value = []
    notGoingGuests.value = []
    searchTerm.value = ''
    searchResults.value = []
  } catch {
    errorMessage.value = 'Não foi possível confirmar. Verifique se algum convidado já respondeu.'
  } finally {
    saving.value = false
  }
}

function open () {
  dialog.value = true
  errorMessage.value = ''
}

defineExpose({
  open
})
</script>

<style scoped lang="scss">
.confirmation-card {
  min-width: 360px;
  max-width: 520px;
  border-radius: 18px;
  background: #fff8f4;
  padding-bottom: 12px;
  border: 1px solid rgba(200, 107, 90, 0.25);
  box-shadow: 0 20px 40px rgba(70, 35, 28, 0.25);
}

.title-section {
  text-align: center;
  padding-bottom: 0;
}

.title {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  color: #5a332d;
  margin-top: 12px;
}

.subtitle {
  color: #7b5a4c;
  font-size: 0.88rem;
  margin-top: 6px;
}

.divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.line {
  width: 52px;
  height: 1px;
  background: linear-gradient(to right, #e0b79c, #c86b5a);
}

.heart {
  color: #c86b5a;
  font-size: 0.9rem;
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 180px;
  overflow-y: auto;
}

.search-result {
  border: 1px solid rgba(200, 107, 90, 0.25);
  background: #fff;
  border-radius: 12px;
  padding: 10px 12px;
  text-align: left;
  cursor: pointer;
  color: #5a332d;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.search-result:hover {
  background: #fdf3ec;
  border-color: #c86b5a;
}

.search-result-badge {
  margin-left: 8px;
  font-size: 0.72rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #a2503b;
}

.search-empty {
  color: #8a1c1c;
  font-size: 0.85rem;
}

.lists-container {
  display: flex;
  gap: 16px;
}

.guest-list-column {
  flex: 1;
  min-width: 0;
}

.confirmed-title {
  font-size: 0.85rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #a2503b;
  margin-bottom: 8px;
}

.guest-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.guest-chip {
  font-size: 0.8rem;
  padding: 6px 8px;
  border-radius: 999px;
  font-weight: 500;
  letter-spacing: 0.04em;
  cursor: grab;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.guest-chip__label {
  white-space: nowrap;
}

.guest-chip__remove {
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  padding: 0 2px;
  font-size: 0.8rem;
}

.guest-chip--yes {
  background: #e6f4e8;
  color: #295b31;
  border: 1px solid #a2d6a4;
}

.guest-chip--no {
  background: #fdecea;
  color: #8a1c1c;
  border: 1px solid #f5b5b5;
}

.error-banner {
  margin: 0 16px;
  padding: 8px 12px;
  border-radius: 10px;
  background: #fdecea;
  color: #8a1c1c;
  font-size: 0.85rem;
}

.actions {
  justify-content: center;
  margin-top: 8px;
}

.confirm-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  padding: 6px 24px;
  letter-spacing: 0.12em;
}
</style>
