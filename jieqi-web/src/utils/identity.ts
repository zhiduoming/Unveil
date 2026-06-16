// 随机昵称与内置 emoji 头像（零资源，无需上传服务）。

// 内置头像：一组有趣的 emoji，随机分配 / 点击循环切换。
export const AVATARS = [
  '🐯', '🐼', '🦊', '🐲', '👾', '🤖', '🐙', '🦄',
  '🐶', '🐱', '🦁', '🐵', '🐸', '🦅', '🐺', '🐧',
] as const

// 象棋主题随机昵称词库：形容词 + 名词组合。
const NICKNAME_ADJ = [
  '摸鱼的', '无敌', '沉默的', '闪电', '佛系', '暴走', '隐形', '传说级',
  '快乐的', '沙雕', '头铁', '人间清醒', '和气', '骨灰级', '迷糊', '稳健',
]
const NICKNAME_NOUN = [
  '过河卒', '当头炮', '马后炮', '卧槽马', '铁门栓', '棋圣', '老司机', '小炮兵',
  '象棋魂', '车神', '士角炮', '兵线杀手', '棋坛新秀', '将军', '中炮王', '边马',
]

function pick<T>(arr: readonly T[]): T {
  return arr[Math.floor(Math.random() * arr.length)]
}

/** 随机一个有趣昵称，如「摸鱼的过河卒」。 */
export function randomNickname(): string {
  return pick(NICKNAME_ADJ) + pick(NICKNAME_NOUN)
}

/** 随机一个 emoji 头像。 */
export function randomAvatar(): string {
  return pick(AVATARS)
}

/** 循环切换到下一个头像（点击头像时用）。 */
export function nextAvatar(current: string): string {
  const i = AVATARS.indexOf(current as (typeof AVATARS)[number])
  return AVATARS[(i + 1) % AVATARS.length]
}

/** 生成稳定唯一的账号 userId（与显示昵称解耦，避免随机重名导致账号冲突）。 */
export function genUserId(): string {
  return 'u_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}
