<template>
  <q-page class="dashboard-page q-pa-md q-pa-xl-md">
    <DashboardLoginForm
      v-if="!isAuthenticated"
      @logged-in="handleLoggedIn"
    />

    <div
      v-else
      class="dashboard-card q-pa-lg"
    >
      <div class="dashboard-header">
        <div>
          <p class="dashboard-kicker">Dashboard secreta</p>
          <h1 class="dashboard-title">Visão geral do casamento</h1>
        </div>
        <div class="dashboard-actions">
          <span v-if="username" class="dashboard-user">Olá, {{ username }}</span>
          <q-btn flat label="Sair" class="logout-btn" @click="handleLogout" />
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
          :total-elements="guestTotalElements"
          :search="guestSearch"
          :filter="guestFilter"
          :format-date-time="formatDateTime"
          @update:page="value => { guestPage = value }"
          @update:filter="value => { guestFilter = value }"
          @search="handleGuestSearch"
          @refresh="loadGuests"
        />

        <DashboardGiftsSection />

        <DashboardMessagesSection
          :messages="messages"
          :loading="messagesLoading"
          :page="messagesPage"
          :total-pages="messagesTotalPages"
          :format-date-time="formatDateTime"
          @update:page="value => { messagesPage = value }"
        />

        <DashboardUsersSection />
      </div>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { api } from 'src/boot/axios'
import { useAuth } from 'src/composables/useAuth'
import DashboardLoginForm from 'components/dashboard/DashboardLoginForm.vue'
import DashboardStatsSection from 'components/dashboard/DashboardStatsSection.vue'
import DashboardGuestSection from 'components/dashboard/DashboardGuestSection.vue'
import DashboardGiftsSection from 'components/dashboard/DashboardGiftsSection.vue'
import DashboardMessagesSection from 'components/dashboard/DashboardMessagesSection.vue'
import DashboardUsersSection from 'components/dashboard/DashboardUsersSection.vue'

interface GuestSummary {
  id: number
  name: string
  confirmed: boolean
  godparent: boolean
  responded: boolean
  confirmationDate: string | null
}

interface DashboardSummaryResponse {
  totalGuests: number
  confirmedGuests: number
  unconfirmedGuests: number
  pendingGuests: number
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

const { isAuthenticated, username, logout } = useAuth()

const loading = ref(true)
const summary = ref<DashboardSummaryResponse | null>(null)

const guestSearch = ref('')
const guestFilter = ref<'all' | 'pending' | 'yes' | 'no'>('all')
const guestPage = ref(1)
const guestPageSize = ref(10)
const guestRows = ref<GuestSummary[]>([])
const guestsLoading = ref(false)
const guestTotalPages = ref(1)
const guestTotalElements = ref(0)

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

    const { data } = await api.get<PagedGuestsResponse>('/guests', {
      params: {
        page,
        size,
        ...(guestSearch.value.trim() ? { search: guestSearch.value.trim() } : {}),
        ...(guestFilter.value !== 'all' ? { status: guestFilter.value } : {})
      }
    })

    guestRows.value = data.content ?? []
    guestTotalElements.value = data.totalElements ?? 0
    guestTotalPages.value = Math.max(1, data.totalPages ?? 1)
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

function handleLoggedIn () {
  loading.value = true
  void loadDashboardData()
}

function handleLogout () {
  logout()
  summary.value = null
  guestRows.value = []
  messages.value = []
}

async function loadDashboardData () {
  loading.value = true
  await Promise.all([
    loadSummary(),
    loadGuests(),
    loadMessages()
  ])
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
  if (isAuthenticated.value) {
    void loadDashboardData()
  } else {
    loading.value = false
  }
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

.dashboard-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dashboard-user {
  color: #7b5a4c;
  font-size: 0.9rem;
}

.logout-btn {
  color: #a2503b;
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
</style>
