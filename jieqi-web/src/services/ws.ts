// WebSocket 服务：封装与揭棋服务端的连接、消息收发
// 协议参考：docs/_teacher_interface.md (老师公共接口 JSON)

export type MessageType =
  | 'Login' | 'loginResult'
  | 'startMatch' | 'matchSuccess'
  | 'requestFirstHand' | 'roomInfo'
  | 'Ready' | 'gameStart'
  | 'move' | 'moveResult' | 'flipResult'
  | 'Resign' | 'gameOver' | 'timeout'
  | 'drawOffer' | 'drawAccept' | 'drawDecline'
  | 'drawOffered' | 'drawDeclined'
  | 'ping' | 'pong'
  | 'error'

export interface WsMessage {
  messageType: MessageType | string
  [key: string]: any
}

type Listener = (msg: WsMessage) => void

class WsService {
  private socket: WebSocket | null = null
  private listeners = new Set<Listener>()
  private statusListeners = new Set<(status: 'connecting' | 'open' | 'closed' | 'error') => void>()

  connect(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      this.notifyStatus('connecting')
      try {
        this.socket = new WebSocket(url)
      } catch (e) {
        this.notifyStatus('error')
        reject(e); return
      }
      this.socket.onopen = () => { this.notifyStatus('open'); resolve() }
      this.socket.onerror = () => { this.notifyStatus('error'); reject(new Error('WebSocket 连接失败')) }
      this.socket.onclose = () => this.notifyStatus('closed')
      this.socket.onmessage = (ev) => {
        try {
          const msg = JSON.parse(ev.data) as WsMessage
          this.listeners.forEach(l => l(msg))
        } catch {
          // 非 JSON 消息忽略
        }
      }
    })
  }

  send(msg: WsMessage) {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      console.warn('[WS] socket not open, drop:', msg)
      return
    }
    this.socket.send(JSON.stringify(msg))
  }

  close() {
    this.socket?.close()
    this.socket = null
  }

  onMessage(fn: Listener): () => void {
    this.listeners.add(fn)
    return () => this.listeners.delete(fn)
  }

  onStatus(fn: (s: 'connecting' | 'open' | 'closed' | 'error') => void): () => void {
    this.statusListeners.add(fn)
    return () => this.statusListeners.delete(fn)
  }

  private notifyStatus(s: 'connecting' | 'open' | 'closed' | 'error') {
    this.statusListeners.forEach(fn => fn(s))
  }
}

export const ws = new WsService()
