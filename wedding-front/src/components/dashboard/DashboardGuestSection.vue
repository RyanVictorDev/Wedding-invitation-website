<template>
  <div class="q-mt-xl">
    <div class="section-header">
      <h2 class="section-title">Lista de convidados</h2>
      <div class="section-subtitle">
        Quem já confirmou presença e quem ainda não.
      </div>
    </div>

    <div class="guest-filters row q-col-gutter-md q-mb-sm">
      <div class="col-12 col-md-6">
        <q-input
          v-model="localSearch"
          dense
          outlined
          clearable
          label="Buscar convidado"
          @keyup.enter="emitSearch"
          @clear="handleClear"
        >
          <template #append>
            <q-btn
              round
              dense
              flat
              icon="search"
              @click="emitSearch"
            />
          </template>
        </q-input>
      </div>
      <div class="col-12 col-md-6 flex items-center justify-end">
        <q-tabs
          v-model="filterModel"
          dense
          align="right"
          active-color="brown-7"
          indicator-color="brown-5"
          class="guest-filter-tabs"
        >
          <q-tab
            name="all"
            label="Todos"
          />
          <q-tab
            name="yes"
            label="confirmados"
          />
          <q-tab
            name="no"
            label="Não confirmados"
          />
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

    <DashboardPagination
      v-if="rows.length"
      v-model:model-value="pageModel"
      :total-pages="totalPages"
      :loading="loading"
      class="q-mt-md"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import DashboardPagination from 'components/dashboard/DashboardPagination.vue'

interface GuestSummary {
  id: number
  name: string
  confirmed: boolean
  godparent: boolean
  confirmationDate: string | null
}

const props = defineProps<{
  rows: GuestSummary[]
  loading: boolean
  page: number
  totalPages: number
  search: string
  filter: 'all' | 'yes' | 'no'
  formatDateTime: (value: string) => string
}>()

const emit = defineEmits<{
  (e: 'update:page', value: number): void
  (e: 'search', value: string): void
  (e: 'update:filter', value: 'all' | 'yes' | 'no'): void
}>()

const columns = [
  { name: 'name', label: 'Nome', field: 'name', align: 'left' as const },
  { name: 'confirmed', label: 'Presença', field: 'confirmed', align: 'left' as const },
  { name: 'godparent', label: 'Padrinho/Madrinha', field: 'godparent', align: 'left' as const },
  { name: 'confirmationDate', label: 'Data de confirmação', field: 'confirmationDate', align: 'left' as const }
]

const localSearch = ref(props.search)

watch(
  () => props.search,
  (value) => {
    localSearch.value = value
  }
)

const filterModel = computed({
  get: () => props.filter,
  set: value => emit('update:filter', value)
})

const pageModel = computed({
  get: () => props.page,
  set: value => emit('update:page', value)
})

const formatDateTime = (value: string) => props.formatDateTime(value)

function emitSearch () {
  emit('search', localSearch.value?.trim() ?? '')
}

function handleClear () {
  localSearch.value = ''
  emitSearch()
}
</script>

<style scoped lang="scss">
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
</style>

