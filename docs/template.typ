// Unveil 文档共用模板（派生自 INTERFACE.typ v3.1）
// Typst 0.14.2 兼容

#let metadata = (
  project: "Unveil — 揭棋对弈程序",
  course: "揭棋对弈程序设计 · 北京邮电大学",
  team: "第一组",
  members: ("张恒基（组长）", "秦博宇", "陈艺博", "陈雨飞"),
)

#let page-footer = context place(
  bottom + center,
  dy: -14pt,
)[
  #text(size: 10.5pt, fill: rgb(148, 163, 184))[#counter(page).display()]
]

#let setup-doc(title: "", author: ("第一组",)) = {
  set document(
    title: title,
    author: author,
    date: datetime(year: 2026, month: 6, day: 18),
  )
  set page(
    margin: (left: 2.5cm, right: 2.5cm, top: 2.2cm, bottom: 2.2cm),
    numbering: "1",
    footer: page-footer,
  )
  set text(font: ("SimSun", "SimHei", "Microsoft YaHei"), size: 12pt)
  set par(leading: 0.85em, first-line-indent: 0pt, spacing: 0.65em)
  set heading(numbering: "1.")

  show heading.where(level: 1): it => {
    block(breakable: false, above: 2em, below: 1em)[
      block(
        width: 100%,
        inset: (left: 10pt, top: 10pt, bottom: 10pt),
        fill: rgb(239, 246, 255),
        radius: 4pt,
        stroke: (left: 4pt + rgb(30, 64, 175)),
      )[
        text(size: 20pt, weight: "bold", fill: rgb(30, 58, 138))[#it]
      ]
    ]
  }

  show heading.where(level: 2): it => {
    block(above: 1.4em, below: 0.7em)[
      text(size: 15pt, weight: "bold", fill: rgb(30, 64, 175))[#it]
      v(0.15em)
      line(length: 100%, stroke: 0.5pt + rgb(191, 219, 254))
    ]
  }

  show heading.where(level: 3): it => {
    block(above: 1.1em, below: 0.55em)[
      text(size: 13pt, weight: "bold", fill: rgb(51, 65, 85))[#it]
    ]
  }

  set table(inset: (x: 10pt, y: 8pt))
  show figure.caption: set text(size: 11pt)
}

#let cover(title: "", subtitle: "", doc-type: "技术文档") = {
  page(margin: (top: 2.2cm, bottom: 2.2cm, x: 2.8cm), numbering: none, footer: none)[
    align(center + horizon)[
      block(
        width: 15cm,
        inset: (y: 1.1cm),
        stroke: (top: 2.5pt + rgb(26, 54, 93), bottom: 0.75pt + rgb(203, 213, 225)),
      )[
        align(center)[
          text(size: 10.5pt, tracking: 0.35em, fill: rgb(71, 85, 105))[大 作 业]
          v(0.55cm)
          text(size: 26pt, weight: "bold", fill: rgb(15, 23, 42))[揭棋对弈程序设计]
          v(0.65cm)
          text(size: 19pt, weight: "medium", fill: rgb(30, 64, 175))[#title]
          v(0.35cm)
          if subtitle != "" [
            text(size: 13pt, fill: rgb(100, 116, 139))[#subtitle]
            v(0.35cm)
          ]
        ]
      ]

      v(1.5cm)

      box(
        width: 13cm,
        inset: (x: 1.2cm, y: 0.95cm),
        fill: rgb(248, 250, 252),
        radius: 6pt,
        stroke: 0.75pt + rgb(226, 232, 240),
      )[
        align(center)[
          text(size: 11pt, weight: "bold", fill: rgb(51, 65, 85))[小组成员]
          v(0.55cm)
          grid(
            columns: (1fr, 1fr),
            column-gutter: 1.6cm,
            row-gutter: 0.65cm,
            align: center + horizon,
            [text(size: 13pt, weight: "bold")[张恒基（组长）]\ text(size: 12pt, fill: rgb(100, 116, 139))[2024211301 / 2024210926]],
            [text(size: 13pt, weight: "bold")[秦博宇]\ text(size: 12pt, fill: rgb(100, 116, 139))[2024211302 / 2024210940]],
            [text(size: 13pt, weight: "bold")[陈艺博]\ text(size: 12pt, fill: rgb(100, 116, 139))[2024211302 / 2024210931]],
            [text(size: 13pt, weight: "bold")[陈雨飞]\ text(size: 12pt, fill: rgb(100, 116, 139))[2024211301 / 2024210918]],
          )
        ]
      ]

      v(1cm)
      text(size: 12pt, fill: rgb(100, 116, 139))[#doc-type · 2026-06-18]
      v(0.5cm)
      text(size: 10.5pt, fill: rgb(148, 163, 184))[北京邮电大学 · 计算机学院]
    ]
  ]
}

#let status-ok = text(fill: rgb(21, 128, 61))[已实现]
#let status-warn = text(fill: rgb(194, 65, 12))[待强化]
#let status-exp = text(fill: rgb(161, 98, 7))[实验性]
#let status-plan = text(fill: rgb(185, 28, 28))[规划中]

#let mono-font = ("Consolas", "Courier New", "DejaVu Sans Mono")
#show raw.where(block: false): set text(font: mono-font, size: 10.5pt)
#show raw.where(block: true): it => block(
  width: 100%,
  breakable: true,
  fill: rgb(248, 250, 252),
  inset: 10pt,
  radius: 3pt,
  stroke: 0.5pt + rgb(226, 232, 240),
)[
  set text(font: mono-font, size: 10.5pt)
  set par(leading: 0.65em)
  it
]

#let note-box(body) = block(
  width: 100%,
  fill: rgb(240, 249, 255),
  inset: 12pt,
  radius: 4pt,
  stroke: 0.5pt + rgb(186, 230, 253),
)[
  set text(size: 11pt, fill: rgb(3, 105, 161))
  body
]
