<script setup lang="ts">
import { watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'

const router = useRouter()
const store = useGameStore()

onMounted(() => {
  if (!store.loggedIn) router.replace('/')
})

watch(() => store.gameStart, (v) => {
  if (v) router.push('/game')
})

function onMatch() {
  store.startMatch()
}
function onReady() {
  store.setReady()
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-4">
    <div class="w-full max-w-2xl bg-amber-50/80 border-2 border-amber-900/40 rounded-lg shadow-xl p-8">
      <h2 class="text-3xl text-amber-900 mb-1">大 厅</h2>
      <p class="text-amber-800/70 text-sm mb-6">欢迎，{{ store.userId }}</p>

      <!-- 状态卡片 -->
      <div class="bg-white/50 border border-amber-900/20 rounded p-4 mb-6 space-y-1 text-sm">
        <p><span class="text-amber-800/70">连接：</span>{{ store.connectionStatus }}</p>
        <p v-if="store.room">
          <span class="text-amber-800/70">房间：</span>{{ store.room.roomId }}
          <span class="text-amber-800/70 ml-2">对手：</span>{{ store.room.opponentNickname }}
        </p>
        <p v-if="store.matching" class="text-amber-700">正在匹配，等待对手加入...</p>
        <p v-if="store.ready" class="text-amber-700">你已准备，等待对手准备...</p>
        <p v-if="store.opponentReady" class="text-amber-700">对手已准备</p>
        <p v-if="store.lastError" class="text-red-700">{{ store.lastError }}</p>
      </div>

      <div class="flex gap-3">
        <button @click="onMatch" :disabled="store.matching || !!store.room"
          class="flex-1 py-3 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ store.matching ? '匹配中...' : '开始匹配' }}
        </button>
        <button @click="onReady" :disabled="!store.room || store.ready"
          class="flex-1 py-3 bg-amber-700 text-amber-50 rounded hover:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ store.ready ? '已准备' : '我已准备' }}
        </button>
      </div>

      <p class="text-xs text-amber-800/60 mt-6 text-center">
        匹配成功后点「我已准备」，双方都就绪即开局
      </p>
    </div>
  </div>
</template>
