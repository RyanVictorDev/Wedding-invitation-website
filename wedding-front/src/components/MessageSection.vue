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
        <q-editor
          v-model="editor"
          min-height="12rem"
          toolbar-rounded
          flat
          placeholder="Escreva aqui seu recado especial para os noivos ✧"
        />

        <div class="message-actions">
          <q-btn
            color="primary"
            unelevated
            :loading="saving"
            :disable="!editor"
            label="Enviar recado"
            class="message-submit-btn"
            @click="submitMessage"
          />
        </div>

        <q-slide-transition>
          <div v-if="success" class="message-success">
            Obrigado pelo carinho! Seu recado foi enviado.
          </div>
        </q-slide-transition>

        <q-slide-transition>
          <div v-if="error" class="message-error">
            Ops, algo deu errado ao enviar o recado. Tente novamente em alguns instantes.
          </div>
        </q-slide-transition>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from 'src/boot/axios'

const detailsSection = ref<HTMLElement | null>(null)
const editor = ref('')
const saving = ref(false)
const success = ref(false)
const error = ref(false)

async function submitMessage () {
  if (!editor.value) {
    return
  }

  saving.value = true
  success.value = false
  error.value = false

  try {
    await api.post('/messages', {
      content: editor.value
    })
    success.value = true
    editor.value = ''
  } catch (e) {
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
  detailsSection.value?.scrollIntoView({ behavior: 'smooth' })
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
