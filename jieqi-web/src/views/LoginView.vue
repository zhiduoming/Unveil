<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'

const router = useRouter()
const store = useGameStore()

const userId = ref('player1')
const password = ref('123456')
const serverUrl = ref(store.serverUrl)
const busy = ref(false)
const errorMsg = ref('')

watch(() => store.loggedIn, (v) => {
  if (v) router.push('/lobby')
})
watch(() => store.lastError, (v) => {
  if (v) { errorMsg.value = v; busy.value = false }
})

async function onLogin() {
  errorMsg.value = ''
  busy.value = true
  try {
    await store.connect(serverUrl.value)
    store.login(userId.value, password.value)
  } catch (e: any) {
    errorMsg.value = '无法连接服务器，请检查地址和端口'
    busy.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-4">
    <div class="w-full max-w-md bg-amber-50/80 border-2 border-amber-900/40 rounded-lg shadow-xl p-8 backdrop-blur">
      <h1 class="text-4xl text-center text-amber-900 mb-2">揭 棋</h1>
      <p class="text-center text-amber-800/70 mb-6 text-sm">揭棋在线对战客户端</p>

      <div class="space-y-4">
        <div>
          <label class="block text-sm text-amber-900 mb-1">服务器地址</label>
          <input v-model="serverUrl" class="w-full px-3 py-2 border border-amber-900/30 rounded bg-white/60 focus:outline-none focus:border-amber-700" />
        </div>
        <div>
          <label class="block text-sm text-amber-900 mb-1">用户名</label>
          <input v-model="userId" class="w-full px-3 py-2 border border-amber-900/30 rounded bg-white/60 focus:outline-none focus:border-amber-700" />
        </div>
        <div>
          <label class="block text-sm text-amber-900 mb-1">密码</label>
          <input v-model="password" type="password" class="w-full px-3 py-2 border border-amber-900/30 rounded bg-white/60 focus:outline-none focus:border-amber-700" />
        </div>

        <button @click="onLogin" :disabled="busy"
          class="w-full py-2 bg-amber-800 text-amber-50 rounded hover:bg-amber-900 disabled:opacity-60 disabled:cursor-not-allowed transition">
          {{ busy ? '连接中...' : '登 录' }}
        </button>

        <p v-if="errorMsg" class="text-red-700 text-sm text-center">{{ errorMsg }}</p>
        <p class="text-xs text-amber-800/60 text-center mt-4">
          连接状态: {{ store.connectionStatusText }}
        </p>
      </div>
    </div>
  </div>
</template>
