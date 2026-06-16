// 揭棋棋子类型与初始局面定义

export type Color = 'red' | 'black'

export type PieceType =
  | 'king'    // 帅/将
  | 'advisor' // 仕/士
  | 'bishop'  // 相/象
  | 'rook'    // 俥/車
  | 'knight'  // 傌/馬
  | 'cannon'  // 炮/砲
  | 'pawn'    // 兵/卒

export interface Piece {
  type: PieceType
  color: Color
  revealed: boolean // false = 暗子（背面）
  row: number       // 0~9, 0=红方底线
  col: number       // 0~8, 0=列 a
}

// 被吃棋子记录（揭棋信息差）：
//   type = null 表示「未知暗子」——被吃方看不到被吃暗子的真实身份；
//   wasDark = true 表示被吃前是暗子（用于左下战利品区的"变暗"显示）。
export interface CapturedEntry {
  color: Color
  type: PieceType | null
  wasDark: boolean
}

// 红方明子文字 / 黑方明子文字
export const PIECE_CHAR: Record<Color, Record<PieceType, string>> = {
  red:   { king: '帅', advisor: '仕', bishop: '相', rook: '俥', knight: '傌', cannon: '炮', pawn: '兵' },
  black: { king: '将', advisor: '士', bishop: '象', rook: '車', knight: '馬', cannon: '砲', pawn: '卒' },
}

// 揭棋初始局面：帅/将露明，其余 15+15 子全暗，按原始位置摆放
// （暗子的 type 是它的"潜在身份"——前端只用于走子规则估算；服务端有权威 type）
export function initialJieqiBoard(): Piece[] {
  const pieces: Piece[] = []
  const backRow: PieceType[] = ['rook','knight','bishop','advisor','king','advisor','bishop','knight','rook']

  // 红方底线 row=0
  backRow.forEach((type, col) => {
    pieces.push({
      type, color: 'red', row: 0, col,
      revealed: type === 'king', // 只有帅是露明的
    })
  })
  // 红方炮 row=2，col=1 和 7
  pieces.push({ type: 'cannon', color: 'red', row: 2, col: 1, revealed: false })
  pieces.push({ type: 'cannon', color: 'red', row: 2, col: 7, revealed: false })
  // 红方兵 row=3，col=0,2,4,6,8
  ;[0,2,4,6,8].forEach(col => {
    pieces.push({ type: 'pawn', color: 'red', row: 3, col, revealed: false })
  })

  // 黑方底线 row=9
  backRow.forEach((type, col) => {
    pieces.push({
      type, color: 'black', row: 9, col,
      revealed: type === 'king',
    })
  })
  // 黑方炮 row=7
  pieces.push({ type: 'cannon', color: 'black', row: 7, col: 1, revealed: false })
  pieces.push({ type: 'cannon', color: 'black', row: 7, col: 7, revealed: false })
  // 黑方卒 row=6
  ;[0,2,4,6,8].forEach(col => {
    pieces.push({ type: 'pawn', color: 'black', row: 6, col, revealed: false })
  })

  return pieces
}
