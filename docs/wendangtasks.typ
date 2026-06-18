#import "template.typ": *
#show: doc => [ #cover(title: "文档体系任务书", subtitle: "Docs Tasks — 六类文档体系重建", doc-type: "内部实现指南") #doc ]
#setup-doc(title: "Unveil — 文档体系任务书")

= 目标目录结构

```
docs/
├── 00-overview/      验收报告（3 份）
├── 01-requirements/  需求分析（2 份）
├── 02-design/        技术设计（5 份）
├── 03-interface/     接口协议（3 份）
├── 04-deployment/    工程交付（3 份）
├── 05-testing/       测试证明（4 份）
├── 06-product/       产品体验（3 份）
├── 07-presentation/  答辩材料（3 份）
└── 任务文件（4 份，不入库）
```

= 任务状态

#table(
  columns: (auto, auto),
  [*任务*], [*状态*],
  [六类目录 00-07 搭建], [#status-ok],
  [24 份文档全部交付], [#status-ok],
  [统计数字修正（113→63）], [#status-ok],
  [旧文件清理（11 个）], [#status-ok],
  [count-loc.ps1 创建], [#status-ok],
  [docs README 索引], [#status-ok],
  [所有 MD 转 Typst], [#status-ok],
  [所有 Typst 编译 PDF], [#status-ok],
)

= 文档质量检查清单

- 目标读者明确（老师 / 同学 / 用户）
- 无超过 10 行的纯文字段落
- 设计文档有 Mermaid 图或 ASCII 图
- 统计数字用工具实测（count-loc.ps1）
- 与 INTERFACE.typ 无矛盾
- 文档间引用链接有效

= 最终交付

#table(
  columns: (auto, auto, auto),
  [*类别*], [*文档数*], [*优先级*],
  [00-overview], [3 份], [★★★],
  [01-requirements], [2 份], [★★☆],
  [02-design], [5 份], [★★★],
  [03-interface], [3 份], [★★☆],
  [04-deployment], [3 份], [★☆☆],
  [05-testing], [4 份], [★★★],
  [06-product], [3 份], [★☆☆],
  [07-presentation], [3 份], [★★★],
  [合计], [26 份], [—],
)
