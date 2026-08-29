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
        <p class="subtitle">Escreva seu nome completo e informe como você participará.</p>
      </q-card-section>

      <q-card-section class="select-section">
        <q-input
          v-model="selfName"
          label="Nome completo"
          outlined
          dense
          class="guest-input"
          clearable
          @update:model-value="errorMessage = ''"
        />

        <div class="attendance-options q-mt-md">
          <div class="attendance-label">É adulto ou criança?</div>
          <q-option-group
            v-model="selfAgeCategory"
            :options="ageOptions"
            type="radio"
            class="attendance-group"
            @update:model-value="errorMessage = ''"
          />
        </div>

        <div class="attendance-options q-mt-md">
          <div class="attendance-label">Como você participará?</div>
          <q-option-group
            v-model="attendanceChoice"
            :options="attendanceOptions"
            type="radio"
            class="attendance-group"
            @update:model-value="errorMessage = ''"
          />
        </div>

        <q-separator class="q-my-lg" />

        <p class="list-hint">Ou busque na lista de convidados (famílias e grupos):</p>

        <q-input
          :model-value="searchTerm"
          label="Buscar convidado (mín. 3 letras)"
          outlined
          dense
          class="guest-input q-mt-sm"
          clearable
          :loading="searchLoading"
          @update:model-value="onSearchInput"
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
          Nome já confirmado ou não encontrado na lista de convidados.
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
                  class="guest-chip__age"
                  :aria-label="`Alterar faixa etária de ${guest.name}`"
                  @click.stop="toggleGuestAge(guest)"
                >
                  {{ guest.ageCategory === 'CHILD' ? 'Criança' : 'Adulto' }}
                </button>
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
                  class="guest-chip__age"
                  :aria-label="`Alterar faixa etária de ${guest.name}`"
                  @click.stop="toggleGuestAge(guest)"
                >
                  {{ guest.ageCategory === 'CHILD' ? 'Criança' : 'Adulto' }}
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
          :disable="!canSubmit"
          label="Confirmar"
          class="confirm-btn"
          @click="confirmPresence"
        />
      </q-card-actions>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { api } from 'src/boot/axios'

interface GuestLookup {
  id: number
  name: string
  godparent: boolean
}

interface ListGuest extends GuestLookup {
  ageCategory: 'ADULT' | 'CHILD'
}

type AttendanceChoice = 'CEREMONY_AND_RECEPTION' | 'CEREMONY_ONLY' | 'RECEPTION_ONLY' | 'NOT_GOING'
type AgeCategory = 'ADULT' | 'CHILD'
type AttendingChoice = 'CEREMONY_AND_RECEPTION' | 'CEREMONY_ONLY' | 'RECEPTION_ONLY'

interface ConfirmPayloadEntry {
  id?: number
  name?: string
  willAttend: boolean
  attendanceType?: AttendingChoice
  ageCategory: AgeCategory
}

const ageOptions = [
  { label: 'Adulto', value: 'ADULT' as AgeCategory },
  { label: 'Criança', value: 'CHILD' as AgeCategory }
]

const attendanceOptions = [
  {
    label: 'Estarei presente no casamento e na recepção',
    value: 'CEREMONY_AND_RECEPTION' as AttendanceChoice
  },
  {
    label: 'Vou apenas para o casamento na igreja',
    value: 'CEREMONY_ONLY' as AttendanceChoice
  },
  {
    label: 'Vou apenas para a recepção',
    value: 'RECEPTION_ONLY' as AttendanceChoice
  },
  {
    label: 'Não poderá ir',
    value: 'NOT_GOING' as AttendanceChoice
  }
]

const dialog = ref(false)
const selfName = ref('')
const selfAgeCategory = ref<AgeCategory | null>(null)
const attendanceChoice = ref<AttendanceChoice | null>(null)
const searchTerm = ref('')
const searchResults = ref<GuestLookup[]>([])
const searchLoading = ref(false)
const confirmedGuests = ref<ListGuest[]>([])
const notGoingGuests = ref<ListGuest[]>([])
const saving = ref(false)
const errorMessage = ref('')

const draggingGuest = ref<ListGuest | null>(null)
const draggingFrom = ref<'yes' | 'no' | null>(null)

let searchTimer: number | null = null

const hasSelfEntry = computed(() => selfName.value.trim().length > 0)
const hasListEntries = computed(() => confirmedGuests.value.length > 0 || notGoingGuests.value.length > 0)
const attendingChoice = computed(() =>
  attendanceChoice.value === 'CEREMONY_AND_RECEPTION'
  || attendanceChoice.value === 'CEREMONY_ONLY'
  || attendanceChoice.value === 'RECEPTION_ONLY'
)

const canSubmit = computed(() => {
  if (!hasSelfEntry.value && !hasListEntries.value) return false

  if (hasSelfEntry.value && !selfAgeCategory.value) return false

  if (hasSelfEntry.value && !attendanceChoice.value) return false

  if (confirmedGuests.value.length > 0 && !attendingChoice.value) return false

  return true
})

function toListGuest (guest: GuestLookup): ListGuest {
  return { ...guest, ageCategory: 'ADULT' }
}

function toggleGuestAge (guest: ListGuest) {
  guest.ageCategory = guest.ageCategory === 'ADULT' ? 'CHILD' : 'ADULT'
}

function onSearchInput (value: string | number | null) {
  searchTerm.value = value == null ? '' : String(value)
}

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
  confirmedGuests.value.push(toListGuest(guest))
  searchResults.value = searchResults.value.filter(g => g.id !== guest.id)
  searchTerm.value = ''
  errorMessage.value = ''
}

function handleDragStart (guest: ListGuest, from: 'yes' | 'no') {
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

function buildPayload (): ConfirmPayloadEntry[] {
  const guests: ConfirmPayloadEntry[] = []
  const trimmedName = selfName.value.trim()

  if (trimmedName && selfAgeCategory.value) {
    if (attendanceChoice.value === 'NOT_GOING') {
      guests.push({ name: trimmedName, willAttend: false, ageCategory: selfAgeCategory.value })
    } else if (attendingChoice.value) {
      guests.push({
        name: trimmedName,
        willAttend: true,
        attendanceType: attendanceChoice.value as AttendingChoice,
        ageCategory: selfAgeCategory.value
      })
    }
  }

  if (attendingChoice.value) {
    const attendanceType = attendanceChoice.value as AttendingChoice
    for (const guest of confirmedGuests.value) {
      guests.push({
        id: guest.id,
        willAttend: true,
        attendanceType,
        ageCategory: guest.ageCategory
      })
    }
  }

  for (const guest of notGoingGuests.value) {
    guests.push({ id: guest.id, willAttend: false, ageCategory: guest.ageCategory })
  }

  return guests
}

function resetForm () {
  selfName.value = ''
  selfAgeCategory.value = null
  attendanceChoice.value = null
  confirmedGuests.value = []
  notGoingGuests.value = []
  searchTerm.value = ''
  searchResults.value = []
  errorMessage.value = ''
}

async function confirmPresence () {
  if (!canSubmit.value) return

  const guests = buildPayload()
  if (!guests.length) {
    errorMessage.value = 'Informe seu nome ou selecione convidados da lista.'
    return
  }

  saving.value = true
  errorMessage.value = ''

  try {
    await api.post('/guests/confirm', { guests })
    dialog.value = false
    resetForm()
  } catch {
    errorMessage.value = 'Não foi possível confirmar. Verifique se o nome já foi cadastrado ou se algum convidado já respondeu.'
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

.list-hint {
  color: #7b5a4c;
  font-size: 0.85rem;
  margin: 0;
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

.attendance-label {
  font-size: 0.85rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #a2503b;
  margin-bottom: 8px;
}

.attendance-group :deep(.q-radio) {
  margin-bottom: 6px;
}

.attendance-group :deep(.q-radio__label) {
  color: #5a332d;
  font-size: 0.9rem;
  line-height: 1.35;
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

.guest-chip__age {
  border: none;
  background: rgba(0, 0, 0, 0.06);
  color: inherit;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 999px;
  font-size: 0.68rem;
  letter-spacing: 0.04em;
  line-height: 1.2;
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
