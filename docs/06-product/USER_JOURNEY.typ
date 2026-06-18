#import "../template.typ": *
#show: doc => [ #cover(title: "用户旅程", subtitle: "User Journey — 真人对战/人机对战/验收三条路径", doc-type: "产品体验") #doc ]
#setup-doc(title: "Unveil — 用户旅程")

= 旅程总览

```text
                    进入系统（启动菜单/JAR）
                           │
              ┌────────────┼────────────┐
              ▼            ▼            ▼
         WS 真人对战    人机对战     AI 自对弈
              │            │            │
              └────────────┼────────────┘
                           ▼
                    登录（userId+密码）
                           ▼
                    匹配（match）
                           ▼
                 准备+争先手（ready/first）
                           ▼
                    开局（gameStart）
                           ▼
          ┌────────────────┴────────────────┐
          ▼                ▼                ▼
        走子           聊天/提和          认输
       (move)       (chat/draw)       (resign)
          │                │                │
          └────────────────┼────────────────┘
                           ▼
                    终局（gameOver）
                           ▼
                  查看结果（摘要/棋谱路径）
                           ▼
                    复盘（replay 帧切换）
                           ▼
              ┌────────────┴────────────┐
              ▼                         ▼
          再来一局                   返回大厅
         (rematch)                    (退出)
```

= 分步旅程表

#table(
  columns: (auto, auto, auto, auto, auto),
  [*步骤*], [*用户操作*], [*系统行为*], [*界面输出*], [*可选分支*],
  [S1 进入], [运行启动菜单或 JAR], [显示 1–9 菜单], [9 项菜单], [命令行传参跳过],
  [S2 选模式], [输入 3（WS 客户端）或 5（人机）], [提示输入参数], [URL / userId 默认值], [选 TCP 客户端],
  [S3 登录], [输入 login], [发送 Login JSON；返回 loginResponse], [success: true], [密码错误→提示],
  [S4 匹配], [输入 match], [撮合两名玩家；返回 matchSuccess], [显示 roomId、yourColor], [人机：分配 AI 房间],
  [S5 准备], [双方输入 ready], [就绪后进入争先手], [提示等待对手], [—],
  [S6 争先手], [输入 first true/false], [服务器决定先手；广播 gameStart], [显示初始棋盘], [—],
  [S7 开局], [查看棋盘], [下发 initialBoard；开始计时], [ASCII 棋盘；行棋方高亮], [—],
  [S8 走子], [输入 move e6 e5], [校验→moveResult（含 flipResult）→切换回合], [棋盘更新；翻子显示真实 type], [非法→error 不变盘],
  [S9 交互], [chat 你好 / draw / resign], [广播聊天；处理和棋；认输终局], [聊天显示], [对方拒绝和棋→继续],
  [S10 超时], [无操作 65s], [服务器判负], [gameOver reason=TIMEOUT], [可申请加时],
  [S11 终局], [—], [广播 gameOver（winner + reason）], [显示终局原因], [—],
  [S12 结果], [阅读终局摘要], [客户端打印 GameSummary], [胜者、步数、棋谱路径], [—],
  [S13 复盘], [输入 replay / next / prev], [发送 replayRequest；返回 replayFrame], [按帧显示历史棋盘], [终局后上帝视角],
  [S14 重赛], [输入 rematch], [rematchRequest / 新 gameStart], [新房间或同房间重置], [拒绝则返回大厅],
  [S15 退出], [quit 或关闭终端], [断开 WebSocket；房间清理], [连接关闭提示], [断线可能判负],
)

= 人机对战差异

#table(
  columns: (auto, auto),
  [*步骤*], [*与真人对战差异*],
  [S4 匹配], [服务器分配 AI 对手，无需第二名玩家],
  [S8 走子], [用户走子后 AI 自动思考并走子],
  [S9], [无聊天需求；可认输结束],
  [S13 复盘], [与真人对战相同],
)

= 教师验收旅程（精简）

#table(
  columns: (auto, auto, auto),
  [*时间*], [*步骤*], [*关注点*],
  [0:00], [运行 verify.ps1], [测试全绿],
  [0:30], [启动服务器 + 双客户端], [8887 端口],
  [2:00], [匹配开局 + 走一步], [棋盘同步],
  [3:00], [非法走法], [拒绝],
  [4:00], [AI 对战一步], [合法 + 限时],
  [5:00], [终局 + 复盘], [摘要 + 帧切换],
  [6:00], [展示 INTERFACE.pdf], [协议对齐],
)

详见 `DEMO_SCRIPT.typ`。
