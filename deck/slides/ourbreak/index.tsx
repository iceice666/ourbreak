import type { DesignSystem, Page, SlideMeta, SlideTransition } from '@open-slide/core';
import { useSlidePageNumber } from '@open-slide/core';

import interFont from './assets/Inter-Variable.ttf';

// ─── inm theme tokens (github.com/iceice666/inm · OKLCH → hex) ───────────────
// Light = warm stone canvas · Dark = plum black canvas. Clay carries action,
// mist is the cool support tone. We keep the palettes explicit (not driven off
// one --osd var) because the deck deliberately alternates light/dark scopes.
const LIGHT = {
  bg: '#C9BFB6',
  surface: '#DAD2CB',
  raised: '#E8E1DB',
  text: '#2F2730',
  muted: '#625B5E',
  border: '#AFA8A3',
  accent: '#7E4F49',
  accentSoft: '#A1736B',
  cool: '#79837F',
  onAccent: '#E8E1DB',
} as const;

const DARK = {
  bg: '#3E343F',
  surface: '#493E4A',
  raised: '#584B59',
  text: '#E8E1DB',
  muted: '#B6AFAD',
  border: '#6B6069',
  accent: '#D7A095',
  accentSoft: '#C58E82',
  cool: '#AAB4B0',
  onAccent: '#3E343F',
} as const;

const ANCHOR = { plum: '#3E343F', clay: '#A1736B', mist: '#979F9B', stone: '#C9BFB6' } as const;

type Scope = typeof LIGHT;

const SANS =
  'Inter, "PingFang TC", "Microsoft JhengHei", "Noto Sans TC", ui-sans-serif, system-ui, sans-serif';
const MONO = '"SFMono-Regular", "JetBrains Mono", Consolas, Menlo, monospace';

// ─── Panel-tweakable tokens (light scope default) ────────────────────────────
export const design: DesignSystem = {
  palette: { bg: '#C9BFB6', text: '#2F2730', accent: '#7E4F49' },
  fonts: { display: SANS, body: SANS },
  typeScale: { hero: 132, body: 36 },
  radius: 8,
};

// ─── One-time @font-face + keyframes injection ───────────────────────────────
if (typeof document !== 'undefined' && !document.getElementById('ourbreak-theme')) {
  const el = document.createElement('style');
  el.id = 'ourbreak-theme';
  el.textContent = `
    @font-face {
      font-family: 'Inter';
      src: url('${interFont}') format('truetype');
      font-weight: 100 900;
      font-stretch: 75% 125%;
      font-display: swap;
    }
    @keyframes ob-rise { from { opacity: 0; transform: translateY(14px); } to { opacity: 1; transform: translateY(0); } }
    @keyframes ob-grow { from { transform: scaleX(0); } to { transform: scaleX(1); } }
  `;
  document.head.appendChild(el);
}

// ─── House transition: one quiet DNA across the deck ─────────────────────────
const EASE_OUT = 'cubic-bezier(0, 0, 0.2, 1)';
const EASE_IN = 'cubic-bezier(0.4, 0, 1, 1)';
export const transition: SlideTransition = {
  duration: 200,
  exit: {
    duration: 140,
    easing: EASE_IN,
    keyframes: [
      { opacity: 1, transform: 'translateY(0)' },
      { opacity: 0, transform: 'translateY(-4px)' },
    ],
  },
  enter: {
    duration: 200,
    delay: 80,
    easing: EASE_OUT,
    keyframes: [
      { opacity: 0, transform: 'translateY(6px)' },
      { opacity: 1, transform: 'translateY(0)' },
    ],
  },
};

// ─── Shared chrome ───────────────────────────────────────────────────────────
const fillBase = { width: '100%', height: '100%', fontFamily: SANS, position: 'relative' } as const;

const page = (s: Scope) => ({
  ...fillBase,
  background: s.bg,
  color: s.text,
  boxSizing: 'border-box' as const,
});

const Eyebrow = ({ children, color }: { children: React.ReactNode; color: string }) => (
  <div
    style={{
      fontSize: 24,
      fontWeight: 760,
      letterSpacing: '0.18em',
      textTransform: 'uppercase',
      color,
    }}
  >
    {children}
  </div>
);

const Footer = ({ scope, label }: { scope: Scope; label: string }) => {
  const { current, total } = useSlidePageNumber();
  return (
    <div
      style={{
        position: 'absolute',
        left: 130,
        right: 130,
        bottom: 64,
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        fontSize: 22,
        fontWeight: 620,
        color: scope.muted,
      }}
    >
      <span>{label}</span>
      <span style={{ fontFamily: MONO, letterSpacing: '0.04em' }}>
        {String(current).padStart(2, '0')} / {String(total).padStart(2, '0')}
      </span>
    </div>
  );
};

const Rule = ({ color, width = 132 }: { color: string; width?: number }) => (
  <div
    style={{
      width,
      height: 5,
      background: color,
      borderRadius: 999,
      transformOrigin: 'left',
      animation: 'ob-grow 0.5s ease-out both',
    }}
  />
);

// Decorative anchor blocks for the title (the four inm anchors, kept visible).
const AnchorBlocks = () => (
  <div style={{ position: 'absolute', top: 130, right: 130, display: 'flex', gap: 18 }} aria-hidden>
    <i style={{ width: 64, height: 64, background: ANCHOR.stone, borderRadius: 8, display: 'block' }} />
    <i style={{ width: 64, height: 64, background: ANCHOR.mist, borderRadius: 8, display: 'block', marginTop: 34 }} />
    <i style={{ width: 64, height: 64, background: ANCHOR.clay, borderRadius: 8, display: 'block' }} />
  </div>
);

// ─── 01 · Title ──────────────────────────────────────────────────────────────
const Cover: Page = () => (
  <div style={{ ...page(DARK), display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 130px' }}>
    <AnchorBlocks />
    <div style={{ position: 'absolute', top: 130, left: 130, display: 'flex', alignItems: 'center', gap: 16 }}>
      <span style={{ width: 34, height: 34, background: DARK.accent, borderRadius: 7, display: 'block' }} />
      <span style={{ fontSize: 30, fontWeight: 800, letterSpacing: '-0.01em' }}>ourbreak</span>
    </div>
    <Eyebrow color={DARK.accent}>工程紀律 × AI Agent</Eyebrow>
    <h1 style={{ fontSize: 132, fontWeight: 900, lineHeight: 1.08, letterSpacing: '-0.02em', margin: '26px 0 0' }}>
      如何避免 vibe<br />出一堆垃圾
    </h1>
    <p style={{ fontSize: 38, lineHeight: 1.5, color: DARK.muted, margin: '34px 0 0', maxWidth: 1180 }}>
      用一套工程框架馴服 coding agent —— 以 <span style={{ color: DARK.text, fontWeight: 600 }}>ourbreak</span> 這款海灘破壞遊戲為例。
    </p>
    <div style={{ marginTop: 40 }}>
      <Rule color={DARK.accent} width={160} />
    </div>
    <Footer scope={DARK} label="ourbreak · 第一人稱海灘破壞遊戲" />
  </div>
);

// ─── 02 · Thesis ───────────────────────────────────────────────────────────��─
const ThesisChip = ({ label }: { label: string }) => (
  <span
    style={{
      display: 'inline-flex',
      alignItems: 'center',
      padding: '12px 28px',
      fontSize: 30,
      fontWeight: 720,
      color: DARK.accent,
      border: `2px solid ${DARK.border}`,
      borderRadius: 999,
    }}
  >
    {label}
  </span>
);

const Thesis: Page = () => (
  <div style={{ ...page(DARK), display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 130px' }}>
    <Eyebrow color={DARK.accent}>命題</Eyebrow>
    <h2 style={{ fontSize: 76, fontWeight: 820, lineHeight: 1.28, margin: '34px 0 0', letterSpacing: '-0.01em' }}>
      agent 沒規格、沒記憶、沒驗證，<br />於是自信地生出一堆垃圾。
    </h2>
    <div style={{ display: 'flex', gap: 20, marginTop: 52 }}>
      <ThesisChip label="沒規格 → 自己編需求" />
      <ThesisChip label="沒記憶 → 重蹈覆轍" />
      <ThesisChip label="沒驗證 → 壞了也不知道" />
    </div>
    <p style={{ fontSize: 46, fontWeight: 600, lineHeight: 1.45, color: DARK.text, margin: '56px 0 0', maxWidth: 1320 }}>
      解法不是換更強的模型 ——<br />
      是把 agent <span style={{ color: DARK.accent }}>關進工程框架</span>，讓它只能交出好東西。
    </p>
    <div style={{ marginTop: 40 }}>
      <Rule color={DARK.accent} width={160} />
    </div>
    <Footer scope={DARK} label="為什麼會 vibe 出垃圾" />
  </div>
);

// ─── 03 · Agenda ───────────────────────────────────────────────────────────��─
const AgendaRow = ({
  num,
  title,
  gloss,
  active,
}: {
  num: string;
  title: string;
  gloss: string;
  active?: boolean;
}) => (
  <div
    style={{
      display: 'flex',
      alignItems: 'baseline',
      gap: 36,
      padding: '26px 0',
      borderTop: `1px solid ${LIGHT.border}`,
    }}
  >
    <span style={{ fontFamily: MONO, fontSize: 34, fontWeight: 700, color: active ? LIGHT.accent : LIGHT.cool, width: 70 }}>
      {num}
    </span>
    <span style={{ fontSize: 46, fontWeight: 780, color: active ? LIGHT.text : LIGHT.muted, width: 460 }}>
      {title}
    </span>
    <span style={{ fontSize: 30, fontWeight: 460, color: LIGHT.muted, lineHeight: 1.4 }}>{gloss}</span>
  </div>
);

const Agenda: Page = () => (
  <div style={{ ...page(LIGHT), display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 130px' }}>
    <Eyebrow color={LIGHT.accent}>本場路線</Eyebrow>
    <h2 style={{ fontSize: 72, fontWeight: 820, margin: '20px 0 40px', letterSpacing: '-0.01em' }}>Agenda</h2>
    <div style={{ borderBottom: `1px solid ${LIGHT.border}` }}>
      <AgendaRow num="01" title="設計架構" gloss="GDD · TDD · AGENTS.md · OpenSpec —— 先把要做什麼說清楚" active />
      <AgendaRow num="02" title="開發流程" gloss="Devlog · Milestone —— 留下不可變的軌跡" />
      <AgendaRow num="03" title="成果檢驗" gloss="Unit / Auto Test · Manual Test —— 過不了就 commit 不了" />
      <AgendaRow num="04" title="遊戲架構 + Demo" gloss="ECS · Logic Flow —— 框架長出來的成品" />
    </div>
    <Footer scope={LIGHT} label="ourbreak · 簡報路線" />
  </div>
);

// ─── Content-page primitives ─────────────────────────────────────────────────
const contentPage = (s: Scope) => ({
  ...page(s),
  display: 'flex',
  flexDirection: 'column' as const,
  padding: '104px 130px 140px',
});

const PageHead = ({
  scope,
  eyebrow,
  title,
  sub,
}: {
  scope: Scope;
  eyebrow: string;
  title: React.ReactNode;
  sub?: React.ReactNode;
}) => (
  <div>
    <Eyebrow color={scope.accent}>{eyebrow}</Eyebrow>
    <h2 style={{ fontSize: 66, fontWeight: 820, margin: '16px 0 0', letterSpacing: '-0.01em', lineHeight: 1.12 }}>
      {title}
    </h2>
    {sub && <p style={{ fontSize: 31, fontWeight: 500, color: scope.muted, margin: '16px 0 0', lineHeight: 1.5 }}>{sub}</p>}
  </div>
);

const PointCard = ({
  scope,
  n,
  title,
  body,
}: {
  scope: Scope;
  n: string;
  title: string;
  body: React.ReactNode;
}) => (
  <div
    style={{
      display: 'flex',
      gap: 22,
      padding: '26px 30px',
      background: scope.surface,
      border: `1px solid ${scope.border}`,
      borderRadius: 8,
    }}
  >
    <span style={{ fontFamily: MONO, fontSize: 28, fontWeight: 700, color: scope.accent, lineHeight: 1.3 }}>{n}</span>
    <div>
      <h4 style={{ fontSize: 33, fontWeight: 760, margin: 0, lineHeight: 1.25 }}>{title}</h4>
      <p style={{ fontSize: 26, fontWeight: 460, color: scope.muted, margin: '9px 0 0', lineHeight: 1.42 }}>{body}</p>
    </div>
  </div>
);

const Chip = ({ scope, children }: { scope: Scope; children: React.ReactNode }) => (
  <span
    style={{
      display: 'inline-flex',
      alignItems: 'center',
      padding: '11px 22px',
      fontFamily: MONO,
      fontSize: 25,
      fontWeight: 600,
      color: scope.text,
      background: scope.raised,
      border: `1px solid ${scope.border}`,
      borderRadius: 7,
    }}
  >
    {children}
  </span>
);

// ─── Code window (always dark surface, even on light slides) ──────────────────
const CODE_BG = '#2C242D';
const T = { com: '#8E857F', key: '#D7A095', str: '#AAB4B0', fn: '#F1EAE4', num: '#C58E82', punc: '#9A8F94' };
const C = ({
  c,
  b,
  i,
  children,
}: {
  c: string;
  b?: boolean;
  i?: boolean;
  children: React.ReactNode;
}) => <span style={{ color: c, fontWeight: b ? 700 : undefined, fontStyle: i ? 'italic' : undefined }}>{children}</span>;

const Dot = ({ c }: { c: string }) => (
  <span style={{ width: 13, height: 13, borderRadius: 999, background: c, display: 'block' }} />
);

const CodeWin = ({
  file,
  badge,
  fontSize = 25,
  children,
  style,
}: {
  file: string;
  badge?: string;
  fontSize?: number;
  children: React.ReactNode;
  style?: React.CSSProperties;
}) => (
  <div
    style={{
      background: CODE_BG,
      borderRadius: 11,
      overflow: 'hidden',
      border: '1px solid #4A3F4B',
      boxShadow: '0 18px 44px rgba(20,16,20,0.22)',
      ...style,
    }}
  >
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 14,
        padding: '13px 20px',
        background: 'rgba(255,255,255,0.045)',
        borderBottom: '1px solid rgba(255,255,255,0.08)',
      }}
    >
      <div style={{ display: 'flex', gap: 9 }}>
        <Dot c="#C77B72" />
        <Dot c="#C9A24B" />
        <Dot c="#8BA877" />
      </div>
      <span style={{ fontFamily: MONO, fontSize: 19, color: '#B6AFAD' }}>{file}</span>
      {badge && (
        <span
          style={{
            marginLeft: 'auto',
            fontFamily: MONO,
            fontSize: 17,
            color: '#3E343F',
            background: '#D7A095',
            padding: '3px 12px',
            borderRadius: 999,
            fontWeight: 700,
          }}
        >
          {badge}
        </span>
      )}
    </div>
    <pre
      style={{
        margin: 0,
        padding: '26px 30px',
        fontFamily: MONO,
        fontSize,
        lineHeight: 1.62,
        color: '#E8E1DB',
        whiteSpace: 'pre',
        overflow: 'hidden',
      }}
    >
      {children}
    </pre>
  </div>
);

// ─── 04 · Section divider — 設計架構 (dark) ───────────────────────────────────
const DesignSection: Page = () => (
  <div style={{ ...page(DARK), display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 130px' }}>
    <div style={{ fontFamily: MONO, fontSize: 30, fontWeight: 700, color: DARK.accent, letterSpacing: '0.04em' }}>
      01 — 設計架構
    </div>
    <h2 style={{ fontSize: 88, fontWeight: 860, lineHeight: 1.18, margin: '30px 0 0', letterSpacing: '-0.02em', maxWidth: 1480 }}>
      先把「要做什麼」<br />說清楚，agent 才不會自己編。
    </h2>
    <div style={{ marginTop: 46 }}>
      <Rule color={DARK.accent} width={200} />
    </div>
    <p style={{ fontSize: 30, fontWeight: 500, color: DARK.muted, margin: '40px 0 0', maxWidth: 1280, lineHeight: 1.5 }}>
      GDD 說玩法 · TDD 說技術 · AGENTS.md 說家規 · OpenSpec 把每件事先寫成規格。
    </p>
    <Footer scope={DARK} label="01 · 設計架構" />
  </div>
);

// ─── 05 · GDD (light, two-col) ────────────────────────────────────────────────
const GddPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="設計架構 · 給 agent「遊戲是什麼」"
      title="GDD — Game Design Document"
    />
    <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 60, marginTop: 44, alignItems: 'center' }}>
      <div>
        <p style={{ fontSize: 33, fontWeight: 480, lineHeight: 1.6, color: LIGHT.text, margin: 0 }}>
          把玩法用人話寫死 —— 回合結構、方塊耐久、武器剋制、計分規則。agent 照著實作，而不是自由發揮。
        </p>
        <div
          style={{
            marginTop: 30,
            padding: '24px 28px',
            background: LIGHT.surface,
            border: `1px solid ${LIGHT.border}`,
            borderLeft: `5px solid ${LIGHT.accent}`,
            borderRadius: 8,
          }}
        >
          <div style={{ fontFamily: MONO, fontSize: 30, fontWeight: 700, color: LIGHT.text }}>
            ρ(r) = 1.20 − 0.40 × 0.85<sup style={{ fontSize: 17 }}>(r−5)</sup>
          </div>
          <div style={{ fontSize: 25, color: LIGHT.muted, marginTop: 14, lineHeight: 1.45 }}>
            要求清除速率漸近 1.20、永遠到不了 —— 難度無限上升卻永不暴衝，玩家輸在技巧用盡而非遊戲耍賴。
          </div>
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <PointCard scope={LIGHT} n="01" title="回合迴圈" body="BUILD → ATTACK，清光進下一關，逾時 Game Over" />
        <PointCard scope={LIGHT} n="02" title="剋制矩陣" body="沙 / 珊瑚 / 貝殼 / 礁石 / 水母 × 劍 / 槍 / 無人機" />
        <PointCard scope={LIGHT} n="03" title="無「贏」終局" body="生存計分，撐到 Reached Round N、刷新最高紀錄" />
      </div>
    </div>
    <Footer scope={LIGHT} label="設計架構 · GDD" />
  </div>
);

// ─── 06 · TDD (light, two-col) ────────────────────────────────────────────────
const TddPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="設計架構 · 給 agent「怎麼實作」"
      title="TDD — Technical Design Document"
    />
    <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 60, marginTop: 44, alignItems: 'center' }}>
      <div>
        <p style={{ fontSize: 33, fontWeight: 480, lineHeight: 1.6, color: LIGHT.text, margin: 0 }}>
          把 GDD 翻成技術決策 —— 引擎、ECS 切分、每個 system 的職責、測試策略。agent 不必猜架構。
        </p>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginTop: 30 }}>
          <Chip scope={LIGHT}>Java 21</Chip>
          <Chip scope={LIGHT}>jMonkeyEngine 3.9</Chip>
          <Chip scope={LIGHT}>Zay-ES 1.6</Chip>
          <Chip scope={LIGHT}>Lemur UI</Chip>
          <Chip scope={LIGHT}>JUnit 5</Chip>
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <PointCard scope={LIGHT} n="01" title="AppState 狀態機" body="Menu ⇄ HowTo → Gameplay → GameEnd，各畫面自管輸入" />
        <PointCard scope={LIGHT} n="02" title="ECS 切分" body="9 個 component × ~15 個 system，共讀寫一份 EntityData" />
        <PointCard scope={LIGHT} n="03" title="Headless 測試" body="所有遊戲邏輯不開視窗、不依賴 render thread 就能測" />
      </div>
    </div>
    <Footer scope={LIGHT} label="設計架構 · TDD" />
  </div>
);

// ─── 07a · AGENTS.md — commands + conventions (light) ─────────────────────────
const RuleRow = ({ scope, k, children }: { scope: Scope; k: string; children: React.ReactNode }) => (
  <div style={{ display: 'flex', gap: 16, alignItems: 'baseline', padding: '13px 0', borderTop: `1px solid ${scope.border}` }}>
    <span style={{ fontFamily: MONO, fontSize: 22, color: scope.accent, fontWeight: 700, whiteSpace: 'nowrap', width: 118, flexShrink: 0 }}>{k}</span>
    <span style={{ fontSize: 26, fontWeight: 460, color: scope.text, lineHeight: 1.36 }}>{children}</span>
  </div>
);

const AgentsCommandsPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="設計架構 · 給 agent「怎麼動手」"
      title="AGENTS.md — 規範寫死，agent 不臨場發明"
      sub="CLAUDE.md 只是 symlink，真正內容一份就好：環境、指令、編碼規範全講死。"
    />
    <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.05fr 1fr', gap: 56, marginTop: 36, alignItems: 'center' }}>
      <div>
        <div style={{ fontSize: 23, fontWeight: 700, color: LIGHT.muted, letterSpacing: '0.04em', textTransform: 'uppercase', marginBottom: 14 }}>
          Common commands
        </div>
        <CodeWin file="bash · 一切都在 nix develop 裡" badge="dev shell">
          <C c={T.key}>$</C> <C c={T.fn}>nix</C> develop{'        '}<C c={T.com} i>{'# 先進 dev shell'}</C>{'\n'}
          <C c={T.key}>$</C> <C c={T.fn}>./gradlew</C> build{'      '}<C c={T.com} i>{'# 編譯'}</C>{'\n'}
          <C c={T.key}>$</C> <C c={T.fn}>./gradlew</C> test{'       '}<C c={T.com} i>{'# 必須全綠 0 fail'}</C>{'\n'}
          <C c={T.key}>$</C> <C c={T.fn}>./gradlew</C> clean{'      '}<C c={T.com} i>{'# 清掉 build/'}</C>{'\n'}
          <C c={T.key}>$</C> <C c={T.fn}>./gradlew</C> test <C c={T.str}>--tests</C> <C c={T.str}>"*DifficultyCurveTest"</C>
        </CodeWin>
      </div>
      <div>
        <div style={{ fontSize: 23, fontWeight: 700, color: LIGHT.muted, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
          Code conventions
        </div>
        <div style={{ marginTop: 6, borderBottom: `1px solid ${LIGHT.border}` }}>
          <RuleRow scope={LIGHT} k="lang">Java 21 —— records · sealed · pattern matching · text block</RuleRow>
          <RuleRow scope={LIGHT} k="pkg">根套件統一 com.ourbreak</RuleRow>
          <RuleRow scope={LIGHT} k="style">4-space 縮排 · K&amp;R 大括號 · 行長 ≤120</RuleRow>
          <RuleRow scope={LIGHT} k="naming">類別 UpperCamel · 方法 lowerCamel · 常數 UPPER_SNAKE</RuleRow>
          <RuleRow scope={LIGHT} k="null">可空回傳用 Optional&lt;T&gt;，public API 不回 null</RuleRow>
          <RuleRow scope={LIGHT} k="test">JUnit 5 · 一類一 *Test · Arrange–Act–Assert</RuleRow>
        </div>
      </div>
    </div>
    <Footer scope={LIGHT} label="設計架構 · AGENTS.md（規範）" />
  </div>
);

// ─── 07b · AGENTS.md — agent rules (light) ────────────────────────────────────
const AgentsRulesPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="設計架構 · agent 的家規"
      title="Agent 守則 —— 8 條硬規則"
      sub="不是建議，是 pre-commit hook 會擋下的硬規則。違反就 commit 不了。"
    />
    <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1fr 1fr', columnGap: 60, marginTop: 32, alignItems: 'center' }}>
      <div style={{ borderBottom: `1px solid ${LIGHT.border}` }}>
        <RuleRow scope={LIGHT} k="env">一律在 nix develop 裡跑，不賭系統 Java / Gradle</RuleRow>
        <RuleRow scope={LIGHT} k="test">./gradlew test 全綠才算完成，0 fail / 0 error</RuleRow>
        <RuleRow scope={LIGHT} k="commit">Conventional Commits，一律走 /commit skill</RuleRow>
        <RuleRow scope={LIGHT} k="devlog">每個 commit 附一篇 devlog，hook 強制</RuleRow>
      </div>
      <div style={{ borderBottom: `1px solid ${LIGHT.border}` }}>
        <RuleRow scope={LIGHT} k="flake">flake.lock 釘死工具鏈，不問不動 flake.nix</RuleRow>
        <RuleRow scope={LIGHT} k="deps">加依賴先問，優先標準庫 / 知名庫</RuleRow>
        <RuleRow scope={LIGHT} k="progress">每 commit 更新 milestone 表，跟程式同包</RuleRow>
        <RuleRow scope={LIGHT} k="artefacts">build/ · *.class · *.jar 一律不進 git</RuleRow>
      </div>
    </div>
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        gap: 16,
        padding: '20px 28px',
        background: LIGHT.surface,
        border: `1px solid ${LIGHT.border}`,
        borderLeft: `5px solid ${LIGHT.accent}`,
        borderRadius: 8,
        fontSize: 26,
        color: LIGHT.text,
      }}
    >
      <span style={{ fontFamily: MONO, fontSize: 22, fontWeight: 700, color: LIGHT.accent }}>.claude/hooks/</span>
      pre-commit 雙閘 —— test gate + devlog gate，靠機制把關，不靠 agent 自覺。
    </div>
    <Footer scope={LIGHT} label="設計架構 · AGENTS.md（守則）" />
  </div>
);

// ─── 08 · OpenSpec SDD (light, code window) ───────────────────────────────────
const OpenSpecPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="設計架構 · 反 vibe 招牌動作"
      title="OpenSpec — Spec-Driven Design"
      sub="寫 code 前先寫 spec。一個 change = 一整包文件，用 SDD 取代 plan mode。"
    />
    <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '1.15fr 1fr', gap: 60, marginTop: 40, alignItems: 'center' }}>
      <CodeWin file="openspec/changes/2026-06-16-shell-splitting/" fontSize={24}>
        <C c={T.punc}>├─</C> <C c={T.fn} b>proposal.md</C>      <C c={T.com} i>{'# 為什麼要做'}</C>{'\n'}
        <C c={T.punc}>├─</C> <C c={T.fn} b>design.md</C>        <C c={T.com} i>{'# 怎麼做'}</C>{'\n'}
        <C c={T.punc}>├─</C> <C c={T.fn} b>tasks.md</C>         <C c={T.com} i>{'# 拆成可勾選步驟'}</C>{'\n'}
        <C c={T.punc}>└─</C> <C c={T.key}>specs/</C>{'\n'}
        {'   '}<C c={T.punc}>├─</C> weapon-damage/<C c={T.fn}>spec.md</C>{'\n'}
        {'   '}<C c={T.punc}>└─</C> shell-splitting/<C c={T.fn}>spec.md</C>
      </CodeWin>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <PointCard scope={LIGHT} n="→" title="先規格、後實作" body="需求與行為先成文，agent 照 spec 寫、不自由發揮" />
        <PointCard scope={LIGHT} n="→" title="一個 change 一包" body="proposal + design + tasks + specs/ 綁在一起 review" />
        <PointCard scope={LIGHT} n="12+" title="changes/archive/" body="已累積十多包歸檔的 change，每包都可回溯" />
      </div>
    </div>
    <Footer scope={LIGHT} label="設計架構 · OpenSpec (SDD)" />
  </div>
);

// ─── 09 · Devlog (dark, timeline) ─────────────────────────────────────────────
const LogRow = ({
  time,
  slug,
  frozen,
}: {
  time: string;
  slug: string;
  frozen?: boolean;
}) => (
  <div style={{ display: 'flex', alignItems: 'center', gap: 26, padding: '13px 0' }}>
    <span style={{ width: 13, height: 13, borderRadius: 999, background: frozen ? DARK.cool : DARK.accent, flexShrink: 0 }} />
    <span style={{ fontFamily: MONO, fontSize: 22, color: DARK.muted, width: 130 }}>{time}</span>
    <span style={{ fontFamily: MONO, fontSize: 24, color: DARK.text, fontWeight: 500 }}>{slug}</span>
    {frozen && (
      <span style={{ marginLeft: 'auto', fontSize: 18, fontWeight: 700, color: DARK.cool, letterSpacing: '0.08em' }}>
        FROZEN
      </span>
    )}
  </div>
);

const DevlogPage: Page = () => (
  <div style={contentPage(DARK)}>
    <PageHead
      scope={DARK}
      eyebrow="開發流程 · 留下不可變的軌跡"
      title="Devlog — 每次 commit 一篇"
      sub="YYYYMMDD/hh-mm-ss-slug.md。一旦 commit 就凍結，錯了只能在後面的條目修正。"
    />
    <div style={{ display: 'grid', gridTemplateColumns: '1.25fr 1fr', gap: 64, marginTop: 40, alignItems: 'start' }}>
      <div
        style={{
          background: DARK.surface,
          border: `1px solid ${DARK.border}`,
          borderRadius: 10,
          padding: '20px 30px',
        }}
      >
        <div style={{ fontFamily: MONO, fontSize: 20, color: DARK.muted, marginBottom: 6 }}>devlog/20260616/</div>
        <LogRow time="14:33:07" slug="shell-splitting-and-gun" frozen />
        <LogRow time="16:35:01" slug="weapon-icon-hud" frozen />
        <LogRow time="18:11:53" slug="beach-floor-environment" frozen />
        <LogRow time="19:18:39" slug="mascot-crab" frozen />
        <LogRow time="13:20:33" slug="endless-survival-mode" frozen />
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <PointCard scope={DARK} n="→" title="逼 agent 留記憶" body="每個 commit 寫下做了什麼、為何這樣決定" />
        <PointCard scope={DARK} n="→" title="凍結不可改" body="過去是時間切片，不回頭潤稿、不補洞" />
        <PointCard scope={DARK} n="⛓" title="hook 強制" body="pre-commit 沒附 devlog 就擋下，不靠自覺" />
      </div>
    </div>
    <Footer scope={DARK} label="開發流程 · Devlog" />
  </div>
);

// ─── 10 · Milestone (light, progress track) ───────────────────────────────────
const Milestone = ({
  id,
  label,
  state,
}: {
  id: string;
  label: string;
  state: 'done' | 'next' | 'todo';
}) => {
  const done = state === 'done';
  const next = state === 'next';
  return (
    <div
      style={{
        flex: 1,
        padding: '26px 20px',
        background: done ? LIGHT.surface : LIGHT.bg,
        border: `1px solid ${next ? LIGHT.accent : LIGHT.border}`,
        borderRadius: 8,
        display: 'flex',
        flexDirection: 'column',
        gap: 12,
        opacity: state === 'todo' ? 0.55 : 1,
      }}
    >
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontFamily: MONO, fontSize: 31, fontWeight: 700, color: next ? LIGHT.accent : LIGHT.text }}>{id}</span>
        <span style={{ fontSize: 26, color: done ? LIGHT.accent : next ? LIGHT.accent : LIGHT.muted, fontWeight: 700 }}>
          {done ? '✓' : next ? '◆' : '○'}
        </span>
      </div>
      <span style={{ fontSize: 23, fontWeight: 500, color: LIGHT.muted, lineHeight: 1.34 }}>{label}</span>
    </div>
  );
};

const MilestonePage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="開發流程 · 一次只推進一格"
      title="Milestone — 切成可驗收的關卡"
      sub="關鍵路徑 M1 → M2 → M3 → M4 才是第一個能玩的版本。每個 commit 同步更新進度表。"
    />
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 16, marginTop: 40 }}>
      <div style={{ display: 'flex', gap: 14 }}>
        <Milestone id="M0" label="jME3 + Zay-ES 骨架" state="done" />
        <Milestone id="M1" label="核心回合迴圈" state="done" />
        <Milestone id="M2" label="方塊與破壞" state="done" />
        <Milestone id="M3" label="NPC 建造者" state="done" />
        <Milestone id="M4" label="第一人稱可玩" state="done" />
      </div>
      <div style={{ display: 'flex', gap: 14 }}>
        <Milestone id="M5" label="方塊特效" state="done" />
        <Milestone id="M6" label="UI / HUD" state="done" />
        <Milestone id="M7" label="平衡調校" state="done" />
        <Milestone id="M8" label="美術 / 延伸（next）" state="next" />
        <div style={{ flex: 1 }} />
      </div>
    </div>
    <Footer scope={LIGHT} label="開發流程 · Milestone" />
  </div>
);

// ─── 11 · 成果檢驗 (light, Auto vs Manual) ────────────────────────────────────
const VerifyPanel = ({
  scope,
  tag,
  title,
  children,
  accent,
}: {
  scope: Scope;
  tag: string;
  title: string;
  children: React.ReactNode;
  accent: boolean;
}) => (
  <div
    style={{
      flex: 1,
      padding: '30px 32px',
      background: scope.surface,
      border: `1px solid ${accent ? scope.accent : scope.border}`,
      borderRadius: 10,
      display: 'flex',
      flexDirection: 'column',
    }}
  >
    <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
      <span style={{ width: 12, height: 12, borderRadius: 999, background: accent ? scope.accent : scope.cool, display: 'block' }} />
      <span style={{ fontFamily: MONO, fontSize: 21, fontWeight: 700, color: scope.muted, letterSpacing: '0.04em' }}>{tag}</span>
    </div>
    <h3 style={{ fontSize: 38, fontWeight: 800, margin: '14px 0 22px', lineHeight: 1.18 }}>{title}</h3>
    {children}
  </div>
);

const VLine = ({ scope, children }: { scope: Scope; children: React.ReactNode }) => (
  <div style={{ display: 'flex', gap: 14, alignItems: 'baseline', padding: '11px 0', borderTop: `1px solid ${scope.border}` }}>
    <span style={{ color: scope.accent, fontWeight: 700, fontSize: 24 }}>·</span>
    <span style={{ fontSize: 25, fontWeight: 460, color: scope.text, lineHeight: 1.4 }}>{children}</span>
  </div>
);

const VerifyPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="成果檢驗 · 過不了就 commit 不了"
      title="自動測試 ‖ 手動測試"
    />
    <div style={{ display: 'flex', gap: 30, marginTop: 40, alignItems: 'stretch' }}>
      <VerifyPanel scope={LIGHT} tag="AUTO · 20 個 JUnit" title="Headless 邏輯測試" accent>
        <div style={{ borderBottom: `1px solid ${LIGHT.border}` }}>
          <VLine scope={LIGHT}>DifficultyCurveTest — 鎖死難度永遠遞增、不暴衝</VLine>
          <VLine scope={LIGHT}>VillageGeneratorTest — 種子可重現、受預算上限</VLine>
          <VLine scope={LIGHT}>VictorySystemTest — 清光進關 / 逾時 Game Over</VLine>
          <VLine scope={LIGHT}>不開視窗、不依賴 render thread，CI 可跑</VLine>
        </div>
      </VerifyPanel>
      <VerifyPanel scope={LIGHT} tag="MANUAL · 人在迴圈" title="手感與體驗" accent={false}>
        <div style={{ borderBottom: `1px solid ${LIGHT.border}` }}>
          <VLine scope={LIGHT}>實機 playtest：手感、節奏、視覺破壞回饋</VLine>
          <VLine scope={LIGHT}>跨平台：原生 Windows 捕獲式 FPS、WSLg workaround</VLine>
          <VLine scope={LIGHT}>錄影留證：每版 gameplay demo 存檔對照</VLine>
          <VLine scope={LIGHT}>自動測不到的「好不好玩」由人來判</VLine>
        </div>
      </VerifyPanel>
    </div>
    <div
      style={{
        marginTop: 28,
        padding: '18px 28px',
        background: LIGHT.raised,
        border: `1px solid ${LIGHT.border}`,
        borderLeft: `5px solid ${LIGHT.accent}`,
        borderRadius: 8,
        fontSize: 26,
        fontWeight: 600,
        color: LIGHT.text,
      }}
    >
      pre-commit test gate：<span style={{ color: LIGHT.accent }}>./gradlew test</span> 零失敗才進得了 commit —— 壞掉的程式碼根本進不了 repo。
    </div>
    <Footer scope={LIGHT} label="成果檢驗 · Auto + Manual" />
  </div>
);

// ─── 12 · ECS (light, EntityData hub + systems) ───────────────────────────────
const SysTile = ({ name, role }: { name: string; role: string }) => (
  <div
    style={{
      padding: '22px 22px',
      background: LIGHT.surface,
      border: `1px solid ${LIGHT.border}`,
      borderRadius: 8,
    }}
  >
    <div style={{ fontFamily: MONO, fontSize: 25, fontWeight: 700, color: LIGHT.text }}>{name}</div>
    <div style={{ fontSize: 22, color: LIGHT.muted, marginTop: 6, lineHeight: 1.32 }}>{role}</div>
  </div>
);

const EcsPage: Page = () => (
  <div style={contentPage(LIGHT)}>
    <PageHead
      scope={LIGHT}
      eyebrow="遊戲架構 · 框架長出來的成品"
      title="ECS — 一份資料，眾系統共用"
      sub="Zay-ES：9 個 component × ~15 個 system，每幀讀寫同一份 EntityData。資料與行為徹底分離。"
    />
    <div style={{ flex: 1, display: 'grid', gridTemplateColumns: '320px 1fr', gap: 54, marginTop: 36, alignItems: 'center' }}>
      <div
        style={{
          padding: '40px 28px',
          background: LIGHT.accent,
          color: LIGHT.onAccent,
          borderRadius: 12,
          textAlign: 'center',
        }}
      >
        <div style={{ fontFamily: MONO, fontSize: 34, fontWeight: 800 }}>EntityData</div>
        <div style={{ fontSize: 23, opacity: 0.85, margin: '14px 0 22px' }}>單一共享狀態</div>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8, justifyContent: 'center' }}>
          {['Position', 'Block', 'Weapon', 'Round', 'Phase', 'Effect', 'Mascot', 'Model', 'Result'].map((c) => (
            <span key={c} style={{ fontFamily: MONO, fontSize: 19, background: 'rgba(232,225,219,0.16)', padding: '4px 11px', borderRadius: 6 }}>
              {c}
            </span>
          ))}
        </div>
      </div>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 16 }}>
        <SysTile name="RoundSystem" role="回合 / 階段 / 計時" />
        <SysTile name="WeaponSystem" role="命中判定 + 扣血" />
        <SysTile name="NpcBuilderSystem" role="程序化村莊放塊" />
        <SysTile name="BlockEffectSystem" role="無人機球 / 珊瑚減速" />
        <SysTile name="CoralGrowthSystem" role="珊瑚再生蔓延" />
        <SysTile name="VictorySystem" role="進關 / Game Over" />
        <SysTile name="ModelViewState" role="渲染方塊 entity" />
        <SysTile name="PoisonState" role="水母中毒彩虹化" />
        <SysTile name="HudState" role="HUD 即時更新" />
      </div>
    </div>
    <Footer scope={LIGHT} label="遊戲架構 · ECS" />
  </div>
);

// ─── 13 · Logic Flow (dark, state machine + round loop) ───────────────────────
const FlowBox = ({
  title,
  sub,
  accent,
}: {
  title: string;
  sub?: string;
  accent?: boolean;
}) => (
  <div
    style={{
      padding: '22px 30px',
      background: accent ? DARK.accent : DARK.surface,
      color: accent ? DARK.onAccent : DARK.text,
      border: `1px solid ${accent ? DARK.accent : DARK.border}`,
      borderRadius: 9,
      textAlign: 'center',
      minWidth: 186,
    }}
  >
    <div style={{ fontSize: 31, fontWeight: 760 }}>{title}</div>
    {sub && <div style={{ fontSize: 21, opacity: 0.82, marginTop: 5 }}>{sub}</div>}
  </div>
);

const Arrow = ({ label }: { label?: string }) => (
  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 4, color: DARK.muted }}>
    {label && <span style={{ fontSize: 19, color: DARK.cool, whiteSpace: 'nowrap' }}>{label}</span>}
    <span style={{ fontSize: 34, lineHeight: 1, color: DARK.accentSoft }}>→</span>
  </div>
);

const LogicFlowPage: Page = () => (
  <div style={contentPage(DARK)}>
    <PageHead
      scope={DARK}
      eyebrow="遊戲架構 · 螢幕與回合怎麼跑"
      title="Logic Flow — AppState 機 + 回合迴圈"
    />
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 50, marginTop: 36 }}>
      <div>
        <div style={{ fontFamily: MONO, fontSize: 22, color: DARK.cool, marginBottom: 18 }}>// 畫面狀態機（jME AppState）</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, flexWrap: 'wrap' }}>
          <FlowBox title="Main Menu" />
          <Arrow label="Start" />
          <FlowBox title="Gameplay" accent />
          <Arrow label="result" />
          <FlowBox title="Game Over" sub="Reached Round N" />
          <Arrow label="Restart" />
          <FlowBox title="Main Menu" />
        </div>
      </div>
      <div>
        <div style={{ fontFamily: MONO, fontSize: 22, color: DARK.cool, marginBottom: 18 }}>// 每回合迴圈（無限生存）</div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, flexWrap: 'wrap' }}>
          <FlowBox title="BUILD" sub="NPC 程序化堆牆" />
          <Arrow label="牆蓋完" />
          <FlowBox title="ATTACK" sub="玩家限時清牆" accent />
          <Arrow label="清光 → 更難一關" />
          <FlowBox title="Round N+1" />
        </div>
        <div
          style={{
            marginTop: 26,
            display: 'inline-flex',
            alignItems: 'center',
            gap: 14,
            padding: '16px 26px',
            border: `1px dashed ${DARK.border}`,
            borderRadius: 8,
            fontSize: 26,
            color: DARK.muted,
          }}
        >
          <span style={{ color: DARK.accent, fontWeight: 700 }}>⟂</span>
          ATTACK 計時器歸零仍有牆 → Game Over（score = 撐到的回合）
        </div>
      </div>
    </div>
    <Footer scope={DARK} label="遊戲架構 · Logic Flow" />
  </div>
);

// ─── 14 · Demo → QA (dark, play cue + repo + QA) ──────────────────────────────
const DemoPage: Page = () => (
  <div style={{ ...page(DARK), display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 130px' }}>
    <AnchorBlocks />
    <Eyebrow color={DARK.accent}>實機錄影</Eyebrow>
    <div style={{ display: 'flex', alignItems: 'center', gap: 30, margin: '28px 0 0' }}>
      <div
        style={{
          width: 132,
          height: 132,
          borderRadius: 999,
          background: DARK.accent,
          color: DARK.onAccent,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          flexShrink: 0,
        }}
      >
        <span style={{ fontSize: 54, marginLeft: 10 }}>▶</span>
      </div>
      <h1 style={{ fontSize: 116, fontWeight: 900, letterSpacing: '-0.02em', margin: 0, lineHeight: 1 }}>Demo</h1>
    </div>
    <p style={{ fontSize: 36, fontWeight: 500, color: DARK.muted, margin: '34px 0 0', maxWidth: 1280, lineHeight: 1.5 }}>
      crab-village 圍城、太陽 / 陰影 / god rays 光影，與第一人稱手持武器 —— 框架接住，agent 才做得出來。
    </p>
    <div style={{ display: 'flex', gap: 14, marginTop: 36 }}>
      <Chip scope={DARK}>WASD 移動</Chip>
      <Chip scope={DARK}>1 劍 · 2 槍 · 3 無人機</Chip>
      <Chip scope={DARK}>清光進關 · 逾時 Game Over</Chip>
    </div>
    <a
      href="https://youtu.be/97LbCJacOzY"
      target="_blank"
      rel="noopener noreferrer"
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: 22,
        marginTop: 40,
        padding: '26px 52px',
        background: DARK.accent,
        color: DARK.onAccent,
        borderRadius: 14,
        fontSize: 44,
        fontWeight: 820,
        letterSpacing: '-0.01em',
        textDecoration: 'none',
        boxShadow: '0 18px 44px rgba(20,16,20,0.32)',
        alignSelf: 'flex-start',
      }}
    >
      <span style={{ fontSize: 40, lineHeight: 1 }}>▶</span>
      觀看 Demo 影片
    </a>
    <div style={{ marginTop: 44 }}>
      <Rule color={DARK.accent} width={180} />
    </div>
    <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', marginTop: 30 }}>
      <div style={{ fontFamily: MONO, fontSize: 24, color: DARK.cool }}>github.com/iceice666/ourbreak</div>
      <div style={{ fontSize: 48, fontWeight: 820, color: DARK.text }}>
        Q<span style={{ color: DARK.accent }}>&amp;</span>A
      </div>
    </div>
    <Footer scope={DARK} label="Demo → Q&A" />
  </div>
);

export const meta: SlideMeta = {
  title: '如何避免 vibe 出垃圾 — ourbreak',
  createdAt: '2026-06-19T00:34:08.129Z',
};

export default [
  Cover,
  Thesis,
  Agenda,
  DesignSection,
  GddPage,
  TddPage,
  AgentsCommandsPage,
  AgentsRulesPage,
  OpenSpecPage,
  DevlogPage,
  MilestonePage,
  VerifyPage,
  EcsPage,
  LogicFlowPage,
  DemoPage,
] satisfies Page[];
