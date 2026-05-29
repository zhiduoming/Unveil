#!/usr/bin/env python3
"""Assemble INTERFACE.typ v3.1: v2.0 full content + WS chapter 6 + appendix B TCP."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent
v2 = (ROOT / "_v2_extract.typ").read_text(encoding="utf-8").splitlines()
cur = (ROOT / "INTERFACE.typ").read_text(encoding="utf-8").splitlines()

def slice_lines(lines, start, end):
    """1-based inclusive start/end."""
    return "\n".join(lines[start - 1 : end]) + "\n"

def find_line(lines, prefix, start=0):
    for i in range(start, len(lines)):
        if lines[i].startswith(prefix):
            return i + 1
    raise ValueError(f"not found: {prefix!r}")

# --- preamble (cover + toc) from current ---
preamble_end = find_line(cur, "= 基础约定") - 1
preamble = slice_lines(cur, 1, preamble_end)

# --- ch1-5 base from v2 ---
ch1_start = find_line(v2, "= 基础约定")
ch5_end = find_line(v2, "// 第三章：网络通信协议") - 1
ch1_5 = slice_lines(v2, ch1_start, ch5_end)

# Update §1.2 技术约定
tech_old = """  [传输协议], [TCP Socket], [],
  [默认端口], [8888], [可配置，启动时打印实际端口],
  [字符编码], [*UTF-8*], [所有字符串均为 UTF-8 编码],
  [行尾], [LF（`\\n`，0x0A）], [每条消息以单个换行符结尾],"""

tech_new = """  [传输协议], [*WebSocket* + *JSON*], [课程公共接口；默认端口 *8887*；见第 6 章],
  [TCP 扩展], [文本帧 `msgType|len|payload\\n`], [本组保留；默认端口 *8888*；见附录 B],
  [字符编码], [*UTF-8*], [JSON 与 TCP payload 均为 UTF-8],
  [TCP 行尾], [LF（`\\n`，0x0A）], [附录 B 每条 TCP 消息以单个换行符结尾],
  [WS 消息识别], [`messageType` 字符串], [每条 JSON 对象必含此字段],
  [心跳], [`ping` / `pong`，建议 10s], [未实现心跳的客户端应被兼容],"""

ch1_5 = ch1_5.replace(tech_old, tech_new)

# §3.2 / §3.3 JSON mapping
json_piece = """
== 老师 JSON piece 枚举映射（§3.2）

老师文档 `piece` 枚举首字母大写（Rook/Knight/…），JSON 示例使用全小写。本组统一 *全小写*（`PieceJsonMapper`），组间互操作以联调为准。

#table(
  columns: (auto, auto, auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center, center, center),
  table.header(
    [*内部编码*], [*中文名*], [*JSON piece*], [*红方*], [*黑方*], [*本组*],
  ),
  [0], [将/帅], [`king`], [帅], [将], [#ok],
  [1], [车], [`rook`], [车], [車], [#ok],
  [2], [马], [`knight`], [马], [馬], [#ok],
  [3], [炮], [`cannon`], [炮], [砲], [#ok],
  [4], [兵/卒], [`pawn`], [兵], [卒], [#ok],
  [5], [士/仕], [`guard`], [仕], [士], [#ok],
  [6], [象/相], [`bishop`], [相], [象], [#ok],
)

== 内部编码 ↔ JSON ↔ 中文三向对照（§3.3）

#table(
  columns: (auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*内部编码*], [*JSON piece*], [*红方中文*], [*黑方中文*]),
  [0], [`king`], [帅], [将],
  [1], [`rook`], [车], [车],
  [2], [`knight`], [马], [马],
  [3], [`cannon`], [炮], [炮],
  [4], [`pawn`], [兵], [卒],
  [5], [`guard`], [仕], [士],
  [6], [`bishop`], [相], [象],
)

*注意*：内部编码 0–6 用于领域层、棋谱记法与附录 B BOARD_STATE；JSON 层仅使用 `piece` 英文字符串。
"""

marker = "*注意*：基准价值仅作为 AI 评估函数的参考，组间互操作不依赖此值。"
ch1_5 = ch1_5.replace(marker, marker + json_piece)

# §5.4 / §5.5 JSON move (renumber WS refs to §6 in move section)
json_move = """
== JSON move 对象格式（§5.4）

与老师文档一致，坐标拆分为列字母与行数字：

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*JSON 字段*], [*类型*], [*说明*]),
  [`fromX` / `toX`], [String], [列 `a`–`i`],
  [`fromY` / `toY`], [int], [行 `0`–`9`（0=红方底线，9=黑方底线）],
  [`isFlip`], [boolean], [true=本步翻子；`from==to` 时为原地翻子],
)

*映射*：`"b"+1` → `source="b1"`；`JsonMessages.parseMove` / `toMoveJson` 负责与 Java `Move` 互转。

== JSON moveResult 对象（§5.5）

#table(
  columns: (auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*字段*], [*类型*], [*说明*], [*本组*]),
  [`success`], [boolean], [消息处理是否成功], [#ok],
  [`valid`], [boolean], [走子是否符合规则], [#ok],
  [`move`], [object], [同客户端 move 结构], [#ok],
  [`flipResult`], [String?], [isFlip 为真时，翻出棋子 `piece` 枚举], [#ok],
)

完整 WS 消息定义见第 6 章。
"""

flip_marker = "+ *安全性*：客户端无法通过伪造 type 值改变翻子结果"
ch1_5 = ch1_5.replace(flip_marker, flip_marker + json_move)

# --- Chapter 6 WS from current (renumber §5 -> §6) ---
ws_start = find_line(cur, "= WebSocket + JSON 通信协议")
ws_end = find_line(cur, "== 本组实现状态总览") - 1
ws_ch6 = slice_lines(cur, ws_start, ws_end)
ws_ch6 = ws_ch6.replace("（§5.", "（§6.").replace("== 传输层约定（§6.1）", "== 传输层约定（§6.1）")
ws_ch6 = ws_ch6.replace(
    "// 第三章：WebSocket + JSON 通信协议（课程公共接口）",
    "// 第六章：WebSocket + JSON 通信协议（课程公共接口，正文主协议）",
)
ws_ch6 = ws_ch6.replace("见 §3]", "见 §3.2]")

# --- Chapter 7 WS flows from current + extras ---
flow_start = find_line(cur, "= 典型通信流程")
flow_end = find_line(cur, "// 第五章：暗子走法与虚拟类型") - 1
ch7_ws = slice_lines(cur, flow_start, flow_end)
ch7_ws = ch7_ws.replace(
    "// 第四章：通信流程",
    "// 第七章：典型通信流程（WebSocket JSON）",
)
ch7_ws += """
== 先手协商时序（requestFirstHand）

#seq-diagram(
  \"
      Client A                    Server                    Client B
            |                         |                         |
            |------ Ready ------------>|------ Ready ------------>|
            |-- requestFirstHand ----->|<-- requestFirstHand -----|
            |<---- gameStart ---------|<---- gameStart --------->|
            |   (colors per wannaFirst negotiation)             |
  \",
  [可选：10s 窗口内双方发送 requestFirstHand，服务器分配红/黑],
  roles: [#grid(columns: (1fr, 1fr, 1fr), align: center,
    [客户端 A], [服务器], [客户端 B],
  )],
)

== 断线判负

#seq-diagram(
  \"
      Client A                    Server                    Client B
            |                         |                         |
            |   (connection close)    |                         |
            |                         |------ gameOver -------->|
            |                         |  reason=disconnect      |
  \",
  [一方 WebSocket 关闭：对方收到 gameOver（reason=disconnect）],
  roles: [#grid(columns: (1fr, 1fr, 1fr), align: center,
    [客户端 A], [服务器], [客户端 B],
  )],
)

#v(0.3cm)
*注*：附录 B §B.7 保留 v2.0 TCP 文本帧典型时序图（LOGIN/MOVE/BOARD_STATE 等），供 TCP 联调参考。
"""

# --- Chapters 8-13 from v2 (暗子 -> 开放问题) ---
ch8_start = find_line(v2, "= 暗子走法与虚拟类型")
ch13_end = find_line(v2, "// 附录") - 1
ch8_13 = slice_lines(v2, ch8_start, ch13_end)

# Insert teacher rules + 不与不应将 into ch9
teacher_rules = """
== 老师揭棋规则对照（§9.0）

以下逐条对照课程《2026大作业——揭棋》「附：揭棋的规则」与本组实现：

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, left, center),
  table.header([*老师规则*], [*本组实现*], [*状态*]),
  [开局：将/帅明放九宫原位，其余 15 枚随机暗放], [`Board` 初始化 + `RandomRevealService`], [#ok],
  [走暗子：按虚拟类型规则行走，走完必翻开；类型由服务器随机决定], [`RuleValidator` + 服务器揭示], [#ok],
  [可原地翻暗子（flip only），消耗一回合], [`isFlip=true` 且 from=to], [#ok],
  [明子士：可出九宫、可过河，斜走一格], [`RuleValidator` 明士强化], [#ok],
  [明子象：可过河，塞象眼/蹩马腿规则不变], [`RuleValidator` 明象强化], [#ok],
  [暗子被吃：吃子方知道类型，被吃方不知道类型], [双方收到相同 `moveResult`/`flipResult`], [#warn],
  [40 回合无吃子（80 半步）→ 和棋], [`EndgameJudge` `noCaptureCount >= 80`], [#ok],
  [长将/长捉：≤6 次重复局面 → 判负；兵卒长捉 → 判和], [局面哈希 + `EndgameJudge`], [#ok],
  [超时：每步 1 分钟 + 5s 网络容忍（65s）], [`WsGameServer` 65000ms 阈值], [#ok],
  [不应将：允许送将，对方下一步可吃将], [仅校验 `isValidMove`，不拦截送将], [#ok],
)

"""
buziyingjiang = """
== 不与「不应将」自动判负

遵循课程要求：走子后己方仍被将军的着法（送将）#strong[允许执行]。`Game` / `ClientHandler` / `WsGameServer` / 客户端仅校验 `isValidMove`，#strong[不]调用 `isMoveLegal` 拦截。对方下一步可通过合法吃将走子，并以 `king_captured`（TCP 原因码 5 / WS `gameOver.reason`）结束对局。TCP 错误码 106 保留编号，本组实现中服务器不因送将返回 106。

"""

ch8_13 = ch8_13.replace(
    "= 胜负与和棋判定\n\n== 胜负条件",
    "= 胜负与和棋判定\n\n" + teacher_rules + "== 胜负条件",
)
ch8_13 = ch8_13.replace(
    "- 计数器在每次吃子后重置\n\n== 超时判定公式",
    "- 计数器在每次吃子后重置\n\n" + buziyingjiang + "== 超时判定公式",
)

# Update checklist: append WS items
ws_checklist = """
#v(0.4cm)
*WebSocket JSON 联调附加项（第 6 章）*：

#enum(
  [WebSocket 连接 `ws://host:8887` 可建立并保持],
  [收到未知 `messageType` 不崩溃（静默忽略或返回 error 4001）],
  [`initialBoard` 能完整解析并重建棋盘],
  [双方 client 收到 `moveResult`（valid=true）后本地同步棋盘],
  [`flipResult` 由服务器生成，客户端不可伪造],
  [`move` 中 `isFlip=true` 且 from≠to 时不误判为原地翻子],
  [超时默认 65s，以服务器 `turnStartTime` 判定],
  [`cancelMatch` 后双方状态与房间已清理],
  [AI 客户端 `WsAIGameClient` 可完成自动对弈],
)
"""
ch8_13 = ch8_13.replace(
    "  [对方断线时本方收到 GAME_OVER（原因码 = 4）而非无限等待],\n)",
    "  [对方断线时本方收到 GAME_OVER（原因码 = 4）而非无限等待],\n)" + ws_checklist,
)

# Replace Q section with current (has Q2 update)
q_start_cur = find_line(cur, "= 待老师确认的开放问题")
q_end_cur = find_line(cur, "// 附录") - 1
q_section = slice_lines(cur, q_start_cur, q_end_cur)

# Add Q45
q45 = """
  [Q45], [
    WS 与 TCP 双协议并存期间，组间联调优先级如何确定？
  ], [
    本组方案：默认 WS 8887 联调；TCP 8888 作为备用/调试通道（附录 B）。
  ], [本组方案],
"""

q_section = q_section.replace(
    "*提交建议*：请老师对 Q1–Q44 逐条确认",
    q45 + "\n*提交建议*：请老师对 Q1–Q45 逐条确认",
).replace("Q1–Q44", "Q1–Q45")

# Replace Q block in ch8_13
q_start_v2 = find_line(v2, "= 待老师确认的开放问题")
ch8_before_q = slice_lines(v2, ch8_start, q_start_v2 - 1)
ch8_13 = ch8_before_q + q_section

# --- Chapter 14 Implementation status ---
ch14 = """
// ============================================================
// 第十四章：实现状态标注
// ============================================================

= 实现状态标注

本章按本组实际代码与测试覆盖，采用以下状态标注：#ok（代码完整且测试通过）；#warn（已实现，但与规范存在差异或测试未全覆盖）；#no（尚未开发或仅作规划）。

== WebSocket 消息类型（第 6 章）

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, center, left),
  table.header([*messageType / 特性*], [*状态*], [*说明*]),
  [Login / register], [#ok], [`WsGameServerIntegrationTest`],
  [startMatch / cancelMatch], [#ok], [含对手 error 3002 通知],
  [requestFirstHand], [#ok], [10s 窗口 + 换色],
  [Ready / roomInfo], [#ok], [],
  [move（含 isFlip）], [#ok], [`JsonMessages.parseMove` 修复],
  [loginResult / matchSuccess / gameStart], [#ok], [含 initialBoard],
  [moveResult / flipResult], [#ok], [服务器权威翻子],
  [timeout / gameOver], [#ok], [65s 阈值],
  [ping / pong], [#ok], [],
  [Resign], [#ok], [],
  [error 1001–4001], [#ok], [12 场景集成测试],
  [未知 messageType 静默忽略], [#warn], [本组返回 error 4001],
)

== 揭棋规则与领域层

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, center, left),
  table.header([*规则/模块*], [*状态*], [*说明*]),
  [RandomRevealService 翻子随机], [#ok], [],
  [RuleValidator 走法/虚拟类型], [#ok], [],
  [EndgameJudge 将死/困毙/和棋], [#ok], [],
  [长将/长捉/40 回合和棋], [#ok], [],
  [暗子被吃信息差], [#warn], [见 Q1；双方广播相同 flipResult],
  [不应将（允许送将）], [#ok], [见 §9],
  [棋谱 .jieqi 存储], [#ok], [`GameRecordStore`],
  [多盘并发 `Map<String, Game>`], [#ok], [`GameRecordStore`],
)

== TCP 扩展（附录 B）

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, center, left),
  table.header([*特性*], [*状态*], [*说明*]),
  [MSG 1–7 核心消息], [#ok], [端口 8888],
  [MSG 8–10 扩展消息], [#ok], [DRAW/RESIGN/CHAT],
  [BOARD_STATE Cell 编码], [#ok], [],
  [FrameDecoder 帧解析], [#ok], [],
)

== 工程与 DevOps

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, center, left),
  table.header([*特性*], [*状态*], [*说明*]),
  [WsGameServerIntegrationTest 12 场景], [#ok], [`jieqi-server`],
  [WsAIGameClient 自动对弈], [#ok], [`jieqi-app` 菜单 9 / `ai-ws`],
  [Docker 默认 WS 8887], [#ok], [`docker-compose.yml`],
  [Bento Web 旁观端], [#no], [加分扩展 Q40–Q44],
  [Redis 匹配队列], [#no], [加分扩展 Q29–Q32],
)

== v2.0 内容保留检查清单（§0.2 自检）

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, left, center),
  table.header([*v2.0 内容*], [*新文档位置*], [*状态*]),
  [术语定义表], [§1.1], [#ok],
  [技术约定], [§1.2 + 附录 B], [#ok],
  [坐标定义与转换公式], [§2.1–2.2], [#ok],
  [棋子类型编码（含基准价值）], [§3.1], [#ok],
  [颜色编码], [§4], [#ok],
  [Move 类/字段/翻子随机性], [§5.1–5.3], [#ok],
  [TCP 帧格式与解析], [附录 B §B.1–B.2], [#ok],
  [MSG 1–10 详细格式], [附录 B §B.4], [#ok],
  [BOARD_STATE Cell 与开局示例], [附录 B §B.4.7], [#ok],
  [ERROR 100–202 / GAME_OVER 0–9], [附录 B §B.5–B.6], [#ok],
  [TCP 五组时序图], [附录 B §B.7], [#ok],
  [虚拟类型/明子强化], [§8.1–8.2], [#ok],
  [胜负/和棋/长将长捉/超时公式], [§9.1–9.4], [#ok],
  [棋谱记录格式], [§10], [#ok],
  [多盘对弈], [§11], [#ok],
  [组间联调清单 14 条 + WS 扩展], [§12], [#ok],
  [Q1–Q44 开放问题], [§13], [#ok],
  [附录 A TCP 速查], [附录 B §B.8], [#ok],
  [版本历史 v0.0.0–v2.0], [附录 C], [#ok],
  [老师 WS+JSON 协议], [§6 新增], [#ok],
  [实现状态标注], [§14 新增], [#ok],
)
"""

# --- Appendix B: full TCP from v2 ---
tcp_start = find_line(v2, "= 网络通信协议")
tcp_proto_end = find_line(v2, "// 第四章：通信流程") - 1
tcp_flow_start = find_line(v2, "= 典型通信流程")
tcp_flow_end = find_line(v2, "// 第五章：暗子走法与虚拟类型") - 1

tcp_body = slice_lines(v2, tcp_start, tcp_proto_end)
tcp_flows = slice_lines(v2, tcp_flow_start, tcp_flow_end)

# Restructure TCP body headings for appendix
tcp_body = tcp_body.replace("= 网络通信协议", "= 附录 B：TCP 文本帧扩展协议 v2.0（Unveil 扩展）")
tcp_body = tcp_body.replace("== 消息帧格式", "== B.1 消息帧格式")
tcp_body = tcp_body.replace("== 消息帧解析规则", "== B.2 消息帧解析规则")
tcp_body = tcp_body.replace("== 消息类型目录", "== B.3 消息类型目录")
tcp_body = tcp_body.replace("== 各消息详细格式", "== B.4 各消息详细格式")
tcp_body = tcp_body.replace("=== MSG_", "=== B.4.x MSG_")

# Fix double B.4.x prefix if any
tcp_body = tcp_body.replace("=== B.4.x B.4.x MSG_", "=== B.4.x MSG_")

tcp_flows = tcp_flows.replace("= 典型通信流程", "== B.7 TCP 典型通信时序图")
tcp_flows = tcp_flows.replace("== 正常对弈时序", "=== B.7.1 正常对弈时序")
tcp_flows = tcp_flows.replace("== 非法着法被拒绝", "=== B.7.2 非法着法被拒绝")
tcp_flows = tcp_flows.replace("== 超时判负", "=== B.7.3 超时判负")
tcp_flows = tcp_flows.replace("== 提和流程", "=== B.7.4 提和流程")
tcp_flows = tcp_flows.replace("== 认输流程", "=== B.7.5 认输流程")

appendix_b_intro = """
本附录为 Unveil 历史扩展协议（v2.0 正文第 6 章完整迁移），供需要 TCP 通信的组参考或 telnet 调试使用。组间联调默认使用正文 WebSocket + JSON 协议（第 6 章）。

Unveil 保留 TCP `msgType|payloadLen|payload\\n` 实现（端口 *8888*）。实现类：`Protocol.java`、`GameServer`、`GameClient`、`FrameDecoder`。

*组间互操作*：与对方联调前须约定使用 WebSocket JSON 或 TCP v2.0（本附录）；不可混用。

"""

tcp_appendix = (
    "// ============================================================\n"
    "// 附录 B\n"
    "// ============================================================\n\n"
    + appendix_b_intro
    + tcp_body
    + "\n"
    + tcp_flows
    + "\n"
    + "== B.8 TCP 消息快速参考卡片\n\n"
    + slice_lines(v2, find_line(v2, "= 附录 A：消息快速参考卡片") + 1, find_line(v2, "= 附录 B：版本历史") - 1)
)

# --- Appendix A WS quick ref ---
app_a_start = find_line(cur, "= 附录 A：JSON messageType 快速参考")
app_a_end = find_line(cur, "= 附录 B：TCP 文本帧扩展协议") - 1
appendix_a = slice_lines(cur, app_a_start, app_a_end).replace(
    "= 附录 A：JSON messageType 快速参考",
    "= 附录 A：WS JSON messageType 快速参考卡片",
)

# --- Appendix C version history ---
ver_start = find_line(v2, "= 附录 B：版本历史")
ver_v2 = slice_lines(v2, ver_start, len(v2))
ver_v2 = ver_v2.replace("= 附录 B：版本历史", "= 附录 C：版本历史")
ver_v2 = ver_v2.rstrip()
if ver_v2.endswith("),"):
    ver_v2 = ver_v2[:-2]
ver_v2 += """
  [v3.0], [2026-05-29], [
    对齐课程《2026大作业公共接口》：WebSocket + JSON 升为正文主协议（第 6 章）；\
    原 v2.0 TCP 协议完整迁移至附录 B；默认端口 8887；\
    新增 WS 联调清单条目
  ],
  [v3.1], [2026-05-30], [
    在 v2.0 全文保留基础上扩展（27 条保留自检清单，§14 实现状态标注）；\
    补全 C→S/S→C 字段级说明与老师原文 JSON 示例；\
    附录 B 含 MSG 1–10、BOARD_STATE/Cell 编码、TCP 五组时序图
  ],
)
"""

# Assemble
out = (
    preamble
    + ch1_5
    + "\n// ============================================================\n"
    + "// 第六章：WebSocket + JSON（老师公共接口）\n"
    + "// ============================================================\n\n"
    + ws_ch6
    + "\n// ============================================================\n"
    + "// 第七章：通信流程\n"
    + "// ============================================================\n\n"
    + ch7_ws
    + ch8_13
    + ch14
    + "\n// ============================================================\n"
    + "// 附录\n"
    + "// ============================================================\n\n"
    + appendix_a
    + "\n"
    + tcp_appendix
    + "\n"
    + ver_v2
    + "\n"
)

out_path = ROOT / "INTERFACE.typ"
out_path.write_text(out, encoding="utf-8")
print(f"Wrote {out_path} ({len(out.splitlines())} lines)")
