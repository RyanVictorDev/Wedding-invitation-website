<template>
  <div>
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
  </div>
</template>

<script setup lang="ts">
interface DashboardSummary {
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
}

defineProps<{
  summary: DashboardSummary | null
  formattedTotalPayments: string
  formattedPaidTotal: string
  formattedPendingTotal: string
}>()
</script>

<style scoped lang="scss">
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
</style>

