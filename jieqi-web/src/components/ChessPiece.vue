<script setup lang="ts">
import { computed } from 'vue'
import type { Piece } from '../types/chess'
import { PIECE_CHAR } from '../types/chess'

const props = defineProps<{
  piece: Piece
  selected?: boolean
  hint?: boolean
}>()

defineEmits<{ (e: 'click'): void }>()

const isRed = computed(() => props.piece.color === 'red')
const isDark = computed(() => !props.piece.revealed)
const charText = computed(() =>
  isDark.value ? '' : PIECE_CHAR[props.piece.color][props.piece.type],
)

// 每个棋子实例的唯一 id（避免 SVG defs 冲突）
const uid = computed(() => `${props.piece.row}-${props.piece.col}`)

// 24 个回字纹单元绕圆周分布，每个间隔 15°
const meanderAngles = Array.from({ length: 24 }, (_, i) => i * 15)
</script>

<template>
  <button
    @click.stop="$emit('click')"
    class="piece-btn"
    :class="{ 'piece-selected': selected }"
  >
    <span v-if="hint" class="capture-hint" aria-hidden="true"></span>
    <svg viewBox="0 0 100 100" class="piece-svg" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <!-- 浅枫木：扁平分布，中央与边缘色差很小，只在最外圈轻微暗化 -->
        <radialGradient :id="`wood-${uid}`" cx="0.5" cy="0.5" r="0.55">
          <stop offset="0%" stop-color="#ecca94"/>
          <stop offset="80%" stop-color="#e3bf85"/>
          <stop offset="100%" stop-color="#c69e62"/>
        </radialGradient>

        <!-- 暗子：同样扁平分布，深一档 -->
        <radialGradient :id="`wood-dark-${uid}`" cx="0.5" cy="0.5" r="0.55">
          <stop offset="0%" stop-color="#a87a4a"/>
          <stop offset="80%" stop-color="#8e6539"/>
          <stop offset="100%" stop-color="#5d3a1e"/>
        </radialGradient>

        <!-- 顶部微弱柔光（极淡，仅给扁平表面一点点光泽，不再让人误以为是球面） -->
        <radialGradient :id="`gloss-${uid}`" cx="0.5" cy="0.18" r="0.55">
          <stop offset="0%" stop-color="#ffffff" stop-opacity="0.20"/>
          <stop offset="60%" stop-color="#ffffff" stop-opacity="0"/>
        </radialGradient>

        <!-- 凹刻效果（深色偏移阴影） -->
        <filter :id="`engrave-${uid}`" x="-15%" y="-15%" width="130%" height="130%">
          <feGaussianBlur stdDeviation="0.5" result="blur"/>
          <feOffset in="blur" dx="0.7" dy="0.9" result="offsetBlur"/>
          <feFlood flood-color="#000" flood-opacity="0.55"/>
          <feComposite in2="offsetBlur" operator="in" result="shadow"/>
          <feMerge>
            <feMergeNode in="shadow"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      </defs>

      <!-- 落地阴影 -->
      <ellipse cx="50" cy="95" rx="38" ry="2.5" fill="#000" opacity="0.3"/>

      <!-- 主体圆盘（扁平木色） -->
      <circle
        cx="50" cy="50" r="47"
        :fill="isDark ? `url(#wood-dark-${uid})` : `url(#wood-${uid})`"
        :stroke="isDark ? '#3d2410' : '#7a4a1f'"
        stroke-width="1"
      />

      <!-- 顶部微弱柔光：让木材表面有一点点光泽，但不再像球面凸起 -->
      <circle cx="50" cy="50" r="47" :fill="`url(#gloss-${uid})`"/>

      <!-- 外圈装饰细圆 -->
      <circle
        cx="50" cy="50" r="43"
        fill="none"
        :stroke="isDark ? '#3d2410' : '#7a4a1f'"
        stroke-width="0.6"
        opacity="0.55"
      />

      <!-- 真正的回字纹：24 个 Γ 形单元绕圆排列 -->
      <g :stroke="isDark ? '#3d2410' : '#7a4a1f'" stroke-width="0.9"
         fill="none" stroke-linecap="round" stroke-linejoin="round" opacity="0.85">
        <g v-for="angle in meanderAngles" :key="angle"
           :transform="`rotate(${angle} 50 50) translate(50 11)`">
          <!-- Γ 形回字纹单元，居中在 (0, 0) -->
          <!-- 大 L: 外勾  -->
          <path d="M -3.2 1.4 L -3.2 -1.4 L 3.2 -1.4 L 3.2 1.4"/>
          <!-- 内嵌小一圈：模拟双层 -->
          <path d="M -1.6 1.4 L -1.6 0 L 1.6 0 L 1.6 1.4"/>
        </g>
      </g>

      <!-- 内圈装饰细圆 -->
      <circle
        cx="50" cy="50" r="36"
        fill="none"
        :stroke="isDark ? '#3d2410' : '#7a4a1f'"
        stroke-width="0.6"
        opacity="0.5"
      />

      <!-- 明子：中央字 -->
      <text
        v-if="!isDark"
        x="50" y="64"
        font-family="STKaiti, KaiTi, '楷体', serif"
        font-size="42"
        font-weight="900"
        text-anchor="middle"
        :fill="isRed ? '#b91c1c' : '#1c1917'"
        :filter="`url(#engrave-${uid})`"
      >{{ charText }}</text>

      <!-- 暗子：中央雕刻的同心圆，对应参考图右边的空背面 -->
      <g v-else>
        <circle cx="50" cy="50" r="12"
          fill="none" stroke="#3d2410" stroke-width="0.8" opacity="0.5"/>
      </g>
    </svg>
  </button>
</template>

<style scoped>
.piece-btn {
  width: 100%;
  height: 100%;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  position: relative;
  transition: transform 0.12s ease;
}

.capture-hint {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 32%;
  height: 32%;
  border-radius: 50%;
  background: rgba(245, 158, 11, 0.78);
  transform: translate(-50%, -50%);
  z-index: 2;
  pointer-events: none;
  box-shadow: 0 0 0 3px rgba(245, 158, 11, 0.22);
}

.piece-btn:hover:not(.piece-selected) {
  transform: translateY(-2px);
}

.piece-selected {
  transform: translateY(-4px) scale(1.06);
  filter: drop-shadow(0 0 6px rgba(245, 158, 11, 0.9));
}

.piece-svg {
  width: 100%;
  height: 100%;
  display: block;
  filter: drop-shadow(0 2px 3px rgba(0, 0, 0, 0.4));
}
</style>
