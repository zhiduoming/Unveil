import { defineStore } from 'pinia'
import { ws, type WsMessage } from '../services/ws'
import { initialJieqiBoard, type Piece, type PieceType } from '../types/chess'

export type Color = 'red' | 'black'

export interface RoomInfo {
  roomId: string
  opponentId: string
  opponentNickname: string
}

export interface GameStartInfo {
  redPlayerId: string
  blackPlayerId: string
  yourColor: Color
  firstHand: boolean
}

const COLS = ['a','b','c','d','e','f','g','h','i']

// 把老师协议里的英文 type 转成内部 PieceType
function mapType(t: string | undefined): PieceType | undefined {
  if (!t) return undefined
  const m: Record<string, PieceType> = {
    king: 'king', advisor: 'advisor', guard: 'advisor', bishop: 'bishop',
    rook: 'rook', knight: 'knight', cannon: 'cannon', pawn: 'pawn',
  }
  return m[t.toLowerCase()]
}

function colToIdx(x: string | number): number {
  if (typeof x === 'number') return x
  return COLS.indexOf(String(x).toLowerCase())
}

function rowToProtocolY(row: number): number {
  return row
}

function protocolYToRow(y: string | number): number {
  return Number(y)
}

function coordText(col: number, row: number): string {
  return `${COLS[col] ?? '?'}${row}`
}

export const useGameStore = defineStore('game', {
  state: () => ({
    serverUrl: 'ws://127.0.0.1:8887',
    userId: '' as string,
    loggedIn: false as boolean,
    matching: false as boolean,
    ready: false as boolean,
    opponentReady: false as boolean,
    room: null as RoomInfo | null,
    gameStart: null as GameStartInfo | null,
    currentTurn: null as Color | null,
    lastError: '' as string,
    gameOver: null as { winner: string; reason: string; winnerId?: string } | null,
    connectionStatus: 'closed' as 'connecting' | 'open' | 'closed' | 'error',

    // 棋盘状态
    board: [] as Piece[],
    selectedCoord: '' as string, // 'a0' 之类，空字符串=未选
    lastMove: null as { from: string; to: string } | null,
  }),

  getters: {
    selectedPiece(state): Piece | null {
      if (!state.selectedCoord) return null
      const col = COLS.indexOf(state.selectedCoord[0])
      const row = Number(state.selectedCoord.slice(1))
      return state.board.find(p => p.row === row && p.col === col) || null
    },
  },

  actions: {
    bind() {
      ws.onStatus((s) => { this.connectionStatus = s })
      ws.onMessage((msg) => this.handleMessage(msg))
    },
    async connect(url: string) {
      this.serverUrl = url
      await ws.connect(url)
    },
    login(userId: string, password: string) {
      this.userId = userId
      ws.send({ messageType: 'Login', userId, password })
    },
    startMatch() {
      this.matching = true
      ws.send({ messageType: 'startMatch' })
    },
    setReady() {
      this.ready = true
      ws.send({ messageType: 'Ready' })
    },
    resign() {
      ws.send({ messageType: 'Resign' })
    },
    ping() {
      ws.send({ messageType: 'ping', timestamp: Date.now() })
    },

    // ── 走子相关 ─────────────────────────────────────────
    selectCell(row: number, col: number) {
      const coord = `${COLS[col]}${row}`
      const piece = this.board.find(p => p.row === row && p.col === col)
      const yourColor = this.gameStart?.yourColor

      // 调试信息（浏览器 Console 可见）
      console.log('[selectCell]', {
        click: coord, row, col,
        pieceColor: piece?.color, pieceType: piece?.type, revealed: piece?.revealed,
        yourColor, currentTurn: this.currentTurn,
        currentSelected: this.selectedCoord,
      })

      // 当前没有选中：只能选自己的子
      if (!this.selectedCoord) {
        if (!piece) {
          this.lastError = `空格 ${coord} 上没有棋子`
          return
        }
        if (this.currentTurn !== yourColor) {
          this.lastError = `未轮到本方（当前轮到 ${this.currentTurn}, 你是 ${yourColor}）`
          return
        }
        if (piece.color !== yourColor) {
          this.lastError = `${coord} 是 ${piece.color} 方棋子，不能选`
          return
        }
        this.selectedCoord = coord
        this.lastError = ''
        return
      }

      // 已经选中一个子：
      // 1. 再次点击同一格 → 取消选中 / 或发原地翻子
      if (this.selectedCoord === coord) {
        const sel = this.selectedPiece
        if (sel && !sel.revealed) {
          // 暗子原地翻
          this.sendMove(sel.col, sel.row, sel.col, sel.row, true)
          this.selectedCoord = ''
          return
        }
        this.selectedCoord = ''
        return
      }

      // 2. 点击其他自己的子 → 改选这个
      if (piece && piece.color === yourColor) {
        this.selectedCoord = coord
        return
      }

      // 3. 点击空格或敌子 → 发走子
      const fromCol = COLS.indexOf(this.selectedCoord[0])
      const fromRow = Number(this.selectedCoord.slice(1))
      const selected = this.selectedPiece
      const shouldFlipAfterMove = selected ? !selected.revealed : false
      this.sendMove(fromCol, fromRow, col, row, shouldFlipAfterMove)
      this.selectedCoord = ''
    },

    sendMove(fromCol: number, fromRow: number, toCol: number, toRow: number, isFlip: boolean) {
      this.lastError = ''
      ws.send({
        messageType: 'move',
        fromX: COLS[fromCol],
        fromY: rowToProtocolY(fromRow),
        toX: COLS[toCol],
        toY: rowToProtocolY(toRow),
        isFlip,
      })
    },

    clearSelection() { this.selectedCoord = '' },

    reset() {
      this.matching = false
      this.ready = false
      this.opponentReady = false
      this.room = null
      this.gameStart = null
      this.gameOver = null
      this.board = []
      this.selectedCoord = ''
      this.lastMove = null
    },

    // ── 消息处理 ─────────────────────────────────────────
    handleMessage(msg: WsMessage) {
      switch (msg.messageType) {
        case 'loginResult':
          this.loggedIn = msg.success === true
          if (!msg.success) this.lastError = msg.message || '登录失败'
          break

        case 'matchSuccess':
          this.matching = false
          this.room = {
            roomId: msg.roomId,
            opponentId: msg.opponentId,
            opponentNickname: msg.opponentNickname || msg.opponentId,
          }
          break

        case 'roomInfo':
          this.opponentReady = msg.opponentReady === true
          break

        case 'gameStart':
          this.gameStart = {
            redPlayerId: msg.redPlayerId,
            blackPlayerId: msg.blackPlayerId,
            yourColor: msg.yourColor as Color,
            firstHand: msg.firstHand === true,
          }
          this.currentTurn = 'red'
          // 初始棋盘：优先用服务端推的，没有就用默认布局
          this.board = msg.initialBoard && Array.isArray(msg.initialBoard) && msg.initialBoard.length > 0
            ? parseInitialBoard(msg.initialBoard)
            : initialJieqiBoard()
          this.selectedCoord = ''
          this.lastMove = null
          break

        case 'moveResult':
          console.log('[moveResult]', msg)
          if (msg.valid === false || msg.success === false) {
            this.lastError = msg.message || '着法无效：请确认是否轮到本方、源格是否有自己的棋子、目标格是否符合揭棋规则。'
            this.selectedCoord = ''
            break
          }
          if (msg.move) this.applyMove(msg.move, msg.flipResult)
          this.lastError = ''
          break

        case 'flipResult': {
          // 兼容单独 flipResult 消息：{ x, y, type } 或 { x, y, piece }
          const col = colToIdx(msg.x ?? msg.toX ?? msg.fromX)
          const y = msg.y ?? msg.toY ?? msg.fromY
          const row = protocolYToRow(y)
          const t = mapType(msg.type ?? msg.piece ?? msg.flipResult)
          const target = this.board.find(p => p.row === row && p.col === col)
          if (target && t) {
            target.type = t
            target.revealed = true
          }
          break
        }

        case 'timeout':
          this.lastError = `超时：${msg.loserId} 判负`
          break

        case 'gameOver':
          this.gameOver = { winner: msg.winner, reason: msg.reason, winnerId: msg.winnerId }
          this.selectedCoord = ''
          break

        case 'error':
          this.lastError = `[${msg.code}] ${msg.message || '未知错误'}`
          this.matching = false
          this.selectedCoord = ''
          break
      }
    },

    applyMove(move: any, flipResult?: string) {
      // 老师 JSON 协议的 y 与前端内部 row 都采用棋盘显示行号：0=红方底线，9=黑方底线。
      const fromCol = colToIdx(move.fromX)
      const fromRow = protocolYToRow(move.fromY)
      const toCol = colToIdx(move.toX)
      const toRow = protocolYToRow(move.toY)
      if (fromCol < 0 || toCol < 0 || Number.isNaN(fromRow) || Number.isNaN(toRow)) {
        this.lastError = `服务端返回的走子坐标无法解析：${JSON.stringify(move)}`
        return
      }

      const piece = this.board.find(p => p.row === fromRow && p.col === fromCol)
      if (!piece) {
        this.lastError = `本地棋盘不同步：${coordText(fromCol, fromRow)} 没有棋子`
        return
      }
      const revealedType = mapType(move.type ?? move.piece ?? flipResult)

      // 原地翻子
      if (fromCol === toCol && fromRow === toRow) {
        piece.revealed = true
        if (revealedType) piece.type = revealedType
        this.lastMove = { from: coordText(fromCol, fromRow), to: coordText(toCol, toRow) }
      } else {
        // 移动：吃掉目标位置上的子（如果有）
        const target = this.board.find(p => p.row === toRow && p.col === toCol)
        if (target) {
          this.board = this.board.filter(p => p !== target)
        }
        piece.row = toRow
        piece.col = toCol
        // 暗子被移动后会被翻开（由 flipResult 进一步处理 type）
        if (revealedType) {
          piece.type = revealedType
          piece.revealed = true
        }
        this.lastMove = { from: coordText(fromCol, fromRow), to: coordText(toCol, toRow) }
      }

      // 切换回合
      this.currentTurn = this.currentTurn === 'red' ? 'black' : 'red'
    },
  },
})

// 解析服务端推送的 initialBoard 数组
//
// 老师 JSON 协议：
//   { x: 'a'-'i', y: 0-9, piece: 'king'/'rook'/..., visible: bool }
// 这里的 y 与前端内部 row 都采用棋盘显示行号：0=红方底线，9=黑方底线。
// 服务端不发 color 时，按显示行号推断：row < 5 → 红方，row >= 5 → 黑方。
function parseInitialBoard(cells: any[]): Piece[] {
  const result: Piece[] = []
  for (const cell of cells) {
    if (!cell || typeof cell !== 'object') continue
    const col = colToIdx(cell.x ?? cell.col)
    const y = Number(cell.y ?? cell.row)
    if (col < 0 || isNaN(y)) continue
    const row = protocolYToRow(y)
    const color: Color = row < 5 ? 'red' : 'black'
    const t = mapType(cell.piece ?? cell.type) || 'pawn'    // 字段名是 piece 不是 type
    const visible = cell.visible === true || cell.revealed === true
    result.push({ type: t, color, row, col, revealed: visible })
  }
  return result.length > 0 ? result : initialJieqiBoard()
}
