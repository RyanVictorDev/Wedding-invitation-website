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
        <DashboardStatsSection
          :summary="summary"
          :formatted-total-payments="formattedTotalPayments"
          :formatted-paid-total="formattedPaidTotal"
          :formatted-pending-total="formattedPendingTotal"
        />

        <DashboardGuestSection
          :rows="guestRows"
          :loading="guestsLoading"
          :page="guestPage"
          :total-pages="guestTotalPages"
          :search="guestSearch"
          :filter="guestFilter"
          :format-date-time="formatDateTime"
          @update:page="value => { guestPage = value }"
          @update:filter="value => { guestFilter = value }"
          @search="handleGuestSearch"
        />

        <DashboardMessagesSection
          :messages="messages"
          :loading="messagesLoading"
          :page="messagesPage"
          :total-pages="messagesTotalPages"
          :format-date-time="formatDateTime"
          @update:page="value => { messagesPage = value }"
        />
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { api } from 'src/boot/axios'
import DashboardStatsSection from 'components/dashboard/DashboardStatsSection.vue'
import DashboardGuestSection from 'components/dashboard/DashboardGuestSection.vue'
import DashboardMessagesSection from 'components/dashboard/DashboardMessagesSection.vue'

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

interface PagedGuestsResponse {
  content: GuestSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
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

const guestSearch = ref('')
const guestFilter = ref<'all' | 'yes' | 'no'>('all')
const guestPage = ref(1)
const guestPageSize = ref(10)
const guestRows = ref<GuestSummary[]>([])
const guestsLoading = ref(false)
const guestTotalPages = ref(1)

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

async function loadGuests () {
  guestsLoading.value = true
  try {
    const page = guestPage.value - 1
    const size = guestPageSize.value

    const params: Record<string, unknown> = { page, size }

    if (guestSearch.value.trim()) {
      params.search = guestSearch.value.trim()
    }

    if (guestFilter.value !== 'all') {
      params.status = guestFilter.value
    }

    const { data } = await api.get<PagedGuestsResponse>('/guests', {
      params
    })

    guestRows.value = data.content
    guestTotalPages.value = data.totalPages || 1
  } catch (e) {
    console.error('Erro ao carregar convidados', e)
  } finally {
    guestsLoading.value = false
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

function handleGuestSearch (term: string) {
  guestSearch.value = term
  guestPage.value = 1
  void loadGuests()
}

watch(guestFilter, () => {
  guestPage.value = 1
  void loadGuests()
})

watch(guestPage, () => {
  void loadGuests()
})

watch(messagesPage, () => {
  void loadMessages()
})

onMounted(() => {
  void loadSummary()
  void loadMessages()
  void loadGuests()
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

.guest-filters {
  align-items: center;
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
  background-color: #6b7a3a;
  color: #fdfaf4;
  border-radius: 999px;
}

.messages-page-indicator {
  text-align: center;
  font-size: 0.8rem;
  color: #7b5a4c;
}
</style>


