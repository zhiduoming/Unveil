<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGameStore } from '../stores/game'
import { initialJieqiBoard } from '../types/chess'
import ChessBoard from '../components/ChessBoard.vue'
import CapturedTray from '../components/CapturedTray.vue'

const router = useRouter()
const store = useGameStore()

let clockTimer: number | undefined

// 对局结束后"查看棋局"模式：true 时暂时隐藏结算弹窗，让用户复盘终局
const viewingFinishedBoard = ref(false)

// 对局结束状态变化时（新结束 / 已清空）重置查看模式
watch(() => store.gameOver, (v) => {
  if (!v) viewingFinishedBoard.value = false
})

// ── 屏幕中央浮动 toast（3 秒淡化，半透明，不挡棋盘） ──
type ToastKind = 'check' | 'error' | 'info'
const toastMessage = ref<string>('')
const toastKind = ref<ToastKind>('info')
let toastTimer: number | undefined
const pendingConfirm = ref<null | 'draw' | 'resign'>(null)
const chatDraft = ref('')
const chatLogRef = ref<HTMLElement | null>(null)
type BattleKind = 'move' | 'capture' | 'check'
const battleBadge = ref<null | { kind: 'capture' | 'check'; key: number }>(null)
let battleTimer: number | undefined
const battleSoundFiles: Record<BattleKind, string> = {
  move: '/sounds/move.wav',
  capture: '/sounds/chi.wav',
  check: '/sounds/jiangjun.wav',
}
const battleSoundCache: Partial<Record<BattleKind, HTMLAudioElement>> = {}

function showToast(msg: string, kind: ToastKind = 'info', durationMs = 3000) {
  toastMessage.value = msg
  toastKind.value = kind
  if (toastTimer !== undefined) clearTimeout(toastTimer)
  toastTimer = window.setTimeout(() => {
    toastMessage.value = ''
    // 错误同步清掉 store.lastError，避免下次同样的错误不再触发 watch
    if (kind === 'error') store.lastError = ''
  }, durationMs)
}

onMounted(() => {
  if (!store.gameStart) router.replace('/lobby')
  if (store.board.length === 0) store.board = initialJieqiBoard()
  clockTimer = window.setInterval(() => store.tickClock(), 1000)
  store.tickClock()
  preloadBattleSounds()
})

onUnmounted(() => {
  if (clockTimer !== undefined) clearInterval(clockTimer)
  if (toastTimer !== undefined) clearTimeout(toastTimer)
  if (battleTimer !== undefined) clearTimeout(battleTimer)
})

const yourColor = computed(() => store.gameStart?.yourColor)
const isRedView = computed(() => yourColor.value === 'red')
const isMyTurn = computed(() => store.currentTurn === yourColor.value)
const isAiGame = computed(() => store.room?.opponentId === 'ai_bot')
const isAiBattle = computed(() => store.room?.mode === 'aiBattle')
const isHumanGame = computed(() => store.room?.mode === 'human')
const gameOverTitle = computed(() => {
  if (!store.gameOver) return ''
  if (store.gameOver.winner === 'draw') return '和棋'
  return store.gameOver.winner === yourColor.value ? '你赢了' : '你输了'
})

// 倒计时（mm:ss）
const remainText = computed(() => {
  const s = store.remainSeconds
  if (s < 0) return '--:--'
  const m = Math.floor(s / 60)
  const ss = s % 60
  return `${String(m).padStart(2, '0')}:${String(ss).padStart(2, '0')}`
})
const isTimeWarning = computed(() => store.remainSeconds >= 0 && store.remainSeconds <= 10)

// 监听 store 状态触发 toast
watch(() => store.lastError, (err) => {
  if (err) showToast(err, 'error')
})
watch(() => store.battleEffectSeq, () => {
  const effect = store.battleEffect
  if (!effect) return
  playBattleSound(effect.kind)
  if (effect.kind === 'move') return
  battleBadge.value = { kind: effect.kind, key: effect.seq }
  if (battleTimer !== undefined) clearTimeout(battleTimer)
  battleTimer = window.setTimeout(() => {
    battleBadge.value = null
  }, effect.kind === 'check' ? 1450 : 1150)
})
watch(() => store.chatMessages.length, async () => {
  await nextTick()
  if (chatLogRef.value) {
    chatLogRef.value.scrollTop = chatLogRef.value.scrollHeight
  }
})

function preloadBattleSounds() {
  for (const kind of ['move', 'capture', 'check'] as const) {
    const audio = new Audio(battleSoundFiles[kind])
    audio.preload = 'auto'
    audio.volume = 1
    battleSoundCache[kind] = audio
  }
}

function primeBattleSounds() {
  for (const audio of Object.values(battleSoundCache)) {
    if (!audio) continue
    audio.load()
  }
}

function playBattleSound(kind: BattleKind) {
  const base = battleSoundCache[kind] || new Audio(battleSoundFiles[kind])
  const audio = base.cloneNode(true) as HTMLAudioElement
  audio.volume = kind === 'move' ? 0.65 : kind === 'check' ? 1 : 0.95
  audio.currentTime = 0
  void audio.play().catch(() => {
    // 浏览器如果拦截了第一次自动播放，下一次用户点击棋盘后会重新预加载。
  })
}

// 终局提示（前端规则推断；用户可点认输或忽略）
const endgameModal = computed(() => {
  const v = store.endgameVerdict
  if (!v) return null
  const youLose = v.loserColor === yourColor.value
  if (v.kind === 'checkmate') {
    return youLose
      ? { title: '你被将死', desc: '没有任何合法走法可以解除将军。点击认输结束对局。', canResign: true }
      : { title: '对方被将死', desc: '对方已无任何合法走法解将。等待服务端判定，或对方认输。', canResign: false }
  }
  // stalemate
  return youLose
    ? { title: '困毙', desc: '你没有任何合法走法（未被将军）。按象棋规则也判负。', canResign: true }
    : { title: '对方困毙', desc: '对方无任何合法走法。等待服务端判定。', canResign: false }
})

function dismissEndgame() {
  store.endgameVerdict = null
}
function endgameResign() {
  pendingConfirm.value = 'resign'
  store.endgameVerdict = null
}

function onCellClick(row: number, col: number) {
  primeBattleSounds()
  store.selectCell(row, col)
}

function onResign() {
  pendingConfirm.value = 'resign'
}

function onOfferDraw() {
  pendingConfirm.value = 'draw'
}

function closeConfirm() {
  pendingConfirm.value = null
}

function confirmAction() {
  if (pendingConfirm.value === 'resign') store.resign()
  if (pendingConfirm.value === 'draw') store.offerDraw()
  pendingConfirm.value = null
}

function sendChat() {
  const text = chatDraft.value
  store.sendChat(text)
  if (text.trim()) chatDraft.value = ''
}

function backToLobby() {
  store.returnToLobby()
  router.push('/lobby')
}
</script>

<template>
  <div class="page">
    <div class="layout">
      <!-- ========= 左栏 ========= -->
      <aside class="side-col player-side-col">
        <!-- 对手卡片 -->
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

        <!-- 计时器 -->
        <div
          class="clock-card"
          :class="[isMyTurn ? 'clock-card-active' : 'clock-card-idle', isTimeWarning ? 'clock-card-warning' : '']"
        >
          <div class="clock-label">
            <span class="clock-dot"></span>
            步时
          </div>
          <div
            class="clock-display"
            :class="[isMyTurn ? 'clock-active' : 'clock-idle', isTimeWarning ? 'clock-warning' : '']"
          >
            <span class="clock-text">{{ remainText }}</span>
          </div>
          <div class="clock-state">{{ isMyTurn ? '轮到你走' : '等待对手' }}</div>
        </div>

        <!-- 自己卡片 -->
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
          <!-- 右上：我方被吃的棋子（损失）。暗子被吃倒扣不显身份。 -->
          <div class="captured-bar captured-bar-top">
            <CapturedTray :entries="store.capturedFromMe" variant="loss" />
          </div>
          <ChessBoard
            :pieces="store.board"
            :is-red-view="isRedView"
            :selected-coord="store.selectedCoord"
            :hint-coords="store.hintCoords"
            :last-move="store.lastMove"
            @cell-click="onCellClick"
          />
          <!-- 左下：我方吃掉对方的棋子（战利品）。暗子被吃可见身份但变暗。 -->
          <div class="captured-bar captured-bar-bottom">
            <CapturedTray :entries="store.capturedByMe" variant="trophy" />
          </div>
          <Transition name="battle-pop">
            <div
              v-if="battleBadge"
              :key="battleBadge.key"
              class="battle-overlay"
              :class="battleBadge.kind === 'check' ? 'battle-check' : 'battle-capture'"
            >
              <div class="battle-burst"></div>
              <div class="battle-blades" aria-hidden="true"></div>
              <div class="battle-text">{{ battleBadge.kind === 'check' ? '将军' : '吃' }}</div>
              <div class="battle-ribbon"></div>
              <span v-for="n in 8" :key="n" class="battle-spark" :style="{ '--i': n }"></span>
            </div>
          </Transition>
        </div>
      </main>

      <!-- ========= 右栏：操作 ========= -->
      <aside class="side-col">
        <div class="action-card">
          <h3 class="action-title">操作</h3>
          <!-- AI 对弈（人机 / AI 观战）：暂停/继续 + 结束对局 -->
          <template v-if="isAiGame || isAiBattle">
            <button
              v-if="!store.paused"
              @click="store.pauseAi()"
              :disabled="!!store.gameOver"
              class="action-btn btn-draw"
            >⏸ 暂停对局</button>
            <button
              v-else
              @click="store.resumeAi()"
              :disabled="!!store.gameOver"
              class="action-btn btn-draw"
            >▶ 继续对局</button>
            <button @click="backToLobby" class="action-btn btn-resign">🚪 结束对局</button>
          </template>
          <!-- 真人对局：完整操作 -->
          <template v-else>
            <button @click="onResign" :disabled="!!store.gameOver" class="action-btn btn-resign">🏳️ 认 输</button>
            <button
              @click="onOfferDraw"
              :disabled="store.myDrawOffered || !!store.drawOfferFrom || !!store.gameOver"
              class="action-btn btn-draw"
            >
              {{ store.myDrawOffered ? '等待回应' : '🤝 提 和' }}
            </button>
          </template>
        </div>

        <div v-if="isHumanGame" class="chat-card">
          <div class="chat-head">
            <h3 class="chat-title">局内聊天</h3>
            <span class="chat-count">{{ store.chatMessages.length }}</span>
          </div>
          <div ref="chatLogRef" class="chat-log">
            <div v-if="store.chatMessages.length === 0" class="chat-empty">还没有消息</div>
            <div
              v-for="msg in store.chatMessages"
              :key="msg.id"
              class="chat-row"
              :class="msg.mine ? 'chat-row-mine' : 'chat-row-opponent'"
            >
              <div class="chat-meta">
                <span class="chat-side" :class="msg.fromColor === 'red' ? 'chat-side-red' : 'chat-side-black'">
                  {{ msg.fromColor === 'red' ? '红方' : '黑方' }}
                </span>
                <span class="chat-name">{{ msg.mine ? '你' : msg.fromUserId }}</span>
              </div>
              <div class="chat-bubble">{{ msg.content }}</div>
            </div>
          </div>
          <div class="chat-input-row">
            <input
              v-model="chatDraft"
              class="chat-input"
              maxlength="120"
              placeholder="输入消息"
              :disabled="!!store.gameOver"
              @keydown.enter.prevent="sendChat"
            />
            <button class="chat-send" :disabled="!!store.gameOver || !chatDraft.trim()" @click="sendChat">发送</button>
          </div>
        </div>

        <div class="info-card">
          <p>房间: {{ store.room?.roomId?.slice(-6) }}</p>
          <p>状态: {{ store.connectionStatusText }}</p>
        </div>
      </aside>
    </div>

    <!-- 屏幕中央浮动 Toast（将军 / 错误，3 秒后淡化） -->
    <Transition name="toast-fade">
      <div v-if="toastMessage" class="toast" :class="`toast-${toastKind}`">
        {{ toastMessage }}
      </div>
    </Transition>

    <!-- 终局推断弹窗（前端规则推断，不直接结束对局） -->
    <div v-if="store.drawOfferFrom && !store.gameOver" class="modal">
      <div class="modal-card">
        <h3>对方提和</h3>
        <p>{{ store.drawOfferFrom }} 请求提和。</p>
        <div class="modal-actions">
          <button @click="store.acceptDraw()" class="modal-btn modal-btn-primary">同 意</button>
          <button @click="store.declineDraw()" class="modal-btn modal-btn-secondary">拒 绝</button>
        </div>
      </div>
    </div>

    <div v-if="endgameModal && !store.gameOver" class="modal">
      <div class="modal-card">
        <h3>{{ endgameModal.title }}</h3>
        <p>{{ endgameModal.desc }}</p>
        <div class="modal-actions">
          <button v-if="endgameModal.canResign" @click="endgameResign" class="modal-btn modal-btn-danger">
            认 输
          </button>
          <button @click="dismissEndgame" class="modal-btn modal-btn-secondary">
            关闭提示
          </button>
        </div>
      </div>
    </div>

    <!-- 对局结束弹窗（服务端判定）— 集成 rematch 流程 -->
    <div v-if="store.gameOver && !viewingFinishedBoard" class="modal">
      <div class="modal-card">
        <h3>{{ gameOverTitle }}</h3>
        <p class="rematch-hint">原因：{{ store.gameOverReasonText }}</p>

        <!-- 收到对方邀请：接受 / 拒绝 -->
        <div v-if="store.rematchOfferFrom && !store.myRematchAsked" class="rematch-hint">
          🤝 对方 <b>{{ store.rematchOfferFrom }}</b> 邀请再来一局
        </div>
        <!-- 我已邀请、等对方反应 -->
        <p v-else-if="store.myRematchAsked && !store.rematchDeclinedBy" class="rematch-hint">
          ⏳ 已发出"再来一局"邀请，等对方决定…
        </p>
        <!-- 对方拒绝 -->
        <p v-else-if="store.rematchDeclinedBy" class="rematch-hint rematch-hint-danger">
          ❌ 对方拒绝了再来一局
        </p>

        <div class="modal-actions">
          <!-- 场景 1：收到邀请 -->
          <template v-if="store.rematchOfferFrom && !store.myRematchAsked">
            <button @click="store.requestRematch()" class="modal-btn modal-btn-primary">接 受</button>
            <button @click="store.declineRematch(); backToLobby()" class="modal-btn modal-btn-secondary">拒绝并返回大厅</button>
          </template>
          <!-- 场景 2：还未邀请，正常按钮 -->
          <template v-else-if="!store.myRematchAsked">
            <button @click="store.requestRematch()" class="modal-btn modal-btn-primary">🔁 再来一局</button>
            <button @click="viewingFinishedBoard = true" class="modal-btn modal-btn-secondary">🔍 查看棋局</button>
            <button @click="backToLobby" class="modal-btn modal-btn-secondary">返回大厅</button>
          </template>
          <!-- 场景 3：已邀请等待 / 被拒 -->
          <template v-else>
            <button @click="viewingFinishedBoard = true" class="modal-btn modal-btn-secondary">🔍 查看棋局</button>
            <button @click="backToLobby" class="modal-btn modal-btn-secondary">返回大厅</button>
          </template>
        </div>
      </div>
    </div>

    <!-- 查看棋局模式：右下角悬浮"返回结算"按钮，点击重新打开结算弹窗 -->
    <div v-if="store.gameOver && viewingFinishedBoard" class="finished-bar">
      <span class="finished-bar-text">
        {{ store.gameOver.winner === yourColor ? '✓ 你赢了' : (store.gameOver.winner === 'draw' ? '⚖ 和棋' : '✗ 你输了') }}
        · 原因：{{ store.gameOverReasonText }}
        · 复盘模式
      </span>
      <button @click="viewingFinishedBoard = false" class="finished-bar-btn">返回结算</button>
      <button @click="backToLobby" class="finished-bar-btn finished-bar-btn-secondary">返回大厅</button>
    </div>

    <div v-if="pendingConfirm" class="modal">
      <div class="modal-card confirm-card">
        <h3>{{ pendingConfirm === 'resign' ? '确认认输' : '确认提和' }}</h3>
        <p>
          {{ pendingConfirm === 'resign'
            ? '认输后对局会立即结束，对方获胜。'
            : '将向对方发送提和请求，对方同意后对局将以和棋结束。' }}
        </p>
        <div class="modal-actions">
          <button
            @click="confirmAction"
            class="modal-btn"
            :class="pendingConfirm === 'resign' ? 'modal-btn-danger' : 'modal-btn-primary'"
          >
            {{ pendingConfirm === 'resign' ? '确认认输' : '发送提和' }}
          </button>
          <button @click="closeConfirm" class="modal-btn modal-btn-secondary">取 消</button>
        </div>
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
  grid-template-columns: 210px minmax(620px, 1fr) 270px;
  gap: 28px;
  align-items: stretch;
  min-height: calc(100vh - 32px);
}

.side-col {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.player-side-col {
  justify-content: center;
  gap: clamp(44px, 8vh, 88px);
  padding-block: clamp(34px, 6vh, 76px);
}

.layout > .side-col:last-child {
  justify-content: flex-start;
  gap: 14px;
  padding-top: clamp(14px, 2.2vh, 28px);
}

/* 玩家卡片 */
.player-card {
  width: 100%;
  min-height: 108px;
  background:
    linear-gradient(180deg, rgba(255, 250, 236, 0.96), rgba(250, 239, 213, 0.92));
  border: 1px solid rgba(74, 36, 16, 0.28);
  border-radius: 8px;
  padding: 12px 13px;
  box-shadow:
    0 10px 26px rgba(78, 45, 18, 0.09),
    inset 0 1px 0 rgba(255, 255, 255, 0.5);
  transition: border-color 0.2s, box-shadow 0.2s, opacity 0.2s, transform 0.2s;
}
.player-active {
  border-color: #d97706;
  box-shadow:
    0 0 0 2px rgba(217, 119, 6, 0.18),
    0 14px 30px rgba(78, 45, 18, 0.14),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
  transform: translateX(3px);
}
.player-idle {
  opacity: 0.72;
}

.card-head {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-bottom: 9px;
  flex-wrap: wrap;
}
.avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; font-size: 16px;
  border: 2px solid rgba(255, 248, 230, 0.75);
  color: white;
  box-shadow: 0 3px 8px rgba(52, 28, 12, 0.18);
}
.avatar-red { background: linear-gradient(135deg, #dc2626, #991b1b); }
.avatar-black { background: linear-gradient(135deg, #44403c, #1c1917); }

.side-tag {
  font-size: 11px;
  font-weight: 800;
  line-height: 1;
  padding: 4px 7px;
  border-radius: 4px;
}
.tag-red { background: #dc2626; color: white; }
.tag-black { background: #1c1917; color: white; }
.tag-you { background: #fbbf24; color: #78350f; }

.player-name {
  font-size: 17px;
  font-weight: 800;
  color: #5d2f0d;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.25;
}
.player-role {
  font-size: 12px;
  color: rgba(93, 47, 13, 0.6);
  margin-top: 5px;
}

/* 计时器 */
.clock-card {
  width: 100%;
  background:
    linear-gradient(180deg, rgba(255, 251, 241, 0.97), rgba(249, 235, 205, 0.94));
  border: 1px solid rgba(74, 36, 16, 0.26);
  border-radius: 8px;
  padding: 12px;
  box-shadow:
    0 12px 30px rgba(78, 45, 18, 0.10),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.clock-card-active {
  border-color: rgba(217, 119, 6, 0.62);
  box-shadow:
    0 0 0 2px rgba(217, 119, 6, 0.13),
    0 14px 30px rgba(78, 45, 18, 0.13),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
}
.clock-card-warning {
  border-color: rgba(185, 28, 28, 0.72);
}
.clock-label {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-bottom: 8px;
  text-align: center;
  font-size: 12px;
  font-weight: 800;
  color: rgba(93, 47, 13, 0.58);
}
.clock-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: #a8a29e;
  box-shadow: 0 0 0 3px rgba(168, 162, 158, 0.14);
}
.clock-card-active .clock-dot {
  background: #d97706;
  box-shadow: 0 0 0 3px rgba(217, 119, 6, 0.18);
}
.clock-card-warning .clock-dot {
  background: #dc2626;
  box-shadow: 0 0 0 3px rgba(220, 38, 38, 0.16);
}
.clock-state {
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  color: rgba(93, 47, 13, 0.58);
}
.clock-state { margin-top: 9px; }
.clock-display {
  border-radius: 7px;
  padding: 11px 8px 10px;
  text-align: center;
  border: 1px solid rgba(74, 36, 16, 0.18);
  transition: background 0.2s, box-shadow 0.2s, border-color 0.2s;
}
.clock-active {
  background: linear-gradient(180deg, #8a4f1f, #5d2f0d);
  border-color: rgba(93, 47, 13, 0.42);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.12),
    0 8px 18px rgba(93, 47, 13, 0.18);
}
.clock-idle {
  background: linear-gradient(180deg, #57534e, #292524);
  border-color: rgba(41, 37, 36, 0.36);
  opacity: 0.88;
}
.clock-warning {
  background: linear-gradient(180deg, #dc2626, #991b1b);
  border-color: rgba(127, 29, 29, 0.62);
  animation: clockBlink 1s infinite;
}
@keyframes clockBlink {
  0%, 100% { box-shadow: 0 8px 18px rgba(153, 27, 27, 0.28); }
  50%      { box-shadow: 0 8px 26px rgba(220, 38, 38, 0.62); }
}
.clock-text {
  font-family: 'Menlo', 'Monaco', monospace;
  font-size: 29px;
  font-weight: 900;
  color: #fde68a;
  letter-spacing: 1px;
  line-height: 1;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.34);
}

/* 棋盘列：让棋盘居中且尽量大 */
.board-col {
  display: flex;
  align-items: center;
  justify-content: center;
}
.board-stack {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

/* 被吃棋子展示条：上方（损失）靠右、下方（战利品）靠左 */
.captured-bar {
  width: 100%;
  max-width: 880px;
  padding: 4px 7%;
  box-sizing: border-box;
}
.captured-bar-top { margin-bottom: 4px; }
.captured-bar-bottom { margin-top: 4px; }

.battle-overlay {
  position: absolute;
  inset: 0;
  z-index: 35;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  overflow: hidden;
}
.battle-burst {
  position: absolute;
  width: min(48vw, 520px);
  aspect-ratio: 1;
  border-radius: 999px;
  background:
    radial-gradient(circle, rgba(255, 243, 160, 0.95) 0 9%, rgba(248, 113, 22, 0.82) 10% 24%, rgba(127, 29, 29, 0.48) 25% 42%, rgba(0, 0, 0, 0) 63%),
    conic-gradient(from 15deg, rgba(255, 214, 102, 0), rgba(251, 146, 60, 0.9), rgba(127, 29, 29, 0), rgba(255, 214, 102, 0.7), rgba(0, 0, 0, 0));
  filter: blur(1px);
  transform: scale(0.9);
}
.battle-blades {
  position: absolute;
  width: min(42vw, 440px);
  height: 104px;
  transform: rotate(-13deg);
  background:
    linear-gradient(90deg, transparent 0 8%, #fefce8 9% 19%, #713f12 20% 23%, transparent 24% 76%, #713f12 77% 80%, #fefce8 81% 91%, transparent 92%),
    linear-gradient(0deg, transparent 0 44%, rgba(255, 255, 255, 0.82) 45% 52%, transparent 53%);
  clip-path: polygon(0 50%, 17% 17%, 36% 44%, 64% 44%, 83% 17%, 100% 50%, 83% 83%, 64% 56%, 36% 56%, 17% 83%);
  opacity: 0.86;
  mix-blend-mode: screen;
}
.battle-text {
  position: relative;
  z-index: 2;
  font-family: "Songti SC", "STSong", "Noto Serif CJK SC", serif;
  font-size: clamp(86px, 13vw, 180px);
  font-weight: 950;
  line-height: 0.88;
  letter-spacing: 0;
  color: #fef08a;
  -webkit-text-stroke: clamp(5px, 0.9vw, 12px) #4a1608;
  text-shadow:
    0 3px 0 #7c2d12,
    0 8px 0 #991b1b,
    0 13px 18px rgba(69, 26, 3, 0.78),
    0 0 24px rgba(251, 146, 60, 0.9);
  transform: skewX(-7deg);
}
.battle-check .battle-text {
  color: #fff7ad;
  -webkit-text-stroke-color: #3b0a05;
  text-shadow:
    0 4px 0 #b91c1c,
    0 9px 0 #7f1d1d,
    0 15px 22px rgba(69, 10, 10, 0.8),
    0 0 30px rgba(251, 191, 36, 0.95);
}
.battle-ribbon {
  position: absolute;
  z-index: 1;
  width: min(44vw, 500px);
  height: 54px;
  margin-top: clamp(74px, 11vw, 145px);
  background: linear-gradient(90deg, #7f1d1d, #f59e0b 18%, #fef3c7 50%, #f59e0b 82%, #7f1d1d);
  border: 4px solid #7c2d12;
  box-shadow: 0 8px 18px rgba(69, 26, 3, 0.45);
  clip-path: polygon(0 20%, 9% 20%, 12% 0, 88% 0, 91% 20%, 100% 20%, 94% 50%, 100% 80%, 91% 80%, 88% 100%, 12% 100%, 9% 80%, 0 80%, 6% 50%);
}
.battle-spark {
  position: absolute;
  width: 10px;
  height: 28px;
  border-radius: 999px;
  background: linear-gradient(#fef08a, #f97316);
  box-shadow: 0 0 16px rgba(251, 146, 60, 0.9);
  transform:
    rotate(calc(var(--i) * 42deg))
    translateY(clamp(-190px, -18vw, -94px));
  opacity: 0.95;
}

.battle-pop-enter-active {
  animation: battlePop 0.34s cubic-bezier(0.16, 1.2, 0.28, 1);
}
.battle-pop-leave-active {
  transition: opacity 0.42s ease, transform 0.42s ease;
}
.battle-pop-leave-to {
  opacity: 0;
  transform: scale(1.08);
}
@keyframes battlePop {
  0% {
    opacity: 0;
    transform: scale(0.54) rotate(-2deg);
  }
  70% {
    opacity: 1;
    transform: scale(1.08) rotate(1deg);
  }
  100% {
    transform: scale(1) rotate(0);
  }
}
/* 屏幕中央浮动 Toast */
.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 100;
  min-width: 260px;
  max-width: 90vw;
  padding: 22px 38px;
  border-radius: 16px;
  font-size: 26px;
  font-weight: 800;
  text-align: center;
  pointer-events: none;             /* 不挡棋子点击 */
  /* 高透明：背后棋盘清晰可见；文字靠描边阴影保证可读 */
  background: rgba(20, 12, 6, 0.18);
  color: #fff7e0;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
  backdrop-filter: blur(1.5px);
  -webkit-backdrop-filter: blur(1.5px);
  border: 1.5px solid rgba(255, 255, 255, 0.22);
  /* 文字描边 + 阴影：高对比，无需背景遮挡 */
  text-shadow:
    0 0 6px rgba(0, 0, 0, 0.85),
    0 2px 4px rgba(0, 0, 0, 0.7),
    -1px -1px 0 rgba(0, 0, 0, 0.6),
    1px 1px 0 rgba(0, 0, 0, 0.6);
}
.toast-check {
  background: rgba(153, 27, 27, 0.22);
  border-color: rgba(254, 226, 226, 0.45);
  color: #fff;
}
.toast-error {
  background: rgba(120, 53, 15, 0.22);
  border-color: rgba(254, 243, 199, 0.45);
  font-size: 18px;
  font-weight: 700;
}

/* Toast 出入场动画：进 0.25s 缩放淡入，出 0.6s 淡出 */
.toast-fade-enter-active {
  transition: opacity 0.25s ease-out, transform 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}
.toast-fade-leave-active {
  transition: opacity 0.6s ease-in, transform 0.6s ease-in;
}
.toast-fade-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.7);
}
.toast-fade-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) scale(1.05);
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
.chat-card {
  background:
    linear-gradient(180deg, rgba(255, 248, 230, 0.96), rgba(250, 239, 213, 0.9));
  border: 2px solid rgba(74, 36, 16, 0.38);
  border-radius: 10px;
  padding: 12px;
  color: #5d2f0d;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.36);
}
.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.chat-title {
  font-size: 14px;
  font-weight: 800;
  color: #5d2f0d;
}
.chat-count {
  min-width: 24px;
  height: 20px;
  padding: 0 7px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: rgba(120, 53, 15, 0.14);
  color: rgba(93, 47, 13, 0.76);
  font-size: 12px;
  font-weight: 800;
}
.chat-log {
  height: clamp(250px, 38vh, 430px);
  overflow-y: auto;
  padding: 10px;
  border: 1px solid rgba(74, 36, 16, 0.2);
  border-radius: 8px;
  background:
    linear-gradient(180deg, rgba(255, 251, 235, 0.76), rgba(254, 243, 199, 0.5));
}
.chat-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(93, 47, 13, 0.48);
  font-size: 13px;
  font-weight: 700;
}
.chat-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}
.chat-row:last-child {
  margin-bottom: 0;
}
.chat-row-mine {
  align-items: flex-end;
}
.chat-row-opponent {
  align-items: flex-start;
}
.chat-meta {
  display: flex;
  align-items: center;
  gap: 5px;
  max-width: 100%;
  margin-bottom: 3px;
  font-size: 11px;
  color: rgba(93, 47, 13, 0.64);
}
.chat-side {
  padding: 1px 5px;
  border-radius: 4px;
  font-weight: 800;
  color: #fff7ed;
}
.chat-side-red {
  background: #991b1b;
}
.chat-side-black {
  background: #292524;
}
.chat-name {
  max-width: 118px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 700;
}
.chat-bubble {
  max-width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  font-size: 13px;
  line-height: 1.45;
  word-break: break-word;
  white-space: pre-wrap;
  border: 1px solid rgba(74, 36, 16, 0.18);
}
.chat-row-mine .chat-bubble {
  background: #7c2d12;
  color: #fff7ed;
  border-color: rgba(124, 45, 18, 0.45);
}
.chat-row-opponent .chat-bubble {
  background: rgba(255, 251, 235, 0.92);
  color: #5d2f0d;
}
.chat-input-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 54px;
  gap: 8px;
  margin-top: 10px;
}
.chat-input {
  min-width: 0;
  height: 34px;
  border: 1px solid rgba(74, 36, 16, 0.28);
  border-radius: 6px;
  padding: 0 9px;
  background: rgba(255, 251, 235, 0.94);
  color: #5d2f0d;
  font-size: 13px;
  outline: none;
}
.chat-input:focus {
  border-color: #92400e;
  box-shadow: 0 0 0 2px rgba(146, 64, 14, 0.18);
}
.chat-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.chat-send {
  height: 34px;
  border: none;
  border-radius: 6px;
  background: #5d2f0d;
  color: #fef3c7;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}
.chat-send:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.chat-send:not(:disabled):hover {
  background: #78350f;
}

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
.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 8px;
}
.modal-btn-danger {
  background: #991b1b;
}
.modal-btn-secondary {
  background: #78716c;
}
.modal-btn-primary {
  background: #15803d;
}
.modal-btn-primary:hover {
  background: #166534;
}

/* 查看棋局（复盘模式）悬浮条 */
.finished-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 18px;
  background: rgba(28, 25, 23, 0.92);
  border: 1px solid rgba(217, 119, 6, 0.5);
  border-radius: 999px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  z-index: 50;
}
.finished-bar-text {
  color: #fef3c7;
  font-size: 14px;
  font-weight: 500;
  padding-right: 4px;
}
.finished-bar-btn {
  padding: 6px 14px;
  border-radius: 999px;
  background: #d97706;
  color: #fef3c7;
  font-size: 13px;
  cursor: pointer;
  border: none;
}
.finished-bar-btn:hover {
  background: #b45309;
}
.finished-bar-btn-secondary {
  background: #57534e;
}
.finished-bar-btn-secondary:hover {
  background: #44403c;
}
.rematch-hint {
  margin: 12px 0;
  padding: 10px 14px;
  background: #fef3c7;
  border: 1px solid #d97706;
  border-radius: 8px;
  color: #78350f;
  font-size: 15px;
  text-align: center;
}
.rematch-hint-danger {
  background: #fee2e2;
  border-color: #b91c1c;
  color: #7f1d1d;
}

@media (max-width: 1100px) {
  .layout {
    grid-template-columns: minmax(160px, 190px) minmax(520px, 1fr);
    grid-template-areas:
      "players board"
      "actions board";
    gap: 18px;
  }

  .player-side-col {
    grid-area: players;
    gap: 18px;
    padding-block: 0;
  }

  .board-col {
    grid-area: board;
  }

  .layout > .side-col:last-child {
    grid-area: actions;
    padding-top: 0;
  }
}
</style>
