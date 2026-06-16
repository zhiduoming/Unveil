<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'
import { randomNickname, randomAvatar, nextAvatar, genUserId } from '../utils/identity'

const router = useRouter()
const store = useGameStore()

const nickname = ref(randomNickname())
const avatar = ref(randomAvatar())
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

function rollNickname() {
  nickname.value = randomNickname()
}
function rollAvatar() {
  avatar.value = nextAvatar(avatar.value)
}

async function onLogin() {
  errorMsg.value = ''
  if (!nickname.value.trim()) { errorMsg.value = '昵称不能为空'; return }
  busy.value = true
  try {
    await store.connect(serverUrl.value)
    // userId 作为稳定账号（与显示昵称解耦，避免随机重名冲突）
    store.login(genUserId(), password.value, nickname.value.trim(), avatar.value)
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

      <!-- 头像：点击随机换 -->
      <div class="flex flex-col items-center mb-5">
        <button type="button" @click="rollAvatar"
          class="avatar-pick" title="点击更换头像">
          {{ avatar }}
        </button>
        <span class="text-xs text-amber-800/60 mt-1">点击更换头像</span>
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-sm text-amber-900 mb-1">服务器地址</label>
          <input v-model="serverUrl" class="w-full px-3 py-2 border border-amber-900/30 rounded bg-white/60 focus:outline-none focus:border-amber-700" />
        </div>
        <div>
          <label class="block text-sm text-amber-900 mb-1">昵称</label>
          <div class="flex gap-2">
            <input v-model="nickname"
              class="flex-1 px-3 py-2 border border-amber-900/30 rounded bg-white/60 focus:outline-none focus:border-amber-700" />
            <button type="button" @click="rollNickname" title="随机昵称"
              class="dice-btn">🎲</button>
          </div>
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

<style scoped>
.avatar-pick {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  font-size: 40px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 50% 40%, #fde68a, #f59e0b);
  border: 3px solid #92400e;
  box-shadow: 0 4px 10px rgba(120, 60, 10, 0.35);
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}
.avatar-pick:hover {
  transform: translateY(-2px) scale(1.04);
  box-shadow: 0 6px 14px rgba(120, 60, 10, 0.45);
}
.avatar-pick:active {
  transform: scale(0.96);
}

.dice-btn {
  flex: 0 0 auto;
  width: 42px;
  font-size: 20px;
  border: 1px solid rgba(120, 53, 15, 0.4);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: background 0.12s ease, transform 0.12s ease;
}
.dice-btn:hover {
  background: rgba(253, 230, 138, 0.7);
}
.dice-btn:active {
  transform: rotate(-18deg) scale(0.92);
}
</style>
