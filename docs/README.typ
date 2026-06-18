#import "template.typ": *
#show: doc => [ #cover(title: "文档索引", subtitle: "Docs Index — 六类文档体系导航", doc-type: "验收总览") #doc ]
#setup-doc(title: "Unveil — 文档索引")

= 00 · 验收总览

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [PROJECT_OVERVIEW.typ], [3 分钟项目总览],
  [FEATURE_MATRIX.typ], [功能完成度矩阵（四档状态）],
  [GLOSSARY.typ], [术语表],
)

= 01 · 需求分析

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [REQUIREMENTS.typ], [需求追踪（分级标注）],
  [ACCEPTANCE_CRITERIA.typ], [课程验收对照表],
)

= 02 · 技术设计

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [ARCHITECTURE.typ], [总体架构与模块依赖],
  [DOMAIN_MODEL.typ], [领域模型与状态机],
  [RULE_ENGINE_DESIGN.typ], [规则引擎设计],
  [AI_DESIGN.typ], [AI 算法设计],
  [REPLAY_DESIGN.typ], [复盘时间线设计],
)

= 03 · 接口协议

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [INTERFACE.typ], [*权威协议* v3.0（Typst 源文件）],
  [INTERFACE.pdf], [协议 PDF],
  [MESSAGE_EXAMPLES.typ], [JSON 消息交互示例],
  [INTEROP.typ], [组间联调说明],
)

= 04 · 工程交付

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [BUILD_AND_RUN.typ], [构建与运行],
  [DOCKER_DEPLOYMENT.typ], [Docker 部署],
  [TROUBLESHOOTING.typ], [常见问题排查],
)

= 05 · 测试证明

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [TEST_PLAN.typ], [测试方案],
  [TEST_CASES.typ], [测试用例清单（45 条）],
  [TEST_REPORT.typ], [测试报告],
  [COMPLETION_REPORT.typ], [历史自检流水（参考）],
)

= 06 · 产品体验

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [PRODUCT_REQUIREMENTS.typ], [产品需求 PRD],
  [USER_JOURNEY.typ], [用户旅程],
  [COMPETITOR_ANALYSIS.typ], [竞品对比],
)

= 07 · 答辩材料

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [DEMO_SCRIPT.typ], [6–8 分钟演示脚本],
  [DEFENSE_QA.typ], [答辩问答预演],
  [FINAL_REPORT.typ], [最终大报告（整合版）],
)

= 其他

#table(
  columns: (auto, auto),
  [*文档*], [*说明*],
  [TEAM.typ], [团队分工与贡献度],
  [TASKS_COMPLETION_STATUS.typ], [四大任务完成状态],
  [fupantasks.typ / suanfatasks.typ / chanpintasks.typ / wendangtasks.typ], [内部实现指南，不提交老师],
)
