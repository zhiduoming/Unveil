import { defineStore } from 'pinia'
import { ws, type WsMessage } from '../services/ws'

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
    lastError: '' as string,
    gameOver: null as { winner: string; reason: string; winnerId?: string } | null,
    connectionStatus: 'closed' as 'connecting' | 'open' | 'closed' | 'error',
  }),
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
    reset() {
      this.matching = false
      this.ready = false
      this.opponentReady = false
      this.room = null
      this.gameStart = null
      this.gameOver = null
    },
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
          break
        case 'gameOver':
          this.gameOver = { winner: msg.winner, reason: msg.reason, winnerId: msg.winnerId }
          break
        case 'error':
          this.lastError = `[${msg.code}] ${msg.message || '未知错误'}`
          this.matching = false
          break
      }
    },
  },
})
