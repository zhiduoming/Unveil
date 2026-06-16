<script setup lang="ts">
import { computed } from 'vue'
import type { Piece } from '../types/chess'
import ChessPiece from './ChessPiece.vue'

const props = withDefaults(defineProps<{
  pieces: Piece[]
  isRedView?: boolean
  selectedCoord?: string
  hintCoords?: string[]
  lastMove?: { from: string; to: string } | null
}>(), {
  isRedView: true,
  selectedCoord: '',
  hintCoords: () => [],
  lastMove: null,
})

defineEmits<{
  (e: 'cell-click', row: number, col: number): void
}>()

// 10 行 × 9 列所有格点（row, col）
const allCells = computed(() => {
  const cells: { row: number; col: number }[] = []
  for (let r = 0; r < 10; r++) {
    for (let c = 0; c < 9; c++) cells.push({ row: r, col: c })
  }
  return cells
})

const colLabels = computed(() => {
  // 顶部/底部都显示 1-9，但根据视角左右翻转
  return props.isRedView ? [1,2,3,4,5,6,7,8,9] : [9,8,7,6,5,4,3,2,1]
})

// 视角换算：把逻辑坐标 (row, col) 转成屏幕显示坐标 (x%, y%)
function toScreenPos(row: number, col: number) {
  const c = props.isRedView ? col : 8 - col
  const r = props.isRedView ? 9 - row : row
  return {
    left: `${(c / 8) * 100}%`,
    top: `${(r / 9) * 100}%`,
  }
}

function isSelected(row: number, col: number) {
  return props.selectedCoord === `${'abcdefghi'[col]}${row}`
}
function isHint(row: number, col: number) {
  return props.hintCoords.includes(`${'abcdefghi'[col]}${row}`)
}
function coordToCell(coord: string) {
  const col = 'abcdefghi'.indexOf(coord[0])
  const row = Number(coord.slice(1))
  if (col < 0 || Number.isNaN(row)) return null
  return { row, col }
}
const lastMoveMarks = computed(() => {
  if (!props.lastMove) return []
  const from = coordToCell(props.lastMove.from)
  return from ? [from] : []
})
function isLastMoveTarget(row: number, col: number) {
  return props.lastMove?.to === `${'abcdefghi'[col]}${row}`
}

// 用于找某格上是否有棋子
const piecesMap = computed(() => {
  const m: Record<string, Piece> = {}
  for (const p of props.pieces) m[`${p.row}-${p.col}`] = p
  return m
})
</script>

<template>
  <div class="board-outer">
    <!-- 顶部列号 -->
    <div class="col-labels">
      <span v-for="n in colLabels" :key="`top-${n}`">{{ n }}</span>
    </div>

    <!-- 棋盘主体（木纹背景） -->
    <div class="board">
      <!-- 内部坐标区：四周留 padding 让格点不贴到边框 -->
      <div class="board-inner">
        <!-- 格线 SVG -->
        <svg class="lines" viewBox="0 0 800 900" preserveAspectRatio="none">
          <!-- 10 条水平线 -->
          <line v-for="i in 10" :key="`h-${i}`"
            :x1="0" :x2="800" :y1="(i-1)*100" :y2="(i-1)*100"
            stroke="#5d2f0d" stroke-width="2"/>

          <!-- 9 条垂直线（中间 7 条在楚河汉界处断开） -->
          <line x1="0" x2="0" y1="0" y2="900" stroke="#5d2f0d" stroke-width="2"/>
          <line x1="800" x2="800" y1="0" y2="900" stroke="#5d2f0d" stroke-width="2"/>
          <line v-for="i in 7" :key="`v-top-${i}`"
            :x1="i*100" :x2="i*100" :y1="0" :y2="400"
            stroke="#5d2f0d" stroke-width="2"/>
          <line v-for="i in 7" :key="`v-bot-${i}`"
            :x1="i*100" :x2="i*100" :y1="500" :y2="900"
            stroke="#5d2f0d" stroke-width="2"/>

          <!-- 九宫斜线 -->
          <line x1="300" y1="0" x2="500" y2="200" stroke="#5d2f0d" stroke-width="2"/>
          <line x1="500" y1="0" x2="300" y2="200" stroke="#5d2f0d" stroke-width="2"/>
          <line x1="300" y1="700" x2="500" y2="900" stroke="#5d2f0d" stroke-width="2"/>
          <line x1="500" y1="700" x2="300" y2="900" stroke="#5d2f0d" stroke-width="2"/>

          <!-- 楚河汉界（左右居中对称） -->
          <text x="200" y="478" font-size="58" fill="#5d2f0d"
            text-anchor="middle"
            font-family="STKaiti,KaiTi,serif" font-weight="bold" letter-spacing="30">楚 河</text>
          <text x="600" y="478" font-size="58" fill="#5d2f0d"
            text-anchor="middle"
            font-family="STKaiti,KaiTi,serif" font-weight="bold" letter-spacing="30">漢 界</text>
          <!-- 中间 BUPT 标识 -->
          <text x="400" y="472" font-size="36" fill="#5d2f0d"
            text-anchor="middle"
            font-family="Georgia,'Times New Roman',serif" font-style="italic"
            font-weight="bold" letter-spacing="3" opacity="0.85">BUPT</text>
        </svg>

        <!-- 90 个交叉点：空格也可点击（用于走子目标） -->
        <button v-for="cell in allCells" :key="`hit-${cell.row}-${cell.col}`"
          class="hit-area"
          :style="toScreenPos(cell.row, cell.col)"
          @click="$emit('cell-click', cell.row, cell.col)"
        >
          <!-- 走子提示点（空格时） -->
          <span v-if="isHint(cell.row, cell.col) && !piecesMap[`${cell.row}-${cell.col}`]"
            class="hint-dot"></span>
        </button>

        <!-- 棋子层：每个棋子按 (row, col) 绝对定位 -->
        <div v-for="p in pieces" :key="`p-${p.row}-${p.col}`"
          class="piece-pos"
          :style="toScreenPos(p.row, p.col)"
        >
          <ChessPiece
            :piece="p"
            :selected="isSelected(p.row, p.col)"
            :hint="isHint(p.row, p.col)"
            :last-move-target="isLastMoveTarget(p.row, p.col)"
            @click="$emit('cell-click', p.row, p.col)"
          />
        </div>

        <!-- 上一步起点：红色标记，终点由棋子光晕强调 -->
        <div
          v-for="mark in lastMoveMarks"
          :key="`last-from-${mark.row}-${mark.col}`"
          class="last-move-mark"
          :style="toScreenPos(mark.row, mark.col)"
        ></div>
      </div>
    </div>

    <!-- 底部列号 -->
    <div class="col-labels">
      <span v-for="n in colLabels" :key="`bot-${n}`">{{ n }}</span>
    </div>
  </div>
</template>

<style scoped>
.board-outer {
  width: 100%;
  max-width: 880px;
}

.col-labels {
  display: grid;
  grid-template-columns: repeat(9, 1fr);
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  color: #4a2410;
  padding: 8px 7%;
}

.board {
  position: relative;
  background-image: url('/wood-texture.svg');
  background-size: cover;
  background-position: center;
  border: 8px solid #6b3a1a;
  border-radius: 10px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.4), inset 0 0 30px rgba(60, 30, 10, 0.15);
  /* 棋盘整体宽高比：8:9（格点矩阵+边距） */
  aspect-ratio: 8 / 9;
  width: 100%;
}

/* 内部格点区：四周 padding 7%，让格线和棋子不贴边 */
.board-inner {
  position: absolute;
  top: 7%;
  left: 7%;
  right: 7%;
  bottom: 7%;
}

.lines {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

/* 棋子定位：absolute + transform 让坐标 (left, top) 是棋子中心 */
.piece-pos {
  position: absolute;
  /* 棋子大小：占一格宽度的约 80% */
  width: 11.5%;
  aspect-ratio: 1;
  transform: translate(-50%, -50%);
  z-index: 10;
}

/* 点击热区：覆盖每个格点附近，方便点空格走子 */
.hit-area {
  position: absolute;
  width: 11.5%;
  aspect-ratio: 1;
  transform: translate(-50%, -50%);
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
  z-index: 5;
}

.hint-dot {
  position: absolute;
  top: 50%; left: 50%;
  width: 30%; height: 30%;
  background: rgba(245, 158, 11, 0.75);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
}

.last-move-mark {
  position: absolute;
  width: 4.4%;
  aspect-ratio: 1;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background: rgba(220, 38, 38, 0.88);
  border: 2px solid rgba(255, 235, 190, 0.95);
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.22), 0 2px 8px rgba(80, 20, 10, 0.35);
  pointer-events: none;
  z-index: 20;
  opacity: 0.72;
}
</style>
