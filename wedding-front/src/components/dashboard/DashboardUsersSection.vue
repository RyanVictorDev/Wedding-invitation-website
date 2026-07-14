<template>
  <div class="q-mt-xl">
    <div class="section-header row items-center justify-between">
      <div>
        <h2 class="section-title">Usuários admin</h2>
        <div class="section-subtitle">Gerencie quem pode acessar o dashboard.</div>
      </div>
      <q-btn
        unelevated
        label="Novo usuário"
        class="action-btn"
        @click="openCreateDialog"
      />
    </div>

    <div v-if="loading" class="text-center q-my-lg">
      <q-spinner color="primary" size="2em" />
    </div>

    <q-table
      v-else-if="users.length"
      flat
      bordered
      :rows="users"
      :columns="columns"
      row-key="id"
      hide-bottom
      class="users-table"
    >
      <template #body-cell-createdAt="props">
        <q-td :props="props">
          {{ formatDateTime(props.row.createdAt) }}
        </q-td>
      </template>

      <template #body-cell-actions="props">
        <q-td :props="props">
          <q-btn flat dense icon="edit" @click="openEditDialog(props.row)" />
          <q-btn flat dense icon="delete" color="negative" @click="removeUser(props.row.id)" />
        </q-td>
      </template>
    </q-table>

    <q-dialog v-model="dialogOpen" persistent>
      <q-card class="q-pa-md" style="min-width: 360px">
        <div class="dialog-title">{{ editingId ? 'Editar usuário' : 'Novo usuário' }}</div>
        <q-input v-model="form.username" label="Usuário" outlined dense class="q-mt-md" />
        <q-input
          v-model="form.password"
          :label="editingId ? 'Nova senha (opcional)' : 'Senha'"
          type="password"
          outlined
          dense
          class="q-mt-sm"
        />
        <div v-if="formError" class="form-error q-mt-sm">{{ formError }}</div>
        <div class="dialog-actions q-mt-md">
          <q-btn flat label="Cancelar" @click="dialogOpen = false" />
          <q-btn unelevated label="Salvar" class="action-btn" :loading="saving" @click="saveUser" />
        </div>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { api } from 'src/boot/axios'

interface AdminUser {
  id: number
  username: string
  createdAt: string
}

const users = ref<AdminUser[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editingId = ref<number | null>(null)
const formError = ref('')

const form = reactive({
  username: '',
  password: ''
})

const columns = [
  { name: 'username', label: 'Usuário', field: 'username', align: 'left' as const },
  { name: 'createdAt', label: 'Criado em', field: 'createdAt', align: 'left' as const },
  { name: 'actions', label: 'Ações', field: 'actions', align: 'right' as const }
]

function formatDateTime (value: string) {
  return new Date(value).toLocaleString('pt-BR')
}

async function loadUsers () {
  loading.value = true
  try {
    const { data } = await api.get<AdminUser[]>('/admin/users')
    users.value = data
  } catch (e) {
    console.error('Erro ao carregar usuários', e)
  } finally {
    loading.value = false
  }
}

function openCreateDialog () {
  editingId.value = null
  form.username = ''
  form.password = ''
  formError.value = ''
  dialogOpen.value = true
}

function openEditDialog (row: AdminUser) {
  editingId.value = row.id
  form.username = row.username
  form.password = ''
  formError.value = ''
  dialogOpen.value = true
}

async function saveUser () {
  formError.value = ''
  saving.value = true

  try {
    if (editingId.value) {
      await api.put(`/admin/users/${editingId.value}`, {
        username: form.username.trim(),
        password: form.password.trim() || null
      })
    } else {
      await api.post('/admin/users', {
        username: form.username.trim(),
        password: form.password
      })
    }
    dialogOpen.value = false
    await loadUsers()
  } catch {
    formError.value = 'Não foi possível salvar o usuário.'
  } finally {
    saving.value = false
  }
}

async function removeUser (id: number) {
  if (!window.confirm('Excluir este usuário?')) return
  try {
    await api.delete(`/admin/users/${id}`)
    await loadUsers()
  } catch (e) {
    console.error('Erro ao excluir usuário', e)
  }
}

onMounted(() => {
  void loadUsers()
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

.users-table {
  margin-top: 12px;
  border-radius: 14px;
  overflow: hidden;
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
