<script setup lang="ts">
import { computed } from 'vue'
import type { CapturedEntry, Color, PieceType } from '../types/chess'
import { PIECE_CHAR } from '../types/chess'

const props = defineProps<{
  entries: CapturedEntry[]
  // trophy = 我方吃掉对方的（左下，可见真实身份；暗子被吃变暗）
  // loss   = 对方吃掉我方的（右上；未知暗子倒扣不显身份）
  variant: 'trophy' | 'loss'
}>()

interface TrayItem {
  key: string
  color: Color
  type: PieceType | null
  wasDark: boolean
  count: number
}

// 聚合同身份棋子为一条 + 数字角标。
//   trophy：按 (type, 明/暗) 分组——保留「暗子被吃变暗」的语义。
//   loss  ：已知身份按 type 分组；未知暗子全部合并为一条「倒扣」。
const items = computed<TrayItem[]>(() => {
  const map = new Map<string, TrayItem>()
  for (const e of props.entries) {
    const unknown = e.type == null
    const key = unknown
      ? 'unknown'
      : props.variant === 'trophy'
        ? `${e.type}:${e.wasDark ? 'd' : 'l'}`
        : `${e.type}`
    const existing = map.get(key)
    if (existing) {
      existing.count++
    } else {
      map.set(key, { key, color: e.color, type: e.type, wasDark: e.wasDark, count: 1 })
    }
  }
  return [...map.values()]
})

// 未知暗子（仅 loss 区）渲染为倒扣的棋子背面，不显身份。
function showAsBack(item: TrayItem) {
  return props.variant === 'loss' && item.type == null
}
// 暗子被吃的「已揭示」棋子整体调暗（trophy 区，以及终局揭晓后的 loss 区）。
function dim(item: TrayItem) {
  return item.wasDark && !showAsBack(item)
}
function charOf(item: TrayItem) {
  return item.type ? PIECE_CHAR[item.color][item.type] : ''
}
</script>

<template>
  <div class="cap-tray" :class="`cap-${variant}`">
    <div
      v-for="item in items"
      :key="item.key"
      class="cap-slot"
    >
      <div
        class="cap-disc"
        :class="{ back: showAsBack(item), dim: dim(item), red: item.color === 'red', black: item.color === 'black' }"
      >
        <span v-if="!showAsBack(item)" class="cap-char">{{ charOf(item) }}</span>
      </div>
      <span v-if="item.count > 1" class="cap-badge">{{ item.count }}</span>
    </div>
  </div>
</template>

<style scoped>
.cap-tray {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 62px;
  align-items: center;
  width: 100%;
}
/* 左下战利品：从左向右排列 */
.cap-trophy { justify-content: flex-start; }
/* 右上损失：靠右、从右向左排列 */
.cap-loss { flex-direction: row-reverse; justify-content: flex-start; }

.cap-slot {
  position: relative;
  width: 60px;
  height: 60px;
  flex: 0 0 auto;
}

.cap-disc {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 明子/已揭示：浅枫木 */
  background: radial-gradient(circle at 50% 50%, #ecca94 0%, #e3bf85 80%, #c69e62 100%);
  border: 1px solid #7a4a1f;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.35);
}
/* 暗子被吃（倒扣背面）：深棕木 + 180° 倒置，不显身份 */
.cap-disc.back {
  background: radial-gradient(circle at 50% 50%, #a87a4a 0%, #8e6539 80%, #5d3a1e 100%);
  border-color: #3d2410;
  transform: rotate(180deg);
}
/* 暗子被吃后已知身份：整体调暗，与正常明子被吃区分 */
.cap-disc.dim {
  filter: brightness(0.78) saturate(0.9);
  opacity: 0.92;
}

.cap-char {
  font-family: STKaiti, KaiTi, '楷体', serif;
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}
.cap-disc.red .cap-char { color: #b91c1c; }
.cap-disc.black .cap-char { color: #1c1917; }

/* 右上角数字角标 */
.cap-badge {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 20px;
  height: 20px;
  padding: 0 5px;
  border-radius: 10px;
  background: #b91c1c;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  line-height: 20px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.45);
}
</style>
