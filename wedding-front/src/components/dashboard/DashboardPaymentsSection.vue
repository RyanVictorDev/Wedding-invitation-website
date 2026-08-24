<template>
  <div class="q-mt-xl">
    <div class="section-header row items-center justify-between q-col-gutter-sm">
      <div class="col-12 col-md">
        <h2 class="section-title">Pagamentos</h2>
        <div class="section-subtitle">
          Veja quem enviou presentes e o status de cada pagamento.
        </div>
      </div>
      <div class="col-12 col-md-auto">
        <q-tabs
          v-model="filterModel"
          dense
          align="right"
          active-color="brown-7"
          indicator-color="brown-5"
          class="payment-filter-tabs"
        >
          <q-tab name="paid" label="Pagos" />
          <q-tab name="pending" label="Pendentes" />
          <q-tab name="all" label="Todos" />
        </q-tabs>
      </div>
    </div>

    <q-table
      v-if="rows.length"
      flat
      bordered
      :rows="rows"
      :columns="columns"
      row-key="id"
      hide-bottom
      :pagination="{ rowsPerPage: 0 }"
      class="payment-table q-mt-md"
      :loading="loading"
    >
      <template #body-cell-payerName="slotProps">
        <q-td :props="slotProps">
          <span class="payer-name">{{ slotProps.row.payerName }}</span>
        </q-td>
      </template>

      <template #body-cell-amount="slotProps">
        <q-td :props="slotProps">
          <span class="payment-amount">
            R$ {{ formatAmount(slotProps.row.amount) }}
          </span>
        </q-td>
      </template>

      <template #body-cell-status="slotProps">
        <q-td :props="slotProps">
          <q-chip
            :color="statusColor(slotProps.row.status)"
            text-color="white"
            dense
            square
          >
            {{ statusLabel(slotProps.row.status) }}
          </q-chip>
        </q-td>
      </template>

      <template #body-cell-description="slotProps">
        <q-td :props="slotProps">
          <span v-if="slotProps.row.description">{{ slotProps.row.description }}</span>
          <span v-else>—</span>
        </q-td>
      </template>

      <template #body-cell-createdAt="slotProps">
        <q-td :props="slotProps">
          {{ formatDateTime(slotProps.row.createdAt) }}
        </q-td>
      </template>
    </q-table>

    <div v-else-if="!loading" class="text-center q-mt-md text-grey-7">
      Nenhum pagamento encontrado.
    </div>

    <div v-if="loading" class="text-center q-my-md">
      <q-spinner size="2em" color="primary" />
    </div>

    <div
      v-if="rows.length || totalElements > 0"
      class="payment-pagination-wrap q-mt-md"
    >
      <DashboardPagination
        v-model="pageModel"
        :total-pages="totalPages"
        :loading="loading"
      />
      <div class="payment-page-indicator q-mt-sm">
        Página {{ page }} de {{ totalPages }}
        <span v-if="totalElements"> · {{ totalElements }} pagamento{{ totalElements === 1 ? '' : 's' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DashboardPagination from 'components/dashboard/DashboardPagination.vue'

type PaymentStatus = 'CREATED' | 'PENDING' | 'PAID' | 'FAILED'
type PaymentFilter = 'paid' | 'pending' | 'all'

interface PaymentRow {
  id: number
  amount: number
  currency: string
  payerName: string
  description: string | null
  status: PaymentStatus
  createdAt: string
}

const props = defineProps<{
  rows: PaymentRow[]
  loading: boolean
  page: number
  totalPages: number
  totalElements: number
  filter: PaymentFilter
  formatDateTime: (value: string) => string
}>()

const emit = defineEmits<{
  (e: 'update:page', value: number): void
  (e: 'update:filter', value: PaymentFilter): void
}>()

const columns = [
  { name: 'payerName', label: 'Quem enviou', field: 'payerName', align: 'left' as const },
  { name: 'amount', label: 'Valor', field: 'amount', align: 'left' as const },
  { name: 'status', label: 'Status', field: 'status', align: 'left' as const },
  { name: 'description', label: 'Descrição', field: 'description', align: 'left' as const },
  { name: 'createdAt', label: 'Data', field: 'createdAt', align: 'left' as const }
]

const filterModel = computed({
  get: () => props.filter,
  set: value => emit('update:filter', value)
})

const pageModel = computed({
  get: () => props.page,
  set: value => emit('update:page', value)
})

const formatDateTime = (value: string) => props.formatDateTime(value)

function formatAmount (value: number) {
  return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusLabel (status: PaymentStatus) {
  switch (status) {
    case 'PAID': return 'Pago'
    case 'PENDING': return 'Pendente'
    case 'FAILED': return 'Falhou'
    default: return 'Criado'
  }
}

function statusColor (status: PaymentStatus) {
  switch (status) {
    case 'PAID': return 'positive'
    case 'PENDING': return 'warning'
    case 'FAILED': return 'negative'
    default: return 'grey-6'
  }
}
</script>

<style scoped lang="scss">
.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #5a332d;
}

.section-subtitle {
  font-size: 0.9rem;
  color: #7b5a4c;
}

.payment-table {
  border-radius: 14px;
  overflow: hidden;
}

.payer-name {
  font-weight: 600;
  color: #5a332d;
}

.payment-amount {
  font-weight: 600;
  color: #6b7a3a;
}

.payment-pagination-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.payment-page-indicator {
  font-size: 0.85rem;
  color: #7b5a4c;
  text-align: center;
}

.payment-filter-tabs {
  min-width: 0;
}
</style>
