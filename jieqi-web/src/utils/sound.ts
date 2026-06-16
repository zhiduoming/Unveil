// 新消息提示音：用 Web Audio 实时合成「叮咚」，零音频资源、无需上传。

let ctx: AudioContext | null = null

function audioCtx(): AudioContext | null {
  try {
    if (!ctx) {
      const Ctor = window.AudioContext || (window as any).webkitAudioContext
      if (!Ctor) return null
      ctx = new Ctor()
    }
    if (ctx.state === 'suspended') void ctx.resume()
    return ctx
  } catch {
    return null
  }
}

/** 播放一声轻快的「叮咚」提示音（新消息到达时）。 */
export function playMessageBeep() {
  const ac = audioCtx()
  if (!ac) return
  try {
    const t = ac.currentTime
    const osc = ac.createOscillator()
    const gain = ac.createGain()
    osc.connect(gain)
    gain.connect(ac.destination)
    osc.type = 'sine'
    // 两段频率构成「叮—咚」
    osc.frequency.setValueAtTime(880, t)
    osc.frequency.setValueAtTime(1174.7, t + 0.09)
    gain.gain.setValueAtTime(0.0001, t)
    gain.gain.exponentialRampToValueAtTime(0.25, t + 0.02)
    gain.gain.exponentialRampToValueAtTime(0.0001, t + 0.3)
    osc.start(t)
    osc.stop(t + 0.33)
  } catch {
    /* 音频不可用时静默忽略，不影响聊天功能 */
  }
}
