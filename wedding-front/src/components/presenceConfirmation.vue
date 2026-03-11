<template>
  <q-dialog v-model="dialog" backdrop-filter="blur(6px) saturate(150%)">
    <q-card class="confirmation-card">

      <q-card-section class="title-section">
        <div class="divider">
          <span class="line"></span>
          <span class="heart">✧</span>
          <span class="line"></span>
        </div>

        <h2 class="title">
          Confirmar presença
        </h2>
      </q-card-section>

      <q-card-section class="select-section">
        <q-input
          v-model="newGuest"
          label="Nome do convidado"
          outlined
          dense
          class="guest-input"
          @keyup.enter="addGuest"
        >
          <template #append>
            <q-btn
              flat
              dense
              icon="send"
              @click="addGuest"
            />
          </template>
        </q-input>
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
            <div class="confirmed-title">
              Confirmados
            </div>

            <div class="guest-list">
              <span
                v-for="guest in confirmedGuests"
                :key="`yes-${guest}`"
                class="guest-chip guest-chip--yes"
                draggable="true"
                @dragstart="handleDragStart(guest, 'yes')"
              >
                <span class="guest-chip__label">
                  {{ guest }}
                </span>
                <button
                  type="button"
                  class="guest-chip__remove"
                  aria-label="Remover convidado"
                  @click.stop="removeGuest('yes', guest)"
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
            <div class="confirmed-title">
              Não poderão ir
            </div>

            <div class="guest-list">
              <span
                v-for="guest in notGoingGuests"
                :key="`no-${guest}`"
                class="guest-chip guest-chip--no"
                draggable="true"
                @dragstart="handleDragStart(guest, 'no')"
              >
                <span class="guest-chip__label">
                  {{ guest }}
                </span>
                <button
                  type="button"
                  class="guest-chip__remove"
                  aria-label="Remover convidado"
                  @click.stop="removeGuest('no', guest)"
                >
                  ✕
                </button>
              </span>
            </div>
          </div>
        </div>
      </q-card-section>

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
import { ref } from 'vue'
import { api } from 'src/boot/axios'

const dialog = ref(false)

const newGuest = ref('')
const confirmedGuests = ref<string[]>([])
const notGoingGuests = ref<string[]>([])
const saving = ref(false)

const draggingName = ref<string | null>(null)
const draggingFrom = ref<'yes' | 'no' | null>(null)

function addGuest () {
  const name = newGuest.value.trim()
  if (!name) return
  if (!confirmedGuests.value.includes(name) && !notGoingGuests.value.includes(name)) {
    confirmedGuests.value.push(name)
  }
  newGuest.value = ''
}

function handleDragStart (name: string, from: 'yes' | 'no') {
  draggingName.value = name
  draggingFrom.value = from
}

function handleDrop (target: 'yes' | 'no') {
  if (!draggingName.value || !draggingFrom.value || draggingFrom.value === target) {
    draggingName.value = null
    draggingFrom.value = null
    return
  }

  const fromList = draggingFrom.value === 'yes' ? confirmedGuests.value : notGoingGuests.value
  const toList = target === 'yes' ? confirmedGuests.value : notGoingGuests.value

  const idx = fromList.indexOf(draggingName.value)
  if (idx !== -1) {
    fromList.splice(idx, 1)
    if (!toList.includes(draggingName.value)) {
      toList.push(draggingName.value)
    }
  }

  draggingName.value = null
  draggingFrom.value = null
}

function removeGuest (list: 'yes' | 'no', name: string) {
  const targetList = list === 'yes' ? confirmedGuests.value : notGoingGuests.value
  const idx = targetList.indexOf(name)
  if (idx !== -1) {
    targetList.splice(idx, 1)
  }
}

async function confirmPresence () {
  const allGuests = [...confirmedGuests.value, ...notGoingGuests.value]
  if (!allGuests.length) {
    return
  }

  saving.value = true

  try {
    await api.post('/guests/confirm', {
      guests: [
        ...confirmedGuests.value.map(name => ({ name, willAttend: true })),
        ...notGoingGuests.value.map(name => ({ name, willAttend: false }))
      ]
    })

    dialog.value = false
    confirmedGuests.value = []
    notGoingGuests.value = []
    newGuest.value = ''
  } catch (e) {
    console.error('Erro ao confirmar presença', e)
  } finally {
    saving.value = false
  }
}

function open () {
  dialog.value = true
}

defineExpose({
  open
})
</script>

<style scoped lang="scss">

.confirmation-card {
  min-width: 360px;
  max-width: 420px;
  border-radius: 18px;
  background: #fff8f4;
  padding-bottom: 12px;
  border: 1px solid rgba(200, 107, 90, 0.25);
  box-shadow: 0 20px 40px rgba(70, 35, 28, 0.25);
}

.title-section {
  text-align: center;
  padding-bottom: 0px;
}

.title {
  font-family: 'Playfair Display', serif;
  font-size: 1.5rem;
  color: #5a332d;
  margin-top: 12px;
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

.select-section {
  padding-top: 8px;
}

.guest-input {
  .q-field__control {
    border-radius: 12px;
  }
}
.confirmed-section {
  padding-top: 4px;
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

.actions {
  justify-content: center;
  margin-top: 8px;
}

.close-btn {
  background: linear-gradient(135deg, #954936, #a2503b);
  color: #fdfaf4;
  border-radius: 999px;
  padding: 6px 24px;
  letter-spacing: 0.12em;
}

.close-btn:hover {
  background: linear-gradient(135deg, rgb(96, 43, 30), #ac4228);
}

.confirm-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  padding: 6px 24px;
  letter-spacing: 0.12em;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #5a6a30, #76853e);
}

</style>
