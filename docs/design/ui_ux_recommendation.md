# UI/UX Recommendation: Busan Smart Weighing System

## 1. Design Concept: "Modern Industrial Intelligence"
부산 스마트 계량 시스템을 위한 UI/UX 컨셉으로 **"Modern Industrial Intelligence"**를 제안합니다.
전통적인 투박한 산업용 소프트웨어에서 벗어나, **SF 영화의 관제 센터**와 같은 미래지향적이고 직관적인 인터페이스를 지향합니다.

### 🎯 Core Values
1.  **Visibility (가시성)**: 야외/실내, 낮/밤 등 다양한 환경에서도 핵심 정보(중량, 차량번호)가 명확히 보여야 합니다.
2.  **Real-time (실시간성)**: 계량 진행 상황, 차량 진입, 인식 결과가 지연 없이 즉각적으로 피드백되어야 합니다.
3.  **Trust (신뢰성)**: 데이터의 정확성을 시각적으로 전달하는 정돈되고 깔끔한 레이아웃을 사용합니다.

## 2. Visual Identity & Color Palette
장시간 모니터링하는 관제 요원의 눈 피로도를 줄이고, 정보의 집중도를 높이기 위해 **Dark Mode**를 기본으로 제안합니다.

### 🎨 Color Palette (Dark Theme)
| Role | Color | Hex Code | Usage |
| :--- | :--- | :--- | :--- |
| **Background** | **Deep Navy** | `#0B1120` | 메인 배경색 (깊이감 있는 어두운 남색) |
| **Surface** | **Charcoal** | `#1E293B` | 카드, 패널 배경 (Glassmorphism 효과 적용 가능) |
| **Primary** | **Neon Cyan** | `#06B6D4` | 핵심 데이터 (현재 중량값), 주요 버튼 |
| **Success** | **Emerald** | `#10B981` | 계량 완료, 정상 인식, 시스템 정상 |
| **Warning** | **Amber** | `#F59E0B` | 주의, 재계량 필요, 인식 신뢰도 낮음 |
| **Error** | **Rose** | `#F43F5E` | 에러, 통신 두절, 차단 |
| **Text** | **White/Gray** | `#F8FAFC` | 기본 텍스트 (가독성 최우선) |

## 3. Key UI Components (Dashboard)

### 📸 Dashboard Mockup
![Smart Weighing Dashboard Mockup](smart_weighing_dashboard_mockup_1769582079553.png)

### 1) Real-time Weight Card (핵심)
- **Design**: 화면 중앙 또는 상단에 가장 크게 배치.
- **Content**: 현재 중량값(kg)을 7-segment 디지털 폰트 스타일로 크게 표시.
- **Interaction**: 중량 안정화 시 색상 변경 (Gray → Cyan) 또는 테두리 발광 효과.

### 2) Live Monitoring Panel
- **LPR Camera**: 차량 진입 시 실시간 스냅샷 또는 스트리밍 영상 표시.
- **Status Badge**: "차량 진입", "인식 중", "계량 완료" 등의 상태를 뱃지로 표시.

### 3) Data Grid (Recent History)
- **Style**: Ant Design Table을 커스터마이징하여 테두리를 없애고, 행 간격을 넓혀 가독성 확보.
- **Features**: 최근 10건의 계량 이력을 표시하며, 이상 데이터(오차 범위 초과 등)는 행 배경색으로 강조.

### 4) Mobile Driver App
- **Simple & Big**: 운전자가 장갑을 끼고도 조작하기 쉽도록 버튼과 텍스트를 큼직하게 배치.
- **Step-by-Step**: 복잡한 한 화면 대신, 단계별(차량번호 확인 -> OTP 입력 -> 완료) 위저드 방식 적용.

## 4. Implementation Strategy (React + Ant Design)

### 🛠 Ant Design ConfigProvider
Ant Design의 강력한 테마 기능을 활용하여 손쉽게 스타일을 적용할 수 있습니다.

```typescript
// themeConfig.ts
import { ThemeConfig } from 'antd';

export const darkTheme: ThemeConfig = {
  token: {
    colorPrimary: '#06B6D4', // Neon Cyan
    colorBgBase: '#0B1120', // Deep Navy
    colorTextBase: '#F8FAFC',
    borderRadius: 8,
    fontFamily: "'Inter', sans-serif",
  },
  components: {
    Card: {
      colorBgContainer: '#1E293B', // Charcoal
      boxShadowSecondary: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    },
    Table: {
      colorBgContainer: '#1E293B',
      headerBg: '#0F172A',
    }
  }
};
```

### 💡 UX Recommendations
1.  **Sound Feedback**: 계량 완료, 에러 발생 시 시각적 알림뿐만 아니라 효과음(띵동, 삐-)을 함께 제공하여 운영 효율성 증대.
2.  **Keyboard Shortcuts**: 마우스를 쓰기 어려운 현장 상황을 고려하여, 주요 기능(계량 확정, 초기화 등)에 단축키(F1~F12) 지원.
3.  **Responsive**: 관리자 웹은 데스크탑/태블릿 해상도에 최적화하되, 모바일에서도 조회 가능하도록 반응형 처리.

## 5. Mobile App Design (Driver App)

### 📱 Mobile App Mockup
![Smart Weighing Mobile App Mockup](smart_weighing_mobile_app_mockup_1769582889213.png)

### 🎯 Design Principles
1.  **Big & Bold (크고 명확하게)**: 운전 중이거나 장갑을 낀 상태에서도 오터치 없이 조작할 수 있도록 버튼 높이를 최소 **56dp** 이상으로 설계합니다.
2.  **High Contrast (고대비)**: 야외 직사광선 아래에서도 화면이 잘 보이도록 배경(Deep Navy)과 텍스트(White/Neon)의 대비를 극대화합니다.
3.  **Linear Flow (선형적 흐름)**: 한 화면에 하나의 핵심 작업만 수행하도록 유도합니다. (예: 로그인 → 대기 → 계량 확인 → 완료)

### 🛠 Key Screens
1.  **OTP Login**: 복잡한 아이디/비번 대신, 숫자 키패드를 크게 배치하여 간편하게 인증 코드를 입력합니다.
2.  **Weighing Status**: 현재 계량 진행 상태를 원형 프로그레스 바와 큰 아이콘으로 시각화하여 직관적으로 전달합니다.
3.  **Digital Slip**: 계량 완료 후 종이 영수증 대신, 핵심 정보(중량, 시간, 차량번호)가 담긴 디지털 계량표를 카드 형태로 제공합니다.
