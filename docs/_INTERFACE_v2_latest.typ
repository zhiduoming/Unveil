// 鎻瀵瑰紙 鈥?鍏叡閫氫俊鍗忚 v2.0
// Typst 鏂囨。锛屽彲鐩存帴缂栬瘧涓?PDF
// 缂栬瘧: typst compile docs/INTERFACE.typ

#set document(
  title: "鎻瀵瑰紙 鈥?鍏叡閫氫俊鍗忚瑙勮寖",
  author: ("寮犳亽鍩?, "绉﹀崥瀹?, "闄堣壓鍗?, "闄堥洦椋?),
  date: datetime(year: 2026, month: 5, day: 22),
)

#let page-footer = context place(
  bottom + center,
  dy: -14pt,
)[
  #text(size: 10.5pt, fill: rgb(148, 163, 184))[#counter(page).display()]
]

#set page(
  margin: (left: 2.5cm, right: 2.5cm, top: 2.2cm, bottom: 2.2cm),
  numbering: "1",
  footer: page-footer,
)

// 姝ｆ枃锛氳‖绾夸綋锛堝畫浣撲紭鍏堬級
#let main-font = ("SimSun", "SimHei", "Microsoft YaHei")
#set text(font: main-font, size: 12pt)
#set par(leading: 0.85em, first-line-indent: 0pt, spacing: 0.65em)
#set heading(numbering: "1.")

// 鍏ㄦ枃瀛楀彿鍩哄噯
#let h1-size = 20pt
#let h2-size = 15pt
#let h3-size = 13pt
#let code-size = 10.5pt
#let seq-size = 11pt
#let payload-size = 9pt
#let caption-size = 11pt
#let hint-size = 11pt

#show heading: set text(font: main-font)

// 姝ｆ枃鏍囬灞傜骇鏍峰紡锛堜笌鐩綍鏉＄洰鍖哄垎锛?#show heading.where(level: 1): it => {
  block(breakable: false, above: 2em, below: 1em)[
    #block(
      width: 100%,
      inset: (left: 10pt, top: 10pt, bottom: 10pt),
      fill: rgb("#eff6ff"),
      radius: 4pt,
      stroke: (left: 4pt + rgb("#1e40af")),
    )[
      #text(size: h1-size, weight: "bold", fill: rgb("#1e3a8a"))[#it]
    ]
  ]
}
#show heading.where(level: 2): it => {
  block(above: 1.4em, below: 0.7em)[
    #text(size: h2-size, weight: "bold", fill: rgb("#1e40af"))[#it]
    #v(0.15em)
    #line(length: 100%, stroke: 0.5pt + rgb("#bfdbfe"))
  ]
}
#show heading.where(level: 3): it => {
  block(above: 1.1em, below: 0.55em)[
    #text(size: h3-size, weight: "bold", fill: rgb("#334155"))[#it]
  ]
}

// 鍥捐〃棰樻敞
#show figure.caption: set text(size: caption-size)

// 姝ｆ枃琛ㄦ牸锛氬姞澶у崟鍏冩牸鍐呰竟璺?#set table(inset: (x: 10pt, y: 8pt))

// 浠ｇ爜鍧?/ 鏃跺簭鍥撅細绛夊瀛椾綋锛岄伩鍏?| 琚瑙ｆ瀽
#let mono-font = ("Consolas", "Courier New", "DejaVu Sans Mono")
#show raw.where(block: false): set text(font: mono-font, size: code-size)

// 鏃跺簭鍥撅細绛夊 ASCII锛岀珫绾?| 鍒楀榻愶紙鍥惧唴鑻辨枃鏍囩锛宑aption 涓枃璇存槑锛?#let seq-diagram(content, caption, roles: none) = figure(
  block(
    width: 100%,
    fill: rgb("#f8fafc"),
    inset: 14pt,
    radius: 4pt,
    stroke: 0.5pt + rgb("#e2e8f0"),
    breakable: true,
  )[
    #if roles != none [
      #align(center)[
        #text(size: hint-size, fill: rgb("#334155"))[#roles]
      ]
      #v(8pt)
    ]
    #set text(font: mono-font, size: seq-size)
    #set par(leading: 0.75em, spacing: 0pt)
    #raw(block: true, lang: "text", content.trim())
  ],
  caption: caption,
)

// 闀?payload锛氭寜琛屽睍绀猴紙閫愯 trim锛岄伩鍏嶆簮鐮佺缉杩涚┖鏍硷紱閬垮厤涓庨〉鐮侀噸鍙狅級
#let payload-block(content, title: none) = block(
  width: 100%,
  fill: rgb("#f8fafc"),
  inset: 12pt,
  radius: 4pt,
  stroke: 0.5pt + rgb("#e2e8f0"),
  breakable: true,
)[
  #if title != none [
    #text(weight: "bold", size: hint-size)[#title]
    #v(6pt)
  ]
  #set text(font: mono-font, size: payload-size)
  #set par(leading: 0.62em, spacing: 0pt)
  #for line in content.trim().split("\n") {
    let row = line.trim()
    if row.len() > 0 [
      #raw(row)
      #linebreak()
    ]
  }
]

// 鏅€氫唬鐮佸潡锛氶檺鍒跺湪鐗堝績瀹藉害鍐?#show raw.where(block: true): it => block(
  width: 100%,
  breakable: true,
  fill: rgb("#f8fafc"),
  inset: 10pt,
  radius: 3pt,
  stroke: 0.5pt + rgb("#e2e8f0"),
)[
  #set text(font: mono-font, size: code-size)
  #set par(leading: 0.65em)
  #it
]

// ============================================================
// 灏侀潰
// ============================================================

#page(margin: (top: 2.2cm, bottom: 2.2cm, x: 2.8cm), numbering: none, footer: none)[
  #set text(font: main-font)
  #align(center + horizon)[
    #block(
      width: 15cm,
      inset: (y: 1.1cm),
      stroke: (top: 2.5pt + rgb("#1a365d"), bottom: 0.75pt + rgb("#cbd5e1")),
    )[
      #align(center)[
        #text(size: 10.5pt, tracking: 0.35em, fill: rgb("#475569"))[澶?浣?涓?路 绗?涓€ 缁刔
        #v(0.55cm)
        #text(size: 26pt, weight: "bold", fill: rgb("#0f172a"))[鎻瀵瑰紙绋嬪簭璁捐]
        #v(0.65cm)
        #text(size: 19pt, weight: "medium", fill: rgb("#1e40af"))[鍏叡閫氫俊鍗忚瑙勮寖]
        #v(0.35cm)
        #text(size: 13pt, fill: rgb("#64748b"))[Interface Protocol Specification 路 v2.0]
      ]
    ]

    #v(1.5cm)

    #box(
      width: 13cm,
      inset: (x: 1.2cm, y: 0.95cm),
      fill: rgb("#f8fafc"),
      radius: 6pt,
      stroke: 0.75pt + rgb("#e2e8f0"),
    )[
      #align(center)[
        #text(size: 11pt, weight: "bold", fill: rgb("#334155"))[灏忕粍鎴愬憳]
        #v(0.55cm)
        #grid(
          columns: (1fr, 1fr),
          column-gutter: 1.6cm,
          row-gutter: 0.65cm,
          align: center + horizon,
          [
            #text(size: 13pt, weight: "bold")[寮犳亽鍩猴紙缁勯暱锛塢 \
            #v(0.25cm)
            #text(size: 12pt, fill: rgb("#64748b"))[
              2024211301 \
              2024210926
            ]
          ],
          [
            #text(size: 13pt, weight: "bold")[绉﹀崥瀹嘳 \
            #v(0.25cm)
            #text(size: 12pt, fill: rgb("#64748b"))[
              2024211302 \
              2024210940
            ]
          ],
          [
            #text(size: 13pt, weight: "bold")[闄堣壓鍗歖 \
            #v(0.25cm)
            #text(size: 12pt, fill: rgb("#64748b"))[
              2024211302 \
              2024210959
            ]
          ],
          [
            #text(size: 13pt, weight: "bold")[闄堥洦椋瀅 \
            #v(0.25cm)
            #text(size: 12pt, fill: rgb("#64748b"))[
              2024211304 \
              2024211005
            ]
          ],
        )
      ]
    ]

    #v(1.4cm)

    #align(center)[
      #text(size: 11pt, fill: rgb("#64748b"))[椤圭洰浠ｅ彿锛歎nveil]
      #v(0.25cm)
      #text(size: 11pt, fill: rgb("#64748b"))[2026 骞?5 鏈?22 鏃
    ]
  ]

  #v(1fr)
  #align(center)[
    #text(size: caption-size, fill: rgb(148, 163, 184))[
      鏈枃妗ｄ负 Unveil 鎿嶄綔鐨勫敮涓€鏉冨▉鏍煎紡
    ]
  ]
]

#pagebreak()

// ============================================================
// 鐩綍
// ============================================================

#set page(numbering: none, footer: none)
#outline(title: "鐩綍", indent: 2em)

#pagebreak()
#set page(
  numbering: "1",
  footer: page-footer,
)
#counter(page).update(1)

// ============================================================
// 绗竴绔狅細鍩虹绾﹀畾
// ============================================================

= 鍩虹绾﹀畾

== 鏈瀹氫箟

#table(
  columns: (auto, auto),
  stroke: none,
  align: (left, left),
  [*鏈*], [*鍚箟*],
  [鏆楀瓙], [鑳岄潰鏈濅笂銆佸皻鏈炕寮€鐨勬瀛愶紝鎸夋墍鍦ㄤ綅缃搴旂殑涓浗璞℃妫嬪瓙瑙勫垯绉诲姩],
  [鏄庡瓙], [姝ｉ潰鏈濅笂銆佸凡缈诲紑鐨勬瀛愶紝鎸夊疄闄呯被鍨嬭鍒欑Щ鍔╙,
  [缈诲瓙], [灏嗘殫瀛愬彉涓烘槑瀛愮殑鎿嶄綔銆傚彲閫氳繃绉诲姩瑙﹀彂锛屼篃鍙師鍦扮炕瀛愶紙娑堣€椾竴鍥炲悎锛塢,
  [鍏堟墜], [绾㈡柟锛岃妫嬩紭鍏堟潈锛屾鐩樹笅鏂筣,
  [鍚庢墜], [榛戞柟锛屾鐩樹笂鏂筣,
  [鍥炲悎], [涓€鏂瑰畬鎴愪竴娆¤蛋瀛愭垨缈诲瓙鎿嶄綔],
  [鍗婃], [涓€鏂圭殑涓€娆¤蛋瀛愶紙40 鍥炲悎 = 鍙屾柟鍏?80 涓崐姝ワ級],
)

== 鎶€鏈害瀹?
#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (left, left, left),
  table.header(
    [*椤圭洰*], [*绾﹀畾*], [*澶囨敞*],
  ),
  [浼犺緭鍗忚], [TCP Socket], [],
  [榛樿绔彛], [8888], [鍙厤缃紝鍚姩鏃舵墦鍗板疄闄呯鍙,
  [瀛楃缂栫爜], [*UTF-8*锛堝己鍒讹級], [鎵€鏈夊瓧绗︿覆鍧囦负 UTF-8 缂栫爜],
  [琛屽熬], [LF锛坄\n`锛?x0A锛塢, [姣忔潯娑堟伅浠ュ崟涓崲琛岀缁撳熬],
  [瓒呮椂闃堝€糫, [65 绉掞紙60 绉掓€濊€?+ 5 绉掔綉缁滆閲忥級], [鏈嶅姟鍣ㄥ彲閰嶇疆],
  [鏃堕棿鎴冲崟浣峕, [姣], [`System.currentTimeMillis()` 椋庢牸],
  [鏃堕棿鎴虫潈濞佹柟], [*鏈嶅姟鍣?], [瀹㈡埛绔椂闂存埑浠呬緵鍙傝€冿紝瓒呮椂鍒ゅ畾浠ユ湇鍔″櫒涓哄噯],
)

= 鍧愭爣绯荤粺

== 鍧愭爣瀹氫箟

妫嬬洏涓?10 琛?脳 9 鍒楋紝鍧愭爣閲囩敤\"瀛楁瘝鍒?+ 鏁板瓧琛孿"鐨勫瓧绗︿覆琛ㄧず銆?
#v(0.3cm)
#table(
  columns: (auto, auto, auto),
  stroke: none,
  [*缁村害*], [*鑼冨洿*], [*鏂瑰悜*],
  [琛宂, [`0`鈥揱9`锛堝叡 10 琛岋級], [浠庝笂鍒颁笅渚濇涓?9, 8, 鈥? 1, 0],
  [鍒梋, [`a`鈥揱i`锛堝叡 9 鍒楋級], [浠庡乏鍒板彸渚濇涓?a, b, 鈥? i],
)

#v(0.3cm)
#figure(
  table(
    columns: (1.2cm,) + 9 * (1.2cm,),
    stroke: 0.3pt + rgb(203, 213, 225),
    align: center + horizon,
    // 鍒楀ご锛堝姞绮楀簳杈癸級
    table.cell(stroke: (bottom: 1pt + black))[], table.cell(stroke: (bottom: 1pt + black))[a], table.cell(stroke: (bottom: 1pt + black))[b], table.cell(stroke: (bottom: 1pt + black))[c], table.cell(stroke: (bottom: 1pt + black))[d], table.cell(stroke: (bottom: 1pt + black))[e], table.cell(stroke: (bottom: 1pt + black))[f], table.cell(stroke: (bottom: 1pt + black))[g], table.cell(stroke: (bottom: 1pt + black))[h], table.cell(stroke: (bottom: 1pt + black))[i],
    // row 9 榛戞柟搴曠嚎
    table.cell(fill: rgb(241, 245, 249))[9], [杌奭, [棣琞, [璞, [澹玗, [灏嘳, [澹玗, [璞, [棣琞, [杌奭,
    // row 8
    [8], [路], [路], [路], [路], [路], [路], [路], [路], [路],
    // row 7 鐐綅
    table.cell(fill: rgb(241, 245, 249))[7], [路], [鐐甝, [路], [路], [路], [路], [路], [鐐甝, [路],
    // row 6 鍗掍綅
    table.cell(fill: rgb(241, 245, 249))[6], [鍗抅, [路], [鍗抅, [路], [鍗抅, [路], [鍗抅, [路], [鍗抅,
    // row 5 妤氭渤姹夌晫锛堝姞绮楀簳杈癸級
    table.cell(stroke: (bottom: 1pt + black))[5], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路],
    // row 4
    [4], [路], [路], [路], [路], [路], [路], [路], [路], [路],
    // row 3 鍏典綅
    table.cell(fill: rgb(241, 245, 249))[3], [鍏礭, [路], [鍏礭, [路], [鍏礭, [路], [鍏礭, [路], [鍏礭,
    // row 2 鐐綅
    table.cell(fill: rgb(241, 245, 249))[2], [路], [鐐甝, [路], [路], [路], [路], [路], [鐐甝, [路],
    // row 1
    [1], [路], [路], [路], [路], [路], [路], [路], [路], [路],
    // row 0 绾㈡柟搴曠嚎
    table.cell(fill: rgb(241, 245, 249))[0], [杌奭, [棣琞, [鐩竇, [澹玗, [甯, [澹玗, [鐩竇, [棣琞, [杌奭,
  ),
  caption: [妫嬬洏鍧愭爣琛ㄦ牸锛堣 0 = 绾㈡柟搴曠嚎锛岃 9 = 榛戞柟搴曠嚎锛岀矖绾垮垎闅旀娌虫眽鐣岋級],
)

*閲嶈绾﹀畾*锛?- 鍏堟墜锛堢孩鏂癸級濮嬬粓鍦ㄤ笅鏂癸紙琛?0鈥?锛夛紝鍚庢墜锛堥粦鏂癸級鍦ㄤ笂鏂癸紙琛?5鈥?锛?- 瀹㈡埛绔?UI 鍙皢宸辨柟缃簬涓嬫柟鏄剧ず锛屼絾閫昏緫鍧愭爣蹇呴』鍩轰簬涓婂浘鍧愭爣绯?- 鍧愭爣瀛楃涓蹭腑琛屽彿鐩存帴浣跨敤鏄剧ず琛屽彿锛屼笉缈昏浆锛堝绾㈡柟甯?= `"e0"`锛岄粦鏂瑰皢 = `"e9"`锛?
== 鍧愭爣杞崲鍏紡

鍧愭爣瀛楃涓蹭笌鍐呴儴妫嬬洏鏁扮粍绱㈠紩鐨勮浆鎹紙Java 鍙傝€冨疄鐜帮級锛?
```java
// 鍧愭爣瀛楃涓?鈫?鍐呴儴鏁扮粍绱㈠紩
// "b3" 鈫?row=6, col=1锛堟暟缁?row 0 = 妫嬬洏椤惰锛?public static int[] fromCoord(String coord) {
    return new int[]{
        9 - (coord.charAt(1) - '0'),   // 琛岋細鏄剧ず琛屽彿 鈫?鏁扮粍绱㈠紩
        coord.charAt(0) - 'a'           // 鍒楋細'a' 鈫?0
    };
}

// 鍐呴儴鏁扮粍绱㈠紩 鈫?鍧愭爣瀛楃涓?// row=6, col=1 鈫?"b3"
public static String toCoord(int row, int col) {
    return "" + (char)('a' + col) + (9 - row);
}
```

= 妫嬪瓙绫诲瀷缂栫爜

#table(
  columns: (auto, auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, right, left),
  table.header(
    [*缂栫爜*], [*绾㈡柟鍚嶇О*], [*榛戞柟鍚嶇О*], [*鍩哄噯浠峰€?], [*澶囨敞*],
  ),
  [0], [甯匽, [灏哴, [10000], [寮€灞€鍗虫槑锛屽弻鏂瑰悇 1 鏋歖,
  [1], [杞, [杞, [600], [鍙屾柟鍚?2 鏋歖,
  [2], [椹琞, [椹琞, [270], [韫╅┈鑵胯鍒欏悓璞℃],
  [3], [鐐甝, [鐐甝, [285], [缈诲北鍚冨瓙瑙勫垯鍚岃薄妫媇,
  [4], [鍏礭, [鍗抅, [30], [杩囨渤鍚庡彲妯Щ锛屽弻鏂瑰悇 5 鏋歖,
  [5], [浠昡, [澹玗, [120], [鏄庡瓙鍚庡彲绂诲銆佸彲杩囨渤锛屾枩璧颁竴鏍糫,
  [6], [鐩竇, [璞, [120], [鏄庡瓙鍚庡彲杩囨渤锛屽璞＄溂瑙勫垯涓嶅彉],
)

*娉ㄦ剰*锛氬熀鍑嗕环鍊间粎浣滀负 AI 璇勪及鍑芥暟鐨勫弬鑰冿紝缁勯棿浜掓搷浣滀笉渚濊禆姝ゅ€笺€?
= 棰滆壊缂栫爜

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*缂栫爜*], [*闃佃惀*], [*璇存槑*]),
  [0], [绾㈡柟], [鍏堟墜锛屾鐩樹笅鏂筣,
  [1], [榛戞柟], [鍚庢墜锛屾鐩樹笂鏂筣,
)

// ============================================================
// 绗簩绔狅細Move 瀵硅薄
// ============================================================

= Move 瀵硅薄瑙勮寖

== 绫诲畾涔?
```java
public class Move {
    private String  source;          // 璧风偣鍧愭爣锛屽 "a0"
    private String  destination;     // 缁堢偣鍧愭爣锛屽 "a1"
    private Integer type;            // 缈诲嚭鐨勬瀛愮被鍨嬶紙0鈥?锛夛紝闈炵炕瀛愭涓?null
    private long    turnStartTime;   // 鍥炲悎寮€濮嬫椂闂存埑锛堟绉掞級锛屼互鏈嶅姟鍣ㄨ褰曞€间负鍑?    private long    clientTimestamp; // 瀹㈡埛绔彂閫佹椂闂存埑锛堝彲閫夛紝鏈嶅姟鍣ㄥ拷鐣ラ槻浼€狅級
    private long    serverTimestamp; // 鏈嶅姟鍣ㄥ鐞嗘椂闂存埑锛堟湇鍔″櫒鍐欏叆锛?    private boolean isFlipOnly;      // 鏄惁鍘熷湴缈诲瓙鎿嶄綔
}
```

== 瀛楁瑙勫垯

#table(
  columns: (auto, auto),
  [*瑙勫垯*], [*璇存槑*],
  [棣栫炕蹇呭甫 type], [鏆楀瓙棣栨琚Щ鍔ㄦ垨缈诲紑鍚庯紝鏈嶅姟鍣ㄥ繀椤诲皢鐪熷疄 type 濉叆骞跺箍鎾璢,
  [闈為缈?type 涓虹┖], [宸茬煡鏄庡瓙鐨勭Щ鍔紝type 瀛楁涓?null 鎴栫暀绌篯,
  [鏃堕棿鎴虫潈濞乚, [瓒呮椂鍒ゅ畾浠ユ湇鍔″櫒鍥炲悎寮€濮嬫椂闂翠负鍑嗭紝瀹㈡埛绔椂闂存埑浠呬緵鍙傝€僝,
  [flipOnly 绛変环鎬, [`source.equals(destination)` 鈬?`isFlipOnly == true`锛屼袱鑰呯瓑鏁圿,
  [缈诲瓙闅忔満鎬, [鏈嶅姟鍣ㄥ湪妫嬪眬鍒濆鍖栨椂瀹屾垚鏆楀瓙闅忔満鎺掑垪锛岀炕瀛愭椂浠呮彮绀洪缃被鍨嬶紱瀹㈡埛绔彁浜ょ殑 type 琚拷鐣ュ苟瑕嗙洊],
)

== 缈诲瓙闅忔満鎬ф満鍒?
+ 鏈嶅姟鍣ㄥ湪*妫嬪眬鍒濆鍖栨椂*瀹屾垚鏆楀瓙闅忔満鎺掑垪锛堟瘡鏂?15 鏋氭殫瀛愶紝浠庣被鍨嬫睜闅忔満鍒嗛厤鑷冲悇浣嶇疆锛?+ 绫诲瀷姹狅紙姣忔柟锛夛細杞?脳2銆侀┈ 脳2銆佺偖 脳2銆佸崚 脳5銆佸＋ 脳2銆佽薄 脳2
+ 瀹㈡埛绔棤娉曢鐭ユ殫瀛愮湡瀹炵被鍨?+ 瀹㈡埛绔蛋瀛?缈诲瓙鏃讹紝鏈嶅姟鍣ㄤ粎*鎻ず*宸查缃殑鐪熷疄绫诲瀷
+ 缈诲瓙鍚庤妫嬪瓙绫诲瀷姘镐箙鍥哄畾
+ *瀹夊叏鎬?锛氬鎴风鏃犳硶閫氳繃浼€?type 鍊兼敼鍙樼炕瀛愮粨鏋?
// ============================================================
// 绗笁绔狅細缃戠粶閫氫俊鍗忚
// ============================================================

= 缃戠粶閫氫俊鍗忚

== 娑堟伅甯ф牸寮?
姣忔潯娑堟伅涓?涓€琛屾枃鏈?锛堜互 `\n` 缁撳熬锛夛細

```
<msgType>|<payloadByteLength>|<payload>\n
```

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*绫诲瀷*], [*璇存槑*]),
  [`msgType`], [`int`], [娑堟伅绫诲瀷缂栧彿锛堣 搂3.2锛塢,
  [`payloadByteLength`], [`int`], [`payload` 瀛楁鐨?*UTF-8 瀛楄妭鏁?锛堜笉鍚?`|` 鍜?`\n`锛塢,
  [`payload`], [`String`], [瀹為檯璐熻浇鏁版嵁锛屽唴閮ㄥ彲鍚?`|` 鍒嗛殧绗,
)

*绀轰緥*锛?```
2|17|a0|a1||12345678|0
```

== 娑堟伅甯цВ鏋愯鍒?
#enum[
  浠?TCP 娴佷腑璇诲彇鐩村埌 `\n`锛屽緱鍒颁竴琛屾枃鏈?  浠?`|` 鍒嗗壊锛屽彇绗?1 娈典负 `msgType`
  浠?`|` 鍒嗗壊锛屽彇绗?2 娈典负 `payloadByteLength`
  鍓╀綑閮ㄥ垎锛堢 3 娈佃捣锛岀敤 `|` 杩炴帴杩樺師锛変负 `payload`
  *鏍￠獙*锛歚payload` 鐨?UTF-8 瀛楄妭鏁板繀椤荤瓑浜?`payloadByteLength`锛屽惁鍒欎涪寮冨苟杩斿洖 ERROR
]

瑙ｆ瀽鍙傝€冨疄鐜帮紙Java锛夛細

```java
public static int parseMsgType(String line) {
    return Integer.parseInt(line.split("\\|")[0]);
}

public static String parsePayload(String line) {
    String[] parts = line.split("\\|", 3);
    if (parts.length < 3) return "";
    int declaredLen = Integer.parseInt(parts[1]);
    String payload = parts[2];
    if (payload.getBytes("UTF-8").length != declaredLen) {
        return null;  // 甯ф崯鍧?    }
    return payload;
}
```

*璁捐鐞嗙敱*锛氶噰鐢ㄦ崲琛屽垎闅旇€岄潪绾暱搴﹀墠缂€锛屾槸鍥犱负璇剧▼鍦烘櫙涓嬪彲鐢?telnet 鎵嬪姩璋冭瘯锛屼笖 payload 鍐?`|` 鏃犻渶杞箟銆俙payloadByteLength` 鐨勯瑕佷綔鐢ㄦ槸鏍￠獙甯у畬鏁存€э紝娆¤浣滅敤鏄厑璁?payload 鍖呭惈鎹㈣绗︼紙褰撳墠鏈娇鐢ㄦ鐗规€э級銆?
== 娑堟伅绫诲瀷鐩綍

#table(
  columns: (auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, center + horizon, left),
  table.header(
    [*缂栧彿*], [*甯搁噺鍚?], [*鏂瑰悜*], [*璇存槑*],
  ),
  [1], [`MSG_LOGIN`], [C 鈫?S], [瀹㈡埛绔櫥褰?/ 鍔犲叆娓告垙],
  [2], [`MSG_MOVE`], [C 鈫?S], [璧板瓙鎻愪氦涓庡箍鎾‘璁,
  [3], [`MSG_GAME_STATE`], [S 鈫?C], [娓告垙鐘舵€佸彉鏇撮€氱煡],
  [4], [`MSG_ERROR`], [S 鈫?C], [閿欒娑堟伅锛堝惈閿欒鐮侊級],
  [5], [`MSG_QUIT`], [C 鈫?S], [瀹㈡埛绔富鍔ㄩ€€鍑篯,
  [6], [`MSG_GAME_OVER`], [S 鈫?C], [娓告垙缁撴潫閫氱煡锛堝惈缁撴灉涓庡師鍥犵爜锛塢,
  [7], [`MSG_BOARD_STATE`], [S 鈫?C], [瀹屾暣妫嬬洏鍚屾锛堝惈褰撳墠璧板瓙鏂癸級],
  [8], [`MSG_DRAW_REQUEST`], [C 鈫?S], [鎻愬拰 / 鍜屾鍝嶅簲],
  [9], [`MSG_RESIGN`], [C 鈫?S], [璁よ緭],
  [10], [`MSG_CHAT`], [C 鈫?S], [鏂囨湰鑱婂ぉ锛堝彲閫夊疄鐜帮級],
)

*瀹炵幇瑕佹眰*锛?- 1鈥? 涓?蹇呴』瀹炵幇*鐨勬牳蹇冩秷鎭?- 8鈥?0 涓?鍙€夋墿灞?锛屼絾蹇呴』鑳芥敹鍒版湭鐭ユ秷鎭笉宕╂簝锛堥潤榛樺拷鐣ワ級
- 鍚勭粍鍙湪鏈湴鍗忚涓娇鐢?100+ 鐨勭鏈夋秷鎭彿锛岀粍闂撮€氫俊鏃朵笉寰楀彂閫?
== 鍚勬秷鎭缁嗘牸寮?
=== MSG_LOGIN锛?锛夆€?瀹㈡埛绔櫥褰?
*瀹㈡埛绔?鈫?鏈嶅姟鍣?

#table(
  columns: (1.7cm, 1.2cm, 2.4cm, 3.2cm, 3.2cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*color*], [*playerName*], [*gameId*]),
  [`1`], [`<len>`], [`<color>`], [`<playerName>`], [`<gameId>`],
)

#v(0.2cm)
#table(
  columns: (auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*绫诲瀷*], [*蹇呭～*], [*璇存槑*]),
  [`color`], [`int`], [鏄痌, [`0` = 绾㈡柟锛宍1` = 榛戞柟],
  [`playerName`], [`String`], [鏄痌, [鐜╁鏄电О锛堝缓璁笉鍚?`|`锛塢,
  [`gameId`], [`String`], [鍚, [鎸囧畾娓告垙 ID锛涚┖ = 鑷姩鍖归厤],
)

*绀轰緥*锛?
#table(
  columns: (1.7cm, 1.2cm, 2.4cm, 3.2cm, 3.2cm),
  stroke: 0.3pt + rgb(203, 213, 225),
  align: center + horizon,
  table.header([*msgType*], [*len*], [*color*], [*playerName*], [*gameId*]),
  [`1`], [`12`], [`0`], [`寮犱笁`], [锛堢┖锛塢,
  [`1`], [`20`], [`1`], [`鏉庡洓`], [`a1b2c3d4`],
)

*琛屼负绾﹀畾*锛?- `gameId` 涓虹┖鏃讹紝鏈嶅姟鍣ㄥ皢瀹㈡埛绔斁鍏ュ尮閰嶆睜锛屽噾榻愬弻鏂瑰悗鑷姩寮€灞€
- `gameId` 闈炵┖鏃讹紝鏈嶅姟鍣ㄦ煡鎵炬寚瀹氭父鎴忥紱鑻ヤ笉瀛樺湪鍒欒繑鍥?ERROR
- `color` 涓哄鎴风鍋忓ソ锛?鏈嶅姟鍣ㄥ彲瑕嗙洊*锛堝鍏堝埌鍏堝緱锛?
=== MSG_MOVE锛?锛夆€?璧板瓙

*瀹㈡埛绔?鈫?鏈嶅姟鍣紙璧板瓙璇锋眰锛?

#table(
  columns: (1.7cm, 1.2cm, 2cm, 2cm, 1.5cm, 2.4cm, 2cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*source*], [*destination*], [*type*], [*turnStartTime*], [*isFlipOnly*]),
  [`2`], [`<len>`], [`<source>`], [`<destination>`], [`<type>`], [`<turnStartTime>`], [`<isFlipOnly>`],
)

*鏈嶅姟鍣?鈫?鍙屾柟锛堝箍鎾‘璁わ級*

鏍煎紡鍚屼笂锛屾湇鍔″櫒鍦ㄥ箍鎾墠瀹屾垚锛?+ 鍚堟硶鎬ф牎楠岋紙鐫€娉曡鍒?+ 璺緞鏈夋晥鎬э級
+ 灏嗗啗妫€娴嬶紙璧板瓙鍚庡繁鏂逛笉鑳藉浜庤灏嗙姸鎬侊級
+ 鑻ユ殫瀛愰娆＄炕寮€锛岀敤鏈嶅姟鍣ㄩ鐢熸垚绫诲瀷*瑕嗙洊* `type`
+ 鐢ㄦ湇鍔″櫒褰撳墠鏃堕棿*瑕嗙洊* `turnStartTime`

#table(
  columns: (auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*绫诲瀷*], [*蹇呭～*], [*璇存槑*]),
  [`source`], [`String`], [鏄痌, [鍘熷潗鏍囷紝濡?`"b3"`],
  [`destination`], [`String`], [鏄痌, [鐩爣鍧愭爣锛屽 `"b4"`],
  [`type`], [`int` 鎴栫┖], [鏉′欢], [鏆楀瓙缈诲紑鏃剁敱鏈嶅姟鍣ㄥ～鍏?0鈥?锛涘惁鍒欎负绌哄瓧绗︿覆],
  [`turnStartTime`], [`long`], [鍚, [瀹㈡埛绔椂闂存埑锛?鏈嶅姟鍣ㄥ拷鐣ュ苟瑕嗙洊*],
  [`isFlipOnly`], [`int`], [鏄痌, [`1` = 鍘熷湴缈诲瓙锛宍0` = 鏅€氱Щ鍔╙,
)

*绀轰緥*:

#table(
  columns: (1.7cm, 1.2cm, 2cm, 2cm, 1.5cm, 2.4cm, 2cm, 3fr),
  stroke: 0.3pt + rgb(203, 213, 225),
  align: center + horizon,
  table.header([*msgType*], [*len*], [*source*], [*destination*], [*type*], [*turnStartTime*], [*isFlipOnly*], [*璇存槑*]),
  [`2`], [`10`], [`b1`], [`b3`], [ ], [`0`], [`0`], [姝ｅ父璧板瓙锛坱ype 涓虹┖锛宼urnStartTime 鍗犱綅 = 0锛塢,
  [`2`], [`10`], [`a0`], [`a0`], [ ], [`0`], [`1`], [鍘熷湴缈诲瓙 a0 浣嶇疆鐨勬殫瀛怾,
  [`2`], [`20`], [`c4`], [`e4`], [`3`], [`1700000000`], [`0`], [缈诲嚭绫诲瀷 3锛堢偖锛夛紝鏃堕棿鎴充細琚鐩朷,
)

=== MSG_GAME_STATE锛?锛夆€?娓告垙鐘舵€?
*浠呮湇鍔″櫒 鈫?瀹㈡埛绔?锛岄€氳繃棣栦釜瀛楁鍖哄垎瀛愮被鍨嬨€?
*瀛愮被鍨?1锛歀OGIN_ACK锛堢櫥褰曠‘璁わ級*

#table(
  columns: (1.7cm, 1.2cm, 2.8cm, 2.5cm, 2.2cm, 2.2cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*subType*], [*gameId*], [*assignedColor*], [*status*]),
  [`3`], [`<len>`], [`LOGIN_ACK`], [`<gameId>`], [`<assignedColor>`], [`<status>`],
)

#table(
  columns: (auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*璇存槑*]),
  [`LOGIN_ACK`], [鍥哄畾瀛楅潰閲廬,
  [`gameId`], [鍒嗛厤鐨勬父鎴?ID],
  [`assignedColor`], [鏈嶅姟鍣ㄥ垎閰嶇殑闃佃惀锛坄0` = 绾紝`1` = 榛戯級],
  [`status`], [`WAITING` 鎴?`PLAYING`],
)

鏈嶅姟鍣ㄦ敹鍒?LOGIN 鍚庣珛鍗冲洖澶嶃€?
*瀛愮被鍨?2锛欸AME_START锛堝紑灞€骞挎挱锛?

#table(
  columns: (1.7cm, 1.2cm, 3cm, 3cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*subType*], [*firstMoveColor*]),
  [`3`], [`<len>`], [`GAME_START`], [`<firstMoveColor>`],
)

#table(
  columns: (auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*璇存槑*]),
  [`GAME_START`], [鍥哄畾瀛楅潰閲廬,
  [`firstMoveColor`], [`0` = 绾㈡柟鍏堟墜],
)

鍙屾柟鍒伴綈涓旀鐩樺垵濮嬪寲鍚庡箍鎾€傚鎴风鏀跺埌鍚庤繘鍏ュ寮堢姸鎬併€?
*瀛愮被鍨?3锛歍URN_CHANGE锛堝洖鍚堝垏鎹級*

#table(
  columns: (1.7cm, 1.2cm, 3cm, 3cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*subType*], [*currentTurnColor*]),
  [`3`], [`<len>`], [`TURN_CHANGE`], [`<currentTurnColor>`],
)

姣忔鍚堟硶璧板瓙鍚庡箍鎾紝閫氱煡褰撳墠杞埌璋佽蛋銆?
*鐘舵€佸€兼灇涓?锛?
#table(
  columns: (auto, auto),
  stroke: none,
  [`WAITING`], [绛夊緟瀵规墜鍔犲叆],
  [`PLAYING`], [瀵瑰紙杩涜涓璢,
  [`RED_WIN`], [绾㈡柟鑾疯儨],
  [`BLACK_WIN`], [榛戞柟鑾疯儨],
  [`DRAW`], [鍜屾],
  [`TIMEOUT`], [瓒呮椂鍒よ礋],
)

=== MSG_ERROR锛?锛夆€?閿欒娑堟伅

*浠呮湇鍔″櫒 鈫?瀹㈡埛绔?

#table(
  columns: (1.7cm, 1.2cm, 2.4cm, 5cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*errorCode*], [*errorDescription*]),
  [`4`], [`<len>`], [`<errorCode>`], [`<errorDescription>`],
)

#table(
  columns: (auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*璇存槑*]),
  [`errorCode`], [鏁存暟閿欒鐮侊紙瑙佷笅琛級],
  [`errorDescription`], [浜虹被鍙鐨勪腑鏂囬敊璇弿杩癩,
)

*閿欒鐮佽〃*锛?
#table(
  columns: (auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left),
  table.header([*缂栫爜*], [*鍚箟*]),
  [100], [鏈煡閿欒],
  [101], [闈炴硶鐫€娉曪紙妫嬪瓙绉诲姩瑙勫垯杩濆弽锛塢,
  [102], [绉诲姩璺緞琚樆鎸,
  [103], [鍚岃壊鍚冨瓙锛堢洰鏍囦负宸辨柟妫嬪瓙锛塢,
  [104], [韫╅┈鑵縘,
  [105], [濉炶薄鐪糫,
  [106], [璧板瓙鍚庡繁鏂硅灏嗗啗],
  [107], [涓嶆槸浣犵殑鍥炲悎],
  [108], [娓告垙鏈湪杩涜涓璢,
  [109], [鏆楀瓙宸茬炕寮€锛屼笉鍙噸澶嶇炕瀛怾,
  [110], [婧愪綅缃棤妫嬪瓙],
  [111], [娑堟伅鏍煎紡閿欒锛堝抚瑙ｆ瀽澶辫触锛塢,
  [112], [閲嶅鐧诲綍],
  [200], [娓告垙鎴块棿涓嶅瓨鍦╙,
  [201], [娓告垙鎴块棿宸叉弧],
  [202], [鎵€閫夐鑹插凡琚崰鐢╙,
)

=== MSG_QUIT锛?锛夆€?閫€鍑?
*瀹㈡埛绔?鈫?鏈嶅姟鍣?

#table(
  columns: (1.7cm, 1.2cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*]),
  [`5`], [`<len>`],
)

鏈嶅姟鍣ㄦ敹鍒板悗锛氶€€鍑烘柟鍒よ礋 鈫?骞挎挱 GAME_OVER 鈫?鍏抽棴杩炴帴銆?
=== MSG_GAME_OVER锛?锛夆€?娓告垙缁撴潫

*浠呮湇鍔″櫒 鈫?瀹㈡埛绔?

#table(
  columns: (1.7cm, 1.2cm, 2cm, 2.4cm, 4cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*winner*], [*reasonCode*], [*reasonDescription*]),
  [`6`], [`<len>`], [`<winner>`], [`<reasonCode>`], [`<reasonDescription>`],
)

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*瀛楁*], [*绫诲瀷*], [*璇存槑*]),
  [`winner`], [`int`], [`0` = 绾㈣儨锛宍1` = 榛戣儨锛宍-1` = 鍜屾],
  [`reasonCode`], [`int`], [缁撴潫鍘熷洜鐮侊紙瑙佷笅琛級],
  [`reasonDescription`], [`String`], [浜虹被鍙鐨勫師鍥犳弿杩癩,
)

*绀轰緥*锛?
#table(
  columns: (1.7cm, 1.2cm, 2cm, 2.4cm, 4cm),
  stroke: 0.3pt + rgb(203, 213, 225),
  align: center + horizon,
  table.header([*msgType*], [*len*], [*winner*], [*reasonCode*], [*reasonDescription*]),
  [`6`], [`27`], [`0`], [`0`], [绾㈡柟灏嗘榛戞柟鑾疯儨],
  [`6`], [`14`], [`-1`], [`6`], [40鍥炲悎鏃犲悆瀛愶紝鍜屾],
  [`6`], [`16`], [`1`], [`3`], [榛戞柟璁よ緭锛岀孩鏂硅儨],
)

*娓告垙缁撴潫鍘熷洜鐮?锛?
#table(
  columns: (auto, auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, center + horizon, left),
  table.header([*缂栫爜*], [*鍘熷洜*], [*鑳滄柟*], [*璇存槑*]),
  [0], [灏嗘 (Checkmate)], [瀵规柟], [琚皢鍐涗笖鏃犱换浣曞悎娉曠潃娉曞彲瑙,
  [1], [鍥版瘷 (Stalemate)], [瀵规柟], [鏈灏嗗啗浣嗘棤浠讳綍鍚堟硶鐫€娉曞彲璧癩,
  [2], [瓒呮椂 (Timeout)], [瀵规柟], [鍗曟瓒呰繃 65 绉掓湭璧板瓙],
  [3], [璁よ緭 (Resign)], [瀵规柟], [涓诲姩璁よ緭],
  [4], [鏂嚎 (Disconnect)], [瀵规柟], [瀵规墜鏂紑 TCP 杩炴帴],
  [5], [鍚冨皢鑾疯儨], [鍚冨皢鏂筣, [瀵规柟鏈簲灏嗭紝宸辨柟鐩存帴鍚冩帀灏?甯咃紙浣滀笟鏄庣‘鍏佽锛塢,
  [6], [40 鍥炲悎鏃犲悆瀛怾, [鏃狅紙鍜屾锛塢, [杩炵画 80 涓崐姝ユ棤鍚冨瓙],
  [7], [闀垮皢/闀挎崏鍒よ礋], [瀵规柟], [鍚屽眬闈㈤噸澶?鈮? 娆★紝涓旈潪鍏靛崚闀挎崏],
  [8], [鍏靛崚闀挎崏鍜宂, [鏃狅紙鍜屾锛塢, [鍏靛崚闀挎崏瀵艰嚧灞€闈㈤噸澶?鈮? 娆,
  [9], [鍗忚鍜屾], [鏃狅紙鍜屾锛塢, [鍙屾柟鍚屾剰鍜屾],
)

=== MSG_BOARD_STATE锛?锛夆€?妫嬬洏鍚屾

*浠呮湇鍔″櫒 鈫?瀹㈡埛绔?

#table(
  columns: (1.7cm, 1.2cm, 2.4cm, 6cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*currentTurn*], [*rows*]),
  [`7`], [`<len>`], [`<currentTurn>`], [`<row0>|<row1>|...|<row9>`],
)

*妫嬬洏琛岀紪鐮?锛?- 鍏?10 琛岋紝浠?`|` 鍒嗛殧锛堜笌 `currentTurn` 缁勬垚 11 娈碉紝浠?`|` 鍒囧垎 payload锛?- *row0* = 妫嬬洏鏈€椤惰锛堟樉绀鸿鍙?9锛岄粦鏂瑰簳绾匡級
- *row9* = 妫嬬洏鏈€搴曡锛堟樉绀鸿鍙?0锛岀孩鏂瑰簳绾匡級
- 姣忚 9 涓?cell锛屼互 `,` 鍒嗛殧

*Cell 缂栫爜瑙勫垯*锛?
#table(
  columns: (auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left),
  table.header([*Cell 鍊?], [*鍚箟*]),
  [`.`], [绌轰綅锛堣鏍兼棤妫嬪瓙锛塢,
  [`0` + type], [绾㈡柟宸茬炕寮€妫嬪瓙锛宼ype 涓?0鈥?锛堜緥锛歚01` = 绾㈣溅锛塢,
  [`1` + type], [榛戞柟宸茬炕寮€妫嬪瓙锛宼ype 涓?0鈥?锛堜緥锛歚13` = 榛戠偖锛塢,
  [`0?`], [绾㈡柟鏆楀瓙锛堟湭缈诲紑锛塢,
  [`1?`], [榛戞柟鏆楀瓙锛堟湭缈诲紑锛塢,
)

*瀹屾暣绀轰緥*锛堝紑灞€妫嬬洏锛宍currentTurn=0` 绾㈡柟琛屾銆傚抚鏍煎紡锛歚7|len|0|<row0>|...|<row9>`锛夛細

#v(0.3cm)
#figure(
  table(
    columns: (1.1cm,) + 9 * (1.3cm,),
    stroke: 0.3pt + rgb(203, 213, 225),
    align: center + horizon,
    // 鍒楀ご锛堝姞绮楀簳杈癸級
    table.cell(stroke: (bottom: 1pt + black))[], table.cell(stroke: (bottom: 1pt + black))[a], table.cell(stroke: (bottom: 1pt + black))[b], table.cell(stroke: (bottom: 1pt + black))[c], table.cell(stroke: (bottom: 1pt + black))[d], table.cell(stroke: (bottom: 1pt + black))[e], table.cell(stroke: (bottom: 1pt + black))[f], table.cell(stroke: (bottom: 1pt + black))[g], table.cell(stroke: (bottom: 1pt + black))[h], table.cell(stroke: (bottom: 1pt + black))[i],
    // row 9 榛戞柟搴曠嚎
    table.cell(fill: rgb(241, 245, 249))[9], [`1?`], [`1?`], [`1?`], [`1?`], [`10`], [`1?`], [`1?`], [`1?`], [`1?`],
    // row 8
    [8], [路], [路], [路], [路], [路], [路], [路], [路], [路],
    // row 7 榛戠偖浣?    table.cell(fill: rgb(241, 245, 249))[7], [路], [`1?`], [路], [路], [路], [路], [路], [`1?`], [路],
    // row 6 榛戝崚浣?    table.cell(fill: rgb(241, 245, 249))[6], [`1?`], [路], [`1?`], [路], [`1?`], [路], [`1?`], [路], [`1?`],
    // row 5 妤氭渤锛堝姞绮楀簳杈癸級
    table.cell(stroke: (bottom: 1pt + black))[5], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路], table.cell(stroke: (bottom: 1pt + black))[路],
    // row 4 姹夌晫
    [4], [路], [路], [路], [路], [路], [路], [路], [路], [路],
    // row 3 绾㈠叺浣?    table.cell(fill: rgb(241, 245, 249))[3], [`0?`], [路], [`0?`], [路], [`0?`], [路], [`0?`], [路], [`0?`],
    // row 2 绾㈢偖浣?    table.cell(fill: rgb(241, 245, 249))[2], [路], [`0?`], [路], [路], [路], [路], [路], [`0?`], [路],
    // row 1
    [1], [路], [路], [路], [路], [路], [路], [路], [路], [路],
    // row 0 绾㈡柟搴曠嚎
    table.cell(fill: rgb(241, 245, 249))[0], [`0?`], [`0?`], [`0?`], [`0?`], [`00`], [`0?`], [`0?`], [`0?`], [`0?`],
  ),
  caption: [BOARD\_STATE 寮€灞€甯э紙`len=211`锛宍currentTurn=0`锛夈€俙e9`=`10`锛堥粦灏嗭級銆乣e0`=`00`锛堢孩甯咃級寮€灞€鍗虫槑锛沗0?`=绾㈡柟鏆楀瓙锛宍1?`=榛戞柟鏆楀瓙锛宍路`=绌轰綅],
)

=== MSG_DRAW_REQUEST锛?锛夆€?鎻愬拰

*鍙屽悜*锛圕 鈫?S 鎴?S 鈫?C锛?
*瀹㈡埛绔彁鍜?锛?
#table(
  columns: (1.7cm, 1.2cm, 2.4cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*action*]),
  [`8`], [`<len>`], [`OFFER`],
)

*瀵规柟鍚屾剰*锛?
#table(
  columns: (1.7cm, 1.2cm, 2.4cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*action*]),
  [`8`], [`<len>`], [`ACCEPT`],
)

*瀵规柟鎷掔粷*锛?
#table(
  columns: (1.7cm, 1.2cm, 2.4cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*action*]),
  [`8`], [`<len>`], [`DECLINE`],
)

鏈嶅姟鍣ㄦ敹鍒?ACCEPT 鍚庡箍鎾?GAME_OVER锛堝師鍥犵爜 = 9锛屽崗璁拰妫嬶級銆傛敹鍒?DECLINE 鍚庝粎杞彂锛屾灞€缁х画銆?
=== MSG_RESIGN锛?锛夆€?璁よ緭

*瀹㈡埛绔杈?锛?
#table(
  columns: (1.7cm, 1.2cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*]),
  [`9`], [`<len>`],
)

*鏈嶅姟鍣ㄩ€氱煡*锛?
#table(
  columns: (1.7cm, 1.2cm, 2cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*color*]),
  [`9`], [`<len>`], [`<color>`],
)

鏈嶅姟鍣ㄦ敹鍒拌杈撳悗绔嬪嵆骞挎挱 GAME_OVER锛堝師鍥犵爜 = 3锛夈€?
=== MSG_CHAT锛?0锛夆€?鑱婂ぉ

*鍙屽悜*锛堝彲閫夊疄鐜帮級

#table(
  columns: (1.7cm, 1.2cm, 2.4cm, 3cm, 3.5cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: center + horizon,
  table.header([*msgType*], [*len*], [*playerColor*], [*playerName*], [*message*]),
  [`10`], [`<len>`], [`<playerColor>`], [`<playerName>`], [`<message>`],
)

涓嶅奖鍝嶆灞€鐘舵€侊紝鏈嶅姟鍣ㄤ粎璐熻矗杞彂銆?
// ============================================================
// 绗洓绔狅細閫氫俊娴佺▼
// ============================================================

= 鍏稿瀷閫氫俊娴佺▼

#v(0.2cm)
#text(size: hint-size, fill: gray)[鏃跺簭鍥剧珫绾?`|` 琛ㄧず鍚勬柟杩炴帴锛沗-->` / `<--` 琛ㄧず娑堟伅鏂瑰悜銆傚浘鍐呬负绛夊鑻辨枃鏍囩浠ヤ繚璇佸榻愩€俔

== 姝ｅ父瀵瑰紙鏃跺簭

#seq-diagram(
  "
      Client A (Red)              Server              Client B (Black)
            |                         |                         |
            |------ LOGIN(c=0) ------>|                         |
            |<----- LOGIN_ACK ---------|                         |
            |      (WAITING)          |                         |
            |                         |<------ LOGIN(c=1) -------|
            |                         |----- LOGIN_ACK -------->|
            |                         |       (PLAYING)         |
            |<---- GAME_START ---------|<---- GAME_START -------->|
            |<---- BOARD_STATE --------|<---- BOARD_STATE ------>|
            |                         |                         |
            |------ MOVE(b1-b3) ------>|                         |
            |                         |                         |
            |    [server: validate + fill type on reveal]       |
            |<---- MOVE (broadcast) ---|<---- MOVE (broadcast) ->|
            |<---- BOARD_STATE --------|<---- BOARD_STATE ------>|
            |<---- TURN_CHANGE ---------|<---- TURN_CHANGE ------>|
            |                         |                         |
            |                         |<------ MOVE(c7-c5) -------|
            |                         |        [ok]             |
            |<---- MOVE (broadcast) ---|<---- MOVE (broadcast) ->|
            |<---- BOARD_STATE --------|<---- BOARD_STATE ------>|
            |                         |                         |
            |       ... repeat until game over ...              |
            |<---- GAME_OVER ----------|<---- GAME_OVER -------->|
  ",
  [浠庣櫥褰曞埌缁堝眬鐨勬秷鎭簭鍒楋紙瀹㈡埛绔?A 绾㈡柟銆佹湇鍔″櫒銆佸鎴风 B 榛戞柟锛塢,
  roles: [#grid(columns: (1fr, 1fr, 1fr), align: center,
    [瀹㈡埛绔?A锛堢孩鏂癸級], [鏈嶅姟鍣╙, [瀹㈡埛绔?B锛堥粦鏂癸級],
  )],
)

== 闈炴硶鐫€娉曡鎷掔粷

#seq-diagram(
  "
      Client A                    Server
            |                         |
            |------ MOVE(b1-b5) ------>|
            |                         |  [reject]
            |<-- ERROR(102, blocked) --|
            |   (state unchanged)     |
  ",
  [闈炴硶鐫€娉曡鎷掔粷锛氭灞€鐘舵€佷笉鍙橈紝浠嶈疆鍒板鎴风 A],
  roles: [#grid(columns: (1fr, 1fr), align: center, [瀹㈡埛绔?A], [鏈嶅姟鍣╙)],
)

== 瓒呮椂鍒よ礋

#seq-diagram(
  "
      Client A (Red)              Server
            |                         |
            |   (no MOVE within 65s)  |
            |                         |
            |                         |  [timer fires]
            |<-- GAME_OVER(1,2,timeout)|
  ",
  [瓒呮椂鍒よ礋锛氱孩鏂?65 绉掑唴鏈蛋瀛愶紝鏈嶅姟鍣ㄥ畾鏃跺櫒瑙﹀彂鍚庡箍鎾?GAME\_OVER],
  roles: [#grid(columns: (1fr, 1fr), align: center, [瀹㈡埛绔?A锛堢孩鏂癸級], [鏈嶅姟鍣╙)],
)

== 鎻愬拰娴佺▼

#seq-diagram(
  "
      Client A                    Server                    Client B
            |                         |                         |
            |--- DRAW_REQUEST OFFER --->|                         |
            |                         |--- DRAW_REQUEST OFFER ->|
            |                         |                         |
            |                         |<- DRAW_REQUEST ACCEPT --|
            |                         |                         |
            |<------ GAME_OVER --------|<------ GAME_OVER ------>|
            |    (-1,9, agreed draw)    |    (-1,9, agreed draw)   |
  ",
  [鎻愬拰娴佺▼锛氬弻鏂?OFFER 鍚庝竴鏂?ACCEPT锛屾湇鍔″櫒骞挎挱鍜屾 GAME\_OVER],
  roles: [#grid(columns: (1fr, 1fr, 1fr), align: center,
    [瀹㈡埛绔?A], [鏈嶅姟鍣╙, [瀹㈡埛绔?B],
  )],
)

== 璁よ緭娴佺▼

#seq-diagram(
  "
      Client A                    Server                    Client B
            |                         |                         |
            |-------- RESIGN --------->|                         |
            |                         |-------- RESIGN -------->|
            |<------ GAME_OVER --------|<------ GAME_OVER ------>|
  ",
  [璁よ緭娴佺▼锛氬鎴风 A 璁よ緭锛屾湇鍔″櫒閫氱煡 B 骞跺箍鎾?GAME\_OVER],
  roles: [#grid(columns: (1fr, 1fr, 1fr), align: center,
    [瀹㈡埛绔?A], [鏈嶅姟鍣╙, [瀹㈡埛绔?B],
  )],
)

// ============================================================
// 绗簲绔狅細鏆楀瓙璧版硶涓庤櫄鎷熺被鍨?// ============================================================

= 鏆楀瓙璧版硶涓庤櫄鎷熺被鍨?
== 铏氭嫙绫诲瀷鏈哄埗

鏆楀瓙鏈炕寮€鏃讹紝鍏?绉诲姩瑙勫垯*鐢辨墍鍦ㄤ綅缃殑\"铏氭嫙绫诲瀷\"鍐冲畾锛岃€岄潪瀹為檯绫诲瀷銆?
铏氭嫙绫诲瀷 = 璇ヤ綅缃寜涓浗璞℃鍒濆甯冨眬鏈簲鏀剧疆鐨勬瀛愮被鍨嬨€?
#v(0.3cm)
#figure(
  table(
    columns: (1.2cm,) + 9 * (1.2cm,),
    stroke: 0.3pt + rgb(203, 213, 225),
    align: center + horizon,
    // 鍒楀ご锛堝姞绮楀簳杈癸級
    table.cell(stroke: (bottom: 1pt + black))[], table.cell(stroke: (bottom: 1pt + black))[a], table.cell(stroke: (bottom: 1pt + black))[b], table.cell(stroke: (bottom: 1pt + black))[c], table.cell(stroke: (bottom: 1pt + black))[d], table.cell(stroke: (bottom: 1pt + black))[e], table.cell(stroke: (bottom: 1pt + black))[f], table.cell(stroke: (bottom: 1pt + black))[g], table.cell(stroke: (bottom: 1pt + black))[h], table.cell(stroke: (bottom: 1pt + black))[i],
    // row 9 榛戞柟鍖哄煙
    table.cell(fill: rgb(248, 250, 252))[9], [杌奭, [棣琞, [璞, [澹玗, table.cell(fill: rgb(254, 242, 242))[灏嘳, [澹玗, [璞, [棣琞, [杌奭,
    // row 8
    [8], [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡,
    // row 7
    table.cell(fill: rgb(248, 250, 252))[7], [鈥昡, [鐐甝, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鐐甝, [鈥昡,
    // row 6
    table.cell(fill: rgb(248, 250, 252))[6], [鍗抅, [鈥昡, [鍗抅, [鈥昡, [鍗抅, [鈥昡, [鍗抅, [鈥昡, [鍗抅,
    // row 5 妤氭渤姹夌晫锛堝姞绮楀簳杈癸級
    table.cell(stroke: (bottom: 1pt + black))[5], table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡, table.cell(stroke: (bottom: 1pt + black))[鈥昡,
    // row 4
    [4], [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡,
    // row 3
    table.cell(fill: rgb(248, 250, 252))[3], [鍏礭, [鈥昡, [鍏礭, [鈥昡, [鍏礭, [鈥昡, [鍏礭, [鈥昡, [鍏礭,
    // row 2
    table.cell(fill: rgb(248, 250, 252))[2], [鈥昡, [鐐甝, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鐐甝, [鈥昡,
    // row 1
    [1], [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡, [鈥昡,
    // row 0 绾㈡柟鍖哄煙
    table.cell(fill: rgb(248, 250, 252))[0], [杌奭, [棣琞, [鐩竇, [澹玗, table.cell(fill: rgb(254, 242, 242))[甯, [澹玗, [鐩竇, [棣琞, [杌奭,
  ),
  caption: [妫嬬洏鍚勪綅缃搴旂殑铏氭嫙绫诲瀷锛坄鈥昤 琛ㄧず鏃犳瀛愶紝绾㈠簳鏍间负寮€灞€鍗虫槑鐨勫皢/甯咃級],
)

*璇存槑*锛?- 灏嗗竻浣嶇疆锛?e 鍜?9e锛変负鏄庡瓙锛屽疄闄呯被鍨?= 铏氭嫙绫诲瀷 = 灏?甯?- 鏆楀瓙涓ユ牸鎸夎櫄鎷熺被鍨嬬殑涓浗璞℃鍘熷瑙勫垯绉诲姩锛堝惈浣嶇疆闄愬埗锛夛紝*涓嶄韩鏈夋槑瀛愬己鍖?
- 渚嬪锛氫綅浜庡＋浣嶇殑鏆楀瓙鍙兘鏂滆蛋涓€鏍间簬涔濆鍐咃紱缈诲紑鍚庤嫢涓烘槑瀛愬＋锛屾柟鍙瀹繃娌?
== 鏄庡瓙寮哄寲瑙勫垯

鏆楀瓙缈诲紑涓烘槑瀛愬悗锛屽＋鍜岃薄鑾峰緱寮哄寲锛?
#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*妫嬪瓙*], [*鏆楀瓙鐘舵€?], [*鏄庡瓙鐘舵€?]),
  [澹?/ 浠昡, [鏂滆蛋涓€鏍硷紝闄愪簬涔濆鍐匽, [鏂滆蛋涓€鏍硷紝*鍙瀹€佸彲杩囨渤*],
  [璞?/ 鐩竇, [鐢板瓧璧版硶锛屼笉鍙繃娌筹紝濉炶薄鐪兼湁鏁圿, [鐢板瓧璧版硶锛?鍙繃娌?锛屽璞＄溂涓嶅彉],
)

鍏朵粬妫嬪瓙鐨勮蛋娉曡鍒欎笌涓浗璞℃瀹屽叏涓€鑷淬€?
// ============================================================
// 绗叚绔狅細鑳滆礋涓庡拰妫?// ============================================================

= 鑳滆礋涓庡拰妫嬪垽瀹?
== 鑳滆礋鏉′欢

#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left),
  table.header([*鏉′欢*], [*缁撴灉*], [*璇存槑*]),
  [灏嗘], [瀵规柟鑳淽, [琚皢鍐涗笖鏃犱换浣曞悎娉曠潃娉曞彲瑙ｉ櫎灏嗗啗],
  [鍥版瘷], [瀵规柟鑳淽, [鏈灏嗗啗浣嗘病鏈変换浣曞悎娉曠潃娉曞彲璧癩,
  [瓒呮椂], [瀵规柟鑳淽, [鍗曟瓒呰繃 65 绉掓湭璧板瓙],
  [璁よ緭], [瀵规柟鑳淽, [涓诲姩鍙戦€?RESIGN 娑堟伅],
  [鏂嚎], [瀵规柟鑳淽, [TCP 杩炴帴寮傚父鏂紑],
  [涓嶅簲灏哴, [*瀵规柟*涓嬩竴姝ュ悆灏嗗悗鑳淽, [绯荤粺涓嶈嚜鍔ㄥ垽璐燂紝鐢卞鏂归€氳繃姝ｅ父鍚冨瓙鎿嶄綔瀹炵幇],
)

== 鍜屾鏉′欢

#table(
  columns: (auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left),
  table.header([*鏉′欢*], [*璇存槑*]),
  [40 鍥炲悎鏃犲悆瀛怾, [鍙屾柟杩炵画 80 涓崐姝ユ棤浠讳綍鍚冨瓙鍙戠敓锛堢炕瀛愪笉绠楀悆瀛愶級],
  [鍗忚鍜屾], [涓€鏂规彁鍜岋紝瀵规柟鍚屾剰],
  [鍏靛崚闀挎崏], [鍏?鍗掕繛缁暱鎹夊鑷村悓灞€闈㈤噸澶?鈮? 娆★紝鍒ゅ拰鑰岄潪鍒よ礋],
)

== 闀垮皢 / 闀挎崏鍒ゅ畾

#table(
  columns: (auto, auto),
  [*绫诲瀷*], [*鍒ゅ畾*],
  [闀垮皢], [鍚屼竴鏂硅繛缁皢鍐?鈮? 娆★紝涓斿眬闈㈤噸澶嶏紙鍝堝笇鐩稿悓锛夛紝*鍒よ礋*锛堝鏂硅儨锛塢,
  [闀挎崏锛堥潪鍏靛崚锛塢, [鍚屼竴鏂硅繛缁崏瀛?鈮? 娆★紝涓斿眬闈㈤噸澶嶏紝*鍒よ礋*锛堝鏂硅儨锛塢,
  [鍏靛崚闀挎崏], [鍏?鍗掕繛缁崏瀛愬鑷村眬闈㈤噸澶?鈮? 娆★紝*鍒ゅ拰*],
)

*瀹炵幇缁嗚妭*锛?- 灞€闈㈤噸澶嶅垽瀹氫娇鐢ㄥ眬闈㈠搱甯岋紙鍚瘡涓綅缃殑鏄庡瓙绫诲瀷/棰滆壊/鏆楀瓙鏍囪 + 褰撳墠璧板瓙鏂癸級
- 鏆楀瓙鐨勮櫄鎷熺被鍨嬩笉鍙備笌鍝堝笇锛堝悓浣嶇疆铏氭嫙绫诲瀷鍥哄畾锛?- 璁℃暟鍣ㄥ湪姣忔鍚冨瓙鍚庨噸缃?
== 瓒呮椂鍒ゅ畾鍏紡

```
if (serverCurrentTime 鈭?serverTurnStartTime > 60000 + 5000) {
    // 褰撳墠璧板瓙鏂硅秴鏃跺垽璐?}
```

- `60000` ms = 60 绉掞紙姣忔鏃堕檺锛?- `5000` ms = 5 绉掞紙缃戠粶寤惰繜瑁曢噺锛?- 鎬婚槇鍊?= *65000 姣*

*娉ㄦ剰*锛?- 鍥炲悎寮€濮嬫椂闂?`serverTurnStartTime` 鐢辨湇鍔″櫒鍦ㄦ瘡娆″垏鎹㈣蛋瀛愭柟鏃惰褰?- 瀹㈡埛绔?Move 涓殑 `turnStartTime` *涓嶈淇′换*锛屼粎鐢ㄤ簬鏃ュ織
- 鑻ュ鎴风鍙戦€?MOVE 鏃跺凡瓒呮椂锛屾湇鍔″櫒鎷掔粷璧板瓙骞跺垽褰撳墠鏂硅礋

// ============================================================
// 绗竷绔狅細妫嬭氨璁板綍
// ============================================================

= 妫嬭氨璁板綍鏍煎紡

== 姣忔妫嬬殑璁版硶

```
<姝ユ暟>. <婧愬潗鏍?-<鐩爣鍧愭爣>[(<缈诲嚭妫嬪瓙绫诲瀷>)] [缈籡
```

*绀轰緥*锛?```
1.  b1-c3(2)      绗?1 姝ワ紝璧板瓙 b1鈫抍3锛岀炕鍑虹被鍨?2锛堥┈锛?2.  h7-g7          绗?2 姝ワ紝h7鈫抔7锛堟槑瀛愮Щ鍔紝鏃犵炕瀛愶級
3.  a0-a0(1)[缈籡   绗?3 姝ワ紝鍘熷湴缈诲紑 a0锛岀炕鍑虹被鍨?1锛堣溅锛?```

== 妫嬪瓙绫诲瀷鐨勬璋卞悕绉?
#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*绫诲瀷缂栫爜*], [*绾㈡柟鍚嶇О*], [*榛戞柟鍚嶇О*]),
  [0], [甯匽, [灏哴,
  [1], [杞, [杞,
  [2], [椹琞, [椹琞,
  [3], [鐐甝, [鐐甝,
  [4], [鍏礭, [鍗抅,
  [5], [浠昡, [澹玗,
  [6], [鐩竇, [璞,
)

== 鏈嶅姟鍣ㄦ璋卞瓨鍌?
鏈嶅姟鍣ㄥ繀椤绘寜鏃堕棿椤哄簭淇濆瓨姣忓眬妫嬬殑鎵€鏈夎蛋娉曘€傚缓璁牸寮忥細
- *鍐呭瓨*锛歚List<Move>` 鎴?`List<String>`锛屽疄鏃惰拷璁?- *鎸佷箙鍖?锛堝彲閫夛級锛氬灞€缁撴潫鏃跺啓鍏ユ枃浠讹紝鏂囦欢鍚嶄负 `"<gameId>_<鏃堕棿鎴?.pgn"` 鎴?`".txt"`
- 妫嬭氨鍐呭搴斿寘鍚灞€鍏冧俊鎭紙鍙屾柟鏄电О銆佽捣濮嬫椂闂淬€佺粓灞€鍘熷洜锛?
// ============================================================
// 绗叓绔狅細澶氱洏瀵瑰紙
// ============================================================

= 澶氱洏瀵瑰紙

鏀寔涓€鍙版湇鍔″櫒鍚屾椂杩涜澶氬眬瀵瑰紙銆?
== 瀹炵幇瑕佺偣

#enum[
 姣忓眬妫嬪垎閰嶅敮涓€ `gameId`锛堝缓璁?UUID 鍓?8 浣嶏級
 瀹㈡埛绔?LOGIN 鏃堕€氳繃 `gameId` 瀛楁鎸囧畾鍔犲叆鐩爣瀵瑰眬
 `gameId` 涓虹┖鏃舵湇鍔″櫒鑷姩鍖归厤
 鏈嶅姟鍣ㄥ唴閮ㄤ互 `Map<String, Game>` 绠＄悊澶氬眬锛堢嚎绋嬪畨鍏ㄩ泦鍚堬級
]

== 璺ㄧ粍鍏煎

- 鑻ュ鏂规湇鍔″櫒*鏀寔*澶氱洏锛氭甯告寚瀹?`gameId` 鍔犲叆
- 鑻ュ鏂规湇鍔″櫒*涓嶆敮鎸?澶氱洏锛堜粎鍗曞眬锛夛細鎷掔粷 `gameId` 闈炵┖鐨?LOGIN锛岃繑鍥?ERROR 200锛堟父鎴忔埧闂翠笉瀛樺湪锛?- 瀹㈡埛绔簲瀵规閿欒鍋氬弸濂芥彁绀?
// ============================================================
// 绗節绔狅細缁勯棿鑱旇皟娓呭崟
// ============================================================

= 缁勯棿鑱旇皟妫€鏌ユ竻鍗?
浠ヤ笅鏉＄洰涓虹粍闂磋仈璋冨噯鍏ユ爣鍑嗭紝鎵€鏈夋帴鍏ユ柟锛堝惈鏈粍锛夊湪鑱旇皟鍓嶅潎搴旈€愰」鑷閫氳繃锛?
#enum(
  [娑堟伅甯ф牸寮忔纭細`msgType|payloadLen|payload\n`锛孶TF-8 缂栫爜],
  [鏀跺埌鏈煡 `msgType`锛?/9/10/100+锛変笉浼氬穿婧冿紝闈欓粯蹇界暐],
  [鍧愭爣瑙ｆ瀽姝ｇ‘锛歚"a0"` = 宸︿笅瑙掞紙绾㈡柟搴曠嚎宸﹁溅浣嶏級锛宍"i9"` = 鍙充笂瑙掞紙榛戞柟搴曠嚎鍙宠溅浣嶏級],
  [缈诲瓙鎿嶄綔 `source == destination` 琚纭鐞嗕负鍘熷湴缈诲瓙],
  [鏈嶅姟鍣ㄥ箍鎾殑 MOVE 涓?`type` 涓烘湇鍔″櫒鐢熸垚鍊硷紝闈炲鎴风鎻愪氦鍊糫,
  [BOARD_STATE 鐨?`row0` = 妫嬬洏鏈€椤惰锛堥粦鏂癸級锛宍row9` = 鏈€搴曡锛堢孩鏂癸級],
  [BOARD_STATE 鑳藉畬鏁磋В鏋愬苟閲嶅缓妫嬬洏瀵硅薄],
  [GAME_OVER 鐨?`winner == -1` 鏃舵纭樉绀篭"鍜屾\"],
  [GAME_OVER 姝ｇ‘鎼哄甫 `reasonCode` 鍜?`reasonDescription`],
  [GAME_STATE 鐨勫瓙绫诲瀷锛圠OGIN_ACK / GAME_START / TURN_CHANGE锛夊潎姝ｇ‘瑙ｆ瀽],
  [瓒呮椂榛樿 65s锛屼娇鐢ㄦ湇鍔″櫒鏃堕棿鎴冲垽瀹歖,
  [绔彛鍙峰湪鍚姩鏃舵槑纭墦鍗板埌鎺у埗鍙癩,
  [閿欒娑堟伅锛圡SG_ERROR锛夎兘瑙ｆ瀽閿欒鐮佸苟鍙嬪ソ灞曠ず],
  [瀵规柟鏂嚎鏃舵湰鏂规敹鍒?GAME_OVER锛堝師鍥犵爜 = 4锛夎€岄潪鏃犻檺绛夊緟],
)

// ============================================================
// 绗崄绔狅細寰呯‘璁ら棶棰橈紙瀹屽杽棰樼洰瀹氫箟锛?// ============================================================

= 寰呰€佸笀纭鐨勫紑鏀鹃棶棰?
浣滀笟棰樼洰璇存槑銆屾湰棰樼洰瀹氫箟搴旇鏄笉瀹屾暣鐨勩€嶃€傛湰鑺備负鏈粍鍦ㄩ渶姹傚垎鏋愩€佽鍒欑爺璇汇€佸崗璁璁′笌鑱旇皟涓?#strong[涓诲姩鎻愬嚭骞剁粰鍑烘殏瀹氭柟妗圿 鐨勪簤璁偣锛屼緵鑰佸笀瑁佸畾鍚庡啓鍏ュ叏浣撶粺涓€鐨勫叕鍏辫鑼冦€?
#v(0.2cm)
*璇存槑*锛氬甫銆屽緟纭銆嶈€呴渶鏁欏笀瑁佸畾锛涘甫銆屾湰缁勬柟妗堛€嶈€呬负鏈崗璁?v2.0 宸查噰鐢ㄣ€佸缓璁€佸笀璁ゅ彲鍚庡叏缁勯伒鐓х殑榛樿瀹炵幇銆?
== 瑙勫垯涓庤儨璐熷垽瀹?
#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q1], [
    鍚冩殫瀛愭椂锛岃鍚冩柟鏄惁搴旂煡閬撹鍚冨瓙鐨?鐪熷疄*绫诲瀷锛熻鍒欏啓銆岃鍚冩柟涓嶇煡閬撱€嶏紝浣?MOVE 骞挎挱鍚?type銆?  ], [
    鏂规 A锛氬弻鏂瑰箍鎾浉鍚岋紙瀹炵幇绠€鍗曪級锛沑
    鏂规 B锛氬琚悆鏂瑰彂閫?type 缃┖鐨?MOVE锛堜笌瑙勫垯涓€鑷达紝闇€鍙岄€氶亾骞挎挱锛夈€?  ], [寰呯‘璁,
  [Q2], [
    棰樼洰鍐欍€屼笉鑰冭檻涓嶅簲灏嗐€嶏紝璧板瓙鍚庡繁鏂硅灏嗗啗鏄惁浠嶄竴寰嬫嫆缁濓紵鑻ュ鏂规湭搴斿皢锛屾槸鍚﹀厑璁哥洿鎺ュ悆甯呯粨鏉燂紵
  ], [
    闈炴硶鐫€娉曟嫆缁濓紙璧板瓙鍚庡繁鏂硅灏嗭級锛涘悆鏄庡竻浠嶅垽鑳滐紱涓嶅疄鐜般€屽簲灏嗐€嶆祦绋嬨€?  ], [寰呯‘璁,
  [Q3], [
    銆?0 鍥炲悎鏃犲悆瀛愩€嶆寚 40 涓畬鏁村洖鍚堬紙80 鍗婃锛夎繕鏄?40 鍗婃锛熶笌瀹炵幇 `noCaptureCount >= 80` 鏄惁涓€鑷达紵
  ], [
    閲囩敤 80 鍗婃锛堢瓑浠峰弻鏂瑰悇 40 姝ユ棤鍚冨瓙锛夛紝GAME_OVER 鍘熷洜鐮?6銆?  ], [寰呯‘璁,
  [Q4], [
    闀垮皢/闀挎崏銆? 娆°€嶆槸鍚﹀繀椤?杩炵画*锛熶腑闂存彃鍏ラ潪灏嗗啗銆侀潪鎹夊瓙姝ユ槸鍚﹂噸缃鏁帮紵
  ], [
    鍚屽眬闈㈠搱甯岀疮璁¤揪 6 娆″垽璐?鍜岋紙涓嶅己鍒惰繛缁級锛涘叺鍗掗暱鎹夊崟鐙垽鍜岋紙Q5锛夈€?  ], [寰呯‘璁,
  [Q5], [
    銆屽叺鍗掗暱鎹夊垽鍜屻€嶏細鍏靛崚闀挎崏*鍏靛崚*鍜岋紝杩樻槸鍏靛崚闀挎崏*浠绘剰瀛?鍧囧拰锛?  ], [
    鍏靛崚闀挎崏瀵艰嚧閲嶅灞€闈?鈮? 娆″垽鍜岋紙涓嶉檺琚崏瀛愮被鍨嬶級銆?  ], [寰呯‘璁,
  [Q6], [
    鍥版瘷銆佹棤鍚堟硶璧板瓙锛堝惈浠呰兘鍘熷湴缈诲瓙锛夋槸鍚﹀潎鍒よ礋锛熷崟鏂瑰彧鍓╁皢甯呮槸鍚﹀拰妫嬶紵
  ], [
    鏃犲悎娉曠潃娉曪紙鍚炕瀛愶級涓旀湭琚皢鏃跺垽鍥版瘷璐燂紱鍗曟柟灏嗗竻鍒ゅ拰锛堝疄鐜板彲鎵╁睍锛夈€?  ], [寰呯‘璁,
  [Q7], [
    灏嗗竻鏄惁鍏佽鐓ч潰锛堜腑闂存棤瀛愶級锛熶簹娲茶鍒欏父瑙併€屼笉鍙収闈€嶃€?  ], [
    鍏佽鐓ч潰锛涚収闈㈡椂涓嶅垽璐燂紙涓庝腑鍥借薄妫嬮儴鍒嗚鍒欎竴鑷达紝寰呮潈濞佽瀹氾級銆?  ], [寰呯‘璁,
  [Q8], [
    鏆楀瓙鍦ㄥ＋/璞′綅锛氭槸鍚︿韩鍙楁槑澹?鏄庤薄鐨勮繃娌冲己鍖栵紵杩樻槸涓ユ牸鎸?浣嶇疆铏氭嫙绫诲瀷*鐨勫師濮嬭薄妫嬭鍒欙紵
  ], [
    *鍚?锛氭殫瀛愭寜铏氭嫙绫诲瀷璧板瓙锛堝＋闄愪節瀹€佽薄涓嶈繃娌筹級锛涚炕寮€涓烘槑鍚庢墠寮哄寲銆?  ], [鏈粍鏂规],
)

== 缈诲瓙銆侀殢鏈轰笌妫嬭氨

#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q9], [
    鏆楀瓙棣栨缈诲紑鏃?type 鐢辫皝鐢熸垚锛熷鎴风涓婁紶鐨?type 鏄惁涓€寰嬬敱鏈嶅姟鍣ㄨ鐩栵紵
  ], [
    浠呮湇鍔″櫒闅忔満鐢熸垚骞跺啓鍏ュ箍鎾紱蹇界暐瀹㈡埛绔?type锛堥槻浣滃紛锛夈€?  ], [鏈粍鏂规],
  [Q10], [
    闅忔満鎵撲贡鏄惁闇€鍙鐜帮紙鍏紑 random seed锛変互渚垮鐩樹笌 AI 璋冭瘯锛?  ], [
    鑱旇皟涓嶈姹傦紱妫嬭氨璁板綍缈诲嚭鍚庣殑 type锛涘彲閫夋棩蹇楄褰?seed锛堟墿灞曪級銆?  ], [寰呯‘璁,
  [Q11], [
    鍘熷湴缈诲瓙锛坰ource=destination锛夋槸鍚︽瘡鍥炲悎浠呴檺涓€鏋氾紵鑳藉惁杩炵画澶氬洖鍚堝彧缈诲瓙锛?  ], [
    姣忓洖鍚堜粎鍏佽涓€娆＄炕瀛愭垨璧板瓙锛涘彲杩炵画澶氬洖鍚堝彧缈诲瓙锛堝悎娉曞嵆鍏佽锛夈€?  ], [寰呯‘璁,
  [Q12], [
    妫嬭氨涓缈绘槸鍚﹀繀椤昏褰?type锛熷悗缁槑瀛愯蛋瀛?type 瀛楁鏄惁搴斾负绌猴紵
  ], [
    棣栨缈诲紑蹇呰 type锛涘悗缁 type 涓虹┖瀛楃涓诧紱鍘熷湴缈诲瓙鏍囪 `[缈籡`銆?  ], [鏈粍鏂规],
  [Q13], [
    鍚冨瓙鍚庣炕寮€鐨?type 鏄惁瀵?鍚冨瓙鏂?绔嬪嵆鍙銆佸*琚悆鏂?鏄惁闅愯棌锛堣 Q1锛夛紵
  ], [
    涓?Q1 鑱斿姩锛涘綋鍓嶅崗璁鍙屾柟骞挎挱鐩稿悓 MOVE銆?  ], [寰呯‘璁,
)

== 缃戠粶銆佽鏃朵笌浜掓搷浣?
#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q14], [
    姣忔闄愭椂锛?0 绉?+ 5 绉掔綉缁滆閲忔槸鍚︿负璇剧▼缁熶竴鏍囧噯锛烝I 鏈湴瀵瑰紙鏄惁鍚屾爣鍑嗭紵
  ], [
    缃戠粶瀵规垬 65s锛堟湇鍔″櫒 `turnStartTime`锛夛紱AI 鏈湴鍙嚜瀹氾紝鎶ュ憡璇存槑鍗冲彲銆?  ], [寰呯‘璁,
  [Q15], [
    瀹㈡埛绔?`turnStartTime` 鏄惁瀹屽叏蹇界暐锛熸槸鍚︿繚鐣欑敤浜庢棩蹇椾笌鎺掗敊锛?  ], [
    鍒よ秴鏃朵粎浠ユ湇鍔″櫒鏃堕棿涓哄噯锛涘鎴风鏃堕棿鎴冲彲璁板綍浣嗕笉鍙備笌鍒ゅ畾銆?  ], [鏈粍鏂规],
  [Q16], [
    TCP 绔彛鏄惁缁熶竴锛熶笉鍚岀粍榛樿绔彛涓嶄竴鑷存椂濡備綍鑱旇皟锛?  ], [
    寤鸿缁熶竴榛樿 8888锛涘悇缁勫彲鏀圭鍙ｄ絾鍚姩鏃舵墦鍗帮紝鏂囨。鍐欐槑銆?  ], [鏈粍鏂规],
  [Q17], [
    鏂嚎閲嶈繛锛氭帀绾垮悗鑳藉惁鐢ㄧ浉鍚岃韩浠介噸杩炲苟鎭㈠妫嬪眬锛熸槸鍚︾撼鍏ュ叕鍏卞崗璁紵
  ], [
    v2.0 鏈畾涔夛紱鏂嚎鍒ゅ鏂硅儨锛堝師鍥犵爜 4锛夛紱閲嶈繛鍒椾负 v2.1 鎵╁睍寤鸿銆?  ], [寰呯‘璁,
  [Q18], [
    澶氱洏瀵瑰紙锛歚gameId` 涓虹┖鏃惰嚜鍔ㄥ尮閰嶈鍒欙紵婊″憳鍚庢槸鍚︽柊寤烘埧闂达紵
  ], [
    鍏堝尮閰?WAITING 鎴块棿锛涙棤鍒欏垱寤?UUID 鍓?8 浣嶏紱婊″憳鎷掔粷锛圗RROR 201锛夈€?  ], [鏈粍鏂规],
  [Q19], [
    瑙傛垬/瑁佸垽锛氱涓夋柟鍙瀹㈡埛绔槸鍚﹂渶鏍囧噯娑堟伅锛堝 SPECTATOR锛夛紵
  ], [
    鏆備笉绾冲叆蹇呭仛锛涜鎴樼粍鍙闃?BOARD_STATE 鎵╁睍瀹炵幇銆?  ], [寰呯‘璁,
  [Q20], [
    鍜屾锛氭彁鍜屾槸鍚﹂渶鍙屾柟 OFFER 鍚?ACCEPT锛熷崟鏂规彁鍜屾槸鍚﹁冻澶燂紵
  ], [
    涓€鏂?ACCEPT 鍗冲拰锛堝師鍥犵爜 9锛夛紱鎷掔粷鍒?DECLINE 缁х画銆?  ], [鏈粍鏂规],
)

== AI 鍗氬紙涓庤瘎浼帮紙閫夊仛 AI 閮ㄥ垎鐨勭粍鍙竴骞惰瀹氾級

#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q21], [
    鏆楀瓙璇勪及锛氭湭缈诲紑瀛愮敤*铏氭嫙浣嶇疆浠峰€?杩樻槸*鍓╀綑姹犳湡鏈涘€?锛熸槸鍚﹀悜鑰佸笀鎶ュ璇勪及鍏紡锛?  ], [
    浜掓搷浣滀笉浼犺緭璇勪及锛涘悇缁?AI 鑷畾锛屽疄楠屾姤鍛婂叕寮€鍏紡涓庤儨璐熺粺璁°€?  ], [鏈粍鏂规],
  [Q22], [
    AI 涓庣湡浜哄寮堟槸鍚﹀繀椤昏繛鎺ユ湰鍗忚鏈嶅姟鍣紵鑳藉惁鏈湴鍏辩敤涓€涓?Board 绫伙紵
  ], [
    榧撳姳鍏辩敤 `jieqi-core` 棰嗗煙妯″潡锛涚綉缁?AI 浣滅壒娈婂鎴风杩炴帴鏈嶅姟鍣ㄣ€?  ], [寰呯‘璁,
  [Q23], [
    Agent 瀵硅薄鏄惁闇€缁熶竴鎺ュ彛锛堝 `JieqiAgent.selectMove(Board)`锛変互渚跨粍闂?AI 瀵规垬锛?  ], [
    涓嶅己鍒讹紱寤鸿璇剧▼鎻愪緵鍙€夋帴鍙ｇ害瀹氾紝渚夸簬鎿傚彴璧涖€?  ], [寰呯‘璁,
  [Q24], [
    闅忔満缈诲瓙瀵艰嚧鎼滅储鏍戣啫鑳€锛欰I 鏄惁鍏佽銆屾湡鏈涚潃娉曘€嶄笌銆屽疄闄呯炕寮€銆嶅垎鏀笉涓€鑷寸殑鏃ュ織璇存槑锛?  ], [
    鍏佽锛涙姤鍛婅褰曟湡鏈涘垎鏁颁笌瀹為檯缁撴灉鐨勫樊寮傛牱鏈€?  ], [寰呯‘璁,
)

浠ヤ笅 Q25鈥換44 涓烘湰缁勬彁鍑虹殑 #strong[鎵╁睍鏂瑰悜]锛堥潪浣滀笟蹇呭仛锛夛紝鐢ㄤ簬瀹屽杽棰樼洰杈圭晫銆佷簤鍙栬绋嬪姞鍒嗭紝骞朵緵鑰佸笀瑁佸畾鏄惁绾冲叆鍏叡瑙勮寖 v2.1 鎴栦粎浣滄湰缁勫疄楠屾姤鍛婁寒鐐广€?
#table(
  columns: (auto, 1fr),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  table.header([*鏂瑰悜*], [*缂栧彿*]),
  [I 缃戠粶搴曞眰涓庡苟鍙慮, [Q25鈥換32],
  [II 澶氭櫤鑳戒綋涓?LLM], [Q33鈥換39],
  [III 鍏ㄦ爤宸ョ▼鍖栦笌 UI], [Q40鈥換44],
)

== I. 缃戠粶搴曞眰涓庡苟鍙戞灦鏋?
#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q25], [
    TCP 绮樺寘/鍗婂寘锛氫粎鐢?`readLine()` 鎸?`\n` 鍒囧抚鏄惁瓒冲锛熸槸鍚﹀簲寮哄埗銆岄暱搴﹀瓧娈?+ 瀛楄妭缂撳啿銆嶈В鐮侊紵
  ], [
    v2.0 甯ф牸寮忎负 `msgType\|payloadLen\|payload\n`锛沑
    鎺ㄨ崘瀹炵幇 `FrameDecoder`锛堢幆褰㈢紦鍐?ByteBuffer锛夛紝鍏堝噾婊?`payloadLen` 鍐嶈В鏋愶紝`\n` 浠呬綔甯у熬鏍￠獙锛沑
    鎶ュ憡涓撹妭璇存槑绮樺寘/鍗婂寘涓庡崐鍖呮仮澶嶆祦绋嬨€?  ], [鏈粍鏂规],
  [Q26], [
    `payloadLen` 涓?UTF-8 瀹為檯瀛楄妭鏁颁笉涓€鑷存椂濡備綍澶勭悊锛熷瀛楄妭涓枃 CHAT 鏄惁璁″叆闀垮害锛?  ], [
    闀垮害鎸?UTF-8 *瀛楄妭*璁★紱涓嶄竴鑷村垯 ERROR 111 骞朵涪寮冭甯у墿浣欏瓧鑺傦紙闃插崗璁敊涔憋級銆?  ], [鏈粍鏂规],
  [Q27], [
    鍗曞抚 `payloadLen` 涓婇檺鏄惁璇剧▼缁熶竴锛熼槻姝㈡伓鎰忓鎴风鎾戠垎鍐呭瓨銆?  ], [
    寤鸿涓婇檺 64 KiB锛涜秴闄愬叧闂繛鎺ュ苟璁版棩蹇楋紙寰呰€佸笀纭鏁板€硷級銆?  ], [寰呯‘璁,
  [Q28], [
    楂樺苟鍙戯細姣忚繛鎺ヤ竴绾跨▼锛圔IO锛夎繕鏄?`NIO Selector` / 铏氭嫙绾跨▼锛熸槸鍚﹀奖鍝嶄簰鎿嶄綔锛?  ], [
    浜掓搷浣滀粎绾︽潫*瀛楄妭娴佽涔?锛屼笉绾︽潫 IO 妯″瀷锛涙湰缁?server 鍙凯浠ｄ负 NIO锛屾姤鍛婂姣斿悶鍚愩€?  ], [寰呯‘璁,
  [Q29], [
    澶氱洏瀵瑰紙瑙勬ā鎵╁ぇ鍚庯紝鍐呭瓨 `Map<gameId, GameSession>` 鏄惁鏀逛负 Redis 缂撳瓨瀵瑰眬鐘舵€侊紵
  ], [
    蹇呭仛鍙粛鐢ㄥ唴瀛橈紱鍔犲垎璺緞锛歚jieqi-server` + Redis 瀛?BOARD/MOVE 搴忓垪涓庢埧闂村厓鏁版嵁锛沑
    閿懡鍚嶅缓璁?`jieqi:room:{gameId}`銆乣jieqi:queue:waiting`銆?  ], [寰呯‘璁,
  [Q30], [
    Redis 鍖归厤姹狅紙Matchmaking Queue锛夛細LOGIN 鏃?`gameId` 涓虹┖鏄惁鍏堝叆闃熷啀 pop 閰嶅锛?  ], [
    涓?Q18 涓€鑷撮€昏緫锛岄槦鍒楀彲鐢?Redis LIST锛涙棤 Redis 鏃堕€€鍖栦负鍐呭瓨 Map 鎵弿銆?  ], [寰呯‘璁,
  [Q31], [
    鍒嗗竷寮忎笅璁℃椂鍣細瓒呮椂鍒ゅ畾鏄惁浠嶄互*娓告垙杩涚▼*鏈湴鏃堕挓涓哄噯锛岃繕鏄?Redis TTL / 鐙珛璋冨害鏈嶅姟锛?  ], [
    鑱旇皟鏈€灏忔柟妗堬細浠嶄互鎸佸眬杩涚▼ `turnStartTime` 涓哄噯锛汻edis 浠呯紦瀛樼姸鎬佷笉鏇夸唬鍒ゆ椂銆?  ], [寰呯‘璁,
  [Q32], [
    瀹瑰櫒鍖栦氦浠橈細`Dockerfile` + `docker-compose.yml` 鏄惁浣滀负鎺ㄨ崘鎻愪氦鐗╋紙鍚?server銆佸彲閫?Redis锛夛紵
  ], [
    鎻愪緵涓€閿?`docker compose up`锛涢粯璁ゆ毚闇?8888锛涚幆澧冨彉閲?`JIEQI_PORT`銆乣REDIS_URL`锛沑
    涓嶅己鍒朵粬缁勪娇鐢紝鏈粍瀹為獙鎶ュ憡闄勯儴缃茶妭銆?  ], [鏈粍鏂规],
)

== II. 澶氭櫤鑳戒綋鍗忎綔涓庡ぇ妯″瀷璧嬭兘

#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q33], [
    AI 鏄惁蹇呴』浠庡崟浣撶被鎷嗕负澶?Agent锛歚SearchAgent`銆乣ProbabilityAgent`銆乣EndgameAgent`锛?  ], [
    浣滀笟浠呰姹傘€岃嚦灏戜竴涓?Agent 瀵硅薄銆嶏紱鏈粍寤鸿鎷嗗垎骞?`Orchestrator` 缂栨帓锛屾姤鍛婇檮鏋舵瀯鍥撅紱\
    涓嶅己鍒朵粬缁勬媶鍒嗐€?  ], [寰呯‘璁,
  [Q34], [
    澶?Agent 鍐崇瓥鍚堝苟锛氫覆琛岋紙鍏堟鐜囦慨姝ｈ瘎浼板啀鎼滅储锛夎繕鏄苟琛屾姇绁紵
  ], [
    鏈粍閲囩敤涓茶绠￠亾锛歅robability 鈫?Search锛涙畫灞€搴撳懡涓垯鐭矾杩斿洖銆?  ], [鏈粍鏂规],
  [Q35], [
    鏄惁瀹氫箟鍙€夋帴鍙?`JieqiSubAgent.contribute(Board, Context)` 渚夸簬缁勯棿 AI 妯″潡浜掓崲锛?  ], [
    璇剧▼绾у彲閫夌害瀹氾紝缃簬 `jieqi-ai`锛涗簰鎿嶄綔浠嶅彧渚濊禆 TCP MOVE銆?  ], [寰呯‘璁,
  [Q36], [
    鍗忚宸查鐣?MSG\_CHAT(10)锛氭槸鍚﹀厑璁?AI/LLM 鑷姩鍙戙€屽績鐞嗘垬/瑙ｈ銆嶏紵鏄惁绠楁寮忓姛鑳斤紵
  ], [
    鍏佽浣滄紨绀轰笌鍔犲垎锛涢粯璁ゅ叧闂紱寮€鍚渶鍦?LOGIN 鎴栭厤缃腑澹版槑 `allowChatAI=true`銆?  ], [寰呯‘璁,
  [Q37], [
    LLM Prompt 杈撳叆鑼冨洿锛氫粎鎶借薄灞€闈紙瀛愬姏宸€佸洖鍚堛€佹槸鍚﹁灏嗭級杩樻槸鍚畬鏁?FEN/BOARD\_STATE锛?  ], [
    绂佹涓婁紶瀵规墜闅愮锛涗粎鍙戦€佽劚鏁忔憳瑕?+ 宸辨柟瑙嗚锛汚PI Key 浠呭瓨鏈嶅姟绔?鏈湴閰嶇疆銆?  ], [鏈粍鏂规],
  [Q38], [
    CHAT 棰戠巼涓庨暱搴︼細鏄惁闄愬埗锛堝姣?30s 涓€鏉°€佲墹200 瀛楋級锛熸槸鍚﹂渶鏁忔劅璇嶈繃婊わ紵
  ], [
    鏈嶅姟鍣ㄩ檺閫?1 鏉?10s/浜猴紱瓒呴暱鎴柇锛涜绋嬫紨绀哄彲浜哄伐瀹℃牳寮€鍏炽€?  ], [寰呯‘璁,
  [Q39], [
    LLM 澶辫触锛堣秴鏃?閰嶉锛夋椂锛氶潤榛樿烦杩囪繕鏄彂閫佸浐瀹?fallback 鏂囨锛?  ], [
    闈欓粯璺宠繃锛屼笉褰卞搷瀵瑰紙锛汣HAT 涓?MOVE 瑙ｈ€︺€?  ], [鏈粍鏂规],
)

== III. 鍏ㄦ爤宸ョ▼鍖栦笌鏋佽嚧 UI

#table(
  columns: (0.9cm, 1fr, 1fr, 1.1cm),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left, center + horizon),
  table.header([*搴忓彿*], [*闂*], [*鏈粍鏆傚畾鏂规*], [*鐘舵€?]),
  [Q40], [
    鏃佽/鏁版嵁绔細鏄惁鏂板鏍囧噯娑堟伅锛堝 `STATS`銆乣SPECTATOR`锛夊箍鎾儨鐜囥€佹殫瀛愭睜姒傜巼锛?  ], [
    v2.1 鎵╁睍锛泇2.0 瑙傛垬鍙粎鏀?BOARD\_STATE + 鏈湴浼扮畻锛汼TATS 瀛楁鏍煎紡寰呰€佸笀纭鍚庡啓鍏?搂4銆?  ], [寰呯‘璁,
  [Q41], [
    Bento 鏁版嵁鐪嬫澘锛氳儨鐜囨洸绾裤€佹殫瀛愰浄杈惧浘鐢辫皝璁＄畻锛熸湇鍔″櫒缁熶竴杩樻槸瑙傛垬 Web 鑷畻锛?  ], [
    鍔犲垎婕旂ず锛氳鎴樼鏈湴鍩轰簬 BOARD\_STATE + 鍏紑璇勪及鍏紡锛涢伩鍏嶅鍔?server 璐熸媴銆?  ], [鏈粍鏂规],
  [Q42], [
    Web 鏃佽绔紙Vite/React 绛夛級涓?Console 瀹㈡埛绔槸鍚﹀潎闇€閬靛畧鍚屼竴 TCP 鍗忚浜掓搷浣滐紵
  ], [
    鏄紱Web 閫氳繃缃戝叧鎴?Java 鍚庣浠ｇ悊 TCP锛屼笉鍙︽悶绉佹湁 WebSocket 闄ら潪鍙︾珛闄勫綍鍗忚銆?  ], [寰呯‘璁,
  [Q43], [
    銆屽鎴风涓嶅繀杩囧垎缇庡寲銆嶏細Console 涓?Bento Web 鏄惁骞跺瓨锛熻瘎鍒嗘槸鍚﹀彧鐪嬪姛鑳芥纭紵
  ], [
    Console 婊¤冻蹇呭仛锛沇eb 鐪嬫澘涓哄姞鍒嗗睍绀猴紝涓嶆浛浠ｈ蛋瀛愬鎴风銆?  ], [鏈粍鏂规],
  [Q44], [
    `docker-compose` 鏄惁鍖呭惈鍙€?Web 鐩戞帶鏈嶅姟锛堝 `:3000` 鏃佽锛変笌 Redis锛?  ], [
    鏈粍 compose 涓夋湇鍔★細`server`銆乣redis`锛坧rofile 鍙€夛級銆乣spectator-web`锛坧rofile 鍙€夛級銆?  ], [鏈粍鏂规],
)

#v(0.3cm)
*鎻愪氦寤鸿*锛氳鑰佸笀瀵?Q1鈥換44 閫愭潯纭鎴栦慨姝ｏ紱Q25鈥換44 鍙暣浣撴爣娉ㄤ负銆屽姞鍒嗘墿灞?/ v2.1 鑽夋銆嶃€傜‘璁ゅ悗鏈粍鏇存柊鍗忚鐗堟湰骞堕€氱煡浠栫粍銆傚繀鍋氫簰鎿嶄綔浠嶄互 Q1鈥換24 涓庢鏂囧崗璁负鍑嗐€?
// ============================================================
// 闄勫綍
// ============================================================

= 闄勫綍 A锛氭秷鎭揩閫熷弬鑰冨崱鐗?
#table(
  columns: (auto, auto, auto),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, left, left),
  table.header([*绫诲瀷*], [*鏂瑰悜*], [*payload 鏍煎紡*]),
  [LOGIN], [C鈫扴], [`<color>\|<name>\|<gameId>`],
  [MOVE], [鍙岃竟], [`<src>\|<dst>\|<type>\|<time>\|<flip>`],
  [GAME\_STATE], [S鈫扖], [`LOGIN_ACK\|<id>\|<color>\|<status>` / `GAME_START\|...` / `TURN_CHANGE\|...`],
  [ERROR], [S鈫扖], [`<code>\|<msg>`],
  [QUIT], [C鈫扴], [锛堢┖锛塢,
  [GAME\_OVER], [S鈫扖], [`<winner>\|<reasonCode>\|<desc>`],
  [BOARD\_STATE], [S鈫扖], [`<turn>\|<r0>;...;<r9>`],
  [DRAW\_REQUEST], [C鈫擲], [`OFFER` / `ACCEPT` / `DECLINE`],
  [RESIGN], [C鈫擲], [锛堢┖锛夋垨 `<color>`],
  [CHAT], [鍙岃竟], [`<color>\|<name>\|<msg>`],
)

= 闄勫綍 B锛氱増鏈巻鍙?
#table(
  columns: (1.1cm, 2.4cm, 1fr),
  stroke: (x, y) => if y < 1 { (bottom: 0.5pt + black) },
  align: (center + horizon, center + horizon, left),
  table.header([*鐗堟湰*], [*鏃ユ湡*], [*涓昏鍙樻洿*]),
  [v0.1], [2026-05-08], [
    椤圭洰缁勫唴閮ㄨ崏妗堬紱纭畾閲囩敤 TCP 鏂囨湰甯с€乁TF-8/LF 绾﹀畾锛沑
    鍒濇瀹氫箟鍧愭爣绯伙紙琛?9鈥?銆佸垪 a鈥搃锛変笌鍏堝悗鎵嬫柟浣?  ],
  [v0.2], [2026-05-10], [
    琛ュ厖 Move 瀵硅薄瀛楁锛坰ource銆乨estination銆乼ype銆佹椂闂存埑锛夛紱\
    绾﹀畾鏆楀瓙棣栨缈诲紑鐢辨湇鍔″櫒闅忔満鐢熸垚 type锛沑
    璧疯崏 MSG_LOGIN / MSG_MOVE 涓ょ被娑堟伅
  ],
  [v1.0], [2026-05-12], [
    鎻愪氦璇剧▼鍒濈锛氬潗鏍囩郴缁熴€丮ove 瑙勮寖銆? 绉嶆牳蹇冩秷鎭被鍨媆
    锛圠OGIN銆丮OVE銆丟AME_STATE銆丒RROR銆丵UIT銆丟AME_OVER銆丅OARD_STATE锛夛紱\
    瀹炵幇缁勫唴 Reference Server 鑱旇皟閫氳繃
  ],
  [v1.1], [2026-05-15], [
    瀹屽杽 BOARD_STATE 琛?鍒楃紪鐮佷笌 Cell 瑙勫垯锛坄0?`/`1?`/鏄庡瓙缂栫爜锛夛紱\
    澧炲姞闈炴硶鐫€娉?ERROR 閿欒鐮佽〃锛?00鈥?12锛夛紱\
    琛ュ厖瓒呮椂鍒よ礋涓庢柇绾垮鐞嗚鏄?  ],
  [v1.2], [2026-05-18], [
    鏂板 GAME_STATE 瀛愮被鍨嬶紙LOGIN_ACK銆丟AME_START銆乀URN_CHANGE锛夛紱\
    璧疯崏 MSG_DRAW_REQUEST銆丮SG_RESIGN銆丮SG_CHAT 鎵╁睍娑堟伅锛沑
    缂栧啓铏氭嫙绫诲瀷琛ㄤ笌鏄庡＋/鏄庤薄寮哄寲瑙勫垯璇存槑锛沑
    瀹屾垚涓庣浜岀粍瀹㈡埛绔娆′簰閫氭祴璇?  ],
  [v1.3], [2026-05-20], [
    缁熶竴 payload 闀垮害鏍￠獙瑙勫垯锛涜ˉ鍏?GAME_OVER 鍘熷洜鐮侊紙0鈥?锛夛紱\
    澧炲姞 40 姝ユ棤鍚冨瓙鍜屻€侀暱灏?闀挎崏鍒や緥璇存槑锛沑
    鏁寸悊妫嬭氨璁拌氨鏍煎紡涓庣粍闂磋仈璋冩鏌ユ竻鍗曞垵鐗?  ],
  [v2.0], [2026-05-22], [
    鍏ㄩ潰閲嶆瀯涓虹幇琛屾潈濞佺増鏈紱瀹屾暣甯ф牸寮忥紙msgType\|len\|payload锛変笌瑙ｆ瀽浼唬鐮侊紱\
    姝ｅ紡绾冲叆 8鈥?0 鍙锋秷鎭紙鎻愬拰銆佽杈撱€佽亰澶╋級锛沑
    瀹屽杽閿欒鐮?鍘熷洜鐮佷綋绯汇€佸吀鍨嬮€氫俊鏃跺簭鍥俱€佽竟鐣屽垽渚嬶紱\
    鎵╁睍 BOARD_STATE 瀹屾暣绀轰緥涓庨€愯瑙ｆ瀽锛沑
    瀹氱铏氭嫙绫诲瀷琛ㄣ€佸鐩樺寮堢害瀹氬強寰呰€佸笀纭闂鍒楄〃锛沑
    鏈枃妗ｄ綔涓虹粍闂翠簰鎿嶄綔鍞竴鍙傝€?  ],
)
