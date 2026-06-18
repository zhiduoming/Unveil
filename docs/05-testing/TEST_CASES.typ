#import "../template.typ": *
#show: doc => [ #cover(title: "测试用例清单", subtitle: "Test Cases — 45 条测试用例（规则/终局/AI/网络/复盘/工程）", doc-type: "测试证明") #doc ]
#setup-doc(title: "Unveil — 测试用例清单")

= 规则测试（R01–R15）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [R01], [规则], [车直线走子], [isValidMove=true], [BoardMakeMoveTest],
  [R02], [规则], [马日字走子], [含合法马步], [BoardMakeMoveTest],
  [R03], [规则], [炮隔子吃子], [吃子合法], [DarkPieceRuleTest],
  [R04], [规则], [兵过河横走], [合法], [RuleValidator + 手动],
  [R05], [规则], [将九宫走子], [合法], [RuleEdgeCaseTest],
  [R06], [规则], [暗士限九宫], [拒绝], [DarkPieceRuleTest],
  [R07], [规则], [明象可过河], [允许], [DarkPieceRuleTest],
  [R08], [规则], [蹩马腿], [拒绝], [DarkPieceRuleTest],
  [R09], [规则], [塞象眼], [拒绝], [DarkPieceRuleTest],
  [R10], [规则], [炮吃需炮架], [拒绝], [DarkPieceRuleTest],
  [R11], [规则], [送将拒绝], [isMoveLegal=false], [RuleEdgeCaseTest],
  [R12], [规则], [将帅照面], [isMoveLegal=false], [RuleEdgeCaseTest],
  [R13], [规则], [被将须解将], [严格合法集⊂全部走法], [RuleEdgeCaseTest],
  [R14], [规则], [将死判定], [CHECKMATE], [EndgameJudgeTest],
  [R15], [规则], [困毙判定], [STALEMATE], [EndgameJudgeTest],
)

= 终局测试（E01–E06）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [E01], [终局], [40 步无吃子和], [DRAW], [RuleEdgeCaseTest],
  [E02], [终局], [长将 6 次判负], [REPETITION_LOSS], [RuleEdgeCaseTest],
  [E03], [终局], [长捉 6 次判负], [REPETITION_LOSS], [RuleEdgeCaseTest],
  [E04], [终局], [兵卒长捉和], [REPETITION_DRAW], [RuleEdgeCaseTest],
  [E05], [终局], [超时], [TIMEOUT], [GameEndgameTest + WS 集成],
  [E06], [终局], [认输], [对方胜], [GameServerResignIntegrationTest],
)

= AI 测试（A01–A06）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [A01], [AI], [Easy 合法走法], [返回合法步], [AiBotFactoryTest],
  [A02], [AI], [Medium 限时返回], [5 秒内有结果], [JieqiAgentTest],
  [A03], [AI], [不透视暗子], [对手暗子 UNKNOWN], [AiFairnessTest],
  [A04], [AI], [被将须解将], [解将或合法 fallback], [OptimizedAlphaBetaTacticalTest],
  [A05], [AI], [无步可走走法 null], [null 或 fallback], [AiBotFactoryTest],
  [A06], [AI], [搜索不污染棋盘], [positionKey 不变], [BoardUndoTest],
)

= 网络测试（N01–N05）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [N01], [网络], [双客户端匹配开局], [gameStart], [WsGameServerIntegrationTest],
  [N02], [网络], [走子双方同步], [双方 moveResult], [GameServerMoveIntegrationTest],
  [N03], [网络], [非法走法拒绝], [valid=false / error], [GameServerIllegalMoveIntegrationTest],
  [N04], [网络], [聊天], [chatMessage 广播], [GameServerChatIntegrationTest],
  [N05], [网络], [提和/认输], [gameOver], [GameServerDrawIntegrationTest / resign],
)

= 复盘测试（P01–P04）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [P01], [复盘], [开局帧], [stepIndex=0], [ReplayTimelineTest],
  [P02], [复盘], [每步递增], [帧数+2], [GameReplayTest],
  [P03], [复盘], [帧棋盘拷贝], [独立 Board], [ReplayTimelineTest],
  [P04], [复盘], [replay.json 落盘], [文件存在], [WsGameServerIntegrationTest],
)

= 工程测试（B01–B04）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [B01], [工程], [全模块编译], [BUILD SUCCESS], [CI / verify.ps1],
  [B02], [工程], [单元+集成测试], [142 tests, 0 failures], [mvn test],
  [B03], [工程], [自检脚本], [OK: verify passed], [scripts/verify.ps1],
  [B04], [工程], [Fat JAR], [unveil-jieqi.jar], [verify.ps1],
)

= 扩展用例（X01–X05）

#table(
  columns: (auto, auto, auto, auto, auto),
  [*编号*], [*模块*], [*用例名称*], [*预期结果*], [*实现位置*],
  [X01], [协议], [JSON 棋盘往返], [一致], [BoardJsonMapperReplayTest],
  [X02], [协议], [capturedReveal 序列化], [JSON 数组], [JsonMessagesCapturedTest],
  [X03], [棋谱], [导入导出], [往返一致], [GameRecordImportTest],
  [X04], [AI], [Agent 编排], [合法], [AgentOrchestratorTest],
  [X05], [AI], [长将规避启发], [分数变化], [OptimizedAlphaBetaRepetitionTest],
)

*合计*：45 条（≥ 40 要求）。
