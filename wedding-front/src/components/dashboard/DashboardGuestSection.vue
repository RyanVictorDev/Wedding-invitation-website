<template>
  <div class="q-mt-xl">
    <div class="section-header row items-center justify-between">
      <div>
        <h2 class="section-title">Lista de convidados</h2>
        <div class="section-subtitle">
          Cadastre convidados e acompanhe confirmações de presença.
        </div>
      </div>
      <q-btn
        unelevated
        label="Adicionar convidado"
        class="action-btn"
        @click="openCreateDialog"
      />
    </div>

    <div class="guest-filters row q-col-gutter-md q-mb-sm q-mt-md">
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
            <q-btn round dense flat icon="search" @click="emitSearch" />
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
          <q-tab name="all" label="Todos" />
          <q-tab name="pending" label="Pendentes" />
          <q-tab name="yes" label="Confirmados" />
          <q-tab name="no" label="Não vão" />
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
      <template #body-cell-status="props">
        <q-td :props="props">
          <q-chip
            :color="statusColor(props.row)"
            text-color="white"
            dense
            square
          >
            {{ statusLabel(props.row) }}
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

      <template #body-cell-actions="props">
        <q-td :props="props">
          <q-btn flat dense icon="edit" @click="openEditDialog(props.row)" />
          <q-btn
            v-if="!props.row.responded"
            flat
            dense
            icon="delete"
            color="negative"
            @click="removeGuest(props.row.id)"
          />
        </q-td>
      </template>
    </q-table>

    <div v-else class="text-center q-mt-md text-grey-7">
      Nenhum convidado encontrado.
    </div>

    <DashboardPagination
      v-if="rows.length"
      v-model:model-value="pageModel"
      :total-pages="totalPages"
      :loading="loading"
      class="q-mt-md"
    />

    <q-dialog v-model="dialogOpen" persistent>
      <q-card class="q-pa-md" style="min-width: 360px">
        <div class="dialog-title">{{ editingId ? 'Editar convidado' : 'Novo convidado' }}</div>
        <q-input v-model="form.name" label="Nome completo" outlined dense class="q-mt-md" />
        <q-toggle v-model="form.godparent" label="Padrinho / Madrinha" class="q-mt-sm" />
        <q-toggle
          v-if="editingId"
          v-model="form.resetResponse"
          label="Resetar resposta (volta para pendente)"
          class="q-mt-xs"
        />
        <div v-if="formError" class="form-error q-mt-sm">{{ formError }}</div>
        <div class="dialog-actions q-mt-md">
          <q-btn flat label="Cancelar" @click="dialogOpen = false" />
          <q-btn unelevated label="Salvar" class="action-btn" :loading="saving" @click="saveGuest" />
        </div>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { api } from 'src/boot/axios'
import DashboardPagination from 'components/dashboard/DashboardPagination.vue'

interface GuestSummary {
  id: number
  name: string
  confirmed: boolean
  godparent: boolean
  responded: boolean
  confirmationDate: string | null
}

const props = defineProps<{
  rows: GuestSummary[]
  loading: boolean
  page: number
  totalPages: number
  search: string
  filter: 'all' | 'pending' | 'yes' | 'no'
  formatDateTime: (value: string) => string
}>()

const emit = defineEmits<{
  (e: 'update:page', value: number): void
  (e: 'search', value: string): void
  (e: 'update:filter', value: 'all' | 'pending' | 'yes' | 'no'): void
  (e: 'refresh'): void
}>()

const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  name: '',
  godparent: false,
  resetResponse: false
})

const columns = [
  { name: 'name', label: 'Nome', field: 'name', align: 'left' as const },
  { name: 'status', label: 'Status', field: 'status', align: 'left' as const },
  { name: 'godparent', label: 'Padrinho/Madrinha', field: 'godparent', align: 'left' as const },
  { name: 'confirmationDate', label: 'Data de confirmação', field: 'confirmationDate', align: 'left' as const },
  { name: 'actions', label: 'Ações', field: 'actions', align: 'right' as const }
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

function statusLabel (row: GuestSummary) {
  if (!row.responded) return 'Pendente'
  return row.confirmed ? 'Confirmado' : 'Não vai'
}

function statusColor (row: GuestSummary) {
  if (!row.responded) return 'grey-6'
  return row.confirmed ? 'positive' : 'negative'
}

function openCreateDialog () {
  editingId.value = null
  form.name = ''
  form.godparent = false
  form.resetResponse = false
  formError.value = ''
  dialogOpen.value = true
}

function openEditDialog (row: GuestSummary) {
  editingId.value = row.id
  form.name = row.name
  form.godparent = row.godparent
  form.resetResponse = false
  formError.value = ''
  dialogOpen.value = true
}

async function saveGuest () {
  formError.value = ''
  saving.value = true

  try {
    if (editingId.value) {
      await api.put(`/guests/${editingId.value}`, {
        name: form.name.trim(),
        godparent: form.godparent,
        resetResponse: form.resetResponse
      })
    } else {
      await api.post('/guests', {
        name: form.name.trim(),
        godparent: form.godparent
      })
    }
    dialogOpen.value = false
    emit('refresh')
  } catch {
    formError.value = 'Não foi possível salvar o convidado.'
  } finally {
    saving.value = false
  }
}

async function removeGuest (id: number) {
  if (!window.confirm('Excluir este convidado?')) return
  try {
    await api.delete(`/guests/${id}`)
    emit('refresh')
  } catch (e) {
    console.error('Erro ao excluir convidado', e)
  }
}

function emitSearch () {
  emit('search', localSearch.value?.trim() ?? '')
}

function handleClear () {
  localSearch.value = ''
  emitSearch()
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

.action-btn {
  background: linear-gradient(135deg, #6b7a3a, #87964b);
  color: #fdfaf4;
  border-radius: 999px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 0.75rem;
}

.guest-table {
  margin-top: 8px;
  border-radius: 14px;
  overflow: hidden;
}

.guest-filters {
  align-items: center;
}

.dialog-title {
  font-weight: 600;
  color: #5a332d;
}

.form-error {
  color: #8a1c1c;
  font-size: 0.85rem;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
