<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'

const router = useRouter()
const store = useGameStore()
const roomCode = ref('')
const panel = ref<'main' | 'human' | 'room'>('main')
// 二次确认弹窗：进入人机/AI 对弈前给用户一次反悔机会
const pendingConfirm = ref<null | 'aiGame' | 'aiBattle'>(null)
const aiLevel = ref<'easy' | 'medium' | 'hard'>('medium')
const shouldShowReady = computed(() =>
  store.room?.mode === 'human' && !!store.room.opponentId && !store.gameStart
)

onMounted(() => {
  if (!store.loggedIn) router.replace('/')
})

watch(() => store.gameStart, (v) => {
  if (v) router.push('/game')
})

function onMatch() {
  store.startMatch()
}
function openHumanPanel() {
  panel.value = 'human'
}
function openRoomPanel() {
  panel.value = 'room'
}
function backMain() {
  panel.value = 'main'
}
function backHuman() {
  panel.value = 'human'
}
function onStartAiGame() {
  pendingConfirm.value = 'aiGame'
}
function onStartAiBattle() {
  pendingConfirm.value = 'aiBattle'
}
function confirmStart() {
  if (pendingConfirm.value === 'aiGame') store.startAiGame(aiLevel.value)
  else if (pendingConfirm.value === 'aiBattle') store.startAiBattle()
  pendingConfirm.value = null
}
function cancelStart() {
  pendingConfirm.value = null
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
  <div class="min-h-screen relative flex items-center justify-center p-4">
    <div class="absolute left-6 top-6 bg-amber-50/80 border border-amber-900/30 rounded-lg shadow-md px-5 py-4 flex items-center gap-3">
      <span class="text-4xl leading-none">{{ store.myAvatar || '👤' }}</span>
      <div>
        <div class="text-xs text-amber-800/60 mb-1">用户</div>
        <div class="text-xl text-amber-950">{{ store.myNickname || store.userId }}</div>
      </div>
    </div>

    <div class="w-full max-w-xl bg-amber-50/80 border-2 border-amber-900/40 rounded-lg shadow-xl p-8">
      <h2 class="text-3xl text-amber-900 mb-8 text-center">大 厅</h2>

      <!-- 状态卡片 -->
      <div v-if="store.room || store.matching || store.lastError" class="bg-white/50 border border-amber-900/20 rounded p-4 mb-6 space-y-1 text-sm">
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
        <p v-if="shouldShowReady && store.ready" class="text-amber-700">你已准备，等待对手准备...</p>
        <p v-if="store.opponentReady" class="text-amber-700">对手已准备</p>
        <p v-if="store.lastError" class="text-red-700">{{ store.lastError }}</p>
      </div>

      <div v-if="panel === 'main'" class="space-y-3">
        <button @click="openHumanPanel" :disabled="store.matching || !!store.room"
          class="w-full py-4 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-50 disabled:cursor-not-allowed">
          真人对战
        </button>
        <button @click="onStartAiBattle" :disabled="store.matching || !!store.room"
          class="w-full py-4 bg-stone-700 text-amber-50 rounded hover:bg-stone-800 disabled:opacity-50 disabled:cursor-not-allowed">
          AI 自动对弈
        </button>
        <button @click="onStartAiGame" :disabled="store.matching || !!store.room"
          class="w-full py-4 bg-stone-800 text-amber-50 rounded hover:bg-stone-900 disabled:opacity-50 disabled:cursor-not-allowed">
          人机对战
        </button>
      </div>

      <div v-else-if="panel === 'human'" class="space-y-3">
        <button @click="onMatch" :disabled="store.matching || !!store.room"
          class="w-full py-4 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ store.matching ? '匹配中...' : '随机匹配' }}
        </button>
        <button @click="openRoomPanel" :disabled="store.matching || !!store.room"
          class="w-full py-4 bg-amber-700 text-amber-50 rounded hover:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed">
          房间对战
        </button>
        <button @click="backMain" :disabled="store.matching || !!store.room"
          class="w-full py-3 bg-white/60 border border-amber-900/25 text-amber-900 rounded hover:bg-white/80 disabled:opacity-50 disabled:cursor-not-allowed">
          返回
        </button>
      </div>

      <div v-else class="space-y-3">
        <button @click="onCreateRoom" :disabled="store.matching || !!store.room"
          class="w-full py-4 bg-amber-700 text-amber-50 rounded hover:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed">
          创建房间
        </button>
        <div class="flex gap-3">
          <input
            v-model="roomCode"
            inputmode="numeric"
            maxlength="6"
            placeholder="输入 6 位房间号"
            :disabled="store.matching || !!store.room"
            class="flex-1 min-w-0 px-4 py-3 bg-white/70 border border-amber-900/25 rounded text-amber-950 placeholder:text-amber-900/40 outline-none focus:border-amber-800 disabled:opacity-50"
          />
          <button @click="onJoinRoom" :disabled="store.matching || !!store.room"
            class="px-6 py-3 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-50 disabled:cursor-not-allowed">
            加入房间
          </button>
        </div>
        <button @click="backHuman" :disabled="store.matching || !!store.room"
          class="w-full py-3 bg-white/60 border border-amber-900/25 text-amber-900 rounded hover:bg-white/80 disabled:opacity-50 disabled:cursor-not-allowed">
          返回
        </button>
      </div>

      <div v-if="shouldShowReady" class="mt-3">
        <button @click="onReady" :disabled="!store.room || store.ready"
          class="w-full py-3 bg-amber-700 text-amber-50 rounded hover:bg-amber-800 disabled:opacity-50 disabled:cursor-not-allowed">
          {{ store.ready ? '已准备' : '我已准备' }}
        </button>
      </div>
    </div>

    <!-- 进入人机 / AI 对弈前的二次确认 -->
    <div v-if="pendingConfirm" class="fixed inset-0 z-50 flex items-center justify-center bg-stone-900/60">
      <div class="bg-amber-50 border-2 border-amber-900/40 rounded-lg shadow-xl p-7 w-[360px] max-w-[90vw]">
        <h3 class="text-xl text-amber-900 mb-3 text-center">
          {{ pendingConfirm === 'aiGame' ? '确认进行人机对战？' : '确认开启 AI 自动对弈？' }}
        </h3>
        <p class="text-sm text-amber-800/80 text-center mb-4">
          {{ pendingConfirm === 'aiGame'
            ? '你将作为红方与本机 AI 进行揭棋对局。'
            : '观战两位 AI 自动对弈，全程可暂停 / 结束。' }}
        </p>
        <div v-if="pendingConfirm === 'aiGame'" class="mb-5">
          <label class="block text-sm text-amber-900 mb-2 text-center">AI 难度</label>
          <div class="flex gap-2 justify-center">
            <button type="button" @click="aiLevel = 'easy'"
              :class="aiLevel === 'easy' ? 'bg-amber-800 text-amber-50' : 'bg-white/60 text-amber-900'"
              class="px-3 py-1.5 rounded border border-amber-900/25 text-sm">入门</button>
            <button type="button" @click="aiLevel = 'medium'"
              :class="aiLevel === 'medium' ? 'bg-amber-800 text-amber-50' : 'bg-white/60 text-amber-900'"
              class="px-3 py-1.5 rounded border border-amber-900/25 text-sm">标准</button>
            <button type="button" @click="aiLevel = 'hard'"
              :class="aiLevel === 'hard' ? 'bg-amber-800 text-amber-50' : 'bg-white/60 text-amber-900'"
              class="px-3 py-1.5 rounded border border-amber-900/25 text-sm">挑战</button>
          </div>
        </div>
        <div class="flex gap-3">
          <button @click="confirmStart"
            class="flex-1 py-2.5 bg-amber-800 text-amber-50 rounded hover:bg-amber-900">
            确 认
          </button>
          <button @click="cancelStart"
            class="flex-1 py-2.5 bg-white/60 border border-amber-900/25 text-amber-900 rounded hover:bg-white/80">
            取 消
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
