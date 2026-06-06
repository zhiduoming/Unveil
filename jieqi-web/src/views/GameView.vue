<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'

const router = useRouter()
const store = useGameStore()

onMounted(() => {
  if (!store.gameStart) router.replace('/lobby')
})

// 棋盘视角：红方在下，黑方在上
const yourColor = computed(() => store.gameStart?.yourColor)
const isRedView = computed(() => yourColor.value === 'red')

// 9 列 x 10 行 占位（后续接入实际棋子）
const rows = Array.from({ length: 10 }, (_, i) => i)
const cols = ['a','b','c','d','e','f','g','h','i']

function onResign() {
  if (confirm('确认认输？')) store.resign()
}
</script>

<template>
  <div class="min-h-screen p-4">
    <div class="max-w-5xl mx-auto">
      <!-- 顶部状态栏 -->
      <header class="bg-amber-50/80 border-2 border-amber-900/40 rounded-lg shadow p-4 mb-4 flex items-center justify-between">
        <div>
          <h2 class="text-2xl text-amber-900">揭棋对弈</h2>
          <p class="text-sm text-amber-800/70">
            红方 {{ store.gameStart?.redPlayerId }} vs 黑方 {{ store.gameStart?.blackPlayerId }}
            · 你执 {{ yourColor === 'red' ? '红方' : '黑方' }}
            · {{ store.gameStart?.firstHand ? '你先手' : '对手先手' }}
          </p>
        </div>
        <button @click="onResign"
          class="px-4 py-2 bg-red-800 text-amber-50 rounded hover:bg-red-900">
          认 输
        </button>
      </header>

      <!-- 棋盘占位（v0.1 仅画格子，后续替换为真实棋盘组件） -->
      <div class="bg-amber-100 border-4 border-amber-900 rounded-lg shadow-xl p-4">
        <div class="grid grid-cols-9 gap-0 aspect-[9/10] max-w-2xl mx-auto"
             :style="{ direction: isRedView ? 'ltr' : 'rtl' }">
          <template v-for="r in (isRedView ? [...rows].reverse() : rows)" :key="r">
            <div v-for="c in cols" :key="`${r}-${c}`"
                 class="border border-amber-900/40 flex items-center justify-center text-xs text-amber-900/40">
              {{ c }}{{ r }}
            </div>
          </template>
        </div>
        <p class="text-center text-amber-800/60 text-sm mt-4">
          [v0.1 棋盘占位] 下一版会画出棋子、支持点击走子
        </p>
      </div>

      <!-- 对局结束弹窗 -->
      <div v-if="store.gameOver"
           class="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div class="bg-amber-50 border-2 border-amber-900 rounded-lg p-8 max-w-sm">
          <h3 class="text-2xl text-amber-900 mb-2 text-center">
            {{ store.gameOver.winner === yourColor ? '你赢了！' : (store.gameOver.winner === 'draw' ? '和棋' : '你输了') }}
          </h3>
          <p class="text-center text-amber-800 mb-4">原因：{{ store.gameOver.reason }}</p>
          <button @click="router.push('/lobby')" class="w-full py-2 bg-amber-800 text-amber-50 rounded hover:bg-amber-900">
            返回大厅
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
