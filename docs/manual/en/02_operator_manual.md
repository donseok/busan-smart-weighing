# Busan Smart Weighing System - Operator Manual

**Document Number**: MAN-OPS-002
**Version**: 1.1
**Date**: 2026-01-29
**Target Audience**: System Administrator / Operator (ADMIN)
**Reference Documents**: TRD-20260127-155235, PRD-20260127-154446, FUNC-SPEC v1.0
**Status**: Draft

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Installation and Environment Configuration](#2-installation-and-environment-configuration)
3. [User Management](#3-user-management)
4. [Security Management](#4-security-management)
5. [System Monitoring](#5-system-monitoring)
6. [Database Management](#6-database-management)
7. [Master Data Management](#7-master-data-management)
8. [Equipment Management](#8-equipment-management)
9. [Incident Response](#9-incident-response)
10. [Backup and Recovery](#10-backup-and-recovery)
11. [Performance Management](#11-performance-management)
12. [Operational Checklists](#12-operational-checklists)
13. [Appendix](#13-appendix)

---

## 1. System Overview

### 1.1 System Architecture

The Busan Smart Weighing System is an unmanned weighing automation system based on LPR (License Plate Recognition). It consists of on-site hardware, a weighbridge client program, an API server, a database, and web/mobile frontends.

```
+-----------------------------------------------------------------------+
|                    Busan Plant Weighbridge Site                        |
|                                                                       |
|  [LPR Camera] [LiDAR Sensor] [Radar] [Vehicle Detector] [Indicator]  |
|       |            |          |          |            |               |
|       +------------+----------+----------+------------+               |
|                              |                                        |
|                   +----------v-----------+                            |
|                   | Weighbridge CS Program|<--- RS-232C (Indicator)   |
|                   | (C# .NET WinForms)   |---- TCP/UDP (LPR/Sensor)  |
|                   +----------+-----------+                            |
|                              |                                        |
|  [Display Board (OTP)]  [Auto Barrier]  |    [Intercom (Master/Sub)] |
+------------------------------+----------------------------------------+
                               | HTTPS (REST API)
                               |
+------------------------------+----------------------------------------+
|                         Server Infrastructure                         |
|                              |                                        |
|               +--------------v--------------+                         |
|               |    Nginx (Reverse Proxy)     |                         |
|               |   SSL Termination / LB       |                         |
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
|            |  - Dispatch Management Service   |                       |
|            |  - Weighing Management Service   |                       |
|            |  - User/Auth Service             |                       |
|            |  - OTP Auth Service              |                       |
|            |  - LPR/AI Integration Service    |                       |
|            |  - Notification Service          |                       |
|            |  - WebSocket Real-time Push      |                       |
|            +------+---------------+-----------+                       |
|                   |               |                                   |
|        +----------v--+   +-------v------+   +-----------+             |
|        | PostgreSQL   |   |   Redis     |   | AI Recog. |             |
|        | (Primary +   |   | (Cache/OTP/ |   | Engine    |             |
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
             | Web      | |Mobile  | | External     |
             | (React)  | |App     | | Services     |
             |          | |(Flutter)| | (Kakao/SMS/  |
             |          | |         | |  FCM)        |
             +---------+  +--------+  +-------------+
```

### 1.2 Technology Stack

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| Backend | Java | 17 LTS | API server development language |
| Backend | Spring Boot | 3.2.5 | Application framework |
| Backend | Spring Security | 6.x | JWT authentication + RBAC authorization |
| Database | PostgreSQL | 16.x | Primary DB (ACID transactions) |
| Cache | Redis | 7.x | OTP/session/cache/message queue |
| Frontend (Web) | React + TypeScript | 18.x / 5.x | Admin web system |
| Frontend (Web) | Ant Design + Vite | 5.x / 5.x | UI components + build tool |
| Frontend (Mobile) | Flutter | 3.x | iOS/Android mobile app |
| Weighbridge CS | C# .NET | 11 / 7+ | Weighbridge client program |
| Reverse Proxy | Nginx | 1.24.x | SSL termination, load balancing |
| Container | Docker + Docker Compose | Latest | Container deployment |
| Monitoring | Prometheus + Grafana | Latest | Metrics collection and visualization |
| Logging | ELK Stack | Latest | Centralized log management |
| CI/CD | Jenkins / GitLab CI | Latest | Build and deployment automation |
| VCS | Git (GitLab) | Latest | Source code management |

### 1.3 Network Configuration

| Source | Destination | Protocol | Port | Description |
|--------|------------|----------|------|-------------|
| LPR/Sensor Equipment | Weighbridge CS | TCP/UDP | Configured | License plate recognition, sensor events |
| Indicator | Weighbridge CS | RS-232C | COM Port | Stabilized weight value reception |
| Weighbridge CS | Display Board/Barrier | TCP/RS-485 | Configured | OTP display, barrier control |
| Weighbridge CS | Nginx | HTTPS | 443 | API calls |
| React Web | Nginx | HTTPS | 443 | Web access |
| React Web | Nginx | WSS | 443 | WebSocket real-time |
| Flutter App | Nginx | HTTPS | 443 | Mobile API |
| Nginx | Spring Boot | HTTP | 8080/8081 | Reverse proxy |
| Spring Boot | PostgreSQL | TCP | 5432 | DB connection |
| Spring Boot | Redis | TCP | 6379 | Cache/OTP/session |
| Spring Boot | AI Engine | HTTPS | Configured | License plate AI verification |
| Spring Boot | Kakao API | HTTPS | 443 | Kakao notification |
| Spring Boot | SMS Gateway | HTTPS | 443 | SMS delivery |
| Spring Boot | FCM | HTTPS | 443 | Push notification |

### 1.4 Server Configuration

| Environment | Configuration | Specs | Purpose |
|-------------|--------------|-------|---------|
| Production - WAS | 2 VMs (Active-Active) | 8 vCPU, 32GB RAM, 100GB SSD | Spring Boot API Server |
| Production - DB | 1 VM + Standby | 8 vCPU, 32GB RAM, 500GB SSD | PostgreSQL Primary + Standby |
| Production - Redis | 1 VM | 4 vCPU, 16GB RAM, 50GB SSD | Redis Cache/OTP/Session |
| Production - Nginx | 1 VM | 2 vCPU, 4GB RAM, 50GB SSD | Reverse Proxy, SSL termination |
| Monitoring | 1 VM | 4 vCPU, 8GB RAM, 200GB SSD | Prometheus, Grafana, ELK |
| Staging | 2 VMs | 4 vCPU, 16GB RAM, 200GB SSD | Pre-production validation environment |
| Development | 1 VM | 4 vCPU, 16GB RAM, 200GB SSD | Development/test integrated environment |

> **Note**: All servers run on CentOS / Rocky Linux 9 and are deployed in an on-premises environment.

---

## 2. Installation and Environment Configuration

### 2.1 Server Environment Requirements

#### Operating System Requirements

```
OS: Rocky Linux 9.x (or CentOS Stream 9)
Kernel: 5.14 or higher
SELinux: enforcing mode recommended
Firewall: firewalld enabled
```

#### Required Package Installation

```bash
# Install base packages
sudo dnf update -y
sudo dnf install -y \
    curl wget vim git \
    net-tools lsof htop \
    tar gzip unzip \
    openssl ca-certificates

# Install Java 17 (WAS server)
sudo dnf install -y java-17-openjdk java-17-openjdk-devel
java -version
# Expected output: openjdk version "17.0.x"

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk' >> /etc/profile.d/java.sh
source /etc/profile.d/java.sh
```

#### Firewall Configuration

```bash
# Nginx server (external access)
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --permanent --add-port=443/tcp

# WAS server (internal network only)
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=8081/tcp

# DB server (accessible only from WAS)
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.x.x.0/24" port protocol="tcp" port="5432" accept'

# Redis server (accessible only from WAS)
sudo firewall-cmd --permanent --add-rich-rule='rule family="ipv4" source address="10.x.x.0/24" port protocol="tcp" port="6379" accept'

# Monitoring server
sudo firewall-cmd --permanent --add-port=9090/tcp   # Prometheus
sudo firewall-cmd --permanent --add-port=3000/tcp   # Grafana
sudo firewall-cmd --permanent --add-port=5601/tcp   # Kibana

# Apply firewall rules
sudo firewall-cmd --reload
sudo firewall-cmd --list-all
```

### 2.2 Docker-Based Deployment

#### Docker and Docker Compose Installation

```bash
# Install Docker
sudo dnf config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo dnf install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start Docker service and enable auto-start
sudo systemctl start docker
sudo systemctl enable docker

# Add operations account to Docker group
sudo usermod -aG docker weighing-admin

# Verify installation
docker --version
docker compose version
```

#### Docker Compose Configuration (docker-compose.yml)

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

#### Deployment and Rolling Update Procedure

```bash
# 1. Pull new image
docker pull registry.internal/weighing-api:${NEW_VERSION}

# 2. Update WAS #1 (service maintained: WAS #2 handles traffic)
docker compose stop weighing-api-1
APP_VERSION=${NEW_VERSION} docker compose up -d weighing-api-1

# 3. Verify WAS #1 health check (wait up to 90 seconds)
echo "Waiting for WAS #1 health check..."
for i in $(seq 1 30); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "WAS #1 startup confirmed"
        break
    fi
    sleep 3
done

# 4. Update WAS #2
docker compose stop weighing-api-2
APP_VERSION=${NEW_VERSION} docker compose up -d weighing-api-2

# 5. Verify WAS #2 health check
echo "Waiting for WAS #2 health check..."
for i in $(seq 1 30); do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "WAS #2 startup confirmed"
        break
    fi
    sleep 3
done

echo "Rolling update complete: version=${NEW_VERSION}"
```

#### Rollback Procedure (within 5 minutes)

```bash
# Immediately rollback to previous version
ROLLBACK_VERSION="previous_version_tag"

docker compose stop weighing-api-1 weighing-api-2
APP_VERSION=${ROLLBACK_VERSION} docker compose up -d weighing-api-1 weighing-api-2

# Verify health check
curl -s http://localhost:8080/actuator/health | python3 -m json.tool
curl -s http://localhost:8081/actuator/health | python3 -m json.tool
```

### 2.3 Environment Variable Configuration

Production environment variables are managed in the `.env` file. This file must have its permissions restricted with `chmod 600`.

#### .env File Configuration

```bash
# File path: /opt/weighing/.env
# Permissions: chmod 600, chown weighing-admin:weighing-admin

# === Database ===
DB_HOST=10.x.x.10
DB_PORT=5432
DB_NAME=weighing
DB_USERNAME=weighing_app
DB_PASSWORD=<strong_password>

# === Redis ===
REDIS_HOST=10.x.x.20
REDIS_PORT=6379
REDIS_PASSWORD=<strong_password>

# === JWT ===
# Base64-encoded secret key of 256 bits or more
JWT_SECRET=<Base64_encoded_secret_key>

# === AES-256 Encryption Key ===
AES_SECRET_KEY=<Base64_encoded_AES_key>

# === CORS ===
CORS_ORIGIN_WEB=https://weighing.factory.internal

# === Internal API Key ===
API_INTERNAL_KEY=<weighbridge_CS_internal_auth_key>

# === Application Version ===
APP_VERSION=1.0.0
```

#### Environment Variable Security Notes

- The `.env` file must never be committed to Git (verify it is included in `.gitignore`).
- Set file permissions to `600` so that only the owner can read it.
- JWT_SECRET and AES_SECRET_KEY must use random values of at least 256 bits, Base64-encoded.
- When changing passwords, related services must be restarted sequentially.

#### Secret Key Generation Methods

```bash
# Generate JWT Secret (256-bit)
openssl rand -base64 32

# Generate AES-256 Key
openssl rand -base64 32

# Generate Internal API Key
openssl rand -hex 32
```

### 2.4 Nginx Configuration

#### Base Configuration (/etc/nginx/nginx.conf)

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

    # Log format
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

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript
               text/xml application/xml image/svg+xml;

    # Rate Limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=30r/s;
    limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=5r/s;

    # Upstream (API server load balancing)
    upstream weighing_api {
        least_conn;
        server 10.x.x.1:8080 max_fails=3 fail_timeout=30s;
        server 10.x.x.1:8081 max_fails=3 fail_timeout=30s;
        keepalive 32;
    }

    include /etc/nginx/conf.d/*.conf;
}
```

#### Site Configuration (/etc/nginx/conf.d/weighing.conf)

```nginx
# HTTP -> HTTPS redirect
server {
    listen 80;
    server_name weighing.factory.internal;
    return 301 https://$host$request_uri;
}

# HTTPS server
server {
    listen 443 ssl http2;
    server_name weighing.factory.internal;

    # SSL certificate
    ssl_certificate     /etc/nginx/ssl/weighing.crt;
    ssl_certificate_key /etc/nginx/ssl/weighing.key;

    # TLS settings
    ssl_protocols TLSv1.3;
    ssl_prefer_server_ciphers on;
    ssl_ciphers 'TLS_AES_256_GCM_SHA384:TLS_CHACHA20_POLY1305_SHA256:TLS_AES_128_GCM_SHA256';

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options DENY always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' wss://$host" always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    # React static files
    location / {
        root /var/www/weighing;
        index index.html;
        try_files $uri $uri/ /index.html;

        # Static file caching
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }

    # API proxy
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

    # Auth API rate limiting (stricter)
    location /api/v1/auth/ {
        limit_req zone=auth_limit burst=5 nodelay;
        proxy_pass http://weighing_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket proxy
    location /ws/ {
        proxy_pass http://weighing_api;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_read_timeout 86400s;
    }

    # Actuator (internal network only)
    location /actuator/ {
        allow 10.0.0.0/8;
        deny all;
        proxy_pass http://weighing_api;
        proxy_set_header Host $host;
    }

    # Swagger UI (dev/staging environments only)
    # location /swagger-ui/ {
    #     allow 10.0.0.0/8;
    #     deny all;
    #     proxy_pass http://weighing_api;
    # }
}
```

#### Nginx Configuration Validation and Application

```bash
# Validate configuration syntax
sudo nginx -t

# Reload configuration (zero downtime)
sudo nginx -s reload

# Full restart (if needed)
sudo systemctl restart nginx

# Check status
sudo systemctl status nginx
```

### 2.5 SSL Certificate Management

#### Certificate File Structure

```
/etc/nginx/ssl/
  weighing.crt      # Server certificate (+ intermediate CA chain)
  weighing.key       # Private key (chmod 600)
  ca-bundle.crt      # CA certificate bundle (if needed)
```

#### Certificate Installation

```bash
# Create certificate directory
sudo mkdir -p /etc/nginx/ssl
sudo chmod 700 /etc/nginx/ssl

# Copy certificate files
sudo cp weighing.crt /etc/nginx/ssl/
sudo cp weighing.key /etc/nginx/ssl/

# Set permissions
sudo chmod 644 /etc/nginx/ssl/weighing.crt
sudo chmod 600 /etc/nginx/ssl/weighing.key
sudo chown root:root /etc/nginx/ssl/*
```

#### Certificate Expiration Check

```bash
# Check certificate expiration date
openssl x509 -enddate -noout -in /etc/nginx/ssl/weighing.crt
# Example output: notAfter=Jan 29 00:00:00 2027 GMT

# Check certificate details
openssl x509 -text -noout -in /etc/nginx/ssl/weighing.crt | head -20

# Check remote certificate
openssl s_client -connect weighing.factory.internal:443 -servername weighing.factory.internal </dev/null 2>/dev/null | openssl x509 -noout -dates
```

#### Certificate Renewal Procedure

1. Obtain a new certificate (from internal CA or external CA).
2. Copy the new certificate files to `/etc/nginx/ssl/`.
3. Validate the configuration with `sudo nginx -t`.
4. Apply with zero downtime using `sudo nginx -s reload`.
5. Verify the certificate information in the browser.

> **Important**: Begin the renewal procedure 30 days before certificate expiration. It is recommended to configure certificate expiration monitoring alerts in Prometheus.

### 2.6 Database Initial Setup

#### PostgreSQL Installation and Configuration

```bash
# Install PostgreSQL 16
sudo dnf install -y https://download.postgresql.org/pub/repos/yum/reporpms/EL-9-x86_64/pgdg-redhat-repo-latest.noarch.rpm
sudo dnf install -y postgresql16-server postgresql16-contrib
sudo /usr/pgsql-16/bin/postgresql-16-setup initdb
sudo systemctl start postgresql-16
sudo systemctl enable postgresql-16
```

#### Database and User Creation

```sql
-- Connect to PostgreSQL
-- sudo -u postgres psql

-- Create application user
CREATE USER weighing_app WITH PASSWORD 'set_strong_password_here';

-- Create database
CREATE DATABASE weighing
    OWNER weighing_app
    ENCODING 'UTF8'
    LC_COLLATE 'ko_KR.UTF-8'
    LC_CTYPE 'ko_KR.UTF-8'
    TEMPLATE template0;

-- Set permissions
GRANT ALL PRIVILEGES ON DATABASE weighing TO weighing_app;
\c weighing
GRANT ALL ON SCHEMA public TO weighing_app;

-- Read-only user (for monitoring)
CREATE USER weighing_readonly WITH PASSWORD 'separate_password';
GRANT CONNECT ON DATABASE weighing TO weighing_readonly;
GRANT USAGE ON SCHEMA public TO weighing_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO weighing_readonly;
```

#### pg_hba.conf Configuration

```
# /var/lib/pgsql/16/data/pg_hba.conf

# TYPE  DATABASE        USER            ADDRESS                 METHOD
local   all             postgres                                peer
local   all             all                                     scram-sha-256
host    weighing        weighing_app    10.x.x.0/24            scram-sha-256
host    weighing        weighing_readonly 10.x.x.0/24          scram-sha-256
host    replication     replicator      10.x.x.11/32           scram-sha-256
```

#### postgresql.conf Key Settings (Production Environment)

```
# /var/lib/pgsql/16/data/postgresql.conf

# Connection settings
listen_addresses = '10.x.x.10'
port = 5432
max_connections = 200

# Memory settings (based on 32GB RAM)
shared_buffers = 8GB
effective_cache_size = 24GB
work_mem = 64MB
maintenance_work_mem = 2GB

# WAL settings
wal_level = replica
max_wal_senders = 5
wal_keep_size = 1GB
archive_mode = on
archive_command = 'cp %p /data/pg_archive/%f'

# Logging
logging_collector = on
log_directory = 'log'
log_filename = 'postgresql-%Y-%m-%d_%H%M%S.log'
log_rotation_age = 1d
log_rotation_size = 100MB
log_min_duration_statement = 3000
log_line_prefix = '%t [%p] %u@%d '

# Performance
checkpoint_completion_target = 0.9
random_page_cost = 1.1
effective_io_concurrency = 200

# Timezone
timezone = 'Asia/Seoul'
```

#### Initial Data Insertion

```sql
-- Create system administrator account (initial password: Admin1234!)
-- bcrypt hash (cost=12)
INSERT INTO tb_user (user_name, phone_number, user_role, login_id, password_hash)
VALUES (
    'System Administrator',
    '010-0000-0000',
    'ADMIN',
    'admin',
    '$2a$12$LJ3MFgfFw.PAGtv.Q0n.aeF8VPx4dSmA5WVUkRrXQJQNvk7z3K5Hm'
);
```

> **Important**: The password must be changed immediately after creating the initial administrator account.

### 2.7 Redis Configuration

#### Redis Installation

```bash
sudo dnf install -y redis
sudo systemctl start redis
sudo systemctl enable redis
```

#### redis.conf Key Settings

```
# /etc/redis/redis.conf

# Network
bind 10.x.x.20
port 6379
protected-mode yes

# Authentication
requirepass <strong_password>

# Memory (based on 16GB RAM)
maxmemory 8gb
maxmemory-policy allkeys-lru

# Persistence
save 3600 1
save 300 100
save 60 10000

# RDB snapshot
dbfilename dump.rdb
dir /var/lib/redis

# Logging
loglevel notice
logfile /var/log/redis/redis.log

# Security
rename-command FLUSHALL ""
rename-command FLUSHDB ""
rename-command DEBUG ""

# Performance
tcp-keepalive 300
timeout 300
```

#### Redis Connection Verification

```bash
# Verify local connection
redis-cli -h 10.x.x.20 -a '<password>' ping
# Output: PONG

# Check memory usage
redis-cli -h 10.x.x.20 -a '<password>' info memory | grep used_memory_human

# Check key patterns (operational monitoring)
redis-cli -h 10.x.x.20 -a '<password>' --scan --pattern 'auth:*' | head -10
redis-cli -h 10.x.x.20 -a '<password>' --scan --pattern 'otp:*' | head -10
```

---

## 3. User Management

### 3.1 User Role System

The system manages access permissions based on 3 roles.

| Role | Code | Description | Key Permissions |
|------|------|-------------|-----------------|
| System Administrator | ADMIN | Full system operations management | All feature access, user management, system settings, notices/FAQ management |
| Weighing Manager | MANAGER | Weighing operations | Dispatch registration/modification, weighing management, gate pass management, master data viewing |
| Driver | DRIVER | Vehicle driver | Mobile app login, dispatch viewing, weighing process, electronic weighing slip |

#### Role-Based Access Permission Matrix

| Feature | ADMIN | MANAGER | DRIVER |
|---------|:-----:|:-------:|:------:|
| Create/modify/delete users | O | X | X |
| Change roles | O | X | X |
| Unlock accounts | O | X | X |
| Reset passwords | O | X | X |
| View audit logs | O | X | X |
| Manage system settings | O | X | X |
| Manage notices (create/edit/delete/pin) | O | X | X |
| Manage FAQ (create/edit/delete) | O | X | X |
| Equipment monitoring | O | X | X |
| Trigger equipment health check | O | X | X |
| Register/modify dispatches | O | O | X |
| Delete dispatches | O | X | X |
| Weighing management | O | O | X |
| Gate pass management | O | O | X |
| Register/modify master data | O | X | X |
| View master data | O | O | X |
| View statistics | O | O | X |
| View my dispatches | O | O | O |
| Mobile weighing process | X | X | O |
| View electronic weighing slips | O | O | O |
| OTP authentication | X | X | O |

### 3.2 User Registration

#### Registration via Web Admin Interface

1. Log in to the web system with an ADMIN account.
2. Select **[System Management] > [User Management]** from the left menu.
3. Click the **[New Registration]** button.
4. Enter the required fields:
   - **Login ID**: 3-50 characters, alphanumeric combination (must be unique)
   - **Password**: 8 characters or more, must include letters + numbers
   - **User Name**: Enter real name
   - **Phone Number**: 010-XXXX-XXXX format (used for mobile OTP authentication)
   - **Role**: Select from ADMIN / MANAGER / DRIVER
   - **Affiliated Transport Company**: Required for DRIVER role
5. Click the **[Save]** button.
6. Confirm the registration completion message.

#### Registration via API

```bash
# User creation API
curl -X POST https://weighing.factory.internal/api/v1/users \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "loginId": "driver001",
    "password": "SecurePass123",
    "userName": "Hong Gildong",
    "phoneNumber": "010-1234-5678",
    "userRole": "DRIVER",
    "companyId": 1
  }'
```

Response example:
```json
{
  "success": true,
  "data": {
    "userId": 10,
    "userName": "Hong Gildong",
    "phoneNumber": "010-****-5678",
    "userRole": "DRIVER",
    "companyName": "ABC Transport",
    "isActive": true,
    "createdAt": "2026-01-29T10:00:00+09:00"
  }
}
```

### 3.3 User Modification and Deactivation

#### Modifying User Information

1. Search for the target user on the **[User Management]** screen.
2. Click the **[Edit]** button on the user's row.
3. Editable fields: User Name, Phone Number, Role, Affiliated Transport Company.
4. Click **[Save]** to apply the changes.

> **Note**: The Login ID cannot be changed after creation.

#### User Deactivation

Users who have resigned or no longer require access should be deactivated instead of deleted.

```bash
# User active/inactive toggle API
curl -X PATCH https://weighing.factory.internal/api/v1/users/10/toggle-active \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

A deactivated account returns the `AUTH_002` error (deactivated account) upon login attempt.

### 3.4 Password Policy

| Item | Policy |
|------|--------|
| Minimum length | 8 characters or more |
| Character requirements | Must include both letters and numbers |
| Hashing algorithm | bcrypt (cost factor 12) |
| Login failure lockout | Locked for 30 minutes after 5 consecutive failures |
| Initial password | Manually set by ADMIN and communicated to user |

#### Password Reset Procedure

When a user has lost their password:

1. The user (or management department) requests a password reset from the ADMIN.
2. The ADMIN searches for the target user on the **[User Management]** screen.
3. Click the **[Reset Password]** button.
4. Generate and set a temporary password.
5. Communicate the temporary password to the user through a secure channel (e.g., in person).
6. The user changes the password after logging in.

### 3.5 Account Lockout and Unlock

#### Automatic Lockout Conditions

- An account is automatically locked for 30 minutes after 5 consecutive login failures.
- While locked, login is denied even with the correct password.
- After 30 minutes, the lock is automatically released and the failure count is reset.

#### Manual Unlock

An ADMIN can immediately unlock a locked account.

```bash
# Account unlock API
curl -X POST https://weighing.factory.internal/api/v1/users/10/unlock \
  -H "Authorization: Bearer ${ADMIN_TOKEN}"
```

On the web interface, click the **[Unlock]** button next to the locked user in **[User Management]**.

#### Checking Lock Status

```sql
-- Check lock status directly in the database
SELECT user_id, login_id, user_name, failed_login_count, locked_until,
       CASE WHEN locked_until > NOW() THEN 'LOCKED'
            ELSE 'UNLOCKED' END AS lock_status
FROM tb_user
WHERE failed_login_count > 0 OR locked_until IS NOT NULL;
```

---

## 4. Security Management

### 4.1 JWT Authentication Structure

The system uses JWT (JSON Web Token)-based stateless authentication.

#### Token Composition

| Token Type | Expiration | Storage Location | Purpose |
|------------|-----------|-----------------|---------|
| Access Token | 30 minutes (1800s) | Client memory | API authentication |
| Refresh Token | 7 days | Redis (SHA-256 hash) | Access Token renewal |

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

#### Authentication Flow

```
[Client]                       [Nginx]              [Spring Boot]            [Redis]
    |                            |                       |                      |
    |-- POST /auth/login ------->|---proxy------------>  |                      |
    |                            |                       |-- User verification ->|
    |                            |                       |<- Return result ------|
    |                            |                       |-- Store Refresh Tkn ->|
    |<-- Access + Refresh Token--|<-- Issue tokens ------|                      |
    |                            |                       |                      |
    |-- API request (Bearer) --->|---proxy------------>  |                      |
    |                            |                       |-- JWT validation      |
    |                            |                       |-- Blacklist check --->|
    |                            |                       |<- Return result ------|
    |<-- API response -----------|<-- Return response ---|                      |
    |                            |                       |                      |
    |-- POST /auth/refresh ----->|---proxy------------>  |                      |
    |                            |                       |-- Verify Refresh Tkn>|
    |                            |                       |<- Compare stored val-|
    |<-- New Access Token -------|<-- Issue new token ---|                      |
```

#### Logout Processing

Two operations are performed during logout:
1. The Refresh Token is deleted from Redis to prevent renewal.
2. The Access Token's JTI is registered in the blacklist to block usage during its remaining validity period.

### 4.2 OTP Authentication Management

OTP is a security mechanism for mobile weighing when LPR license plate recognition fails.

#### OTP Processing Flow

```
[Weighbridge CS]   [API Server]       [Redis]          [Display Board]  [Mobile App]
    |                   |               |                 |                |
    |-- OTP gen req --->|               |                 |                |
    |                   |-- Gen 6-digit>|                 |                |
    |                   |   (TTL 5min)  |                 |                |
    |<-- OTP code ------|               |                 |                |
    |-- Display OTP --->|               |                 |                |
    |                   |               |               [123456]           |
    |                   |               |                 |                |
    |                   |               |                 |   OTP input    |
    |                   |<-- Verify req-|-----------------|----+---------->|
    |                   |-- Redis query>|                 |                |
    |                   |<- Session ret-|                 |                |
    |                   |-- Code verify |                 |                |
    |                   |-- On success ->| (delete key)   |                |
    |<-- Proceed weigh -|               |                 |                |
```

#### OTP Policy

| Item | Setting | Description |
|------|---------|-------------|
| Code length | 6-digit number | Generated with SecureRandom |
| Validity period (TTL) | 5 minutes (300s) | Managed by Redis TTL |
| Maximum failure attempts | 3 | OTP invalidated when exceeded |
| One-time use | Immediately deleted upon successful verification | Cannot be reused |

#### Redis OTP Key Structure

| Key Pattern | Value | TTL | Purpose |
|-------------|-------|-----|---------|
| `otp:code:{otpCode}` | JSON (session data) | 5 min | OTP session storage |
| `otp:scale:{scaleId}` | otpCode string | 5 min | Current OTP per weighbridge |
| `otp:fail:{otpCode}` | Failure count | 5 min | Failure count tracking |

#### OTP Failure Response

In the event of a Redis failure, the OTP service falls back to directly querying the `tb_otp_session` table in PostgreSQL.

### 4.3 Encryption Policy

| Target | Algorithm | Details |
|--------|-----------|---------|
| Passwords | bcrypt | Cost factor 12, one-way hash |
| Phone numbers (DB storage) | AES-256-GCM | IV + CipherText + Tag, Base64 encoded |
| Communication | TLS 1.3 | Nginx SSL termination |
| JWT signature | HMAC-SHA256 | Base64-encoded secret key |
| Refresh Token (Redis) | SHA-256 | Hash stored instead of original |

#### Encryption Key Management Guidelines

- AES keys and JWT secrets are managed as environment variables.
- Keys must be at least 256 bits (32 bytes).
- Key rotation cycle: annually or immediately upon a security incident.
- Key rotation requires re-encryption migration of existing encrypted data.

### 4.4 Access Control (RBAC)

Spring Security-based RBAC policies are applied as follows.

#### API Endpoint Access Control

| HTTP Method | Endpoint Pattern | Required Role |
|-------------|-----------------|---------------|
| POST | /api/v1/auth/login | No auth required |
| POST | /api/v1/auth/login/otp | No auth required |
| POST | /api/v1/auth/refresh | Refresh Token |
| POST | /api/v1/otp/verify | No auth required |
| POST | /api/v1/otp/generate | Internal API Key |
| DELETE | /api/v1/dispatches/** | ADMIN |
| POST, PUT | /api/v1/master/** | ADMIN |
| POST | /api/v1/dispatches | ADMIN, MANAGER |
| PUT | /api/v1/dispatches/** | ADMIN, MANAGER |
| ALL | /api/v1/gate-passes/** | ADMIN, MANAGER |
| GET | /api/v1/dispatches/my | DRIVER |
| ALL | /api/v1/** (other) | Authenticated user |
| ALL | /actuator/health | No auth required |

### 4.5 Audit Log Management

All security-related events are recorded in the audit log.

#### Recorded Events

| Event | Recorded Fields | Log Level |
|-------|----------------|-----------|
| Login success | userId, loginId, deviceType, IP, timestamp | INFO |
| Login failure | loginId, failure reason, failure count, IP | WARN |
| Account lockout | userId, loginId, lockedUntil | WARN |
| Logout | userId, loginId, deviceType | INFO |
| OTP generation | scaleId, vehicleId, plateNumber | INFO |
| OTP verification success | otpCode (masked), phoneNumber (masked), vehicleId | INFO |
| OTP verification failure | otpCode (masked), phoneNumber (masked), failure count | WARN |
| Permission denied | userId, request URI, required role | WARN |
| User create/modify | performer userId, target userId, change details | INFO |
| Dispatch change | performer userId, dispatchId, change details | INFO |
| Weighing data change | performer userId, weighingId, change details | INFO |

#### Audit Log Format

```
[AUDIT] {event} | userId={} | ip={} | detail={}
```

Examples:
```
[AUDIT] LOGIN_SUCCESS | userId=1 | ip=192.168.1.100 | detail=loginId=admin, device=WEB
[AUDIT] LOGIN_FAILED  | userId=null | ip=192.168.1.100 | detail=loginId=hong, reason=PASSWORD_MISMATCH, attempts=3
[AUDIT] ACCOUNT_LOCKED | userId=5 | ip=192.168.1.100 | detail=loginId=driver01, lockedUntil=2026-01-29T15:30:00
```

#### Audit Log Search

In the web admin interface, under **[System Management] > [Audit Logs]**, you can search by the following criteria:
- Date range (start date - end date)
- Event type
- User ID / name
- IP address

```sql
-- Direct DB query (if needed)
SELECT audit_id, event_type, user_id, ip_address, detail, created_at
FROM tb_audit_log
WHERE created_at BETWEEN '2026-01-01' AND '2026-01-31'
  AND event_type = 'LOGIN_FAILED'
ORDER BY created_at DESC
LIMIT 100;
```

### 4.6 Security Monitoring

#### Real-Time Monitoring Items

| Item | Threshold | Response |
|------|-----------|----------|
| Login failure frequency | > 10/min (same IP) | Review IP blocking, suspected brute force attack |
| Account lockout occurrences | > 5/hour | Suspected mass account attack, analyze patterns |
| 401/403 error frequency | > 50/min | Suspected authentication bypass attempt |
| OTP failure frequency | > 20/min | Suspected OTP brute force attack |
| Abnormal API call patterns | Rate limit exceeded | Auto-block (Nginx rate limiting) |

#### OWASP Top 10 Defense Status

| Threat | Defense Mechanism |
|--------|-------------------|
| SQL Injection | JPA Parameterized Query (PreparedStatement) |
| XSS | React auto-escaping + CSP header |
| CSRF | JWT-based stateless (no SameSite Cookie) |
| Broken Authentication | bcrypt hashing, account lockout, JWT expiry management |
| Security Misconfiguration | Environment-specific profile separation, security headers |
| Sensitive Data Exposure | AES-256 encryption, TLS 1.3, data masking |
| Broken Access Control | RBAC, @PreAuthorize annotations |
| Injection | Bean Validation, DTO validation |
| Insufficient Logging | Audit logs, ELK Stack |
| SSRF | Internal network access restriction, whitelist |

---

## 5. System Monitoring

### 5.1 Equipment Status Monitoring

The online/offline/error status of weighbridge site equipment is monitored in real time.

#### Monitored Equipment

| Equipment | Communication | Health Check Method | Check Interval |
|-----------|--------------|-------------------|----------------|
| LPR Camera | TCP | TCP connection check + capture test | 30s |
| LiDAR Sensor | TCP/UDP | Sensor data reception check | 10s |
| Radar Sensor | TCP/UDP | Sensor data reception check | 10s |
| Vehicle Detector | TCP/UDP | Event reception check | 10s |
| Indicator | RS-232C | Weight value reception check | 5s |
| Display Board | TCP/RS-485 | Communication status check | 30s |
| Auto Barrier | TCP/RS-485 | Status response check | 30s |

#### Equipment Status Display

The following statuses can be checked on the **[System Management] > [Equipment Monitoring]** dashboard in the web admin interface:

- **ONLINE (Normal)**: Equipment is communicating normally
- **OFFLINE**: Communication has been lost (3 consecutive health check failures)
- **ERROR**: Equipment responds but returns abnormal data
- **MAINTENANCE**: Operator has manually set maintenance mode

#### Equipment Health Check

Administrators can manually trigger an equipment health check via the web interface or API.

- Web: Check the **[Health Summary]** card on the Equipment Monitoring dashboard and click the **[Run Health Check]** button
- API: `POST /api/v1/monitoring/health-check` (ADMIN permission)
- Summary: `GET /api/v1/monitoring/summary` to view the overall equipment health summary

#### Equipment Failure Alerts

When equipment transitions to OFFLINE or ERROR status, alerts are automatically generated:
- Real-time dashboard alerts via WebSocket
- SMS/Kakao notifications sent to designated administrator contacts

### 5.2 Server Performance Monitoring (Prometheus + Grafana)

#### Prometheus Configuration

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

  # Node Exporter (Server OS metrics)
  - job_name: 'node-exporter'
    static_configs:
      - targets:
          - '10.x.x.1:9100'   # WAS server
          - '10.x.x.10:9100'  # DB server
          - '10.x.x.20:9100'  # Redis server
          - '10.x.x.30:9100'  # Nginx server

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

#### Key Alert Rules (alerts/weighing-alerts.yml)

```yaml
groups:
  - name: weighing-system-alerts
    rules:
      # API response time p95 > 1 second
      - alert: HighAPILatency
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{job="weighing-api"}[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "API response time p95 exceeds 1 second"

      # API error rate > 1%
      - alert: HighAPIErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) / rate(http_server_requests_seconds_count[5m]) > 0.01
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "API 5xx error rate exceeds 1%"

      # CPU usage > 80%
      - alert: HighCPUUsage
        expr: 100 - (avg by(instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100) > 80
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "CPU usage exceeds 80%: {{ $labels.instance }}"

      # Memory usage > 85%
      - alert: HighMemoryUsage
        expr: (1 - node_memory_MemAvailable_bytes / node_memory_MemTotal_bytes) * 100 > 85
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "Memory usage exceeds 85%: {{ $labels.instance }}"

      # Disk usage > 90%
      - alert: HighDiskUsage
        expr: (1 - node_filesystem_avail_bytes{fstype=~"ext4|xfs"} / node_filesystem_size_bytes{fstype=~"ext4|xfs"}) * 100 > 90
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Disk usage exceeds 90%: {{ $labels.instance }}"

      # PostgreSQL connections > 80%
      - alert: HighDBConnections
        expr: pg_stat_activity_count / pg_settings_max_connections * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "DB connection usage exceeds 80%"

      # Redis memory > 80%
      - alert: HighRedisMemory
        expr: redis_memory_used_bytes / redis_memory_max_bytes * 100 > 80
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Redis memory usage exceeds 80%"

      # Spring Boot instance down
      - alert: APIServerDown
        expr: up{job="weighing-api"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "API server instance down: {{ $labels.instance }}"
```

#### Grafana Dashboard Configuration

The following dashboards are configured for operations:

| Dashboard | Key Panels | Purpose |
|-----------|-----------|---------|
| System Overview | CPU, Memory, Disk, Network | Server infrastructure status |
| API Performance | Request Rate, Latency (p50/p95/p99), Error Rate | API performance |
| Database | Connections, Query Duration, TPS, Replication Lag | DB status |
| Redis | Memory Usage, Connected Clients, Hit Rate, Commands/s | Cache status |
| Business Metrics | Daily Weighings, LPR Recognition Rate, OTP Usage | Business metrics |

#### Grafana Access Information

```
URL: http://10.x.x.50:3000
Initial account: admin / (configured password)
```

### 5.3 Log Management (ELK Stack)

#### Log Architecture

```
[Spring Boot] --> [Logback JSON] --> [Filebeat] --> [Logstash] --> [Elasticsearch] --> [Kibana]
[Nginx]       --> [Access/Error Log] --> [Filebeat] -+
[PostgreSQL]  --> [PostgreSQL Log]   --> [Filebeat] -+
```

#### Logback Configuration (logback-spring.xml)

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

#### Filebeat Configuration

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

#### Log Search in Kibana

```
URL: http://10.x.x.50:5601

Key search patterns:
- Error logs: level:ERROR AND app:weighing-api
- Audit logs: message:"[AUDIT]*"
- Slow APIs: response_time:>1000
- Specific user: userId:10
- OTP related: message:"OTP*"
```

#### Log Retention Policy

| Log Type | Retention Period | Storage Location |
|----------|-----------------|------------------|
| Application logs | 90 days | ELK + local files |
| Audit logs | 1 year | ELK + DB (tb_audit_log) |
| Nginx access logs | 90 days | ELK + local files |
| PostgreSQL logs | 30 days | Local files |
| LPR images | 90 days | NAS file storage |

### 5.4 Alert Configuration

#### Alert Channel Configuration

| Channel | Target | Purpose |
|---------|--------|---------|
| Grafana Alert -> SMS | Operations staff | Server/DB failures (Critical) |
| Grafana Alert -> Kakao | Operations team group | Performance degradation warnings (Warning) |
| WebSocket Push | Web dashboard | Real-time equipment status changes |
| Email | Operations team | Daily operations report |

#### Alertmanager Configuration

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

## 6. Database Management

### 6.1 Table Structure and Relationships

#### Key Tables

| Table Name | Description | Key Relationships |
|-----------|-------------|-------------------|
| tb_user | Users (ADMIN/MANAGER/DRIVER) | tb_company (FK) |
| tb_company | Transport company master | tb_user, tb_vehicle, tb_dispatch (parent) |
| tb_vehicle | Vehicle master (LPR matching key) | tb_company (FK) |
| tb_dispatch | Dispatch records | tb_vehicle, tb_company, tb_user (FK) |
| tb_weighing | Weighing records | tb_dispatch, tb_vehicle, tb_scale (FK) |
| tb_weighing_slip | Electronic weighing slips | tb_weighing (FK) |
| tb_gate_pass | Gate pass records | tb_weighing, tb_dispatch (FK) |
| tb_otp_session | OTP sessions | tb_user, tb_vehicle (FK) |
| tb_scale | Weighbridge master | tb_weighing (parent) |
| tb_master_code | Common codes | Self-referencing (parent_code_id) |
| tb_audit_log | Audit logs | - |
| tb_notification | Notifications | tb_user (FK) |
| tb_inquiry_call | Inquiry call records | tb_user (FK) |

#### Index Overview

```sql
-- Query index list
SELECT schemaname, tablename, indexname, indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;
```

Key indexes:

| Table | Index | Column | Type |
|-------|-------|--------|------|
| tb_vehicle | idx_vehicle_plate | plate_number | B-Tree UNIQUE |
| tb_dispatch | idx_dispatch_date_status | dispatch_date, dispatch_status | B-Tree |
| tb_weighing | idx_weighing_dispatch | dispatch_id | B-Tree |
| tb_weighing | idx_weighing_date | weighed_at | B-Tree |
| tb_weighing_slip | idx_slip_number | slip_number | B-Tree UNIQUE |
| tb_user | idx_user_login | login_id | B-Tree UNIQUE |
| tb_otp_session | idx_otp_code_expires | otp_code, expires_at | B-Tree |

### 6.2 Backup and Recovery Procedures

#### Backup Policy

| Backup Type | Target | Method | Frequency | Retention Period |
|-------------|--------|--------|-----------|-----------------|
| Full backup | PostgreSQL | pg_dump | Daily at 02:00 | 30 days |
| WAL archive | PostgreSQL | Continuous archiving | Real-time | 7 days |
| RDB snapshot | Redis | RDB dump | Every 6 hours | 3 days |
| LPR images | NAS | rsync | Daily at 03:00 | 90 days |

#### Automated Backup Script (pg_backup.sh)

```bash
#!/bin/bash
# /opt/weighing/scripts/pg_backup.sh

BACKUP_DIR="/data/backup/postgresql"
DB_NAME="weighing"
DB_USER="weighing_app"
RETENTION_DAYS=30
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/weighing_${TIMESTAMP}.sql.gz"

# Verify backup directory
mkdir -p ${BACKUP_DIR}

# Execute pg_dump (Custom format, compressed)
echo "[$(date)] Backup started: ${BACKUP_FILE}"
pg_dump -h ${DB_HOST} -U ${DB_USER} -d ${DB_NAME} \
    --format=custom --compress=9 \
    --file=${BACKUP_FILE}

if [ $? -eq 0 ]; then
    echo "[$(date)] Backup successful: $(du -sh ${BACKUP_FILE})"
else
    echo "[$(date)] Backup failed!" >&2
    # Send alert
    exit 1
fi

# Delete old backups
find ${BACKUP_DIR} -name "weighing_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
echo "[$(date)] Cleaned up backup files older than ${RETENTION_DAYS} days"
```

#### Crontab Registration

```bash
# crontab -e
# Full PostgreSQL backup daily at 02:00
0 2 * * * /opt/weighing/scripts/pg_backup.sh >> /var/log/weighing/backup.log 2>&1
```

#### Recovery Procedure

```bash
# 1. Check backup file list
ls -lah /data/backup/postgresql/

# 2. Verify recovery target (table list)
pg_restore --list /data/backup/postgresql/weighing_20260129_020000.sql.gz

# 3. Full recovery (to a new DB)
createdb -h ${DB_HOST} -U postgres weighing_restored
pg_restore -h ${DB_HOST} -U postgres \
    -d weighing_restored \
    --clean --if-exists \
    /data/backup/postgresql/weighing_20260129_020000.sql.gz

# 4. Recover specific tables only
pg_restore -h ${DB_HOST} -U postgres \
    -d weighing \
    --table=tb_weighing \
    --data-only \
    /data/backup/postgresql/weighing_20260129_020000.sql.gz
```

### 6.3 Data Migration

This is the procedure for migrating data from the legacy system.

#### Migration Principles

1. **Phased migration**: Proceed in order: master data (transport companies, vehicles) -> historical data (dispatches, weighings).
2. **Parallel operations**: Operate in parallel with the existing system during the migration period.
3. **Validation phase**: Perform data integrity verification after each phase.
4. **Rollback plan**: Prepare rollback procedures for each phase in advance.

#### Migration Validation Queries

```sql
-- Compare record counts
SELECT 'tb_company' AS table_name, COUNT(*) AS cnt FROM tb_company
UNION ALL SELECT 'tb_vehicle', COUNT(*) FROM tb_vehicle
UNION ALL SELECT 'tb_user', COUNT(*) FROM tb_user
UNION ALL SELECT 'tb_dispatch', COUNT(*) FROM tb_dispatch
UNION ALL SELECT 'tb_weighing', COUNT(*) FROM tb_weighing;

-- Check for duplicate license plate numbers
SELECT plate_number, COUNT(*) AS cnt
FROM tb_vehicle
GROUP BY plate_number
HAVING COUNT(*) > 1;

-- Verify dispatch-weighing data integrity
SELECT d.dispatch_id, d.dispatch_status,
       COUNT(w.weighing_id) AS weighing_count
FROM tb_dispatch d
LEFT JOIN tb_weighing w ON d.dispatch_id = w.dispatch_id
WHERE d.dispatch_status = 'COMPLETED'
GROUP BY d.dispatch_id, d.dispatch_status
HAVING COUNT(w.weighing_id) = 0;
```

### 6.4 Performance Tuning

#### Slow Query Monitoring

```sql
-- Check queries taking over 3 seconds (pg_stat_statements extension)
SELECT query, calls, total_exec_time / calls AS avg_time_ms,
       rows / calls AS avg_rows
FROM pg_stat_statements
WHERE total_exec_time / calls > 3000
ORDER BY total_exec_time DESC
LIMIT 20;

-- Check currently running queries
SELECT pid, now() - pg_stat_activity.query_start AS duration,
       query, state
FROM pg_stat_activity
WHERE (now() - pg_stat_activity.query_start) > interval '5 seconds'
  AND state != 'idle';
```

#### Table Statistics Update

```sql
-- Update all table statistics (recommended weekly)
ANALYZE VERBOSE;

-- Update specific table statistics
ANALYZE tb_weighing;
ANALYZE tb_dispatch;
```

#### VACUUM Management

```sql
-- Check auto-vacuum status
SELECT schemaname, relname, n_dead_tup, last_autovacuum, last_autoanalyze
FROM pg_stat_user_tables
WHERE n_dead_tup > 1000
ORDER BY n_dead_tup DESC;

-- Manual VACUUM (if needed)
VACUUM (VERBOSE, ANALYZE) tb_weighing;
```

#### Connection Pool Monitoring

```sql
-- Check current connection status
SELECT state, COUNT(*)
FROM pg_stat_activity
GROUP BY state;

-- Check maximum connections
SHOW max_connections;

-- Check idle connections
SELECT pid, usename, client_addr, state, query_start
FROM pg_stat_activity
WHERE state = 'idle'
  AND query_start < now() - interval '30 minutes';
```

---

## 7. Master Data Management

### 7.1 Transport Company Management

Transport company (tb_company) information is master data that serves as the basis for dispatches, vehicles, and users.

#### Registering a Transport Company

Register the following information from the web admin interface under **[Master Data Management] > [Transport Company Management]**:

| Field | Required | Description |
|-------|:--------:|-------------|
| Company Name | O | Official company name |
| Business Registration No. | O | 10 digits |
| Company Type | O | By-product / Waste / Sub-material / Export / General |
| Representative Name | O | Name of representative |
| Contact Number | O | Main phone number |
| Address | X | Company location |
| Active Status | O | Default: Active |

#### Deactivating a Transport Company

Transport companies that have terminated their business relationship are deactivated instead of deleted. Vehicles and drivers belonging to a deactivated company are excluded from selection lists during dispatch registration.

### 7.2 Vehicle Management (LPR Matching)

Vehicle (tb_vehicle) information is the core reference data for LPR automatic matching.

#### Vehicle Registration

| Field | Required | Description |
|-------|:--------:|-------------|
| License Plate Number | O | LPR matching key. **Globally unique (UNIQUE)** |
| Affiliated Company | O | FK: tb_company |
| Vehicle Type | O | Dump truck / Cargo truck / Tanker, etc. |
| Max Load Weight (kg) | X | Overload warning threshold |
| Active Status | O | Default: Active |

> **Important**: The license plate number (plate_number) is the only key used to match LPR camera recognition results. If a license plate number changes (e.g., plate replacement), it must be updated in the system immediately. Otherwise, LPR automatic weighing will fail.

#### License Plate Number Format Management

```
Standard format examples:
  - 12ga3456    (Standard vehicle)
  - 123ga4567   (New numbering system)
  - Busan12ga3456 (Including region)
```

The format of the number recognized by the LPR camera must match the format stored in the database for successful matching. When registering, remove unnecessary characters such as spaces and hyphens, and store in standardized format.

#### Bulk Vehicle Registration

When a large number of vehicles need to be registered, use the Excel upload function:
1. Click **[Download Template]** on the **[Vehicle Management]** screen to get the form.
2. Enter data according to the form.
3. Upload the file using the **[Bulk Upload]** button.
4. Review validation results and correct any errors.
5. Confirm and execute the bulk registration.

### 7.3 Weighbridge Management

The weighbridge (tb_scale) contains master information for physical weighing equipment.

| Field | Required | Description |
|-------|:--------:|-------------|
| Weighbridge Name | O | E.g., "Weighbridge #1", "By-product Weighbridge" |
| Installation Location | O | Location within the plant |
| Weighbridge Type | O | SMART (automatic) / MANUAL |
| Max Capacity (kg) | X | Maximum weighable weight |
| Active Status | O | Operational status |
| Last Calibration Date | X | Date of last calibration |

#### Weighbridge Calibration Management

- Weighbridges must be calibrated regularly (in compliance with legally mandated calibration intervals).
- Calibration history is managed through the `last_calibrated_at` field.
- Configure alerts for approaching calibration expiration dates.

### 7.4 Common Code Management

Common codes (tb_master_code) centrally manage code values used throughout the system.

#### Key Code Groups

| Code Group | Description | Example Values |
|------------|-------------|----------------|
| ITEM_TYPE | Item type | By-product, Waste, Sub-material, Export, General |
| VEHICLE_TYPE | Vehicle type | Dump truck, Cargo truck, Tanker |
| DISPATCH_STATUS | Dispatch status | REGISTERED, IN_PROGRESS, COMPLETED, CANCELLED |
| WEIGHING_TYPE | Weighing sequence | FIRST, SECOND, THIRD |
| WEIGHING_MODE | Weighing method | LPR_AUTO, MOBILE_OTP, MANUAL, RE_WEIGH |
| WEIGHING_STATUS | Weighing status | IN_PROGRESS, COMPLETED, RE_WEIGHING, ERROR |
| INQUIRY_TYPE | Inquiry type | Logistics Control Room, Material Warehouse, Other |

#### Adding/Modifying Codes

1. Access the **[Master Data Management] > [Common Code Management]** screen.
2. Select a code group or create a new group.
3. Enter the code value, code name, and sort order.
4. Changes take effect immediately on related screens after saving (Redis cache refresh).

> **Caution**: Code values that are referenced by existing data cannot be deleted. Only deactivation is possible.

### 7.5 System Settings Management

System settings management (`/admin/settings`) allows adjustment of system-wide operational parameters. Only the ADMIN role has access.

#### System Settings Screen

1. Select **[System Management] > [System Settings]** from the left menu.
2. Setting items are displayed by category.
3. Modify setting values and click the **[Save]** button.
4. Individual item modification or **bulk modification** functionality is available.

> **Caution**: System setting changes take effect immediately. Record the current values before making changes and verify normal operation after changes. Setting change history is automatically recorded in the audit log.

### 7.6 Notice Management

Administrators can create, edit, and delete notices in the web system.

#### Creating a Notice

1. Click **[Notices]** in the left menu, then click the **[New Registration]** button.
2. Enter the following fields:

   | Field | Required | Description |
   |-------|:--------:|-------------|
   | Title | O | Notice title |
   | Category | O | Notice classification (System, Operations, Maintenance, Other, etc.) |
   | Content | O | Notice body (markdown supported) |
   | Attachments | X | Related file attachments |

3. Click the **[Save]** button. After saving, the notice is in **unpublished** status by default.

#### Publishing/Unpublishing Notices

- Click the **[Publish]** button on a notice in the list to make it visible to users.
- To make a published notice private, click the **[Unpublish]** button.

#### Pinning/Unpinning Notices

- Important notices can be pinned to the top of the list by clicking the **[Pin (📌)]** button.
- To unpin a notice, click the **[Unpin]** button.

### 7.7 FAQ Management

Administrators can create and manage Frequently Asked Questions (FAQ).

#### Creating a FAQ

1. Click **[Help]** in the left menu and select the **[FAQ Management]** tab.
2. Click the **[New Registration]** button.
3. Enter the following fields:

   | Field | Required | Description |
   |-------|:--------:|-------------|
   | Category | O | Question classification (Login, Dispatch, Weighing, Gate Pass, Other, etc.) |
   | Question | O | Frequently asked question |
   | Answer | O | Answer to the question |

4. Click the **[Save]** button.

#### Editing/Deleting FAQs

- Click the **[Edit]** or **[Delete]** button on the target item in the FAQ list.
- Deleted FAQs are immediately removed from the user-facing screen.

> **Note**: FAQs are exposed to users immediately upon registration, without a publish/unpublish setting.

---

## 8. Equipment Management

### 8.1 LPR Camera Management

#### LPR Camera Configuration

| Item | Setting |
|------|---------|
| Communication protocol | TCP |
| Integration method | Direct communication from Weighbridge CS program |
| Trigger | Capture command upon vehicle detection by LiDAR/Radar sensor |
| Output data | Captured image + initial recognized license plate number |
| Image storage | NAS path (90-day retention) |

#### LPR Recognition Rate Monitoring

```sql
-- Query daily LPR recognition rate
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

Target recognition rate: 95% or higher. If the recognition rate drops below 90%, check the following:
- LPR camera lens contamination
- Auxiliary lighting operation (nighttime)
- Camera angle and focus condition
- AI engine response time

### 8.2 LiDAR Sensor Management

| Item | Setting |
|------|---------|
| Communication protocol | TCP/UDP |
| Role | Vehicle approach detection -> LPR capture trigger |
| Detection range | Weighbridge entry lane (configured based on installation location) |
| Duplicate detection prevention | Minimum 10-second interval |

#### Sensor Inspection Items

- Sensor surface cleanliness (dust, foreign objects)
- Obstacles in detection area
- Power and cable connection status
- Normal communication data reception

### 8.3 Indicator (RS-232C) Management

The indicator is a device that displays the weight measured at the weighbridge and transmits it to the Weighbridge CS program.

#### Communication Settings

| Item | Value |
|------|-------|
| Port | COM port (e.g., COM1, COM3) |
| Baud Rate | 9600 (varies by device) |
| Data Bits | 8 |
| Parity | None |
| Stop Bits | 1 |
| Flow Control | None |
| Timeout | 3 seconds |
| Retries | 3 |

#### Indicator Failure Response

| Symptom | Cause | Action |
|---------|-------|--------|
| No weight value received | RS-232C cable disconnected | Replace cable or check connection |
| Unstable values received | Poor contact | Reconnect connector, check serial converter |
| Communication timeout | COM port conflict | Check port in Device Manager |
| Abnormal data | Communication setting mismatch | Verify baud rate and other parameters |

```
RS-232C communication failure response procedure:
1. Check indicator status in the Weighbridge CS program
2. Verify COM port connection status (Device Manager)
3. Check physical connection of serial cable
4. Verify communication parameter (baud rate) match
5. Check Serial-USB converter driver status if applicable
6. Test with a spare converter
7. If unresolved, contact the indicator manufacturer for support
```

### 8.4 Display Board Management

The display board shows OTP codes and other guidance information to vehicles entering the weighbridge.

| Item | Setting |
|------|---------|
| Communication | TCP or RS-485 |
| Display content | 6-digit OTP, license plate number, guidance messages |
| Control | Sent from the Weighbridge CS program |

#### Display Board Inspection Items

- LED brightness and readability (daytime/nighttime)
- Communication connection status
- OTP code display verification
- Korean character display verification

### 8.5 Auto Barrier Management

The auto barrier physically controls entry/exit at the weighbridge.

| Item | Setting |
|------|---------|
| Communication | TCP or RS-485 |
| Control commands | Open (OPEN) / Close (CLOSE) |
| Safety device | Prevents closing when a vehicle is detected (safety sensor) |
| Fallback | Manual opening available during communication failure |

#### Barrier Emergency Operation

When automatic control is unavailable due to communication failure or other issues:
1. Use the manual switch on the barrier control box to open the barrier.
2. Switch to manual mode and keep the barrier in a fixed open state.
3. Identify and resolve the root cause, then return to automatic mode.
4. Record the emergency operation history.

---

## 9. Incident Response

### 9.1 Incident Response by Failure Type

#### Incident Severity Classification

| Severity | Definition | Response Time | Examples |
|----------|-----------|---------------|----------|
| Critical | Complete service outage | Immediate (recovery begins within 30 min) | All WAS down, DB failure, network disconnection |
| Major | Major feature failure | Within 1 hour | One WAS down, LPR failure, Redis failure |
| Minor | Partial performance degradation | Within 4 hours | Performance degradation, individual sensor failure, notification delivery failure |
| Info | No service impact | Next business day | Log warnings, disk usage increase |

#### WAS Server Failure

```
Detection: Nginx health check failure (max_fails=3, fail_timeout=30s)
Auto-response: Nginx automatically removes failed server from upstream (Active-Active)
RTO: < 30 seconds (automatic failover)

Manual recovery procedure:
1. Check failed server status
   $ docker ps -a | grep weighing-api
   $ docker logs weighing-api-1 --tail 100

2. Restart container
   $ docker compose restart weighing-api-1

3. Verify health check
   $ curl -s http://localhost:8080/actuator/health | python3 -m json.tool

4. Root cause analysis
   - Check application logs: /data/weighing/logs/
   - Check JVM memory: /actuator/metrics/jvm.memory.used
   - Check OOM Killer: dmesg | grep -i oom

5. Verify server recovery in Nginx
   $ curl -s http://localhost/api/v1/health
```

#### Database Failure

```
Detection: Spring Boot connection failure, Prometheus pg_up == 0

PostgreSQL Primary failure - Standby promotion procedure:
RTO: < 30 minutes (manual promotion)

1. Check Primary status
   $ pg_isready -h 10.x.x.10 -p 5432
   $ sudo systemctl status postgresql-16

2. Attempt Primary recovery
   $ sudo systemctl restart postgresql-16
   $ tail -100 /var/lib/pgsql/16/data/log/postgresql-*.log

3. If Primary cannot be recovered, promote Standby
   -- Execute on Standby server:
   $ pg_ctl promote -D /var/lib/pgsql/16/data

4. Update application DB connection information
   -- Change DB_HOST to Standby IP in .env file
   $ vi /opt/weighing/.env
   DB_HOST=10.x.x.11    # Change to Standby IP

5. Restart applications
   $ docker compose restart weighing-api-1 weighing-api-2

6. Verify data integrity
   $ psql -h 10.x.x.11 -U weighing_app -d weighing -c "SELECT COUNT(*) FROM tb_weighing;"

7. After original Primary is recovered, reconfigure as Standby
```

#### Redis Failure

```
Detection: Spring Boot Redis connection failure, redis_up == 0

Automatic fallback:
- Refresh Token verification: JWT self-verification only (blacklist disabled)
- OTP sessions: Direct DB (tb_otp_session) query
RTO: < 10 minutes

Manual recovery procedure:
1. Check Redis status
   $ redis-cli -h 10.x.x.20 ping
   $ sudo systemctl status redis

2. Restart Redis
   $ sudo systemctl restart redis

3. Verify connection
   $ redis-cli -h 10.x.x.20 -a '<password>' info server

4. Cache warm-up (if needed)
   - Master data cache is automatically reloaded on API calls
   - Active sessions require user re-login
```

#### LPR Equipment Failure

```
Detection: CS program detects LPR communication loss

Immediate response:
1. Warning displayed in Weighbridge CS program
2. Automatic guidance to manual weighing mode
3. Display board shows "Manual Weighing Mode" message

Recovery procedure:
1. Check LPR camera power and network
2. Check camera status in LPR management software
3. TCP connection test
4. Restart camera (power OFF/ON)
5. Attempt LPR reconnection from CS program
6. If recovery fails, request manufacturer technical support

Alternative operations:
- Switch to mobile OTP weighing
- Use manual weighing mode (touchscreen) in parallel
```

#### Network Failure (Weighbridge-Server)

```
Detection: CS program API server communication failure

Automatic response:
1. Weighbridge CS program switches to local caching mode
2. Weighing data temporarily stored in local SQLite
3. Automatic synchronization after network recovery
RTO: < 1 hour (automatic sync)

Manual recovery procedure:
1. Check network connection status
   $ ping 10.x.x.1 (WAS server)
   $ traceroute 10.x.x.1

2. Check VPN connection status (if applicable)

3. Check network equipment (switches, routers)

4. After network recovery, verify data synchronization status
   - Check pending synchronization count in CS program
   - Verify server data integrity after synchronization completes
```

### 9.2 System Recovery Procedure

#### Full System Recovery Order

In case of a full system failure, recover in the following order:

```
Phase 1: Infrastructure Recovery
  1. Check and recover network equipment
  2. Verify server hardware

Phase 2: Data Layer Recovery
  3. Start PostgreSQL and verify data integrity
  4. Start Redis and verify connection

Phase 3: Application Recovery
  5. Start Spring Boot WAS #1
  6. Verify WAS #1 health check
  7. Start Spring Boot WAS #2
  8. Verify WAS #2 health check

Phase 4: Frontend and Proxy Recovery
  9. Start Nginx and verify configuration
  10. Test web UI access

Phase 5: On-Site Equipment Recovery
  11. Start Weighbridge CS program
  12. Verify LPR/sensor/indicator communication
  13. Verify display board/barrier operation

Phase 6: Validation
  14. Perform E2E weighing test
  15. Verify local cache data synchronization
  16. Clear monitoring system alerts
```

### 9.3 Rollback Procedures

#### Application Rollback

```bash
# Check current version
docker images | grep weighing-api

# Rollback to previous version (complete within 5 minutes)
export ROLLBACK_VERSION="previous_version_tag"

# Rollback both WAS instances simultaneously
docker compose stop weighing-api-1 weighing-api-2
APP_VERSION=${ROLLBACK_VERSION} docker compose up -d weighing-api-1 weighing-api-2

# Verify health check
sleep 30
curl -sf http://localhost:8080/actuator/health && echo "WAS#1 OK" || echo "WAS#1 FAIL"
curl -sf http://localhost:8081/actuator/health && echo "WAS#2 OK" || echo "WAS#2 FAIL"
```

#### Database Rollback

For deployments that include DB schema changes, follow this rollback procedure:

1. Rollback the application first (deploy previous version image).
2. Execute the rollback SQL for DB changes (prepared in advance).
3. Verify data integrity.

> **Caution**: Since the production environment uses `ddl-auto: none`, DB schema changes are performed only through manual migrations. Always prepare rollback SQL in advance.

### 9.4 Emergency Contact List

| Category | Responsible | Contact | Scope |
|----------|------------|---------|-------|
| System Operations | System Administrator (Primary) | Ext. XXXX | Server, DB, network |
| System Operations | System Administrator (Secondary) | Ext. XXXX | Server, DB, network |
| Application | Development Team | Ext. XXXX | Software issues, deployment |
| On-Site Equipment | DMES Team | Ext. XXXX | LPR, sensors, indicator |
| Network | Infrastructure Team | Ext. XXXX | Network equipment, VPN |
| DB Expert | DBA | Ext. XXXX | DB recovery, performance issues |
| LPR Manufacturer | External Vendor | Contact XXXX | LPR hardware failures |
| Indicator Manufacturer | External Vendor | Contact XXXX | Weighing equipment hardware |

> **Note**: The emergency contact list should be validated and updated at least quarterly.

---

## 10. Backup and Recovery

### 10.1 Backup Policy

| Target | Method | Frequency | Retention Period | Storage Location |
|--------|--------|-----------|-----------------|------------------|
| PostgreSQL full | pg_dump (Custom) | Daily at 02:00 | 30 days | /data/backup/postgresql/ |
| PostgreSQL WAL | WAL archive | Real-time | 7 days | /data/pg_archive/ |
| Redis RDB | RDB Snapshot | Every 6 hours | 3 days | /var/lib/redis/ |
| LPR images | NAS rsync | Daily at 03:00 | 90 days | NAS storage |
| Application logs | ELK collection | Real-time | 90 days | Elasticsearch |
| Nginx configuration | Git managed | On change | Indefinite | GitLab |
| Docker Compose | Git managed | On change | Indefinite | GitLab |
| .env (encrypted) | Manual backup | On change | Indefinite | Stored in separate secure vault |

### 10.2 Automated Backup Configuration

#### PostgreSQL Automated Backup (crontab)

```bash
# crontab -l (weighing-admin account)

# PostgreSQL full backup (daily at 02:00)
0 2 * * * /opt/weighing/scripts/pg_backup.sh >> /var/log/weighing/backup.log 2>&1

# Redis RDB backup copy (every 6 hours)
0 */6 * * * cp /var/lib/redis/dump.rdb /data/backup/redis/dump_$(date +\%Y\%m\%d_\%H\%M).rdb 2>&1

# LPR image NAS synchronization (daily at 03:00)
0 3 * * * rsync -avz --delete /data/weighing/lpr-images/ /mnt/nas/weighing/lpr-images/ >> /var/log/weighing/lpr-sync.log 2>&1

# Old backup cleanup (every Sunday at 04:00)
0 4 * * 0 /opt/weighing/scripts/cleanup_old_backups.sh >> /var/log/weighing/cleanup.log 2>&1
```

#### Backup Verification Script

```bash
#!/bin/bash
# /opt/weighing/scripts/verify_backup.sh
# Perform backup restoration test once a week

LATEST_BACKUP=$(ls -t /data/backup/postgresql/weighing_*.sql.gz | head -1)
TEST_DB="weighing_backup_test"

echo "[$(date)] Backup verification started: ${LATEST_BACKUP}"

# Create test DB and restore
dropdb -h localhost -U postgres --if-exists ${TEST_DB}
createdb -h localhost -U postgres ${TEST_DB}
pg_restore -h localhost -U postgres -d ${TEST_DB} ${LATEST_BACKUP}

if [ $? -eq 0 ]; then
    # Verify key table record counts
    psql -h localhost -U postgres -d ${TEST_DB} -c "
        SELECT 'tb_user' AS tbl, COUNT(*) AS cnt FROM tb_user
        UNION ALL SELECT 'tb_dispatch', COUNT(*) FROM tb_dispatch
        UNION ALL SELECT 'tb_weighing', COUNT(*) FROM tb_weighing
        UNION ALL SELECT 'tb_vehicle', COUNT(*) FROM tb_vehicle;
    "
    echo "[$(date)] Backup verification successful"
else
    echo "[$(date)] Backup verification failed!" >&2
fi

# Delete test DB
dropdb -h localhost -U postgres ${TEST_DB}
```

### 10.3 Recovery Procedures

#### PITR (Point-in-Time Recovery)

WAL archives can be used to recover to a specific point in time.

```bash
# 1. Stop PostgreSQL
sudo systemctl stop postgresql-16

# 2. Backup data directory
sudo mv /var/lib/pgsql/16/data /var/lib/pgsql/16/data_backup

# 3. Restore from base backup
sudo -u postgres pg_basebackup -D /var/lib/pgsql/16/data -R

# 4. Set recovery target time in recovery.conf (or postgresql.auto.conf)
echo "recovery_target_time = '2026-01-29 14:00:00+09'" >> /var/lib/pgsql/16/data/postgresql.auto.conf
echo "restore_command = 'cp /data/pg_archive/%f %p'" >> /var/lib/pgsql/16/data/postgresql.auto.conf

# 5. Start PostgreSQL (recovery mode)
sudo systemctl start postgresql-16

# 6. Verify recovery completion and check timeline
sudo -u postgres psql -c "SELECT pg_is_in_recovery();"
# Result: f (false) -> Recovery complete, promoted to Primary
```

### 10.4 Disaster Recovery Plan (DR)

#### RPO / RTO Targets

| Metric | Target | Method |
|--------|--------|--------|
| RPO (Recovery Point Objective) | Within 1 hour | Real-time WAL archiving |
| RTO (Recovery Time Objective) | Within 1 hour | Standby DB + Docker images |

#### DR Scenarios

| Scenario | Response | RTO |
|----------|----------|-----|
| Single WAS failure | Nginx automatic failover | < 30 seconds |
| All WAS failure | Redeploy with Docker images | < 10 minutes |
| DB Primary failure | Manual Standby promotion | < 30 minutes |
| Redis failure | Redis restart + DB fallback | < 10 minutes |
| Server room fire/flood | Build new server from offsite backup | < 24 hours |
| Full system failure | Switch to manual operations (parallel legacy process) | Immediate |

#### DR Drills

- **Frequency**: Semi-annually
- **Scope**: DB Standby promotion, backup restoration, manual operations switchover
- **Documentation**: Document drill results and incorporate improvements

---

## 11. Performance Management

### 11.1 Performance Metrics and Targets

| Metric | Target | Measurement Method |
|--------|--------|-------------------|
| LPR recognition -> auto weighing completion | < 3s (E2E) | Application log timestamps |
| API response time p50 | < 200ms | Prometheus + Spring Actuator |
| API response time p95 | < 500ms | Prometheus + Spring Actuator |
| API response time p99 | < 1,000ms | Prometheus + Spring Actuator |
| Web FCP (First Contentful Paint) | < 2s | Lighthouse |
| Web LCP (Largest Contentful Paint) | < 3s | Lighthouse |
| Concurrent web users | 50+ | Load testing |
| Concurrent mobile users | 200+ | Load testing |
| Daily weighing transactions | 500+ | DB aggregation |
| API TPS (normal) | 100 TPS | Prometheus |
| API TPS (peak) | 300 TPS | Prometheus |

### 11.2 Performance Monitoring Methods

#### API Response Time Monitoring

```
Grafana Dashboard: API Performance
Panels:
  - Request Rate (req/s) by endpoint
  - Latency p50 / p95 / p99 by endpoint
  - Error Rate (4xx, 5xx) by endpoint
  - Active Connections
```

#### JVM Memory Monitoring

```bash
# Check JVM metrics via Actuator
curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | python3 -m json.tool
curl -s http://localhost:8080/actuator/metrics/jvm.gc.pause | python3 -m json.tool

# Key monitoring items:
# - jvm.memory.used: Heap usage
# - jvm.gc.pause: GC pause time
# - jvm.threads.live: Active thread count
```

#### DB Connection Pool Monitoring

```bash
# Check HikariCP metrics
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | python3 -m json.tool
curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.pending | python3 -m json.tool

# Key monitoring items:
# - hikaricp.connections.active: Active connections (max: 30)
# - hikaricp.connections.pending: Pending connection requests
# - hikaricp.connections.timeout: Connection timeout occurrences
```

### 11.3 Optimization Guide

#### Redis Cache Optimization

Current caching targets:

| Cache Target | Key Pattern | TTL | Invalidation Condition |
|-------------|-------------|-----|----------------------|
| Master data (companies) | cache:company:* | 5 min | On company info change |
| Master data (vehicles) | cache:vehicle:* | 5 min | On vehicle info change |
| Dispatch list | cache:dispatch:* | 1 min | On dispatch create/change |
| Common codes | cache:code:* | 30 min | On code change |

If the cache hit rate drops below 80%, review TTL adjustments or cache strategy.

```bash
# Check Redis cache hit rate
redis-cli -h 10.x.x.20 -a '<password>' info stats | grep keyspace
# keyspace_hits / (keyspace_hits + keyspace_misses) = hit rate
```

#### DB Query Optimization

```sql
-- Check index usage
SELECT schemaname, tablename, indexrelname, idx_scan, idx_tup_read, idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan ASC
LIMIT 20;

-- Check unused indexes (removal candidates)
SELECT schemaname, tablename, indexrelname
FROM pg_stat_user_indexes
WHERE idx_scan = 0
  AND schemaname = 'public';

-- Sequential scan vs index scan ratio by table
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

#### Nginx Performance Tuning

```nginx
# Production environment tuning points

# Worker processes (match CPU core count)
worker_processes auto;

# Connections (below ulimit -n value)
worker_connections 2048;

# File transfer optimization
sendfile on;
tcp_nopush on;
tcp_nodelay on;

# Buffer settings
proxy_buffer_size 128k;
proxy_buffers 4 256k;
proxy_busy_buffers_size 256k;

# Keepalive
keepalive_timeout 65;
upstream weighing_api {
    keepalive 32;   # upstream keepalive connection count
}
```

---

## 12. Operational Checklists

### 12.1 Daily Inspection Items

Inspect the following items at the start of each business day (08:00).

| No. | Inspection Item | Method | Normal Criteria |
|-----|----------------|--------|----------------|
| D-01 | WAS server status | Actuator /health check | All instances UP |
| D-02 | DB connection status | pg_isready execution | Normal response |
| D-03 | Redis connection status | redis-cli ping | PONG response |
| D-04 | Nginx status | systemctl status nginx | active (running) |
| D-05 | Disk usage | df -h | < 80% |
| D-06 | Overnight backup result | backup.log check | Success log exists |
| D-07 | Error log review | Kibana ERROR search | No abnormal errors |
| D-08 | Equipment status dashboard | Web equipment monitoring | All equipment ONLINE |
| D-09 | LPR recognition rate | Previous day statistics | >= 95% |
| D-10 | Previous day weighing count | Dashboard check | Within normal range |

#### Daily Inspection Script

```bash
#!/bin/bash
# /opt/weighing/scripts/daily_check.sh

echo "=========================================="
echo " Busan Smart Weighing System Daily Check"
echo " $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

# WAS status
echo ""
echo "[D-01] WAS Server Status"
for port in 8080 8081; do
    STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${port}/actuator/health)
    if [ "$STATUS" = "200" ]; then
        echo "  WAS (${port}): OK"
    else
        echo "  WAS (${port}): FAIL (HTTP ${STATUS})"
    fi
done

# DB status
echo ""
echo "[D-02] PostgreSQL Status"
pg_isready -h ${DB_HOST} -p 5432 && echo "  DB: OK" || echo "  DB: FAIL"

# Redis status
echo ""
echo "[D-03] Redis Status"
redis-cli -h ${REDIS_HOST} -a "${REDIS_PASSWORD}" ping 2>/dev/null && echo "  Redis: OK" || echo "  Redis: FAIL"

# Disk usage
echo ""
echo "[D-05] Disk Usage"
df -h | grep -E '^/dev/' | awk '{print "  " $6 ": " $5}'

# Backup result
echo ""
echo "[D-06] Recent Backups"
ls -lah /data/backup/postgresql/ | tail -3

# Error log count (last 24 hours)
echo ""
echo "[D-07] Error Log Count (Last 24 Hours)"
if [ -f /data/weighing/logs/weighing-api.json ]; then
    ERROR_COUNT=$(grep -c '"level":"ERROR"' /data/weighing/logs/weighing-api.json 2>/dev/null || echo "0")
    echo "  ERROR count: ${ERROR_COUNT}"
fi

echo ""
echo "=========================================="
echo " Inspection Complete"
echo "=========================================="
```

### 12.2 Weekly Inspection Items

Performed every Monday morning.

| No. | Inspection Item | Method | Normal Criteria |
|-----|----------------|--------|----------------|
| W-01 | DB table statistics update | Run ANALYZE | Completed successfully |
| W-02 | Slow query review | pg_stat_statements query | No queries exceeding 3s |
| W-03 | Redis memory trend | Grafana Redis dashboard | < 80% |
| W-04 | Log disk usage | du -sh /data/weighing/logs | < 70% |
| W-05 | SSL certificate expiry | openssl x509 check | 30+ days remaining |
| W-06 | Docker image cleanup | docker image prune | Unused images removed |
| W-07 | Weekly LPR recognition statistics | DB aggregation query | >= 95% |
| W-08 | Account lockout history | Audit log review | No abnormal patterns |
| W-09 | Prometheus alert history | Alertmanager review | Critical alert causes resolved |
| W-10 | Backup restoration verification | Run verify_backup.sh | Restoration successful |

### 12.3 Monthly Inspection Items

Performed during the first week of each month.

| No. | Inspection Item | Method | Normal Criteria |
|-----|----------------|--------|----------------|
| M-01 | OS security patches | dnf update --security | Latest patches applied |
| M-02 | DB VACUUM execution | VACUUM ANALYZE | Completed successfully |
| M-03 | Expired OTP session cleanup | Delete records older than 90 days | Cleanup complete |
| M-04 | User account audit | Check inactive accounts, former employees | Unnecessary accounts deactivated |
| M-05 | Performance trend analysis | Grafana monthly dashboard | Within target range |
| M-06 | Log retention policy verification | Delete logs exceeding retention period | Policy compliant |
| M-07 | Equipment physical inspection | On-site equipment appearance/cleanliness | No issues |
| M-08 | Emergency contact list update | Verify contact validity | Up to date |

#### Monthly OTP Session Cleanup Query

```sql
-- Delete OTP sessions older than 90 days
DELETE FROM tb_otp_session
WHERE created_at < NOW() - INTERVAL '90 days';

-- Check deleted count
-- DELETE n (n rows deleted)
```

### 12.4 Quarterly Inspection Items

Performed quarterly (March, June, September, December).

| No. | Inspection Item | Method | Normal Criteria |
|-----|----------------|--------|----------------|
| Q-01 | DR drill execution | DB Standby promotion test | Recovery within RTO target |
| Q-02 | Security vulnerability assessment | OWASP checklist review | No unresolved vulnerabilities |
| Q-03 | Encryption key inspection | Key validity and strength check | Policy compliant |
| Q-04 | Load testing | Load test with JMeter or similar | Performance targets met |
| Q-05 | Weighbridge calibration check | Calibration history and expiry review | Valid calibration maintained |
| Q-06 | Documentation update | Operations manual update | Current documentation |
| Q-07 | Emergency contact list verification | Conduct contact test | All contacts reachable |
| Q-08 | Capacity planning review | Disk/memory/CPU trend analysis | No shortage expected within 6 months |

---

## 13. Appendix

### 13.1 API Endpoint List

#### Authentication API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/auth/login | ID/PW login | Not required |
| POST | /api/v1/auth/login/otp | OTP-based login (mobile) | Not required |
| POST | /api/v1/auth/refresh | Access Token renewal | Refresh Token |
| POST | /api/v1/auth/logout | Logout | Required |

#### OTP API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/otp/generate | OTP generation (CS program) | Internal Key |
| POST | /api/v1/otp/verify | OTP verification (mobile) | Not required |

#### Dispatch Management API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/dispatches | Dispatch list query | Required |
| POST | /api/v1/dispatches | Create dispatch | ADMIN, MANAGER |
| GET | /api/v1/dispatches/{id} | Dispatch detail query | Required |
| PUT | /api/v1/dispatches/{id} | Update dispatch | ADMIN, MANAGER |
| DELETE | /api/v1/dispatches/{id} | Delete dispatch | ADMIN |
| GET | /api/v1/dispatches/my | My dispatch list | DRIVER |

#### Weighing Management API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/weighings | Start weighing | Required |
| PUT | /api/v1/weighings/{id}/complete | Complete weighing | Required |
| PUT | /api/v1/weighings/{id}/re-weigh | Re-weigh processing | Required |
| GET | /api/v1/weighings | Weighing records list | Required |
| GET | /api/v1/weighings/{id} | Weighing detail query | Required |
| GET | /api/v1/weighings/realtime | Real-time weighing status (WebSocket) | Required |
| GET | /api/v1/weighings/statistics | Weighing statistics | Required |

#### Electronic Weighing Slip API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/slips/{weighingId} | Electronic weighing slip query | Required |
| POST | /api/v1/slips/{slipId}/share | Share weighing slip (Kakao/SMS) | Required |
| GET | /api/v1/slips/history | Weighing slip history | Required |

#### Gate Pass Management API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/gate-passes | Gate pass list query | ADMIN, MANAGER |
| POST | /api/v1/gate-passes | Create gate pass | MANAGER |
| PUT | /api/v1/gate-passes/{id} | Update gate pass status | MANAGER |

#### Master Data API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/master/companies | Transport company list | Required |
| POST | /api/v1/master/companies | Register transport company | ADMIN |
| GET | /api/v1/master/vehicles | Vehicle list | Required |
| POST | /api/v1/master/vehicles | Register vehicle | ADMIN |
| GET | /api/v1/master/scales | Weighbridge list | Required |
| GET | /api/v1/master/codes/{group} | Common code query | Required |

#### Notification API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/notifications | Notification list | Required |
| PUT | /api/v1/notifications/{id}/read | Mark notification as read | Required |
| POST | /api/v1/notifications/push/register | Register FCM token | Required |

#### User Management API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/users | Create user | ADMIN |
| GET | /api/v1/users | User list | ADMIN, MANAGER |
| GET | /api/v1/users/{id} | User query | ADMIN, MANAGER |
| PATCH | /api/v1/users/{id}/toggle-active | Toggle active/inactive | ADMIN |
| POST | /api/v1/users/{id}/unlock | Unlock account | ADMIN |
| PATCH | /api/v1/users/{id}/role | Change role | ADMIN |
| POST | /api/v1/users/{id}/reset-password | Reset password | ADMIN |
| DELETE | /api/v1/users/{id} | Delete user | ADMIN |

#### Inquiry/Call API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/inquiries/contacts | Inquiry contact list | Required |
| POST | /api/v1/inquiries/call-log | Record call history | Required |
| GET | /api/v1/inquiries/call-log | Query call history | ADMIN, MANAGER |
| GET | /api/v1/inquiries/call-log/my | Query my call history | Required |

#### My Page API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/mypage | My profile query | Required |
| PUT | /api/v1/mypage/profile | Update profile | Required |
| PUT | /api/v1/mypage/password | Change password | Required |
| PUT | /api/v1/mypage/notifications | Change notification settings | Required |

#### Favorites API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/favorites | Favorites list query | Required |
| GET | /api/v1/favorites/type/{type} | Favorites by type (DISPATCH, COMPANY, VEHICLE) | Required |
| POST | /api/v1/favorites | Add to favorites | Required |
| POST | /api/v1/favorites/toggle | Toggle favorite | Required |
| GET | /api/v1/favorites/check | Check favorite status | Required |
| PUT | /api/v1/favorites/reorder | Reorder favorites | Required |
| DELETE | /api/v1/favorites/{id} | Delete favorite | Required |

#### Notice API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/notices | Published notice list | Required |
| GET | /api/v1/notices/{id} | Notice detail | Required |
| GET | /api/v1/notices/pinned | Pinned notice list | Required |
| GET | /api/v1/notices/category/{category} | Query by category | Required |
| GET | /api/v1/notices/search | Search notices | Required |
| GET | /api/v1/notices/admin | All notices (published/unpublished) | ADMIN |
| POST | /api/v1/notices | Create notice | ADMIN |
| PUT | /api/v1/notices/{id} | Update notice | ADMIN |
| DELETE | /api/v1/notices/{id} | Delete notice | ADMIN |
| PATCH | /api/v1/notices/{id}/publish | Toggle publish/unpublish | ADMIN |
| PATCH | /api/v1/notices/{id}/pin | Toggle pin/unpin | ADMIN |

#### FAQ Management API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/help/faqs | FAQ list | Not required |
| GET | /api/v1/help/faqs/{id} | FAQ detail | Not required |
| GET | /api/v1/help/faqs/category/{category} | FAQs by category | Not required |
| GET | /api/v1/help/faqs/admin | Full FAQ list (admin) | ADMIN |
| POST | /api/v1/help/faqs | Create FAQ | ADMIN |
| PUT | /api/v1/help/faqs/{id} | Update FAQ | ADMIN |
| DELETE | /api/v1/help/faqs/{id} | Delete FAQ | ADMIN |

#### System Settings API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/admin/settings | All system settings query | ADMIN |
| GET | /api/v1/admin/settings/category/{category} | Settings by category | ADMIN |
| PUT | /api/v1/admin/settings/{id} | Update individual setting | ADMIN |
| PUT | /api/v1/admin/settings/bulk | Bulk update settings | ADMIN |

#### Equipment Monitoring API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/monitoring/devices | All equipment status query | Required |
| GET | /api/v1/monitoring/devices/type/{deviceType} | Query by equipment type | Required |
| GET | /api/v1/monitoring/devices/{deviceId} | Individual equipment status | Required |
| PUT | /api/v1/monitoring/devices/{deviceId}/status | Update equipment status | ADMIN, MANAGER |
| GET | /api/v1/monitoring/summary | Equipment health summary | Required |
| POST | /api/v1/monitoring/health-check | Trigger equipment health check | ADMIN |

#### Statistics API (Supplementary)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/statistics/daily | Daily statistics | Required |
| GET | /api/v1/statistics/monthly | Monthly statistics | Required |
| GET | /api/v1/statistics/summary | Summary statistics | Required |
| GET | /api/v1/statistics/export | Excel export | Required |

#### Dashboard API (Supplementary)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/dashboard/summary | Today's weighing summary (total, in-progress, completed) | Required |
| GET | /api/v1/dashboard/company-stats | Company-wise weighing statistics | Required |

#### Gate Pass Management API (Supplementary)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/gate-passes | Gate pass list query | ADMIN, MANAGER |
| GET | /api/v1/gate-passes/{id} | Gate pass detail | ADMIN, MANAGER |
| GET | /api/v1/gate-passes?status= | Query by status (default PENDING) | ADMIN, MANAGER |
| POST | /api/v1/gate-passes | Create gate pass request | ADMIN, MANAGER |
| PUT | /api/v1/gate-passes/{id}/pass | Approve gate pass | ADMIN, MANAGER |
| PUT | /api/v1/gate-passes/{id}/reject | Reject gate pass | ADMIN, MANAGER |

#### LPR API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/v1/lpr/capture | Register LPR capture event | Internal Key |
| POST | /api/v1/lpr/verify | AI license plate verification | Internal Key |
| POST | /api/v1/lpr/{captureId}/match | Dispatch matching | Internal Key |
| GET | /api/v1/lpr/{captureId} | Capture event detail | Required |
| GET | /api/v1/lpr/scale/{scaleId}/latest | Latest capture per weighbridge | Required |

### 13.2 Complete Environment Variable List

| Variable | Required | Description | Example Value |
|----------|:--------:|-------------|---------------|
| DB_HOST | O | PostgreSQL host | 10.x.x.10 |
| DB_PORT | X | PostgreSQL port (default 5432) | 5432 |
| DB_NAME | O | Database name | weighing |
| DB_USERNAME | O | DB user | weighing_app |
| DB_PASSWORD | O | DB password | (secure value) |
| REDIS_HOST | O | Redis host | 10.x.x.20 |
| REDIS_PORT | X | Redis port (default 6379) | 6379 |
| REDIS_PASSWORD | O | Redis password | (secure value) |
| JWT_SECRET | O | JWT signing key (Base64) | (secure value) |
| AES_SECRET_KEY | O | AES-256 encryption key (Base64) | (secure value) |
| CORS_ORIGIN_WEB | O | Allowed CORS origin | https://weighing.factory.internal |
| CORS_ORIGIN_MOBILE | X | Mobile CORS origin | http://localhost:8081 |
| API_INTERNAL_KEY | O | CS program internal API key | (secure value) |
| SPRING_PROFILES_ACTIVE | O | Active profile | prod |
| APP_VERSION | X | Application version tag | 1.0.0 |

### 13.3 Database ERD

```
tb_company (Transport Company)       tb_vehicle (Vehicle)
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
tb_user (User)         |       |                           |
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
tb_dispatch (Dispatch)                                     |
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
tb_weighing (Weighing)   |
+-------------------+    |
| weighing_id (PK)  |    |
| dispatch_id (FK)  |    |
| vehicle_id (FK)   |    |
| scale_id (FK)     |--->| tb_scale (Weighbridge)
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
    |   (Weighing Slip)  |
    |   +--------------+ |
    |   | slip_id (PK) | |
    |   | weighing_id  | |
    |   | slip_number  | |
    |   | slip_data    | |
    |   | shared_via   | |
    |   +--------------+ |
    |                    |
    v                    |
tb_gate_pass (Gate Pass) |
+-----------------+      |
| gate_pass_id(PK)|      |
| weighing_id(FK) |      |
| dispatch_id(FK) |------+
| pass_status     |
| passed_at       |
+-----------------+

tb_master_code (Common Code)     tb_audit_log (Audit Log)
+------------------+             +------------------+
| code_id (PK)     |             | audit_id (PK)    |
| code_group       |             | event_type       |
| code_value       |             | user_id          |
| code_name        |             | ip_address       |
| sort_order       |             | detail           |
| is_active        |             | created_at       |
| parent_code_id   |             +------------------+
+------------------+

tb_notification (Notification)   tb_inquiry_call (Inquiry)
+------------------+             +------------------+
| noti_id (PK)     |             | call_id (PK)     |
| user_id (FK)     |             | user_id (FK)     |
| noti_type        |             | inquiry_type     |
| title            |             | target_dept      |
| message          |             | call_status      |
| is_read          |             | created_at       |
| sent_at          |             +------------------+
+------------------+
```

### 13.4 Glossary

| Term | English | Description |
|------|---------|-------------|
| LPR | License Plate Recognition | Automatic vehicle license plate recognition device |
| LiDAR | Light Detection and Ranging | Laser-based distance measurement sensor (for vehicle detection) |
| OTP | One-Time Password | One-time security password (6-digit number) |
| Indicator | Indicator | Device that displays/transmits weight values at the weighbridge |
| Electronic Weighing Slip | Electronic Weighing Slip | Digital weighing certificate replacing paper slips |
| Dispatch | Dispatch | Vehicle transport schedule assignment |
| Weighbridge | Scale / Weighbridge | Facility for measuring vehicle weight |
| Gross Weight | Gross Weight | Combined weight of vehicle + cargo |
| Tare Weight | Tare Weight | Weight of the empty vehicle without cargo |
| Net Weight | Net Weight | Gross Weight - Tare Weight = pure cargo weight |
| RS-232C | - | Serial communication standard (for indicator communication) |
| JWT | JSON Web Token | Authentication token standard (stateless authentication) |
| RBAC | Role-Based Access Control | Role-based access control |
| bcrypt | - | Password hashing algorithm (one-way) |
| AES-256 | Advanced Encryption Standard | Symmetric key encryption algorithm (256-bit) |
| TLS | Transport Layer Security | Communication encryption protocol |
| WAL | Write-Ahead Logging | PostgreSQL transaction log (for PITR recovery) |
| PITR | Point-in-Time Recovery | Recovery to a specific point in time |
| RPO | Recovery Point Objective | Maximum acceptable data loss time window |
| RTO | Recovery Time Objective | Target service recovery time |
| HikariCP | - | JDBC connection pool library |
| ELK Stack | Elasticsearch + Logstash + Kibana | Centralized log management system |
| Prometheus | - | Time-series metrics collection/storage system |
| Grafana | - | Metrics visualization dashboard tool |
| Active-Active | - | Redundancy configuration where both servers handle traffic simultaneously |
| Rolling Update | - | Zero-downtime deployment by sequentially updating servers |
| Failover | - | Automatic switchover to a backup system upon failure |
| CSP | Content Security Policy | Web security header (XSS defense) |
| OWASP | Open Web Application Security Project | Web application security project |

---

**Document History**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-29 | System Admin Team | Initial release |
| 1.1 | 2026-01-29 | System Admin Team | User management API enhancements (role change, password reset, delete), system settings management added, notice ADMIN management (create/edit/delete/publish/pin) added, FAQ ADMIN management added, equipment monitoring API improvements (summary/health check), my page/favorites/notices/statistics/dashboard/gate pass/LPR API added, role-based access permission matrix updated |
| 1.2 | 2026-01-29 | System Admin Team | Web frontend new features (onboarding guide, keyboard shortcuts, empty state UI, number animation, tab visibility detection) reflected, mobile offline cache reflected, desktop splash form/hardware interface abstraction/xUnit tests reflected, page registry-based central routing management reflected, module-specific detailed design document references added |

---

## Appendix C: Operational Reference - New Architecture Elements (v1.2)

### C.1 Page Registry-Based Routing

All pages in the web frontend are centrally managed in `frontend/src/config/pageRegistry.ts`. Modify this file when adding new menus or changing permissions.

```
Key configuration items:
- component: React.lazy-based code-split component
- title: Menu/tab display name
- icon: Ant Design icon
- closable: Whether the tab can be closed (false for Weighing Station)
- roles: Array of accessible roles (accessible to all if not specified)
```

### C.2 Web Frontend New Features List

| Feature | File | Operational Impact |
|---------|------|-------------------|
| Onboarding Guide | `OnboardingTour.tsx` | Reduces new user training burden |
| Keyboard Shortcuts | `useKeyboardShortcuts.ts` | Improves staff work efficiency |
| Empty State UI | `EmptyState.tsx` | Improved no-data state guidance |
| Number Animation | `AnimatedNumber.tsx` | Dashboard KPI visual effects |
| Tab Visibility Detection | `useTabVisible.ts` | Auto-refresh data on tab switch |
| Drag-and-Drop Sorting | `SortableTable.tsx` | Table row reorder UX improvement |

### C.3 Mobile Offline Cache Operations

The `OfflineCacheService` has been added to the mobile app, enabling basic data viewing even in unstable network environments.

| Item | Setting |
|------|---------|
| **Storage** | SharedPreferences (device internal storage) |
| **Cache Targets** | Dispatch list, weighing history |
| **Validity Period** | 1 hour (auto-refresh) |
| **Capacity Limit** | Depends on device storage (lightweight data only) |
| **Security** | Sensitive data (tokens, etc.) stored separately in flutter_secure_storage |

### C.4 Desktop Architecture Improvements

The following improvements have been applied to the WeighingCS program:

| Improvement | Description |
|-------------|-------------|
| **SplashForm** | Displays initialization status on app startup (config load, equipment connection check) |
| **Interface Abstraction** | Hardware separated through `ILprCamera`, `IVehicleDetector`, `IVehicleSensor` interfaces |
| **Simulators** | Simulator implementations for each device in `Simulators/` directory (for development/testing) |
| **xUnit Tests** | `ApiServiceTests`, `IndicatorServiceTests`, `LocalCacheServiceTests` unit tests |

### C.5 Detailed Design Document References

Detailed design documents for each module have been added to the `docs/design/` directory:

| Document | Target Module |
|----------|--------------|
| `auth-basic-design.md` | Authentication basic design |
| `auth-detail-design.md` | Authentication detailed design |
| `dispatch-detail-design.md` | Dispatch module detailed design |
| `weighing-detail-design.md` | Weighing module detailed design |
| `gatepass-slip-detail-design.md` | Gate pass/slip module detailed design |
| `lpr-otp-notification-detail-design.md` | LPR/OTP/notification module detailed design |
| `ui_ux_recommendation.md` | UI/UX design guide |

---

*This document is the official manual for the operation and management of the Busan Smart Weighing System. This document must be updated whenever system changes are made.*
