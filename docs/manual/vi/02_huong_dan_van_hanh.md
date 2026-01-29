# Hệ thống Cân thông minh Busan - Hướng dẫn Vận hành

**Mã tài liệu**: MAN-OPS-002
**Phiên bản**: 1.1
**Ngày lập**: 2026-01-29
**Đối tượng**: Quản trị viên hệ thống / Người vận hành (ADMIN)
**Tài liệu tham chiếu**: TRD-20260127-155235, PRD-20260127-154446, FUNC-SPEC v1.0
**Trạng thái**: Draft

---

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Cài đặt và Cấu hình môi trường](#2-cài-đặt-và-cấu-hình-môi-trường)
3. [Quản lý người dùng](#3-quản-lý-người-dùng)
4. [Quản lý bảo mật](#4-quản-lý-bảo-mật)
5. [Giám sát hệ thống](#5-giám-sát-hệ-thống)
6. [Quản lý cơ sở dữ liệu](#6-quản-lý-cơ-sở-dữ-liệu)
7. [Quản lý dữ liệu danh mục](#7-quản-lý-dữ-liệu-danh-mục)
8. [Quản lý thiết bị](#8-quản-lý-thiết-bị)
9. [Xử lý sự cố](#9-xử-lý-sự-cố)
10. [Sao lưu và Phục hồi](#10-sao-lưu-và-phục-hồi)
11. [Quản lý hiệu năng](#11-quản-lý-hiệu-năng)
12. [Danh mục kiểm tra vận hành](#12-danh-mục-kiểm-tra-vận-hành)
13. [Phụ lục](#13-phụ-lục)

---

## 1. Tổng quan hệ thống

### 1.1 Kiến trúc hệ thống

Hệ thống Cân thông minh Busan là hệ thống cân tự động không người lái dựa trên LPR (Nhận dạng biển số xe tự động). Hệ thống bao gồm phần cứng hiện trường, chương trình client trạm cân, máy chủ API, cơ sở dữ liệu, và giao diện web/di động.

```
+-----------------------------------------------------------------------+
|                    Hiện trường trạm cân Nhà máy Busan                  |
|                                                                       |
|  [Camera LPR] [Cảm biến LiDAR] [Radar] [Bộ phát hiện xe] [Indicator] |
|       |            |          |          |            |               |
|       +------------+----------+----------+------------+               |
|                              |                                        |
|                   +----------v-----------+                            |
|                   | Chương trình CS       |<--- RS-232C (Indicator)   |
|                   | trạm cân             |---- TCP/UDP (LPR/Cảm biến)|
|                   | (C# .NET WinForms)   |                           |
|                   +----------+-----------+                            |
|                              |                                        |
|  [Bảng điện tử(OTP)] [Barrier tự động]  |  [Interphone (tự/mẫu)]    |
+------------------------------+----------------------------------------+
                               | HTTPS (REST API)
                               |
+------------------------------+----------------------------------------+
|                      Hạ tầng máy chủ                                  |
|                              |                                        |
|               +--------------v--------------+                         |
|               |    Nginx (Reverse Proxy)     |                         |
|               |   Kết thúc SSL / Cân bằng tải|                         |
|               +------+---------------+------+                         |
|                      |               |                                |
|            +---------v----+  +-------v--------+                       |
|            | Spring Boot  |  | Spring Boot    |                       |
|            | WAS #1       |  | WAS #2         |                       |
|            | (API Server) |  | (API Server)   |                       |
|            +------+-------+  +-------+--------+                       |
|                   |                  |                                 |
|            +------v------------------v--------+                       |
|            |         Service Layer            |                       |
|            |  - Dịch vụ quản lý điều xe        |                       |
|            |  - Dịch vụ quản lý cân            |                       |
|            |  - Dịch vụ người dùng/xác thực    |                       |
|            |  - Dịch vụ xác thực OTP           |                       |
|            |  - Dịch vụ liên kết LPR/AI        |                       |
|            |  - Dịch vụ thông báo              |                       |
|            |  - Truyền thời gian thực WebSocket|                       |
|            +------+---------------+-----------+                       |
|                   |               |                                   |
|        +----------v--+   +-------v------+   +-----------+             |
|        | PostgreSQL   |   |   Redis     |   | AI Engine |             |
|        | (Primary +   |   | (Cache/OTP/ |   | Nhận dạng |             |
|        |  Standby)    |   |  MQ/Session)|   |           |             |
|        +-------------+   +-------------+   +-----------+             |
|                                                                       |
|        +---------------------------------------------+               |
|        |  Monitoring: Prometheus + Grafana            |               |
|        |  Logging: ELK Stack                         |               |
|        +---------------------------------------------+               |
+-----------------------------------------------------------------------+
                               |
                    +----------+----------+
                    |          |          |
             +------v--+ +----v----+ +---v----------+
             | Web      | |Ứng dụng| | Dịch vụ      |
             | (React)  | |di động | | bên ngoài    |
             |          | |(Flutter)| | (KakaoTalk/  |
             |          | |         | |  SMS/FCM)    |
             +---------+  +--------+  +-------------+
```

### 1.2 Công nghệ sử dụng

| Phân loại | Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|-----------|----------|
| Backend | Java | 17 LTS | Ngôn ngữ phát triển API Server |
| Backend | Spring Boot | 3.2.5 | Framework ứng dụng |
| Backend | Spring Security | 6.x | Xác thực JWT + Phân quyền RBAC |
| Database | PostgreSQL | 16.x | CSDL chính (giao dịch ACID) |
| Cache | Redis | 7.x | OTP/Phiên/Cache/Hàng đợi tin nhắn |
| Frontend (Web) | React + TypeScript | 18.x / 5.x | Hệ thống quản lý web |
| Frontend (Web) | Ant Design + Vite | 5.x / 5.x | Thành phần UI + Build |
| Frontend (Di động) | Flutter | 3.x | Ứng dụng di động iOS/Android |
| CS Trạm cân | C# .NET | 11 / 7+ | Chương trình client trạm cân |
| Reverse Proxy | Nginx | 1.24.x | Kết thúc SSL, Cân bằng tải |
| Container | Docker + Docker Compose | Latest | Triển khai container |
| Monitoring | Prometheus + Grafana | Latest | Thu thập và trực quan hóa metrics |
| Logging | ELK Stack | Latest | Quản lý log tập trung |
| CI/CD | Jenkins / GitLab CI | Latest | Tự động hóa build và triển khai |
| VCS | Git (GitLab) | Latest | Quản lý mã nguồn |

### 1.3 Cấu hình mạng

| Nguồn | Đích | Giao thức | Cổng | Mô tả |
|-------|------|-----------|------|-------|
| Thiết bị LPR/Cảm biến | CS Trạm cân | TCP/UDP | Giá trị cấu hình | Nhận dạng biển số xe, sự kiện cảm biến |
| Indicator | CS Trạm cân | RS-232C | Cổng COM | Nhận giá trị khối lượng ổn định |
| CS Trạm cân | Bảng điện tử/Barrier | TCP/RS-485 | Giá trị cấu hình | Hiển thị OTP, Điều khiển barrier |
| CS Trạm cân | Nginx | HTTPS | 443 | Gọi API |
| React Web | Nginx | HTTPS | 443 | Truy cập web |
| React Web | Nginx | WSS | 443 | WebSocket thời gian thực |
| Flutter App | Nginx | HTTPS | 443 | API di động |
| Nginx | Spring Boot | HTTP | 8080/8081 | Reverse proxy |
| Spring Boot | PostgreSQL | TCP | 5432 | Kết nối DB |
| Spring Boot | Redis | TCP | 6379 | Cache/OTP/Phiên |
| Spring Boot | AI Engine | HTTPS | Giá trị cấu hình | Xác minh biển số xe bằng AI |
| Spring Boot | KakaoTalk API | HTTPS | 443 | Gửi tin nhắn thông báo |
| Spring Boot | SMS Gateway | HTTPS | 443 | Gửi SMS |
| Spring Boot | FCM | HTTPS | 443 | Thông báo đẩy |

### 1.4 Cấu hình máy chủ

| Môi trường | Cấu hình | Thông số | Mục đích |
|------------|----------|----------|----------|
| Production - WAS | 2 VM (Active-Active) | 8 vCPU, 32GB RAM, 100GB SSD | Spring Boot API Server |
| Production - DB | 1 VM + Standby | 8 vCPU, 32GB RAM, 500GB SSD | PostgreSQL Primary + Standby |
| Production - Redis | 1 VM | 4 vCPU, 16GB RAM, 50GB SSD | Redis Cache/OTP/Session |
| Production - Nginx | 1 VM | 2 vCPU, 4GB RAM, 50GB SSD | Reverse Proxy, Kết thúc SSL |
| Monitoring | 1 VM | 4 vCPU, 8GB RAM, 200GB SSD | Prometheus, Grafana, ELK |
| Staging | 2 VM | 4 vCPU, 16GB RAM, 200GB SSD | Môi trường kiểm thử trước |
| Development | 1 VM | 4 vCPU, 16GB RAM, 200GB SSD | Môi trường phát triển/kiểm thử tích hợp |

> **Lưu ý**: Tất cả máy chủ vận hành trên nền tảng CentOS / Rocky Linux 9, triển khai trong môi trường on-premise nội bộ.

---

## 2. Cài đặt và Cấu hình môi trường

### 2.1 Yêu cầu môi trường máy chủ

#### Yêu cầu hệ điều hành

```
OS: Rocky Linux 9.x (hoặc CentOS Stream 9)
Kernel: 5.14 trở lên
SELinux: Khuyến nghị chế độ enforcing
Firewall: Kích hoạt firewalld
```

#### Cài đặt gói cần thiết

```bash
# Cài đặt gói cơ bản
sudo dnf update -y
sudo dnf install -y \
    curl wget vim git \
    net-tools lsof htop \
    tar gzip unzip \
    openssl ca-certificates

# Cài đặt Java 17 (Máy chủ WAS)
sudo dnf install -y java-17-openjdk java-17-openjdk-devel
java -version
# Ví dụ đầu ra: openjdk version "17.0.x"

# Thiết lập JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> /etc/profile.d/java.sh
source /etc/profile.d/java.sh
```

#### Cấu hình tường lửa

```bash
# Máy chủ Nginx (Truy cập từ bên ngoài)
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --permanent --add-port=443/tcp

# Máy chủ WAS (Chỉ mạng nội bộ)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp

# Máy chủ DB (Chỉ truy cập từ WAS)
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.x.x.0/24" port protocol="tcp" port="5432" accept'

# Máy chủ Redis (Chỉ truy cập từ WAS)
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.x.x.0/24" port protocol="tcp" port="6379" accept'

# Máy chủ Monitoring
sudo firewall-cmd --permanent --add-port=9090/tcp   # Prometheus
sudo firewall-cmd --permanent --add-port=3000/tcp   # Grafana
sudo firewall-cmd --permanent --add-port=5601/tcp   # Kibana

# Áp dụng tường lửa
sudo firewall-cmd --reload
sudo firewall-cmd --list-all
```

### 2.2 Triển khai dựa trên Docker

#### Cài đặt Docker và Docker Compose

```bash
# Cài đặt Docker
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Khởi động và kích hoạt tự động dịch vụ Docker
sudo systemctl start docker
sudo systemctl enable docker

# Thêm tài khoản vận hành vào nhóm Docker
sudo usermod -aG docker weighing-admin

# Xác nhận cài đặt
docker --version
docker compose version
```

#### Cấu hình Docker Compose (docker-compose.yml)

```yaml
version: '3.8'

services:
  # === Spring Boot API Server #1 ===
  weighing-api-1:
    image: registry.internal/weighing-api:${APP_VERSION:-latest}
    container_name: weighing-api-1
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=${DB_HOST}
      - DB_PORT=${DB_PORT:-5432}
      - DB_NAME=${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=${REDIS_HOST}
      - REDIS_PORT=${REDIS_PORT:-6379}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - AES_SECRET_KEY=${AES_SECRET_KEY}
      - CORS_ORIGIN_WEB=${CORS_ORIGIN_WEB}
      - API_INTERNAL_KEY=${API_INTERNAL_KEY}
    volumes:
      - /data/weighing/logs:/app/logs
      - /data/weighing/lpr-images:/app/lpr-images
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - weighing-net
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 8G

  # === Spring Boot API Server #2 ===
  weighing-api-2:
    image: registry.internal/weighing-api:${APP_VERSION:-latest}
    container_name: weighing-api-2
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=${DB_HOST}
      - DB_PORT=${DB_PORT:-5432}
      - DB_NAME=${DB_NAME}
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=${REDIS_HOST}
      - REDIS_PORT=${REDIS_PORT:-6379}
      - REDIS_PASSWORD=${REDIS_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - AES_SECRET_KEY=${AES_SECRET_KEY}
      - CORS_ORIGIN_WEB=${CORS_ORIGIN_WEB}
      - API_INTERNAL_KEY=${API_INTERNAL_KEY}
    volumes:
      - /data/weighing/logs:/app/logs
      - /data/weighing/lpr-images:/app/lpr-images
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    networks:
      - weighing-net
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 8G

networks:
  weighing-net:
    driver: bridge
```

#### Quy trình triển khai và cập nhật luân phiên

```bash
# 1. Pull image mới
docker pull registry.internal/weighing-api:${NEW_VERSION}

# 2. Cập nhật WAS #1 (Duy trì dịch vụ: WAS #2 xử lý lưu lượng)
docker compose stop weighing-api-1
APP_VERSION=${NEW_VERSION} docker compose up -d weighing-api-1

# 3. Kiểm tra health check WAS #1 (Chờ tối đa 90 giây)
echo "Đang chờ health check WAS #1..."
for i in $(seq 1 30); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "WAS #1 khởi động bình thường"
        break
    fi
    sleep 3
done

# 4. Cập nhật WAS #2
docker compose stop weighing-api-2
APP_VERSION=${NEW_VERSION} docker compose up -d weighing-api-2

# 5. Kiểm tra health check WAS #2
echo "Đang chờ health check WAS #2..."
for i in $(seq 1 30); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "WAS #2 khởi động bình thường"
        break
    fi
    sleep 3
done

echo "Hoàn tất cập nhật luân phiên: version=${NEW_VERSION}"
```

#### Quy trình rollback (trong vòng 5 phút)

```bash
# Rollback ngay lập tức về phiên bản trước
ROLLBACK_VERSION="tag_phiên_bản_trước"

docker compose stop weighing-api-1 weighing-api-2
APP_VERSION=${ROLLBACK_VERSION} docker compose up -d weighing-api-1 weighing-api-2

# Kiểm tra health check
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
curl -s http://localhost:8081/actuator/health | python3 -m json.tool
```

### 2.3 Cấu hình biến môi trường

Biến môi trường của môi trường vận hành được quản lý trong file `.env`. File này phải được hạn chế quyền truy cập bằng `chmod 600`.

#### Cấu hình file .env

```bash
# Đường dẫn file: /opt/weighing/.env
# Quyền: chmod 600, chown weighing-admin:weighing-admin

# === Database ===
DB_HOST=10.x.x.10
DB_PORT=5432
DB_NAME=weighing
DB_USERNAME=weighing_app
DB_PASSWORD=<mật_khẩu_mạnh>

# === Redis ===
REDIS_HOST=10.x.x.20
REDIS_PORT=6379
REDIS_PASSWORD=<mật_khẩu_mạnh>

# === JWT ===
# Khóa bí mật mã hóa Base64 từ 256bit trở lên
JWT_SECRET=<khóa_bí_mật_mã_hóa_Base64>

# === Khóa mã hóa AES-256 ===
AES_SECRET_KEY=<khóa_AES_mã_hóa_Base64>

# === CORS ===
CORS_ORIGIN_WEB=https://weighing.factory.internal

# === Internal API Key ===
API_INTERNAL_KEY=<khóa_xác_thực_nội_bộ_CS_trạm_cân>

# === Application Version ===
APP_VERSION=1.0.0
```

#### Lưu ý bảo mật biến môi trường

- File `.env` tuyệt đối không được commit vào Git (kiểm tra đã có trong `.gitignore`).
- Đặt quyền file là `600` để chỉ chủ sở hữu mới có thể đọc.
- JWT_SECRET và AES_SECRET_KEY sử dụng giá trị ngẫu nhiên từ 256bit trở lên được mã hóa Base64.
- Khi thay đổi mật khẩu, cần khởi động lại các dịch vụ liên quan theo thứ tự.

#### Cách tạo khóa bí mật

```bash
# Tạo JWT Secret (256bit)
openssl rand -base64 32

# Tạo khóa AES-256
openssl rand -base64 32

# Tạo Internal API Key
openssl rand -hex 32
```

### 2.4 Cấu hình Nginx

#### Cấu hình cơ bản (/etc/nginx/nginx.conf)

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
    multi_accept on;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    # Định dạng log
    log_format main '$remote_addr - $remote_user [$time_local] '
                    '"$request" $status $body_bytes_sent '
                    '"$http_referer" "$http_user_agent" '
                    '$request_time $upstream_response_time';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    client_max_body_size 50M;

    # Nén Gzip
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript
               text/xml application/xml image/svg+xml;

    # Giới hạn tần suất
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/s;
    limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=5r/s;

    # Upstream (Cân bằng tải API Server)
    upstream weighing_api {
        least_conn;
        server 10.x.x.1:8080 max_fails=3 fail_timeout=30s;
        server 10.x.x.1:8081 max_fails=3 fail_timeout=30s;
        keepalive 32;
    }

    include /etc/nginx/conf.d/*.conf;
}
```

#### Cấu hình site (/etc/nginx/conf.d/weighing.conf)

```nginx
# Chuyển hướng HTTP -> HTTPS
server {
    listen 80;
    server_name weighing.factory.internal;
    return 301 https://$host$request_uri;
}

# Máy chủ HTTPS
server {
    listen 443 ssl http2;
    server_name weighing.factory.internal;

    # Chứng chỉ SSL
    ssl_certificate     /etc/nginx/ssl/weighing.crt;
    ssl_certificate_key /etc/nginx/ssl/weighing.key;

    # Cấu hình TLS
    ssl_protocols TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers 'TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256';

    # Header bảo mật
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' wss://$host" always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    # File tĩnh React
    location / {
        root /var/www/weighing;
        index index.html;
        try_files $uri $uri/ /index.html;

        # Cache file tĩnh
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # Proxy API
    location /api/ {
        limit_req zone=api_limit burst=20 nodelay;
        proxy_pass http://weighing_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 5s;
        proxy_read_timeout 30s;
        proxy_send_timeout 10s;
    }

    # Giới hạn tần suất API Xác thực (nghiêm ngặt hơn)
    location /api/v1/auth/ {
        limit_req zone=auth_limit burst=5 nodelay;
        proxy_pass http://weighing_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Proxy WebSocket
    location /ws/ {
        proxy_pass http://weighing_api;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 86400s;
    }

    # Actuator (Chỉ cho phép mạng nội bộ)
    location /actuator/ {
        allow 10.0.0.0/8;
        deny all;
        proxy_pass http://weighing_api;
        proxy_set_header Host $host;
    }

    # Swagger UI (Chỉ môi trường phát triển/staging)
    # location /swagger-ui/ {
    #     allow 10.0.0.0/8;
    #     deny all;
    #     proxy_pass http://weighing_api;
    # }
}
```

#### Kiểm tra và áp dụng cấu hình Nginx

```bash
# Kiểm tra cú pháp cấu hình
sudo nginx -t

# Tải lại cấu hình (không gián đoạn)
sudo nginx -s reload

# Khởi động lại toàn bộ (khi cần)
sudo systemctl restart nginx

# Kiểm tra trạng thái
sudo systemctl status nginx
```

### 2.5 Quản lý chứng chỉ SSL

#### Cấu trúc file chứng chỉ

```
/etc/nginx/ssl/
  weighing.crt      # Chứng chỉ máy chủ (+ Chuỗi chứng chỉ trung gian)
  weighing.key       # Khóa riêng tư (chmod 600)
  ca-bundle.crt      # Gói chứng chỉ CA (khi cần)
```

#### Cài đặt chứng chỉ

```bash
# Tạo thư mục chứng chỉ
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl

# Sao chép file chứng chỉ
sudo cp weighing.crt /etc/nginx/ssl/
sudo cp weighing.key /etc/nginx/ssl/

# Thiết lập quyền
sudo chmod 644 /etc/nginx/ssl/weighing.crt
sudo chmod 600 /etc/nginx/ssl/weighing.key
sudo chown root:root /etc/nginx/ssl/*
```

#### Kiểm tra hạn chứng chỉ

```bash
# Kiểm tra ngày hết hạn chứng chỉ
openssl x509 -enddate -noout -in /etc/nginx/ssl/weighing.crt
# Ví dụ đầu ra: notAfter=Jan 29 00:00:00 2027 GMT

# Kiểm tra thông tin chi tiết chứng chỉ
openssl x509 -text -noout -in /etc/nginx/ssl/weighing.crt | head -20

# Kiểm tra chứng chỉ từ xa
openssl s_client -connect weighing.factory.internal:443 -servername weighing.factory.internal </dev/null 2>/dev/null | openssl x509 -noout -dates
```

#### Quy trình gia hạn chứng chỉ

1. Yêu cầu cấp chứng chỉ mới (CA nội bộ hoặc CA bên ngoài).
2. Sao chép file chứng chỉ mới vào đường dẫn `/etc/nginx/ssl/`.
3. Thực hiện kiểm tra cấu hình bằng `sudo nginx -t`.
4. Áp dụng không gián đoạn bằng `sudo nginx -s reload`.
5. Xác nhận thông tin chứng chỉ trên trình duyệt.

> **Quan trọng**: Bắt đầu quy trình gia hạn 30 ngày trước khi chứng chỉ hết hạn. Khuyến nghị cấu hình cảnh báo giám sát hết hạn chứng chỉ trong Prometheus.

### 2.6 Thiết lập cơ sở dữ liệu ban đầu

#### Cài đặt và cấu hình PostgreSQL

```bash
# Cài đặt PostgreSQL 16
sudo dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-9-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo dnf install -y postgresql16-server postgresql16-contrib
sudo /usr/pgsql-16/bin/postgresql-16-setup initdb
sudo systemctl start postgresql-16
sudo systemctl enable postgresql-16
```

#### Tạo cơ sở dữ liệu và người dùng

```sql
-- Kết nối PostgreSQL
-- sudo -u postgres psql

-- Tạo người dùng ứng dụng
CREATE USER weighing_app WITH PASSWORD 'thiết_lập_mật_khẩu_mạnh';

-- Tạo cơ sở dữ liệu
CREATE DATABASE weighing
    OWNER weighing_app
    ENCODING 'UTF8'
    LC_COLLATE 'ko_KR.UTF-8'
    LC_CTYPE 'ko_KR.UTF-8'
    TEMPLATE template0;

-- Thiết lập quyền
GRANT ALL PRIVILEGES ON DATABASE weighing TO weighing_app;
\c weighing
GRANT ALL ON SCHEMA public TO weighing_app;

-- Người dùng chỉ đọc (dùng cho giám sát)
CREATE USER weighing_readonly WITH PASSWORD 'mật_khẩu_riêng';
GRANT CONNECT ON DATABASE weighing TO weighing_readonly;
GRANT USAGE ON SCHEMA public TO weighing_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO weighing_readonly;
```

#### Cấu hình pg_hba.conf

```
# /var/lib/pgsql/16/data/pg_hba.conf

# TYPE  DATABASE        USER            ADDRESS                 METHOD
local   all             postgres                                peer
local   all             all                                     scram-sha-256
host    weighing        weighing_app    10.x.x.0/24            scram-sha-256
host    weighing        weighing_readonly 10.x.x.0/24          scram-sha-256
host    replication     replicator      10.x.x.11/32           scram-sha-256
```

#### Cấu hình chính postgresql.conf (Môi trường vận hành)

```
# /var/lib/pgsql/16/data/postgresql.conf

# Cấu hình kết nối
listen_addresses = '10.x.x.10'
port = 5432
max_connections = 200

# Cấu hình bộ nhớ (Dựa trên 32GB RAM)
shared_buffers = 8GB
effective_cache_size = 24GB
work_mem = 64MB
maintenance_work_mem = 2GB

# Cấu hình WAL
wal_level = replica
max_wal_senders = 5
wal_keep_size = 1GB
archive_mode = on
archive_command = 'cp %p /data/pg_archive/%f'

# Ghi log
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_rotation_age = 1d
log_rotation_size = 100MB
log_min_duration_statement = 3000
log_line_prefix = '%t [%p] %u@%d '

# Hiệu năng
checkpoint_completion_target = 0.9
random_page_cost = 1.1
effective_io_concurrency = 200

# Múi giờ
timezone = 'Asia/Seoul'
```

#### Nhập dữ liệu ban đầu

```sql
-- Tạo tài khoản quản trị viên hệ thống (Mật khẩu ban đầu: Admin1234!)
-- bcrypt hash (cost=12)
INSERT INTO tb_user (user_name, phone_number, user_role, login_id, password_hash)
VALUES (
    '시스템관리자',
    '010-0000-0000',
    'ADMIN',
    'admin',
    '$2a$12$LJ3MFgfFw.PAGtv.Q0n.aeF8VPx4dSmA5WVUkRrXQJQNvk7z3K5Hm'
);
```

> **Quan trọng**: Sau khi tạo tài khoản quản trị viên ban đầu, bắt buộc phải thay đổi mật khẩu.

### 2.7 Cấu hình Redis

#### Cài đặt Redis

```bash
sudo dnf install -y redis
sudo systemctl start redis
sudo systemctl enable redis
```

#### Cấu hình chính redis.conf

```
# /etc/redis/redis.conf

# Mạng
bind 10.x.x.20
port 6379
protected-mode yes

# Xác thực
requirepass <mật_khẩu_mạnh>

# Bộ nhớ (Dựa trên 16GB RAM)
maxmemory 8gb
maxmemory-policy allkeys-lru

# Bền vững
save 3600 1
save 300 100
save 60 10000

# Snapshot RDB
dbfilename dump.rdb
dir /var/lib/redis

# Ghi log
loglevel notice
logfile /var/log/redis/redis.log

# Bảo mật
rename-command FLUSHALL ""
rename-command FLUSHDB ""
rename-command DEBUG ""

# Hiệu năng
tcp-keepalive 300
timeout 300
```

#### Kiểm tra kết nối Redis

```bash
# Kiểm tra kết nối nội bộ
redis-cli -h 10.x.x.20 -a '<mật_khẩu>' ping
# Đầu ra: PONG

# Kiểm tra sử dụng bộ nhớ
redis-cli -h 10.x.x.20 -a '<mật_khẩu>' info memory | grep used_memory_human

# Kiểm tra mẫu khóa (Giám sát trong vận hành)
redis-cli -h 10.x.x.20 -a '<mật_khẩu>' --scan --pattern 'auth:*' | head -10
redis-cli -h 10.x.x.20 -a '<mật_khẩu>' --scan --pattern 'otp:*' | head -10
```

---

## 3. Quản lý người dùng

### 3.1 Hệ thống vai trò người dùng

Hệ thống quản lý quyền truy cập dựa trên 3 vai trò (Role).

| Vai trò | Mã | Mô tả | Quyền chính |
|---------|-----|-------|-------------|
| Quản trị viên hệ thống | ADMIN | Quản lý vận hành toàn bộ hệ thống | Truy cập tất cả chức năng, Quản lý người dùng, Cài đặt hệ thống, Quản lý thông báo/FAQ |
| Nhân viên cân | MANAGER | Phụ trách nghiệp vụ cân | Đăng ký/sửa điều xe, Quản lý cân, Quản lý xuất cổng, Xem dữ liệu danh mục |
| Tài xế | DRIVER | Lái xe | Đăng nhập ứng dụng di động, Xem điều xe, Tiến hành cân, Phiếu cân điện tử |

#### Ma trận quyền truy cập theo vai trò

| Chức năng | ADMIN | MANAGER | DRIVER |
|-----------|:-----:|:-------:|:------:|
| Tạo/sửa/xóa người dùng | O | X | X |
| Thay đổi vai trò | O | X | X |
| Mở khóa tài khoản | O | X | X |
| Đặt lại mật khẩu | O | X | X |
| Xem nhật ký kiểm toán | O | X | X |
| Quản lý cài đặt hệ thống | O | X | X |
| Quản lý thông báo (Đăng/Sửa/Xóa/Ghim) | O | X | X |
| Quản lý FAQ (Đăng/Sửa/Xóa) | O | X | X |
| Giám sát thiết bị | O | X | X |
| Kích hoạt health check thiết bị | O | X | X |
| Đăng ký/sửa điều xe | O | O | X |
| Xóa điều xe | O | X | X |
| Quản lý cân | O | O | X |
| Quản lý xuất cổng | O | O | X |
| Đăng ký/sửa dữ liệu danh mục | O | X | X |
| Xem dữ liệu danh mục | O | O | X |
| Xem thống kê | O | O | X |
| Xem điều xe của tôi | O | O | O |
| Tiến hành cân trên di động | X | X | O |
| Xem phiếu cân điện tử | O | O | O |
| Xác thực OTP | X | X | O |

### 3.2 Đăng ký người dùng

#### Đăng ký qua giao diện quản lý web

1. Đăng nhập hệ thống web bằng tài khoản ADMIN.
2. Chọn **[Quản lý hệ thống] > [Quản lý người dùng]** từ menu bên trái.
3. Nhấp nút **[Đăng ký mới]**.
4. Nhập các trường bắt buộc:
   - **ID đăng nhập**: 3~50 ký tự, kết hợp chữ cái/số (không trùng lặp)
   - **Mật khẩu**: Tối thiểu 8 ký tự, bắt buộc có chữ cái + số
   - **Tên người dùng**: Nhập tên thật
   - **Số điện thoại**: Định dạng 010-XXXX-XXXX (Dùng cho xác thực OTP di động)
   - **Vai trò**: Chọn ADMIN / MANAGER / DRIVER
   - **Công ty vận tải**: Bắt buộc chọn khi vai trò là DRIVER
5. Nhấp nút **[Lưu]**.
6. Xác nhận thông báo đăng ký thành công.

#### Đăng ký qua API

```bash
# API tạo người dùng
curl -X POST https://weighing.factory.internal/api/v1/users \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "loginId": "driver001",
    "password": "SecurePass123",
    "userName": "홍길동",
    "phoneNumber": "010-1234-5678",
    "userRole": "DRIVER",
    "companyId": 1
  }'
```

Ví dụ phản hồi:
```json
{
  "success": true,
  "data": {
    "userId": 10,
    "userName": "홍길동",
    "phoneNumber": "010-****-5678",
    "userRole": "DRIVER",
    "companyName": "ABC운수",
    "isActive": true,
    "createdAt": "2026-01-29T10:00:00+09:00"
  }
}
```

### 3.3 Sửa đổi và vô hiệu hóa người dùng

#### Sửa đổi thông tin người dùng

1. Tìm kiếm người dùng mục tiêu trên màn hình **[Quản lý người dùng]**.
2. Nhấp nút **[Sửa]** trên hàng người dùng.
3. Các trường có thể sửa: Tên người dùng, Số điện thoại, Vai trò, Công ty vận tải.
4. Nhấp **[Lưu]** để áp dụng thay đổi.

> **Chú ý**: ID đăng nhập không thể thay đổi sau khi tạo.

#### Vô hiệu hóa người dùng

Đối với nhân viên nghỉ việc hoặc không cần truy cập nữa, thực hiện vô hiệu hóa thay vì xóa.

```bash
# API bật/tắt trạng thái hoạt động người dùng
curl -X PATCH https://weighing.factory.internal/api/v1/users/10/toggle-active \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

Tài khoản bị vô hiệu hóa sẽ trả về lỗi `AUTH_002` (Tài khoản bị vô hiệu hóa) khi đăng nhập.

### 3.4 Chính sách mật khẩu

| Mục | Chính sách |
|-----|-----------|
| Độ dài tối thiểu | Tối thiểu 8 ký tự |
| Thành phần ký tự | Bắt buộc có chữ cái + số |
| Thuật toán băm | bcrypt (cost factor 12) |
| Khóa khi đăng nhập thất bại | Khóa 30 phút sau 5 lần thất bại liên tiếp |
| Mật khẩu ban đầu | ADMIN thiết lập thủ công và gửi cho người dùng |

#### Quy trình đặt lại mật khẩu

Khi người dùng quên mật khẩu:

1. Người dùng (hoặc bộ phận quản lý) yêu cầu ADMIN đặt lại mật khẩu.
2. ADMIN tìm kiếm người dùng mục tiêu trên màn hình **[Quản lý người dùng]**.
3. Nhấp nút **[Đặt lại mật khẩu]**.
4. Tạo và thiết lập mật khẩu tạm thời.
5. Gửi mật khẩu tạm thời cho người dùng qua kênh an toàn (gửi trực tiếp, v.v.).
6. Người dùng đăng nhập và thay đổi mật khẩu.

### 3.5 Khóa và mở khóa tài khoản

#### Điều kiện khóa tự động

- Tài khoản tự động bị khóa trong 30 phút sau 5 lần đăng nhập thất bại liên tiếp.
- Khi bị khóa, đăng nhập bị từ chối ngay cả khi nhập đúng mật khẩu.
- Sau 30 phút, khóa tự động được giải phóng và số lần thất bại được đặt lại.

#### Mở khóa thủ công

ADMIN có thể mở khóa tài khoản bị khóa ngay lập tức.

```bash
# API mở khóa tài khoản
curl -X POST https://weighing.factory.internal/api/v1/users/10/unlock \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

Trên giao diện web, nhấp nút **[Mở khóa]** bên cạnh người dùng bị khóa trong **[Quản lý người dùng]**.

#### Kiểm tra trạng thái khóa

```sql
-- Kiểm tra trạng thái khóa trực tiếp từ DB
SELECT user_id, login_id, user_name, failed_login_count, locked_until,
       CASE WHEN locked_until > NOW() THEN 'LOCKED'
            ELSE 'UNLOCKED' END AS lock_status
FROM tb_user
WHERE failed_login_count > 0 OR locked_until IS NOT NULL;
```

---

## 4. Quản lý bảo mật

### 4.1 Cấu trúc xác thực JWT

Hệ thống sử dụng phương thức xác thực Stateless dựa trên JWT (JSON Web Token).

#### Cấu trúc token

| Loại token | Thời hạn | Nơi lưu trữ | Mục đích |
|------------|----------|-------------|----------|
| Access Token | 30 phút (1800 giây) | Bộ nhớ client | Xác thực API |
| Refresh Token | 7 ngày | Redis (băm SHA-256) | Gia hạn Access Token |

#### Access Token Claims (Payload)

```json
{
  "sub": "1",
  "login_id": "admin",
  "role": "ADMIN",
  "company_id": null,
  "device_type": "WEB",
  "jti": "unique-token-id",
  "iss": "weighing-api",
  "iat": 1738123200,
  "exp": 1738125000
}
```

#### Luồng xác thực

```
[Client]                       [Nginx]              [Spring Boot]            [Redis]
    |                            |                       |                      |
    |-- POST /auth/login ------->|---proxy------------>  |                      |
    |                            |                       |-- Xác minh user ---->|
    |                            |                       |<- Trả kết quả ------|
    |                            |                       |-- Lưu Refresh Token->|
    |<-- Access + Refresh Token--|<-- Cấp token ---------|                      |
    |                            |                       |                      |
    |-- Yêu cầu API (Bearer)--->|---proxy------------>  |                      |
    |                            |                       |-- Xác minh JWT       |
    |                            |                       |-- Kiểm tra blacklist>|
    |                            |                       |<- Trả kết quả ------|
    |<-- Phản hồi API ----------|<-- Trả phản hồi ------|                      |
    |                            |                       |                      |
    |-- POST /auth/refresh ----->|---proxy------------>  |                      |
    |                            |                       |-- Xác minh Refresh ->|
    |                            |                       |<- So sánh giá trị --|
    |<-- Access Token mới -------|<-- Cấp token mới ----|                      |
```

#### Xử lý đăng xuất

Khi đăng xuất, hai thao tác sau được thực hiện:
1. Xóa Refresh Token khỏi Redis để chặn gia hạn.
2. Đăng ký JTI của Access Token vào blacklist để chặn sử dụng trong thời gian còn hiệu lực.

### 4.2 Quản lý xác thực OTP

OTP là biện pháp bảo mật cho việc cân qua di động khi nhận dạng biển số xe LPR thất bại.

#### Luồng xử lý OTP

```
[CS Trạm cân]     [API Server]       [Redis]          [Bảng điện tử]  [App di động]
    |                   |               |                 |                |
    |-- Yêu cầu tạo OTP->|              |                 |                |
    |                   |-- Tạo 6 số -->|                 |                |
    |                   |   (TTL 5 phút)|                 |                |
    |<-- Mã OTP --------|               |                 |                |
    |-- Hiển thị OTP -->|               |                 |                |
    |                   |               |               [123456]           |
    |                   |               |                 |                |
    |                   |               |                 |   Nhập OTP    |
    |                   |<-- Yêu cầu xác minh--|---------|----+---------->|
    |                   |-- Truy vấn Redis ->|           |                |
    |                   |<- Trả phiên -------|           |                |
    |                   |-- Xác minh mã     |           |                |
    |                   |-- Khi thành công ->| (Xóa khóa)|               |
    |<-- Tiếp tục cân --|               |                 |                |
```

#### Chính sách OTP

| Mục | Giá trị cấu hình | Mô tả |
|-----|-------------------|-------|
| Độ dài mã | 6 chữ số | Tạo bằng SecureRandom |
| Thời gian hiệu lực (TTL) | 5 phút (300 giây) | Quản lý bằng Redis TTL |
| Số lần thất bại tối đa | 3 lần | Vô hiệu hóa OTP khi vượt quá |
| Dùng một lần | Xóa ngay khi xác minh thành công | Không thể tái sử dụng |

#### Cấu trúc khóa OTP trong Redis

| Mẫu khóa | Giá trị | TTL | Mục đích |
|-----------|---------|-----|----------|
| `otp:code:{otpCode}` | JSON (Dữ liệu phiên) | 5 phút | Lưu phiên OTP |
| `otp:scale:{scaleId}` | Chuỗi otpCode | 5 phút | OTP hiện tại theo trạm cân |
| `otp:fail:{otpCode}` | Số lần thất bại | 5 phút | Đếm số lần thất bại |

#### Xử lý khi OTP gặp sự cố

Khi Redis gặp sự cố, dịch vụ OTP sẽ truy vấn trực tiếp bảng `tb_otp_session` trong PostgreSQL để xử lý Fallback.

### 4.3 Chính sách mã hóa

| Đối tượng | Thuật toán | Chi tiết |
|-----------|-----------|----------|
| Mật khẩu | bcrypt | cost factor 12, Băm một chiều |
| Số điện thoại (Lưu DB) | AES-256-GCM | IV + CipherText + Tag, Mã hóa Base64 |
| Truyền thông | TLS 1.3 | Kết thúc SSL tại Nginx |
| Chữ ký JWT | HMAC-SHA256 | Khóa bí mật mã hóa Base64 |
| Refresh Token (Redis) | SHA-256 | Lưu giá trị băm thay vì bản gốc |

#### Hướng dẫn quản lý khóa mã hóa

- Khóa AES và JWT Secret được quản lý qua biến môi trường.
- Khóa phải có tối thiểu 256bit (32byte).
- Chu kỳ thay khóa: Mỗi năm 1 lần hoặc thay ngay khi có sự cố bảo mật.
- Khi thay khóa, cần thực hiện migration tái mã hóa dữ liệu đã mã hóa.

### 4.4 Kiểm soát truy cập (RBAC)

Chính sách RBAC dựa trên Spring Security được áp dụng như sau.

#### Kiểm soát truy cập theo API endpoint

| HTTP Method | Mẫu endpoint | Vai trò yêu cầu |
|-------------|-------------|-----------------|
| POST | /api/v1/auth/login | Không cần xác thực |
| POST | /api/v1/auth/login/otp | Không cần xác thực |
| POST | /api/v1/auth/refresh | Refresh Token |
| POST | /api/v1/otp/verify | Không cần xác thực |
| POST | /api/v1/otp/generate | Internal API Key |
| DELETE | /api/v1/dispatches/** | ADMIN |
| POST, PUT | /api/v1/master/** | ADMIN |
| POST | /api/v1/dispatches | ADMIN, MANAGER |
| PUT | /api/v1/dispatches/** | ADMIN, MANAGER |
| ALL | /api/v1/gate-passes/** | ADMIN, MANAGER |
| GET | /api/v1/dispatches/my | DRIVER |
| ALL | /api/v1/** (Khác) | Người dùng đã xác thực |
| ALL | /actuator/health | Không cần xác thực |

### 4.5 Quản lý nhật ký kiểm toán

Tất cả sự kiện liên quan đến bảo mật đều được ghi vào nhật ký kiểm toán.

#### Các sự kiện được ghi

| Sự kiện | Nội dung ghi | Mức log |
|---------|-------------|---------|
| Đăng nhập thành công | userId, loginId, deviceType, IP, timestamp | INFO |
| Đăng nhập thất bại | loginId, Lý do thất bại, Số lần thất bại, IP | WARN |
| Khóa tài khoản | userId, loginId, lockedUntil | WARN |
| Đăng xuất | userId, loginId, deviceType | INFO |
| Tạo OTP | scaleId, vehicleId, plateNumber | INFO |
| Xác minh OTP thành công | otpCode(đã che), phoneNumber(đã che), vehicleId | INFO |
| Xác minh OTP thất bại | otpCode(đã che), phoneNumber(đã che), Số lần thất bại | WARN |
| Từ chối quyền | userId, URI yêu cầu, Vai trò cần thiết | WARN |
| Tạo/sửa người dùng | userId thực hiện, userId đối tượng, Nội dung thay đổi | INFO |
| Thay đổi điều xe | userId thực hiện, dispatchId, Nội dung thay đổi | INFO |
| Thay đổi dữ liệu cân | userId thực hiện, weighingId, Nội dung thay đổi | INFO |

#### Định dạng nhật ký kiểm toán

```
[AUDIT] {event} | userId={} | ip={} | detail={}
```

Ví dụ:
```
[AUDIT] LOGIN_SUCCESS | userId=1 | ip=192.168.1.100 | detail=loginId=admin, device=WEB
[AUDIT] LOGIN_FAILED  | userId=null | ip=192.168.1.100 | detail=loginId=hong, reason=PASSWORD_MISMATCH, attempts=3
[AUDIT] ACCOUNT_LOCKED | userId=5 | ip=192.168.1.100 | detail=loginId=driver01, lockedUntil=2026-01-29T15:30:00
```

#### Truy vấn nhật ký kiểm toán

Trên giao diện quản lý web, có thể tìm kiếm theo các điều kiện sau trong menu **[Quản lý hệ thống] > [Nhật ký kiểm toán]**:
- Khoảng thời gian (Ngày bắt đầu ~ Ngày kết thúc)
- Loại sự kiện
- ID / Tên người dùng
- Địa chỉ IP

```sql
-- Truy vấn trực tiếp từ DB (khi cần)
SELECT audit_id, event_type, user_id, ip_address, detail, created_at
FROM tb_audit_log
WHERE created_at BETWEEN '2026-01-01' AND '2026-01-31'
  AND event_type = 'LOGIN_FAILED'
ORDER BY created_at DESC
LIMIT 100;
```

### 4.6 Giám sát bảo mật

#### Các mục giám sát thời gian thực

| Mục | Ngưỡng | Xử lý |
|-----|--------|-------|
| Tần suất đăng nhập thất bại | > 10 lần/phút (Cùng IP) | Xem xét chặn IP, Nghi ngờ tấn công brute-force |
| Phát sinh khóa tài khoản | > 5 trường hợp/giờ | Nghi ngờ tấn công hàng loạt tài khoản, Phân tích mẫu |
| Tần suất lỗi 401/403 | > 50 lần/phút | Nghi ngờ cố gắng bỏ qua xác thực |
| Tần suất OTP thất bại | > 20 lần/phút | Nghi ngờ tấn công brute-force OTP |
| Mẫu gọi API bất thường | Vượt Rate limit | Tự động chặn (Nginx rate limiting) |

#### Trạng thái phòng thủ OWASP Top 10

| Mối đe dọa | Biện pháp phòng thủ |
|------------|---------------------|
| SQL Injection | JPA Parameterized Query (PreparedStatement) |
| XSS | React tự động escape + Header CSP |
| CSRF | Dựa trên JWT Stateless (Không có SameSite Cookie) |
| Broken Authentication | Băm bcrypt, Khóa tài khoản, Quản lý hết hạn JWT |
| Security Misconfiguration | Tách profile theo môi trường, Áp dụng header bảo mật |
| Sensitive Data Exposure | Mã hóa AES-256, TLS 1.3, Xử lý che dấu |
| Broken Access Control | RBAC, Annotation @PreAuthorize |
| Injection | Bean Validation, Xác minh DTO |
| Insufficient Logging | Nhật ký kiểm toán, ELK Stack |
| SSRF | Hạn chế truy cập mạng nội bộ, Whitelist |

---

## 5. Giám sát hệ thống

### 5.1 Giám sát trạng thái thiết bị

Giám sát thời gian thực trạng thái online/offline/error của thiết bị hiện trường trạm cân.

#### Thiết bị được giám sát

| Thiết bị | Phương thức giao tiếp | Phương pháp health check | Chu kỳ kiểm tra |
|---------|----------------------|-------------------------|----------------|
| Camera LPR | TCP | Xác nhận kết nối TCP + Kiểm tra chụp | 30 giây |
| Cảm biến LiDAR | TCP/UDP | Xác nhận nhận dữ liệu cảm biến | 10 giây |
| Cảm biến Radar | TCP/UDP | Xác nhận nhận dữ liệu cảm biến | 10 giây |
| Bộ phát hiện xe | TCP/UDP | Xác nhận nhận sự kiện | 10 giây |
| Indicator | RS-232C | Xác nhận nhận giá trị khối lượng | 5 giây |
| Bảng điện tử | TCP/RS-485 | Xác nhận trạng thái giao tiếp | 30 giây |
| Barrier tự động | TCP/RS-485 | Xác nhận phản hồi trạng thái | 30 giây |

#### Hiển thị trạng thái thiết bị

Có thể xác nhận các trạng thái sau trên bảng điều khiển **[Quản lý hệ thống] > [Giám sát thiết bị]** của giao diện quản lý web:

- **ONLINE (Bình thường)**: Thiết bị đang giao tiếp bình thường
- **OFFLINE (Ngoại tuyến)**: Mất kết nối (3 lần health check liên tiếp thất bại)
- **ERROR (Lỗi)**: Thiết bị phản hồi nhưng trả về dữ liệu bất thường
- **MAINTENANCE (Bảo trì)**: Người vận hành đặt chế độ bảo trì thủ công

#### Health check thiết bị

Quản trị viên có thể kích hoạt health check thiết bị thủ công qua giao diện web hoặc API.

- Web: Xác nhận thẻ **[Tóm tắt tình trạng sức khỏe]** trên bảng điều khiển giám sát thiết bị và nhấp nút **[Thực hiện health check]**
- API: `POST /api/v1/monitoring/health-check` (Quyền ADMIN)
- Xem tóm tắt: Xác nhận tóm tắt tình trạng sức khỏe toàn bộ thiết bị bằng `GET /api/v1/monitoring/summary`

#### Cảnh báo sự cố thiết bị

Khi thiết bị chuyển sang trạng thái OFFLINE hoặc ERROR, cảnh báo tự động được phát:
- Cảnh báo bảng điều khiển thời gian thực qua WebSocket
- Gửi cảnh báo SMS/KakaoTalk đến liên hệ quản trị viên được chỉ định

### 5.2 Giám sát hiệu năng máy chủ (Prometheus + Grafana)

#### Cấu hình Prometheus

```yaml
# /etc/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['localhost:9093']

rule_files:
  - "alerts/*.yml"

scrape_configs:
  # Spring Boot Actuator
  - job_name: 'weighing-api'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
          - '10.x.x.1:8080'
          - '10.x.x.1:8081'
        labels:
          app: 'weighing-api'

  # Node Exporter (Metrics OS máy chủ)
  - job_name: 'node-exporter'
    static_configs:
      - targets:
          - '10.x.x.1:9100'   # Máy chủ WAS
          - '10.x.x.10:9100'  # Máy chủ DB
          - '10.x.x.20:9100'  # Máy chủ Redis
          - '10.x.x.30:9100'  # Máy chủ Nginx

  # PostgreSQL Exporter
  - job_name: 'postgres-exporter'
    static_configs:
      - targets: ['10.x.x.10:9187']

  # Redis Exporter
  - job_name: 'redis-exporter'
    static_configs:
      - targets: ['10.x.x.20:9121']

  # Nginx Exporter
  - job_name: 'nginx-exporter'
    static_configs:
      - targets: ['10.x.x.30:9113']
```

#### Quy tắc cảnh báo chính (alerts/weighing-alerts.yml)

```yaml
groups:
  - name: weighing-system-alerts
    rules:
      # Thời gian phản hồi API p95 > 1 giây
      - alert: HighAPILatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job="weighing-api"}[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Thời gian phản hồi API p95 vượt quá 1 giây"

      # Tỷ lệ lỗi API > 1%
      - alert: HighAPIErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.01
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Tỷ lệ lỗi 5xx API vượt quá 1%"

      # Sử dụng CPU > 80%
      - alert: HighCPUUsage
        expr: 100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Sử dụng CPU vượt quá 80%: {{ $labels.instance }}"

      # Sử dụng bộ nhớ > 85%
      - alert: HighMemoryUsage
        expr: (1 - node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100 > 85
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Sử dụng bộ nhớ vượt quá 85%: {{ $labels.instance }}"

      # Sử dụng đĩa > 90%
      - alert: HighDiskUsage
        expr: (1 - node_filesystem_avail_bytes{fstype=~"ext4|xfs"} / node_filesystem_size_bytes{fstype=~"ext4|xfs"}) * 100 > 90
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Sử dụng đĩa vượt quá 90%: {{ $labels.instance }}"

      # Kết nối PostgreSQL > 80%
      - alert: HighDBConnections
        expr: pg_stat_activity_count / pg_settings_max_connections * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Tỷ lệ sử dụng kết nối DB vượt quá 80%"

      # Bộ nhớ Redis > 80%
      - alert: HighRedisMemory
        expr: redis_memory_used_bytes / redis_memory_max_bytes * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Tỷ lệ sử dụng bộ nhớ Redis vượt quá 80%"

      # Instance Spring Boot ngừng hoạt động
      - alert: APIServerDown
        expr: up{job="weighing-api"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Instance API Server ngừng hoạt động: {{ $labels.instance }}"
```

#### Cấu hình bảng điều khiển Grafana

Cấu hình và vận hành các bảng điều khiển sau:

| Bảng điều khiển | Bảng chính | Mục đích |
|-----------------|-----------|----------|
| System Overview | CPU, Memory, Disk, Network | Trạng thái hạ tầng máy chủ |
| API Performance | Request Rate, Latency (p50/p95/p99), Error Rate | Hiệu năng API |
| Database | Connections, Query Duration, TPS, Replication Lag | Trạng thái DB |
| Redis | Memory Usage, Connected Clients, Hit Rate, Commands/s | Trạng thái cache |
| Business Metrics | Daily Weighings, LPR Recognition Rate, OTP Usage | Chỉ số nghiệp vụ |

#### Thông tin truy cập Grafana

```
URL: http://10.x.x.50:3000
Tài khoản ban đầu: admin / (Mật khẩu đã cấu hình)
```

### 5.3 Quản lý log (ELK Stack)

#### Kiến trúc log

```
[Spring Boot] --> [Logback JSON] --> [Filebeat] --> [Logstash] --> [Elasticsearch] --> [Kibana]
[Nginx]       --> [Access/Error Log] --> [Filebeat] -+
[PostgreSQL]  --> [PostgreSQL Log]   --> [Filebeat] -+
```

#### Cấu hình Logback (logback-spring.xml)

```xml
<configuration>
  <appender name="JSON_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>/app/logs/weighing-api.json</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>/app/logs/weighing-api.%d{yyyy-MM-dd}.json</fileNamePattern>
      <maxHistory>90</maxHistory>
      <totalSizeCap>10GB</totalSizeCap>
    </rollingPolicy>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <timeZone>Asia/Seoul</timeZone>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="JSON_FILE" />
  </root>
</configuration>
```

#### Cấu hình Filebeat

```yaml
# /etc/filebeat/filebeat.yml
filebeat.inputs:
  - type: log
    paths:
      - /data/weighing/logs/*.json
    json.keys_under_root: true
    json.add_error_key: true
    fields:
      app: weighing-api

  - type: log
    paths:
      - /var/log/nginx/access.log
    fields:
      app: nginx-access

  - type: log
    paths:
      - /var/log/nginx/error.log
    fields:
      app: nginx-error

output.logstash:
  hosts: ["10.x.x.50:5044"]
```

#### Truy vấn log trên Kibana

```
URL: http://10.x.x.50:5601

Mẫu tìm kiếm chính:
- Log lỗi: level:ERROR AND app:weighing-api
- Nhật ký kiểm toán: message:"[AUDIT]*"
- API chậm: response_time:>1000
- Người dùng cụ thể: userId:10
- Liên quan OTP: message:"OTP*"
```

#### Chính sách lưu trữ log

| Loại log | Thời gian lưu | Nơi lưu |
|---------|--------------|---------|
| Log ứng dụng | 90 ngày | ELK + File cục bộ |
| Nhật ký kiểm toán | 1 năm | ELK + DB (tb_audit_log) |
| Log Nginx Access | 90 ngày | ELK + File cục bộ |
| Log PostgreSQL | 30 ngày | File cục bộ |
| Ảnh LPR | 90 ngày | NAS lưu trữ file |

### 5.4 Cấu hình cảnh báo

#### Cấu hình kênh cảnh báo

| Kênh | Đối tượng | Mục đích |
|------|----------|----------|
| Grafana Alert -> SMS | Nhân viên vận hành | Sự cố máy chủ/DB (Critical) |
| Grafana Alert -> KakaoTalk | Nhóm đội vận hành | Cảnh báo giảm hiệu năng (Warning) |
| WebSocket Push | Bảng điều khiển web | Thay đổi trạng thái thiết bị thời gian thực |
| Email | Đội vận hành | Báo cáo vận hành hàng ngày |

#### Cấu hình Alertmanager

```yaml
# /etc/alertmanager/alertmanager.yml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 1h
  receiver: 'default'
  routes:
    - match:
        severity: critical
      receiver: 'critical-sms'
      repeat_interval: 15m
    - match:
        severity: warning
      receiver: 'warning-kakao'
      repeat_interval: 30m

receivers:
  - name: 'default'
    webhook_configs:
      - url: 'http://localhost:8080/api/internal/alerts'

  - name: 'critical-sms'
    webhook_configs:
      - url: 'http://localhost:8080/api/internal/alerts/sms'

  - name: 'warning-kakao'
    webhook_configs:
      - url: 'http://localhost:8080/api/internal/alerts/kakao'
```

---

## 6. Quản lý cơ sở dữ liệu

### 6.1 Cấu trúc và quan hệ bảng

#### Danh sách bảng chính

| Tên bảng | Mô tả | Quan hệ chính |
|----------|-------|---------------|
| tb_user | Người dùng (ADMIN/MANAGER/DRIVER) | tb_company (FK) |
| tb_company | Danh mục công ty vận tải | tb_user, tb_vehicle, tb_dispatch (Cha) |
| tb_vehicle | Danh mục xe (Tiêu chí khớp LPR) | tb_company (FK) |
| tb_dispatch | Bản ghi điều xe | tb_vehicle, tb_company, tb_user (FK) |
| tb_weighing | Kết quả cân | tb_dispatch, tb_vehicle, tb_scale (FK) |
| tb_weighing_slip | Phiếu cân điện tử | tb_weighing (FK) |
| tb_gate_pass | Bản ghi xuất cổng | tb_weighing, tb_dispatch (FK) |
| tb_otp_session | Phiên OTP | tb_user, tb_vehicle (FK) |
| tb_scale | Danh mục trạm cân | tb_weighing (Cha) |
| tb_master_code | Mã chung | Tự tham chiếu (parent_code_id) |
| tb_audit_log | Nhật ký kiểm toán | - |
| tb_notification | Thông báo | tb_user (FK) |
| tb_inquiry_call | Bản ghi cuộc gọi hỏi đáp | tb_user (FK) |

#### Tình trạng chỉ mục

```sql
-- Truy vấn danh sách chỉ mục
SELECT schemaname, tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

Chỉ mục chính:

| Bảng | Chỉ mục | Cột | Loại |
|------|---------|-----|------|
| tb_vehicle | idx_vehicle_plate | plate_number | B-Tree UNIQUE |
| tb_dispatch | idx_dispatch_date_status | dispatch_date, dispatch_status | B-Tree |
| tb_weighing | idx_weighing_dispatch | dispatch_id | B-Tree |
| tb_weighing | idx_weighing_date | weighed_at | B-Tree |
| tb_weighing_slip | idx_slip_number | slip_number | B-Tree UNIQUE |
| tb_user | idx_user_login | login_id | B-Tree UNIQUE |
| tb_otp_session | idx_otp_code_expires | otp_code, expires_at | B-Tree |

### 6.2 Quy trình sao lưu và phục hồi

#### Chính sách sao lưu

| Loại sao lưu | Đối tượng | Phương thức | Chu kỳ | Thời gian lưu |
|-------------|----------|-------------|--------|---------------|
| Sao lưu toàn bộ | PostgreSQL | pg_dump | Hàng ngày 02:00 | 30 ngày |
| WAL Archive | PostgreSQL | Lưu trữ liên tục | Thời gian thực | 7 ngày |
| Snapshot RDB | Redis | RDB dump | Mỗi 6 giờ | 3 ngày |
| Ảnh LPR | NAS | rsync | Hàng ngày 03:00 | 90 ngày |

#### Script sao lưu tự động (pg_backup.sh)

```bash
#!/bin/bash
# /opt/weighing/scripts/pg_backup.sh

BACKUP_DIR="/data/backup/postgresql"
DB_NAME="weighing"
DB_USER="weighing_app"
RETENTION_DAYS=30
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/weighing_${TIMESTAMP}.sql.gz"

# Kiểm tra thư mục sao lưu
mkdir -p ${BACKUP_DIR}

# Thực thi pg_dump (Định dạng Custom, nén)
echo "[$(date)] Bắt đầu sao lưu: ${BACKUP_FILE}"
pg_dump -h ${DB_HOST} -U ${DB_USER} -d ${DB_NAME} \
    --format=custom --compress=9 \
    --file=${BACKUP_FILE}

if [ $? -eq 0 ]; then
    echo "[$(date)] Sao lưu thành công: $(du -sh ${BACKUP_FILE})"
else
    echo "[$(date)] Sao lưu thất bại!" >&2
    # Gửi cảnh báo
    exit 1
fi

# Xóa bản sao lưu cũ
find ${BACKUP_DIR} -name "weighing_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
echo "[$(date)] Hoàn tất dọn dẹp file sao lưu quá ${RETENTION_DAYS} ngày"
```

#### Đăng ký crontab

```bash
# crontab -e
# Sao lưu toàn bộ PostgreSQL hàng ngày lúc 02:00
0 2 * * * /opt/weighing/scripts/pg_backup.sh >> /var/log/weighing/backup.log 2>&1
```

#### Quy trình phục hồi

```bash
# 1. Kiểm tra danh sách file sao lưu
ls -lah /data/backup/postgresql/

# 2. Xác nhận đối tượng phục hồi (Danh sách bảng)
pg_restore --list /data/backup/postgresql/weighing_20260129_020000.sql.gz

# 3. Phục hồi toàn bộ (Vào DB mới)
createdb -h ${DB_HOST} -U postgres weighing_restored
pg_restore -h ${DB_HOST} -U postgres \
    -d weighing_restored \
    --clean --if-exists \
    /data/backup/postgresql/weighing_20260129_020000.sql.gz

# 4. Phục hồi chỉ bảng cụ thể
pg_restore -h ${DB_HOST} -U postgres \
    -d weighing \
    --table=tb_weighing \
    --data-only \
    /data/backup/postgresql/weighing_20260129_020000.sql.gz
```

### 6.3 Di chuyển dữ liệu

Quy trình di chuyển dữ liệu từ hệ thống cũ (legacy).

#### Nguyên tắc di chuyển

1. **Di chuyển từng bước**: Thực hiện theo thứ tự dữ liệu danh mục (Công ty vận tải, Xe) -> Dữ liệu lịch sử (Điều xe, Cân).
2. **Vận hành song song**: Vận hành song song với hệ thống cũ trong thời gian di chuyển.
3. **Giai đoạn xác minh**: Thực hiện xác minh tính nhất quán dữ liệu sau mỗi bước hoàn thành.
4. **Kế hoạch rollback**: Chuẩn bị trước quy trình rollback cho từng bước.

#### Truy vấn xác minh di chuyển

```sql
-- So sánh số lượng bản ghi
SELECT 'tb_company' AS table_name, COUNT(*) AS cnt FROM tb_company
UNION ALL SELECT 'tb_vehicle', COUNT(*) FROM tb_vehicle
UNION ALL SELECT 'tb_user', COUNT(*) FROM tb_user
UNION ALL SELECT 'tb_dispatch', COUNT(*) FROM tb_dispatch
UNION ALL SELECT 'tb_weighing', COUNT(*) FROM tb_weighing;

-- Kiểm tra trùng lặp biển số xe
SELECT plate_number, COUNT(*) AS cnt
FROM tb_vehicle
GROUP BY plate_number
HAVING COUNT(*) > 1;

-- Kiểm tra tính nhất quán dữ liệu điều xe - cân
SELECT d.dispatch_id, d.dispatch_status,
       COUNT(w.weighing_id) AS weighing_count
FROM tb_dispatch d
LEFT JOIN tb_weighing w ON d.dispatch_id = w.dispatch_id
WHERE d.dispatch_status = 'COMPLETED'
GROUP BY d.dispatch_id, d.dispatch_status
HAVING COUNT(w.weighing_id) = 0;
```

### 6.4 Tinh chỉnh hiệu năng

#### Giám sát truy vấn chậm

```sql
-- Kiểm tra truy vấn có thời gian thực thi trên 3 giây (Tiện ích mở rộng pg_stat_statements)
SELECT query, calls, total_exec_time / calls AS avg_time_ms,
       rows / calls AS avg_rows
FROM pg_stat_statements
WHERE total_exec_time / calls > 3000
ORDER BY total_exec_time DESC
LIMIT 20;

-- Kiểm tra truy vấn đang chạy
SELECT pid, now() - pg_stat_activity.query_start AS duration,
       query, state
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '5 seconds'
  AND state != 'idle';
```

#### Cập nhật thống kê bảng

```sql
-- Cập nhật thống kê toàn bộ bảng (Khuyến nghị mỗi tuần 1 lần)
ANALYZE VERBOSE;

-- Cập nhật thống kê bảng cụ thể
ANALYZE tb_weighing;
ANALYZE tb_dispatch;
```

#### Quản lý VACUUM

```sql
-- Kiểm tra trạng thái tự động VACUUM
SELECT schemaname, relname, n_dead_tup, last_autovacuum, last_autoanalyze
FROM pg_stat_user_tables
WHERE n_dead_tup > 1000
ORDER BY n_dead_tup DESC;

-- VACUUM thủ công (khi cần)
VACUUM (VERBOSE, ANALYZE) tb_weighing;
```

#### Giám sát Connection Pool

```sql
-- Kiểm tra trạng thái kết nối hiện tại
SELECT state, COUNT(*)
FROM pg_stat_activity
GROUP BY state;

-- Kiểm tra số kết nối tối đa
SHOW max_connections;

-- Kiểm tra kết nối nhàn rỗi
SELECT pid, usename, client_addr, state, query_start
FROM pg_stat_activity
WHERE state = 'idle'
  AND query_start < now() - interval '30 minutes';
```

---

## 7. Quản lý dữ liệu danh mục

### 7.1 Quản lý công ty vận tải

Thông tin công ty vận tải (tb_company) là dữ liệu danh mục làm cơ sở cho điều xe, xe và người dùng.

#### Đăng ký công ty vận tải

Đăng ký thông tin sau trên giao diện quản lý web **[Quản lý dữ liệu danh mục] > [Quản lý công ty vận tải]**:

| Mục | Bắt buộc | Mô tả |
|-----|:--------:|-------|
| Tên công ty vận tải | O | Tên chính thức doanh nghiệp |
| Mã số đăng ký kinh doanh | O | 10 chữ số |
| Loại công ty vận tải | O | Phụ phẩm/Chất thải/Phụ liệu/Xuất hàng/Chung |
| Tên đại diện | O | Họ tên người đại diện |
| Số liên hệ | O | Số điện thoại đại diện |
| Địa chỉ | X | Địa chỉ doanh nghiệp |
| Trạng thái hoạt động | O | Mặc định: Hoạt động |

#### Vô hiệu hóa công ty vận tải

Công ty vận tải đã ngừng giao dịch sẽ được xử lý vô hiệu hóa thay vì xóa. Xe và tài xế thuộc công ty vận tải bị vô hiệu hóa sẽ bị loại khỏi danh sách chọn khi đăng ký điều xe.

### 7.2 Quản lý xe (Khớp LPR)

Thông tin xe (tb_vehicle) là dữ liệu danh mục cốt lõi cho việc khớp tự động LPR.

#### Đăng ký xe

| Mục | Bắt buộc | Mô tả |
|-----|:--------:|-------|
| Biển số xe | O | Tiêu chí khớp LPR. **Duy nhất trong toàn hệ thống (UNIQUE)** |
| Công ty vận tải | O | FK: tb_company |
| Loại xe | O | Xe tải ben/Xe tải thùng/Xe bồn, v.v. |
| Tải trọng tối đa (kg) | X | Tiêu chí cảnh báo quá tải |
| Trạng thái hoạt động | O | Mặc định: Hoạt động |

> **Quan trọng**: Biển số xe (plate_number) là khóa duy nhất để khớp với kết quả nhận dạng từ camera LPR. Khi biển số xe thay đổi (thay biển số, v.v.), bắt buộc phải cập nhật ngay trên hệ thống. Nếu không, việc cân tự động LPR sẽ thất bại.

#### Quản lý định dạng biển số xe

```
Ví dụ định dạng chuẩn:
  - 12가3456    (Xe thông thường)
  - 123가4567   (Hệ thống mới)
  - 부산12가3456 (Bao gồm khu vực)
```

Biển số nhận dạng từ camera LPR và biển số lưu trong DB phải cùng định dạng để khớp thành công. Khi đăng ký, loại bỏ khoảng trắng, dấu gạch ngang và các ký tự không cần thiết, lưu theo định dạng chuẩn hóa.

#### Đăng ký xe hàng loạt

Khi cần đăng ký số lượng lớn xe, sử dụng chức năng tải lên Excel:
1. Nhấp **[Tải mẫu]** trên màn hình **[Quản lý xe]** để nhận biểu mẫu.
2. Nhập dữ liệu theo biểu mẫu.
3. Tải file lên bằng nút **[Tải lên hàng loạt]**.
4. Xác nhận kết quả xác minh và sửa các mục lỗi.
5. Xác nhận và thực hiện đăng ký hàng loạt.

### 7.3 Quản lý trạm cân

Trạm cân (tb_scale) là thông tin danh mục của thiết bị cân vật lý.

| Mục | Bắt buộc | Mô tả |
|-----|:--------:|-------|
| Tên trạm cân | O | Ví dụ: "Trạm cân số 1", "Trạm cân phụ phẩm" |
| Vị trí lắp đặt | O | Thông tin vị trí trong nhà máy |
| Loại trạm cân | O | SMART (Tự động) / MANUAL (Thủ công) |
| Dung lượng tối đa (kg) | X | Khối lượng tối đa có thể cân |
| Trạng thái hoạt động | O | Trạng thái vận hành |
| Thời điểm hiệu chuẩn cuối | X | Ngày hiệu chuẩn gần nhất |

#### Quản lý hiệu chuẩn trạm cân

- Trạm cân phải được hiệu chuẩn định kỳ (Tuân thủ chu kỳ hiệu chuẩn theo quy định).
- Quản lý lịch sử hiệu chuẩn qua trường `last_calibrated_at`.
- Cấu hình gửi cảnh báo khi gần đến ngày hết hạn hiệu chuẩn.

### 7.4 Quản lý mã chung

Mã chung (tb_master_code) quản lý tập trung các giá trị mã được sử dụng trong toàn hệ thống.

#### Các nhóm mã chính

| Nhóm mã | Mô tả | Ví dụ giá trị mã |
|---------|-------|------------------|
| ITEM_TYPE | Loại hàng hóa | Phụ phẩm, Chất thải, Phụ liệu, Xuất hàng, Chung |
| VEHICLE_TYPE | Loại xe | Xe tải ben, Xe tải thùng, Xe bồn |
| DISPATCH_STATUS | Trạng thái điều xe | REGISTERED, IN_PROGRESS, COMPLETED, CANCELLED |
| WEIGHING_TYPE | Lần cân | FIRST, SECOND, THIRD |
| WEIGHING_MODE | Phương thức cân | LPR_AUTO, MOBILE_OTP, MANUAL, RE_WEIGH |
| WEIGHING_STATUS | Trạng thái cân | IN_PROGRESS, COMPLETED, RE_WEIGHING, ERROR |
| INQUIRY_TYPE | Loại hỏi đáp | Phòng điều phối vật lưu, Kho vật liệu, Khác |

#### Thêm/sửa mã

1. Truy cập màn hình **[Quản lý dữ liệu danh mục] > [Quản lý mã chung]**.
2. Chọn nhóm mã hoặc tạo nhóm mới.
3. Nhập giá trị mã, tên mã, thứ tự sắp xếp.
4. Sau khi lưu, thay đổi được phản ánh ngay trên các màn hình liên quan (Cập nhật cache Redis).

> **Chú ý**: Không thể xóa giá trị mã đang được tham chiếu bởi dữ liệu hiện có. Chỉ có thể vô hiệu hóa.

### 7.5 Quản lý cài đặt hệ thống

Quản lý cài đặt hệ thống (`/admin/settings`) cho phép điều chỉnh các tham số vận hành chung của hệ thống. Chỉ vai trò ADMIN được truy cập.

#### Màn hình cài đặt hệ thống

1. Chọn **[Quản lý hệ thống] > [Cài đặt hệ thống]** từ menu bên trái.
2. Các mục cài đặt được hiển thị theo danh mục.
3. Thay đổi giá trị cài đặt và nhấp nút **[Lưu]**.
4. Có thể sử dụng chức năng sửa từng mục hoặc **sửa hàng loạt**.

> **Chú ý**: Thay đổi cài đặt hệ thống được áp dụng ngay lập tức. Ghi lại giá trị hiện tại trước khi thay đổi, và xác nhận hoạt động bình thường sau khi thay đổi. Lịch sử thay đổi cài đặt được tự động ghi vào nhật ký kiểm toán.

### 7.6 Quản lý thông báo

Quản trị viên có thể đăng ký, sửa đổi, xóa thông báo trên hệ thống web.

#### Đăng ký thông báo

1. Nhấp **[Thông báo]** từ menu bên trái, sau đó nhấp nút **[Đăng ký mới]**.
2. Nhập các mục sau:

   | Mục nhập | Bắt buộc | Mô tả |
   |----------|:--------:|-------|
   | Tiêu đề | O | Tiêu đề thông báo |
   | Danh mục | O | Phân loại thông báo (Hệ thống, Vận hành, Bảo trì, Khác, v.v.) |
   | Nội dung | O | Nội dung thông báo (Hỗ trợ markdown) |
   | File đính kèm | X | Đính kèm file liên quan |

3. Nhấp nút **[Lưu]**. Sau khi lưu, mặc định ở trạng thái **chưa đăng**.

#### Đăng/Hủy đăng thông báo

- Nhấp nút **[Đăng]** của thông báo mục tiêu trong danh sách để công khai cho người dùng.
- Để chuyển thông báo đã đăng thành không công khai, nhấp nút **[Hủy đăng]**.

#### Ghim/Bỏ ghim thông báo

- Thông báo quan trọng có thể được ghim lên đầu danh sách bằng cách nhấp nút **[Ghim(📌)]**.
- Để bỏ ghim, nhấp nút **[Bỏ ghim]**.

### 7.7 Quản lý FAQ

Quản trị viên có thể đăng ký và quản lý các câu hỏi thường gặp (FAQ).

#### Đăng ký FAQ

1. Nhấp **[Trợ giúp]** từ menu bên trái, chọn tab **[Quản lý FAQ]**.
2. Nhấp nút **[Đăng ký mới]**.
3. Nhập các mục sau:

   | Mục nhập | Bắt buộc | Mô tả |
   |----------|:--------:|-------|
   | Danh mục | O | Phân loại câu hỏi (Đăng nhập, Điều xe, Cân, Xuất cổng, Khác, v.v.) |
   | Câu hỏi | O | Nội dung câu hỏi thường gặp |
   | Trả lời | O | Câu trả lời cho câu hỏi |

4. Nhấp nút **[Lưu]**.

#### Sửa/Xóa FAQ

- Nhấp nút **[Sửa]** hoặc **[Xóa]** của mục FAQ mục tiêu trong danh sách.
- FAQ đã xóa sẽ bị loại khỏi màn hình người dùng ngay lập tức.

> **Lưu ý**: FAQ không có cài đặt trạng thái đăng, được hiển thị cho người dùng ngay khi đăng ký.

---

## 8. Quản lý thiết bị

### 8.1 Quản lý Camera LPR

#### Thông tin cấu hình Camera LPR

| Mục | Cấu hình |
|-----|----------|
| Giao thức giao tiếp | TCP |
| Phương thức liên kết | Giao tiếp trực tiếp từ chương trình CS trạm cân |
| Kích hoạt | Lệnh chụp khi cảm biến LiDAR/Radar phát hiện xe |
| Dữ liệu đầu ra | Ảnh chụp + Biển số xe nhận dạng lần 1 |
| Lưu trữ ảnh | Đường dẫn NAS (Lưu 90 ngày) |

#### Giám sát tỷ lệ nhận dạng LPR

```sql
-- Truy vấn tỷ lệ nhận dạng LPR theo ngày
SELECT DATE(created_at) AS dt,
       COUNT(*) AS total,
       COUNT(CASE WHEN ai_confidence >= 0.90 THEN 1 END) AS auto_confirmed,
       COUNT(CASE WHEN ai_confidence < 0.90 AND ai_confidence >= 0.70 THEN 1 END) AS low_confidence,
       COUNT(CASE WHEN ai_confidence < 0.70 OR ai_confidence IS NULL THEN 1 END) AS failed,
       ROUND(COUNT(CASE WHEN ai_confidence >= 0.90 THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 2) AS auto_rate_pct
FROM tb_weighing
WHERE weighing_mode = 'LPR_AUTO'
  AND created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY dt DESC;
```

Mục tiêu tỷ lệ nhận dạng: Trên 95%. Khi tỷ lệ nhận dạng giảm xuống dưới 90%, kiểm tra:
- Tình trạng bẩn ống kính camera LPR
- Hoạt động bình thường của đèn hỗ trợ (Ban đêm)
- Góc và tiêu điểm camera
- Thời gian phản hồi AI Engine

### 8.2 Quản lý cảm biến LiDAR

| Mục | Cấu hình |
|-----|----------|
| Giao thức giao tiếp | TCP/UDP |
| Vai trò | Phát hiện xe vào -> Kích hoạt chụp LPR |
| Phạm vi phát hiện | Đường vào trạm cân (Cấu hình theo vị trí lắp đặt) |
| Chống phát hiện trùng | Khoảng cách tối thiểu 10 giây |

#### Mục kiểm tra cảm biến

- Tình trạng sạch bề mặt cảm biến (Bụi, vật lạ)
- Kiểm tra vật cản trong vùng phát hiện
- Trạng thái kết nối nguồn điện và cáp
- Xác nhận nhận dữ liệu giao tiếp bình thường

### 8.3 Quản lý Indicator (RS-232C)

Indicator là thiết bị hiển thị và truyền giá trị khối lượng đo được từ trạm cân đến chương trình CS trạm cân.

#### Cấu hình giao tiếp

| Mục | Giá trị |
|-----|---------|
| Cổng | Cổng COM (Ví dụ: COM1, COM3) |
| Baud Rate | 9600 (Thay đổi tùy thiết bị) |
| Data Bits | 8 |
| Parity | None |
| Stop Bits | 1 |
| Flow Control | None |
| Timeout | 3 giây |
| Thử lại | 3 lần |

#### Xử lý sự cố Indicator

| Triệu chứng | Nguyên nhân | Xử lý |
|-------------|-----------|-------|
| Không nhận được giá trị khối lượng | Đứt cáp RS-232C | Thay cáp hoặc kiểm tra kết nối |
| Nhận giá trị không ổn định | Tiếp xúc kém | Kết nối lại connector, Kiểm tra bộ chuyển đổi serial |
| Timeout giao tiếp | Xung đột cổng COM | Kiểm tra cổng trong Device Manager |
| Dữ liệu bất thường | Cấu hình giao tiếp không khớp | Kiểm tra tham số như Baud Rate |

```
Quy trình xử lý sự cố giao tiếp RS-232C:
1. Kiểm tra trạng thái Indicator trên chương trình CS trạm cân
2. Kiểm tra trạng thái kết nối cổng COM (Device Manager)
3. Kiểm tra kết nối vật lý cáp serial
4. Kiểm tra khớp tham số giao tiếp (Baud Rate)
5. Kiểm tra trạng thái driver khi dùng bộ chuyển đổi Serial-USB
6. Thử thay bằng bộ chuyển đổi dự phòng
7. Nếu không giải quyết được, yêu cầu hỗ trợ từ nhà sản xuất Indicator
```

### 8.4 Quản lý bảng điện tử

Bảng điện tử hiển thị thông tin hướng dẫn như mã OTP cho xe vào trạm cân.

| Mục | Cấu hình |
|-----|----------|
| Giao tiếp | TCP hoặc RS-485 |
| Nội dung hiển thị | OTP 6 chữ số, Biển số xe, Thông báo hướng dẫn |
| Điều khiển | Gửi từ chương trình CS trạm cân |

#### Mục kiểm tra bảng điện tử

- Độ sáng LED và khả năng đọc (Ban ngày/Ban đêm)
- Trạng thái kết nối giao tiếp
- Hiển thị mã OTP bình thường
- Hiển thị tiếng Hàn bình thường

### 8.5 Quản lý barrier tự động

Barrier tự động kiểm soát vật lý việc ra vào trạm cân.

| Mục | Cấu hình |
|-----|----------|
| Giao tiếp | TCP hoặc RS-485 |
| Lệnh điều khiển | Mở (OPEN) / Đóng (CLOSE) |
| Thiết bị an toàn | Ngăn đóng khi phát hiện xe (Cảm biến an toàn) |
| Fallback | Có thể mở thủ công khi sự cố giao tiếp |

#### Vận hành barrier khẩn cấp

Khi không thể điều khiển tự động do sự cố giao tiếp:
1. Sử dụng công tắc thủ công trên tủ điều khiển barrier để mở.
2. Chuyển sang chế độ thủ công, giữ barrier ở trạng thái mở cố định.
3. Xác định và giải quyết nguyên nhân sự cố, sau đó trở lại chế độ tự động.
4. Ghi lại lịch sử vận hành khẩn cấp.

---

## 9. Xử lý sự cố

### 9.1 Quy trình xử lý theo loại sự cố

#### Phân loại mức độ sự cố

| Mức độ | Định nghĩa | Thời gian xử lý | Ví dụ |
|--------|-----------|-----------------|-------|
| Critical (Khẩn cấp) | Ngừng toàn bộ dịch vụ | Ngay lập tức (Bắt đầu phục hồi trong 30 phút) | Toàn bộ WAS ngừng, Sự cố DB, Mất kết nối mạng |
| Major (Nghiêm trọng) | Sự cố chức năng chính | Xử lý trong 1 giờ | 1 WAS ngừng, Sự cố LPR, Sự cố Redis |
| Minor (Nhẹ) | Giảm chức năng một phần | Xử lý trong 4 giờ | Giảm hiệu năng, Sự cố cảm biến đơn lẻ, Lỗi gửi cảnh báo |
| Info (Thông tin) | Không ảnh hưởng dịch vụ | Giờ làm việc tiếp theo | Cảnh báo log, Tăng sử dụng đĩa |

#### Sự cố máy chủ WAS

```
Phát hiện sự cố: Health check Nginx thất bại (max_fails=3, fail_timeout=30s)
Xử lý tự động: Nginx tự động loại máy chủ lỗi khỏi upstream (Active-Active)
RTO: < 30 giây (Failover tự động)

Quy trình phục hồi thủ công:
1. Kiểm tra trạng thái máy chủ lỗi
   $ docker ps -a | grep weighing-api
   $ docker logs weighing-api-1 --tail 100

2. Khởi động lại container
   $ docker compose restart weighing-api-1

3. Kiểm tra health check
   $ curl -s http://localhost:8080/actuator/health | python3 -m json.tool

4. Phân tích nguyên nhân
   - Kiểm tra log ứng dụng: /data/weighing/logs/
   - Kiểm tra bộ nhớ JVM: /actuator/metrics/jvm.memory.used
   - Kiểm tra OOM Killer: dmesg | grep -i oom

5. Xác nhận máy chủ trở lại trên Nginx
   $ curl -s http://localhost/api/v1/health
```

#### Sự cố cơ sở dữ liệu

```
Phát hiện sự cố: Spring Boot kết nối thất bại, Prometheus pg_up == 0

Quy trình nâng cấp Standby khi PostgreSQL Primary gặp sự cố:
RTO: < 30 phút (Nâng cấp thủ công)

1. Kiểm tra trạng thái Primary
   $ pg_isready -h 10.x.x.10 -p 5432
   $ sudo systemctl status postgresql-16

2. Thử phục hồi Primary
   $ sudo systemctl restart postgresql-16
   $ tail -100 /var/lib/pgsql/16/data/log/postgresql-*.log

3. Khi không thể phục hồi Primary, nâng cấp Standby
   -- Thực thi trên máy chủ Standby:
   $ pg_ctl promote -D /var/lib/pgsql/16/data

4. Thay đổi thông tin kết nối DB ứng dụng
   -- Thay đổi DB_HOST thành IP Standby trong file .env
   $ vi /opt/weighing/.env
   DB_HOST=10.x.x.11    # Thay đổi sang IP Standby

5. Khởi động lại ứng dụng
   $ docker compose restart weighing-api-1 weighing-api-2

6. Kiểm tra tính nhất quán dữ liệu
   $ psql -h 10.x.x.11 -U weighing_app -d weighing -c "SELECT COUNT(*) FROM tb_weighing;"

7. Sau khi phục hồi Primary ban đầu, tái cấu hình thành Standby
```

#### Sự cố Redis

```
Phát hiện sự cố: Spring Boot kết nối Redis thất bại, redis_up == 0

Fallback tự động:
- Xác minh Refresh Token: Chỉ thực hiện xác minh JWT (Vô hiệu hóa blacklist)
- Phiên OTP: Truy vấn trực tiếp DB (tb_otp_session)
RTO: < 10 phút

Quy trình phục hồi thủ công:
1. Kiểm tra trạng thái Redis
   $ redis-cli -h 10.x.x.20 ping
   $ sudo systemctl status redis

2. Khởi động lại Redis
   $ sudo systemctl restart redis

3. Kiểm tra kết nối
   $ redis-cli -h 10.x.x.20 -a '<mật_khẩu>' info server

4. Làm ấm cache (Khi cần)
   - Cache dữ liệu danh mục tự động tải lại khi gọi API
   - Phiên hoạt động cần người dùng đăng nhập lại
```

#### Sự cố thiết bị LPR

```
Phát hiện sự cố: Chương trình CS phát hiện mất kết nối LPR

Xử lý ngay:
1. Hiển thị cảnh báo trên chương trình CS trạm cân
2. Tự động hướng dẫn chế độ cân thủ công
3. Hiển thị "Chế độ cân thủ công" trên bảng điện tử

Quy trình phục hồi:
1. Kiểm tra nguồn điện và mạng camera LPR
2. Kiểm tra trạng thái camera trên phần mềm quản lý LPR
3. Kiểm tra kết nối TCP
4. Khởi động lại camera (Tắt/Bật nguồn)
5. Thử kết nối lại LPR trên chương trình CS
6. Nếu phục hồi thất bại, yêu cầu hỗ trợ kỹ thuật nhà sản xuất

Vận hành thay thế:
- Chuyển sang cân OTP di động
- Sử dụng song song chế độ cân thủ công (Màn hình cảm ứng)
```

#### Sự cố mạng (Giữa trạm cân và máy chủ)

```
Phát hiện sự cố: Chương trình CS giao tiếp API Server thất bại

Xử lý tự động:
1. Chương trình CS trạm cân chuyển sang chế độ cache cục bộ
2. Lưu dữ liệu cân tạm thời vào SQLite cục bộ
3. Tự động đồng bộ sau khi phục hồi mạng
RTO: < 1 giờ (Đồng bộ tự động)

Quy trình phục hồi thủ công:
1. Kiểm tra trạng thái kết nối mạng
   $ ping 10.x.x.1 (Máy chủ WAS)
   $ traceroute 10.x.x.1

2. Kiểm tra trạng thái kết nối VPN (Nếu có)

3. Kiểm tra thiết bị mạng (Switch, Router)

4. Kiểm tra trạng thái đồng bộ dữ liệu sau khi phục hồi mạng
   - Kiểm tra số lượng chờ đồng bộ trên chương trình CS
   - Kiểm tra tính nhất quán dữ liệu máy chủ sau khi đồng bộ hoàn tất
```

### 9.2 Quy trình phục hồi hệ thống

#### Thứ tự phục hồi toàn bộ hệ thống

Khi toàn bộ hệ thống gặp sự cố, phục hồi theo thứ tự sau:

```
Giai đoạn 1: Phục hồi hạ tầng
  1. Kiểm tra và phục hồi thiết bị mạng
  2. Kiểm tra phần cứng máy chủ

Giai đoạn 2: Phục hồi tầng dữ liệu
  3. Khởi động PostgreSQL và kiểm tra tính nhất quán dữ liệu
  4. Khởi động Redis và kiểm tra kết nối

Giai đoạn 3: Phục hồi ứng dụng
  5. Khởi động Spring Boot WAS #1
  6. Kiểm tra health check WAS #1
  7. Khởi động Spring Boot WAS #2
  8. Kiểm tra health check WAS #2

Giai đoạn 4: Phục hồi frontend và proxy
  9. Khởi động Nginx và kiểm tra cấu hình
  10. Kiểm tra truy cập giao diện web

Giai đoạn 5: Phục hồi thiết bị hiện trường
  11. Khởi động chương trình CS trạm cân
  12. Kiểm tra giao tiếp LPR/Cảm biến/Indicator
  13. Kiểm tra hoạt động Bảng điện tử/Barrier

Giai đoạn 6: Xác minh
  14. Thực hiện kiểm thử cân E2E
  15. Kiểm tra đồng bộ dữ liệu cache cục bộ
  16. Tắt cảnh báo hệ thống giám sát
```

### 9.3 Quy trình Rollback

#### Rollback ứng dụng

```bash
# Kiểm tra phiên bản hiện tại
docker images | grep weighing-api

# Rollback về phiên bản trước (Hoàn tất trong 5 phút)
export ROLLBACK_VERSION="tag_phiên_bản_trước"

# Rollback đồng thời hai WAS
docker compose stop weighing-api-1 weighing-api-2
APP_VERSION=${ROLLBACK_VERSION} docker compose up -d weighing-api-1 weighing-api-2

# Kiểm tra health check
sleep 30
curl -sf http://localhost:8080/actuator/health && echo "WAS#1 OK" || echo "WAS#1 FAIL"
curl -sf http://localhost:8081/actuator/health && echo "WAS#2 OK" || echo "WAS#2 FAIL"
```

#### Rollback cơ sở dữ liệu

Rollback triển khai bao gồm thay đổi schema DB theo quy trình sau:

1. Rollback ứng dụng trước (Triển khai image phiên bản trước).
2. Thực thi SQL rollback thay đổi DB (Script rollback chuẩn bị trước).
3. Kiểm tra tính nhất quán dữ liệu.

> **Chú ý**: Vì `ddl-auto: none` (Môi trường vận hành), thay đổi schema DB chỉ được thực hiện qua migration thủ công. Luôn chuẩn bị SQL rollback trước.

### 9.4 Danh sách liên hệ khẩn cấp

| Phân loại | Phụ trách | Liên hệ | Phạm vi xử lý |
|----------|----------|---------|---------------|
| Vận hành hệ thống | Quản trị viên hệ thống (Lần 1) | Nội bộ XXXX | Máy chủ, DB, Mạng |
| Vận hành hệ thống | Quản trị viên hệ thống (Lần 2) | Nội bộ XXXX | Máy chủ, DB, Mạng |
| Ứng dụng | Đội phát triển | Nội bộ XXXX | Sự cố phần mềm, Triển khai |
| Thiết bị hiện trường | Đội DMES | Nội bộ XXXX | LPR, Cảm biến, Indicator |
| Mạng | Đội hạ tầng | Nội bộ XXXX | Thiết bị mạng, VPN |
| Chuyên gia DB | DBA | Nội bộ XXXX | Phục hồi DB, Vấn đề hiệu năng |
| Nhà sản xuất LPR | Đơn vị bên ngoài | Liên hệ XXXX | Sự cố phần cứng LPR |
| Nhà sản xuất Indicator | Đơn vị bên ngoài | Liên hệ XXXX | Phần cứng thiết bị cân |

> **Lưu ý**: Danh sách liên hệ khẩn cấp phải được xác minh tính hợp lệ và cập nhật tối thiểu mỗi quý 1 lần.

---

## 10. Sao lưu và Phục hồi

### 10.1 Chính sách sao lưu

| Đối tượng | Phương thức | Chu kỳ | Thời gian lưu | Nơi lưu |
|----------|-------------|--------|---------------|---------|
| PostgreSQL toàn bộ | pg_dump (Custom) | Hàng ngày 02:00 | 30 ngày | /data/backup/postgresql/ |
| PostgreSQL WAL | WAL Archive | Thời gian thực | 7 ngày | /data/pg_archive/ |
| Redis RDB | RDB Snapshot | Mỗi 6 giờ | 3 ngày | /var/lib/redis/ |
| Ảnh LPR | NAS rsync | Hàng ngày 03:00 | 90 ngày | NAS lưu trữ |
| Log ứng dụng | Thu thập ELK | Thời gian thực | 90 ngày | Elasticsearch |
| Cấu hình Nginx | Quản lý Git | Khi thay đổi | Vô thời hạn | GitLab |
| Docker Compose | Quản lý Git | Khi thay đổi | Vô thời hạn | GitLab |
| .env (Mã hóa) | Sao lưu thủ công | Khi thay đổi | Vô thời hạn | Két bảo mật riêng |

### 10.2 Cấu hình sao lưu tự động

#### Sao lưu tự động PostgreSQL (crontab)

```bash
# crontab -l (Tài khoản weighing-admin)

# Sao lưu toàn bộ PostgreSQL (Hàng ngày 02:00)
0 2 * * * /opt/weighing/scripts/pg_backup.sh >> /var/log/weighing/backup.log 2>&1

# Sao chép sao lưu Redis RDB (Mỗi 6 giờ)
0 */6 * * * cp /var/lib/redis/dump.rdb /data/backup/redis/dump_$(date +\%Y\%m\%d_\%H\%M).rdb 2>&1

# Đồng bộ ảnh LPR lên NAS (Hàng ngày 03:00)
0 3 * * * rsync -avz --delete /data/weighing/lpr-images/ /mnt/nas/weighing/lpr-images/ >> /var/log/weighing/lpr-sync.log 2>&1

# Dọn dẹp bản sao lưu cũ (Chủ nhật hàng tuần 04:00)
0 4 * * 0 /opt/weighing/scripts/cleanup_old_backups.sh >> /var/log/weighing/cleanup.log 2>&1
```

#### Script xác minh sao lưu

```bash
#!/bin/bash
# /opt/weighing/scripts/verify_backup.sh
# Thực hiện kiểm tra phục hồi sao lưu mỗi tuần 1 lần

LATEST_BACKUP=$(ls -t /data/backup/postgresql/weighing_*.sql.gz | head -1)
TEST_DB="weighing_backup_test"

echo "[$(date)] Bắt đầu xác minh sao lưu: ${LATEST_BACKUP}"

# Tạo DB kiểm thử và phục hồi
dropdb -h localhost -U postgres --if-exists ${TEST_DB}
createdb -h localhost -U postgres ${TEST_DB}
pg_restore -h localhost -U postgres -d ${TEST_DB} ${LATEST_BACKUP}

if [ $? -eq 0 ]; then
    # Kiểm tra số bản ghi các bảng chính
    psql -h localhost -U postgres -d ${TEST_DB} -c "
        SELECT 'tb_user' AS tbl, COUNT(*) AS cnt FROM tb_user
        UNION ALL SELECT 'tb_dispatch', COUNT(*) FROM tb_dispatch
        UNION ALL SELECT 'tb_weighing', COUNT(*) FROM tb_weighing
        UNION ALL SELECT 'tb_vehicle', COUNT(*) FROM tb_vehicle;
    "
    echo "[$(date)] Xác minh sao lưu thành công"
else
    echo "[$(date)] Xác minh sao lưu thất bại!" >&2
fi

# Xóa DB kiểm thử
dropdb -h localhost -U postgres ${TEST_DB}
```

### 10.3 Quy trình phục hồi

#### PITR (Point-in-Time Recovery)

Có thể phục hồi đến thời điểm cụ thể bằng WAL Archive.

```bash
# 1. Dừng PostgreSQL
sudo systemctl stop postgresql-16

# 2. Sao lưu thư mục dữ liệu
sudo mv /var/lib/pgsql/16/data /var/lib/pgsql/16/data_backup

# 3. Phục hồi từ bản sao lưu cơ bản
sudo -u postgres pg_basebackup -D /var/lib/pgsql/16/data -R

# 4. Thiết lập thời điểm phục hồi mục tiêu trong recovery.conf (hoặc postgresql.auto.conf)
echo "recovery_target_time = '2026-01-29 14:00:00+09'" >> /var/lib/pgsql/16/data/postgresql.auto.conf
echo "restore_command = 'cp /data/pg_archive/%f %p'" >> /var/lib/pgsql/16/data/postgresql.auto.conf

# 5. Khởi động PostgreSQL (Chế độ phục hồi)
sudo systemctl start postgresql-16

# 6. Xác nhận timeline sau khi phục hồi hoàn tất
sudo -u postgres psql -c "SELECT pg_is_in_recovery();"
# Kết quả: f (false) -> Phục hồi hoàn tất, Đã chuyển sang Primary
```

### 10.4 Kế hoạch phục hồi thảm họa (DR)

#### Mục tiêu RPO / RTO

| Chỉ số | Giá trị mục tiêu | Phương pháp đạt được |
|--------|-----------------|---------------------|
| RPO (Recovery Point Objective) | Trong vòng 1 giờ | WAL Archive thời gian thực |
| RTO (Recovery Time Objective) | Trong vòng 1 giờ | Standby DB + Docker Image |

#### Kịch bản DR

| Kịch bản | Xử lý | RTO |
|---------|-------|-----|
| Sự cố một WAS | Nginx tự động failover | < 30 giây |
| Sự cố toàn bộ WAS | Triển khai lại bằng Docker Image | < 10 phút |
| Sự cố DB Primary | Nâng cấp Standby thủ công | < 30 phút |
| Sự cố Redis | Khởi động lại Redis + DB Fallback | < 10 phút |
| Cháy/Ngập phòng máy | Xây dựng máy chủ mới từ bản sao lưu ngoại vi | < 24 giờ |
| Sự cố toàn bộ hệ thống | Chuyển sang vận hành thủ công (Song song phương thức cũ) | Ngay lập tức |

#### Diễn tập DR

- **Chu kỳ**: 2 lần/năm
- **Phạm vi**: Nâng cấp DB Standby, Phục hồi sao lưu, Chuyển đổi vận hành thủ công
- **Ghi nhận**: Tài liệu hóa kết quả diễn tập và phản ánh các cải thiện

---

## 11. Quản lý hiệu năng

### 11.1 Chỉ số và mục tiêu hiệu năng

| Chỉ số | Giá trị mục tiêu | Phương pháp đo |
|--------|-----------------|---------------|
| Nhận dạng LPR -> Hoàn tất cân tự động | < 3 giây (E2E) | Timestamp log ứng dụng |
| Thời gian phản hồi API p50 | < 200ms | Prometheus + Spring Actuator |
| Thời gian phản hồi API p95 | < 500ms | Prometheus + Spring Actuator |
| Thời gian phản hồi API p99 | < 1.000ms | Prometheus + Spring Actuator |
| Web FCP (First Contentful Paint) | < 2 giây | Lighthouse |
| Web LCP (Largest Contentful Paint) | < 3 giây | Lighthouse |
| Người truy cập web đồng thời | Trên 50 người | Kiểm thử tải |
| Người truy cập di động đồng thời | Trên 200 người | Kiểm thử tải |
| Số lượng cân xử lý hàng ngày | Trên 500 lượt | Tổng hợp DB |
| API TPS (Bình thường) | 100 TPS | Prometheus |
| API TPS (Cao điểm) | 300 TPS | Prometheus |

### 11.2 Phương pháp giám sát hiệu năng

#### Giám sát thời gian phản hồi API

```
Bảng điều khiển Grafana: API Performance
Bảng:
  - Request Rate (req/s) theo endpoint
  - Latency p50 / p95 / p99 theo endpoint
  - Error Rate (4xx, 5xx) theo endpoint
  - Active Connections
```

#### Giám sát bộ nhớ JVM

```bash
# Kiểm tra metrics JVM qua Actuator
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | python3 -m json.tool
curl -s http://localhost:8080/actuator/metrics/jvm.gc.pause | python3 -m json.tool

# Các mục giám sát chính:
# - jvm.memory.used: Sử dụng Heap
# - jvm.gc.pause: Thời gian tạm dừng GC
# - jvm.threads.live: Số thread hoạt động
```

#### Giám sát DB Connection Pool

```bash
# Kiểm tra metrics HikariCP
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | python3 -m json.tool
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.pending | python3 -m json.tool

# Các mục giám sát chính:
# - hikaricp.connections.active: Số kết nối hoạt động (max: 30)
# - hikaricp.connections.pending: Yêu cầu kết nối đang chờ
# - hikaricp.connections.timeout: Số lần timeout kết nối
```

### 11.3 Hướng dẫn tối ưu hóa

#### Tối ưu hóa cache Redis

Đối tượng cache hiện tại:

| Đối tượng cache | Mẫu khóa | TTL | Điều kiện vô hiệu hóa |
|----------------|-----------|-----|----------------------|
| Danh mục (Công ty vận tải) | cache:company:* | 5 phút | Khi thay đổi thông tin công ty |
| Danh mục (Xe) | cache:vehicle:* | 5 phút | Khi thay đổi thông tin xe |
| Danh sách điều xe | cache:dispatch:* | 1 phút | Khi đăng ký/thay đổi điều xe |
| Mã chung | cache:code:* | 30 phút | Khi thay đổi mã |

Khi tỷ lệ cache hit dưới 80%, xem xét điều chỉnh TTL hoặc chiến lược cache.

```bash
# Kiểm tra tỷ lệ cache hit Redis
redis-cli -h 10.x.x.20 -a '<mật_khẩu>' info stats | grep keyspace
# keyspace_hits / (keyspace_hits + keyspace_misses) = Tỷ lệ hit
```

#### Tối ưu hóa truy vấn DB

```sql
-- Kiểm tra tỷ lệ sử dụng chỉ mục
SELECT schemaname, tablename, indexrelname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC
LIMIT 20;

-- Kiểm tra chỉ mục không sử dụng (Ứng viên xóa)
SELECT schemaname, tablename, indexrelname
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public';

-- Tỷ lệ quét tuần tự vs quét chỉ mục theo bảng
SELECT relname,
       seq_scan, seq_tup_read,
       idx_scan, idx_tup_fetch,
       CASE WHEN (seq_scan + idx_scan) > 0
            THEN ROUND(idx_scan * 100.0 / (seq_scan + idx_scan), 2)
            ELSE 0 END AS idx_scan_pct
FROM pg_stat_user_tables
WHERE (seq_scan + idx_scan) > 0
ORDER BY seq_scan DESC
LIMIT 20;
```

#### Tinh chỉnh hiệu năng Nginx

```nginx
# Điểm tinh chỉnh môi trường vận hành

# Worker process (Phù hợp với số lõi CPU)
worker_processes auto;

# Số kết nối (Dưới giá trị ulimit -n)
worker_connections 2048;

# Tối ưu truyền file
sendfile on;
tcp_nopush on;
tcp_nodelay on;

# Cấu hình buffer
proxy_buffer_size 128k;
proxy_buffers 4 256k;
proxy_busy_buffers_size 256k;

# Keepalive
keepalive_timeout 65;
upstream weighing_api {
    keepalive 32;   # Số kết nối keepalive upstream
}
```

---

## 12. Danh mục kiểm tra vận hành

### 12.1 Hạng mục kiểm tra hàng ngày

Kiểm tra các hạng mục sau khi bắt đầu làm việc hàng ngày (08:00).

| STT | Hạng mục kiểm tra | Phương pháp kiểm tra | Tiêu chí bình thường |
|-----|-------------------|---------------------|---------------------|
| D-01 | Trạng thái máy chủ WAS | Kiểm tra Actuator /health | Tất cả instance UP |
| D-02 | Trạng thái kết nối DB | Thực thi pg_isready | Phản hồi bình thường |
| D-03 | Trạng thái kết nối Redis | redis-cli ping | Phản hồi PONG |
| D-04 | Trạng thái Nginx | systemctl status nginx | active (running) |
| D-05 | Tỷ lệ sử dụng đĩa | df -h | < 80% |
| D-06 | Kết quả sao lưu đêm | Kiểm tra backup.log | Có log thành công |
| D-07 | Kiểm tra log lỗi | Tìm kiếm ERROR trên Kibana | Không có lỗi bất thường |
| D-08 | Bảng điều khiển trạng thái thiết bị | Giám sát thiết bị web | Tất cả thiết bị ONLINE |
| D-09 | Tỷ lệ nhận dạng LPR | Kiểm tra thống kê ngày trước | >= 95% |
| D-10 | Số lượng cân ngày trước | Kiểm tra bảng điều khiển | Trong phạm vi bình thường |

#### Script kiểm tra hàng ngày

```bash
#!/bin/bash
# /opt/weighing/scripts/daily_check.sh

echo "=========================================="
echo " Kiểm tra hàng ngày Hệ thống Cân thông minh Busan"
echo " $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

# Trạng thái WAS
echo ""
echo "[D-01] Trạng thái máy chủ WAS"
for port in 8080 8081; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${port}/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "  WAS (${port}): OK"
    else
        echo "  WAS (${port}): FAIL (HTTP ${STATUS})"
    fi
done

# Trạng thái DB
echo ""
echo "[D-02] Trạng thái PostgreSQL"
pg_isready -h ${DB_HOST} -p 5432 && echo "  DB: OK" || echo "  DB: FAIL"

# Trạng thái Redis
echo ""
echo "[D-03] Trạng thái Redis"
redis-cli -h ${REDIS_HOST} -a "${REDIS_PASSWORD}" ping 2>/dev/null && echo "  Redis: OK" || echo "  Redis: FAIL"

# Tỷ lệ sử dụng đĩa
echo ""
echo "[D-05] Tỷ lệ sử dụng đĩa"
df -h | grep -E '^/dev/' | awk '{print "  " $6 ": " $5}'

# Kết quả sao lưu
echo ""
echo "[D-06] Sao lưu gần nhất"
ls -lah /data/backup/postgresql/ | tail -3

# Số lượng log lỗi (24 giờ gần nhất)
echo ""
echo "[D-07] Số lượng log lỗi 24 giờ gần nhất"
if [ -f /data/weighing/logs/weighing-api.json ]; then
    ERROR_COUNT=$(grep -c '"level":"ERROR"' /data/weighing/logs/weighing-api.json 2>/dev/null || echo "0")
    echo "  Số lượng ERROR: ${ERROR_COUNT}"
fi

echo ""
echo "=========================================="
echo " Kiểm tra hoàn tất"
echo "=========================================="
```

### 12.2 Hạng mục kiểm tra hàng tuần

Thực hiện vào sáng thứ Hai hàng tuần.

| STT | Hạng mục kiểm tra | Phương pháp kiểm tra | Tiêu chí bình thường |
|-----|-------------------|---------------------|---------------------|
| W-01 | Cập nhật thống kê bảng DB | Thực thi ANALYZE | Hoàn thành bình thường |
| W-02 | Kiểm tra truy vấn chậm | Truy vấn pg_stat_statements | Không có truy vấn quá 3 giây |
| W-03 | Xu hướng bộ nhớ Redis | Bảng điều khiển Redis trên Grafana | < 80% |
| W-04 | Dung lượng đĩa log | du -sh /data/weighing/logs | < 70% |
| W-05 | Ngày hết hạn chứng chỉ SSL | Kiểm tra openssl x509 | Còn trên 30 ngày |
| W-06 | Dọn dẹp Docker Image | docker image prune | Xóa image không sử dụng |
| W-07 | Thống kê tỷ lệ nhận dạng LPR tuần | Truy vấn tổng hợp DB | >= 95% |
| W-08 | Kiểm tra lịch sử khóa tài khoản | Truy vấn nhật ký kiểm toán | Không có mẫu bất thường |
| W-09 | Lịch sử cảnh báo Prometheus | Kiểm tra Alertmanager | Đã giải quyết nguyên nhân cảnh báo Critical |
| W-10 | Xác minh phục hồi sao lưu | Thực thi verify_backup.sh | Phục hồi thành công |

### 12.3 Hạng mục kiểm tra hàng tháng

Thực hiện vào tuần đầu tiên hàng tháng.

| STT | Hạng mục kiểm tra | Phương pháp kiểm tra | Tiêu chí bình thường |
|-----|-------------------|---------------------|---------------------|
| M-01 | Vá bảo mật OS | dnf update --security | Áp dụng bản vá mới nhất |
| M-02 | Thực hiện DB VACUUM | VACUUM ANALYZE | Hoàn thành bình thường |
| M-03 | Dọn dẹp phiên OTP hết hạn | Xóa bản ghi quá 90 ngày | Dọn dẹp hoàn tất |
| M-04 | Kiểm toán tài khoản người dùng | Kiểm tra tài khoản không hoạt động, nhân viên nghỉ | Vô hiệu hóa tài khoản không cần thiết |
| M-05 | Phân tích xu hướng hiệu năng | Bảng điều khiển Grafana hàng tháng | Trong phạm vi mục tiêu |
| M-06 | Kiểm tra chính sách lưu trữ log | Xóa log vượt quá thời gian lưu | Tuân thủ chính sách |
| M-07 | Kiểm tra vật lý thiết bị | Kiểm tra bề ngoài/vệ sinh thiết bị hiện trường | Không có bất thường |
| M-08 | Cập nhật danh sách liên hệ khẩn cấp | Xác minh tính hợp lệ liên hệ | Duy trì trạng thái mới nhất |

#### Truy vấn dọn dẹp phiên OTP hàng tháng

```sql
-- Xóa phiên OTP quá 90 ngày
DELETE FROM tb_otp_session
WHERE created_at < NOW() - INTERVAL '90 days';

-- Kiểm tra số bản ghi đã xóa
-- DELETE n (Đã xóa n bản ghi)
```

### 12.4 Hạng mục kiểm tra hàng quý

Thực hiện hàng quý (Tháng 3, 6, 9, 12).

| STT | Hạng mục kiểm tra | Phương pháp kiểm tra | Tiêu chí bình thường |
|-----|-------------------|---------------------|---------------------|
| Q-01 | Thực hiện diễn tập DR | Kiểm tra nâng cấp DB Standby | Phục hồi trong RTO mục tiêu |
| Q-02 | Kiểm tra lỗ hổng bảo mật | Xem xét checklist OWASP | Không có lỗ hổng chưa xử lý |
| Q-03 | Kiểm tra khóa mã hóa | Kiểm tra tính hợp lệ và độ mạnh khóa | Tuân thủ chính sách |
| Q-04 | Thực hiện kiểm thử tải | Kiểm thử tải bằng JMeter, v.v. | Đạt mục tiêu hiệu năng |
| Q-05 | Kiểm tra hiệu chuẩn trạm cân | Kiểm tra lịch sử và ngày hết hạn hiệu chuẩn | Duy trì hiệu chuẩn hợp lệ |
| Q-06 | Cập nhật tài liệu | Cập nhật hướng dẫn vận hành | Hoàn tất cập nhật |
| Q-07 | Xác minh danh sách liên hệ khẩn cấp | Thực hiện kiểm tra liên hệ | Tất cả đều liên lạc được |
| Q-08 | Xem xét kế hoạch dung lượng | Xu hướng Đĩa/Bộ nhớ/CPU | Không dự kiến thiếu trong 6 tháng |

---

## 13. Phụ lục

### 13.1 Danh sách API Endpoint

#### API Xác thực

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| POST | /api/v1/auth/login | Đăng nhập ID/PW | Không cần |
| POST | /api/v1/auth/login/otp | Đăng nhập dựa trên OTP (Di động) | Không cần |
| POST | /api/v1/auth/refresh | Gia hạn Access Token | Refresh Token |
| POST | /api/v1/auth/logout | Đăng xuất | Cần |

#### API OTP

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| POST | /api/v1/otp/generate | Tạo OTP (Chương trình CS) | Internal Key |
| POST | /api/v1/otp/verify | Xác minh OTP (Di động) | Không cần |

#### API Quản lý điều xe

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/dispatches | Truy vấn danh sách điều xe | Cần |
| POST | /api/v1/dispatches | Đăng ký điều xe | ADMIN, MANAGER |
| GET | /api/v1/dispatches/{id} | Truy vấn chi tiết điều xe | Cần |
| PUT | /api/v1/dispatches/{id} | Sửa điều xe | ADMIN, MANAGER |
| DELETE | /api/v1/dispatches/{id} | Xóa điều xe | ADMIN |
| GET | /api/v1/dispatches/my | Danh sách điều xe của tôi | DRIVER |

#### API Quản lý cân

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| POST | /api/v1/weighings | Bắt đầu cân | Cần |
| PUT | /api/v1/weighings/{id}/complete | Xử lý hoàn tất cân | Cần |
| PUT | /api/v1/weighings/{id}/re-weigh | Xử lý cân lại | Cần |
| GET | /api/v1/weighings | Danh sách kết quả cân | Cần |
| GET | /api/v1/weighings/{id} | Truy vấn chi tiết cân | Cần |
| GET | /api/v1/weighings/realtime | Tình trạng cân thời gian thực (WebSocket) | Cần |
| GET | /api/v1/weighings/statistics | Thống kê cân | Cần |

#### API Phiếu cân điện tử

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/slips/{weighingId} | Truy vấn phiếu cân điện tử | Cần |
| POST | /api/v1/slips/{slipId}/share | Chia sẻ phiếu cân (KakaoTalk/SMS) | Cần |
| GET | /api/v1/slips/history | Truy vấn lịch sử phiếu cân | Cần |

#### API Quản lý xuất cổng

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/gate-passes | Truy vấn danh sách xuất cổng | ADMIN, MANAGER |
| POST | /api/v1/gate-passes | Xử lý xuất cổng | MANAGER |
| PUT | /api/v1/gate-passes/{id} | Thay đổi trạng thái xuất cổng | MANAGER |

#### API Dữ liệu danh mục

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/master/companies | Danh sách công ty vận tải | Cần |
| POST | /api/v1/master/companies | Đăng ký công ty vận tải | ADMIN |
| GET | /api/v1/master/vehicles | Danh sách xe | Cần |
| POST | /api/v1/master/vehicles | Đăng ký xe | ADMIN |
| GET | /api/v1/master/scales | Danh sách trạm cân | Cần |
| GET | /api/v1/master/codes/{group} | Truy vấn mã chung | Cần |

#### API Thông báo

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/notifications | Danh sách thông báo | Cần |
| PUT | /api/v1/notifications/{id}/read | Đánh dấu thông báo đã đọc | Cần |
| POST | /api/v1/notifications/push/register | Đăng ký token FCM | Cần |

#### API Quản lý người dùng

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| POST | /api/v1/users | Tạo người dùng | ADMIN |
| GET | /api/v1/users | Danh sách người dùng | ADMIN, MANAGER |
| GET | /api/v1/users/{id} | Truy vấn người dùng | ADMIN, MANAGER |
| PATCH | /api/v1/users/{id}/toggle-active | Bật/Tắt hoạt động | ADMIN |
| POST | /api/v1/users/{id}/unlock | Mở khóa tài khoản | ADMIN |
| PATCH | /api/v1/users/{id}/role | Thay đổi vai trò | ADMIN |
| POST | /api/v1/users/{id}/reset-password | Đặt lại mật khẩu | ADMIN |
| DELETE | /api/v1/users/{id} | Xóa người dùng | ADMIN |

#### API Hỏi đáp/Cuộc gọi

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/inquiries/contacts | Danh sách liên hệ hỏi đáp | Cần |
| POST | /api/v1/inquiries/call-log | Ghi lại lịch sử cuộc gọi | Cần |
| GET | /api/v1/inquiries/call-log | Truy vấn lịch sử cuộc gọi | ADMIN, MANAGER |
| GET | /api/v1/inquiries/call-log/my | Truy vấn lịch sử cuộc gọi của tôi | Cần |

#### API Trang cá nhân

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/mypage | Truy vấn hồ sơ cá nhân | Cần |
| PUT | /api/v1/mypage/profile | Sửa hồ sơ | Cần |
| PUT | /api/v1/mypage/password | Thay đổi mật khẩu | Cần |
| PUT | /api/v1/mypage/notifications | Thay đổi cài đặt thông báo | Cần |

#### API Yêu thích

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/favorites | Truy vấn danh sách yêu thích | Cần |
| GET | /api/v1/favorites/type/{type} | Yêu thích theo loại (DISPATCH, COMPANY, VEHICLE) | Cần |
| POST | /api/v1/favorites | Thêm yêu thích | Cần |
| POST | /api/v1/favorites/toggle | Bật/Tắt yêu thích | Cần |
| GET | /api/v1/favorites/check | Kiểm tra trạng thái yêu thích | Cần |
| PUT | /api/v1/favorites/reorder | Thay đổi thứ tự yêu thích | Cần |
| DELETE | /api/v1/favorites/{id} | Xóa yêu thích | Cần |

#### API Bản tin

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/notices | Danh sách bản tin đã đăng | Cần |
| GET | /api/v1/notices/{id} | Chi tiết bản tin | Cần |
| GET | /api/v1/notices/pinned | Danh sách bản tin ghim | Cần |
| GET | /api/v1/notices/category/{category} | Truy vấn theo danh mục | Cần |
| GET | /api/v1/notices/search | Tìm kiếm bản tin | Cần |
| GET | /api/v1/notices/admin | Danh sách toàn bộ bản tin (Bao gồm đăng/chưa đăng) | ADMIN |
| POST | /api/v1/notices | Đăng ký bản tin | ADMIN |
| PUT | /api/v1/notices/{id} | Sửa bản tin | ADMIN |
| DELETE | /api/v1/notices/{id} | Xóa bản tin | ADMIN |
| PATCH | /api/v1/notices/{id}/publish | Chuyển đổi đăng/hủy đăng | ADMIN |
| PATCH | /api/v1/notices/{id}/pin | Chuyển đổi ghim/bỏ ghim | ADMIN |

#### API Quản lý FAQ

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/help/faqs | Truy vấn danh sách FAQ | Không cần |
| GET | /api/v1/help/faqs/{id} | Chi tiết FAQ | Không cần |
| GET | /api/v1/help/faqs/category/{category} | FAQ theo danh mục | Không cần |
| GET | /api/v1/help/faqs/admin | Danh sách toàn bộ FAQ (Quản lý) | ADMIN |
| POST | /api/v1/help/faqs | Đăng ký FAQ | ADMIN |
| PUT | /api/v1/help/faqs/{id} | Sửa FAQ | ADMIN |
| DELETE | /api/v1/help/faqs/{id} | Xóa FAQ | ADMIN |

#### API Cài đặt hệ thống

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/admin/settings | Truy vấn toàn bộ cài đặt hệ thống | ADMIN |
| GET | /api/v1/admin/settings/category/{category} | Truy vấn cài đặt theo danh mục | ADMIN |
| PUT | /api/v1/admin/settings/{id} | Sửa cài đặt riêng lẻ | ADMIN |
| PUT | /api/v1/admin/settings/bulk | Sửa cài đặt hàng loạt | ADMIN |

#### API Giám sát thiết bị

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/monitoring/devices | Truy vấn trạng thái toàn bộ thiết bị | Cần |
| GET | /api/v1/monitoring/devices/type/{deviceType} | Truy vấn theo loại thiết bị | Cần |
| GET | /api/v1/monitoring/devices/{deviceId} | Trạng thái thiết bị riêng lẻ | Cần |
| PUT | /api/v1/monitoring/devices/{deviceId}/status | Cập nhật trạng thái thiết bị | ADMIN, MANAGER |
| GET | /api/v1/monitoring/summary | Tóm tắt sức khỏe thiết bị | Cần |
| POST | /api/v1/monitoring/health-check | Kích hoạt health check thiết bị | ADMIN |

#### API Thống kê (Bổ sung)

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/statistics/daily | Thống kê hàng ngày | Cần |
| GET | /api/v1/statistics/monthly | Thống kê hàng tháng | Cần |
| GET | /api/v1/statistics/summary | Thống kê tóm tắt | Cần |
| GET | /api/v1/statistics/export | Xuất Excel | Cần |

#### API Bảng điều khiển (Bổ sung)

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/dashboard/summary | Tóm tắt cân hôm nay (Tổng số, Đang xử lý, Hoàn tất) | Cần |
| GET | /api/v1/dashboard/company-stats | Thống kê cân theo công ty | Cần |

#### API Quản lý xuất cổng (Bổ sung)

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| GET | /api/v1/gate-passes | Truy vấn danh sách xuất cổng | ADMIN, MANAGER |
| GET | /api/v1/gate-passes/{id} | Chi tiết xuất cổng | ADMIN, MANAGER |
| GET | /api/v1/gate-passes?status= | Truy vấn theo trạng thái (Mặc định PENDING) | ADMIN, MANAGER |
| POST | /api/v1/gate-passes | Tạo yêu cầu xuất cổng | ADMIN, MANAGER |
| PUT | /api/v1/gate-passes/{id}/pass | Phê duyệt xuất cổng | ADMIN, MANAGER |
| PUT | /api/v1/gate-passes/{id}/reject | Từ chối xuất cổng | ADMIN, MANAGER |

#### API LPR

| Method | Endpoint | Mô tả | Xác thực |
|--------|----------|-------|----------|
| POST | /api/v1/lpr/capture | Đăng ký sự kiện chụp LPR | Internal Key |
| POST | /api/v1/lpr/verify | Xác minh biển số xe AI | Internal Key |
| POST | /api/v1/lpr/{captureId}/match | Xử lý khớp điều xe | Internal Key |
| GET | /api/v1/lpr/{captureId} | Chi tiết sự kiện chụp | Cần |
| GET | /api/v1/lpr/scale/{scaleId}/latest | Lần chụp gần nhất theo trạm cân | Cần |

### 13.2 Danh sách đầy đủ biến môi trường

| Tên biến | Bắt buộc | Mô tả | Giá trị ví dụ |
|----------|:--------:|-------|---------------|
| DB_HOST | O | Host PostgreSQL | 10.x.x.10 |
| DB_PORT | X | Cổng PostgreSQL (Mặc định 5432) | 5432 |
| DB_NAME | O | Tên cơ sở dữ liệu | weighing |
| DB_USERNAME | O | Người dùng DB | weighing_app |
| DB_PASSWORD | O | Mật khẩu DB | (Giá trị bảo mật) |
| REDIS_HOST | O | Host Redis | 10.x.x.20 |
| REDIS_PORT | X | Cổng Redis (Mặc định 6379) | 6379 |
| REDIS_PASSWORD | O | Mật khẩu Redis | (Giá trị bảo mật) |
| JWT_SECRET | O | Khóa ký JWT (Base64) | (Giá trị bảo mật) |
| AES_SECRET_KEY | O | Khóa mã hóa AES-256 (Base64) | (Giá trị bảo mật) |
| CORS_ORIGIN_WEB | O | CORS Origin cho phép | https://weighing.factory.internal |
| CORS_ORIGIN_MOBILE | X | CORS Origin di động | http://localhost:8081 |
| API_INTERNAL_KEY | O | Khóa API nội bộ chương trình CS | (Giá trị bảo mật) |
| SPRING_PROFILES_ACTIVE | O | Profile hoạt động | prod |
| APP_VERSION | X | Tag phiên bản ứng dụng | 1.0.0 |

### 13.3 ERD Cơ sở dữ liệu

```
tb_company (Công ty vận tải)          tb_vehicle (Xe)
+-----------------+                 +------------------+
| company_id (PK) |<---+       +--->| vehicle_id (PK)  |
| company_name    |    |       |    | plate_number (UQ)|
| company_type    |    |       |    | company_id (FK)  |---+
| contact_phone   |    |       |    | vehicle_type     |   |
| is_active       |    |       |    | max_load_weight  |   |
+-----------------+    |       |    | is_active        |   |
        |              |       |    +------------------+   |
        |              |       |                           |
        v              |       |                           |
tb_user (Người dùng)   |       |                           |
+-----------------+    |       |                           |
| user_id (PK)    |    |       |                           |
| company_id (FK) |----+       |                           |
| user_name       |            |                           |
| phone_number    |            |                           |
| user_role       |            |                           |
| login_id (UQ)   |            |                           |
| password_hash   |            |                           |
| is_active       |            |                           |
| failed_login_cnt|            |                           |
| locked_until    |            |                           |
+-----------------+            |                           |
        |                      |                           |
        v                      |                           |
tb_otp_session (OTP)           |                           |
+-----------------+            |                           |
| otp_id (PK)     |            |                           |
| user_id (FK)    |            |                           |
| otp_code        |            |                           |
| vehicle_id (FK) |------------+                           |
| phone_number    |                                        |
| expires_at      |                                        |
| is_verified     |                                        |
+-----------------+                                        |
                                                           |
tb_dispatch (Điều xe)                                      |
+-------------------+                                      |
| dispatch_id (PK)  |                                      |
| vehicle_id (FK)   |--------------------------------------+
| company_id (FK)   |---+
| item_type         |   |
| item_name         |   |
| dispatch_date     |   |
| dispatch_status   |   |
| created_by (FK)   |   |
+-------------------+   |
        |                |
        | 1:N            |
        v                |
tb_weighing (Kết quả cân)|
+-------------------+    |
| weighing_id (PK)  |    |
| dispatch_id (FK)  |    |
| vehicle_id (FK)   |    |
| scale_id (FK)     |--->| tb_scale (Trạm cân)
| weighing_type     |    | +------------------+
| weighing_step     |    | | scale_id (PK)    |
| gross_weight      |    | | scale_name       |
| tare_weight       |    | | location         |
| net_weight        |    | | scale_type       |
| weighing_mode     |    | | max_capacity     |
| lpr_plate_number  |    | | is_active        |
| ai_confidence     |    | +------------------+
| status            |    |
| weighed_at        |    |
+-------------------+    |
    |           |        |
    |           v        |
    |   tb_weighing_slip |
    |   (Phiếu cân       |
    |    điện tử)        |
    |   +--------------+ |
    |   | slip_id (PK) | |
    |   | weighing_id  | |
    |   | slip_number  | |
    |   | slip_data    | |
    |   | shared_via   | |
    |   +--------------+ |
    |                    |
    v                    |
tb_gate_pass (Xuất cổng) |
+-----------------+      |
| gate_pass_id(PK)|      |
| weighing_id(FK) |      |
| dispatch_id(FK) |------+
| pass_status     |
| passed_at       |
+-----------------+

tb_master_code (Mã chung)       tb_audit_log (Nhật ký kiểm toán)
+------------------+         +------------------+
| code_id (PK)     |         | audit_id (PK)    |
| code_group       |         | event_type       |
| code_value       |         | user_id          |
| code_name        |         | ip_address       |
| sort_order       |         | detail           |
| is_active        |         | created_at       |
| parent_code_id   |         +------------------+
+------------------+

tb_notification (Thông báo)     tb_inquiry_call (Hỏi đáp)
+------------------+         +------------------+
| noti_id (PK)     |         | call_id (PK)     |
| user_id (FK)     |         | user_id (FK)     |
| noti_type        |         | inquiry_type     |
| title            |         | target_dept      |
| message          |         | call_status      |
| is_read          |         | created_at       |
| sent_at          |         +------------------+
+------------------+
```

### 13.4 Bảng thuật ngữ

| Thuật ngữ | Tiếng Anh | Mô tả |
|-----------|-----------|-------|
| LPR | License Plate Recognition | Thiết bị nhận dạng biển số xe tự động |
| LiDAR | Light Detection and Ranging | Cảm biến đo khoảng cách bằng laser (Dùng phát hiện xe) |
| OTP | One-Time Password | Mật khẩu bảo mật dùng một lần (6 chữ số) |
| Indicator | Indicator | Thiết bị hiển thị/truyền giá trị khối lượng tại trạm cân |
| Phiếu cân điện tử | Electronic Weighing Slip | Chứng từ cân kỹ thuật số thay thế phiếu cân giấy |
| Điều xe | Dispatch | Phân bổ lịch vận chuyển xe |
| Trạm cân | Scale / Weighbridge | Cơ sở đo khối lượng xe |
| Tổng khối lượng | Gross Weight | Khối lượng tổng xe + hàng hóa |
| Khối lượng xe rỗng | Tare Weight | Khối lượng xe không có hàng |
| Khối lượng ròng | Net Weight | Tổng khối lượng - Khối lượng xe rỗng = Khối lượng ròng hàng hóa |
| RS-232C | - | Tiêu chuẩn giao tiếp nối tiếp (Dùng cho giao tiếp Indicator) |
| JWT | JSON Web Token | Tiêu chuẩn token xác thực (Xác thực Stateless) |
| RBAC | Role-Based Access Control | Kiểm soát truy cập dựa trên vai trò |
| bcrypt | - | Thuật toán băm mật khẩu (Một chiều) |
| AES-256 | Advanced Encryption Standard | Thuật toán mã hóa khóa đối xứng (256bit) |
| TLS | Transport Layer Security | Giao thức mã hóa truyền thông |
| WAL | Write-Ahead Logging | Nhật ký giao dịch PostgreSQL (Dùng cho phục hồi PITR) |
| PITR | Point-in-Time Recovery | Phục hồi đến thời điểm cụ thể |
| RPO | Recovery Point Objective | Thời điểm cho phép mất dữ liệu (Mục tiêu điểm phục hồi) |
| RTO | Recovery Time Objective | Mục tiêu thời gian phục hồi dịch vụ |
| HikariCP | - | Thư viện JDBC Connection Pool |
| ELK Stack | Elasticsearch + Logstash + Kibana | Hệ thống quản lý log tập trung |
| Prometheus | - | Hệ thống thu thập/lưu trữ metrics chuỗi thời gian |
| Grafana | - | Công cụ bảng điều khiển trực quan hóa metrics |
| Active-Active | - | Cấu hình dự phòng kép, cả hai máy chủ xử lý lưu lượng đồng thời |
| Rolling Update | - | Phương thức triển khai không gián đoạn bằng cách cập nhật máy chủ tuần tự |
| Failover | - | Chuyển đổi tự động sang hệ thống thay thế khi xảy ra sự cố |
| CSP | Content Security Policy | Header bảo mật web (Phòng thủ XSS) |
| OWASP | Open Web Application Security Project | Dự án bảo mật ứng dụng web |

---

**Lịch sử tài liệu**

| Phiên bản | Ngày | Tác giả | Nội dung thay đổi |
|-----------|------|---------|-------------------|
| 1.0 | 2026-01-29 | Đội quản trị hệ thống | Bản đầu tiên |
| 1.1 | 2026-01-29 | Đội quản trị hệ thống | Bổ sung API quản lý người dùng (Thay đổi vai trò, Đặt lại mật khẩu, Xóa), Thêm quản lý cài đặt hệ thống, Thêm quản lý bản tin ADMIN (Đăng ký/Sửa/Xóa/Đăng/Ghim), Thêm quản lý FAQ ADMIN, Bổ sung API giám sát thiết bị (Tóm tắt/Health check), Thêm API Trang cá nhân/Yêu thích/Bản tin/Thống kê/Bảng điều khiển/Xuất cổng/LPR, Cập nhật ma trận quyền truy cập theo vai trò |
| 1.2 | 2026-01-29 | Đội quản trị hệ thống | Phản ánh tính năng mới web frontend (Hướng dẫn onboarding, Phím tắt, Giao diện trạng thái trống, Hoạt ảnh số, Phát hiện tab hoạt động), Phản ánh cache offline di động, Phản ánh SplashForm desktop/Trừu tượng hóa giao diện phần cứng/Kiểm thử xUnit, Phản ánh quản lý routing tập trung dựa trên page registry, Thêm tham chiếu tài liệu thiết kế chi tiết theo module |

---

## Phụ lục C: Tham khảo vận hành - Yếu tố kiến trúc mới (v1.2)

### C.1 Routing dựa trên Page Registry

Tất cả trang của web frontend được quản lý tập trung tại `frontend/src/config/pageRegistry.ts`. Khi thêm menu mới hoặc thay đổi quyền, sửa file này.

```
Các mục cấu hình chính:
- component: Thành phần phân tách code dựa trên React.lazy
- title: Tên hiển thị menu/tab
- icon: Icon Ant Design
- closable: Có thể đóng tab hay không (Trạm cân là false)
- roles: Mảng vai trò có thể truy cập (Tất cả truy cập khi không chỉ định)
```

### C.2 Danh sách tính năng mới Web Frontend

| Tính năng | File | Ảnh hưởng vận hành |
|-----------|------|-------------------|
| Hướng dẫn onboarding | `OnboardingTour.tsx` | Giảm gánh nặng đào tạo người dùng mới |
| Phím tắt | `useKeyboardShortcuts.ts` | Nâng cao hiệu quả công việc nhân viên |
| Giao diện trạng thái trống | `EmptyState.tsx` | Cải thiện hướng dẫn khi không có dữ liệu |
| Hoạt ảnh số | `AnimatedNumber.tsx` | Hiệu ứng trực quan KPI bảng điều khiển |
| Phát hiện tab hoạt động | `useTabVisible.ts` | Tự động làm mới dữ liệu khi chuyển tab |
| Sắp xếp kéo thả | `SortableTable.tsx` | Cải thiện UX thay đổi thứ tự hàng bảng |

### C.3 Vận hành cache offline di động

Ứng dụng di động đã thêm `OfflineCacheService` cho phép truy vấn dữ liệu cơ bản ngay cả trong môi trường mạng không ổn định.

| Mục | Cấu hình |
|-----|----------|
| **Lưu trữ** | SharedPreferences (Bộ nhớ trong thiết bị) |
| **Đối tượng cache** | Danh sách điều xe, Lịch sử cân |
| **Thời hạn** | 1 giờ (Tự động làm mới) |
| **Giới hạn dung lượng** | Phụ thuộc bộ nhớ thiết bị (Chỉ cache dữ liệu nhẹ) |
| **Bảo mật** | Dữ liệu nhạy cảm (Token, v.v.) lưu riêng trong flutter_secure_storage |

### C.4 Cải tiến kiến trúc Desktop

Các cải tiến sau đã được áp dụng cho chương trình WeighingCS:

| Mục cải tiến | Mô tả |
|-------------|-------|
| **SplashForm** | Hiển thị trạng thái khởi tạo khi khởi động ứng dụng (Tải cấu hình, Kiểm tra kết nối thiết bị) |
| **Trừu tượng hóa giao diện** | Tách biệt phần cứng qua interface `ILprCamera`, `IVehicleDetector`, `IVehicleSensor` |
| **Simulator** | Triển khai simulator cho từng thiết bị trong thư mục `Simulators/` (Dùng cho phát triển/kiểm thử) |
| **Kiểm thử xUnit** | Kiểm thử đơn vị `ApiServiceTests`, `IndicatorServiceTests`, `LocalCacheServiceTests` |

### C.5 Tham chiếu tài liệu thiết kế chi tiết

Tài liệu thiết kế chi tiết cho từng module đã được thêm vào thư mục `docs/design/`:

| Tài liệu | Module đối tượng |
|----------|-----------------|
| `auth-basic-design.md` | Thiết kế cơ bản xác thực |
| `auth-detail-design.md` | Thiết kế chi tiết xác thực |
| `dispatch-detail-design.md` | Thiết kế chi tiết module điều xe |
| `weighing-detail-design.md` | Thiết kế chi tiết module cân |
| `gatepass-slip-detail-design.md` | Thiết kế chi tiết module xuất cổng/phiếu |
| `lpr-otp-notification-detail-design.md` | Thiết kế chi tiết module LPR/OTP/Thông báo |
| `ui_ux_recommendation.md` | Hướng dẫn thiết kế UI/UX |

---

*Tài liệu này là hướng dẫn chính thức cho việc vận hành và quản lý Hệ thống Cân thông minh Busan. Khi hệ thống thay đổi, bắt buộc phải cập nhật tài liệu này.*
