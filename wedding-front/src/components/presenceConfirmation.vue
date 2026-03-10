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
        v-if="selectedGuests.length"
        class="confirmed-section"
      >
        <div class="confirmed-title">
          Confirmados
        </div>

        <div class="guest-list">
          <span
            v-for="guest in selectedGuests"
            :key="guest"
            class="guest-chip"
          >
            {{ guest }}
          </span>
        </div>
      </q-card-section>

      <q-card-actions class="actions">
        <q-btn
          unelevated
          :loading="saving"
          :disable="!selectedGuests.length"
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
const selectedGuests = ref<string[]>([])
const saving = ref(false)

function addGuest () {
  const name = newGuest.value.trim()
  if (!name) return
  if (!selectedGuests.value.includes(name)) {
    selectedGuests.value.push(name)
  }
  newGuest.value = ''
}

async function confirmPresence () {
  if (!selectedGuests.value.length) {
    return
  }

  saving.value = true

  try {
    await api.post('/guests/confirm', {
      guests: selectedGuests.value.map(name => ({
        name
        // godparent: false por padrão no backend
      }))
    })

    dialog.value = false
    selectedGuests.value = []
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
  background: linear-gradient(135deg, #6b7a3a00, #87964b00);
  color: #fdfaf4;
  font-size: 0.8rem;
  padding: 6px 12px;
  border-radius: 999px;
  border: 1px solid #6b7a3a;
  color: #6b7a3a;
  font-weight: 500;
  letter-spacing: 0.04em;
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
