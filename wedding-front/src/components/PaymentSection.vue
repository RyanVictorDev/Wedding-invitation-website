<template>
  <section class="payment-section" data-reveal>
    <div class="payment-header">
      <p class="payment-kicker">Presentes</p>
      <h2 class="payment-title">Presenteie os noivos</h2>

      <div class="payment-divider">
        <span class="line"></span>
        <span class="heart">✧</span>
        <span class="line"></span>
      </div>
    </div>

    <div class="d-flex">
      <div class="payment-card q-pa-md q-pa-xl-md">
        <div class="payment-methods">
          <q-option-group
            v-model="paymentMethod"
            :options="methodOptions"
            color="primary"
            type="radio"
          />
        </div>

        <div class="payment-form q-mt-md">
          <div class="row q-col-gutter-md">
            <div class="col-12 col-sm-6">
              <q-input
                v-model.number="amount"
                type="number"
                min="1"
                step="1"
                label="Valor do presente (R$)"
                outlined
                dense
              />
            </div>
            <div class="col-12 col-sm-6">
              <q-input
                v-model="payerName"
                label="Seu nome (opcional)"
                outlined
                dense
              />
            </div>
          </div>

          <div class="payment-actions">
            <q-btn
              color="primary"
              unelevated
              :loading="creating"
              :disable="!canCreatePayment"
              label="Gerar pagamento"
              class="payment-submit-btn"
              @click="createPayment"
            />
          </div>
        </div>

        <q-slide-transition>
          <div
            v-if="pixState.pixId"
            class="payment-result q-mt-lg"
          >
            <div class="payment-result-title">
              Escaneie o QRCode Pix ou copie o código
            </div>

            <div class="row q-col-gutter-lg items-center">
              <div class="col-12 col-md-5 text-center">
                <img
                  v-if="pixState.brCodeBase64"
                  :src="pixState.brCodeBase64"
                  alt="QR Code Pix"
                  class="pix-qrcode"
                >
              </div>
              <div class="col-12 col-md-7">
                <q-input
                  v-model="pixState.brCode"
                  type="textarea"
                  readonly
                  autogrow
                  outlined
                  label="Código copia-e-cola"
                  dense
                >
                  <template #append>
                    <q-btn
                      flat
                      dense
                      label="Copiar"
                      @click="copyBrCode"
                    />
                  </template>
                </q-input>

                <div class="payment-status q-mt-md">
                  <div class="status-label">
                    Status:
                    <span :class="['status-chip', `status-chip--${pixState.status.toLowerCase()}`]">
                      {{ statusLabel }}
                    </span>
                  </div>

                  <div
                    v-if="isPaid"
                    class="status-success"
                  >
                    Pagamento confirmado! Muito obrigado pelo carinho.
                  </div>

                  <div
                    v-else-if="isWaiting"
                    class="status-waiting"
                  >
                    Aguardando o pagamento... Assim que o PIX for confirmado, atualizaremos aqui.
                  </div>
                </div>
              </div>
            </div>
          </div>
        </q-slide-transition>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { api } from 'src/boot/axios'

type PaymentMethod = 'PIX'

interface PixPaymentResponse {
  paymentId: number
  pixId: string
  status: string
  brCode: string
  brCodeBase64: string
}

interface PixStatusResponse {
  status: string
  expiresAt: string
}

const paymentMethod = ref<PaymentMethod>('PIX')
const amount = ref<number | null>(100)
const payerName = ref('')
const creating = ref(false)

const pixState = ref<PixPaymentResponse>({
  paymentId: 0,
  pixId: '',
  status: '',
  brCode: '',
  brCodeBase64: ''
})

const pollingTimer = ref<number | null>(null)

const methodOptions = [
  {
    label: 'Pix',
    value: 'PIX'
  }
]

const canCreatePayment = computed(() => {
  return !!amount.value && amount.value >= 1 && paymentMethod.value === 'PIX'
})

const isPaid = computed(() => pixState.value.status === 'PAID')
const isWaiting = computed(() => pixState.value.status === 'PENDING')

const statusLabel = computed(() => {
  switch (pixState.value.status) {
    case 'PAID': return 'Pago'
    case 'PENDING': return 'Pendente'
    case 'EXPIRED': return 'Expirado'
    case 'CANCELLED': return 'Cancelado'
    case 'REFUNDED': return 'Estornado'
    default: return pixState.value.status || '—'
  }
})

async function createPayment () {
  if (!canCreatePayment.value || !amount.value) {
    return
  }

  creating.value = true
  stopPolling()

  try {
    const payload = {
      amount: amount.value,
      currency: 'BRL',
      payerName: payerName.value || 'Convidado',
      description: 'Presente de casamento'
    }

    const { data } = await api.post<PixPaymentResponse>('/payments/pix', payload)
    pixState.value = data

    if (pixState.value.pixId) {
      startPolling()
    }
  } catch (e) {
    console.error('Erro ao criar pagamento PIX', e)
  } finally {
    creating.value = false
  }
}

async function pollStatus () {
  if (!pixState.value.pixId) {
    return
  }

  try {
    const { data } = await api.get<PixStatusResponse>(`/payments/pix/${pixState.value.pixId}/status`)
    if (data.status) {
      pixState.value.status = data.status
    }

    if (isPaid.value || pixState.value.status === 'EXPIRED' || pixState.value.status === 'CANCELLED' || pixState.value.status === 'REFUNDED') {
      stopPolling()
    }
  } catch (e) {
    console.error('Erro ao checar status do PIX', e)
  }
}

function startPolling () {
  stopPolling()
  // Checa a cada 5 segundos
  pollingTimer.value = window.setInterval(() => {
    void pollStatus()
  }, 5000)
}

function stopPolling () {
  if (pollingTimer.value !== null) {
    window.clearInterval(pollingTimer.value)
    pollingTimer.value = null
  }
}

async function copyBrCode () {
  if (!pixState.value.brCode) return
  try {
    await navigator.clipboard.writeText(pixState.value.brCode)
  } catch (e) {
    console.error('Erro ao copiar código PIX', e)
  }
}

onBeforeUnmount(() => {
  stopPolling()
})
</script>

<style scoped lang="scss">
.payment-section {
  background: #fff8f4;
  padding: 64px 16px 72px;
}

.payment-header {
  text-align: center;
  margin-bottom: 32px;
}

.payment-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #6b7a3a;
  margin-bottom: 8px;
}

.payment-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 2.1rem;
  color: #5a332d;
  margin-bottom: 10px;
}

.payment-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.payment-divider .line {
  width: 52px;
  height: 1px;
  background: linear-gradient(to right, #e0b79c, #6b7a3a);
}

.payment-divider .heart {
  font-size: 0.9rem;
  color: #6b7a3a;
}

.payment-card {
  width: 100%;
  max-width: 860px;
  margin: 0 auto;
  background: rgba(255, 250, 245, 0.98);
  border-radius: 20px;
  box-shadow: 0 18px 40px rgba(90, 51, 45, 0.18);
  border: 1px solid rgba(200, 107, 90, 0.18);
}

.payment-methods {
  border-bottom: 1px solid rgba(200, 107, 90, 0.15);
  padding-bottom: 8px;
}

.payment-form {
  margin-top: 12px;
}

.payment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.payment-submit-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  padding-inline: 26px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  font-size: 0.8rem;
}

.payment-submit-btn:hover {
  background: linear-gradient(135deg, #5a6a30, #76853e);
}

.payment-result-title {
  font-size: 0.95rem;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #a2503b;
  margin-bottom: 16px;
}

.pix-qrcode {
  max-width: 220px;
  width: 100%;
  border-radius: 16px;
  background: #ffffff;
  padding: 12px;
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.payment-status {
  font-size: 0.9rem;
}

.status-label {
  margin-bottom: 6px;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  margin-left: 6px;
  border: 1px solid rgba(90, 51, 45, 0.14);
  background: #fff;
}

.status-chip--paid {
  background: #e6f4e8;
  color: #295b31;
  border-color: #a2d6a4;
}

.status-chip--pending {
  background: #fff6e5;
  color: #8a5a1c;
  border-color: #f3cf91;
}

.status-chip--expired,
.status-chip--cancelled,
.status-chip--refunded {
  background: #fdecea;
  color: #8a1c1c;
  border-color: #f5b5b5;
}

.status-success {
  margin-top: 6px;
  color: #295b31;
}

.status-waiting {
  margin-top: 6px;
  color: #8a5a1c;
}
</style>

