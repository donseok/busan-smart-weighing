# Hệ thống Cân thông minh Busan - Đặc tả Chức năng (Functional Specification)

**Phiên bản**: 1.1
**Ngày tạo**: 2026-01-27
**Cập nhật lần cuối**: 2026-01-29
**Tài liệu tham chiếu**: PRD-20260127-154446, TRD-20260127-155235, WBS-20260127-160043
**Trạng thái**: Updated

---

## Mục lục

1. [Tổng quan tài liệu](#1-tổng-quan-tài-liệu)
2. [Mô-đun 1: Hệ thống nhận dạng biển số xe LPR](#2-mô-đun-1-hệ-thống-nhận-dạng-biển-số-xe-lpr)
3. [Mô-đun 2: Hệ thống quản lý cân thông minh trên Web](#3-mô-đun-2-hệ-thống-quản-lý-cân-thông-minh-trên-web)
4. [Mô-đun 3: Chương trình CS cân](#4-mô-đun-3-chương-trình-cs-cân)
5. [Mô-đun 4: Ứng dụng di động quản lý cân](#5-mô-đun-4-ứng-dụng-di-động-quản-lý-cân)
6. [Mô-đun 5: API di động](#6-mô-đun-5-api-di-động)
7. [Mô-đun 6: Tích hợp hạ tầng H/W](#7-mô-đun-6-tích-hợp-hạ-tầng-hw)
8. [Mô-đun 7: Hệ thống phân luồng cuộc gọi cố định](#8-mô-đun-7-hệ-thống-phân-luồng-cuộc-gọi-cố-định)
9. [Ma trận truy vết yêu cầu](#9-ma-trận-truy-vết-yêu-cầu)

---

## 1. Tổng quan tài liệu

### 1.1 Mục đích
Tài liệu này định nghĩa chi tiết hoạt động của từng chức năng trong Hệ thống Cân thông minh Busan, dựa trên PRD, TRD và WBS. Được sử dụng làm tài liệu chuẩn cho phát triển, kiểm thử và nghiệm thu.

### 1.2 Phạm vi
Đặc tả chi tiết tất cả yêu cầu chức năng từ PRD FR-001 đến FR-008, phân loại thành 7 mô-đun.

### 1.3 Thuật ngữ
| Thuật ngữ | Định nghĩa |
|-----------|------------|
| LPR | License Plate Recognition - Thiết bị tự động nhận dạng biển số xe |
| OTP | One-Time Password - Mật khẩu bảo mật dùng một lần |
| LiDAR | Light Detection and Ranging - Cảm biến LiDAR |
| Indicator | Thiết bị hiển thị giá trị trọng lượng tại trạm cân |
| Phiếu cân điện tử | Chứng từ cân kỹ thuật số cung cấp qua ứng dụng di động |
| Điều phối xe | Phân công lịch trình vận chuyển cho xe |

---

## 2. Mô-đun 1: Hệ thống nhận dạng biển số xe LPR

### FUNC-001: Chụp tự động biển số xe bằng LPR

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-001 |
| **Tên chức năng** | Chụp tự động biển số xe bằng LPR |
| **Ánh xạ PRD** | FR-001 |
| **Mô-đun** | Hệ thống nhận dạng biển số xe LPR |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Khi cảm biến LiDAR/radar phát hiện xe tiến vào, camera LPR tự động chụp biển số xe.

**Điều kiện tiên quyết (Preconditions)**:
- Camera LPR hoạt động bình thường
- Cảm biến LiDAR/radar kết nối bình thường
- Chương trình CS trạm cân đang chạy

**Điều kiện hậu (Postconditions)**:
- Hình ảnh chụp được lưu trữ trên máy chủ
- Quy trình xác minh AI được kích hoạt

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| sensor_event | string | Y | Tín hiệu sự kiện phát hiện LiDAR/radar |
| scale_id | bigint | Y | Mã định danh trạm cân |
| timestamp | timestamptz | Y | Thời điểm phát hiện |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| lpr_image_path | string | Đường dẫn lưu trữ hình ảnh chụp |
| raw_plate_number | string | Biển số xe nhận dạng lần 1 bởi LPR |
| capture_timestamp | timestamptz | Thời điểm chụp |

**Quy tắc nghiệp vụ**:
- BR-001-1: Kích hoạt chụp LPR tại thời điểm cảm biến LiDAR/radar phát hiện
- BR-001-2: Hình ảnh chụp được lưu trữ trên NAS trong 90 ngày (chính sách sao lưu TRD)
- BR-001-3: Ngăn chặn chụp trùng lặp cho cùng một xe (khoảng cách tối thiểu 10 giây)

**Luồng chính (Main Flow)**:
1. Cảm biến LiDAR/radar phát hiện xe tiến vào
2. Sự kiện cảm biến được truyền qua TCP/UDP đến chương trình CS trạm cân
3. Chương trình CS gửi lệnh chụp đến camera LPR
4. Camera LPR chụp biển số xe
5. Thiết bị LPR trả về kết quả nhận dạng lần 1 (raw_plate_number)
6. Chương trình CS gửi hình ảnh chụp và kết quả nhận dạng lần 1 đến máy chủ API
7. Máy chủ API chuyển hình ảnh đến engine xác minh AI

**Luồng thay thế (Alternative Flow)**:
- AF-001-1: Khi nhận dạng LPR lần 1 thất bại, thử chụp lại 2 lần (cách nhau 1 giây)
- AF-001-2: Khi chụp ban đêm, tự động bật đèn hỗ trợ

**Luồng ngoại lệ (Exception Flow)**:
- EF-001-1: Mất kết nối camera LPR → Hiển thị cảnh báo trên chương trình CS, hướng dẫn chuyển sang chế độ cân thủ công
- EF-001-2: Lỗi phát hiện cảm biến (không có xe) → Hủy kết quả chụp, ghi log
- EF-001-3: Chụp thất bại 3 lần liên tiếp → Gửi thông báo cho quản trị viên, yêu cầu kiểm tra H/W

**Yêu cầu UI/UX**:
- Hiển thị trạng thái chụp LPR trên màn hình chính chương trình CS (Chờ/Đang chụp/Hoàn thành/Lỗi)
- Xem trước hình ảnh chụp theo thời gian thực

**Chức năng liên quan**: FUNC-002 (Xác minh biển số xe bằng AI), FUNC-010 (Tích hợp cảm biến)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: LPR chụp → Xác minh AI → Trả về kết quả trong vòng 3 giây
- NFR-004 Tính sẵn sàng: Chuyển sang chế độ thủ công khi thiết bị LPR gặp sự cố

---

### FUNC-002: Xác minh biển số xe bằng AI

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-002 |
| **Tên chức năng** | Xác minh biển số xe bằng AI |
| **Ánh xạ PRD** | FR-001 |
| **Mô-đun** | Hệ thống nhận dạng biển số xe LPR |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Engine AI xác minh lại kết quả nhận dạng lần 1 của LPR để xác nhận biển số xe và tính toán điểm độ tin cậy.

**Điều kiện tiên quyết (Preconditions)**:
- Hình ảnh chụp LPR tồn tại
- Engine nhận dạng AI hoạt động bình thường
- Máy chủ API hoạt động bình thường

**Điều kiện hậu (Postconditions)**:
- Biển số xe được xác nhận và lưu độ tin cậy
- Kích hoạt quy trình tự động khớp điều phối xe

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| lpr_image | binary | Y | File hình ảnh chụp |
| raw_plate_number | string | Y | Kết quả nhận dạng lần 1 của LPR |
| scale_id | bigint | Y | Mã định danh trạm cân |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| confirmed_plate_number | string | Biển số xe được AI xác nhận |
| ai_confidence | decimal(5,4) | Độ tin cậy (0.0~1.0) |
| verification_status | string | CONFIRMED / LOW_CONFIDENCE / FAILED |

**Quy tắc nghiệp vụ**:
- BR-002-1: ai_confidence >= 0.90 → CONFIRMED (tiến hành cân tự động)
- BR-002-2: 0.70 <= ai_confidence < 0.90 → LOW_CONFIDENCE (hiển thị OTP trên bảng điện, quy trình OTP di động)
- BR-002-3: ai_confidence < 0.70 → FAILED (hiển thị OTP trên bảng điện, bắt buộc OTP di động)
- BR-002-4: Mục tiêu tỷ lệ nhận dạng tổng thể trên 95%

**Luồng chính (Main Flow)**:
1. Máy chủ API gửi hình ảnh LPR đến engine AI qua HTTPS
2. Engine AI trích xuất biển số xe và tính toán độ tin cậy
3. Trả về kết quả cho máy chủ API
4. Máy chủ API lưu lpr_plate_number, ai_confidence vào bảng tb_weighing
5. Phân nhánh theo độ tin cậy: cân tự động (CONFIRMED) hoặc quy trình OTP (LOW_CONFIDENCE/FAILED)

**Luồng thay thế (Alternative Flow)**:
- AF-002-1: Phản hồi engine AI bị trễ (trên 3 giây) → Timeout, sử dụng kết quả LPR lần 1 + kết hợp OTP

**Luồng ngoại lệ (Exception Flow)**:
- EF-002-1: Engine AI ngừng hoạt động → Sử dụng kết quả LPR lần 1, bắt buộc chuyển sang OTP di động
- EF-002-2: Hình ảnh hỏng/kém chất lượng → Yêu cầu chụp lại

**Chức năng liên quan**: FUNC-001 (Chụp LPR), FUNC-003 (Tự động khớp điều phối xe), FUNC-004 (Cân bảo mật OTP)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Phản hồi xác minh AI trong vòng 3 giây (bao gồm E2E)
- NFR-002 Bảo mật: Mã hóa HTTPS cho giao tiếp engine AI

---

### FUNC-003: Tự động khớp điều phối xe

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-003 |
| **Tên chức năng** | Tự động khớp điều phối xe |
| **Ánh xạ PRD** | FR-001 |
| **Mô-đun** | Hệ thống nhận dạng biển số xe LPR |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Tự động khớp thông tin điều phối xe trong ngày theo biển số xe đã được AI xác nhận để tiến hành cân.

**Điều kiện tiên quyết (Preconditions)**:
- Xác minh biển số xe bằng AI hoàn tất (trạng thái CONFIRMED)
- Tồn tại thông tin điều phối xe trong ngày cho xe đó

**Điều kiện hậu (Postconditions)**:
- Liên kết điều phối xe - thực tích cân
- Bắt đầu quy trình cân tự động

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| confirmed_plate_number | string | Y | Biển số xe được AI xác nhận |
| scale_id | bigint | Y | Mã định danh trạm cân |
| weighing_date | date | Y | Ngày cân (ngày hiện tại) |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| dispatch_id | bigint | ID điều phối xe khớp |
| vehicle_id | bigint | ID xe |
| item_type | string | Loại hàng hóa |
| dispatch_list | array | Danh sách điều phối xe đa dạng (khi khớp nhiều kết quả) |

**Quy tắc nghiệp vụ**:
- BR-003-1: Tra cứu vehicle_id từ tb_vehicle bằng biển số xe (chỉ mục UNIQUE plate_number)
- BR-003-2: Tra cứu điều phối xe trạng thái REGISTERED/IN_PROGRESS từ tb_dispatch bằng vehicle_id + ngày hiện tại
- BR-003-3: Khớp một điều phối xe → Tiến hành cân tự động ngay lập tức
- BR-003-4: Khớp nhiều điều phối xe → Yêu cầu chọn điều phối xe trên ứng dụng di động (hướng dẫn trên bảng điện)
- BR-003-5: Không có điều phối xe khớp → Chuyển sang chế độ cân thủ công

**Luồng chính (Main Flow)**:
1. Tra cứu tb_vehicle bằng biển số xe đã xác nhận
2. Tra cứu điều phối xe hoạt động trong ngày bằng vehicle_id
3. Một điều phối xe → Bắt đầu cân bằng dispatch_id (INSERT vào tb_weighing)
4. Thông báo bắt đầu cân tự động cho CS trạm cân
5. Mở thanh chắn tự động (cho phép vào trạm cân)

**Luồng thay thế (Alternative Flow)**:
- AF-003-1: Nhiều điều phối xe → Hiển thị "Chọn điều phối xe trên di động" trên bảng điện → Chọn trên ứng dụng di động
- AF-003-2: Xe chưa đăng ký → Hiển thị "Xe chưa đăng ký" trên bảng điện, chế độ cân thủ công

**Luồng ngoại lệ (Exception Flow)**:
- EF-003-1: Tra cứu DB thất bại → Chuyển sang chế độ cân thủ công, ghi log lỗi

**Chức năng liên quan**: FUNC-002 (Xác minh AI), FUNC-007 (Quy trình cân), FUNC-012 (Điều khiển bảng điện)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Tra cứu khớp điều phối xe trong vòng 200ms (sử dụng chỉ mục)
- NFR-003 Khả năng mở rộng: Giảm thiểu thay đổi khi bổ sung loại hàng hóa

---

### FUNC-004: Cân bảo mật OTP

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-004 |
| **Tên chức năng** | Cân bảo mật OTP |
| **Ánh xạ PRD** | FR-002 |
| **Mô-đun** | Hệ thống nhận dạng biển số xe LPR |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Khi nhận dạng biển số xe bị sai, hiển thị OTP trên bảng điện, tài xế nhập OTP trên ứng dụng di động để xác thực danh tính rồi tiến hành cân.

**Điều kiện tiên quyết (Preconditions)**:
- Kết quả xác minh AI là LOW_CONFIDENCE hoặc FAILED
- Bảng điện hoạt động bình thường
- Tài xế đã cài đặt và đăng nhập ứng dụng di động

**Điều kiện hậu (Postconditions)**:
- Xác minh OTP hoàn tất, xác nhận xe
- Tiến hành quy trình cân di động

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| otp_code | varchar(6) | Y | 6 chữ số |
| phone_number | varchar(20) | Y | Định dạng số điện thoại đã đăng ký |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| verified | boolean | Kết quả xác minh thành công |
| vehicle_id | bigint | ID xe đã xác nhận |
| plate_number | string | Biển số xe đã xác nhận |
| dispatch_id | bigint | ID điều phối xe khớp |

**Quy tắc nghiệp vụ**:
- BR-004-1: OTP gồm 6 chữ số, lưu trên Redis, TTL 5 phút
- BR-004-2: Khi tạo OTP, hiển thị trên bảng điện của trạm cân tương ứng
- BR-004-3: Khớp người dùng → xe dựa trên số điện thoại (tb_user.phone_number → tb_vehicle)
- BR-004-4: Xác minh OTP thất bại 3 lần → Vô hiệu OTP đó, cấp OTP mới
- BR-004-5: Sau khi OTP hết hạn → Tự động cấp OTP mới, cập nhật bảng điện
- BR-004-6: Ngăn chặn cân sai: Không thể cân trùng lặp với cùng một OTP

**Luồng chính (Main Flow)**:
1. Kết quả xác minh AI trả về LOW_CONFIDENCE/FAILED
2. Máy chủ API tạo OTP 6 chữ số và lưu trên Redis (TTL 5 phút)
3. Chương trình CS hiển thị mã OTP trên bảng điện
4. Tài xế nhập mã OTP trên ứng dụng di động
5. Ứng dụng di động gọi POST /api/v1/otp/verify
6. Máy chủ tra cứu và xác minh OTP từ Redis
7. Khớp người dùng/xe bằng số điện thoại
8. Xác minh thành công → Khớp điều phối xe → Bắt đầu cân di động
9. Xóa khóa OTP trên Redis (dùng một lần)

**Luồng thay thế (Alternative Flow)**:
- AF-004-1: OTP hết hạn → "OTP đã hết hạn. OTP mới sẽ được cấp" → Cấp lại
- AF-004-2: Số điện thoại chưa đăng ký → Thông báo "Số điện thoại chưa đăng ký. Vui lòng liên hệ quản trị viên"

**Luồng ngoại lệ (Exception Flow)**:
- EF-004-1: Sự cố Redis → Fallback tra cứu OTP trực tiếp từ DB
- EF-004-2: Bảng điện hỏng → Gửi OTP qua Push trên ứng dụng di động
- EF-004-3: Xác minh thất bại 3 lần → Hướng dẫn chuyển sang cân thủ công

**Yêu cầu UI/UX**:
- Bảng điện: Hiển thị mã OTP cỡ lớn (thông số kỹ thuật đặc biệt 3 hàng 6 cột)
- Ứng dụng di động: Màn hình nhập OTP, bàn phím số, hiển thị bộ đếm thời gian còn lại

**Chức năng liên quan**: FUNC-002 (Xác minh AI), FUNC-012 (Điều khiển bảng điện), FUNC-020 (Màn hình OTP di động)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-002 Bảo mật: OTP TTL 5 phút, khóa sau 3 lần thất bại, quản lý trên Redis
- NFR-001 Hiệu năng: Phản hồi API xác minh OTP trong vòng 2 giây

---

## 3. Mô-đun 2: Hệ thống quản lý cân thông minh trên Web

### FUNC-005: Đăng ký/Quản lý điều phối xe

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-005 |
| **Tên chức năng** | Đăng ký/Quản lý điều phối xe |
| **Ánh xạ PRD** | FR-004 |
| **Mô-đun** | Hệ thống quản lý cân thông minh trên Web |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Nhân viên phụ trách cân đăng ký và quản lý điều phối xe theo từng loại hàng hóa như phụ phẩm, phế liệu, nguyên vật liệu phụ, xuất kho, hàng thông thường trên hệ thống web.

**Điều kiện tiên quyết (Preconditions)**:
- Đăng nhập hoàn tất (quyền ADMIN hoặc MANAGER)
- Đã đăng ký thông tin cơ sở về công ty vận tải và xe

**Điều kiện hậu (Postconditions)**:
- Tạo/sửa bản ghi tb_dispatch
- Ghi lại lịch sử thay đổi trạng thái điều phối xe

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| vehicle_id | bigint | Y | ID xe đã đăng ký |
| company_id | bigint | Y | ID công ty vận tải đã đăng ký |
| item_type | varchar(20) | Y | Chọn 1 trong: Phụ phẩm/Phế liệu/Nguyên vật liệu phụ/Xuất kho/Hàng thông thường |
| item_name | varchar(100) | Y | Tên hàng hóa (tối đa 100 ký tự) |
| dispatch_date | date | Y | Ngày tương lai hoặc ngày hiện tại |
| origin_location | varchar(100) | N | Nơi xuất phát |
| destination | varchar(100) | N | Nơi đến |
| remarks | text | N | Ghi chú |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| dispatch_id | bigint | ID điều phối xe đã tạo |
| dispatch_status | string | REGISTERED |
| created_at | timestamptz | Thời điểm tạo |

**Quy tắc nghiệp vụ**:
- BR-005-1: Chỉ quyền ADMIN, MANAGER mới được đăng ký điều phối xe
- BR-005-2: Luồng trạng thái điều phối xe: REGISTERED → IN_PROGRESS → COMPLETED / CANCELLED
- BR-005-3: Không thể sửa đổi điều phối xe ở trạng thái COMPLETED
- BR-005-4: Cho phép nhiều điều phối xe cùng ngày cho cùng một xe (kịch bản điều phối xe đa dạng)
- BR-005-5: Chỉ ADMIN mới xóa được điều phối xe, chỉ ở trạng thái REGISTERED

**Luồng chính (Main Flow)**:
1. Nhân viên phụ trách vào màn hình quản lý điều phối xe
2. Nhấn nút "Đăng ký điều phối xe"
3. Nhập thông tin điều phối xe (công ty vận tải, xe, hàng hóa, ngày tháng, v.v.)
4. Nhấn "Lưu" → Gọi POST /api/v1/dispatches
5. Xác minh máy chủ → INSERT vào tb_dispatch
6. Hiển thị mục mới trong danh sách điều phối xe

**Luồng thay thế (Alternative Flow)**:
- AF-005-1: Sao chép đăng ký từ điều phối xe hiện có → Tự động điền thông tin điều phối xe trước đó, chỉ thay đổi ngày tháng
- AF-005-2: Đăng ký điều phối xe hàng loạt → Phương thức tải lên Excel

**Luồng ngoại lệ (Exception Flow)**:
- EF-005-1: Thiếu giá trị bắt buộc → Hiển thị thông báo lỗi hợp lệ
- EF-005-2: Biển số xe chưa đăng ký → Thông báo "Vui lòng đăng ký xe trước"

**Yêu cầu UI/UX**:
- Bảng dữ liệu Ant Design: Bộ lọc, sắp xếp, tổng phụ, cố định ô
- Thiết kế responsive: Tương thích đa thiết bị/trình duyệt
- Màn hình độ phân giải cao, kích thước linh hoạt

**Chức năng liên quan**: FUNC-008 (Quản lý thông tin cơ sở), FUNC-003 (Tự động khớp điều phối xe)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Tải danh sách điều phối xe trong vòng 3 giây (tải trang web)
- NFR-005 Tính dùng được: Các tính năng tiện ích bộ lọc/sắp xếp/tổng phụ/cố định ô

---

### FUNC-006: Quản lý hiện trạng cân

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-006 |
| **Tên chức năng** | Quản lý hiện trạng cân |
| **Ánh xạ PRD** | FR-004 |
| **Mô-đun** | Hệ thống quản lý cân thông minh trên Web |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Giám sát hiện trạng cân theo thời gian thực trên hệ thống web và tra cứu/quản lý thực tích cân.

**Điều kiện tiên quyết (Preconditions)**:
- Đăng nhập hoàn tất
- Tồn tại dữ liệu thực tích cân

**Điều kiện hậu (Postconditions)**:
- Cập nhật màn hình hiện trạng cân

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| date_from | date | N | Ngày bắt đầu tra cứu |
| date_to | date | N | Ngày kết thúc tra cứu |
| item_type | string | N | Bộ lọc loại hàng hóa |
| weighing_mode | string | N | Bộ lọc phương thức cân (LPR_AUTO/MOBILE_OTP/MANUAL) |
| status | string | N | Bộ lọc trạng thái cân |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| weighing_list | array | Danh sách thực tích cân |
| statistics | object | Thông tin thống kê (số lượng, tổng trọng lượng, v.v.) |
| realtime_status | object | Trạng thái trạm cân theo thời gian thực (WebSocket) |

**Quy tắc nghiệp vụ**:
- BR-006-1: Hiện trạng cân thời gian thực được cập nhật trong vòng 500ms qua WebSocket (STOMP)
- BR-006-2: Danh sách cân được phân trang (mặc định 20 bản ghi/trang)
- BR-006-3: Tra cứu thống kê: Cung cấp tổng hợp theo ngày/tháng/loại hàng hóa
- BR-006-4: Khi xem chi tiết cân, hiển thị hình ảnh chụp LPR

**Luồng chính (Main Flow)**:
1. Nhân viên phụ trách vào màn hình quản lý cân
2. Thiết lập kết nối WebSocket (WSS)
3. Hiển thị bảng điều khiển hiện trạng cân thời gian thực (trạng thái trạm cân, lượt cân đang tiến hành)
4. Thiết lập điều kiện để tra cứu danh sách thực tích cân (REST API)
5. Nhấn vào lượt cân cụ thể → Hiển thị thông tin chi tiết + hình ảnh LPR

**Luồng thay thế (Alternative Flow)**:
- AF-006-1: Kết nối WebSocket thất bại → Polling REST (mỗi 5 giây)
- AF-006-2: Thống kê/Bảng điều khiển → Hiển thị biểu đồ ECharts (xu hướng theo ngày, tỷ lệ theo hàng hóa)

**Luồng ngoại lệ (Exception Flow)**:
- EF-006-1: Tra cứu dữ liệu lớn → Hướng dẫn xuất Excel

**Yêu cầu UI/UX**:
- Bảng điều khiển dựa trên ECharts (xu hướng cân theo ngày, tỷ lệ theo hàng hóa, thống kê theo phương thức cân)
- Hiển thị trạng thái trạm cân thời gian thực (Chờ/Đang cân/Hoàn thành/Lỗi)
- Bảng dữ liệu: Bộ lọc, sắp xếp, phân trang, xuất Excel

**Chức năng liên quan**: FUNC-005 (Quản lý điều phối xe), FUNC-007 (Quy trình cân)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Truyền dữ liệu WebSocket trong vòng 500ms, API p95 500ms
- NFR-005 Tính dùng được: Đa dạng biểu đồ, bảng điều khiển

---

### FUNC-007: Xử lý thực tích cân

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-007 |
| **Tên chức năng** | Xử lý thực tích cân |
| **Ánh xạ PRD** | FR-004, FR-005 |
| **Mô-đun** | Hệ thống quản lý cân thông minh trên Web / Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Xử lý quy trình bắt đầu/hoàn thành/cân lại và lưu thực tích. Quản lý cân lần 1/lần 2/lần 3 cùng trọng lượng xe không tải/có tải.

**Điều kiện tiên quyết (Preconditions)**:
- Khớp điều phối xe hoàn tất (tự động hoặc thủ công)
- Nhận giá trị trọng lượng ổn định từ indicator

**Điều kiện hậu (Postconditions)**:
- Tạo/cập nhật bản ghi tb_weighing
- Tạo phiếu cân điện tử (khi cân hoàn thành)
- Gửi thông báo Push

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| dispatch_id | bigint | Y | ID điều phối xe hợp lệ |
| scale_id | bigint | Y | ID trạm cân hợp lệ |
| weighing_mode | varchar(20) | Y | LPR_AUTO / MOBILE_OTP / MANUAL / RE_WEIGH |
| weighing_type | varchar(20) | Y | FIRST / SECOND / THIRD |
| weight_value | decimal(10,2) | Y | Giá trị trọng lượng (kg), > 0 |
| lpr_plate_number | varchar(20) | N | Biển số xe nhận dạng bởi LPR |
| ai_confidence | decimal(5,4) | N | Độ tin cậy AI (0.0~1.0) |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| weighing_id | bigint | ID thực tích cân |
| gross_weight | decimal(10,2) | Tổng trọng lượng (kg) |
| tare_weight | decimal(10,2) | Trọng lượng bì xe (kg) |
| net_weight | decimal(10,2) | Trọng lượng tịnh = gross - tare |
| status | string | IN_PROGRESS / COMPLETED / RE_WEIGHING / ERROR |

**Quy tắc nghiệp vụ**:
- BR-007-1: Luồng weighing_step: GROSS (xe không tải) → TARE (có tải) → COMPLETE
- BR-007-2: net_weight = gross_weight - tare_weight (tính toán tự động)
- BR-007-3: net_weight < 0 → Lỗi (kiểm tra thứ tự xe không tải/có tải)
- BR-007-4: Khi cân lại (RE_WEIGH), đổi status lượt cân hiện tại thành RE_WEIGHING, tạo bản ghi cân mới
- BR-007-5: Khi hoàn thành cân, tự động tạo phiếu cân điện tử (INSERT vào tb_weighing_slip)
- BR-007-6: Khi hoàn thành cân, gửi thông báo Push + tin nhắn thông báo
- BR-007-7: Chỉ lưu giá trị trọng lượng ổn định (phán đoán ổn định của indicator)

**Luồng chính (Main Flow)**:
1. Xe đỗ đúng vị trí trên trạm cân (xác nhận bằng bộ phát hiện xe)
2. Phát hiện giá trị trọng lượng ổn định từ indicator
3. Chương trình CS nhận giá trị trọng lượng ổn định
4. Chương trình CS gọi POST /api/v1/weighings (bắt đầu cân)
5. Hoàn thành cân lần 1 (GROSS) → Lưu giá trị trọng lượng
6. Xe di chuyển, tiến hành cân lần 2 (TARE)
7. Tự động tính toán trọng lượng tịnh (net_weight)
8. Hoàn thành cân (COMPLETE) → PUT /api/v1/weighings/{id}/complete
9. Tự động tạo phiếu cân điện tử
10. Gửi thông báo Push

**Luồng thay thế (Alternative Flow)**:
- AF-007-1: Cần cân lần 3 → Cân bổ sung với loại THIRD
- AF-007-2: Cân lại → PUT /api/v1/weighings/{id}/re-weigh → Xử lý lượt cũ thành RE_WEIGHING, tạo lượt mới
- AF-007-3: Chế độ cân thủ công → Nhân viên phụ trách nhập trực tiếp giá trị trọng lượng trên màn hình cảm ứng

**Luồng ngoại lệ (Exception Flow)**:
- EF-007-1: Mất kết nối indicator → Cảnh báo trên màn hình CS, chuyển sang nhập thủ công
- EF-007-2: Thất bại giao tiếp máy chủ API → Lưu cache cục bộ, tự động đồng bộ khi phục hồi
- EF-007-3: net_weight < 0 → Cảnh báo "Vui lòng kiểm tra thứ tự xe không tải/có tải"

**Chức năng liên quan**: FUNC-003 (Khớp điều phối xe), FUNC-009 (Phiếu cân điện tử), FUNC-010 (Tích hợp indicator)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Đồng bộ dữ liệu cân thời gian thực trong vòng 5 giây
- NFR-004 Tính sẵn sàng: Lưu cache cục bộ rồi đồng bộ khi mạng gặp sự cố

---

### FUNC-008: Quản lý thông tin cơ sở

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-008 |
| **Tên chức năng** | Quản lý thông tin cơ sở |
| **Ánh xạ PRD** | FR-004 |
| **Mô-đun** | Hệ thống quản lý cân thông minh trên Web |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Quản lý thông tin cơ sở hệ thống bao gồm công ty vận tải, xe, trạm cân, mã chung.

**Điều kiện tiên quyết (Preconditions)**:
- Đăng nhập (quyền ADMIN)

**Điều kiện hậu (Postconditions)**:
- Cập nhật bảng thông tin cơ sở
- Cập nhật cache Redis (TTL 5 phút)

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| company_name | varchar(100) | Y | Khi đăng ký công ty vận tải |
| company_type | varchar(20) | Y | Loại công ty vận tải |
| plate_number | varchar(20) | Y | Khi đăng ký xe, UNIQUE |
| vehicle_type | varchar(20) | Y | Loại xe |
| code_group | varchar(50) | Y | Nhóm mã chung |
| code_value | varchar(50) | Y | Giá trị mã |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| record_id | bigint | ID bản ghi đã tạo/sửa |
| operation | string | CREATE / UPDATE / DELETE |

**Quy tắc nghiệp vụ**:
- BR-008-1: Chỉ quyền ADMIN mới được đăng ký/sửa/xóa thông tin cơ sở
- BR-008-2: Biển số xe (plate_number) phải duy nhất trong toàn hệ thống
- BR-008-3: Xóa công ty vận tải chỉ khả dụng khi không có điều phối xe liên quan (hoặc vô hiệu hóa)
- BR-008-4: Vô hiệu hóa cache Redis khi thay đổi thông tin cơ sở

**Luồng chính (Main Flow)**:
1. Quản trị viên vào màn hình quản lý thông tin cơ sở
2. Chọn tab Công ty vận tải/Xe/Trạm cân/Mã chung
3. Tra cứu danh sách (GET /api/v1/master/{type})
4. Thực hiện thao tác đăng ký/sửa/xóa
5. Máy chủ xử lý → Cập nhật cache Redis

**Chức năng liên quan**: FUNC-005 (Đăng ký điều phối xe), FUNC-007 (Xử lý cân)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Cache Redis cho thông tin cơ sở (TTL 5 phút)
- NFR-003 Khả năng mở rộng: Quản lý bổ sung loại hàng hóa bằng mã chung

---

### FUNC-009: Tạo/Tra cứu phiếu cân điện tử

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-009 |
| **Tên chức năng** | Tạo/Tra cứu phiếu cân điện tử |
| **Ánh xạ PRD** | FR-006 |
| **Mô-đun** | Hệ thống quản lý cân thông minh trên Web |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Tự động tạo phiếu cân điện tử khi hoàn thành cân, cung cấp chức năng tra cứu và chia sẻ trên di động/web.

**Điều kiện tiên quyết (Preconditions)**:
- Thực tích cân ở trạng thái COMPLETED

**Điều kiện hậu (Postconditions)**:
- Tạo bản ghi tb_weighing_slip
- Tự động đánh số phiếu cân

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| weighing_id | bigint | Y | ID thực tích cân đã hoàn thành |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| slip_id | bigint | ID phiếu cân |
| slip_number | varchar(30) | Số phiếu cân tự động đánh số (UNIQUE) |
| slip_data | jsonb | Dữ liệu chi tiết phiếu cân (xe, hàng hóa, trọng lượng, thời gian, v.v.) |

**Quy tắc nghiệp vụ**:
- BR-009-1: Quy tắc tự động đánh số phiếu cân: YYYYMMDD-SEQ (ví dụ: 20260527-0001)
- BR-009-2: slip_data bao gồm biển số xe, công ty vận tải, hàng hóa, tổng trọng lượng, trọng lượng bì xe, trọng lượng tịnh, thời gian cân
- BR-009-3: Chia sẻ qua KakaoTalk bằng cách gọi API Kakao Biz
- BR-009-4: Chia sẻ qua SMS bằng cách gọi SMS Gateway
- BR-009-5: Tra cứu lịch sử phiếu cân theo khoảng thời gian

**Luồng chính (Main Flow)**:
1. Phát sinh sự kiện hoàn thành cân
2. Máy chủ tạo bản ghi phiếu cân trong tb_weighing_slip
3. Tự động đánh số slip_number
4. Lưu thông tin chi tiết cân dạng JSON vào slip_data
5. Thông báo Push trên ứng dụng di động (thông báo tạo phiếu cân)
6. Người dùng tra cứu phiếu cân trên di động/web

**Luồng thay thế (Alternative Flow)**:
- AF-009-1: Chia sẻ KakaoTalk → POST /api/v1/slips/{id}/share {type: "KAKAO"}
- AF-009-2: Chia sẻ SMS → POST /api/v1/slips/{id}/share {type: "SMS"}

**Luồng ngoại lệ (Exception Flow)**:
- EF-009-1: Sự cố API Kakao → Gửi thay thế qua SMS
- EF-009-2: Gửi SMS thất bại → Đăng ký hàng đợi thử lại (Circuit Breaker)

**Chức năng liên quan**: FUNC-007 (Thực tích cân), FUNC-019 (Màn hình phiếu cân điện tử di động)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-002 Bảo mật: Đảm bảo tính toàn vẹn dữ liệu phiếu cân
- NFR-004 Tính sẵn sàng: Sử dụng kênh thay thế khi dịch vụ bên ngoài gặp sự cố

---

### FUNC-030: Quản lý xuất cổng

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-030 |
| **Tên chức năng** | Quản lý xuất cổng |
| **Ánh xạ PRD** | FR-004 |
| **Mô-đun** | Hệ thống quản lý cân thông minh trên Web |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Quản lý xử lý xuất cổng cho xe đã hoàn thành cân.

**Điều kiện tiên quyết (Preconditions)**:
- Thực tích cân ở trạng thái COMPLETED
- Quyền MANAGER trở lên

**Điều kiện hậu (Postconditions)**:
- Tạo/cập nhật bản ghi tb_gate_pass

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| weighing_id | bigint | Y | Thực tích cân đã hoàn thành |
| dispatch_id | bigint | Y | ID điều phối xe |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| gate_pass_id | bigint | ID xuất cổng |
| pass_status | string | PENDING / PASSED / REJECTED |
| passed_at | timestamptz | Thời điểm xuất cổng |

**Quy tắc nghiệp vụ**:
- BR-030-1: Chỉ xử lý xuất cổng cho lượt cân đã hoàn thành
- BR-030-2: Khi xử lý xuất cổng, đổi pass_status thành PASSED
- BR-030-3: Có thể tra cứu lịch sử xuất cổng

**Luồng chính (Main Flow)**:
1. Nhân viên phụ trách xác nhận danh sách chờ xuất cổng trên màn hình quản lý xuất cổng
2. Chọn lượt cân đã hoàn thành → Nhấn "Xử lý xuất cổng"
3. Gọi POST /api/v1/gate-passes
4. Đổi trạng thái xuất cổng thành PASSED, ghi lại passed_at

**Chức năng liên quan**: FUNC-007 (Thực tích cân), FUNC-005 (Quản lý điều phối xe)

---

## 4. Mô-đun 3: Chương trình CS cân

### FUNC-010: Nhận giá trị trọng lượng từ Indicator

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-010 |
| **Tên chức năng** | Nhận giá trị trọng lượng từ Indicator |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Nhận giá trị trọng lượng theo thời gian thực từ indicator qua giao tiếp nối tiếp RS-232C và thực hiện phán đoán ổn định.

**Điều kiện tiên quyết (Preconditions)**:
- Kết nối RS-232C indicator hoàn tất
- Cấu hình cổng COM hoàn tất

**Điều kiện hậu (Postconditions)**:
- Xác định giá trị trọng lượng ổn định
- Truyền giá trị trọng lượng cho quy trình cân

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| serial_data | byte[] | Y | Dữ liệu nhận từ RS-232C |
| com_port | string | Y | COM1~COM9 |
| baud_rate | int | Y | 9600/19200/38400 |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| weight_value | decimal(10,2) | Giá trị trọng lượng hiện tại (kg) |
| is_stable | boolean | Trạng thái ổn định |
| stable_weight | decimal(10,2) | Giá trị trọng lượng ổn định đã xác định |

**Quy tắc nghiệp vụ**:
- BR-010-1: Phán đoán ổn định: Trọng lượng đồng nhất N lần liên tiếp (giá trị cấu hình) → Ổn định
- BR-010-2: Phạm vi cho phép biến động trọng lượng: +/- giá trị cấu hình (kg)
- BR-010-3: Timeout giao tiếp: Cảnh báo khi không nhận dữ liệu trên 5 giây
- BR-010-4: Trọng lượng âm → Xử lý lỗi

**Luồng chính (Main Flow)**:
1. Chương trình CS mở cổng COM bằng System.IO.Ports
2. Nhận dữ liệu trọng lượng định kỳ từ indicator (100ms~1 giây)
3. Phân tích dữ liệu → Trích xuất giá trị trọng lượng
4. Thực hiện logic phán đoán ổn định
5. Xác định ổn định → Truyền stable_weight cho quy trình cân
6. Hiển thị giá trị trọng lượng hiện tại theo thời gian thực trên màn hình chính CS

**Luồng ngoại lệ (Exception Flow)**:
- EF-010-1: Kết nối cổng COM thất bại → Thử kết nối lại (3 lần), nếu thất bại thì thông báo quản trị viên
- EF-010-2: Lỗi phân tích dữ liệu → Hủy gói tin đó, ghi log
- EF-010-3: Không nhận dữ liệu trên 5 giây → Hiển thị cảnh báo "Lỗi giao tiếp indicator"

**Chức năng liên quan**: FUNC-007 (Thực tích cân), FUNC-011 (Quy trình cân tự động LPR)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Nhận và hiển thị giá trị trọng lượng theo thời gian thực
- NFR-004 Tính sẵn sàng: Logic thử lại giao tiếp, quản lý timeout

---

### FUNC-011: Quy trình cân tự động LPR

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-011 |
| **Tên chức năng** | Quy trình cân tự động LPR |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Điều khiển toàn bộ quy trình tự động từ chương trình CS: Phát hiện LiDAR → Chụp LPR → Xác minh AI → Khớp điều phối xe → Cân tự động.

**Điều kiện tiên quyết (Preconditions)**:
- Tất cả thiết bị H/W kết nối bình thường (LPR, cảm biến, indicator, bảng điện, thanh chắn)
- Trạng thái kết nối máy chủ API

**Điều kiện hậu (Postconditions)**:
- Lưu thực tích cân
- Tạo phiếu cân điện tử
- Mở thanh chắn

**Quy tắc nghiệp vụ**:
- BR-011-1: Cân tự động chỉ tiến hành khi AI confidence >= 0.90 VÀ khớp một điều phối xe duy nhất
- BR-011-2: Thời gian mục tiêu toàn bộ quy trình (phát hiện cảm biến → hoàn thành cân): trong vòng 30 giây sau khi xe dừng
- BR-011-3: Mục tiêu tỷ lệ cân tự động không người: trên 90%

**Luồng chính (Main Flow)**:
1. Cảm biến LiDAR/radar → Phát hiện xe tiến vào
2. Camera LPR → Chụp biển số xe
3. Máy chủ API → Yêu cầu xác minh AI
4. Nhận kết quả AI (confidence >= 0.90)
5. Tự động khớp điều phối xe (một điều phối xe)
6. Bộ phát hiện xe → Xác nhận đúng vị trí
7. Indicator → Nhận giá trị trọng lượng ổn định
8. Lưu thực tích cân (POST /api/v1/weighings)
9. Bảng điện → Hiển thị "Hoàn thành cân"
10. Thanh chắn → Mở
11. Tự động tạo phiếu cân điện tử

**Luồng thay thế (Alternative Flow)**:
- AF-011-1: AI confidence < 0.90 → Chuyển sang quy trình OTP (FUNC-004)
- AF-011-2: Nhiều điều phối xe → Hiển thị "Chọn điều phối xe trên di động" trên bảng điện
- AF-011-3: Xe chưa đăng ký → Chuyển sang chế độ cân thủ công

**Luồng ngoại lệ (Exception Flow)**:
- EF-011-1: Sự cố thiết bị H/W → Chuyển sang chế độ cân thủ công (màn hình cảm ứng)
- EF-011-2: Thất bại giao tiếp máy chủ API → Chế độ cache cục bộ
- EF-011-3: Indicator không ổn định (trên 60 giây) → Hiển thị "Đang chờ ổn định", cảnh báo timeout

**Chức năng liên quan**: FUNC-001~004, FUNC-007, FUNC-010, FUNC-012~013

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: E2E trong vòng 3 giây (LPR → Xác minh AI)
- NFR-004 Tính sẵn sàng: Có thể chuyển sang chế độ thủ công khi sự cố H/W

---

### FUNC-012: Điều khiển bảng điện

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-012 |
| **Tên chức năng** | Điều khiển bảng điện |
| **Ánh xạ PRD** | FR-002, FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Hiển thị mã OTP, thông báo hướng dẫn, trạng thái cân trên bảng điện tại trạm cân.

**Điều kiện tiên quyết (Preconditions)**:
- Kết nối bảng điện TCP/RS-485 bình thường

**Điều kiện hậu (Postconditions)**:
- Hiển thị thông báo trên bảng điện

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| display_type | string | Y | OTP / STATUS / MESSAGE / ERROR |
| content | string | Y | Nội dung hiển thị |
| scale_id | bigint | Y | Mã định danh trạm cân |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| display_result | boolean | Kết quả hiển thị thành công |

**Quy tắc nghiệp vụ**:
- BR-012-1: Thông số bảng điện: Bảng điện lớn thông số đặc biệt 3 hàng 6 cột
- BR-012-2: Khi hiển thị OTP, hiển thị 6 chữ số bằng font chữ lớn
- BR-012-3: Thông báo trạng thái: "Chờ cân", "Đang cân", "Hoàn thành cân", "Cần xác thực di động"
- BR-012-4: Thông báo lỗi: "Xe chưa đăng ký", "Đang bảo trì hệ thống"

**Luồng chính (Main Flow)**:
1. Chương trình CS phát hiện thay đổi trạng thái cân
2. Tạo lệnh hiển thị bảng điện theo display_type
3. Gửi lệnh đến bảng điện qua TCP/RS-485
4. Hiển thị thông báo trên bảng điện

**Chức năng liên quan**: FUNC-004 (Cân bảo mật OTP), FUNC-011 (Cân tự động)

---

### FUNC-013: Điều khiển thanh chắn tự động

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-013 |
| **Tên chức năng** | Điều khiển thanh chắn tự động |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Điều khiển đóng/mở thanh chắn tự động theo quy trình cân.

**Điều kiện tiên quyết (Preconditions)**:
- Thanh chắn kết nối bình thường (TCP/RS-485)
- Công tắc thủ công kết nối

**Điều kiện hậu (Postconditions)**:
- Thay đổi trạng thái thanh chắn (mở/đóng)

**Quy tắc nghiệp vụ**:
- BR-013-1: Tự động mở khi hoàn thành cân
- BR-013-2: Có thể mở khẩn cấp bằng công tắc thủ công
- BR-013-3: Thông số thanh chắn: Thanh vuông LED 3M
- BR-013-4: An toàn là ưu tiên: Xác nhận xe đúng vị trí trước khi thanh chắn hoạt động

**Luồng chính (Main Flow)**:
1. Nhận tín hiệu hoàn thành cân
2. Xác nhận vị trí xe bằng bộ phát hiện xe
3. Gửi lệnh mở thanh chắn
4. Xác nhận xe đi qua
5. Gửi lệnh đóng thanh chắn

**Luồng ngoại lệ (Exception Flow)**:
- EF-013-1: Thanh chắn hoạt động thất bại → Hướng dẫn sử dụng công tắc thủ công
- EF-013-2: Lỗi phát hiện xe → Giữ thanh chắn, gọi quản trị viên

**Chức năng liên quan**: FUNC-011 (Cân tự động), FUNC-014 (Cân thủ công)

---

### FUNC-014: Chế độ cân thủ công

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-014 |
| **Tên chức năng** | Chế độ cân thủ công |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Khi không thể sử dụng cân LPR/di động, thực hiện cân thủ công theo phương thức màn hình cảm ứng hiện có.

**Điều kiện tiên quyết (Preconditions)**:
- Chương trình CS trạm cân đang chạy
- Indicator kết nối bình thường

**Điều kiện hậu (Postconditions)**:
- Lưu thực tích cân (weighing_mode = MANUAL)

**Quy tắc nghiệp vụ**:
- BR-014-1: Nhân viên phụ trách trực tiếp chọn/nhập biển số xe và thông tin điều phối xe trên màn hình cảm ứng
- BR-014-2: Vận hành song song phương thức RFID hiện có (trong giai đoạn chuyển đổi)
- BR-014-3: Cân thủ công cũng tạo phiếu cân điện tử tương tự

**Luồng chính (Main Flow)**:
1. Nhân viên phụ trách chọn chế độ "Cân thủ công" trên chương trình CS
2. Nhập biển số xe hoặc chọn từ danh sách
3. Chọn thông tin điều phối xe
4. Xác nhận giá trị trọng lượng ổn định từ indicator
5. Nhấn nút "Xác nhận cân"
6. Lưu thực tích cân (weighing_mode = MANUAL)

**Chức năng liên quan**: FUNC-010 (Indicator), FUNC-007 (Thực tích cân)

---

### FUNC-015: Khởi tạo lại/Cân lại

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-015 |
| **Tên chức năng** | Khởi tạo lại/Cân lại |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Thực hiện khởi tạo lại dữ liệu và cân lại khi xảy ra lỗi cân.

**Quy tắc nghiệp vụ**:
- BR-015-1: Khi cân lại, đổi status lượt cân hiện tại thành RE_WEIGHING
- BR-015-2: Tạo bản ghi cân mới với chế độ RE_WEIGH
- BR-015-3: Bắt buộc ghi lý do cân lại
- BR-015-4: Có thể truy vết lịch sử cân lại (tb_weighing_log)

**Luồng chính (Main Flow)**:
1. Nhân viên phụ trách nhấn nút "Cân lại"
2. Nhập lý do cân lại
3. Đổi trạng thái lượt cân hiện tại thành RE_WEIGHING
4. Bắt đầu quy trình cân mới
5. Hoàn thành cân lại → Lưu thực tích
6. Ghi log lịch sử cân lại

**Chức năng liên quan**: FUNC-007 (Thực tích cân), FUNC-010 (Indicator)

---

### FUNC-016: Cache cục bộ/Chế độ ngoại tuyến

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-016 |
| **Tên chức năng** | Cache cục bộ/Chế độ ngoại tuyến |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Chương trình CS cân |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Khi mất kết nối mạng với máy chủ API, lưu cache dữ liệu cân tại cục bộ và tự động đồng bộ khi phục hồi.

**Quy tắc nghiệp vụ**:
- BR-016-1: Phát hiện mất kết nối mạng → Tự động chuyển sang chế độ ngoại tuyến
- BR-016-2: Dữ liệu cân khi ngoại tuyến được lưu vào SQLite/file cục bộ
- BR-016-3: Tự động đồng bộ khi phục hồi mạng (thứ tự FIFO)
- BR-016-4: Các mục đồng bộ thất bại được giữ trong hàng đợi thử lại
- BR-016-5: Hiển thị rõ ràng trạng thái chế độ ngoại tuyến trên màn hình CS

**Chức năng liên quan**: FUNC-011 (Cân tự động), FUNC-014 (Cân thủ công)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-004 Tính sẵn sàng: Cache cục bộ khi sự cố mạng, đồng bộ khi phục hồi (RTO trong vòng 1 giờ)

---

## 5. Mô-đun 4: Ứng dụng di động quản lý cân

### FUNC-017: Đăng nhập di động

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-017 |
| **Tên chức năng** | Đăng nhập di động |
| **Ánh xạ PRD** | FR-003 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Cung cấp chức năng đăng nhập phân biệt nhân viên phụ trách/tài xế và đăng nhập an toàn (mã xác thực).

**Điều kiện tiên quyết (Preconditions)**:
- Cài đặt ứng dụng hoàn tất (iOS/Android)
- Đăng ký tài khoản người dùng hoàn tất

**Điều kiện hậu (Postconditions)**:
- Cấp JWT Access Token + Refresh Token
- Đăng ký FCM token trên máy chủ

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| login_id | varchar(50) | Y | ID đăng nhập đã đăng ký |
| password | varchar(255) | Y | Mật khẩu |
| device_type | string | Y | Cố định MOBILE |

**Quy tắc nghiệp vụ**:
- BR-017-1: Đăng nhập phân biệt vai trò nhân viên phụ trách (MANAGER) / tài xế (DRIVER)
- BR-017-2: Đăng nhập an toàn: Gửi mã xác thực qua số điện thoại → Nhập mã xác thực
- BR-017-3: Access Token 30 phút, Refresh Token 7 ngày
- BR-017-4: Tự động đăng ký FCM token khi đăng nhập thành công
- BR-017-5: Khóa tài khoản sau 5 lần nhập sai mật khẩu

**Luồng chính (Main Flow)**:
1. Chạy ứng dụng → Màn hình đăng nhập
2. Nhập ID đăng nhập/mật khẩu
3. Gọi POST /api/v1/auth/login
4. Nhận JWT token → Lưu cục bộ bằng Hive
5. Đăng ký FCM token (POST /api/v1/notifications/push/register)
6. Chuyển đến màn hình chính theo vai trò

**Luồng thay thế (Alternative Flow)**:
- AF-017-1: Đăng nhập an toàn → POST /api/v1/auth/login/otp (số điện thoại + mã xác thực)
- AF-017-2: Đăng nhập tự động → Làm mới Access Token bằng Refresh Token

**Chức năng liên quan**: FUNC-018~021 (Các chức năng di động)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-002 Bảo mật: Xác thực JWT, mật khẩu bcrypt, đăng nhập an toàn dựa trên OTP
- NFR-005 Tính dùng được: Đa nền tảng Flutter iOS/Android

---

### FUNC-018: Tra cứu/Chọn điều phối xe

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-018 |
| **Tên chức năng** | Tra cứu/Chọn điều phối xe |
| **Ánh xạ PRD** | FR-003 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Tài xế tra cứu hiện trạng chỉ thị điều phối xe và chọn điều phối xe cần cân khi có nhiều điều phối xe.

**Quy tắc nghiệp vụ**:
- BR-018-1: Đăng nhập tài xế → Chỉ hiển thị điều phối xe trong ngày của xe bản thân
- BR-018-2: Đăng nhập nhân viên phụ trách → Tra cứu toàn bộ điều phối xe
- BR-018-3: Khi có nhiều điều phối xe, chọn từ danh sách để tiến hành cân
- BR-018-4: Phân biệt màu theo trạng thái điều phối xe (Đăng ký/Đang xử lý/Hoàn thành)

**Luồng chính (Main Flow)**:
1. Màn hình chính → Tab "Tra cứu điều phối xe"
2. Gọi GET /api/v1/dispatches/my (tài xế) hoặc GET /api/v1/dispatches (nhân viên phụ trách)
3. Hiển thị danh sách điều phối xe
4. Chọn điều phối xe → Chuyển đến màn hình tiến hành cân

**Chức năng liên quan**: FUNC-019 (Cân di động), FUNC-003 (Khớp điều phối xe)

---

### FUNC-019: Tiến hành cân trên di động

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-019 |
| **Tên chức năng** | Tiến hành cân trên di động |
| **Ánh xạ PRD** | FR-003 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Xác nhận trạng thái tiến hành cân theo thời gian thực trên ứng dụng di động và điều khiển cân từ di động khi cân dựa trên OTP.

**Quy tắc nghiệp vụ**:
- BR-019-1: Hiển thị trạng thái tiến hành cân theo thời gian thực (Chờ/Đang cân/Hoàn thành)
- BR-019-2: Khi cân OTP, có thể kích hoạt "Bắt đầu cân" từ di động
- BR-019-3: Sau khi hoàn thành cân, tự động chuyển đến màn hình phiếu cân điện tử

**Chức năng liên quan**: FUNC-004 (Cân OTP), FUNC-007 (Thực tích cân), FUNC-009 (Phiếu cân điện tử)

---

### FUNC-020: Nhập OTP trên di động

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-020 |
| **Tên chức năng** | Nhập OTP trên di động |
| **Ánh xạ PRD** | FR-002, FR-003 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Nhập mã OTP hiển thị trên bảng điện vào ứng dụng di động để xác thực danh tính.

**Quy tắc nghiệp vụ**:
- BR-020-1: Giao diện bàn phím số 6 chữ số
- BR-020-2: Hiển thị bộ đếm thời gian OTP còn lại (5 phút)
- BR-020-3: Xác minh thành công → Chọn điều phối xe → Tiến hành cân
- BR-020-4: Xác minh thất bại → Thông báo lỗi + Hướng dẫn nhập lại

**Luồng chính (Main Flow)**:
1. Vào màn hình nhập OTP (tự động hoặc thủ công)
2. Nhập mã OTP 6 chữ số từ bảng điện
3. Gọi POST /api/v1/otp/verify
4. Xác minh thành công → Khớp điều phối xe → Màn hình tiến hành cân

**Chức năng liên quan**: FUNC-004 (Cân bảo mật OTP), FUNC-018 (Chọn điều phối xe)

---

### FUNC-021: Tra cứu/Chia sẻ phiếu cân điện tử

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-021 |
| **Tên chức năng** | Tra cứu/Chia sẻ phiếu cân điện tử |
| **Ánh xạ PRD** | FR-003, FR-006 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Tra cứu phiếu cân điện tử trên ứng dụng di động và chia sẻ qua KakaoTalk/SMS.

**Quy tắc nghiệp vụ**:
- BR-021-1: Tự động hiển thị màn hình phiếu cân sau khi hoàn thành cân
- BR-021-2: Chia sẻ KakaoTalk: Liên kết phiếu cân + thông tin tóm tắt
- BR-021-3: Chia sẻ SMS: Văn bản tóm tắt phiếu cân
- BR-021-4: Tra cứu lịch sử: Theo tháng/ngày, có thể thiết lập khoảng thời gian

**Luồng chính (Main Flow)**:
1. Hoàn thành cân → Tự động hiển thị màn hình phiếu cân điện tử
2. Hiển thị thông tin chi tiết phiếu cân (xe, hàng hóa, trọng lượng, thời gian)
3. Nút "Chia sẻ" → Chọn KakaoTalk/SMS
4. Gọi POST /api/v1/slips/{id}/share
5. Gửi chia sẻ đến dịch vụ bên ngoài

**Chức năng liên quan**: FUNC-009 (Tạo phiếu cân điện tử)

---

### FUNC-022: Tra cứu thực tích điều phối xe/cân

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-022 |
| **Tên chức năng** | Tra cứu thực tích điều phối xe/cân |
| **Ánh xạ PRD** | FR-003 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Tra cứu thực tích hoàn thành điều phối xe/cân theo tháng/ngày trên ứng dụng di động.

**Quy tắc nghiệp vụ**:
- BR-022-1: Tài xế: Chỉ tra cứu thực tích bản thân
- BR-022-2: Có thể thiết lập khoảng thời gian (ngày bắt đầu~ngày kết thúc)
- BR-022-3: Chuyển đổi tab theo tháng/ngày

**Chức năng liên quan**: FUNC-018 (Tra cứu điều phối xe), FUNC-021 (Tra cứu phiếu cân)

---

### FUNC-023: Thông báo/Cuộc gọi hỏi đáp

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-023 |
| **Tên chức năng** | Thông báo/Cuộc gọi hỏi đáp |
| **Ánh xạ PRD** | FR-003, FR-007 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Xác nhận thông báo trên ứng dụng di động và cung cấp chức năng gọi điện theo loại câu hỏi.

**Quy tắc nghiệp vụ**:
- BR-023-1: Thông báo: Hiển thị thông báo/hướng dẫn do quản trị viên đăng ký
- BR-023-2: Cuộc gọi hỏi đáp: Chọn loại câu hỏi → Tự động kết nối bộ phận phụ trách
- BR-023-3: Loại câu hỏi: Phòng điều phối logistics, kho vật liệu, v.v.

**Luồng chính (Main Flow)**:
1. Tab "Thông báo" → Xác nhận danh sách thông báo
2. Tab "Hỏi đáp" → Chọn loại câu hỏi
3. Hiển thị thông tin liên hệ bộ phận phụ trách theo loại đã chọn
4. Nút "Gọi điện" → Kết nối điện thoại

**Chức năng liên quan**: FUNC-025 (Phân luồng cuộc gọi cố định)

---

### FUNC-024: Nhận thông báo Push

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-024 |
| **Tên chức năng** | Nhận thông báo Push |
| **Ánh xạ PRD** | FR-003 |
| **Mô-đun** | Ứng dụng di động quản lý cân |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Nhận thông báo Push dựa trên FCM để thông báo theo thời gian thực về hoàn thành cân, thay đổi điều phối xe, v.v.

**Quy tắc nghiệp vụ**:
- BR-024-1: Loại thông báo: Hoàn thành cân, đăng ký/thay đổi điều phối xe, thông báo hệ thống
- BR-024-2: Khi nhận thông báo, thêm vào danh sách thông báo trong ứng dụng (tb_notification)
- BR-024-3: Hiển thị huy hiệu thông báo chưa đọc
- BR-024-4: Gửi song song thông báo KakaoTalk

**Luồng chính (Main Flow)**:
1. Máy chủ gửi FCM Push
2. Ứng dụng nhận thông báo
3. Thêm vào danh sách thông báo + Hiển thị huy hiệu
4. Nhấn thông báo → Chuyển đến màn hình chi tiết tương ứng

**Chức năng liên quan**: FUNC-007 (Thực tích cân), FUNC-021 (Phiếu cân điện tử)

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Giảm thiểu độ trễ thông báo Push (tin nhắn độ tin cậy cao FCM)

---

## 6. Mô-đun 5: API di động

### FUNC-025-API: API Người dùng/Xác thực

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-025-API |
| **Tên chức năng** | API Người dùng/Xác thực |
| **Ánh xạ PRD** | FR-008 |
| **Mô-đun** | API di động |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Cung cấp API xác thực người dùng (đăng nhập, OTP, làm mới token) và tra cứu thông tin người dùng.

**Endpoint API**:

| Method | Path | Mô tả |
|--------|------|-------|
| POST | /api/v1/auth/login | Đăng nhập ID/PW |
| POST | /api/v1/auth/login/otp | Đăng nhập dựa trên OTP |
| POST | /api/v1/auth/refresh | Làm mới Token |
| POST | /api/v1/auth/logout | Đăng xuất |

**Quy tắc nghiệp vụ**:
- BR-API-1: JWT Access Token 30 phút, Refresh Token 7 ngày (quản lý phiên Redis)
- BR-API-2: Quyền RBAC: ADMIN > MANAGER > DRIVER
- BR-API-3: Thời gian phản hồi API trong vòng 2 giây

**Ánh xạ yêu cầu phi chức năng**:
- NFR-001 Hiệu năng: Phản hồi API trong vòng 2 giây
- NFR-002 Bảo mật: TLS 1.3, JWT, bcrypt

---

### FUNC-026-API: API Thông tin điều phối xe

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-026-API |
| **Tên chức năng** | API Thông tin điều phối xe |
| **Ánh xạ PRD** | FR-008 |
| **Mô-đun** | API di động |
| **Độ ưu tiên** | HIGH |

**Endpoint API**:

| Method | Path | Mô tả |
|--------|------|-------|
| GET | /api/v1/dispatches | Tra cứu danh sách điều phối xe |
| POST | /api/v1/dispatches | Đăng ký điều phối xe |
| GET | /api/v1/dispatches/{id} | Tra cứu chi tiết điều phối xe |
| PUT | /api/v1/dispatches/{id} | Sửa đổi điều phối xe |
| DELETE | /api/v1/dispatches/{id} | Xóa điều phối xe |
| GET | /api/v1/dispatches/my | Danh sách điều phối xe của tôi |

---

### FUNC-027-API: API Xử lý cân

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-027-API |
| **Tên chức năng** | API Xử lý cân |
| **Ánh xạ PRD** | FR-008 |
| **Mô-đun** | API di động |
| **Độ ưu tiên** | HIGH |

**Endpoint API**:

| Method | Path | Mô tả |
|--------|------|-------|
| POST | /api/v1/weighings | Bắt đầu cân |
| PUT | /api/v1/weighings/{id}/complete | Hoàn thành cân |
| PUT | /api/v1/weighings/{id}/re-weigh | Cân lại |
| GET | /api/v1/weighings | Danh sách thực tích cân |
| GET | /api/v1/weighings/{id} | Chi tiết cân |
| GET | /api/v1/weighings/realtime | Hiện trạng thời gian thực (WebSocket) |
| GET | /api/v1/weighings/statistics | Thống kê |

---

### FUNC-028-API: API Thông báo Push

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-028-API |
| **Tên chức năng** | API Thông báo Push |
| **Ánh xạ PRD** | FR-008 |
| **Mô-đun** | API di động |
| **Độ ưu tiên** | HIGH |

**Endpoint API**:

| Method | Path | Mô tả |
|--------|------|-------|
| GET | /api/v1/notifications | Danh sách thông báo |
| PUT | /api/v1/notifications/{id}/read | Đánh dấu đã đọc thông báo |
| POST | /api/v1/notifications/push/register | Đăng ký FCM token |

---

## 7. Mô-đun 6: Tích hợp hạ tầng H/W

### FUNC-040: Tích hợp camera LPR

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-040 |
| **Tên chức năng** | Tích hợp camera LPR |
| **Ánh xạ PRD** | FR-001 |
| **Mô-đun** | Tích hợp hạ tầng H/W |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Chụp biển số xe và nhận kết quả nhận dạng lần 1 qua giao tiếp TCP/UDP với camera LPR.

**Thông số giao tiếp**:
- Giao thức: TCP/UDP
- Đối tượng kết nối: Chương trình CS trạm cân
- Dữ liệu: Hình ảnh chụp + Biển số xe nhận dạng lần 1

**Chức năng liên quan**: FUNC-001 (Chụp LPR), FUNC-011 (Cân tự động)

---

### FUNC-041: Tích hợp cảm biến LiDAR/Radar

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-041 |
| **Tên chức năng** | Tích hợp cảm biến LiDAR/Radar |
| **Ánh xạ PRD** | FR-001 |
| **Mô-đun** | Tích hợp hạ tầng H/W |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Phát hiện xe tiến vào bằng cảm biến LiDAR/radar và kích hoạt chụp LPR.

**Thông số giao tiếp**:
- Giao thức: TCP/UDP
- Sự kiện: Tín hiệu phát hiện xe tiến vào

**Chức năng liên quan**: FUNC-001 (Chụp LPR), FUNC-011 (Cân tự động)

---

### FUNC-042: Tích hợp bộ phát hiện xe

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-042 |
| **Tên chức năng** | Tích hợp bộ phát hiện xe |
| **Ánh xạ PRD** | FR-005 |
| **Mô-đun** | Tích hợp hạ tầng H/W |
| **Độ ưu tiên** | HIGH |

**Mô tả chức năng**: Xác nhận xe đúng vị trí trên trạm cân bằng bộ phát hiện xe loại cảm biến.

**Thông số giao tiếp**:
- Loại: Cảm biến
- Sự kiện: Tín hiệu có xe/không có xe

**Chức năng liên quan**: FUNC-007 (Thực tích cân), FUNC-011 (Cân tự động)

---

### FUNC-043: Tích hợp Intercom

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-043 |
| **Tên chức năng** | Tích hợp Intercom |
| **Ánh xạ PRD** | FR-007 |
| **Mô-đun** | Tích hợp hạ tầng H/W |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Lắp đặt mỗi bên 1 bộ intercom (thiết bị chính/phụ) để hỗ trợ liên lạc giữa trạm cân và phòng quản lý.

**Chức năng liên quan**: FUNC-025 (Phân luồng cuộc gọi cố định)

---

## 8. Mô-đun 7: Hệ thống phân luồng cuộc gọi cố định

### FUNC-025: Phân luồng cuộc gọi cố định

| Hạng mục | Nội dung |
|----------|---------|
| **Mã chức năng** | FUNC-025 |
| **Tên chức năng** | Phân luồng cuộc gọi cố định |
| **Ánh xạ PRD** | FR-007 |
| **Mô-đun** | Hệ thống phân luồng cuộc gọi cố định |
| **Độ ưu tiên** | MEDIUM |

**Mô tả chức năng**: Giải quyết vấn đề tập trung cuộc gọi hỏi đáp vào một bộ phận cụ thể bằng cách phân luồng cuộc gọi đến bộ phận phụ trách phù hợp theo loại câu hỏi.

**Điều kiện tiên quyết (Preconditions)**:
- Lắp đặt intercom hoàn tất
- Đăng ký thông tin cơ sở liên hệ theo loại câu hỏi

**Điều kiện hậu (Postconditions)**:
- Ghi lại lịch sử cuộc gọi (tb_inquiry_call)

**Dữ liệu đầu vào**:

| Tên trường | Kiểu | Bắt buộc | Quy tắc kiểm tra |
|------------|------|----------|-------------------|
| inquiry_type | string | Y | Loại câu hỏi (Lỗi cân/Hỏi đáp điều phối xe/Hỏi đáp xuất cổng, v.v.) |
| target_dept | string | Y | Bộ phận đích (Phòng điều phối logistics/Kho vật liệu, v.v.) |

**Dữ liệu đầu ra**:

| Tên trường | Kiểu | Mô tả |
|------------|------|-------|
| call_id | bigint | ID lịch sử cuộc gọi |
| target_phone | string | Số điện thoại kết nối |

**Quy tắc nghiệp vụ**:
- BR-025-1: Tự động ánh xạ bộ phận phụ trách theo loại câu hỏi
- BR-025-2: Phòng điều phối logistics: Hỏi đáp lỗi cân, xe tiến vào
- BR-025-3: Kho vật liệu: Hỏi đáp liên quan nguyên vật liệu phụ/vật tư
- BR-025-4: Ghi lại lịch sử cuộc gọi (tb_inquiry_call)

**Luồng chính (Main Flow)**:
1. Tài xế chọn loại câu hỏi trên ứng dụng di động hoặc intercom
2. Hiển thị thông tin liên hệ bộ phận phụ trách theo loại
3. Kết nối điện thoại
4. Ghi lại lịch sử cuộc gọi (POST /api/v1/inquiries/call-log)

**Chức năng liên quan**: FUNC-023 (Cuộc gọi hỏi đáp di động), FUNC-043 (Intercom)

---

## 9. Ma trận truy vết yêu cầu

### 9.1 Bảng ánh xạ FR-ID ↔ FUNC-ID

| PRD FR-ID | Tên FR | FUNC-ID | Tên FUNC | Mô-đun |
|-----------|--------|---------|----------|--------|
| FR-001 | Tự động nhận dạng biển số xe LPR | FUNC-001 | Chụp tự động biển số xe LPR | M1: LPR |
| FR-001 | Tự động nhận dạng biển số xe LPR | FUNC-002 | Xác minh biển số xe bằng AI | M1: LPR |
| FR-001 | Tự động nhận dạng biển số xe LPR | FUNC-003 | Tự động khớp điều phối xe | M1: LPR |
| FR-001 | Tự động nhận dạng biển số xe LPR | FUNC-040 | Tích hợp camera LPR | M6: H/W |
| FR-001 | Tự động nhận dạng biển số xe LPR | FUNC-041 | Tích hợp cảm biến LiDAR/Radar | M6: H/W |
| FR-002 | Cân bảo mật OTP di động | FUNC-004 | Cân bảo mật OTP | M1: LPR |
| FR-002 | Cân bảo mật OTP di động | FUNC-020 | Nhập OTP trên di động | M4: Di động |
| FR-002 | Cân bảo mật OTP di động | FUNC-012 | Điều khiển bảng điện | M3: CS |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-017 | Đăng nhập di động | M4: Di động |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-018 | Tra cứu/Chọn điều phối xe | M4: Di động |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-019 | Tiến hành cân trên di động | M4: Di động |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-021 | Tra cứu/Chia sẻ phiếu cân điện tử | M4: Di động |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-022 | Tra cứu thực tích điều phối xe/cân | M4: Di động |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-023 | Thông báo/Cuộc gọi hỏi đáp | M4: Di động |
| FR-003 | Ứng dụng di động quản lý cân | FUNC-024 | Nhận thông báo Push | M4: Di động |
| FR-004 | Hệ thống quản lý cân trên Web | FUNC-005 | Đăng ký/Quản lý điều phối xe | M2: Web |
| FR-004 | Hệ thống quản lý cân trên Web | FUNC-006 | Quản lý hiện trạng cân | M2: Web |
| FR-004 | Hệ thống quản lý cân trên Web | FUNC-008 | Quản lý thông tin cơ sở | M2: Web |
| FR-004 | Hệ thống quản lý cân trên Web | FUNC-030 | Quản lý xuất cổng | M2: Web |
| FR-005 | Chương trình CS trạm cân | FUNC-010 | Nhận giá trị trọng lượng từ Indicator | M3: CS |
| FR-005 | Chương trình CS trạm cân | FUNC-011 | Quy trình cân tự động LPR | M3: CS |
| FR-005 | Chương trình CS trạm cân | FUNC-013 | Điều khiển thanh chắn tự động | M3: CS |
| FR-005 | Chương trình CS trạm cân | FUNC-014 | Chế độ cân thủ công | M3: CS |
| FR-005 | Chương trình CS trạm cân | FUNC-015 | Khởi tạo lại/Cân lại | M3: CS |
| FR-005 | Chương trình CS trạm cân | FUNC-016 | Cache cục bộ/Chế độ ngoại tuyến | M3: CS |
| FR-005 | Chương trình CS trạm cân | FUNC-042 | Tích hợp bộ phát hiện xe | M6: H/W |
| FR-006 | Phiếu cân điện tử/Không giấy | FUNC-009 | Tạo/Tra cứu phiếu cân điện tử | M2: Web |
| FR-006 | Phiếu cân điện tử/Không giấy | FUNC-021 | Tra cứu/Chia sẻ phiếu cân điện tử (di động) | M4: Di động |
| FR-007 | Phân luồng cuộc gọi cố định/Hỏi đáp | FUNC-025 | Phân luồng cuộc gọi cố định | M7: Cuộc gọi |
| FR-007 | Phân luồng cuộc gọi cố định/Hỏi đáp | FUNC-023 | Thông báo/Cuộc gọi hỏi đáp (di động) | M4: Di động |
| FR-007 | Phân luồng cuộc gọi cố định/Hỏi đáp | FUNC-043 | Tích hợp Intercom | M6: H/W |
| FR-008 | API di động | FUNC-025-API | API Người dùng/Xác thực | M5: API |
| FR-008 | API di động | FUNC-026-API | API Thông tin điều phối xe | M5: API |
| FR-008 | API di động | FUNC-027-API | API Xử lý cân | M5: API |
| FR-008 | API di động | FUNC-028-API | API Thông báo Push | M5: API |

### 9.2 Kết quả xác minh ánh xạ

| PRD FR-ID | Số chức năng | Độ bao phủ | Trạng thái |
|-----------|-------------|------------|------------|
| FR-001 | 5 | 100% | COVERED |
| FR-002 | 3 | 100% | COVERED |
| FR-003 | 7 | 100% | COVERED |
| FR-004 | 4 | 100% | COVERED |
| FR-005 | 7 | 100% | COVERED |
| FR-006 | 2 | 100% | COVERED |
| FR-007 | 3 | 100% | COVERED |
| FR-008 | 4 | 100% | COVERED |

**Tổng cộng 35 đặc tả chức năng bao phủ 100% 8 yêu cầu chức năng PRD.**

### 9.3 Xác minh ánh xạ NFR

| NFR-ID | Tên NFR | FUNC liên quan | Trạng thái phản ánh |
|--------|---------|----------------|---------------------|
| NFR-001 | Hiệu năng | FUNC-001,002,003,004,006,007,010,024 | COVERED |
| NFR-002 | Bảo mật | FUNC-004,009,017,025-API | COVERED |
| NFR-003 | Khả năng mở rộng | FUNC-003,008 | COVERED |
| NFR-004 | Tính sẵn sàng | FUNC-001,007,010,011,016 | COVERED |
| NFR-005 | Tính dùng được | FUNC-005,006,017 | COVERED |

### 9.4 Xác nhận phản ánh ràng buộc kỹ thuật TRD

| Ràng buộc | FUNC phản ánh | Xác nhận |
|-----------|---------------|----------|
| Spring Boot 3.2 Backend | FUNC-025~028-API, FUNC-005~009 | OK |
| React 18 + Ant Design Web | FUNC-005,006,008,009,030 | OK |
| Flutter 3.x Di động | FUNC-017~024 | OK |
| C# .NET WinForms CS | FUNC-010~016 | OK |
| PostgreSQL 16 DB | Toàn bộ chức năng CRUD dữ liệu | OK |
| Redis 7 Cache/OTP | FUNC-004,008,016 | OK |
| RS-232C Indicator | FUNC-010 | OK |
| TCP/UDP LPR/Cảm biến | FUNC-040,041,042 | OK |
| Xác thực JWT | FUNC-017,025-API | OK |
| WebSocket thời gian thực | FUNC-006 | OK |
| Thông báo FCM Push | FUNC-024,028-API | OK |
| Tích hợp KakaoTalk/SMS | FUNC-009,021 | OK |

---

---

## 10. Hiện trạng triển khai (Implementation Status)

> **Cập nhật lần cuối**: 2026-01-29

### 10.1 Hiện trạng triển khai màn hình Web Frontend

| Đường dẫn | Tên màn hình | Ánh xạ FUNC | Trạng thái triển khai | Ghi chú |
|-----------|-------------|-------------|----------------------|---------|
| `/login` | Đăng nhập | FUNC-025-API | ✅ Hoàn thành | Xác thực JWT, tích hợp theme |
| `/dashboard` | Bảng điều khiển | FUNC-006 | ✅ Hoàn thành | Cấu trúc 3 tab (Tổng quan/Thời gian thực/Phân tích), ECharts 6.0 |
| `/dispatch` | Quản lý điều phối xe | FUNC-005 | ✅ Hoàn thành | CRUD, Tìm kiếm/Bộ lọc/Phân trang |
| `/weighing` | Hiện trạng cân | FUNC-006 | ✅ Hoàn thành | Hiện trạng thời gian thực, WebSocket |
| `/inquiry` | Tra cứu cân | FUNC-006 | ✅ Hoàn thành | Tìm kiếm chi tiết, Xuất Excel |
| `/gate-pass` | Quản lý xuất cổng | FUNC-030 | ✅ Hoàn thành | Quy trình phê duyệt/từ chối |
| `/slips` | Phiếu cân điện tử | FUNC-009 | ✅ Hoàn thành | Tra cứu/Chia sẻ/In |
| `/statistics` | Thống kê/Báo cáo | FUNC-006 | ✅ Hoàn thành | Biểu đồ phân tích theo thời kỳ/điều kiện |
| `/weighing-station` | Điều khiển trạm cân | FUNC-011 | ✅ Hoàn thành | Tab cố định, tích hợp thiết bị thời gian thực |
| `/monitoring` | Giám sát thiết bị | FUNC-010 | ✅ Hoàn thành | Giám sát trạng thái thiết bị |
| `/master/companies` | Quản lý công ty vận tải | FUNC-008 | ✅ Hoàn thành | Mô hình MasterCrudPage |
| `/master/vehicles` | Quản lý xe | FUNC-008 | ✅ Hoàn thành | Mô hình MasterCrudPage |
| `/master/scales` | Quản lý trạm cân | FUNC-008 | ✅ Hoàn thành | Mô hình MasterCrudPage |
| `/master/codes` | Quản lý mã chung | FUNC-008 | ✅ Hoàn thành | Mô hình MasterCrudPage |
| `/notices` | Thông báo | - | ✅ Hoàn thành | Bộ lọc danh mục, thông báo ghim |
| `/help` | Hướng dẫn sử dụng | - | ✅ Hoàn thành | Trợ giúp/FAQ |
| `/mypage` | Trang cá nhân | - | ✅ Hoàn thành | Hồ sơ, Đổi mật khẩu |
| `/admin/users` | Quản lý người dùng | FUNC-008 | ✅ Hoàn thành | Chỉ ADMIN |
| `/admin/settings` | Cài đặt hệ thống | - | ✅ Hoàn thành | Chỉ ADMIN |
| `/admin/audit-logs` | Nhật ký kiểm toán | - | ✅ Hoàn thành | Chỉ ADMIN |

### 10.2 Hiện trạng triển khai màn hình ứng dụng di động

| Màn hình | Ánh xạ FUNC | Trạng thái triển khai | Ghi chú |
|----------|-------------|----------------------|---------|
| Đăng nhập (ID/PW) | FUNC-017 | ✅ Hoàn thành | flutter_secure_storage |
| Đăng nhập OTP | FUNC-017 | ✅ Hoàn thành | Đăng nhập nhanh dựa trên OTP |
| Màn hình chính | - | ✅ Hoàn thành | Bảng điều khiển, truy cập nhanh |
| Danh sách điều phối xe | FUNC-018 | ✅ Hoàn thành | Bộ lọc điều phối xe trong ngày |
| Chi tiết điều phối xe | FUNC-018 | ✅ Hoàn thành | Thông tin điều phối xe, chuyển đến cân |
| Nhập OTP | FUNC-020 | ✅ Hoàn thành | Nhập 6 chữ số, bộ đếm thời gian |
| Tiến hành cân | FUNC-019 | ✅ Hoàn thành | Hiển thị trạng thái tiến hành thời gian thực |
| Danh sách phiếu cân điện tử | FUNC-021 | ✅ Hoàn thành | Tra cứu theo ngày |
| Chi tiết phiếu cân điện tử | FUNC-021 | ✅ Hoàn thành | Chia sẻ (share_plus) |
| Tra cứu lịch sử | FUNC-022 | ✅ Hoàn thành | Tìm kiếm theo thời kỳ |
| Thông báo | - | ✅ Hoàn thành | Tra cứu theo danh mục |
| Danh sách thông báo đẩy | FUNC-024 | ✅ Hoàn thành | FCM Push + thông báo trong ứng dụng |

### 10.3 Hiện trạng triển khai Desktop (CS)

| Chức năng | Ánh xạ FUNC | Trạng thái triển khai | Ghi chú |
|-----------|-------------|----------------------|---------|
| Màn hình splash | - | ✅ Hoàn thành | Khởi tạo/Kiểm tra kết nối |
| Màn hình cân chính | FUNC-010,011 | ✅ Hoàn thành | Hiển thị trọng lượng thời gian thực |
| Giao tiếp Indicator | FUNC-010 | ✅ Hoàn thành | SerialPort + Simulator |
| Tích hợp camera LPR | FUNC-040 | ✅ Hoàn thành | Interface + Simulator |
| Tích hợp bộ phát hiện xe | FUNC-042 | ✅ Hoàn thành | Interface + Simulator |
| Điều khiển bảng điện | FUNC-012 | ✅ Hoàn thành | Giao tiếp TCP |
| Điều khiển thanh chắn | FUNC-013 | ✅ Hoàn thành | Giao tiếp TCP |
| Quy trình cân | FUNC-011 | ✅ Hoàn thành | Bộ điều phối WeighingProcessService |
| Cache cục bộ | FUNC-016 | ✅ Hoàn thành | Dựa trên SQLite |
| Tích hợp máy chủ API | FUNC-015 | ✅ Hoàn thành | HttpClient + JWT |
| Kiểm thử đơn vị | - | ✅ Hoàn thành | xUnit (3 lớp kiểm thử) |

### 10.4 Hiện trạng triển khai mô-đun Backend

| Mô-đun | FUNC chính | Trạng thái triển khai | Ghi chú |
|--------|-----------|----------------------|---------|
| auth | FUNC-017, 025-API | ✅ Hoàn thành | JWT + Đăng nhập OTP + Danh sách đen Redis |
| user | FUNC-008 | ✅ Hoàn thành | CRUD + Quản lý vai trò |
| master | FUNC-008 | ✅ Hoàn thành | Công ty vận tải/Xe/Trạm cân/Mã chung |
| dispatch | FUNC-005 | ✅ Hoàn thành | CRUD + Tìm kiếm + Quản lý trạng thái |
| weighing | FUNC-007 | ✅ Hoàn thành | Quy trình cân + Thống kê |
| gatepass | FUNC-030 | ✅ Hoàn thành | Quy trình phê duyệt/từ chối xuất cổng |
| slip | FUNC-009 | ✅ Hoàn thành | Phiếu cân điện tử + Chia sẻ |
| lpr | FUNC-001,002,003 | ✅ Hoàn thành | Chụp/Xác minh AI/Khớp điều phối xe |
| otp | FUNC-004 | ✅ Hoàn thành | Quản lý OTP dựa trên Redis |
| notification | FUNC-024, 028-API | ✅ Hoàn thành | FCM + Thông báo trong ứng dụng |
| websocket | FUNC-006 | ✅ Hoàn thành | Truyền trạng thái cân/thiết bị thời gian thực |
| dashboard | FUNC-006 | ✅ Hoàn thành | API thống kê |
| audit | - | ✅ Hoàn thành | Nhật ký kiểm toán |

### 10.5 Chức năng triển khai bổ sung (ngoài đặc tả)

| Chức năng | Mô tả | Vị trí triển khai |
|-----------|-------|-------------------|
| Tour hướng dẫn | Hướng dẫn người dùng mới | `OnboardingTour.tsx` |
| Phím tắt | Hỗ trợ phím tắt theo trang | `useKeyboardShortcuts.ts` |
| Phát hiện tab hoạt động | Cập nhật dữ liệu khi chuyển tab trình duyệt | `useTabVisible.ts` |
| Hiệu ứng số | Hiệu ứng hoạt hình thẻ KPI bảng điều khiển | `AnimatedNumber.tsx` |
| Sắp xếp kéo thả | Sắp xếp lại hàng bảng bằng kéo thả | `SortableTable.tsx` (@dnd-kit) |
| Giao diện trạng thái trống | Màn hình hướng dẫn khi không có dữ liệu | `EmptyState.tsx` |
| Yêu thích | Yêu thích điều phối xe/công ty | `FavoriteButton.tsx`, `FavoritesList.tsx` |
| Theme sáng/tối | Hỗ trợ chuyển đổi theme | `ThemeContext.tsx`, `themeConfig.ts` |
| Điều hướng đa tab | Tối đa 10 tab, hỗ trợ tab cố định | `TabContext.tsx`, `pageRegistry.ts` |
| Cache ngoại tuyến (di động) | Dựa trên SharedPreferences | `offline_cache_service.dart` |
| Bộ mô phỏng phần cứng | Mô phỏng thiết bị cho phát triển | `Simulators/*.cs` |

---

*Tài liệu này được soạn dựa trên PRD-20260127-154446, TRD-20260127-155235, WBS-20260127-160043.*
*Hiện trạng triển khai tính đến ngày 2026-01-29.*
