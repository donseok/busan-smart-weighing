# UI/UX Design Specification: Busan Smart Weighing System

## 1. Design Concept: "Modern Industrial Intelligence"

The UI/UX concept for the Busan Smart Weighing System is **"Modern Industrial Intelligence"**. It moves away from the clunky interfaces of traditional industrial software and aims for a futuristic, intuitive interface reminiscent of a **sci-fi command center**.

### Core Values

| Value | Description |
|-------|-------------|
| **Visibility** | Key information (weight, plate number) is clearly visible across diverse environments including outdoor/indoor and day/night conditions |
| **Real-time** | Weighing progress, vehicle entry, and recognition results provide immediate, lag-free feedback |
| **Trust** | A clean, well-organized layout visually conveys data accuracy |
| **Consistency** | A unified design language is shared across all three platforms: web, mobile, and desktop |

---

## 2. Visual Identity & Color Palette

To reduce eye fatigue for operators monitoring over long periods and to enhance information focus, **Dark Mode** is provided as the default, with **Light Mode** also available.

### 2.1 Color Palette (Dark Theme)

The palette is built on the Tailwind CSS Slate color system.

| Role | Color Name | HEX | Usage |
|------|------------|-----|-------|
| **Background Darkest** | Deep Navy | `#060D1B` | Header/footer background, deepest layer |
| **Background Base** | Dark Navy | `#0B1120` | Main background color (deep, dark navy) |
| **Background Elevated** | Slate 900 | `#0F172A` | Input field backgrounds, table headers |
| **Surface** | Charcoal | `#1E293B` | Card and panel backgrounds (Glassmorphism effect) |
| **Border** | Slate 700 | `#334155` | Borders and dividers |
| **Primary** | Neon Cyan | `#06B6D4` | Current weight value, primary buttons, active states |
| **Primary Dark** | Cyan 700 | `#0E7490` | Primary hover state, secondary accent |
| **Success** | Emerald | `#10B981` | Weighing complete, successful recognition, system normal |
| **Warning** | Amber | `#F59E0B` | Caution, re-weighing required, low recognition confidence |
| **Error** | Rose | `#F43F5E` | Error, communication failure, blocked |
| **Text Primary** | Slate 50 | `#F8FAFC` | Default text |
| **Text Secondary** | Slate 400 | `#94A3B8` | Secondary text, labels |
| **Text Muted** | Slate 500 | `#64748B` | Disabled text, hints |

### 2.2 Dark/Light Theme Toggle

All platforms (web, desktop) support dark-to-light theme switching.

| Platform | Toggle Method | Persistence |
|----------|---------------|-------------|
| Web (React) | Toggle button in header area | `ThemeContext` + `localStorage` |
| Desktop (WeighingCS) | Icon toggle in HeaderBar (dark=moon, light=sun) | `theme.dat` file |

**Web theme configuration** (`frontend/src/theme/themeConfig.ts`):

```typescript
// Ant Design 5 ConfigProvider theme tokens
export const darkTheme: ThemeConfig = {
  token: {
    colorPrimary: '#06B6D4',
    colorBgBase: '#0B1120',
    colorTextBase: '#F8FAFC',
    borderRadius: 8,
    fontFamily: "'Inter', 'Noto Sans KR', sans-serif",
  },
  components: {
    Card: { colorBgContainer: '#1E293B' },
    Table: { colorBgContainer: '#1E293B', headerBg: '#0F172A' },
  },
};
```

---

## 3. Web Frontend Design System (React + Ant Design)

### 3.1 Layout Structure

**MainLayout** (`frontend/src/layouts/MainLayout.tsx`) serves as the base layout for all pages.

```
+----------------------------------------------------------+
|  Sider (collapsible)  |  Header (tab nav + favorites)     |
|  +---------------+    |  +------------------------------+  |
|  | Logo          |    |  | [Tab1] [Tab2] ... [+]        |  |
|  | Menu Items    |    |  +------------------------------+  |
|  | - Dashboard   |    |  |                              |  |
|  | - Dispatch    |    |  |    Content Area (per route)   |  |
|  | - Weighing    |    |  |                              |  |
|  | - ...         |    |  |                              |  |
|  |               |    |  |                              |  |
|  | Admin Menu    |    |  |                              |  |
|  | - Master Data |    |  |                              |  |
|  | - System Mgmt |    |  +------------------------------+  |
|  +---------------+    |                                     |
+----------------------------------------------------------+
```

**Key layout characteristics**:

| Characteristic | Description |
|----------------|-------------|
| Multi-tab navigation | `TabContext` manages up to 10 simultaneous tabs with pinned tab support (weighing station) |
| Collapsible sidebar | Ant Design Sider `collapsible` feature optimizes screen utilization |
| Role-based menu | `pageRegistry.ts` defines `roles` per route (ADMIN, MANAGER, DRIVER) |
| React.lazy code splitting | All pages are lazy-loaded via `React.lazy` for optimized initial load |

### 3.2 Page Registry (pageRegistry.ts)

The `pageRegistry.ts` file centrally manages all page information, standardizing routes, icons, permissions, and lazy loading.

| Path | Page | Permissions | Description |
|------|------|-------------|-------------|
| `/dashboard` | DashboardPage | All | Dashboard (3 tabs: Overview/Realtime/Analysis) |
| `/dispatch` | DispatchPage | All | Dispatch management |
| `/weighing` | WeighingPage | All | Weighing status |
| `/inquiry` | InquiryPage | All | Weighing inquiry |
| `/gate-pass` | GatePassPage | All | Gate pass management |
| `/slips` | SlipPage | All | Electronic weighing slips |
| `/statistics` | StatisticsPage | All | Statistics/Reports |
| `/weighing-station` | WeighingStationPage | All | Weighing station control (pinned tab) |
| `/monitoring` | MonitoringPage | All | Equipment monitoring |
| `/notices` | NoticePage | All | Notices |
| `/help` | HelpPage | All | Help guide |
| `/mypage` | MyPage | All | My page |
| `/master/*` | Master* Pages | ADMIN, MANAGER | Master data management (4 types) |
| `/admin/*` | Admin* Pages | ADMIN | System administration (3 types) |

### 3.3 Common UI Components

#### SortableTable (Drag-sortable Table)

A `@dnd-kit`-based drag-sortable Ant Design Table wrapper component used across all list pages.

| Feature | Description |
|---------|-------------|
| Drag sorting | Row drag-sorting via `@dnd-kit/core` + `@dnd-kit/sortable` |
| Fill-height | Table height auto-adjusts to fit the parent container |
| Pagination | Integrated Ant Design Pagination (default page size: 20) |
| Responsive | Auto-scroll based on container dimensions |

#### MasterCrudPage (Master Data CRUD Template)

A shared component that standardizes the CRUD pattern for transport company, vehicle, scale, and common code management pages.

```
+--------------------------------------+
|  [Page Title]            [Add] Button |
+--------------------------------------+
|  [Search Input] [Search] [Reset]      |
+--------------------------------------+
|  SortableTable (list)                 |
|  - Edit button -> Edit modal          |
|  - Delete button -> Confirm Popconfirm|
+--------------------------------------+
|  Create/Edit Modal (Ant Design Form)  |
|  - validators.ts common rules         |
+--------------------------------------+
```

#### AnimatedNumber (Number Animation)

A component that renders smooth count-up animations for numeric value changes in dashboard KPI cards.

#### OnboardingTour (Onboarding Guide)

A step-by-step onboarding guide for new users built with the Ant Design Tour component. It walks users through key features on their first visit.

#### EmptyState (No Data State)

A visual placeholder component displayed when no data is available. Combines an icon, descriptive text, and an action button.

#### FavoriteButton / FavoritesList (Favorites)

A feature that allows users to bookmark frequently used pages for quick access.

### 3.4 State Management Hooks

| Hook | Purpose |
|------|---------|
| `useApiCall` | API call wrapper (automatic loading/error state management) |
| `useCrudState` | Common CRUD page state management (list, selection, modal control) |
| `useKeyboardShortcuts` | Keyboard shortcut registration/cleanup |
| `useTabVisible` | Browser tab visibility detection (active/inactive) |
| `useWebSocket` | STOMP WebSocket connection/subscription management |
| `useWeighingStation` | Integrated weighing station control business logic |
| `useWeighingStationSocket` | Weighing station-specific WebSocket subscriptions |

### 3.5 Dashboard (DashboardPage)

An integrated dashboard organized into three tabs.

```
+---------------------------------------------+
|  [Overview]  [Realtime]  [Analysis]          |
+---------------------------------------------+
|  Overview Tab (OverviewTab):                 |
|  +------+ +------+ +------+ +------+        |
|  |Today |  |Today |  |Today |  |Monthly|     |
|  |Total |  |Done  |  |Active|  |Cumul. |     |
|  |  42  |  |  38  |  |   4  |  | 850   |     |
|  |(AnimatedNumber)                           |
|  +------+ +------+ +------+ +------+        |
|  +-------------------+ +---------------+     |
|  | Daily Trend Chart | | Item Dist.    |     |
|  | (ECharts Line)    | | (ECharts Pie) |     |
|  +-------------------+ +---------------+     |
+---------------------------------------------+
|  Realtime Tab (RealtimeTab):                 |
|  - WebSocket-based real-time weighing status |
|  - Live weight changes, equipment updates    |
+---------------------------------------------+
|  Analysis Tab (AnalysisTab):                 |
|  - Detailed stats by period/item/mode        |
|  - ECharts 6.0 interactive charts            |
+---------------------------------------------+
```

### 3.6 Weighing Station Control (WeighingStationPage)

The core control screen where operators monitor and control the weighing process in real time. It is displayed as a pinned tab that always remains in the tab bar.

```
+---------------------------------------------------------+
|  Left Panel (Display Area)     |  Right Panel (Control)  |
|  +------------------------+   |  +-------------------+   |
|  | WeightDisplay          |   |  | ModeToggle        |   |
|  | 45,200.5 kg  [STABLE]  |   |  | [AUTO] / [MANUAL] |   |
|  | (72px digital display) |   |  +-------------------+   |
|  +------------------------+   |  +-------------------+   |
|  +------------------------+   |  | ProcessStateBar   |   |
|  | VehicleInfoPanel       |   |  | o---o---o---*     |   |
|  | 12A3456 | Dongkuk Logi |   |  | Idle Weigh Stab OK|   |
|  | Steel | DIS-0101       |   |  +-------------------+   |
|  +------------------------+   |  +-------------------+   |
|  +------------------------+   |  | ManualControls    |   |
|  | ConnectionStatusBar    |   |  | [Plate Search]    |   |
|  | * Scale  * Display     |   |  | [Dispatch Select] |   |
|  | * Barrier * Network    |   |  | [Start Weighing]  |   |
|  +------------------------+   |  +-------------------+   |
|  +------------------------+   |  +-------------------+   |
|  | WeighingHistoryTable   |   |  | ActionButtons     |   |
|  | Recent weighing history|   |  | [Reset][Barrier]  |   |
|  | (SortableTable)        |   |  +-------------------+   |
|  +------------------------+   |  +-------------------+   |
|                               |  | StatusLog         |   |
|                               |  | Terminal-style log |   |
|                               |  +-------------------+   |
|                               |  +-------------------+   |
|                               |  | SimulatorPanel    |   |
|                               |  | [DEV] Simulator   |   |
|                               |  +-------------------+   |
+---------------------------------------------------------+
```

**10 sub-components** (`frontend/src/components/weighing-station/`):

| Component | Function |
|-----------|----------|
| `WeightDisplay` | Real-time weight digital display (72px monospace, glow effect) |
| `VehicleInfoPanel` | Vehicle/dispatch/transport company info display (5-row icon grid) |
| `ConnectionStatusBar` | 4 equipment connection status LEDs (scale/display board/barrier/network) |
| `ModeToggle` | Auto (AUTO LPR) / Manual (MANUAL) mode switch |
| `ManualControls` | Manual mode vehicle search and weighing start controls |
| `ActionButtons` | Reset, open barrier, re-weighing buttons |
| `ProcessStateBar` | 4-step process progress indicator (IDLE -> WEIGHING -> STABILIZING -> COMPLETE) |
| `StatusLog` | Terminal-style real-time event log (dark background, max 200 entries) |
| `SimulatorPanel` | Hardware simulation panel for development/testing |
| `WeighingHistoryTable` | Recent 50 weighing records table |

### 3.7 UX Principles

| Principle | Description |
|-----------|-------------|
| **Sound Feedback** | Combined visual + audio feedback on weighing completion/error (operational efficiency) |
| **Keyboard Shortcuts** | Key functions accessible without a mouse (`useKeyboardShortcuts` hook) |
| **Responsive** | Optimized for desktop/tablet resolutions, responsive mobile viewing |
| **Accessibility** | Screen reader support via `aria-live` attributes |
| **Auto-refresh** | Real-time data auto-refresh via WebSocket (STOMP over SockJS) |

---

## 4. Mobile App Design (Flutter)

### 4.1 Design Principles

| Principle | Description |
|-----------|-------------|
| **Big & Bold** | Minimum button height of **56dp** to ensure operability while wearing gloves |
| **High Contrast** | Maximized Deep Navy + White/Neon contrast for visibility under direct outdoor sunlight |
| **Linear Flow** | One core task per screen (Login -> Wait -> Confirm Weighing -> Complete) |
| **Offline Resilience** | SharedPreferences-based offline cache to handle network instability |

### 4.2 Color Palette (`app_colors.dart`)

The same Tailwind Slate-based palette used in the web frontend is applied in Flutter.

| Color | HEX | Usage |
|-------|-----|-------|
| Primary | `#06B6D4` | Primary accent (cyan) |
| Background | `#0B1120` | Main background |
| Surface | `#1E293B` | Card background |
| Success | `#10B981` | Complete status |
| Warning | `#F59E0B` | Warning status |
| Error | `#F43F5E` | Error status |

### 4.3 Screen Structure (12 Screens)

```
mobile/lib/screens/
+-- login_screen.dart              # ID/PW login (Glassmorphism effect)
+-- home_screen.dart               # Home (bottom tab navigation)
+-- auth/
|   +-- otp_login_screen.dart      # OTP login
+-- dispatch/
|   +-- dispatch_list_screen.dart  # Dispatch list
|   +-- dispatch_detail_screen.dart# Dispatch detail
+-- weighing/
|   +-- weighing_progress_screen.dart  # Weighing progress
|   +-- otp_input_screen.dart          # OTP input (6-digit custom keypad)
+-- slip/
|   +-- slip_list_screen.dart      # Electronic weighing slip list
|   +-- slip_detail_screen.dart    # Electronic weighing slip detail
+-- history/
|   +-- history_screen.dart        # Weighing/dispatch history
+-- notice/
    +-- notice_screen.dart         # Notices
    +-- notification_list_screen.dart  # Notification list
```

### 4.4 Key Screen Designs

#### Login Screen (LoginScreen)

A login card with Glassmorphism effect. The background features a gradient + blur treatment.

```
+------------------------------+
|     [Blur background +       |
|      gradient]               |
|  +------------------------+  |
|  |  [Factory icon]        |  |
|  |  Busan Smart Weighing  |  |
|  |                        |  |
|  |  [Employee ID]         |  |
|  |  [Password]            |  |
|  |                        |  |
|  |  [       Login        ]|  |
|  +------------------------+  |
+------------------------------+
```

#### Home Screen (HomeScreen)

Main features are accessed via bottom tab navigation.

| Role | Tab Configuration |
|------|-------------------|
| MANAGER | Home, Dispatch, Weighing, Slips, More (5 tabs) |
| DRIVER | Home, Dispatch, Weighing, More (4 tabs) |

#### OTP Input Screen (OtpInputScreen)

A dedicated screen for entering the 6-digit OTP code displayed on the electronic display board.

```
+------------------------------+
|         < OTP Verify         |
|                              |
|        DIS-2026-0101         |
|   Enter the 6-digit OTP code|
|         [ 04:32 ]           |
|                              |
|   [1] [2] [3] [4] [5] [6]  |
|                              |
|       [1] [2] [3]           |
|       [4] [5] [6]           |
|       [7] [8] [9]           |
|       [C] [0] [<]           |
|                              |
|   [       Verify        ]   |
+------------------------------+
```

- 5-minute countdown timer (MM:SS)
- "Request New OTP" button appears on expiry
- Custom 4x3 numeric keypad

#### Weighing Progress Screen (WeighingProgressScreen)

Displays the current weighing status step-by-step using a card-based layout.

```
+------------------------------+
|  Dispatch No: DIS-2026-0101  |
|  12A3456 | Dongkuk Logi [1st]|
|                              |
|  Progress               33% |
|  [========----------]       |
|  Wait   1st   2nd   Done    |
|                              |
|  Gross Weight: 45,201 kg    |
|  Tare Weight: -             |
|  Net Weight: -              |
|                              |
|  [       OTP Verify      ]  |
+------------------------------+
```

- Auto-refresh every 10 seconds (Timer)
- Pull-to-Refresh support
- Dialog displayed upon completion detection

#### Electronic Weighing Slip Detail (SlipDetailScreen)

A digital weighing slip that replaces paper receipts. Includes sharing functionality.

| Share Method | Icon Color | Description |
|-------------|------------|-------------|
| KakaoTalk | `#FEE500` | Share via KakaoTalk |
| SMS | `#06B6D4` | Share via text message |
| Other | `#334155` | OS share sheet via `share_plus` |

### 4.5 Common Widgets

| Widget | Purpose |
|--------|---------|
| `AppDrawer` | Navigation drawer (side menu) |
| `StatusBadge` | Color-coded status badge (Pending=yellow, Complete=green, Error=red) |
| `WeightDisplayCard` | Weight display card (3-column: gross/tare/net weight) |

### 4.6 State Management & Routing

| Technology | Role |
|------------|------|
| Provider | State management (AuthProvider, DispatchProvider) |
| GoRouter | Declarative routing + auth redirect |
| Dio | HTTP client + JWT interceptor |
| flutter_secure_storage | Secure token storage |
| Firebase Messaging | FCM push notifications (Android channel: `busan_weighing_channel`) |

---

## 5. Desktop Program Design (WeighingCS - C# WinForms)

### 5.1 Design System

WeighingCS implements a web-grade dark theme UI in WinForms through GDI+ custom rendering. All controls are rendered directly in `OnPaint` (AntiAlias, ClearTypeGridFit).

#### Theme Design Tokens (`Controls/Theme.cs`)

A centrally managed design token system using the same Tailwind Slate palette as the web frontend.

| Category | Token | Value | Usage |
|----------|-------|-------|-------|
| Background | `BgDarkest` | `#060D1B` | Header/footer |
| Background | `BgBase` | `#0B1120` | Main background |
| Background | `BgElevated` | `#0F172A` | Input fields |
| Background | `BgSurface` | `#1E293B` | Cards |
| Color | `Primary` | `#06B6D4` | Primary accent |
| Color | `Success` | `#10B981` | Success |
| Color | `Warning` | `#F59E0B` | Warning |
| Color | `Error` | `#F43E5E` | Error |
| Scale | `FontScale` | `1.5f` | Font size multiplier |
| Scale | `LayoutScale` | `1.25f` | Layout/spacing multiplier |

**Font system**:

| Token | Font | Size | Usage |
|-------|------|------|-------|
| `FontBody` | Segoe UI | 9.5pt x FontScale | Body text |
| `FontHeading` | Segoe UI Bold | 11pt x FontScale | Headings |
| `FontCaption` | Segoe UI | 8pt x FontScale | Captions |
| `FontMono` | Consolas | 10pt x FontScale | Monospace |
| `FontMonoLarge` | Consolas Bold | 32pt x FontScale | Weight display |

**Common utilities**: Color transformation functions including `WithAlpha()`, `Lighten()`, `Darken()`, and `Blend()`.

> **Caution**: Theme fonts are statically cached and must not be referenced with `using var`. `InvalidateFontCache()` only nullifies references without calling Dispose (controls may still reference previous font objects, and disposing them would cause a "Parameter is not valid" exception).

### 5.2 Layout Structure

Three-tier layout: HeaderBar (Top) -> panelContent (Fill: Left + Divider + Right) -> StatusFooter (Bottom)

```
+------------------------------------------------------+
|  HeaderBar (Dock.Top, 56px)                          |
|  [DK] Busan Smart Weighing   *Scale *Display ... HH:mm|
+-------------------+-+--------------------------------+
|  panelLeftCol     | |  panelRightCol (Dock.Fill)     |
|  (420px, 35%)     | |  +----------------------------+|
|  +---------------+| |  | ModeToggle (44px)          ||
|  |WeightDisplay  || |  | ProcessStepBar (64px)      ||
|  |(220px, glow)  || |  | CardManual (185px)         ||
|  +---------------+| |  | CardActions (88px)         ||
|  |CardVehicle    || |  | CardSimulator (130px)      ||
|  |(250px, 5 rows)|| |  | TerminalLog (Fill)         ||
|  +---------------+| |  +----------------------------+|
|  |CardHistory    || |                                |
|  |(Fill, list)   || |                                |
|  +---------------+| |                                |
+-------------------+-+--------------------------------+
|  StatusFooter (Dock.Bottom, 32px)                    |
|  Scale#1 . COM1 . 9600bps  * Auto Mode  v1.0.0 HH:mm:ss|
+------------------------------------------------------+
```

### 5.3 Custom Controls (16 Types)

| Control | Description |
|---------|-------------|
| **HeaderBar** | Top header (logo, title, theme toggle, equipment LEDs, real-time clock) |
| **StatusFooter** | Bottom status bar (scale info, mode, sync status, time) |
| **WeightDisplayPanel** | Large weight digital display (glow effect, stability badge) |
| **CardPanel** | Glass-effect card container (shadow, accent bar) |
| **ModernButton** | 3 button variants (Primary/Secondary/Danger, glass highlight) |
| **ModernToggle** | Sliding toggle (auto/manual mode switch, animation) |
| **ModernTextBox** | Text input (rounded border, focus glow, placeholder) |
| **ModernComboBox** | Combo box (custom dropdown rendering, focus effect) |
| **ModernCheckBox** | Check box (custom GDI+ rendering, hover effect) |
| **ModernListView** | List view (alternating row colors, custom header, last column auto-fill) |
| **ModernProgressBar** | Progress bar (splash screen) |
| **ProcessStepBar** | 4-step process display (circular indicator, checkmark) |
| **TerminalLogPanel** | Terminal-style log panel (macOS traffic light decoration) |
| **RoundedRectHelper** | Rounded rectangle GraphicsPath utility |
| **ConnectionStatusPanel** | [Legacy] Connection status panel (replaced by HeaderBar) |
| **LedIndicator** | [Legacy] LED indicator (replaced by HeaderBar) |

### 5.4 Key Control Details

#### WeightDisplayPanel (Weight Display)

| Element | Description |
|---------|-------------|
| Background | BgElevated -> BgSurface vertical gradient + glass overlay |
| Weight text | Consolas 32~72pt Bold (width-proportional scaling) |
| Glow effect | 4-layer Primary glow when stable |
| Stability badge | STABLE (green) / UNSTABLE (yellow) / ERROR (red), rounded filled tag |
| Left accent | Status-dependent 4px vertical bar |

#### ProcessStepBar (Process Steps)

```
o-----o-----o-----*
Idle   Weigh  Stab   Done     [Done *]
```

| State | Visual Representation |
|-------|----------------------|
| Completed step | Primary filled circle + white checkmark, Primary connector line |
| Current step | Primary border + center dot + glow, bold label |
| Future step | Border-only circle, muted text label |

#### TerminalLogPanel (Terminal Log)

- Dark background (`#0D1117`) + monospace font
- macOS traffic light decoration in header (red/yellow/green circles)
- Color-coded log levels: info (gray), success (neon green), warning (yellow), error (red)
- Maximum 200 log entries retained, auto-scroll

### 5.5 Hardware Communication

| Equipment | Protocol | Service |
|-----------|----------|---------|
| Weighing indicator | Serial port (COM) | `IndicatorService` |
| Display board | TCP socket | `DisplayBoardService` |
| Barrier gate | TCP socket | `BarrierService` |
| Backend server | REST API (HTTP) | `ApiService` |

**Interface-based abstraction**: `ILprCamera`, `IVehicleDetector`, and `IVehicleSensor` interfaces enable swapping between hardware and simulators.

**Simulator mode**: Development and testing is possible without physical hardware (LprCameraSimulator, VehicleDetectorSimulator, VehicleSensorSimulator).

### 5.6 Splash Screen (SplashForm)

A splash screen that displays initialization status on application startup.

- Gradient background + radial glow effect
- ModernProgressBar showing initialization progress
- Logo + system name + version information

---

## 6. Cross-Platform Design Consistency

### 6.1 Web to Desktop Component Mapping

| Web (React) | Desktop (C# WinForms) | Implementation Difference |
|-------------|------------------------|---------------------------|
| `WeightDisplay` | `WeightDisplayPanel` | GDI+ direct rendering |
| `VehicleInfoPanel` | `CardPanel` + `TableLayoutPanel` | 5-row table inside card |
| `ConnectionStatusBar` | `HeaderBar` (built-in LEDs) | Integrated into header |
| `ModeToggle` | `ModernToggle` | Sliding animation |
| `ManualControls` | `CardPanel` + `ModernTextBox/ComboBox` | Wrapper pattern |
| `ActionButtons` | `CardPanel` + `ModernButton` | 3 button variants |
| `ProcessStateBar` | `ProcessStepBar` | Circular indicators |
| `StatusLog` | `TerminalLogPanel` | macOS traffic light decoration |
| `SimulatorPanel` | `CardPanel` + `ModernCheckBox/Button` | Simulator toggles |
| `WeighingHistoryTable` | `ModernListView` | OwnerDraw ListView |
| -- | `StatusFooter` | Desktop-only bottom bar |

### 6.2 Shared Design Principles

| Principle | Web | Mobile | Desktop |
|-----------|-----|--------|---------|
| Color palette | Ant Design Theme Token | `app_colors.dart` | `Theme.cs` |
| Dark mode | `ThemeContext` | Fixed dark | Dark/Light toggle |
| Real-time communication | STOMP WebSocket | 10-second polling | REST API + COM |
| Offline support | -- | SharedPreferences | SQLite |
| Status colors | `colors.success/warning/error` | Material Colors | Theme.Success/Warning/Error |

---

## 7. Design Reference Mockups

### Web Dashboard Mockup
![Smart Weighing Dashboard Mockup](smart_weighing_dashboard_mockup_1769582079553.png)

### Mobile App Mockup
![Smart Weighing Mobile App Mockup](smart_weighing_mobile_app_mockup_1769582889213.png)
