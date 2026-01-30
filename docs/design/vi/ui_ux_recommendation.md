# Tài liệu Thiết kế UI/UX: Hệ thống Cân thông minh Busan

## 1. Khái niệm Thiết kế: "Modern Industrial Intelligence"

Khái niệm UI/UX của Hệ thống Cân thông minh Busan là **"Modern Industrial Intelligence"**. Thoát khỏi giao diện thô kệch của phần mềm công nghiệp truyền thống, hệ thống hướng tới giao diện trực quan và mang tính tương lai, giống như **trung tâm điều khiển trong phim khoa học viễn tưởng**.

### Giá trị Cốt lõi (Core Values)

| Giá trị | Mô tả |
|---------|-------|
| **Visibility (Khả năng hiển thị)** | Thông tin cốt lõi (trọng lượng, biển số xe) hiển thị rõ ràng trong mọi môi trường: ngoài trời/trong nhà, ban ngày/ban đêm |
| **Real-time (Thời gian thực)** | Tiến trình cân, xe vào cổng, kết quả nhận dạng được phản hồi tức thì không có độ trễ |
| **Trust (Độ tin cậy)** | Sử dụng bố cục gọn gàng, ngăn nắp để truyền đạt trực quan tính chính xác của dữ liệu |
| **Consistency (Tính nhất quán)** | Chia sẻ cùng một ngôn ngữ thiết kế trên ba nền tảng: Web, Mobile, Desktop |

---

## 2. Nhận diện Hình ảnh & Bảng Màu

Để giảm mỏi mắt cho nhân viên giám sát phải theo dõi trong thời gian dài và tăng khả năng tập trung thông tin, hệ thống cung cấp **Chế độ tối (Dark Mode)** làm mặc định, đồng thời cũng hỗ trợ **Chế độ sáng (Light Mode)**.

### 2.1 Bảng Màu (Dựa trên Dark Theme)

Được thiết kế dựa trên bảng màu Tailwind CSS Slate.

| Vai trò | Tên màu | HEX | Công dụng |
|---------|---------|-----|-----------|
| **Background Darkest** | Deep Navy | `#060D1B` | Nền header/footer, lớp sâu nhất |
| **Background Base** | Dark Navy | `#0B1120` | Màu nền chính (xanh navy tối có chiều sâu) |
| **Background Elevated** | Slate 900 | `#0F172A` | Nền trường nhập liệu, header bảng |
| **Surface** | Charcoal | `#1E293B` | Nền thẻ (card), bảng điều khiển (hiệu ứng Glassmorphism) |
| **Border** | Slate 700 | `#334155` | Đường viền, đường phân cách |
| **Primary** | Neon Cyan | `#06B6D4` | Giá trị trọng lượng hiện tại, nút chính, trạng thái hoạt động |
| **Primary Dark** | Cyan 700 | `#0E7490` | Hover của Primary, nhấn mạnh phụ |
| **Success** | Emerald | `#10B981` | Hoàn thành cân, nhận dạng thành công, hệ thống bình thường |
| **Warning** | Amber | `#F59E0B` | Cảnh báo, cần cân lại, độ tin cậy nhận dạng thấp |
| **Error** | Rose | `#F43F5E` | Lỗi, mất kết nối, chặn |
| **Text Primary** | Slate 50 | `#F8FAFC` | Văn bản chính |
| **Text Secondary** | Slate 400 | `#94A3B8` | Văn bản phụ, nhãn |
| **Text Muted** | Slate 500 | `#64748B` | Văn bản không hoạt động, gợi ý |

### 2.2 Chuyển đổi Theme Tối/Sáng

Tất cả các nền tảng (Web, Desktop) đều hỗ trợ chuyển đổi theme Tối ↔ Sáng.

| Nền tảng | Phương thức chuyển đổi | Phương thức lưu trữ |
|----------|------------------------|---------------------|
| Web (React) | Nút toggle trên vùng header | `ThemeContext` + `localStorage` |
| Desktop (WeighingCS) | Icon toggle trên HeaderBar (Tối=🌙, Sáng=☀) | File `theme.dat` |

**Cấu hình Theme Web** (`frontend/src/theme/themeConfig.ts`):

```typescript
// Ant Design 5 ConfigProvider theme token
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

## 3. Hệ thống Thiết kế Frontend Web (React + Ant Design)

### 3.1 Cấu trúc Bố cục

**MainLayout** (`frontend/src/layouts/MainLayout.tsx`) là bố cục cơ bản của tất cả các trang.

```
┌──────────────────────────────────────────────────────────┐
│  Sider (có thể thu gọn)  │  Header (tab điều hướng + yêu thích)  │
│  ┌───────────────┐       │  ┌──────────────────────────────┐     │
│  │ Logo          │       │  │ [Tab1] [Tab2] ... [+]        │     │
│  │ Mục menu      │       │  ├──────────────────────────────┤     │
│  │ - Dashboard   │       │  │                              │     │
│  │ - Quản lý     │       │  │    Vùng nội dung (theo route)│     │
│  │   điều phối   │       │  │                              │     │
│  │ - Tình trạng  │       │  │                              │     │
│  │   cân         │       │  │                              │     │
│  │ - ...         │       │  │                              │     │
│  │ Menu quản trị │       │  │                              │     │
│  │ - Dữ liệu    │       │  │                              │     │
│  │   cơ sở       │       │  └──────────────────────────────┘     │
│  │ - Quản lý     │       │                                       │
│  │   hệ thống   │       │                                       │
│  └───────────────┘       │                                       │
└──────────────────────────────────────────────────────────┘
```

**Đặc điểm bố cục chính**:

| Đặc điểm | Mô tả |
|-----------|-------|
| Điều hướng đa tab | Quản lý đồng thời tối đa 10 tab với `TabContext`, hỗ trợ tab cố định (Trạm cân) |
| Thu gọn sidebar | Tối ưu hóa diện tích màn hình với tính năng collapsible của Ant Design Sider |
| Menu theo quyền | Định nghĩa `roles` theo route trong `pageRegistry.ts` (ADMIN, MANAGER, DRIVER) |
| Tách code React.lazy | Tối ưu tải ban đầu bằng cách lazy loading tất cả trang với `React.lazy` |

### 3.2 Registry Trang (pageRegistry.ts)

`pageRegistry.ts` quản lý tập trung tất cả thông tin trang, chuẩn hóa route, icon, quyền và lazy loading.

| Đường dẫn | Trang | Quyền | Mô tả |
|------------|-------|-------|-------|
| `/dashboard` | DashboardPage | Tất cả | Dashboard (3 tab: Tổng quan/Thời gian thực/Phân tích) |
| `/dispatch` | DispatchPage | Tất cả | Quản lý điều phối |
| `/weighing` | WeighingPage | Tất cả | Tình trạng cân |
| `/inquiry` | InquiryPage | Tất cả | Tra cứu cân |
| `/gate-pass` | GatePassPage | Tất cả | Quản lý phiếu ra cổng |
| `/slips` | SlipPage | Tất cả | Phiếu cân điện tử |
| `/statistics` | StatisticsPage | Tất cả | Thống kê/Báo cáo |
| `/weighing-station` | WeighingStationPage | Tất cả | Điều khiển trạm cân (tab cố định) |
| `/monitoring` | MonitoringPage | Tất cả | Giám sát thiết bị |
| `/notices` | NoticePage | Tất cả | Thông báo |
| `/help` | HelpPage | Tất cả | Hướng dẫn sử dụng |
| `/mypage` | MyPage | Tất cả | Trang cá nhân |
| `/master/*` | Master* Pages | ADMIN, MANAGER | Quản lý dữ liệu cơ sở (4 loại) |
| `/admin/*` | Admin* Pages | ADMIN | Quản lý hệ thống (3 loại) |

### 3.3 Thành phần UI Dùng chung

#### SortableTable (Bảng kéo thả sắp xếp)

Thành phần bao bọc Ant Design Table hỗ trợ kéo thả sắp xếp dựa trên `@dnd-kit`. Được sử dụng chung trong tất cả trang danh sách.

| Đặc điểm | Mô tả |
|-----------|-------|
| Kéo thả sắp xếp | Hỗ trợ sắp xếp hàng bằng kéo thả với `@dnd-kit/core` + `@dnd-kit/sortable` |
| fill-height | Tự động điều chỉnh chiều cao bảng theo container cha |
| Phân trang | Tích hợp Ant Design Pagination (kích thước trang mặc định 20) |
| Responsive | Tự động áp dụng cuộn theo kích thước container |

#### MasterCrudPage (CRUD dữ liệu cơ sở dùng chung)

Thành phần dùng chung chuẩn hóa mẫu CRUD cho các trang quản lý: công ty vận chuyển, xe, trạm cân, mã dùng chung.

```
┌──────────────────────────────────────┐
│  [Tiêu đề trang]       Nút [Thêm]   │
├──────────────────────────────────────┤
│  [Ô tìm kiếm] [Tra cứu] [Đặt lại]  │
├──────────────────────────────────────┤
│  SortableTable (danh sách)           │
│  - Nút sửa → Modal chỉnh sửa       │
│  - Nút xóa → Popconfirm xác nhận    │
├──────────────────────────────────────┤
│  Modal Tạo/Sửa (Ant Design Form)    │
│  - Quy tắc xác thực chung từ        │
│    validators.ts                     │
└──────────────────────────────────────┘
```

#### AnimatedNumber (Hoạt ảnh số)

Thành phần hiển thị sự thay đổi giá trị số bằng hoạt ảnh đếm lên mượt mà trên thẻ KPI của Dashboard.

#### OnboardingTour (Hướng dẫn khởi đầu)

Hướng dẫn khởi đầu cho người dùng mới sử dụng thành phần Tour của Ant Design. Hướng dẫn từng bước các tính năng chính khi truy cập lần đầu.

#### EmptyState (Trạng thái không có dữ liệu)

Thành phần hướng dẫn trực quan hiển thị khi không có dữ liệu. Kết hợp icon + mô tả + nút hành động.

#### FavoriteButton / FavoritesList (Yêu thích)

Tính năng cho phép thêm các trang thường dùng vào danh sách yêu thích để truy cập nhanh.

### 3.4 Hooks Quản lý Trạng thái

| Hook | Công dụng |
|------|-----------|
| `useApiCall` | Bao bọc gọi API (tự động quản lý trạng thái loading/error) |
| `useCrudState` | Quản lý trạng thái chung trang CRUD (danh sách, chọn, điều khiển modal) |
| `useKeyboardShortcuts` | Đăng ký/hủy phím tắt |
| `useTabVisible` | Phát hiện tab trình duyệt hoạt động/không hoạt động |
| `useWebSocket` | Quản lý kết nối/đăng ký STOMP WebSocket |
| `useWeighingStation` | Quản lý tích hợp logic nghiệp vụ điều khiển trạm cân |
| `useWeighingStationSocket` | Đăng ký WebSocket chuyên dụng cho trạm cân |

### 3.5 Dashboard (DashboardPage)

Dashboard tích hợp gồm 3 tab.

```
┌─────────────────────────────────────────────┐
│  [Tổng quan]  [Thời gian thực]  [Phân tích] │
├─────────────────────────────────────────────┤
│  Tab Tổng quan (OverviewTab):                │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐       │
│  │Tổng  │ │Hoàn  │ │Đang  │ │Lũy kế│       │
│  │hôm   │ │thành │ │tiến  │ │tháng │       │
│  │nay   │ │hôm   │ │hành  │ │      │       │
│  │  42  │ │nay   │ │   4  │ │ 850  │       │
│  │      │ │  38  │ │      │ │      │       │
│  │(AnimatedNumber)                          │
│  └──────┘ └──────┘ └──────┘ └──────┘       │
│  ┌───────────────────┐ ┌───────────────┐    │
│  │ Biểu đồ xu hướng │ │ Phân bố theo  │    │
│  │ theo ngày         │ │ mặt hàng      │    │
│  │ (ECharts Line)    │ │ (ECharts Pie) │    │
│  └───────────────────┘ └───────────────┘    │
├─────────────────────────────────────────────┤
│  Tab Thời gian thực (RealtimeTab):           │
│  - Giám sát trạng thái cân thời gian thực   │
│    dựa trên WebSocket                        │
│  - Cập nhật thay đổi trọng lượng, trạng     │
│    thái thiết bị theo thời gian thực         │
├─────────────────────────────────────────────┤
│  Tab Phân tích (AnalysisTab):                │
│  - Thống kê chi tiết theo kỳ/mặt hàng/      │
│    chế độ                                    │
│  - Biểu đồ tương tác dựa trên ECharts 6.0  │
└─────────────────────────────────────────────┘
```

### 3.6 Điều khiển Trạm cân (WeighingStationPage)

Màn hình điều khiển cốt lõi để nhân viên vận hành trạm cân giám sát và điều khiển quy trình cân theo thời gian thực. Hiển thị dưới dạng tab cố định luôn nằm trên thanh tab.

```
┌─────────────────────────────────────────────────────────┐
│  Bảng bên trái (Vùng hiển thị) │  Bảng bên phải (Vùng  │
│                                 │  điều khiển)           │
│  ┌───────────────────────┐     │  ┌───────────────────┐ │
│  │ WeightDisplay         │     │  │ ModeToggle        │ │
│  │ 45,200.5 kg  [STABLE] │     │  │ [AUTO] / [MANUAL] │ │
│  │ (hiển thị số 72px)    │     │  └───────────────────┘ │
│  └───────────────────────┘     │  ┌───────────────────┐ │
│  ┌───────────────────────┐     │  │ ProcessStateBar   │ │
│  │ VehicleInfoPanel      │     │  │ ○───○───○───●     │ │
│  │ 12가3456 | Dongkuk    │     │  │ Chờ  Cân  Ổn định│ │
│  │ Logistics             │     │  │              Xong │ │
│  │ Thép | DIS-0101       │     │  └───────────────────┘ │
│  └───────────────────────┘     │  ┌───────────────────┐ │
│  ┌───────────────────────┐     │  │ ManualControls    │ │
│  │ ConnectionStatusBar   │     │  │ [Tìm biển số xe]  │ │
│  │ ● Cân ● Bảng điện tử │     │  │ [Chọn điều phối]  │ │
│  │ ● Barrier ● Mạng     │     │  │ [Bắt đầu cân]     │ │
│  └───────────────────────┘     │  └───────────────────┘ │
│  ┌───────────────────────┐     │  ┌───────────────────┐ │
│  │ WeighingHistoryTable  │     │  │ ActionButtons     │ │
│  │ Lịch sử cân gần đây  │     │  │ [Đặt lại][Barrier]│ │
│  │ (SortableTable)       │     │  └───────────────────┘ │
│  └───────────────────────┘     │  ┌───────────────────┐ │
│                                 │  │ StatusLog         │ │
│                                 │  │ Nhật ký kiểu      │ │
│                                 │  │ terminal          │ │
│                                 │  └───────────────────┘ │
│                                 │  ┌───────────────────┐ │
│                                 │  │ SimulatorPanel    │ │
│                                 │  │ [DEV] Trình mô    │ │
│                                 │  │ phỏng             │ │
│                                 │  └───────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

**10 thành phần con** (`frontend/src/components/weighing-station/`):

| Thành phần | Chức năng |
|------------|-----------|
| `WeightDisplay` | Hiển thị số trọng lượng thời gian thực (monospace 72px, hiệu ứng phát sáng) |
| `VehicleInfoPanel` | Hiển thị thông tin xe/điều phối/công ty vận chuyển (lưới icon 5 hàng) |
| `ConnectionStatusBar` | LED trạng thái kết nối 4 thiết bị (Cân/Bảng điện tử/Barrier/Mạng) |
| `ModeToggle` | Chuyển đổi chế độ Tự động (AUTO LPR) / Thủ công (MANUAL) |
| `ManualControls` | Tìm kiếm xe và điều khiển bắt đầu cân ở chế độ thủ công |
| `ActionButtons` | Nút Đặt lại, Mở barrier, Cân lại |
| `ProcessStateBar` | Hiển thị tiến trình 4 bước (IDLE→WEIGHING→STABILIZING→COMPLETE) |
| `StatusLog` | Nhật ký sự kiện thời gian thực kiểu terminal (nền tối, tối đa 200 mục) |
| `SimulatorPanel` | Bảng mô phỏng phần cứng cho phát triển/kiểm thử |
| `WeighingHistoryTable` | Bảng lịch sử 50 lần cân gần nhất |

### 3.7 Nguyên tắc UX

| Nguyên tắc | Mô tả |
|------------|-------|
| **Sound Feedback** | Kết hợp hiệu ứng hình ảnh + âm thanh khi hoàn thành cân/xảy ra lỗi (hiệu quả vận hành) |
| **Keyboard Shortcuts** | Truy cập các tính năng chính không cần chuột (hook `useKeyboardShortcuts`) |
| **Responsive** | Tối ưu cho độ phân giải desktop/tablet, responsive cho mobile |
| **Accessibility** | Hỗ trợ trình đọc màn hình với thuộc tính `aria-live` |
| **Auto-refresh** | Tự động làm mới dữ liệu thời gian thực qua WebSocket (STOMP over SockJS) |

---

## 4. Thiết kế Ứng dụng Di động (Flutter)

### 4.1 Nguyên tắc Thiết kế

| Nguyên tắc | Mô tả |
|------------|-------|
| **Big & Bold (Lớn & Rõ ràng)** | Chiều cao nút tối thiểu **56dp** trở lên để có thể thao tác khi đeo găng tay |
| **High Contrast (Tương phản cao)** | Tối đa hóa tương phản Deep Navy + White/Neon để nhìn rõ dưới ánh nắng trực tiếp ngoài trời |
| **Linear Flow (Luồng tuyến tính)** | Mỗi màn hình chỉ thực hiện một tác vụ chính (Đăng nhập→Chờ→Xác nhận cân→Hoàn thành) |
| **Offline Resilience (Khả năng offline)** | Bộ nhớ đệm offline dựa trên SharedPreferences để đối phó với mạng không ổn định |

### 4.2 Bảng Màu (`app_colors.dart`)

Sử dụng bảng màu dựa trên Tailwind Slate giống như Web trong Flutter.

| Màu | HEX | Công dụng |
|-----|-----|-----------|
| Primary | `#06B6D4` | Màu nhấn chính (cyan) |
| Background | `#0B1120` | Nền chính |
| Surface | `#1E293B` | Nền thẻ |
| Success | `#10B981` | Trạng thái hoàn thành |
| Warning | `#F59E0B` | Trạng thái cảnh báo |
| Error | `#F43F5E` | Trạng thái lỗi |

### 4.3 Cấu trúc Màn hình (12 Screen)

```
mobile/lib/screens/
├── login_screen.dart              # Đăng nhập ID/PW (hiệu ứng Glassmorphism)
├── home_screen.dart               # Trang chủ (điều hướng tab dưới cùng)
├── auth/
│   └── otp_login_screen.dart      # Đăng nhập OTP
├── dispatch/
│   ├── dispatch_list_screen.dart  # Danh sách điều phối
│   └── dispatch_detail_screen.dart# Chi tiết điều phối
├── weighing/
│   ├── weighing_progress_screen.dart  # Tiến trình cân
│   └── otp_input_screen.dart          # Nhập OTP (bàn phím tùy chỉnh 6 chữ số)
├── slip/
│   ├── slip_list_screen.dart      # Danh sách phiếu cân điện tử
│   └── slip_detail_screen.dart    # Chi tiết phiếu cân điện tử
├── history/
│   └── history_screen.dart        # Lịch sử cân/điều phối
└── notice/
    ├── notice_screen.dart         # Thông báo
    └── notification_list_screen.dart  # Danh sách thông báo
```

### 4.4 Thiết kế Màn hình Chính

#### Màn hình Đăng nhập (LoginScreen)

Thẻ đăng nhập với hiệu ứng Glassmorphism. Nền có gradient + làm mờ (blur).

```
┌──────────────────────────────┐
│     [Nền blur + gradient]    │
│  ┌────────────────────────┐  │
│  │  🏭  Cân thông minh     │  │
│  │      Busan              │  │
│  │                        │  │
│  │  [Nhập mã nhân viên]   │  │
│  │  [Nhập mật khẩu]       │  │
│  │                        │  │
│  │  [     Đăng nhập     ] │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

#### Màn hình Trang chủ (HomeScreen)

Truy cập các chức năng chính qua điều hướng tab ở dưới cùng.

| Vai trò | Cấu trúc tab |
|---------|--------------|
| MANAGER | Trang chủ, Điều phối, Cân, Phiếu cân, Thêm (5 tab) |
| DRIVER | Trang chủ, Điều phối, Cân, Thêm (4 tab) |

#### Màn hình Nhập OTP (OtpInputScreen)

Màn hình chuyên dụng để nhập mã OTP 6 chữ số hiển thị trên bảng điện tử.

```
┌──────────────────────────────┐
│         < Xác thực OTP       │
│                              │
│        DIS-2026-0101         │
│   Nhập mã OTP 6 chữ số      │
│         [ 04:32 ]            │
│                              │
│   [1] [2] [3] [4] [5] [6]   │
│                              │
│       [1] [2] [3]            │
│       [4] [5] [6]            │
│       [7] [8] [9]            │
│       [C] [0] [<]            │
│                              │
│   [      Xác thực       ]    │
└──────────────────────────────┘
```

- Bộ đếm ngược 5 phút (MM:SS)
- Hiển thị nút "Yêu cầu lại OTP" khi hết hạn
- Bàn phím số tùy chỉnh 4x3

#### Màn hình Tiến trình Cân (WeighingProgressScreen)

Hiển thị trạng thái cân hiện tại theo từng bước dạng thẻ (card).

```
┌──────────────────────────────┐
│  Mã điều phối: DIS-2026-0101│
│  12가3456 | Dongkuk  [Lần 1] │
│  Logistics                   │
│                              │
│  Trạng thái tiến trình  33% │
│  [========----------]        │
│  Chờ   Lần 1  Lần 2  Xong  │
│                              │
│  Tổng trọng lượng: 45,201 kg│
│  Xe không: -                 │
│  Trọng lượng ròng: -        │
│                              │
│  [      Xác thực OTP     ]  │
└──────────────────────────────┘
```

- Tự động làm mới mỗi 10 giây (Timer)
- Hỗ trợ Pull-to-Refresh
- Hiển thị hộp thoại khi phát hiện hoàn thành

#### Chi tiết Phiếu cân Điện tử (SlipDetailScreen)

Phiếu cân số thay thế biên lai giấy. Bao gồm tính năng chia sẻ.

| Phương thức chia sẻ | Màu icon | Mô tả |
|---------------------|----------|-------|
| KakaoTalk | `#FEE500` | Chia sẻ qua KakaoTalk |
| SMS | `#06B6D4` | Chia sẻ qua tin nhắn SMS |
| Khác | `#334155` | Bảng chia sẻ OS qua `share_plus` |

### 4.5 Widget Dùng chung

| Widget | Công dụng |
|--------|-----------|
| `AppDrawer` | Ngăn kéo điều hướng (menu bên) |
| `StatusBadge` | Huy hiệu màu theo trạng thái (Chờ=Vàng, Xong=Xanh lá, Lỗi=Đỏ) |
| `WeightDisplayCard` | Thẻ hiển thị trọng lượng (3 cột: Tổng/Xe không/Ròng) |

### 4.6 Quản lý Trạng thái và Định tuyến

| Công nghệ | Vai trò |
|------------|---------|
| Provider | Quản lý trạng thái (AuthProvider, DispatchProvider) |
| GoRouter | Định tuyến khai báo + chuyển hướng xác thực |
| Dio | HTTP client + JWT interceptor |
| flutter_secure_storage | Lưu trữ token an toàn |
| Firebase Messaging | Thông báo đẩy FCM (Kênh Android: `busan_weighing_channel`) |

---

## 5. Thiết kế Chương trình Desktop (WeighingCS - C# WinForms)

### 5.1 Hệ thống Thiết kế

WeighingCS triển khai giao diện dark theme cấp độ web trên WinForms thông qua custom rendering bằng GDI+. Tất cả các điều khiển được vẽ trực tiếp trong `OnPaint` (AntiAlias, ClearTypeGridFit).

#### Design Token Theme (`Controls/Theme.cs`)

Hệ thống design token quản lý tập trung, sử dụng bảng màu Tailwind Slate giống như Web.

| Danh mục | Token | Giá trị | Công dụng |
|----------|-------|---------|-----------|
| Nền | `BgDarkest` | `#060D1B` | Header/Footer |
| Nền | `BgBase` | `#0B1120` | Nền chính |
| Nền | `BgElevated` | `#0F172A` | Trường nhập liệu |
| Nền | `BgSurface` | `#1E293B` | Thẻ (Card) |
| Màu | `Primary` | `#06B6D4` | Màu nhấn chính |
| Màu | `Success` | `#10B981` | Thành công |
| Màu | `Warning` | `#F59E0B` | Cảnh báo |
| Màu | `Error` | `#F43E5E` | Lỗi |
| Tỷ lệ | `FontScale` | `1.5f` | Hệ số kích thước phông chữ |
| Tỷ lệ | `LayoutScale` | `1.25f` | Hệ số bố cục/khoảng cách |

**Hệ thống Phông chữ**:

| Token | Phông chữ | Kích thước | Công dụng |
|-------|-----------|------------|-----------|
| `FontBody` | Segoe UI | 9.5pt × FontScale | Nội dung |
| `FontHeading` | Segoe UI Bold | 11pt × FontScale | Tiêu đề |
| `FontCaption` | Segoe UI | 8pt × FontScale | Chú thích |
| `FontMono` | Consolas | 10pt × FontScale | Monospace |
| `FontMonoLarge` | Consolas Bold | 32pt × FontScale | Hiển thị trọng lượng |

**Tiện ích chung**: Các hàm chuyển đổi màu `WithAlpha()`, `Lighten()`, `Darken()`, `Blend()`.

> **Lưu ý**: Phông chữ Theme là bộ nhớ đệm tĩnh nên không được tham chiếu bằng `using var`. `InvalidateFontCache()` chỉ đặt tham chiếu thành null mà không Dispose (các điều khiển vẫn có thể đang tham chiếu phông chữ trước đó, Dispose sẽ gây ra ngoại lệ "Parameter is not valid").

### 5.2 Cấu trúc Bố cục

Bố cục 3 phần: HeaderBar(Top) → panelContent(Fill: Left+Divider+Right) → StatusFooter(Bottom)

```
┌──────────────────────────────────────────────────────┐
│  HeaderBar (Dock.Top, 56px)                          │
│  [DK] Hệ thống Cân thông minh   ●Cân ●Bảng ĐT... HH:mm│
│       Busan                                          │
├───────────────────┬─┬────────────────────────────────┤
│  panelLeftCol     │ │  panelRightCol (Dock.Fill)     │
│  (420px, 35%)     │ │  ┌──────────────────────────┐  │
│  ┌──────────────┐ │ │  │ ModeToggle (44px)        │  │
│  │WeightDisplay │ │ │  │ ProcessStepBar (64px)    │  │
│  │(220px, phát  │ │ │  │ CardManual (185px)       │  │
│  │ sáng)        │ │ │  │ CardActions (88px)       │  │
│  ├──────────────┤ │ │  │ CardSimulator (130px)    │  │
│  │CardVehicle   │ │ │  │ TerminalLog (Fill)       │  │
│  │(250px, 5 hàng)│ │ │  └──────────────────────────┘  │
│  ├──────────────┤ │ │                                │
│  │CardHistory   │ │ │                                │
│  │(Fill, danh   │ │ │                                │
│  │ sách)        │ │ │                                │
│  └──────────────┘ │ │                                │
├───────────────────┴─┴────────────────────────────────┤
│  StatusFooter (Dock.Bottom, 32px)                    │
│  Trạm cân#1 · COM1 · 9600bps  ● Chế độ tự động     │
│  v1.0.0 HH:mm:ss                                    │
└──────────────────────────────────────────────────────┘
```

### 5.3 Điều khiển Tùy chỉnh (16 loại)

| Điều khiển | Mô tả |
|------------|-------|
| **HeaderBar** | Header trên cùng (logo, tiêu đề, toggle theme, LED thiết bị, đồng hồ thời gian thực) |
| **StatusFooter** | Thanh trạng thái dưới cùng (thông tin trạm cân, chế độ, trạng thái đồng bộ, thời gian) |
| **WeightDisplayPanel** | Hiển thị số trọng lượng lớn (hiệu ứng phát sáng, huy hiệu ổn định) |
| **CardPanel** | Container thẻ hiệu ứng kính (bóng đổ, thanh accent) |
| **ModernButton** | 3 loại nút (Primary/Secondary/Danger, highlight kính) |
| **ModernToggle** | Toggle trượt (chuyển đổi chế độ Tự động/Thủ công, hoạt ảnh) |
| **ModernTextBox** | Nhập văn bản (viền bo tròn, phát sáng khi focus, placeholder) |
| **ModernComboBox** | ComboBox (dropdown tùy chỉnh, hiệu ứng focus) |
| **ModernCheckBox** | CheckBox (vẽ GDI+ tùy chỉnh, hiệu ứng hover) |
| **ModernListView** | ListView (màu hàng xen kẽ, header tùy chỉnh, cột cuối tự động lấp đầy) |
| **ModernProgressBar** | Thanh tiến trình (dùng cho màn hình splash) |
| **ProcessStepBar** | Hiển thị quy trình 4 bước (chỉ báo hình tròn, dấu tích) |
| **TerminalLogPanel** | Bảng nhật ký kiểu terminal (trang trí traffic light kiểu macOS) |
| **RoundedRectHelper** | Tiện ích GraphicsPath hình chữ nhật bo tròn |
| **ConnectionStatusPanel** | [Kế thừa] Bảng trạng thái kết nối (đã thay thế bởi HeaderBar) |
| **LedIndicator** | [Kế thừa] Chỉ báo LED (đã thay thế bởi HeaderBar) |

### 5.4 Chi tiết Điều khiển Cốt lõi

#### WeightDisplayPanel (Hiển thị Trọng lượng)

| Thành phần | Mô tả |
|------------|-------|
| Nền | Gradient dọc BgElevated→BgSurface + lớp phủ kính |
| Văn bản trọng lượng | Consolas 32~72pt Bold (tỷ lệ theo chiều rộng) |
| Hiệu ứng phát sáng | 4 lớp phát sáng Primary khi Stable |
| Huy hiệu ổn định | STABLE(xanh lá)/UNSTABLE(vàng)/ERROR(đỏ), thẻ tô tròn |
| Accent trái | Thanh dọc 4px theo trạng thái |

#### ProcessStepBar (Bước Quy trình)

```
○─────○─────○─────●
Chờ    Cân    Ổn định  Xong     [Xong ●]
```

| Trạng thái | Biểu diễn trực quan |
|------------|---------------------|
| Bước hoàn thành | Hình tròn tô màu Primary + dấu tích trắng, đường nối Primary |
| Bước hiện tại | Viền Primary + chấm giữa + phát sáng, nhãn Bold |
| Bước tương lai | Hình tròn viền Border, nhãn TextMuted |

#### TerminalLogPanel (Nhật ký Terminal)

- Nền tối (`#0D1117`) + phông chữ monospace
- Trang trí header kiểu traffic light macOS (hình tròn đỏ/vàng/xanh lá)
- Màu theo mức nhật ký: info(xám), success(xanh neon), warning(vàng), error(đỏ)
- Giữ tối đa 200 mục nhật ký, tự động cuộn

### 5.5 Giao tiếp Phần cứng

| Thiết bị | Giao thức | Dịch vụ |
|----------|-----------|---------|
| Indicator cân | Cổng nối tiếp (COM) | `IndicatorService` |
| Bảng điện tử | TCP Socket | `DisplayBoardService` |
| Barrier | TCP Socket | `BarrierService` |
| Máy chủ Backend | REST API (HTTP) | `ApiService` |

**Trừu tượng hóa dựa trên Interface**: Có thể thay thế giữa phần cứng thực và trình mô phỏng với `ILprCamera`, `IVehicleDetector`, `IVehicleSensor`.

**Chế độ Simulator**: Có thể phát triển/kiểm thử mà không cần phần cứng thực (LprCameraSimulator, VehicleDetectorSimulator, VehicleSensorSimulator).

### 5.6 Màn hình Splash (SplashForm)

Màn hình splash hiển thị trạng thái khởi tạo khi khởi động ứng dụng.

- Nền gradient + hiệu ứng phát sáng hướng tâm
- Hiển thị trạng thái tiến trình khởi tạo bằng ModernProgressBar
- Hiển thị logo + tên hệ thống + thông tin phiên bản

---

## 6. Tính nhất quán Thiết kế Đa nền tảng

### 6.1 Ánh xạ Thành phần Web ↔ Desktop

| Web (React) | Desktop (C# WinForms) | Sự khác biệt triển khai |
|------------|------------------------|--------------------------|
| `WeightDisplay` | `WeightDisplayPanel` | Vẽ trực tiếp bằng GDI+ |
| `VehicleInfoPanel` | `CardPanel` + `TableLayoutPanel` | Bảng 5 hàng trong thẻ |
| `ConnectionStatusBar` | `HeaderBar` (LED tích hợp) | Tích hợp vào header |
| `ModeToggle` | `ModernToggle` | Hoạt ảnh trượt |
| `ManualControls` | `CardPanel` + `ModernTextBox/ComboBox` | Mẫu Wrapper |
| `ActionButtons` | `CardPanel` + `ModernButton` | 3 loại nút |
| `ProcessStateBar` | `ProcessStepBar` | Chỉ báo hình tròn |
| `StatusLog` | `TerminalLogPanel` | Traffic light macOS |
| `SimulatorPanel` | `CardPanel` + `ModernCheckBox/Button` | Toggle trình mô phỏng |
| `WeighingHistoryTable` | `ModernListView` | OwnerDraw ListView |
| — | `StatusFooter` | Thanh dưới cùng chỉ có trên Desktop |

### 6.2 Nguyên tắc Thiết kế Chung

| Nguyên tắc | Web | Di động | Desktop |
|------------|-----|---------|---------|
| Bảng màu | Ant Design Theme Token | `app_colors.dart` | `Theme.cs` |
| Chế độ tối | `ThemeContext` | Cố định tối | Toggle Tối/Sáng |
| Giao tiếp thời gian thực | STOMP WebSocket | Polling 10 giây | REST API + COM |
| Hỗ trợ offline | — | SharedPreferences | SQLite |
| Màu trạng thái | `colors.success/warning/error` | Material Colors | Theme.Success/Warning/Error |

---

## 7. Tham khảo Mockup Thiết kế

### Mockup Dashboard Web
![Smart Weighing Dashboard Mockup](smart_weighing_dashboard_mockup_1769582079553.png)

### Mockup Ứng dụng Di động
![Smart Weighing Mobile App Mockup](smart_weighing_mobile_app_mockup_1769582889213.png)
