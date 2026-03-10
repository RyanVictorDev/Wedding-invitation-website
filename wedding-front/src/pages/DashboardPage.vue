<template>
  <q-page class="dashboard-page q-pa-md q-pa-xl-md">
    <div class="dashboard-card q-pa-lg">
      <div class="dashboard-header">
        <div>
          <p class="dashboard-kicker">Dashboard secreta</p>
          <h1 class="dashboard-title">Visão geral do casamento</h1>
        </div>
      </div>

      <q-separator spaced />

      <div v-if="loading" class="text-center q-my-xl">
        <q-spinner color="primary" size="2.5em" />
      </div>

      <div v-else>
        <div class="row q-col-gutter-lg">
          <div class="col-12 col-md-3">
            <div class="stat-card">
              <div class="stat-label">Convidados</div>
              <div class="stat-value">{{ summary?.totalGuests ?? 0 }}</div>
            </div>
          </div>

          <div class="col-12 col-md-3">
            <div class="stat-card">
              <div class="stat-label">Confirmados</div>
              <div class="stat-value text-positive">{{ summary?.confirmedGuests ?? 0 }}</div>
            </div>
          </div>

          <div class="col-12 col-md-3">
            <div class="stat-card">
              <div class="stat-label">Não confirmados</div>
              <div class="stat-value text-negative">{{ summary?.unconfirmedGuests ?? 0 }}</div>
            </div>
          </div>

          <div class="col-12 col-md-3">
            <div class="stat-card">
              <div class="stat-label">Padrinhos / Madrinhas</div>
              <div class="stat-value">{{ summary?.godparents ?? 0 }}</div>
            </div>
          </div>
        </div>

        <div class="row q-col-gutter-lg q-mt-lg">
          <div class="col-12 col-md-4">
            <div class="stat-card stat-card--highlight">
              <div class="stat-label">Total recebido (pagos)</div>
              <div class="stat-value">
                R$ {{ formattedPaidTotal }}
              </div>
              <div class="stat-sub">
                {{ summary?.paidPaymentsCount ?? 0 }} pagamentos pagos
              </div>
            </div>
          </div>

          <div class="col-12 col-md-4">
            <div class="stat-card">
              <div class="stat-label">Pagamentos pendentes</div>
              <div class="stat-value">
                R$ {{ formattedPendingTotal }}
              </div>
              <div class="stat-sub">
                {{ summary?.pendingPaymentsCount ?? 0 }} pagamentos pendentes
              </div>
            </div>
          </div>

          <div class="col-12 col-md-4">
            <div class="stat-card">
              <div class="stat-label">Total geral (todos pagamentos)</div>
              <div class="stat-value">
                R$ {{ formattedTotalPayments }}
              </div>
              <div class="stat-sub">
                {{ summary?.totalPaymentsCount ?? 0 }} pagamentos no total
              </div>
            </div>
          </div>
        </div>

        <div class="q-mt-xl">
          <div class="section-header">
            <h2 class="section-title">Lista de convidados</h2>
            <div class="section-subtitle">
              Quem já confirmou presença e quem ainda não.
            </div>
          </div>

          <q-table
            v-if="guestRows.length"
            flat
            bordered
            :rows="guestRows"
            :columns="columns"
            row-key="id"
            hide-bottom
            class="guest-table"
          >
            <template #body-cell-confirmed="props">
              <q-td :props="props">
                <q-chip
                  :color="props.row.confirmed ? 'positive' : 'grey-6'"
                  text-color="white"
                  dense
                  square
                >
                  {{ props.row.confirmed ? 'Confirmado' : 'Não confirmado' }}
                </q-chip>
              </q-td>
            </template>

            <template #body-cell-godparent="props">
              <q-td :props="props">
                <q-chip
                  v-if="props.row.godparent"
                  color="orange-7"
                  text-color="white"
                  dense
                  square
                >
                  Padrinho / Madrinha
                </q-chip>
                <span v-else>Não</span>
              </q-td>
            </template>

            <template #body-cell-confirmationDate="props">
              <q-td :props="props">
                <span v-if="props.row.confirmationDate">
                  {{ formatDateTime(props.row.confirmationDate) }}
                </span>
                <span v-else>—</span>
              </q-td>
            </template>
          </q-table>

          <div v-else class="text-center q-mt-md text-grey-7">
            Nenhum convidado cadastrado ainda.
          </div>
        </div>

        <div class="q-mt-xl">
          <div class="section-header">
            <h2 class="section-title">Recados</h2>
            <div class="section-subtitle">
              Mensagens deixadas pelos convidados.
            </div>
          </div>

          <div v-if="messagesLoading" class="text-center q-my-md">
            <q-spinner size="2em" color="primary" />
          </div>

          <div v-else>
            <div
              v-if="messages.length"
              class="messages-list"
            >
              <div
                v-for="msg in messages"
                :key="msg.id"
                class="message-card"
              >
                <div class="message-header-row">
                  <div class="message-author">
                    {{ msg.authorName || 'Convidado' }}
                  </div>
                  <div class="message-date">
                    {{ formatDateTime(msg.createdAt) }}
                  </div>
                </div>
                <div
                  class="message-content"
                  v-html="msg.content"
                />
              </div>

              <div class="messages-pagination q-mt-md">
                <q-btn
                  round
                  unelevated
                  color="white"
                  text-color="brown-7"
                  icon="chevron_left"
                  :disable="messagesPage === 1 || messagesLoading"
                  @click="handlePrevMessages"
                />

                <div class="messages-page-numbers">
                  <q-btn
                    v-for="page in messagesTotalPages"
                    :key="page"
                    :label="page"
                    outline
                    rounded
                    color="brown-5"
                    class="messages-page-number"
                    :class="{ 'messages-page-number--active': page === messagesPage }"
                    :disable="messagesLoading"
                    @click="handleGoToMessagesPage(page)"
                  />
                </div>

                <q-btn
                  round
                  unelevated
                  color="white"
                  text-color="brown-7"
                  icon="chevron_right"
                  :disable="messagesPage >= messagesTotalPages || messagesLoading"
                  @click="handleNextMessages"
                />
              </div>
            </div>

            <div v-else class="text-center q-mt-md text-grey-7">
              Nenhum recado enviado ainda.
            </div>
          </div>
        </div>
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { api } from 'src/boot/axios'

interface GuestSummary {
  id: number
  name: string
  confirmed: boolean
  godparent: boolean
  confirmationDate: string | null
}

interface DashboardSummaryResponse {
  totalGuests: number
  confirmedGuests: number
  unconfirmedGuests: number
  godparents: number
  totalPayments: number
  totalPaymentsCount: number
  paidPaymentsTotal: number
  paidPaymentsCount: number
  pendingPaymentsTotal: number
  pendingPaymentsCount: number
  guests: GuestSummary[]
}

interface Message {
  id: number
  content: string
  authorName: string | null
  createdAt: string
}

interface PagedMessagesResponse {
  content: Message[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

const loading = ref(true)
const summary = ref<DashboardSummaryResponse | null>(null)

const columns = [
  { name: 'name', label: 'Nome', field: 'name', align: 'left' as const },
  { name: 'confirmed', label: 'Presença', field: 'confirmed', align: 'left' as const },
  { name: 'godparent', label: 'Padrinho/Madrinha', field: 'godparent', align: 'left' as const },
  { name: 'confirmationDate', label: 'Data de confirmação', field: 'confirmationDate', align: 'left' as const }
]

const guestRows = computed(() => summary.value?.guests ?? [])

const formattedTotalPayments = computed(() => {
  const value = summary.value?.totalPayments ?? 0
  return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

const formattedPaidTotal = computed(() => {
  const value = summary.value?.paidPaymentsTotal ?? 0
  return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

const formattedPendingTotal = computed(() => {
  const value = summary.value?.pendingPaymentsTotal ?? 0
  return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
})

const messages = ref<Message[]>([])
const messagesPage = ref(1)
const messagesPageSize = ref(5)
const messagesLoading = ref(false)
const messagesTotalPages = ref(1)

function formatDateTime (value: string) {
  const date = new Date(value)
  return date.toLocaleString('pt-BR')
}

async function loadSummary () {
  try {
    const { data } = await api.get<DashboardSummaryResponse>('/dashboard/summary')
    summary.value = data
  } catch (e) {
    console.error('Erro ao carregar dashboard', e)
  } finally {
    loading.value = false
  }
}

async function loadMessages () {
  messagesLoading.value = true
  try {
    const page = messagesPage.value - 1
    const size = messagesPageSize.value
    const { data } = await api.get<PagedMessagesResponse>('/messages', {
      params: { page, size }
    })
    messages.value = data.content
    messagesTotalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('Erro ao carregar recados', e)
  } finally {
    messagesLoading.value = false
  }
}

function handlePrevMessages () {
  if (messagesPage.value === 1 || messagesLoading.value) return
  messagesPage.value -= 1
  void loadMessages()
}

function handleNextMessages () {
  if (messagesPage.value >= messagesTotalPages.value || messagesLoading.value) return
  messagesPage.value += 1
  void loadMessages()
}

function handleGoToMessagesPage (page: number) {
  if (page === messagesPage.value || messagesLoading.value) return
  messagesPage.value = page
  void loadMessages()
}

onMounted(() => {
  void loadSummary()
  void loadMessages()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  background: radial-gradient(circle at top left, #f4f7ec, #fff8f4);
  min-height: 100vh;
  display: flex;
  justify-content: center;
}

.dashboard-card {
  width: 100%;
  max-width: 1080px;
  background: rgba(255, 250, 245, 0.98);
  border-radius: 20px;
  box-shadow: 0 18px 40px rgba(90, 51, 45, 0.22);
  border: 1px solid rgba(200, 107, 90, 0.18);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.dashboard-kicker {
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 0.78rem;
  color: #6b7a3a;
  margin-bottom: 8px;
}

.dashboard-title {
  font-family: 'Playfair Display', 'Cormorant Garamond', serif;
  font-size: 1.8rem;
  color: #5a332d;
}

.stat-card {
  background: #fff9f4;
  border-radius: 14px;
  padding: 14px 16px;
  border: 1px solid rgba(200, 107, 90, 0.18);
}

.stat-card--highlight {
  background: linear-gradient(135deg, #f4f7ec, #fff9f4);
}

.stat-label {
  font-size: 0.8rem;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: #a2503b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 1.6rem;
  font-weight: 600;
  color: #5a332d;
}

.stat-sub {
  margin-top: 4px;
  font-size: 0.85rem;
  color: #6b7a3a;
}

.section-header {
  margin-bottom: 8px;
}

.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #5a332d;
}

.section-subtitle {
  font-size: 0.9rem;
  color: #7b5a4c;
}

.guest-table {
  margin-top: 8px;
  border-radius: 14px;
  overflow: hidden;
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-card {
  background: #fff9f4;
  border-radius: 14px;
  padding: 12px 14px;
  border: 1px solid rgba(200, 107, 90, 0.18);
}

.message-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.message-author {
  font-weight: 600;
  color: #5a332d;
}

.message-date {
  font-size: 0.8rem;
  color: #8b6a60;
}

.message-content {
  font-size: 0.9rem;
  color: #4b2b28;
}

.messages-pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.messages-page-numbers {
  display: flex;
  gap: 4px;
}

.messages-page-number {
  min-width: 32px;
  padding: 0 6px;
  font-size: 0.78rem;
}

.messages-page-number--active {
  font-weight: 600;
}
</style>


