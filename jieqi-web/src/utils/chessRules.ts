// 揭棋走子规则引擎（前端预校验版）
//
// 坐标系：row=0 红方底线，row=9 黑方底线；col=0..8（a-i）
// 九宫：红方 row 0-2 col 3-5；黑方 row 7-9 col 3-5
// 河界：红方在 row 0-4，黑方在 row 5-9（红"过河"= row > 4，黑"过河"= row < 5）
//
// 注意：暗子（!revealed）的真实走子规则只有服务端权威知道，
// 前端不预测暗子的可走目标。

import type { Piece, Color, PieceType } from '../types/chess'

export interface Coord { row: number; col: number }

/** 在棋盘上找指定位置的棋子（O(n)，棋子数有限） */
export function pieceAt(board: Piece[], row: number, col: number): Piece | undefined {
  return board.find(p => p.row === row && p.col === col)
}

/** 找指定颜色的将/帅位置 */
export function findKing(board: Piece[], color: Color): Piece | undefined {
  return board.find(p => p.type === 'king' && p.color === color && p.revealed)
}

/** 是否在九宫内 */
function inPalace(color: Color, row: number, col: number): boolean {
  if (col < 3 || col > 5) return false
  return color === 'red' ? (row >= 0 && row <= 2) : (row >= 7 && row <= 9)
}

/** 颜色是否"过河"（到对方半区） */
function crossedRiver(color: Color, row: number): boolean {
  return color === 'red' ? row > 4 : row < 5
}

/** 单步规则校验（不含飞将、不含轮次）
 *
 * 揭棋规则关键：
 * - 明子按 piece.type（真实身份）走
 * - 暗子按 piece.type（此时 type 字段实际存的是 virtualType——它所在位置原本应有的棋子类型）走
 *   例如 a0 的暗子按"车"走，b2 的暗子按"炮"走，a3 的暗子按"兵"走
 *   服务端 BoardJsonMapper.toInitialBoard 给暗子推送的就是 virtualType。
 * 走出去之后服务端会随 moveResult 翻开真实身份，前端的 type 也随之更新。
 */
function canMoveByType(board: Piece[], piece: Piece, toRow: number, toCol: number): boolean {
  // 越界
  if (toRow < 0 || toRow > 9 || toCol < 0 || toCol > 8) return false
  // 起点终点相同
  if (toRow === piece.row && toCol === piece.col) return false
  // 吃己方
  const target = pieceAt(board, toRow, toCol)
  if (target && target.color === piece.color) return false

  const dr = toRow - piece.row
  const dc = toCol - piece.col

  switch (piece.type) {
    case 'king':
      // 九宫内一步直走
      if (!inPalace(piece.color, toRow, toCol)) return false
      return (Math.abs(dr) + Math.abs(dc)) === 1

    case 'advisor':
      // 暗士限九宫；明士/仕可离宫、可过河
      if (!piece.revealed && !inPalace(piece.color, toRow, toCol)) return false
      return Math.abs(dr) === 1 && Math.abs(dc) === 1

    case 'bishop': {
      // 田字格，暗象不过河；明象/相可过河，象眼规则不变
      if (Math.abs(dr) !== 2 || Math.abs(dc) !== 2) return false
      if (!piece.revealed && crossedRiver(piece.color, toRow)) return false
      const eyeR = piece.row + dr / 2
      const eyeC = piece.col + dc / 2
      return !pieceAt(board, eyeR, eyeC)
    }

    case 'knight': {
      // 日字 + 蹩马腿
      const ar = Math.abs(dr), ac = Math.abs(dc)
      if (!((ar === 2 && ac === 1) || (ar === 1 && ac === 2))) return false
      // 马腿位置
      const legR = ar === 2 ? piece.row + dr / 2 : piece.row
      const legC = ac === 2 ? piece.col + dc / 2 : piece.col
      return !pieceAt(board, legR, legC)
    }

    case 'rook': {
      // 横竖直走，中间无子
      if (dr !== 0 && dc !== 0) return false
      const stepR = Math.sign(dr), stepC = Math.sign(dc)
      let r = piece.row + stepR, c = piece.col + stepC
      while (r !== toRow || c !== toCol) {
        if (pieceAt(board, r, c)) return false
        r += stepR; c += stepC
      }
      return true
    }

    case 'cannon': {
      // 横竖
      if (dr !== 0 && dc !== 0) return false
      const stepR = Math.sign(dr), stepC = Math.sign(dc)
      let count = 0
      let r = piece.row + stepR, c = piece.col + stepC
      while (r !== toRow || c !== toCol) {
        if (pieceAt(board, r, c)) count++
        r += stepR; c += stepC
      }
      // 走空：中间无子且目标空
      // 吃子：中间恰好一个子（炮架）且目标有敌子
      return target ? count === 1 : count === 0
    }

    case 'pawn': {
      // 一步内的移动
      if (Math.abs(dr) + Math.abs(dc) !== 1) return false
      const forward = piece.color === 'red' ? 1 : -1
      if (!crossedRiver(piece.color, piece.row)) {
        // 未过河：只能前进
        return dr === forward && dc === 0
      } else {
        // 已过河：前进或左右，不能后退
        return (dr === forward && dc === 0) || (dr === 0 && Math.abs(dc) === 1)
      }
    }
  }
  return false
}

/**
 * 判断 color 方的将是否处于"将无法存活"的状态：
 *   1) 被对方任一明子按规则攻击（普通将军）
 *   2) 双方将同列且中间无任何子（飞将 / 将帅照面）
 *
 * 飞将本质上也是"将被对方将吃"的一种特殊形态，
 * 故统一并入 isInCheck，使后续 wouldLeaveKingInCheck
 * 不需要单独判飞将。
 *
 * 暗子不计入攻击者（其规则按 virtualType 仍可能攻击，但前端保守起见
 * 只让明子产生"将军"威胁，否则界面会过度报"将军"）。
 */
export function isInCheck(board: Piece[], color: Color): boolean {
  const king = findKing(board, color)
  if (!king) return false
  const oppColor: Color = color === 'red' ? 'black' : 'red'

  // 1) 明子攻击
  for (const attacker of board) {
    if (!attacker.revealed) continue
    if (attacker.color !== oppColor) continue
    if (canMoveByType(board, attacker, king.row, king.col)) return true
  }

  // 2) 飞将（对方将与我方将同列、中间无子）
  const oppKing = findKing(board, oppColor)
  if (oppKing && oppKing.col === king.col) {
    const minRow = Math.min(king.row, oppKing.row)
    const maxRow = Math.max(king.row, oppKing.row)
    let blocked = false
    for (let r = minRow + 1; r < maxRow; r++) {
      if (pieceAt(board, r, king.col)) { blocked = true; break }
    }
    if (!blocked) return true
  }

  return false
}

/**
 * 模拟走子后，走子方的将是否处于"被将军"状态（含飞将）。
 * 这条规则同时覆盖三种"非法"：
 *   - 送将：走完后自己反被将
 *   - 没解将：被将军时走了一步无关的子，走完后仍被将
 *   - 飞将：走完后双方将照面，等同于将被对方将吃
 */
function wouldLeaveKingInCheck(
  board: Piece[], piece: Piece, toRow: number, toCol: number,
): boolean {
  const sim: Piece[] = board
    .filter(p => !(p.row === toRow && p.col === toCol))    // 吃掉目标
    .map(p => p === piece ? { ...p, row: toRow, col: toCol } : p)
  return isInCheck(sim, piece.color)
}

export function getMoveErrorMessage(board: Piece[], piece: Piece, toRow: number, toCol: number): string | null {
  if (canMoveByType(board, piece, toRow, toCol)) {
    return wouldLeaveKingInCheck(board, piece, toRow, toCol) ? '不能送将' : null
  }
  const coord = `${'abcdefghi'[toCol] ?? '?'}${toRow}`
  return `${piece.revealed ? '明' : '暗'}子不能走到 ${coord}（不符合${piece.type} 走子规则）`
}

/**
 * 列出某棋子所有合法目标位置。
 *
 * 三层过滤：
 *   1) canMoveByType —— 该棋子按 type 的走子规则
 *   2) wouldLeaveKingInCheck —— 不能送将 / 不解将 / 飞将
 *
 * 揭棋规则：暗子按所在位置的初始类型（virtualType）走，不能原地翻。
 * 前端 piece.type 对暗子来说就是 virtualType。
 */
export function getValidMoves(board: Piece[], piece: Piece): Coord[] {
  const moves: Coord[] = []
  for (let r = 0; r < 10; r++) {
    for (let c = 0; c < 9; c++) {
      if (!canMoveByType(board, piece, r, c)) continue
      if (wouldLeaveKingInCheck(board, piece, r, c)) continue
      moves.push({ row: r, col: c })
    }
  }
  return moves
}

/**
 * 该方是否还有任何合法走法。
 */
export function hasAnyLegalMove(board: Piece[], color: Color): boolean {
  for (const piece of board) {
    if (piece.color !== color) continue
    if (getValidMoves(board, piece).length > 0) return true
  }
  return false
}

/** 被将死 = 被将军 + 无任何合法走法 */
export function isCheckmate(board: Piece[], color: Color): boolean {
  if (!isInCheck(board, color)) return false
  return !hasAnyLegalMove(board, color)
}

/** 困毙 = 没被将军 + 无任何合法走法（按象棋传统也判负） */
export function isStalemate(board: Piece[], color: Color): boolean {
  if (isInCheck(board, color)) return false
  return !hasAnyLegalMove(board, color)
}

/** 棋子类型显示文字（中文） */
export const TYPE_CN: Record<PieceType, string> = {
  king: '将', advisor: '士', bishop: '象',
  rook: '车', knight: '马', cannon: '炮', pawn: '兵',
}
