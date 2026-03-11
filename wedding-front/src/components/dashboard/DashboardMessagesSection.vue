<template>
  <div class="q-mt-xl">
    <div class="section-header">
      <h2 class="section-title">Recados</h2>
      <div class="section-subtitle">
        Mensagens deixadas pelos convidados.
      </div>
    </div>

    <div v-if="loading" class="text-center q-my-md">
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

        <DashboardPagination
          v-model:model-value="pageModel"
          :total-pages="totalPages"
          :loading="loading"
          class="q-mt-md"
        />

        <div class="messages-page-indicator q-mt-sm">
          Página {{ pageModel }} de {{ totalPages }}
        </div>
      </div>

      <div v-else class="text-center q-mt-md text-grey-7">
        Nenhum recado enviado ainda.
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DashboardPagination from 'components/dashboard/DashboardPagination.vue'

interface Message {
  id: number
  content: string
  authorName: string | null
  createdAt: string
}

const props = defineProps<{
  messages: Message[]
  loading: boolean
  page: number
  totalPages: number
  formatDateTime: (value: string) => string
}>()

const emit = defineEmits<{
  (e: 'update:page', value: number): void
}>()

const pageModel = computed({
  get: () => props.page,
  set: value => emit('update:page', value)
})

const formatDateTime = (value: string) => props.formatDateTime(value)
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

.messages-page-indicator {
  text-align: center;
  font-size: 0.8rem;
  color: #7b5a4c;
}
</style>

