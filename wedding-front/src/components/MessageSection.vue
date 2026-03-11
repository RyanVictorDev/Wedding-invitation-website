<template>
  <section ref="messageSection" class="message-section">
    <div class="message-header" data-reveal>
      <p class="message-kicker">Recado</p>
      <h2 class="message-title">Deixe um recado</h2>

      <div class="message-divider">
        <span class="line"></span>
        <span class="heart">✧</span>
        <span class="line"></span>
      </div>
    </div>

    <div class="d-flex justify-center">
      <div class="message-card q-pa-md q-pa-xl-md">
        <form @submit.prevent="submitMessage">
          <input
            v-model="website"
            type="text"
            name="website"
            autocomplete="off"
            class="message-honeypot"
          >

          <div class="message-name-row">
            <q-input
              v-model="authorName"
              label="Seu nome (opcional)"
              outlined
              dense
              class="message-name-input"
            />
          </div>

          <q-editor
            v-model="editor"
            min-height="12rem"
            toolbar-rounded
            flat
            placeholder="Escreva aqui seu recado especial para os noivos ✧"
          />

          <div class="message-actions">
            <q-btn
              type="submit"
              color="primary"
              unelevated
              :loading="saving"
              :disable="!editor || contentTooLong"
              label="Enviar recado"
              class="message-submit-btn"
            />
          </div>

          <div class="message-counter">
            {{ contentLength }} / {{ maxLength }} caracteres
          </div>

          <q-slide-transition>
            <div v-if="success" class="message-success">
              Obrigado pelo carinho! Seu recado foi enviado.
            </div>
          </q-slide-transition>

          <q-slide-transition>
            <div v-if="error" class="message-error">
              {{ errorMessage }}
            </div>
          </q-slide-transition>
        </form>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { api } from 'src/boot/axios'

const messageSection = ref<HTMLElement | null>(null)
const editor = ref('')
const authorName = ref('')
const website = ref('')
const saving = ref(false)
const success = ref(false)
const error = ref(false)
const errorMessage = ref('Ops, algo deu errado ao enviar o recado. Tente novamente em alguns instantes.')

const maxLength = 10000
const contentLength = computed(() => editor.value.length)
const contentTooLong = computed(() => contentLength.value > maxLength)

async function submitMessage () {
  if (!editor.value) {
    return
  }

  if (contentTooLong.value) {
    errorMessage.value = 'Seu recado está muito longo. Reduza o texto antes de enviar.'
    error.value = true
    return
  }

  saving.value = true
  success.value = false
  error.value = false

  try {
    await api.post('/messages', {
      content: editor.value,
      authorName: authorName.value || null,
      website: website.value
    })
    success.value = true
    editor.value = ''
    authorName.value = ''
    website.value = ''
  } catch (e: unknown) {
    const status = (e as { response?: { status?: number } } | undefined)?.response?.status
    if (status === 400) {
      errorMessage.value = 'Detectamos um envio inválido. Por favor, tente novamente.'
    } else if (status === 429) {
      errorMessage.value = 'Por favor, aguarde alguns segundos antes de enviar outro recado.'
    } else {
      errorMessage.value = 'Ops, algo deu errado ao enviar o recado. Tente novamente em alguns instantes.'
    }
    console.error('Erro ao salvar recado', e)
    error.value = true
  } finally {
    saving.value = false
    setTimeout(() => {
      success.value = false
      error.value = false
    }, 5000)
  }
}

function scrollToSection () {
  messageSection.value?.scrollIntoView({ behavior: 'smooth' })
}

defineExpose({
  scrollToSection
})
</script>

<style scoped lang="scss">
.message-section {
  // background: linear-gradient(to top, rgba(236, 207, 192, 0.98), #fdf3ec);
  background: #fdf3ec;
  padding: 64px 16px 88px;
}

.message-card {
  width: 100%;
  max-width: 720px;
  margin: 0 auto;
  background: rgba(255, 248, 244, 0.96);
  border-radius: 18px;
  box-shadow: 0 16px 36px rgba(90, 51, 45, 0.18);
  border: 1px solid rgba(200, 107, 90, 0.22);
}

.message-header {
  text-align: center;
  margin-bottom: 40px;
}

.message-name-row {
  margin-bottom: 12px;
}

.message-name-input {
  .q-field__control {
    border-radius: 999px;
  }
}

.message-honeypot {
  display: none;
}

.message-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #c86b5a;
  margin-bottom: 10px;
}

.message-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 2.1rem;
  color: #5a332d;
  margin-bottom: 12px;
}

.message-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.message-divider .line {
  width: 52px;
  height: 1px;
  background: linear-gradient(to right, #e0b79c, #c86b5a);
}

.message-divider .heart {
  font-size: 0.9rem;
  color: #c86b5a;
}

.message-grid {
  display: flex;
  align-content: center;
}

.message-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.message-counter {
  margin-top: 6px;
  font-size: 0.8rem;
  color: #7b5a4c;
  text-align: right;
}

.message-submit-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  padding-inline: 24px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 0.8rem;
}

.message-submit-btn:hover {
  background: linear-gradient(135deg, #5a6a30, #76853e);
}

.message-success,
.message-error {
  margin-top: 14px;
  font-size: 0.9rem;
  border-radius: 12px;
  padding: 10px 14px;
}

.message-success {
  background: #e6f4e8;
  color: #295b31;
  border: 1px solid #a2d6a4;
}

.message-error {
  background: #fdecea;
  color: #8a1c1c;
  border: 1px solid #f5b5b5;
}

@media (min-width: 768px) {
  .message-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
</style>
