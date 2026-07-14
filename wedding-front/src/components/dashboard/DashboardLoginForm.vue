<template>
  <div class="login-wrapper">
    <div class="login-card q-pa-lg">
      <p class="login-kicker">Dashboard secreta</p>
      <h1 class="login-title">Entrar</h1>
      <p class="login-subtitle">Informe suas credenciais para acessar o painel.</p>

      <q-form class="q-mt-lg" @submit.prevent="handleSubmit">
        <q-input
          v-model="username"
          label="Usuário"
          outlined
          dense
          class="q-mb-md"
          :disable="loading"
        />
        <q-input
          v-model="password"
          label="Senha"
          type="password"
          outlined
          dense
          :disable="loading"
        />

        <div
          v-if="errorMessage"
          class="login-error q-mt-md"
        >
          {{ errorMessage }}
        </div>

        <div class="login-actions q-mt-lg">
          <q-btn
            unelevated
            type="submit"
            label="Entrar"
            class="login-btn"
            :loading="loading"
          />
        </div>
      </q-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuth } from 'src/composables/useAuth'

const emit = defineEmits<{
  (e: 'logged-in'): void
}>()

const { login } = useAuth()

const username = ref('')
const password = ref('')
const loading = ref(false)
const errorMessage = ref('')

async function handleSubmit () {
  errorMessage.value = ''
  loading.value = true

  try {
    await login(username.value.trim(), password.value)
    emit('logged-in')
  } catch (err: unknown) {
    const axiosLike = err as { message?: string; code?: string; response?: { status?: number } }
    const status = axiosLike.response?.status

    if (!status || axiosLike.code === 'ERR_NETWORK' || axiosLike.message === 'Network Error') {
      errorMessage.value = 'Não foi possível conectar ao servidor. Tente novamente.'
    } else if (status === 401 || status === 403) {
      errorMessage.value = 'Usuário ou senha inválidos.'
    } else {
      errorMessage.value = 'Não foi possível entrar. Tente novamente.'
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-wrapper {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 250, 245, 0.98);
  border-radius: 20px;
  box-shadow: 0 18px 40px rgba(90, 51, 45, 0.22);
  border: 1px solid rgba(200, 107, 90, 0.18);
}

.login-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #6b7a3a;
  margin-bottom: 8px;
}

.login-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 1.8rem;
  color: #5a332d;
}

.login-subtitle {
  color: #7b5a4c;
  font-size: 0.92rem;
}

.login-error {
  color: #8a1c1c;
  background: #fdecea;
  border: 1px solid #f5b5b5;
  border-radius: 10px;
  padding: 8px 12px;
  font-size: 0.88rem;
}

.login-actions {
  display: flex;
  justify-content: center;
}

.login-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  padding-inline: 28px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 0.8rem;
}
</style>
