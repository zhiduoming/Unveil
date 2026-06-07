<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'
import { initialJieqiBoard } from '../types/chess'
import ChessBoard from '../components/ChessBoard.vue'

const router = useRouter()
const store = useGameStore()

onMounted(() => {
  if (!store.gameStart) router.replace('/lobby')
  // 如果 store 里还没棋盘（直接刷新页面进来），用默认初始局面
  if (store.board.length === 0) store.board = initialJieqiBoard()
})

const yourColor = computed(() => store.gameStart?.yourColor)
const isRedView = computed(() => yourColor.value === 'red')
const isMyTurn = computed(() => store.currentTurn === yourColor.value)

function onCellClick(row: number, col: number) {
  store.selectCell(row, col)
}

function onResign() {
  if (confirm('确认认输？')) store.resign()
}
</script>

<template>
  <div class="page">
    <div class="layout">
      <!-- ========= 左栏 ========= -->
      <aside class="side-col">
        <!-- 对手卡片：贴顶部 -->
        <div
          class="player-card"
          :class="store.currentTurn !== yourColor ? 'player-active' : 'player-idle'"
        >
          <div class="card-head">
            <div
              class="avatar"
              :class="(yourColor === 'red') ? 'avatar-black' : 'avatar-red'"
            >
              {{ ((yourColor === 'red')
                  ? store.gameStart?.blackPlayerId
                  : store.gameStart?.redPlayerId)?.[0]?.toUpperCase() || '?' }}
            </div>
            <span
              class="side-tag"
              :class="(yourColor === 'red') ? 'tag-black' : 'tag-red'"
            >
              {{ yourColor === 'red' ? '黑方' : '红方' }}
            </span>
          </div>
          <div class="player-name">
            {{ store.room?.opponentNickname || store.room?.opponentId || '对手' }}
          </div>
          <div class="player-role">对手</div>
        </div>

        <!-- 计时器：居中 -->
        <div class="clock-card">
          <div class="clock-label">步时</div>
          <div class="clock-display" :class="isMyTurn ? 'clock-active' : 'clock-idle'">
            <span class="clock-text">01:05</span>
          </div>
          <div class="clock-state">{{ isMyTurn ? '轮到你走' : '等待对手' }}</div>
        </div>

        <!-- 自己卡片：贴底部 -->
        <div
          class="player-card"
          :class="store.currentTurn === yourColor ? 'player-active' : 'player-idle'"
        >
          <div class="card-head">
            <div
              class="avatar"
              :class="yourColor === 'red' ? 'avatar-red' : 'avatar-black'"
            >
              {{ store.userId?.[0]?.toUpperCase() || '?' }}
            </div>
            <span
              class="side-tag"
              :class="yourColor === 'red' ? 'tag-red' : 'tag-black'"
            >
              {{ yourColor === 'red' ? '红方' : '黑方' }}
            </span>
            <span class="side-tag tag-you">你</span>
          </div>
          <div class="player-name">{{ store.userId }}</div>
          <div class="player-role">
            {{ store.gameStart?.firstHand ? '先手' : '后手' }}
          </div>
        </div>
      </aside>

      <!-- ========= 中栏：棋盘 ========= -->
      <main class="board-col">
        <div class="board-stack">
          <div v-if="store.lastError" class="error-banner">
            ⚠️ {{ store.lastError }}
            <button class="err-close" @click="store.lastError = ''">✕</button>
          </div>
          <ChessBoard
            :pieces="store.board"
            :is-red-view="isRedView"
            :selected-coord="store.selectedCoord"
            @cell-click="onCellClick"
          />
        </div>
      </main>

      <!-- ========= 右栏：操作 ========= -->
      <aside class="side-col">
        <div class="action-card">
          <h3 class="action-title">操作</h3>
          <button @click="onResign" class="action-btn btn-resign">🏳️ 认 输</button>
          <button class="action-btn btn-draw">🤝 提 和</button>
          <button class="action-btn btn-chat">💬 聊 天</button>
        </div>

        <div class="info-card">
          <p>房间: {{ store.room?.roomId?.slice(-6) }}</p>
          <p>状态: {{ store.connectionStatus }}</p>
        </div>
      </aside>
    </div>

    <!-- 对局结束弹窗 -->
    <div v-if="store.gameOver" class="modal">
      <div class="modal-card">
        <h3>{{ store.gameOver.winner === yourColor ? '你赢了！' : (store.gameOver.winner === 'draw' ? '和棋' : '你输了') }}</h3>
        <p>原因：{{ store.gameOver.reason }}</p>
        <button @click="router.push('/lobby')" class="modal-btn">返回大厅</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  min-height: 100vh;
  padding: 16px;
  display: flex;
  align-items: center;
}

.layout {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 220px 1fr 200px;
  gap: 24px;
  align-items: stretch;
  min-height: calc(100vh - 32px);
}

/* 左右栏：用 flex 实现"上下贴齐" */
.side-col {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-height: 100%;
}

/* 玩家卡片 */
.player-card {
  background: rgba(255, 248, 230, 0.95);
  border: 2px solid rgba(74, 36, 16, 0.45);
  border-radius: 10px;
  padding: 14px;
  transition: all 0.2s;
}
.player-active {
  border-color: #d97706;
  border-width: 3px;
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.2), 0 4px 12px rgba(0, 0, 0, 0.1);
}
.player-idle { opacity: 0.6; }

.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.avatar {
  width: 40px; height: 40px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 18px;
  border: 2px solid rgba(0, 0, 0, 0.2);
  color: white;
}
.avatar-red { background: linear-gradient(135deg, #dc2626, #991b1b); }
.avatar-black { background: linear-gradient(135deg, #44403c, #1c1917); }

.side-tag {
  font-size: 12px;
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 4px;
}
.tag-red { background: #dc2626; color: white; }
.tag-black { background: #1c1917; color: white; }
.tag-you { background: #fbbf24; color: #78350f; }

.player-name {
  font-size: 20px;
  font-weight: bold;
  color: #5d2f0d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.player-role {
  font-size: 12px;
  color: rgba(93, 47, 13, 0.6);
  margin-top: 2px;
}

/* 计时器 */
.clock-card {
  background: rgba(255, 248, 230, 0.95);
  border: 2px solid rgba(74, 36, 16, 0.45);
  border-radius: 10px;
  padding: 14px;
}
.clock-label, .clock-state {
  text-align: center;
  font-size: 12px;
  color: rgba(93, 47, 13, 0.7);
}
.clock-label { margin-bottom: 6px; }
.clock-state { margin-top: 8px; }
.clock-display {
  border-radius: 6px;
  padding: 14px;
  text-align: center;
  transition: all 0.2s;
}
.clock-active {
  background: #dc2626;
  box-shadow: 0 0 16px rgba(220, 38, 38, 0.5);
}
.clock-idle { background: #1c1917; }
.clock-text {
  font-family: 'Menlo', 'Monaco', monospace;
  font-size: 32px;
  font-weight: bold;
  color: #fde68a;
  letter-spacing: 3px;
}

/* 棋盘列：让棋盘居中且尽量大 */
.board-col {
  display: flex;
  align-items: center;
  justify-content: center;
}
.board-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}
.error-banner {
  width: 100%;
  max-width: 880px;
  margin-bottom: 12px;
  padding: 10px 16px;
  background: #fef3c7;
  border: 2px solid #d97706;
  border-radius: 8px;
  color: #78350f;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.err-close {
  background: transparent;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #78350f;
  padding: 0 6px;
}

/* 操作卡 */
.action-card {
  background: rgba(255, 248, 230, 0.95);
  border: 2px solid rgba(74, 36, 16, 0.45);
  border-radius: 10px;
  padding: 14px;
}
.action-title {
  text-align: center;
  font-size: 14px;
  font-weight: bold;
  color: #5d2f0d;
  margin-bottom: 10px;
}
.action-btn {
  display: block;
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  font-weight: bold;
  font-size: 14px;
  color: white;
  margin-bottom: 8px;
  transition: all 0.15s;
  cursor: pointer;
  border: none;
}
.action-btn:last-child { margin-bottom: 0; }
.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.2);
}
.btn-resign { background: #991b1b; }
.btn-resign:hover { background: #7f1d1d; }
.btn-draw { background: #c2410c; }
.btn-draw:hover { background: #9a3412; }
.btn-chat { background: #525252; }
.btn-chat:hover { background: #404040; }

.info-card {
  background: rgba(255, 248, 230, 0.6);
  border: 1px solid rgba(74, 36, 16, 0.2);
  border-radius: 6px;
  padding: 10px;
  font-size: 12px;
  color: rgba(93, 47, 13, 0.7);
}
.info-card p { margin: 2px 0; }

/* 弹窗 */
.modal {
  position: fixed; inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex; align-items: center; justify-content: center;
  z-index: 50;
}
.modal-card {
  background: #fef3c7;
  border: 2px solid #5d2f0d;
  border-radius: 12px;
  padding: 32px;
  max-width: 400px;
  text-align: center;
}
.modal-card h3 {
  font-size: 28px;
  font-weight: bold;
  color: #5d2f0d;
  margin-bottom: 8px;
}
.modal-card p {
  color: #78350f;
  margin-bottom: 16px;
}
.modal-btn {
  background: #5d2f0d;
  color: #fef3c7;
  padding: 10px 24px;
  border-radius: 6px;
  font-weight: bold;
  border: none;
  cursor: pointer;
}
</style>
