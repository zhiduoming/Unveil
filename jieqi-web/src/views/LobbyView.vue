<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'

const router = useRouter()
const store = useGameStore()
const roomCode = ref('')

onMounted(() => {
  if (!store.loggedIn) router.replace('/')
})

watch(() => store.gameStart, (v) => {
  if (v) router.push('/game')
})

function onMatch() {
  store.startMatch()
}
function onCreateRoom() {
  store.createRoom()
}
function onJoinRoom() {
  store.joinRoom(roomCode.value)
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
          <span class="text-amber-800/70">房间：</span>
          <span class="font-bold tracking-widest text-amber-950">{{ store.room.roomId }}</span>
          <span v-if="store.room.opponentNickname" class="text-amber-800/70 ml-2">对手：</span>
          <span v-if="store.room.opponentNickname">{{ store.room.opponentNickname }}</span>
        </p>
        <p v-if="store.matching" class="text-amber-700">正在匹配，等待对手加入...</p>
        <p v-if="store.room && !store.room.opponentNickname" class="text-amber-700">
          房间已创建，把 6 位房间号告诉对方，等待对方加入...
        </p>
        <p v-if="store.ready" class="text-amber-700">你已准备，等待对手准备...</p>
        <p v-if="store.opponentReady" class="text-amber-700">对手已准备</p>
        <p v-if="store.lastError" class="text-red-700">{{ store.lastError }}</p>
      </div>

      <div class="grid grid-cols-2 gap-3">
        <button @click="onMatch" :disabled="store.matching || !!store.room"
          class="py-3 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ store.matching ? '匹配中...' : '开始匹配' }}
        </button>
        <button @click="onCreateRoom" :disabled="store.matching || !!store.room"
          class="py-3 bg-amber-700 text-amber-50 rounded hover:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed">
          创建房间
        </button>
      </div>

      <div class="mt-3 flex gap-3">
        <input
          v-model="roomCode"
          inputmode="numeric"
          maxlength="6"
          placeholder="输入 6 位房间号"
          :disabled="store.matching || !!store.room"
          class="flex-1 px-4 py-3 bg-white/70 border border-amber-900/25 rounded text-amber-950 placeholder:text-amber-900/40 outline-none focus:border-amber-800 disabled:opacity-50"
        />
        <button @click="onJoinRoom" :disabled="store.matching || !!store.room"
          class="px-6 py-3 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-50 disabled:cursor-not-allowed">
          加入房间
        </button>
      </div>

      <div class="mt-3">
        <button @click="onReady" :disabled="!store.room || store.ready"
          class="w-full py-3 bg-amber-700 text-amber-50 rounded hover:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ store.ready ? '已准备' : '我已准备' }}
        </button>
      </div>

      <p class="text-xs text-amber-800/60 mt-6 text-center">
        自动匹配会按进入顺序两两配对；房间模式需要对方输入 6 位房间号加入
      </p>
    </div>
  </div>
</template>
