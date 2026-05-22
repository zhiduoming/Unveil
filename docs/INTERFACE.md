# 公共接口约定（组间互操作）

> 提交老师认可后，各小组服务器与客户端应遵循本协议。当前实现见 `jieqi-core/src/main/java/com/jieqi/protocol/Protocol.java`。

## 1. 坐标与棋谱记谱

- 行：从上到下 `9` → `0`（共 10 行）
- 列：从左到右 `a` → `i`（共 9 列）
- 先手在棋盘**下方**（客户端 UI 可将己方置于下方，逻辑坐标仍按服务器约定）
- 一步：`source` + `destination`，例如 `b3` → `b4`
- **首次翻开**的棋子：必须带 `type`（由**服务器**随机决定真实类型）

### type 编码

| 值 | 棋子 |
|----|------|
| 0 | 将/帅 |
| 1 | 车 |
| 2 | 马 |
| 3 | 炮 |
| 4 | 兵/卒 |
| 5 | 士 |
| 6 | 象 |

## 2. Move 对象（逻辑模型）

```text
source: String          // 如 "b3"
destination: String     // 如 "b4"
type: Integer | null    // 首翻必填；后续可空
turnStartTime: long     // 毫秒；判超时以服务器收到/记录时刻为准
clientTimestamp: long    // 可选，服务器可忽略防伪造
serverTimestamp: long   // 服务器写入
flipOnly: boolean       // source == destination 表示仅翻子
```

超时建议：`serverNow - turnStartTime > limitMs + networkGraceMs`（默认 `networkGraceMs = 5000`）。

## 3. TCP 消息帧格式

```text
msgType|payloadLength|payload
```

### msgType

| 值 | 含义 |
|----|------|
| 1 | LOGIN |
| 2 | MOVE |
| 3 | GAME_STATE |
| 4 | ERROR |
| 5 | QUIT |
| 6 | GAME_OVER |
| 7 | BOARD_STATE |

### MOVE payload

```text
source|destination|type|turnStartTime|flipOnly
```

`type` 为空字符串表示未指定（非首翻）。

### LOGIN payload

```text
color|playerName
```

`color`：0=红，1=黑（与实现保持一致即可，文档化即可）。

## 4. 行为约定

1. **非法着法**：服务器拒绝并返回 `MSG_ERROR`；不自动因「不应将」判负（作业说明）。
2. **翻子随机**：客户端提交的 `type` 在首翻时由服务器覆盖/生成。
3. **棋谱**：服务器按时间顺序保存每条 MOVE 的序列化串或 JSON 行。
4. **多盘**：LOGIN 或后续扩展字段携带 `gameId`（待各组统一）。

## 5. 待向老师确认的问题（REQUIREMENTS 加分项）

- 长将/长捉：6 回合计负 vs 兵卒长捉和的具体判定算法  
- 重复局面哈希是否包含 Hidden 信息  
- 和棋时是否广播原因码  
- 端口、字符编码（建议 UTF-8）、换行符  
