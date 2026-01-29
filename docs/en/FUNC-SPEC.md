# Busan Smart Weighing System - Functional Specification

**Version**: 1.1
**Created**: 2026-01-27
**Last Modified**: 2026-01-29
**Base Documents**: PRD-20260127-154446, TRD-20260127-155235, WBS-20260127-160043
**Status**: Updated

---

## Table of Contents

1. [Document Overview](#1-document-overview)
2. [Module 1: LPR License Plate Recognition System](#2-module-1-lpr-license-plate-recognition-system)
3. [Module 2: Smart Weighing Web Management System](#3-module-2-smart-weighing-web-management-system)
4. [Module 3: Weighing CS Program](#4-module-3-weighing-cs-program)
5. [Module 4: Weighing Management Mobile APP](#5-module-4-weighing-management-mobile-app)
6. [Module 5: Mobile API](#6-module-5-mobile-api)
7. [Module 6: H/W Infrastructure Integration](#7-module-6-hw-infrastructure-integration)
8. [Module 7: Landline Call Dual-Routing System](#8-module-7-landline-call-dual-routing-system)
9. [Requirements Traceability Matrix](#9-requirements-traceability-matrix)

---

## 1. Document Overview

### 1.1 Purpose
This document defines the detailed behavior of each function based on the PRD, TRD, and WBS of the Busan Smart Weighing System. It serves as the reference document for development, testing, and acceptance.

### 1.2 Scope
All functional requirements from PRD FR-001 through FR-008 are categorized into 7 modules and specified in detail.

### 1.3 Terminology
| Term | Definition |
|------|-----------|
| LPR | License Plate Recognition - Automatic license plate recognition device |
| OTP | One-Time Password - Single-use security password |
| LiDAR | Light Detection and Ranging - LiDAR sensor |
| Indicator | Device that displays weight values on the weighbridge |
| Electronic Weighing Slip | Digital weighing certificate provided via mobile APP |
| Dispatch | Vehicle transport schedule assignment |

---

## 2. Module 1: LPR License Plate Recognition System

### FUNC-001: LPR Automatic License Plate Capture

| Item | Details |
|------|---------|
| **Function ID** | FUNC-001 |
| **Function Name** | LPR Automatic License Plate Capture |
| **PRD Mapping** | FR-001 |
| **Module** | LPR License Plate Recognition System |
| **Priority** | HIGH |

**Function Description**: When the LiDAR/radar sensor detects vehicle entry, the LPR camera automatically captures the license plate.

**Preconditions**:
- LPR camera is in normal operating state
- LiDAR/radar sensor is normally connected
- Weighbridge CS program is running

**Postconditions**:
- Captured image is stored on the server
- AI verification process is triggered

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| sensor_event | string | Y | LiDAR/radar detection event signal |
| scale_id | bigint | Y | Weighbridge identifier |
| timestamp | timestamptz | Y | Detection time |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| lpr_image_path | string | Captured image storage path |
| raw_plate_number | string | LPR primary recognition plate number |
| capture_timestamp | timestamptz | Capture time |

**Business Rules**:
- BR-001-1: LPR capture is triggered at the moment of LiDAR/radar sensor detection
- BR-001-2: Captured images are retained on NAS for 90 days (TRD backup policy)
- BR-001-3: Prevent duplicate captures for a single vehicle (minimum 10-second interval)

**Main Flow**:
1. LiDAR/radar sensor detects vehicle entry
2. Sensor event is transmitted to weighbridge CS program via TCP/UDP
3. CS program sends capture command to LPR camera
4. LPR camera captures the license plate
5. LPR device returns primary recognition result (raw_plate_number)
6. CS program sends captured image and primary recognition result to API server
7. API server forwards image to AI verification engine

**Alternative Flow**:
- AF-001-1: If LPR primary recognition fails, retry capture twice (1-second interval)
- AF-001-2: For nighttime captures, auxiliary lighting turns on automatically

**Exception Flow**:
- EF-001-1: LPR camera disconnected → Display warning on CS program, guide to manual weighing mode
- EF-001-2: Sensor detection error (no vehicle present) → Discard capture result, log record
- EF-001-3: 3 consecutive capture failures → Send admin notification, request H/W inspection

**UI/UX Requirements**:
- Display LPR capture status on CS program main screen (Standby/Capturing/Complete/Error)
- Real-time preview of captured images

**Related Functions**: FUNC-002 (AI License Plate Verification), FUNC-010 (Sensor Integration)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: LPR capture → AI verification → result return within 3 seconds
- NFR-004 Availability: Switch to manual mode in case of LPR equipment failure

---

### FUNC-002: AI License Plate Verification

| Item | Details |
|------|---------|
| **Function ID** | FUNC-002 |
| **Function Name** | AI License Plate Verification |
| **PRD Mapping** | FR-001 |
| **Module** | LPR License Plate Recognition System |
| **Priority** | HIGH |

**Function Description**: The AI engine re-verifies the LPR primary recognition result to confirm the license plate number and calculate a confidence score.

**Preconditions**:
- LPR captured image exists
- AI recognition engine is in normal operating state
- API server is functioning normally

**Postconditions**:
- License plate number confirmed and confidence score stored
- Automatic dispatch matching process triggered

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| lpr_image | binary | Y | Captured image file |
| raw_plate_number | string | Y | LPR primary recognition result |
| scale_id | bigint | Y | Weighbridge identifier |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| confirmed_plate_number | string | AI-confirmed license plate number |
| ai_confidence | decimal(5,4) | Confidence score (0.0~1.0) |
| verification_status | string | CONFIRMED / LOW_CONFIDENCE / FAILED |

**Business Rules**:
- BR-002-1: ai_confidence >= 0.90 → CONFIRMED (proceed with automatic weighing)
- BR-002-2: 0.70 <= ai_confidence < 0.90 → LOW_CONFIDENCE (display board OTP guidance, mobile OTP process)
- BR-002-3: ai_confidence < 0.70 → FAILED (display board OTP guidance, mobile OTP required)
- BR-002-4: Target overall recognition rate of 95% or higher

**Main Flow**:
1. API server sends LPR image to AI engine via HTTPS
2. AI engine extracts license plate number and calculates confidence score
3. Result is returned to API server
4. API server stores lpr_plate_number and ai_confidence in tb_weighing table
5. Branches to automatic weighing (CONFIRMED) or OTP process (LOW_CONFIDENCE/FAILED) based on confidence

**Alternative Flow**:
- AF-002-1: AI engine response delay (exceeds 3 seconds) → Timeout, use LPR primary result + parallel OTP

**Exception Flow**:
- EF-002-1: AI engine service down → Use LPR primary result, switch to mandatory mobile OTP
- EF-002-2: Image damaged/defective → Request re-capture

**Related Functions**: FUNC-001 (LPR Capture), FUNC-003 (Automatic Dispatch Matching), FUNC-004 (OTP Secure Weighing)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: AI verification response within 3 seconds (including E2E)
- NFR-002 Security: HTTPS encryption for AI engine communication

---

### FUNC-003: Automatic Dispatch Matching

| Item | Details |
|------|---------|
| **Function ID** | FUNC-003 |
| **Function Name** | Automatic Dispatch Matching |
| **PRD Mapping** | FR-001 |
| **Module** | LPR License Plate Recognition System |
| **Priority** | HIGH |

**Function Description**: Automatically matches dispatch information for the day using the AI-confirmed license plate number to proceed with weighing.

**Preconditions**:
- AI license plate verification completed (CONFIRMED status)
- Dispatch information for the vehicle exists for the current day

**Postconditions**:
- Dispatch-weighing record linked
- Automatic weighing process started

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| confirmed_plate_number | string | Y | AI-confirmed license plate number |
| scale_id | bigint | Y | Weighbridge identifier |
| weighing_date | date | Y | Weighing date (current day) |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| dispatch_id | bigint | Matched dispatch ID |
| vehicle_id | bigint | Vehicle ID |
| item_type | string | Item type |
| dispatch_list | array | Multiple dispatch list (for multiple matches) |

**Business Rules**:
- BR-003-1: Look up vehicle_id from tb_vehicle by license plate number (plate_number UNIQUE index)
- BR-003-2: Query dispatches with REGISTERED/IN_PROGRESS status from tb_dispatch using vehicle_id + current date
- BR-003-3: Single dispatch match → Proceed with automatic weighing immediately
- BR-003-4: Multiple dispatch matches → Request dispatch selection from mobile APP (display board guidance)
- BR-003-5: No matching dispatch → Switch to manual weighing mode

**Main Flow**:
1. Look up tb_vehicle with confirmed license plate number
2. Query active dispatches for the day using vehicle_id
3. Single dispatch → Start weighing with dispatch_id (tb_weighing INSERT)
4. Notify weighbridge CS of automatic weighing start
5. Open automatic barrier (allow weighbridge entry)

**Alternative Flow**:
- AF-003-1: Multiple dispatches → Display "Select dispatch on mobile" on display board → Select from mobile APP
- AF-003-2: Unregistered vehicle → Display "Unregistered vehicle" on display board, manual weighing mode

**Exception Flow**:
- EF-003-1: DB query failure → Switch to manual weighing mode, record error log

**Related Functions**: FUNC-002 (AI Verification), FUNC-007 (Weighing Process), FUNC-012 (Display Board Control)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: Dispatch matching query within 200ms (using index)
- NFR-003 Scalability: Minimize changes when adding item types

---

### FUNC-004: OTP Secure Weighing

| Item | Details |
|------|---------|
| **Function ID** | FUNC-004 |
| **Function Name** | OTP Secure Weighing |
| **PRD Mapping** | FR-002 |
| **Module** | LPR License Plate Recognition System |
| **Priority** | HIGH |

**Function Description**: When license plate misrecognition occurs, an OTP is displayed on the display board, and the driver enters the OTP in the mobile APP for identity verification before proceeding with weighing.

**Preconditions**:
- AI verification result is LOW_CONFIDENCE or FAILED
- Display board is functioning normally
- Driver has installed and logged into the mobile APP

**Postconditions**:
- OTP verification completed, vehicle confirmed
- Mobile weighing process proceeds

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| otp_code | varchar(6) | Y | 6-digit number |
| phone_number | varchar(20) | Y | Registered phone number format |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| verified | boolean | Verification success status |
| vehicle_id | bigint | Confirmed vehicle ID |
| plate_number | string | Confirmed license plate number |
| dispatch_id | bigint | Matched dispatch ID |

**Business Rules**:
- BR-004-1: OTP is a 6-digit number, stored in Redis, TTL 5 minutes
- BR-004-2: OTP is displayed on the display board of the corresponding weighbridge upon generation
- BR-004-3: Phone number-based user → vehicle matching (tb_user.phone_number → tb_vehicle)
- BR-004-4: 3 OTP verification failures → Invalidate OTP, issue new OTP
- BR-004-5: After OTP expiration → Automatically issue new OTP, refresh display board
- BR-004-6: Prevent mis-weighing: Duplicate weighing with the same OTP is not allowed

**Main Flow**:
1. AI verification result determines LOW_CONFIDENCE/FAILED
2. API server generates 6-digit OTP and stores in Redis (TTL 5 minutes)
3. CS program displays OTP number on display board
4. Driver enters OTP number in mobile APP
5. Mobile APP calls POST /api/v1/otp/verify
6. Server queries and verifies OTP from Redis
7. Matches user/vehicle by phone number
8. Verification success → Dispatch matching → Start mobile weighing
9. Delete OTP Redis key (single-use)

**Alternative Flow**:
- AF-004-1: OTP expired → "OTP has expired. A new OTP will be issued" → Reissue
- AF-004-2: Unregistered phone number → "Unregistered number. Please contact administrator" guidance

**Exception Flow**:
- EF-004-1: Redis failure → Fallback to direct DB OTP query
- EF-004-2: Display board malfunction → Send OTP via mobile APP Push notification
- EF-004-3: 3 verification failures → Guide to manual weighing mode

**UI/UX Requirements**:
- Display board: Large OTP number display (3-row 6-column custom specification)
- Mobile APP: OTP input screen, numeric keypad, remaining time timer display

**Related Functions**: FUNC-002 (AI Verification), FUNC-012 (Display Board Control), FUNC-020 (Mobile OTP Screen)

**Non-Functional Requirements Mapping**:
- NFR-002 Security: OTP TTL 5 minutes, 3-failure lockout, Redis-based management
- NFR-001 Performance: OTP verification API response within 2 seconds

---

## 3. Module 2: Smart Weighing Web Management System

### FUNC-005: Dispatch Registration/Management

| Item | Details |
|------|---------|
| **Function ID** | FUNC-005 |
| **Function Name** | Dispatch Registration/Management |
| **PRD Mapping** | FR-004 |
| **Module** | Smart Weighing Web Management System |
| **Priority** | HIGH |

**Function Description**: Weighing personnel register and manage dispatches by item type (by-products, waste, sub-materials, export, general) through the web system.

**Preconditions**:
- User login completed (ADMIN or MANAGER role)
- Transport company and vehicle master data registered

**Postconditions**:
- tb_dispatch record created/modified
- Dispatch status change history recorded

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| vehicle_id | bigint | Y | Registered vehicle ID |
| company_id | bigint | Y | Registered transport company ID |
| item_type | varchar(20) | Y | Select one of: by-product/waste/sub-material/export/general |
| item_name | varchar(100) | Y | Item name (max 100 characters) |
| dispatch_date | date | Y | Future or current date |
| origin_location | varchar(100) | N | Origin location |
| destination | varchar(100) | N | Destination |
| remarks | text | N | Remarks |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| dispatch_id | bigint | Created dispatch ID |
| dispatch_status | string | REGISTERED |
| created_at | timestamptz | Creation datetime |

**Business Rules**:
- BR-005-1: Dispatch registration permission limited to ADMIN and MANAGER only
- BR-005-2: Dispatch status flow: REGISTERED → IN_PROGRESS → COMPLETED / CANCELLED
- BR-005-3: Dispatches in COMPLETED status cannot be modified
- BR-005-4: Multiple dispatches for the same vehicle on the same date are allowed (multiple dispatch scenario)
- BR-005-5: Dispatch deletion is ADMIN only, only possible in REGISTERED status

**Main Flow**:
1. Personnel enters the dispatch management screen
2. Click "Register Dispatch" button
3. Enter dispatch information (transport company, vehicle, item, date, etc.)
4. Click "Save" → Call POST /api/v1/dispatches
5. Server validation → tb_dispatch INSERT
6. New item displayed in dispatch list

**Alternative Flow**:
- AF-005-1: Copy existing dispatch → Previous dispatch information auto-filled, change date only
- AF-005-2: Batch dispatch registration → Excel upload method

**Exception Flow**:
- EF-005-1: Missing required input → Display validation error message
- EF-005-2: Unregistered vehicle number → "Please register the vehicle first" guidance

**UI/UX Requirements**:
- Ant Design data table: Filters, sorting, subtotals, column pinning
- Responsive design: Compatible across various devices/browsers
- High resolution, variable SIZE screen

**Related Functions**: FUNC-008 (Master Data Management), FUNC-003 (Automatic Dispatch Matching)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: Dispatch list query within 3 seconds (web screen loading)
- NFR-005 Usability: Filter/sort/subtotal/column pinning convenience features

---

### FUNC-006: Weighing Status Management

| Item | Details |
|------|---------|
| **Function ID** | FUNC-006 |
| **Function Name** | Weighing Status Management |
| **PRD Mapping** | FR-004 |
| **Module** | Smart Weighing Web Management System |
| **Priority** | HIGH |

**Function Description**: Monitor real-time weighing status and query/manage weighing records through the web system.

**Preconditions**:
- User login completed
- Weighing record data exists

**Postconditions**:
- Weighing status screen refreshed

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| date_from | date | N | Query start date |
| date_to | date | N | Query end date |
| item_type | string | N | Item type filter |
| weighing_mode | string | N | Weighing mode filter (LPR_AUTO/MOBILE_OTP/MANUAL) |
| status | string | N | Weighing status filter |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| weighing_list | array | Weighing record list |
| statistics | object | Statistical information (count, total weight, etc.) |
| realtime_status | object | Real-time weighbridge status (WebSocket) |

**Business Rules**:
- BR-006-1: Real-time weighing status updates via WebSocket (STOMP) within 500ms
- BR-006-2: Weighing list with pagination (default 20 records/page)
- BR-006-3: Statistics query: Daily/monthly/item-type aggregation provided
- BR-006-4: Display LPR captured image in weighing detail view

**Main Flow**:
1. Personnel enters the weighing management screen
2. WebSocket connection established (WSS)
3. Display real-time weighing status dashboard (weighbridge status, in-progress weighings)
4. Query weighing record list with conditions (REST API)
5. Click specific weighing → Display detail information + LPR image

**Alternative Flow**:
- AF-006-1: WebSocket connection failure → REST polling (5-second interval)
- AF-006-2: Statistics/Dashboard → ECharts chart display (daily trends, item-type ratios)

**Exception Flow**:
- EF-006-1: Large data query → Guide to Excel export

**UI/UX Requirements**:
- ECharts-based dashboard (daily weighing trends, item-type ratios, weighing mode statistics)
- Real-time weighbridge status display (Standby/Weighing/Complete/Error)
- Data table: Filters, sorting, pagination, Excel export

**Related Functions**: FUNC-005 (Dispatch Management), FUNC-007 (Weighing Process)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: WebSocket data delivery within 500ms, API p95 500ms
- NFR-005 Usability: Various charts, dashboard

---

### FUNC-007: Weighing Record Processing

| Item | Details |
|------|---------|
| **Function ID** | FUNC-007 |
| **Function Name** | Weighing Record Processing |
| **PRD Mapping** | FR-004, FR-005 |
| **Module** | Smart Weighing Web Management System / Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Processes weighing start/completion/re-weighing workflows and stores records. Manages 1st/2nd/3rd weighing and tare/gross weight.

**Preconditions**:
- Dispatch matching completed (automatic or manual)
- Stabilized weight value received from indicator

**Postconditions**:
- tb_weighing record created/updated
- Electronic weighing slip generated (upon weighing completion)
- Push notification sent

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| dispatch_id | bigint | Y | Valid dispatch ID |
| scale_id | bigint | Y | Valid weighbridge ID |
| weighing_mode | varchar(20) | Y | LPR_AUTO / MOBILE_OTP / MANUAL / RE_WEIGH |
| weighing_type | varchar(20) | Y | FIRST / SECOND / THIRD |
| weight_value | decimal(10,2) | Y | Weight value (kg), > 0 |
| lpr_plate_number | varchar(20) | N | LPR recognized plate number |
| ai_confidence | decimal(5,4) | N | AI confidence (0.0~1.0) |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| weighing_id | bigint | Weighing record ID |
| gross_weight | decimal(10,2) | Gross weight (kg) |
| tare_weight | decimal(10,2) | Vehicle tare weight (kg) |
| net_weight | decimal(10,2) | Net weight = gross - tare |
| status | string | IN_PROGRESS / COMPLETED / RE_WEIGHING / ERROR |

**Business Rules**:
- BR-007-1: weighing_step flow: GROSS (tare) → TARE (loaded) → COMPLETE
- BR-007-2: net_weight = gross_weight - tare_weight (auto-calculated)
- BR-007-3: net_weight < 0 → Error (verify tare/loaded order)
- BR-007-4: For re-weighing (RE_WEIGH), change existing weighing status to RE_WEIGHING, create new weighing record
- BR-007-5: Upon weighing completion, automatically generate electronic weighing slip (tb_weighing_slip INSERT)
- BR-007-6: Upon weighing completion, send Push notification + KakaoTalk notification
- BR-007-7: Only store stabilized weight values (indicator stabilization determination)

**Main Flow**:
1. Vehicle positions on weighbridge (vehicle detector confirmation)
2. Indicator detects weight stabilization
3. CS program receives stabilized weight value
4. CS program calls POST /api/v1/weighings (weighing start)
5. 1st weighing (GROSS) completed → Store weight value
6. Vehicle moves, 2nd weighing (TARE) proceeds
7. Net weight (net_weight) auto-calculated
8. Weighing completed (COMPLETE) → PUT /api/v1/weighings/{id}/complete
9. Electronic weighing slip auto-generated
10. Push notification sent

**Alternative Flow**:
- AF-007-1: If 3rd weighing needed → Additional weighing with THIRD type
- AF-007-2: Re-weighing → PUT /api/v1/weighings/{id}/re-weigh → Existing record set to RE_WEIGHING, new record created
- AF-007-3: Manual weighing mode → Personnel directly inputs weight value via touchscreen

**Exception Flow**:
- EF-007-1: Indicator communication lost → CS screen warning, switch to manual input
- EF-007-2: API server communication failure → Local caching, auto-sync upon recovery
- EF-007-3: net_weight < 0 → "Please verify tare/loaded order" warning

**Related Functions**: FUNC-003 (Dispatch Matching), FUNC-009 (Electronic Weighing Slip), FUNC-010 (Indicator Integration)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: Real-time weighing data synchronization within 5 seconds
- NFR-004 Availability: Local caching with sync on network failure recovery

---

### FUNC-008: Master Data Management

| Item | Details |
|------|---------|
| **Function ID** | FUNC-008 |
| **Function Name** | Master Data Management |
| **PRD Mapping** | FR-004 |
| **Module** | Smart Weighing Web Management System |
| **Priority** | HIGH |

**Function Description**: Manages system master data including transport companies, vehicles, weighbridges, and common codes.

**Preconditions**:
- User login (ADMIN role)

**Postconditions**:
- Master data table updated
- Redis cache refreshed (TTL 5 minutes)

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| company_name | varchar(100) | Y | For transport company registration |
| company_type | varchar(20) | Y | Transport company type |
| plate_number | varchar(20) | Y | For vehicle registration, UNIQUE |
| vehicle_type | varchar(20) | Y | Vehicle type |
| code_group | varchar(50) | Y | Common code group |
| code_value | varchar(50) | Y | Code value |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| record_id | bigint | Created/modified record ID |
| operation | string | CREATE / UPDATE / DELETE |

**Business Rules**:
- BR-008-1: Master data registration/modification/deletion limited to ADMIN role only
- BR-008-2: License plate number (plate_number) must be unique system-wide
- BR-008-3: Transport company deletion only possible when no associated dispatches exist (or deactivation)
- BR-008-4: Invalidate Redis cache on master data changes

**Main Flow**:
1. Administrator enters master data management screen
2. Select transport company/vehicle/weighbridge/common code tab
3. List query (GET /api/v1/master/{type})
4. Perform registration/modification/deletion operations
5. Server processing → Redis cache refresh

**Related Functions**: FUNC-005 (Dispatch Registration), FUNC-007 (Weighing Processing)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: Master data Redis caching (TTL 5 minutes)
- NFR-003 Scalability: Item types managed via common codes for easy addition

---

### FUNC-009: Electronic Weighing Slip Generation/Query

| Item | Details |
|------|---------|
| **Function ID** | FUNC-009 |
| **Function Name** | Electronic Weighing Slip Generation/Query |
| **PRD Mapping** | FR-006 |
| **Module** | Smart Weighing Web Management System |
| **Priority** | MEDIUM |

**Function Description**: Automatically generates electronic weighing slips upon weighing completion and provides query and sharing functionality via mobile/web.

**Preconditions**:
- Weighing record in COMPLETED status

**Postconditions**:
- tb_weighing_slip record created
- Slip number auto-assigned

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| weighing_id | bigint | Y | Completed weighing record ID |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| slip_id | bigint | Weighing slip ID |
| slip_number | varchar(30) | Auto-assigned slip number (UNIQUE) |
| slip_data | jsonb | Weighing slip detail data (vehicle, item, weight, datetime, etc.) |

**Business Rules**:
- BR-009-1: Slip number auto-assignment rule: YYYYMMDD-SEQ (e.g., 20260527-0001)
- BR-009-2: slip_data includes plate number, transport company, item, gross weight, tare weight, net weight, weighing datetime
- BR-009-3: KakaoTalk sharing calls Kakao Biz API
- BR-009-4: SMS sharing calls SMS Gateway
- BR-009-5: Slip history queryable by date range

**Main Flow**:
1. Weighing completion event occurs
2. Server creates weighing slip record in tb_weighing_slip
3. slip_number auto-assigned
4. Weighing detail information stored as JSON in slip_data
5. Mobile APP Push notification (weighing slip creation notice)
6. User queries weighing slip via mobile/web

**Alternative Flow**:
- AF-009-1: KakaoTalk sharing → POST /api/v1/slips/{id}/share {type: "KAKAO"}
- AF-009-2: SMS sharing → POST /api/v1/slips/{id}/share {type: "SMS"}

**Exception Flow**:
- EF-009-1: Kakao API failure → Fallback to SMS delivery
- EF-009-2: SMS delivery failure → Register in retry queue (circuit breaker)

**Related Functions**: FUNC-007 (Weighing Records), FUNC-019 (Mobile Electronic Weighing Slip Screen)

**Non-Functional Requirements Mapping**:
- NFR-002 Security: Weighing slip data integrity assurance
- NFR-004 Availability: Use alternative channels on external service failure

---

### FUNC-030: Gate Pass Management

| Item | Details |
|------|---------|
| **Function ID** | FUNC-030 |
| **Function Name** | Gate Pass Management |
| **PRD Mapping** | FR-004 |
| **Module** | Smart Weighing Web Management System |
| **Priority** | MEDIUM |

**Function Description**: Manages gate pass processing for vehicles that have completed weighing.

**Preconditions**:
- Weighing record in COMPLETED status
- MANAGER or higher role

**Postconditions**:
- tb_gate_pass record created/updated

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| weighing_id | bigint | Y | Completed weighing record |
| dispatch_id | bigint | Y | Dispatch ID |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| gate_pass_id | bigint | Gate pass ID |
| pass_status | string | PENDING / PASSED / REJECTED |
| passed_at | timestamptz | Gate pass time |

**Business Rules**:
- BR-030-1: Gate pass processing only available for completed weighing records
- BR-030-2: Set pass_status to PASSED upon gate pass processing
- BR-030-3: Gate pass history queryable

**Main Flow**:
1. Personnel checks pending gate pass list on gate pass management screen
2. Select completed weighing record → Click "Process Gate Pass"
3. Call POST /api/v1/gate-passes
4. Gate pass status changed to PASSED, passed_at recorded

**Related Functions**: FUNC-007 (Weighing Records), FUNC-005 (Dispatch Management)

---

## 4. Module 3: Weighing CS Program

### FUNC-010: Indicator Weight Value Reception

| Item | Details |
|------|---------|
| **Function ID** | FUNC-010 |
| **Function Name** | Indicator Weight Value Reception |
| **PRD Mapping** | FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Receives weight values in real-time from the indicator via RS-232C serial communication and performs stabilization determination.

**Preconditions**:
- Indicator RS-232C connection completed
- COM port configuration completed

**Postconditions**:
- Stabilized weight value confirmed
- Weight value transmitted to weighing process

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| serial_data | byte[] | Y | RS-232C received data |
| com_port | string | Y | COM1~COM9 |
| baud_rate | int | Y | 9600/19200/38400 |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| weight_value | decimal(10,2) | Current weight value (kg) |
| is_stable | boolean | Stabilization status |
| stable_weight | decimal(10,2) | Stabilized confirmed weight value |

**Business Rules**:
- BR-010-1: Stabilization determination: Consecutive N readings (configurable) with identical weight → Stabilized
- BR-010-2: Weight value fluctuation tolerance: +/- configurable value (kg)
- BR-010-3: Communication timeout: Warning if no data received for 5+ seconds
- BR-010-4: Negative weight → Error handling

**Main Flow**:
1. CS program opens COM port via System.IO.Ports
2. Periodically receives weight data from indicator (100ms~1 second)
3. Data parsing → Weight value extraction
4. Stabilization determination logic executed
5. Stabilization confirmed → Deliver stable_weight to weighing process
6. Display current weight value in real-time on CS main screen

**Exception Flow**:
- EF-010-1: COM port connection failure → Retry connection (3 times), notify administrator on failure
- EF-010-2: Data parsing error → Discard packet, log record
- EF-010-3: No reception for 5+ seconds → Display "Indicator communication error" warning

**Related Functions**: FUNC-007 (Weighing Records), FUNC-011 (LPR Automatic Weighing Process)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: Real-time weight value reception and display
- NFR-004 Availability: Communication retry logic, timeout management

---

### FUNC-011: LPR Automatic Weighing Process

| Item | Details |
|------|---------|
| **Function ID** | FUNC-011 |
| **Function Name** | LPR Automatic Weighing Process |
| **PRD Mapping** | FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Controls the entire automatic process from LiDAR detection → LPR capture → AI verification → dispatch matching → automatic weighing in the CS program.

**Preconditions**:
- All H/W equipment normally connected (LPR, sensor, indicator, display board, barrier)
- API server connected

**Postconditions**:
- Weighing record stored
- Electronic weighing slip generated
- Barrier opened

**Business Rules**:
- BR-011-1: Automatic weighing proceeds only when AI confidence >= 0.90 AND single dispatch match
- BR-011-2: Target time for entire process (sensor detection → weighing complete): Within 30 seconds after vehicle stop
- BR-011-3: Target unmanned automatic weighing ratio: 90% or higher

**Main Flow**:
1. LiDAR/radar sensor → Vehicle entry detection
2. LPR camera → License plate capture
3. API server → AI verification request
4. AI result received (confidence >= 0.90)
5. Automatic dispatch matching (single dispatch)
6. Vehicle detector → Position confirmation
7. Indicator → Stabilized weight value received
8. Weighing record stored (POST /api/v1/weighings)
9. Display board → "Weighing Complete" displayed
10. Barrier → Opened
11. Electronic weighing slip auto-generated

**Alternative Flow**:
- AF-011-1: AI confidence < 0.90 → Switch to OTP process (FUNC-004)
- AF-011-2: Multiple dispatches → Display "Select dispatch on mobile" on display board
- AF-011-3: Unregistered vehicle → Switch to manual weighing mode

**Exception Flow**:
- EF-011-1: H/W equipment failure → Switch to manual weighing mode (touchscreen)
- EF-011-2: API server communication failure → Local caching mode
- EF-011-3: Indicator not stabilizing (exceeds 60 seconds) → Display "Waiting for stabilization", timeout warning

**Related Functions**: FUNC-001~004, FUNC-007, FUNC-010, FUNC-012~013

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: E2E within 3 seconds (LPR → AI verification)
- NFR-004 Availability: Manual mode switch available on H/W failure

---

### FUNC-012: Display Board Control

| Item | Details |
|------|---------|
| **Function ID** | FUNC-012 |
| **Function Name** | Display Board Control |
| **PRD Mapping** | FR-002, FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Displays OTP numbers, guidance messages, and weighing status on the weighbridge display board.

**Preconditions**:
- Display board TCP/RS-485 connection normal

**Postconditions**:
- Message displayed on display board

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| display_type | string | Y | OTP / STATUS / MESSAGE / ERROR |
| content | string | Y | Content to display |
| scale_id | bigint | Y | Weighbridge identifier |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| display_result | boolean | Display success status |

**Business Rules**:
- BR-012-1: Display board specification: 3-row 6-column custom large display board
- BR-012-2: Display 6-digit number in large font for OTP display
- BR-012-3: Status messages: "Weighing Standby", "Weighing In Progress", "Weighing Complete", "Mobile Authentication Required"
- BR-012-4: Error messages: "Unregistered Vehicle", "System Under Maintenance"

**Main Flow**:
1. CS program detects weighing status change
2. Generate display board command based on display_type
3. Send command to display board via TCP/RS-485
4. Message displayed on display board

**Related Functions**: FUNC-004 (OTP Secure Weighing), FUNC-011 (Automatic Weighing)

---

### FUNC-013: Automatic Barrier Control

| Item | Details |
|------|---------|
| **Function ID** | FUNC-013 |
| **Function Name** | Automatic Barrier Control |
| **PRD Mapping** | FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Controls the automatic barrier open/close according to the weighing process.

**Preconditions**:
- Barrier connection normal (TCP/RS-485)
- Manual switch connected

**Postconditions**:
- Barrier state changed (open/closed)

**Business Rules**:
- BR-013-1: Automatic opening upon weighing completion
- BR-013-2: Emergency opening possible via manual switch
- BR-013-3: Barrier specification: 3M LED square bar
- BR-013-4: Safety first: Confirm vehicle position before barrier operation

**Main Flow**:
1. Weighing completion signal received
2. Verify vehicle position via vehicle detector
3. Send barrier open command
4. Confirm vehicle passage
5. Send barrier close command

**Exception Flow**:
- EF-013-1: Barrier operation failure → Guide to use manual switch
- EF-013-2: Vehicle detection error → Maintain barrier, call administrator

**Related Functions**: FUNC-011 (Automatic Weighing), FUNC-014 (Manual Weighing)

---

### FUNC-014: Manual Weighing Mode

| Item | Details |
|------|---------|
| **Function ID** | FUNC-014 |
| **Function Name** | Manual Weighing Mode |
| **PRD Mapping** | FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Performs manual weighing using the existing touchscreen method when LPR/mobile weighing is not available.

**Preconditions**:
- Weighbridge CS program is running
- Indicator normally connected

**Postconditions**:
- Weighing record stored (weighing_mode = MANUAL)

**Business Rules**:
- BR-014-1: Personnel directly selects/inputs license plate number and dispatch information on touchscreen
- BR-014-2: Parallel operation with existing RFID method (during transition period)
- BR-014-3: Manual weighing also generates electronic weighing slips identically

**Main Flow**:
1. Personnel selects "Manual Weighing" mode in CS program
2. Enter license plate number or select from list
3. Select dispatch information
4. Confirm indicator stabilized weight value
5. Click "Confirm Weighing" button
6. Weighing record stored (weighing_mode = MANUAL)

**Related Functions**: FUNC-010 (Indicator), FUNC-007 (Weighing Records)

---

### FUNC-015: Reset/Re-weighing

| Item | Details |
|------|---------|
| **Function ID** | FUNC-015 |
| **Function Name** | Reset/Re-weighing |
| **PRD Mapping** | FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | HIGH |

**Function Description**: Performs data reset and re-weighing when weighing errors occur.

**Business Rules**:
- BR-015-1: On re-weighing, change existing weighing record status to RE_WEIGHING
- BR-015-2: Create new weighing record in RE_WEIGH mode
- BR-015-3: Re-weighing reason entry is mandatory
- BR-015-4: Re-weighing history trackable (tb_weighing_log)

**Main Flow**:
1. Personnel clicks "Re-weigh" button
2. Enter re-weighing reason
3. Change existing weighing record to RE_WEIGHING status
4. Start new weighing process
5. Re-weighing completed → Store record
6. Record re-weighing history log

**Related Functions**: FUNC-007 (Weighing Records), FUNC-010 (Indicator)

---

### FUNC-016: Local Caching/Offline Mode

| Item | Details |
|------|---------|
| **Function ID** | FUNC-016 |
| **Function Name** | Local Caching/Offline Mode |
| **PRD Mapping** | FR-005 |
| **Module** | Weighing CS Program |
| **Priority** | MEDIUM |

**Function Description**: Caches weighing data locally when network connection to the API server is lost, and automatically synchronizes upon recovery.

**Business Rules**:
- BR-016-1: Network disconnection detected → Automatically switch to offline mode
- BR-016-2: Weighing data during offline is stored in local SQLite/file
- BR-016-3: Automatic synchronization upon network recovery (FIFO order)
- BR-016-4: Failed synchronization items are kept in retry queue
- BR-016-5: Offline mode status clearly displayed on CS screen

**Related Functions**: FUNC-011 (Automatic Weighing), FUNC-014 (Manual Weighing)

**Non-Functional Requirements Mapping**:
- NFR-004 Availability: Local caching on network failure, synchronization on recovery (RTO within 1 hour)

---

## 5. Module 4: Weighing Management Mobile APP

### FUNC-017: Mobile Login

| Item | Details |
|------|---------|
| **Function ID** | FUNC-017 |
| **Function Name** | Mobile Login |
| **PRD Mapping** | FR-003 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | HIGH |

**Function Description**: Provides role-based login (manager/driver) and secure login (authentication code) functionality.

**Preconditions**:
- APP installation completed (iOS/Android)
- User account registered

**Postconditions**:
- JWT Access Token + Refresh Token issued
- FCM token registered on server

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| login_id | varchar(50) | Y | Registered login ID |
| password | varchar(255) | Y | Password |
| device_type | string | Y | Fixed as MOBILE |

**Business Rules**:
- BR-017-1: Role-based login for Manager (MANAGER) / Driver (DRIVER)
- BR-017-2: Secure login: Send authentication code to phone number → Enter authentication code
- BR-017-3: Access Token 30 minutes, Refresh Token 7 days
- BR-017-4: FCM token auto-registered upon login success
- BR-017-5: Account locked after 5 password failures

**Main Flow**:
1. APP launch → Login screen
2. Enter login ID/password
3. Call POST /api/v1/auth/login
4. Receive JWT tokens → Store locally in Hive
5. FCM token registration (POST /api/v1/notifications/push/register)
6. Navigate to main screen based on role

**Alternative Flow**:
- AF-017-1: Secure login → POST /api/v1/auth/login/otp (phone number + authentication code)
- AF-017-2: Auto login → Refresh Access Token using Refresh Token

**Related Functions**: FUNC-018~021 (Mobile functions)

**Non-Functional Requirements Mapping**:
- NFR-002 Security: JWT authentication, bcrypt password, OTP-based secure login
- NFR-005 Usability: Flutter iOS/Android cross-platform

---

### FUNC-018: Dispatch Query/Selection

| Item | Details |
|------|---------|
| **Function ID** | FUNC-018 |
| **Function Name** | Dispatch Query/Selection |
| **PRD Mapping** | FR-003 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | HIGH |

**Function Description**: Drivers view dispatch schedule status and select a dispatch for weighing when multiple dispatches exist.

**Business Rules**:
- BR-018-1: Driver login → Display only today's dispatches for their own vehicle
- BR-018-2: Manager login → Full dispatch query access
- BR-018-3: Select from list for weighing when multiple dispatches exist
- BR-018-4: Color-coded dispatch status (Registered/In Progress/Completed)

**Main Flow**:
1. Main screen → "Dispatch Query" tab
2. Call GET /api/v1/dispatches/my (driver) or GET /api/v1/dispatches (manager)
3. Display dispatch list
4. Select dispatch → Navigate to weighing progress screen

**Related Functions**: FUNC-019 (Mobile Weighing), FUNC-003 (Dispatch Matching)

---

### FUNC-019: Mobile Weighing Progress

| Item | Details |
|------|---------|
| **Function ID** | FUNC-019 |
| **Function Name** | Mobile Weighing Progress |
| **PRD Mapping** | FR-003 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | HIGH |

**Function Description**: View weighing progress status in real-time from the mobile APP, and control weighing from mobile during OTP-based weighing.

**Business Rules**:
- BR-019-1: Real-time weighing progress status display (Standby/Weighing/Complete)
- BR-019-2: During OTP weighing, "Start Weighing" trigger available from mobile
- BR-019-3: Auto-navigate to electronic weighing slip screen after weighing completion

**Related Functions**: FUNC-004 (OTP Weighing), FUNC-007 (Weighing Records), FUNC-009 (Electronic Weighing Slip)

---

### FUNC-020: Mobile OTP Input

| Item | Details |
|------|---------|
| **Function ID** | FUNC-020 |
| **Function Name** | Mobile OTP Input |
| **PRD Mapping** | FR-002, FR-003 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | HIGH |

**Function Description**: Performs identity verification by entering the OTP number displayed on the display board into the mobile APP.

**Business Rules**:
- BR-020-1: 6-digit numeric keypad UI
- BR-020-2: OTP remaining time timer display (5 minutes)
- BR-020-3: Verification success → Dispatch selection → Proceed with weighing
- BR-020-4: Verification failure → Error message + re-entry guidance

**Main Flow**:
1. Auto or manual entry to OTP input screen
2. Enter 6-digit OTP number from display board
3. Call POST /api/v1/otp/verify
4. Verification success → Dispatch matching → Weighing progress screen

**Related Functions**: FUNC-004 (OTP Secure Weighing), FUNC-018 (Dispatch Selection)

---

### FUNC-021: Electronic Weighing Slip Query/Share

| Item | Details |
|------|---------|
| **Function ID** | FUNC-021 |
| **Function Name** | Electronic Weighing Slip Query/Share |
| **PRD Mapping** | FR-003, FR-006 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | HIGH |

**Function Description**: Query electronic weighing slips and share via KakaoTalk/SMS from the mobile APP.

**Business Rules**:
- BR-021-1: Auto-display weighing slip screen after weighing completion
- BR-021-2: KakaoTalk sharing: Weighing slip link + summary information
- BR-021-3: SMS sharing: Weighing slip summary text
- BR-021-4: History query: Monthly/daily, configurable date range

**Main Flow**:
1. Weighing complete → Auto-navigate to electronic weighing slip screen
2. Display weighing slip detail information (vehicle, item, weight, time)
3. "Share" button → Select KakaoTalk/SMS
4. Call POST /api/v1/slips/{id}/share
5. Send sharing via external service

**Related Functions**: FUNC-009 (Electronic Weighing Slip Generation)

---

### FUNC-022: Dispatch/Weighing Record Query

| Item | Details |
|------|---------|
| **Function ID** | FUNC-022 |
| **Function Name** | Dispatch/Weighing Record Query |
| **PRD Mapping** | FR-003 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | MEDIUM |

**Function Description**: Query dispatch/weighing completion records by month/day from the mobile APP.

**Business Rules**:
- BR-022-1: Driver: Query own records only
- BR-022-2: Configurable date range (start date~end date)
- BR-022-3: Monthly/daily tab switching

**Related Functions**: FUNC-018 (Dispatch Query), FUNC-021 (Weighing Slip Query)

---

### FUNC-023: Notices/Inquiry Calls

| Item | Details |
|------|---------|
| **Function ID** | FUNC-023 |
| **Function Name** | Notices/Inquiry Calls |
| **PRD Mapping** | FR-003, FR-007 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | MEDIUM |

**Function Description**: View notices from the mobile APP and provide call functionality by inquiry type.

**Business Rules**:
- BR-023-1: Notices: Display announcements/guidance registered by administrators
- BR-023-2: Inquiry calls: Select inquiry type → Auto-connect to assigned department
- BR-023-3: Inquiry types: Logistics control room, Materials warehouse, etc.

**Main Flow**:
1. "Notices" tab → View announcement list
2. "Inquiry" tab → Select inquiry type
3. Display contact information for selected type's assigned department
4. "Call" button → Phone connection

**Related Functions**: FUNC-025 (Landline Call Dual-Routing)

---

### FUNC-024: Push Notification Reception

| Item | Details |
|------|---------|
| **Function ID** | FUNC-024 |
| **Function Name** | Push Notification Reception |
| **PRD Mapping** | FR-003 |
| **Module** | Weighing Management Mobile APP |
| **Priority** | HIGH |

**Function Description**: Receives FCM-based Push notifications to provide real-time updates on weighing completion, dispatch changes, etc.

**Business Rules**:
- BR-024-1: Notification types: Weighing completion, dispatch registration/changes, system announcements
- BR-024-2: Add to in-APP notification list upon notification reception (tb_notification)
- BR-024-3: Unread notification badge display
- BR-024-4: Parallel KakaoTalk notification dispatch

**Main Flow**:
1. Server sends FCM Push
2. APP receives notification
3. Add to notification list + badge display
4. Click notification → Navigate to corresponding detail screen

**Related Functions**: FUNC-007 (Weighing Records), FUNC-021 (Electronic Weighing Slip)

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: Minimize Push notification delay (FCM high-reliability messages)

---

## 6. Module 5: Mobile API

### FUNC-025-API: User/Authentication API

| Item | Details |
|------|---------|
| **Function ID** | FUNC-025-API |
| **Function Name** | User/Authentication API |
| **PRD Mapping** | FR-008 |
| **Module** | Mobile API |
| **Priority** | HIGH |

**Function Description**: Provides user authentication (login, OTP, token refresh) and user information query APIs.

**API Endpoints**:

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/auth/login | ID/PW login |
| POST | /api/v1/auth/login/otp | OTP-based login |
| POST | /api/v1/auth/refresh | Token refresh |
| POST | /api/v1/auth/logout | Logout |

**Business Rules**:
- BR-API-1: JWT Access Token 30 minutes, Refresh Token 7 days (Redis session management)
- BR-API-2: RBAC permissions: ADMIN > MANAGER > DRIVER
- BR-API-3: API response time within 2 seconds

**Non-Functional Requirements Mapping**:
- NFR-001 Performance: API response within 2 seconds
- NFR-002 Security: TLS 1.3, JWT, bcrypt

---

### FUNC-026-API: Dispatch Information API

| Item | Details |
|------|---------|
| **Function ID** | FUNC-026-API |
| **Function Name** | Dispatch Information API |
| **PRD Mapping** | FR-008 |
| **Module** | Mobile API |
| **Priority** | HIGH |

**API Endpoints**:

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/dispatches | Dispatch list query |
| POST | /api/v1/dispatches | Dispatch registration |
| GET | /api/v1/dispatches/{id} | Dispatch detail query |
| PUT | /api/v1/dispatches/{id} | Dispatch modification |
| DELETE | /api/v1/dispatches/{id} | Dispatch deletion |
| GET | /api/v1/dispatches/my | My dispatch list |

---

### FUNC-027-API: Weighing Processing API

| Item | Details |
|------|---------|
| **Function ID** | FUNC-027-API |
| **Function Name** | Weighing Processing API |
| **PRD Mapping** | FR-008 |
| **Module** | Mobile API |
| **Priority** | HIGH |

**API Endpoints**:

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/v1/weighings | Start weighing |
| PUT | /api/v1/weighings/{id}/complete | Complete weighing |
| PUT | /api/v1/weighings/{id}/re-weigh | Re-weighing |
| GET | /api/v1/weighings | Weighing record list |
| GET | /api/v1/weighings/{id} | Weighing detail |
| GET | /api/v1/weighings/realtime | Real-time status (WebSocket) |
| GET | /api/v1/weighings/statistics | Statistics |

---

### FUNC-028-API: Push Notification API

| Item | Details |
|------|---------|
| **Function ID** | FUNC-028-API |
| **Function Name** | Push Notification API |
| **PRD Mapping** | FR-008 |
| **Module** | Mobile API |
| **Priority** | HIGH |

**API Endpoints**:

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/notifications | Notification list |
| PUT | /api/v1/notifications/{id}/read | Mark notification as read |
| POST | /api/v1/notifications/push/register | FCM token registration |

---

## 7. Module 6: H/W Infrastructure Integration

### FUNC-040: LPR Camera Integration

| Item | Details |
|------|---------|
| **Function ID** | FUNC-040 |
| **Function Name** | LPR Camera Integration |
| **PRD Mapping** | FR-001 |
| **Module** | H/W Infrastructure Integration |
| **Priority** | HIGH |

**Function Description**: Communicates with LPR camera via TCP/UDP to capture license plates and receive primary recognition results.

**Communication Specifications**:
- Protocol: TCP/UDP
- Connection target: Weighbridge CS program
- Data: Captured image + primary recognition plate number

**Related Functions**: FUNC-001 (LPR Capture), FUNC-011 (Automatic Weighing)

---

### FUNC-041: LiDAR/Radar Sensor Integration

| Item | Details |
|------|---------|
| **Function ID** | FUNC-041 |
| **Function Name** | LiDAR/Radar Sensor Integration |
| **PRD Mapping** | FR-001 |
| **Module** | H/W Infrastructure Integration |
| **Priority** | HIGH |

**Function Description**: Detects vehicle entry using LiDAR/radar sensor and triggers LPR capture.

**Communication Specifications**:
- Protocol: TCP/UDP
- Event: Vehicle entry detection signal

**Related Functions**: FUNC-001 (LPR Capture), FUNC-011 (Automatic Weighing)

---

### FUNC-042: Vehicle Detector Integration

| Item | Details |
|------|---------|
| **Function ID** | FUNC-042 |
| **Function Name** | Vehicle Detector Integration |
| **PRD Mapping** | FR-005 |
| **Module** | H/W Infrastructure Integration |
| **Priority** | HIGH |

**Function Description**: Confirms vehicle positioning on the weighbridge using sensor-type vehicle detector.

**Communication Specifications**:
- Type: Sensor-type
- Event: Vehicle present/absent signal

**Related Functions**: FUNC-007 (Weighing Records), FUNC-011 (Automatic Weighing)

---

### FUNC-043: Intercom Integration

| Item | Details |
|------|---------|
| **Function ID** | FUNC-043 |
| **Function Name** | Intercom Integration |
| **PRD Mapping** | FR-007 |
| **Module** | H/W Infrastructure Integration |
| **Priority** | MEDIUM |

**Function Description**: Installs one master and one slave intercom unit to support communication between the weighbridge and control room.

**Related Functions**: FUNC-025 (Landline Call Dual-Routing)

---

## 8. Module 7: Landline Call Dual-Routing System

### FUNC-025: Landline Call Dual-Routing

| Item | Details |
|------|---------|
| **Function ID** | FUNC-025 |
| **Function Name** | Landline Call Dual-Routing |
| **PRD Mapping** | FR-007 |
| **Module** | Landline Call Dual-Routing System |
| **Priority** | MEDIUM |

**Function Description**: Resolves the issue of call concentration to a specific department by routing calls to appropriate departments based on inquiry type.

**Preconditions**:
- Intercom installation completed
- Contact information by inquiry type registered in master data

**Postconditions**:
- Call history recorded (tb_inquiry_call)

**Input Data**:

| Field Name | Type | Required | Validation Rules |
|------------|------|----------|-----------------|
| inquiry_type | string | Y | Inquiry type (weighing anomaly/dispatch inquiry/gate pass inquiry, etc.) |
| target_dept | string | Y | Target department (logistics control room/materials warehouse, etc.) |

**Output Data**:

| Field Name | Type | Description |
|------------|------|-------------|
| call_id | bigint | Call history ID |
| target_phone | string | Connected phone number |

**Business Rules**:
- BR-025-1: Auto-mapping to assigned department based on inquiry type
- BR-025-2: Logistics control room: Weighing anomalies, vehicle entry inquiries
- BR-025-3: Materials warehouse: Sub-material/material-related inquiries
- BR-025-4: Call history recording (tb_inquiry_call)

**Main Flow**:
1. Driver selects inquiry type from mobile APP or intercom
2. Display contact information for assigned department based on type
3. Phone connection established
4. Call history recorded (POST /api/v1/inquiries/call-log)

**Related Functions**: FUNC-023 (Mobile Inquiry Calls), FUNC-043 (Intercom)

---

## 9. Requirements Traceability Matrix

### 9.1 FR-ID ↔ FUNC-ID Mapping Table

| PRD FR-ID | FR Name | FUNC-ID | FUNC Name | Module |
|-----------|---------|---------|-----------|--------|
| FR-001 | LPR Automatic License Plate Recognition | FUNC-001 | LPR Automatic License Plate Capture | M1: LPR |
| FR-001 | LPR Automatic License Plate Recognition | FUNC-002 | AI License Plate Verification | M1: LPR |
| FR-001 | LPR Automatic License Plate Recognition | FUNC-003 | Automatic Dispatch Matching | M1: LPR |
| FR-001 | LPR Automatic License Plate Recognition | FUNC-040 | LPR Camera Integration | M6: H/W |
| FR-001 | LPR Automatic License Plate Recognition | FUNC-041 | LiDAR/Radar Sensor Integration | M6: H/W |
| FR-002 | Mobile OTP Secure Weighing | FUNC-004 | OTP Secure Weighing | M1: LPR |
| FR-002 | Mobile OTP Secure Weighing | FUNC-020 | Mobile OTP Input | M4: Mobile |
| FR-002 | Mobile OTP Secure Weighing | FUNC-012 | Display Board Control | M3: CS |
| FR-003 | Weighing Management Mobile APP | FUNC-017 | Mobile Login | M4: Mobile |
| FR-003 | Weighing Management Mobile APP | FUNC-018 | Dispatch Query/Selection | M4: Mobile |
| FR-003 | Weighing Management Mobile APP | FUNC-019 | Mobile Weighing Progress | M4: Mobile |
| FR-003 | Weighing Management Mobile APP | FUNC-021 | Electronic Weighing Slip Query/Share | M4: Mobile |
| FR-003 | Weighing Management Mobile APP | FUNC-022 | Dispatch/Weighing Record Query | M4: Mobile |
| FR-003 | Weighing Management Mobile APP | FUNC-023 | Notices/Inquiry Calls | M4: Mobile |
| FR-003 | Weighing Management Mobile APP | FUNC-024 | Push Notification Reception | M4: Mobile |
| FR-004 | Weighing Web Management System | FUNC-005 | Dispatch Registration/Management | M2: Web |
| FR-004 | Weighing Web Management System | FUNC-006 | Weighing Status Management | M2: Web |
| FR-004 | Weighing Web Management System | FUNC-008 | Master Data Management | M2: Web |
| FR-004 | Weighing Web Management System | FUNC-030 | Gate Pass Management | M2: Web |
| FR-005 | Weighbridge CS Program | FUNC-010 | Indicator Weight Value Reception | M3: CS |
| FR-005 | Weighbridge CS Program | FUNC-011 | LPR Automatic Weighing Process | M3: CS |
| FR-005 | Weighbridge CS Program | FUNC-013 | Automatic Barrier Control | M3: CS |
| FR-005 | Weighbridge CS Program | FUNC-014 | Manual Weighing Mode | M3: CS |
| FR-005 | Weighbridge CS Program | FUNC-015 | Reset/Re-weighing | M3: CS |
| FR-005 | Weighbridge CS Program | FUNC-016 | Local Caching/Offline Mode | M3: CS |
| FR-005 | Weighbridge CS Program | FUNC-042 | Vehicle Detector Integration | M6: H/W |
| FR-006 | Electronic Weighing Slip/Paperless | FUNC-009 | Electronic Weighing Slip Generation/Query | M2: Web |
| FR-006 | Electronic Weighing Slip/Paperless | FUNC-021 | Electronic Weighing Slip Query/Share (Mobile) | M4: Mobile |
| FR-007 | Landline Call Dual-Routing/Inquiry | FUNC-025 | Landline Call Dual-Routing | M7: Call |
| FR-007 | Landline Call Dual-Routing/Inquiry | FUNC-023 | Notices/Inquiry Calls (Mobile) | M4: Mobile |
| FR-007 | Landline Call Dual-Routing/Inquiry | FUNC-043 | Intercom Integration | M6: H/W |
| FR-008 | Mobile API | FUNC-025-API | User/Authentication API | M5: API |
| FR-008 | Mobile API | FUNC-026-API | Dispatch Information API | M5: API |
| FR-008 | Mobile API | FUNC-027-API | Weighing Processing API | M5: API |
| FR-008 | Mobile API | FUNC-028-API | Push Notification API | M5: API |

### 9.2 Mapping Verification Results

| PRD FR-ID | Function Count | Coverage | Status |
|-----------|---------------|----------|--------|
| FR-001 | 5 | 100% | COVERED |
| FR-002 | 3 | 100% | COVERED |
| FR-003 | 7 | 100% | COVERED |
| FR-004 | 4 | 100% | COVERED |
| FR-005 | 7 | 100% | COVERED |
| FR-006 | 2 | 100% | COVERED |
| FR-007 | 3 | 100% | COVERED |
| FR-008 | 4 | 100% | COVERED |

**A total of 35 functional specifications cover all 8 PRD functional requirements at 100%.**

### 9.3 NFR Mapping Verification

| NFR-ID | NFR Name | Related FUNC | Reflection Status |
|--------|----------|-------------|------------------|
| NFR-001 | Performance | FUNC-001,002,003,004,006,007,010,024 | COVERED |
| NFR-002 | Security | FUNC-004,009,017,025-API | COVERED |
| NFR-003 | Scalability | FUNC-003,008 | COVERED |
| NFR-004 | Availability | FUNC-001,007,010,011,016 | COVERED |
| NFR-005 | Usability | FUNC-005,006,017 | COVERED |

### 9.4 TRD Technical Constraints Reflection Verification

| Constraint | Reflected FUNC | Confirmed |
|------------|---------------|-----------|
| Spring Boot 3.2 Backend | FUNC-025~028-API, FUNC-005~009 | OK |
| React 18 + Ant Design Web | FUNC-005,006,008,009,030 | OK |
| Flutter 3.x Mobile | FUNC-017~024 | OK |
| C# .NET WinForms CS | FUNC-010~016 | OK |
| PostgreSQL 16 DB | All data CRUD functions | OK |
| Redis 7 Cache/OTP | FUNC-004,008,016 | OK |
| RS-232C Indicator | FUNC-010 | OK |
| TCP/UDP LPR/Sensor | FUNC-040,041,042 | OK |
| JWT Authentication | FUNC-017,025-API | OK |
| WebSocket Real-time | FUNC-006 | OK |
| FCM Push Notification | FUNC-024,028-API | OK |
| KakaoTalk/SMS Integration | FUNC-009,021 | OK |

---

---

## 10. Implementation Status

> **Last Updated**: 2026-01-29

### 10.1 Web Frontend Screen Implementation Status

| Path | Screen Name | FUNC Mapping | Implementation Status | Notes |
|------|------------|-------------|----------------------|-------|
| `/login` | Login | FUNC-025-API | ✅ Complete | JWT authentication, theme integration |
| `/dashboard` | Dashboard | FUNC-006 | ✅ Complete | 3-tab structure (Overview/Realtime/Analysis), ECharts 6.0 |
| `/dispatch` | Dispatch Management | FUNC-005 | ✅ Complete | CRUD, search/filter/pagination |
| `/weighing` | Weighing Status | FUNC-006 | ✅ Complete | Real-time status, WebSocket |
| `/inquiry` | Weighing Query | FUNC-006 | ✅ Complete | Advanced search, Excel export |
| `/gate-pass` | Gate Pass Management | FUNC-030 | ✅ Complete | Approval/rejection process |
| `/slips` | Electronic Weighing Slip | FUNC-009 | ✅ Complete | Query/share/print |
| `/statistics` | Statistics/Reports | FUNC-006 | ✅ Complete | Period/condition-based analysis charts |
| `/weighing-station` | Weighing Station Control | FUNC-011 | ✅ Complete | Fixed tab, real-time equipment integration |
| `/monitoring` | Equipment Monitoring | FUNC-010 | ✅ Complete | Equipment status monitoring |
| `/master/companies` | Transport Company Management | FUNC-008 | ✅ Complete | MasterCrudPage pattern |
| `/master/vehicles` | Vehicle Management | FUNC-008 | ✅ Complete | MasterCrudPage pattern |
| `/master/scales` | Weighbridge Management | FUNC-008 | ✅ Complete | MasterCrudPage pattern |
| `/master/codes` | Common Code Management | FUNC-008 | ✅ Complete | MasterCrudPage pattern |
| `/notices` | Notices | - | ✅ Complete | Category filter, pinned notices |
| `/help` | Help Guide | - | ✅ Complete | Help/FAQ |
| `/mypage` | My Page | - | ✅ Complete | Profile, password change |
| `/admin/users` | User Management | FUNC-008 | ✅ Complete | ADMIN only |
| `/admin/settings` | System Settings | - | ✅ Complete | ADMIN only |
| `/admin/audit-logs` | Audit Logs | - | ✅ Complete | ADMIN only |

### 10.2 Mobile App Screen Implementation Status

| Screen | FUNC Mapping | Implementation Status | Notes |
|--------|-------------|----------------------|-------|
| Login (ID/PW) | FUNC-017 | ✅ Complete | flutter_secure_storage |
| OTP Login | FUNC-017 | ✅ Complete | OTP-based simple login |
| Home Screen | - | ✅ Complete | Dashboard, quick access |
| Dispatch List | FUNC-018 | ✅ Complete | Today's dispatch filter |
| Dispatch Detail | FUNC-018 | ✅ Complete | Dispatch info, weighing navigation |
| OTP Input | FUNC-020 | ✅ Complete | 6-digit input, timer |
| Weighing Progress | FUNC-019 | ✅ Complete | Real-time progress status display |
| Electronic Weighing Slip List | FUNC-021 | ✅ Complete | Date-based query |
| Electronic Weighing Slip Detail | FUNC-021 | ✅ Complete | Sharing (share_plus) |
| History Query | FUNC-022 | ✅ Complete | Period-based search |
| Notices | - | ✅ Complete | Category-based query |
| Notification List | FUNC-024 | ✅ Complete | FCM push + in-app notifications |

### 10.3 Desktop (CS) Implementation Status

| Function | FUNC Mapping | Implementation Status | Notes |
|----------|-------------|----------------------|-------|
| Splash Screen | - | ✅ Complete | Initialization/connection check |
| Main Weighing Screen | FUNC-010,011 | ✅ Complete | Real-time weight display |
| Indicator Communication | FUNC-010 | ✅ Complete | SerialPort + Simulator |
| LPR Camera Integration | FUNC-040 | ✅ Complete | Interface + Simulator |
| Vehicle Detector Integration | FUNC-042 | ✅ Complete | Interface + Simulator |
| Display Board Control | FUNC-012 | ✅ Complete | TCP communication |
| Barrier Control | FUNC-013 | ✅ Complete | TCP communication |
| Weighing Process | FUNC-011 | ✅ Complete | WeighingProcessService orchestrator |
| Local Cache | FUNC-016 | ✅ Complete | SQLite-based |
| API Server Integration | FUNC-015 | ✅ Complete | HttpClient + JWT |
| Unit Tests | - | ✅ Complete | xUnit (3 test classes) |

### 10.4 Backend Module Implementation Status

| Module | Primary FUNC | Implementation Status | Notes |
|--------|-------------|----------------------|-------|
| auth | FUNC-017, 025-API | ✅ Complete | JWT + OTP login + Redis blacklist |
| user | FUNC-008 | ✅ Complete | CRUD + role management |
| master | FUNC-008 | ✅ Complete | Transport company/vehicle/weighbridge/common code |
| dispatch | FUNC-005 | ✅ Complete | CRUD + search + status management |
| weighing | FUNC-007 | ✅ Complete | Weighing process + statistics |
| gatepass | FUNC-030 | ✅ Complete | Gate pass approval/rejection process |
| slip | FUNC-009 | ✅ Complete | Electronic weighing slip + sharing |
| lpr | FUNC-001,002,003 | ✅ Complete | Capture/AI verification/dispatch matching |
| otp | FUNC-004 | ✅ Complete | Redis-based OTP management |
| notification | FUNC-024, 028-API | ✅ Complete | FCM + in-app notification |
| websocket | FUNC-006 | ✅ Complete | Real-time weighing/equipment status transmission |
| dashboard | FUNC-006 | ✅ Complete | Statistics API |
| audit | - | ✅ Complete | Audit logs |

### 10.5 Additional Implemented Features (Beyond Specification)

| Feature | Description | Implementation Location |
|---------|-------------|------------------------|
| Onboarding Tour | New user guide | `OnboardingTour.tsx` |
| Keyboard Shortcuts | Page-specific shortcut support | `useKeyboardShortcuts.ts` |
| Tab Activation Detection | Data refresh on browser tab switch | `useTabVisible.ts` |
| Number Animation | Dashboard KPI card animation | `AnimatedNumber.tsx` |
| Drag Sort | Table row drag reordering | `SortableTable.tsx` (@dnd-kit) |
| Empty State UI | Guidance screen when no data | `EmptyState.tsx` |
| Favorites | Dispatch/company favorites | `FavoriteButton.tsx`, `FavoritesList.tsx` |
| Dark/Light Theme | Theme switching support | `ThemeContext.tsx`, `themeConfig.ts` |
| Multi-Tab Navigation | Up to 10 tabs, pinned tab support | `TabContext.tsx`, `pageRegistry.ts` |
| Offline Cache (Mobile) | SharedPreferences-based | `offline_cache_service.dart` |
| Hardware Simulator | Equipment simulation for development | `Simulators/*.cs` |

---

*This document was prepared based on PRD-20260127-154446, TRD-20260127-155235, WBS-20260127-160043.*
*Implementation status is as of 2026-01-29.*
