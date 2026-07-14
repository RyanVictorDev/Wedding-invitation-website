<template>
  <div class="q-mt-xl">
    <div class="section-header row items-center justify-between">
      <div>
        <h2 class="section-title">Lista de presentes</h2>
        <div class="section-subtitle">Cadastre e gerencie os itens da lista pública.</div>
      </div>
      <q-btn
        unelevated
        label="Adicionar presente"
        class="action-btn"
        @click="openCreateDialog"
      />
    </div>

    <div v-if="loading" class="text-center q-my-lg">
      <q-spinner color="primary" size="2em" />
    </div>

    <q-table
      v-else-if="gifts.length"
      flat
      bordered
      :rows="gifts"
      :columns="columns"
      row-key="id"
      hide-bottom
      class="gift-table"
    >
      <template #body-cell-imageUrl="props">
        <q-td :props="props">
          <img
            v-if="props.row.imageUrl"
            :src="props.row.imageUrl"
            alt=""
            class="thumb"
          >
          <span v-else>—</span>
        </q-td>
      </template>

      <template #body-cell-price="props">
        <q-td :props="props">
          <span v-if="props.row.price != null">R$ {{ formatPrice(props.row.price) }}</span>
          <span v-else>—</span>
        </q-td>
      </template>

      <template #body-cell-reserved="props">
        <q-td :props="props">
          <q-chip
            :color="props.row.reserved ? 'orange-7' : 'positive'"
            text-color="white"
            dense
            square
          >
            {{ props.row.reserved ? 'Reservado' : 'Disponível' }}
          </q-chip>
        </q-td>
      </template>

      <template #body-cell-reservedBy="props">
        <q-td :props="props">
          <span v-if="props.row.reserved">{{ props.row.reservedBy || 'Anônimo' }}</span>
          <span v-else>—</span>
        </q-td>
      </template>

      <template #body-cell-actions="props">
        <q-td :props="props">
          <q-btn flat dense icon="edit" @click="openEditDialog(props.row)" />
          <q-btn flat dense icon="delete" color="negative" @click="removeGift(props.row.id)" />
        </q-td>
      </template>
    </q-table>

    <div v-else class="text-center q-mt-md text-grey-7">
      Nenhum presente cadastrado ainda.
    </div>

    <q-dialog v-model="dialogOpen" persistent>
      <q-card class="dialog-card q-pa-md" style="min-width: 420px">
        <div class="dialog-title">{{ editingId ? 'Editar presente' : 'Novo presente' }}</div>

        <q-input v-model="form.title" label="Título" outlined dense class="q-mt-md" />
        <q-input v-model="form.description" label="Descrição" outlined dense type="textarea" autogrow class="q-mt-sm" />
        <q-input v-model="form.productUrl" label="URL do produto" outlined dense class="q-mt-sm" />
        <div class="row q-col-gutter-sm q-mt-xs">
          <div class="col">
            <q-btn flat dense label="Importar da URL" @click="previewFromUrl" />
          </div>
        </div>
        <q-input v-model="form.imageUrl" label="URL da imagem" outlined dense class="q-mt-sm" />
        <q-input v-model="displayPrice" label="Preço (opcional)" prefix="R$" outlined dense class="q-mt-sm" @update:model-value="onPriceInput" />
        <q-input v-model.number="form.sortOrder" label="Ordem" type="number" outlined dense class="q-mt-sm" />
        <q-toggle v-model="form.active" label="Ativo na lista pública" class="q-mt-sm" />
        <q-toggle
          v-if="editingId"
          v-model="form.clearReservation"
          label="Liberar reserva"
          class="q-mt-xs"
        />

        <div v-if="formError" class="form-error q-mt-sm">{{ formError }}</div>

        <div class="dialog-actions q-mt-md">
          <q-btn flat label="Cancelar" @click="dialogOpen = false" />
          <q-btn unelevated label="Salvar" class="action-btn" :loading="saving" @click="saveGift" />
        </div>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from 'src/boot/axios'

interface GiftRow {
  id: number
  title: string
  description: string | null
  imageUrl: string | null
  productUrl: string | null
  price: number | null
  reserved: boolean
  reservedBy: string | null
  active: boolean
  sortOrder: number
}

const gifts = ref<GiftRow[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const formError = ref('')
const displayPrice = ref('')

const form = reactive({
  title: '',
  description: '',
  imageUrl: '',
  productUrl: '',
  price: null as number | null,
  active: true,
  sortOrder: 0,
  clearReservation: false
})

const columns = [
  { name: 'imageUrl', label: 'Imagem', field: 'imageUrl', align: 'left' as const },
  { name: 'title', label: 'Título', field: 'title', align: 'left' as const },
  { name: 'price', label: 'Preço', field: 'price', align: 'left' as const },
  { name: 'reserved', label: 'Status', field: 'reserved', align: 'left' as const },
  { name: 'reservedBy', label: 'Reservado por', field: 'reservedBy', align: 'left' as const },
  { name: 'actions', label: 'Ações', field: 'actions', align: 'right' as const }
]

function formatPrice (value: number) {
  return value.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function resetForm () {
  form.title = ''
  form.description = ''
  form.imageUrl = ''
  form.productUrl = ''
  form.price = null
  form.active = true
  form.sortOrder = 0
  form.clearReservation = false
  displayPrice.value = ''
  formError.value = ''
}

async function loadGifts () {
  loading.value = true
  try {
    const { data } = await api.get<GiftRow[]>('/gifts/admin')
    gifts.value = data
  } catch (e) {
    console.error('Erro ao carregar presentes', e)
  } finally {
    loading.value = false
  }
}

function openCreateDialog () {
  editingId.value = null
  resetForm()
  dialogOpen.value = true
}

function openEditDialog (row: GiftRow) {
  editingId.value = row.id
  form.title = row.title
  form.description = row.description ?? ''
  form.imageUrl = row.imageUrl ?? ''
  form.productUrl = row.productUrl ?? ''
  form.price = row.price
  form.active = row.active
  form.sortOrder = row.sortOrder
  form.clearReservation = false
  displayPrice.value = row.price != null ? formatPrice(row.price) : ''
  formError.value = ''
  dialogOpen.value = true
}

function onPriceInput (val: string | number | null) {
  const raw = String(val ?? '').replace(/\D/g, '')
  if (!raw) {
    form.price = null
    displayPrice.value = ''
    return
  }
  const cents = parseInt(raw, 10)
  form.price = cents / 100
  displayPrice.value = form.price.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function previewFromUrl () {
  if (!form.productUrl.trim()) return
  try {
    const { data } = await api.post<{ title: string | null, description: string | null, imageUrl: string | null }>('/gifts/preview-url', {
      url: form.productUrl.trim()
    })
    if (data.title && !form.title) form.title = data.title
    if (data.description && !form.description) form.description = data.description
    if (data.imageUrl && !form.imageUrl) form.imageUrl = data.imageUrl
  } catch {
    formError.value = 'Não foi possível importar dados da URL.'
  }
}

async function saveGift () {
  formError.value = ''
  saving.value = true

  const payload = {
    title: form.title.trim(),
    description: form.description.trim() || null,
    imageUrl: form.imageUrl.trim() || null,
    productUrl: form.productUrl.trim() || null,
    price: form.price,
    active: form.active,
    sortOrder: form.sortOrder
  }

  try {
    if (editingId.value) {
      await api.put(`/gifts/${editingId.value}`, {
        ...payload,
        clearReservation: form.clearReservation
      })
    } else {
      await api.post('/gifts', payload)
    }
    dialogOpen.value = false
    await loadGifts()
  } catch {
    formError.value = 'Não foi possível salvar o presente.'
  } finally {
    saving.value = false
  }
}

async function removeGift (id: number) {
  if (!window.confirm('Excluir este presente?')) return
  try {
    await api.delete(`/gifts/${id}`)
    await loadGifts()
  } catch (e) {
    console.error('Erro ao excluir presente', e)
  }
}

onMounted(() => {
  void loadGifts()
})
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

.gift-table {
  margin-top: 12px;
  border-radius: 14px;
  overflow: hidden;
}

.thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 8px;
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
