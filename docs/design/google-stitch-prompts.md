# Google Stitch 디자인 프롬프트 - 부산 스마트 계량 모바일 앱

## 공통 디자인 시스템

> **앱 컨셉**: "Modern Industrial Intelligence" - 항만 물류 현장에서 사용하는 산업용 모바일 앱
> **타겟 유저**: 트럭 운전자 (장갑 착용, 야외 직사광선, 빠른 조작 필요)
> **디바이스**: Android/iOS 스마트폰 (390 x 844px 기준)

### 컬러 팔레트
- Background: Deep Navy `#0B1120`
- Surface/Card: Charcoal `#1E293B`
- Primary: Neon Cyan `#06B6D4`
- Success: Emerald `#10B981`
- Warning: Amber `#F59E0B`
- Error: Rose `#F43F5E`
- Text Primary: `#F8FAFC`
- Text Secondary: `#94A3B8`
- Border: `#334155`

### 타이포그래피
- Font: Inter (또는 Pretendard)
- Heading: 24-28px Bold
- Title: 18-20px SemiBold
- Body: 14-16px Regular
- Caption: 12px Regular

---

## 1. 로그인 화면 (Login Screen)

```
Design a premium dark-themed mobile login screen for a smart vehicle weighing system app used at Busan Port, Korea.

Screen size: 390 x 844px (iPhone 14 / standard Android)
Background: Deep navy gradient from #0B1120 (top) to #0F172A (bottom)

Layout (top to bottom):
- Status bar area (44px)
- Top spacing (80px)

LOGO SECTION (centered):
- A minimal geometric scale/weight icon using thin cyan (#06B6D4) line art, 64x64px
- Below it, subtle horizontal line accent in cyan, 40px wide, 2px thick
- App name "BUSAN SMART WEIGHING" in white (#F8FAFC), 13px, letter-spacing 3px, uppercase
- Korean subtitle "부산 스마트 계량" in slate (#94A3B8), 14px, below the English name

Spacing: 56px

LOGIN FORM (inside a card with subtle glassmorphism effect):
- Card: #1E293B background, 20px border-radius, subtle white border (8% opacity), backdrop blur
- Inner padding: 24px
- "아이디" input field:
  - Dark input (#0F172A background), rounded 12px
  - Left icon: user outline in slate
  - Placeholder text in dark slate
  - Height: 52px
- 16px spacing
- "비밀번호" input field:
  - Same style as ID field
  - Left icon: lock outline
  - Right icon: eye/visibility toggle
  - Height: 52px
- 16px spacing
- Row: toggle switch for "자동 로그인" (auto-login) with cyan toggle, white label text 14px

Spacing: 24px

BUTTONS:
- Primary button "로그인": Full width, cyan (#06B6D4) background, white bold text 16px, height 52px, rounded 14px, subtle glow/shadow effect underneath
- 12px spacing
- Secondary button "안전 로그인 (OTP)": Full width, transparent with 1px cyan border, cyan text 14px, height 48px, rounded 14px, left shield icon

FOOTER (bottom of screen, centered):
- "v1.0.0" in very faint slate text (#475569), 12px
- 32px bottom padding

Style notes:
- Overall aesthetic: sleek, futuristic control panel vibe
- No shadows except subtle glow on primary button
- Clean spacing, high contrast for outdoor readability
- Subtle animated gradient or particle effect in the background (suggest with a faint dot grid pattern)
```

---

## 2. 메인 홈 화면 (Home / Dashboard Screen)

```
Design a dark-themed mobile home dashboard screen for a smart vehicle weighing system app. This is the main hub after login.

Screen size: 390 x 844px
Background: #0B1120

TOP BAR (sticky):
- Height: 56px
- Left: Hamburger menu icon in white
- Center: Current tab title "배차" in white, 18px semibold
- Right: User chip/badge showing "김운전 | 기사" with a small person icon, charcoal (#1E293B) pill background, rounded 20px, white text 12px

GREETING SECTION (below top bar, 16px padding):
- Card with glassmorphism: #1E293B bg, subtle border, 16px rounded
- Inside:
  - Row: Cyan dot indicator (pulsing animation suggested) + "현재 상태" label in slate 12px
  - Large text: "계량 대기중" in white 20px bold (or "진행중" in cyan, "완료" in emerald depending on state)
  - Below: "오늘의 배차: 3건 | 완료: 1건" in slate 14px

QUICK ACTION GRID (below greeting, 16px gap):
- 2x2 grid of action cards, each card:
  - #1E293B background, 16px rounded, subtle border #334155
  - Size: approximately equal squares filling width with 12px gap
  - Each card contains:
    - Top-left: Colored icon in a circle (40x40px, icon color on 15% opacity background)
    - Bottom: Action label in white 14px semibold
    - Bottom: Count or status in slate 12px

- Card 1: "배차 조회" - truck icon, cyan accent, "3건 대기"
- Card 2: "계량 진행" - scale icon, amber accent, "1건 진행중"
- Card 3: "계량표" - receipt icon, emerald accent, "오늘 2건"
- Card 4: "알림" - bell icon, rose accent, red badge "3" for unread count

RECENT ACTIVITY SECTION:
- Section header: "최근 활동" in white 16px semibold, right side "전체보기 >" in cyan 13px
- 2-3 activity cards stacked vertically:
  - Each card: #1E293B bg, 12px rounded, 16px padding
  - Left: Status dot (emerald for complete, amber for in-progress, slate for waiting)
  - Content: dispatch number in white 14px bold, vehicle number + company in slate 13px
  - Right: time "14:30" in slate 12px, and chevron-right icon
  - Thin bottom border #334155 separating items

BOTTOM NAVIGATION BAR:
- Height: 80px (including safe area)
- Background: #0F172A with top border #1E293B (1px)
- 5 tabs equally spaced:
  1. Truck icon + "배차" (selected: cyan icon + cyan text + subtle cyan dot above)
  2. Scale icon + "계량"
  3. Receipt icon + "계량표"
  4. Clock/history icon + "이력"
  5. Three dots icon + "더보기"
- Unselected: #64748B icon + text
- Selected: #06B6D4 icon + text, with a small 4px cyan dot indicator above the icon
- Text: 11px

Style notes:
- Futuristic dashboard feel, information density balanced with readability
- All cards have very subtle hover/pressed states (slightly lighter background)
- High contrast text on dark backgrounds
- Icons should be outlined/thin line style, not filled
```

---

## 3. 배차 목록 화면 (Dispatch List Screen)

```
Design a dark-themed mobile dispatch list screen for a vehicle weighing app at a port facility.

Screen size: 390 x 844px
Background: #0B1120

TOP SECTION:
- Seamless with bottom nav (this is a tab within the main shell)
- Filter chips row, horizontally scrollable:
  - Chip style: pill shape, 32px height, 12px rounded
  - "전체" (selected): cyan bg #06B6D4, white text
  - "대기": charcoal bg #1E293B, slate text, slate border
  - "진행중": charcoal bg, slate text
  - "완료": charcoal bg, slate text
  - 8px gap between chips

- Summary bar: subtle divider line, then row showing:
  - "오늘 배차 5건" in white 14px
  - Right side: sort icon + "최신순" in slate 13px

DISPATCH CARDS (scrollable list, 12px gap between cards):

Card design (each dispatch):
- Background: #1E293B
- Border: 1px #334155
- Border-radius: 16px
- Padding: 20px
- Left accent: 4px wide vertical color bar on the left edge indicating status:
  - Waiting: #475569 (slate)
  - In Progress: #06B6D4 (cyan)
  - Completed: #10B981 (emerald)

Card content layout:
- TOP ROW:
  - Left: Dispatch number "D-2026-0142" in white 16px semibold
  - Right: Status badge pill - e.g., "진행중" with blue background at 15% opacity, blue text 12px, or "대기" with slate styling

- MIDDLE SECTION (12px below top):
  - Row 1: Truck icon (14px, slate) + "차량" label (slate 12px, 40px wide) + "부산 12가 3456" (white 14px)
  - Row 2: Building icon + "업체" + "동국제강"
  - Row 3: Package icon + "품목" + "철강 코일"
  - Row 4: Route icon + "경로" + "부산신항 → 동국 창원공장"
  - All rows have 6px vertical spacing, icons are #64748B

- BOTTOM ROW (separated by thin #334155 divider, 12px top padding):
  - Left: Calendar icon (12px) + "2026-01-29 08:30" in slate 12px
  - Right: Chevron-right icon in slate, suggesting tappable

EMPTY STATE (when no dispatches):
- Centered vertically
- Large truck outline icon (64px) in very faint slate (#334155)
- "오늘 배차 내역이 없습니다." in slate 16px
- "아래로 당겨 새로고침하세요." in darker slate 14px
- Pull-to-refresh indicator in cyan

Style notes:
- Cards should feel like data panels in a control room
- Subtle card elevation via shadow or border
- The left accent bar is the key visual differentiator for status
- Spacing should be generous for touch targets (driver with gloves)
```

---

## 4. 계량 진행 화면 (Weighing Progress Screen)

```
Design a dark-themed mobile weighing progress screen showing real-time vehicle weighing status. This is the core operational screen of the app.

Screen size: 390 x 844px
Background: #0B1120

WEIGHING PROGRESS CARD (main card, takes most of screen):
- Background: #1E293B with subtle glassmorphism border
- Border-radius: 20px
- Margin: 16px horizontal, 16px top
- Padding: 24px

Card Header:
- Left column:
  - Dispatch number "D-2026-0142" in white 18px bold
  - Below: "부산 12가 3456 | 동국제강" in slate 14px
- Right: Status badge - "계량중" with animated pulsing cyan dot + cyan text on dark cyan bg (15% opacity), rounded pill

CIRCULAR PROGRESS INDICATOR (centered, hero element):
- Large circle, 180px diameter
- Thick progress ring (8px stroke):
  - Background ring: #334155
  - Progress arc: cyan (#06B6D4) gradient, animated
  - For "계량중" state: arc at ~50%, animated rotation/pulse
- Center of circle:
  - Current weight value "24,580" in white 36px bold (monospace/tabular digits)
  - "kg" in slate 16px below the number
  - Small real-time indicator dot (pulsing cyan) if actively measuring

STEP PROGRESS BAR (below circle, 24px spacing):
- Horizontal stepped progress bar with 4 steps:
  - Step 1: "대기" - completed (emerald dot, emerald connecting line)
  - Step 2: "1차 계량" - completed (emerald)
  - Step 3: "2차 계량" - current (cyan, pulsing)
  - Step 4: "완료" - pending (slate/dim)
- Each step: circle dot (12px) + label below (12px text)
- Connected by lines between dots
- Completed steps: filled emerald circles, emerald text
- Current step: filled cyan circle with outer ring animation, cyan text
- Pending: empty circle with slate border, slate text

WEIGHT SUMMARY ROW (below progress bar, 20px spacing):
- Three columns in a dark card (#0F172A bg, 12px rounded):
  - "총중량" / "24,580 kg" (white)
  - Vertical divider (#334155)
  - "공차중량" / "8,200 kg" (white)
  - Vertical divider
  - "순중량" / "16,380 kg" (cyan, bold - this is the key value)
- Each column: label in slate 11px on top, value in white/cyan 15px bold below

TIMESTAMP SECTION:
- "1차 계량: 08:32" with clock icon
- "2차 계량: 진행중..." with animated dots
- Text: slate 13px, icons 14px

ACTION BUTTON (bottom of card or screen):
- For waiting state: Full width button "OTP 인증" in cyan, white text 16px bold, 52px height, pin icon left, rounded 14px, subtle glow
- For in-progress state: Button is disabled/grayed out showing "계량 진행중..."
- For completed state: "계량표 보기" in emerald green

COMPLETED STATE popup (overlay/modal concept):
- Checkmark animation (large emerald check in circle)
- "계량 완료!" in white 24px bold
- Summary of final weights
- "계량표 보기" button in emerald

Style notes:
- This screen should feel like a real-time monitoring dashboard
- The circular progress with weight value is the focal point
- Numbers should use tabular/monospace figures for stability during real-time updates
- Pulsing/breathing animations suggest live data
- High contrast for outdoor readability
```

---

## 5. OTP 인증 화면 (OTP Input Screen)

```
Design a dark-themed mobile OTP verification screen with a large numeric keypad. Users are truck drivers who may be wearing gloves.

Screen size: 390 x 844px
Background: #0B1120

TOP BAR:
- Back arrow (white) on left
- Center title: "OTP 인증" in white 18px semibold
- Clean, minimal

HEADER SECTION (centered, 24px padding):
- Shield/lock icon in cyan, 48px, with subtle glow effect
- 12px spacing
- Dispatch number "D-2026-0142" in white 16px semibold
- 8px spacing
- Instruction text: "OTP 코드 6자리를 입력하세요" in slate 15px

COUNTDOWN TIMER (centered):
- Pill-shaped container: dark cyan bg (#06B6D4 at 15% opacity), rounded 24px
- Inside: Timer icon (cyan, 18px) + "04:32" in cyan 20px bold (monospace/tabular digits)
- When under 1 minute: pill bg changes to rose at 15% opacity, text becomes rose (#F43F5E)
- When expired: "만료됨" in rose, timer-off icon

OTP CODE DISPLAY (centered, 20px spacing):
- 6 individual boxes in a row, horizontally centered
- Each box: 48x60px
- Empty state: #1E293B background, 1px #334155 border, 12px rounded
- Filled state: #06B6D4 at 10% bg, 2px cyan border, 12px rounded
  - Number displayed in white 28px bold, centered
- Current/active box: subtle cyan border pulse animation
- Boxes have 8px gap between them

Error message area:
- If error: rose text centered below boxes, 14px
- e.g., "OTP 인증에 실패했습니다. 다시 시도해주세요."

NUMERIC KEYPAD (bottom half of screen):
- Full-width, 24px horizontal padding
- 4 rows x 3 columns grid
- Each key button:
  - Width: fills 1/3 of available width minus gaps
  - Height: 64px (extra tall for gloved fingers)
  - Background: #1E293B
  - Border-radius: 16px
  - Gap between buttons: 8px
  - Number text: white 28px semibold, centered
  - Pressed state: slightly lighter bg (#2D3B50)

- Row 1: [1] [2] [3]
- Row 2: [4] [5] [6]
- Row 3: [7] [8] [9]
- Row 4: [C] [0] [<]
  - "C" (clear): Rose (#F43F5E) text on #1E293B bg
  - "0": same as number keys
  - "<" (backspace): Backspace icon in white on #1E293B bg

VERIFY BUTTON (below keypad, 16px padding):
- Full width, height 56px
- Active (6 digits entered): Cyan bg (#06B6D4), white "인증하기" text 16px bold, rounded 14px, subtle glow
- Inactive (less than 6 digits): Dark bg (#1E293B), slate text, no glow
- Loading state: White circular spinner on cyan bg

EXPIRED STATE:
- Below verify button: "OTP 재요청" text button with refresh icon in cyan

Style notes:
- Keypad must be the dominant element - drivers need large, easy-to-press buttons
- Timer creates urgency but shouldn't feel stressful
- The 6-box OTP display should feel satisfying as each digit fills in
- High contrast, minimal distractions
- Consider haptic feedback indication (subtle press effect on buttons)
```

---

## 6. 전자 계량표 상세 화면 (Digital Weighing Receipt / Slip Detail)

```
Design a dark-themed mobile digital weighing receipt screen. This replaces a paper receipt and should feel official yet modern.

Screen size: 390 x 844px
Background: #0B1120

TOP BAR:
- Back arrow (white) left
- Center: "계량표 상세" white 18px semibold
- Right: Share icon (white)

SCROLLABLE CONTENT:

RECEIPT HEADER CARD (hero section):
- Full width card, #1E293B bg, 20px rounded
- Top-center: Large receipt icon (40px) in cyan with subtle circular glow behind it
- 8px spacing
- "전자 계량표" in white 22px bold
- 4px spacing
- Receipt number "S-2026-0142-001" in slate 14px
- Subtle decorative: thin dashed line below (receipt-style tear line effect), using #334155

WEIGHT SUMMARY SECTION (16px below header):
- Dark card (#0F172A bg, 16px rounded, 1px cyan border at 30% opacity)
- Three columns layout:
  - Left: "총중량" label (slate 11px) over "24,580" (white 18px bold) over "kg" (slate 11px)
  - Center divider: vertical line #334155
  - Middle: "공차중량" label over "8,200" over "kg"
  - Center divider
  - Right: "순중량" label over "16,380" (CYAN 20px BOLD - emphasized) over "kg"
- This is the most important data - net weight should pop visually

VEHICLE INFO SECTION (16px spacing):
- Section card: #1E293B bg, 16px rounded, 1px #334155 border
- Section header: "차량 정보" in cyan 14px semibold, with subtle left cyan accent bar (3px)
- 12px padding inside
- Info rows (label-value pairs):
  - "차량번호" (slate 14px, 80px fixed width) → "부산 12가 3456" (white 14px)
  - "운전자" → "김운전"
- 6px vertical spacing between rows

COMPANY/ITEM SECTION:
- Same card style as above
- Header: "업체 / 품목" in cyan
- "업체명" → "동국제강"
- "품목" → "철강 코일"
- "분류" → "원자재"

WEIGHING DETAIL SECTION:
- Same card style
- Header: "계량 정보" in cyan
- "1차 계량" → "2026-01-29 08:32:15"
- "1차 중량" → "24,580 kg"
- Thin divider line
- "2차 계량" → "2026-01-29 14:15:42"
- "2차 중량" → "8,200 kg"
- Thin divider line
- "순중량" → "16,380 kg" (cyan color, bold - highlighted row)
- "계량대" → "A-1 계량대"
- "담당자" → "박관리"

ROUTE SECTION (optional, shown if data exists):
- Same card style
- Header: "경로"
- "출발지" → "부산신항 5부두"
- "도착지" → "동국 창원공장"

SHARE BUTTON (bottom, 16px padding):
- Full width, height 52px
- Cyan (#06B6D4) background
- White text "계량표 공유" 16px bold
- Left: Share icon in white
- Rounded 14px
- Subtle glow effect

SHARE BOTTOM SHEET (shown on share tap - separate frame/overlay):
- Dark overlay, bottom sheet slides up
- Sheet: #1E293B bg, top-left and top-right 20px rounded
- Handle bar: centered, 40px wide, 4px height, #334155
- Title: "계량표 공유" white 18px bold
- 24px spacing
- Three share options as rows:
  1. KakaoTalk: Yellow (#FEE500) icon badge (48x48, 12px rounded) + "카카오톡" white 16px + "카카오톡으로 공유" slate 13px
  2. SMS: Cyan icon badge + "SMS" + "문자 메시지로 공유"
  3. Other: Dark slate (#334155) icon badge + "기타" + "다른 앱으로 공유"
- 8px gap between rows
- Bottom safe area padding

Style notes:
- Should feel like a premium digital document/certificate
- The dashed line detail adds receipt authenticity
- Section headers with cyan accent give clear visual hierarchy
- Weight data is the hero - net weight should be immediately scannable
- Official but not boring - blend between document and modern UI
```

---

## 7. 계량표 목록 화면 (Slip List Screen)

```
Design a dark-themed mobile list screen showing weighing receipts (slips) for today.

Screen size: 390 x 844px
Background: #0B1120

This screen is embedded within the bottom navigation shell (tab 3: "계량표").

TOP INFO:
- Small info bar: "오늘 계량표 3건" in white 14px, right side has filter/sort icon in slate

SLIP CARDS (scrollable list, 12px gap):

Each slip card:
- Background: #1E293B
- Border: 1px #334155
- Border-radius: 16px
- Padding: 20px

Card Layout:
- TOP ROW:
  - Left: Receipt icon (20px, cyan) + Slip number "S-2026-0142-001" in white 16px semibold
  - Right: Share indicator icon (if shared, show small share icon in cyan)

- VEHICLE/COMPANY INFO (8px spacing, each row):
  - Truck icon (14px, slate) + "부산 12가 3456" white 14px
  - Building icon + "동국제강"
  - Package icon + "철강 코일"
  - Icons are #64748B, 8px right margin

- WEIGHT SUMMARY BAR (12px spacing):
  - Contained in a dark sub-card: #0F172A bg, 12px rounded, 12px padding
  - Three columns equally spaced:
    - "총중량" (slate 11px) / "24,580 kg" (white 14px medium)
    - Vertical thin divider
    - "공차" / "8,200 kg"
    - Vertical thin divider
    - "순중량" / "16,380 kg" (cyan 14px bold - emphasized)

- BOTTOM ROW (8px top spacing):
  - Left: Clock icon (14px, slate) + "2026-01-29 14:15" slate 12px
  - Right: Chevron-right icon in slate

EMPTY STATE:
- Receipt outline icon (64px) in very faint slate
- "오늘 계량표가 없습니다." in slate 16px

Style notes:
- Cards should look like mini receipt previews
- The weight summary bar inside each card is key information at a glance
- Tap leads to full slip detail screen
- Clean, scannable list optimized for quick lookup
```

---

## 8. 이력 조회 화면 (History / Performance Screen)

```
Design a dark-themed mobile history screen with tab navigation for monthly and date-range views of weighing records.

Screen size: 390 x 844px
Background: #0B1120

TAB BAR (top, below app shell):
- Two tabs, full width, equally split
- Selected tab: white text 14px semibold + cyan bottom indicator line (3px)
- Unselected tab: slate text 14px
- Tab 1: "월별" (Monthly)
- Tab 2: "기간별" (By Period)
- Background: #0F172A

DATE CONTROL BAR:

For "월별" tab:
- Centered row: Left arrow + "2026년 01월" (white 16px semibold) + Right arrow
- Arrow buttons: circular, 36px, #1E293B bg, white icon
- Background: #1E293B strip with subtle bottom border

For "기간별" tab:
- Centered tappable date range pill:
  - #1E293B background, 1px #334155 border, 10px rounded
  - Calendar icon (cyan, 18px) + "2025.12.30 ~ 2026.01.29" (white 14px) + Edit calendar icon (slate)
  - Height: 44px

MONTHLY SUMMARY CARD (shown only in "월별" tab):
- Card: Cyan (#06B6D4) at 15% opacity background, 16px rounded
- Three stat columns equally spaced:
  - "전체" (light cyan 12px) / "45건" (white 20px bold)
  - "완료" / "42건"
  - "총 순중량" / "156.2톤"
- Clean horizontal layout

GROUPED RECORD LIST:

Date Group Header:
- Row: "2026-01-29" in cyan 14px semibold + pill badge "5건" (cyan bg 15%, cyan text 11px bold, rounded 10px)
- 8px below header

Record Card (each weighing record):
- #1E293B bg, 12px rounded, 1px #334155 border
- Padding: 16px
- TOP ROW:
  - Left: Dispatch number "D-2026-0142" white 14px semibold
  - Right: Status badge (same as dispatch list style)
- MIDDLE ROW (8px spacing):
  - Truck icon + "부산 12가 3456" slate 13px
  - Package icon + "철강 코일" slate 13px
  - Displayed in a row with 12px gap between items
- BOTTOM ROW (if completed, 8px spacing):
  - Scale icon (14px, cyan) + "순중량: 16,380 kg" cyan 14px bold
  - Right: "08:32" in slate 12px (first weighing time)

8px gap between record cards within a date group
16px gap between date groups

EMPTY STATE:
- History/clock icon (64px) in faint slate
- "해당 기간의 이력이 없습니다." slate 16px

Style notes:
- Dashboard analytics feel for the monthly summary
- Date groups create clear visual separation
- Weight data in cyan stands out as the most important metric
- Scrollable, data-dense but not cluttered
```

---

## 9. 공지사항 / 문의 화면 (Notice & Inquiry Screen)

```
Design a dark-themed mobile screen with two tabs: notices/announcements and inquiry/call directory.

Screen size: 390 x 844px
Background: #0B1120

TAB BAR:
- Two tabs: "공지사항" | "문의/전화"
- Same style as history screen tabs (selected: white + cyan underline, unselected: slate)

=== TAB 1: 공지사항 (Notices) ===

Notice Card (expandable accordion style):
- #1E293B bg, 16px rounded, 1px #334155 border
- 16px padding
- 10px bottom margin between cards

Collapsed state:
- TOP ROW:
  - If important: Small "중요" badge (rose bg, white text 10px bold, 4px rounded) on left
  - Title text: white 15px semibold, single line with ellipsis
  - Right: Expand/chevron-down icon in slate
- BOTTOM: Date "2026-01-28" in slate 12px

Expanded state:
- Same top row but chevron-up icon
- Below date: thin divider line (#334155)
- 12px spacing
- Content text in white/light slate 14px, multi-line
- Comfortable line-height (1.6)

=== TAB 2: 문의/전화 (Inquiry & Call) ===

INFO CARD (top):
- #06B6D4 at 10% opacity bg, 16px rounded
- Row: Info circle icon (cyan) + text column:
  - "문의 유형을 선택하면 자동 연결됩니다." white 14px medium
  - "운영시간: 평일 08:00 ~ 18:00" slate 13px

CALL TYPE CARDS (16px top spacing, 10px gap):

Each call type card:
- #1E293B bg, 16px rounded, 1px #334155 border
- 16px padding
- Row layout:
  - LEFT: Colored icon badge (48x48px, 16px rounded)
    - Icon color on 12% opacity background of the same color
  - CENTER (16px left margin):
    - Title: white 15px semibold
    - Subtitle: slate 13px
  - RIGHT:
    - Phone icon in the card's accent color (20px)
    - Phone number in slate 12px below

5 call types with distinct colors:
1. "일반 문의" - Cyan (#06B6D4), help-circle icon, "051-000-0001"
2. "계량 관련" - Amber (#F59E0B), scale icon, "051-000-0002"
3. "배차 문의" - Emerald (#10B981), truck icon, "051-000-0003"
4. "시스템 장애" - Rose (#F43F5E), warning icon, "051-000-0004"
5. "기타 문의" - Slate (#94A3B8), more icon, "051-000-0005"

Style notes:
- Notice tab is a clean, readable announcement board
- Call tab is a quick-access directory - one tap to call
- Color-coded call types for instant recognition
- Large touch targets for call cards
```

---

## 10. 알림 목록 화면 (Notification List Screen)

```
Design a dark-themed mobile notification list screen showing push notifications and system alerts.

Screen size: 390 x 844px
Background: #0B1120

TOP BAR:
- Back arrow (white) left
- Center: "알림" white 18px semibold
- Right: "모두 읽음" text button in cyan 13px (optional)

NOTIFICATION CARDS (scrollable list, 8px gap):

Each notification card:
- UNREAD state:
  - Background: Cyan (#06B6D4) at 8% opacity
  - Border: 1px cyan at 30% opacity
  - Left: 8px blue dot indicator (filled circle, vertically centered)
- READ state:
  - Background: #1E293B
  - Border: 1px #334155
  - Left: 20px empty space (no dot)
- Border-radius: 16px
- Padding: 16px

Card Layout:
- TOP ROW:
  - Left: Type chip - small icon (14px) + type label (12px semibold) in cyan
    - Types: Scale icon + "계량" / Truck icon + "배차" / Megaphone icon + "공지" / Gear icon + "시스템"
  - Right: Timestamp "2026.01.29 14:30" in slate 11px

- TITLE (8px below):
  - Unread: white 15px semibold
  - Read: white 15px regular (lighter weight)
  - e.g., "계량 완료 알림"

- BODY (4px below):
  - Slate 13px, max 2 lines with ellipsis
  - e.g., "D-2026-0142 배차의 계량이 완료되었습니다. 계량표를 확인하세요."

EMPTY STATE:
- Bell outline icon (64px) in faint slate
- "알림이 없습니다." slate 16px

Style notes:
- Unread notifications should clearly stand out with the blue dot and tinted background
- Read notifications fade into the background
- Type chips help users quickly scan notification categories
- Each notification is tappable and navigates to the relevant screen
- Clean, inbox-style layout
- Consider subtle slide-in animation for new notifications
```

---

## 11. OTP 로그인 화면 (OTP Login / Safe Login Screen)

```
Design a dark-themed mobile OTP-based safe login screen, an alternative to password login.

Screen size: 390 x 844px
Background: Deep navy gradient #0B1120 to #0F172A

TOP BAR:
- Back arrow (white) left
- Center: "안전 로그인" white 18px semibold

HEADER (centered, top section):
- Large shield icon with checkmark inside, cyan (#06B6D4), 56px, with subtle radial glow
- 16px spacing
- "휴대폰 번호로 안전하게\n로그인하세요" in white 20px semibold, centered, 2 lines
- 8px spacing
- "등록된 번호로 인증 코드가 발송됩니다" in slate 14px

PHONE NUMBER INPUT (glassmorphism card):
- Card: #1E293B bg, 20px rounded, subtle border
- Padding: 24px
- Label: "휴대폰 번호" in slate 14px
- Input field:
  - #0F172A bg, 12px rounded, 52px height
  - Left: Phone icon in slate
  - Placeholder: "010-0000-0000" in dark slate
  - Number keyboard type
- 20px spacing
- "인증코드 요청" button:
  - Full width, cyan bg, white text 16px bold, 52px height, 14px rounded

OTP INPUT SECTION (appears after code request):
- 24px spacing
- Label: "인증 코드" in slate 14px + Timer pill badge on right ("04:32" in cyan)
- 6 digit input boxes (same style as OTP verification screen)
- 16px spacing
- "인증하기" button:
  - Full width, cyan bg, white text, 52px height, 14px rounded
  - Disabled until 6 digits entered

RESEND OPTION:
- "인증코드가 오지 않나요?" slate text 14px, centered
- "재요청" text button in cyan, underlined

FOOTER:
- "기존 방식으로 로그인" text button, slate 14px, centered
- Bottom padding 32px

Style notes:
- Security-focused visual cues (shield icon, lock metaphors)
- Step-by-step progressive disclosure (phone first, then OTP)
- Large touch targets for all interactive elements
- Calm, trustworthy dark design
```

---

## 12. 배차 상세 화면 (Dispatch Detail Screen)

```
Design a dark-themed mobile dispatch detail screen showing full information about a specific vehicle dispatch assignment.

Screen size: 390 x 844px
Background: #0B1120

TOP BAR:
- Back arrow (white) left
- Center: "배차 상세" white 18px semibold
- Right: More options (three dots) icon in white

STATUS HERO SECTION:
- Full-width section, no card, just colored accent
- Status displayed prominently:
  - Large status icon (48px) in center:
    - Waiting: Clock icon, slate
    - In Progress: Animated scale icon, cyan
    - Completed: Checkmark icon, emerald
  - Below: Status text "진행중" in matching color, 20px bold
  - Below: Dispatch number "D-2026-0142" in white 16px
- Background: subtle gradient of status color at 5% opacity fading down

INFO SECTIONS (card-based, scrollable):

Vehicle Section:
- Card: #1E293B bg, 16px rounded, 1px #334155 border
- Section header: "차량 정보" in cyan 14px semibold with left cyan accent bar (3px)
- Info rows:
  - "차량번호" → "부산 12가 3456"
  - "차종" → "25톤 카고트럭"
  - "운전자" → "김운전"
  - "연락처" → "010-1234-5678" (tappable, cyan color)

Company Section:
- Same card style
- "업체 / 품목" header
- "업체명" → "동국제강"
- "품목" → "철강 코일"
- "수량" → "1식"

Route Section:
- Same card style
- "경로 정보" header
- "출발지" → "부산신항 5부두"
- "도착지" → "동국 창원공장"
- Optional: Simple route visualization (two dots connected by dashed line with arrow)

Schedule Section:
- Same card style
- "일정" header
- "배차일시" → "2026-01-29 08:00"
- "예상도착" → "2026-01-29 10:30"

ACTION BUTTONS (bottom):
- If waiting: "계량 시작" cyan button, full width, 52px height
- If in progress: "계량 진행 보기" outlined cyan button
- If completed: "계량표 보기" emerald button

Style notes:
- Detail-rich but organized with clear section hierarchy
- Status is the first thing users see
- Cyan accent bars on section headers provide visual rhythm
- Action button changes based on status
- Adequate padding and spacing for readability
```

---

## 디자인 적용 시 참고사항

1. **모든 화면에서 일관된 컬러 시스템 유지** - Deep Navy 배경 + Charcoal 카드 + Cyan 포인트
2. **터치 타겟 최소 48dp** - 운전자 장갑 착용 고려
3. **고대비 텍스트** - 야외 직사광선에서 가독성 확보
4. **7-segment 스타일 숫자** - 중량 표시에는 모노스페이스/디지털 폰트
5. **미니멀 아이콘** - Outlined/thin line 스타일, filled 아이콘 지양
6. **글라스모피즘 카드** - 주요 카드에만 subtle하게 적용
7. **상태 색상 통일** - 대기(Slate) / 진행(Cyan) / 완료(Emerald) / 오류(Rose) / 경고(Amber)
8. **애니메이션 힌트** - 실시간 데이터는 pulsing, 진행중은 breathing 효과 제안
