# Khuyến nghị UI/UX: Hệ thống Cân thông minh Busan

## 1. Khái niệm Thiết kế: "Modern Industrial Intelligence"
Chúng tôi đề xuất **"Modern Industrial Intelligence"** làm khái niệm UI/UX cho Hệ thống Cân thông minh Busan.
Thoát khỏi phần mềm công nghiệp truyền thống thô kệch, chúng tôi hướng tới một giao diện tương lai và trực quan, gợi nhớ đến **trung tâm điều khiển trong phim khoa học viễn tưởng**.

### Giá trị Cốt lõi
1.  **Visibility (Khả năng hiển thị)**: Thông tin quan trọng (trọng lượng, biển số xe) phải hiển thị rõ ràng trong mọi môi trường, bao gồm ngoài trời/trong nhà và ban ngày/ban đêm.
2.  **Real-time (Thời gian thực)**: Tiến trình cân, xe vào cổng và kết quả nhận dạng phải phản hồi ngay lập tức mà không có độ trễ.
3.  **Trust (Độ tin cậy)**: Sử dụng bố cục gọn gàng và có tổ chức để truyền tải trực quan độ chính xác của dữ liệu.

## 2. Nhận diện Thương hiệu & Bảng Màu
Chúng tôi đề xuất **Dark Mode** làm mặc định nhằm giảm mỏi mắt cho nhân viên giám sát làm việc ca dài và nâng cao khả năng tập trung vào thông tin.

### Bảng Màu (Dark Theme)
| Vai trò | Màu sắc | Mã Hex | Cách sử dụng |
| :--- | :--- | :--- | :--- |
| **Background** | **Deep Navy** | `#0B1120` | Màu nền chính (xanh navy đậm tạo chiều sâu) |
| **Surface** | **Charcoal** | `#1E293B` | Nền card và panel (có thể áp dụng hiệu ứng Glassmorphism) |
| **Primary** | **Neon Cyan** | `#06B6D4` | Dữ liệu chính (giá trị trọng lượng hiện tại), nút chính |
| **Success** | **Emerald** | `#10B981` | Cân hoàn tất, nhận dạng thành công, hệ thống bình thường |
| **Warning** | **Amber** | `#F59E0B` | Cảnh báo, cần cân lại, độ tin cậy nhận dạng thấp |
| **Error** | **Rose** | `#F43F5E` | Lỗi, mất kết nối, bị chặn |
| **Text** | **White/Gray** | `#F8FAFC` | Văn bản mặc định (ưu tiên khả năng đọc) |

## 3. Các Thành phần UI Chính (Dashboard)

### Dashboard Mockup
![Smart Weighing Dashboard Mockup](smart_weighing_dashboard_mockup_1769582079553.png)

### 1) Card Trọng lượng Thời gian thực (Cốt lõi)
- **Design**: Được đặt ở vị trí lớn nhất, ở trung tâm hoặc phía trên cùng màn hình.
- **Content**: Hiển thị giá trị trọng lượng hiện tại (kg) với kiểu font số 7-segment cỡ lớn.
- **Interaction**: Thay đổi màu khi trọng lượng ổn định (Xám sang Cyan) hoặc hiệu ứng viền phát sáng.

### 2) Panel Giám sát Trực tiếp
- **LPR Camera**: Hiển thị ảnh chụp nhanh thời gian thực hoặc video streaming khi xe vào cổng.
- **Status Badge**: Hiển thị trạng thái như "Xe đang vào", "Đang nhận dạng", "Cân hoàn tất" dưới dạng badge.

### 3) Lưới Dữ liệu (Lịch sử Gần đây)
- **Style**: Tùy chỉnh Ant Design Table bằng cách loại bỏ đường viền và tăng khoảng cách giữa các hàng để cải thiện khả năng đọc.
- **Features**: Hiển thị 10 bản ghi cân gần nhất; dữ liệu bất thường (ví dụ: vượt biên sai số) được tô nổi bằng màu nền hàng.

### 4) Ứng dụng Di động cho Tài xế
- **Simple & Big**: Nút bấm và văn bản được thiết kế lớn để tài xế có thể thao tác dễ dàng ngay cả khi đeo găng tay.
- **Step-by-Step**: Thay vì một màn hình phức tạp, áp dụng phương pháp wizard theo từng bước (Xác nhận Biển số xe -> Nhập OTP -> Hoàn tất).

## 4. Chiến lược Triển khai (React + Ant Design)

### Ant Design ConfigProvider
Khả năng tùy chỉnh theme mạnh mẽ của Ant Design cho phép áp dụng style một cách dễ dàng.

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

### Khuyến nghị UX
1.  **Sound Feedback**: Ngoài thông báo trực quan, cung cấp hiệu ứng âm thanh (tiếng chuông, tiếng bíp) khi cân hoàn tất hoặc xảy ra lỗi để nâng cao hiệu quả vận hành.
2.  **Keyboard Shortcuts**: Xét đến điều kiện hiện trường khó sử dụng chuột, hỗ trợ phím tắt (F1-F12) cho các chức năng chính (xác nhận cân, đặt lại, v.v.).
3.  **Responsive**: Tối ưu hóa giao diện web quản trị cho độ phân giải desktop/tablet, đồng thời đảm bảo hỗ trợ responsive để xem được trên di động.

## 5. Thiết kế Ứng dụng Di động (Driver App)

### Mobile App Mockup
![Smart Weighing Mobile App Mockup](smart_weighing_mobile_app_mockup_1769582889213.png)

### Nguyên tắc Thiết kế
1.  **Big & Bold (Lớn & Rõ ràng)**: Thiết kế nút bấm với chiều cao tối thiểu **56dp** trở lên để có thể thao tác không chạm nhầm ngay cả khi đang lái xe hoặc đeo găng tay.
2.  **High Contrast (Độ tương phản cao)**: Tối đa hóa độ tương phản giữa nền (Deep Navy) và văn bản (White/Neon) để đảm bảo hiển thị rõ ràng dưới ánh sáng mặt trời trực tiếp ngoài trời.
3.  **Linear Flow (Luồng tuyến tính)**: Hướng dẫn người dùng chỉ thực hiện một tác vụ chính trên mỗi màn hình. (ví dụ: Đăng nhập -> Chờ -> Xác nhận Cân -> Hoàn tất)

### Màn hình Chính
1.  **OTP Login**: Thay vì nhập tên đăng nhập/mật khẩu phức tạp, cung cấp bàn phím số lớn để nhập mã xác thực đơn giản.
2.  **Weighing Status**: Trực quan hóa tiến trình cân hiện tại một cách sinh động bằng thanh tiến trình hình tròn và biểu tượng lớn.
3.  **Digital Slip**: Sau khi cân hoàn tất, cung cấp phiếu cân kỹ thuật số dưới dạng card chứa thông tin chính (trọng lượng, thời gian, biển số xe) thay vì biên lai giấy.
