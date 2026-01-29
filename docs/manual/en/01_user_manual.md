# Busan Smart Weighing System - User Manual

| Item | Details |
|------|---------|
| **Document Title** | Busan Smart Weighing System User Manual |
| **Version** | 1.2 |
| **Last Updated** | 2026-01-29 |
| **Target Audience** | Drivers (DRIVER), Weighing Managers (MANAGER) |
| **Security Level** | Internal Use Only |

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Getting Started](#2-getting-started)
3. [Driver Functions](#3-driver-functions)
4. [Manager Functions](#4-manager-functions)
5. [Common Functions](#5-common-functions)
6. [Frequently Asked Questions (FAQ)](#6-frequently-asked-questions-faq)
7. [Troubleshooting Guide](#7-troubleshooting-guide)
8. [Glossary](#8-glossary)
9. [Document History](#document-history)

---

## 1. System Overview

### 1.1 Introduction

The **Busan Smart Weighing System** is an integrated solution that automates vehicle weighing operations at the Busan steelworks. By combining LPR (License Plate Recognition) technology with AI, the system automatically recognizes license plates when vehicles enter the weighbridge, matches them to dispatch records, and performs weighing.

This system aims to digitize the previously manual, paper-based weighing processes to improve operational efficiency and ensure data accuracy and transparency.

### 1.2 Key Features

| Feature | Description |
|---------|-------------|
| **LPR Automated Weighing** | AI-based automatic license plate recognition (accuracy over 95%) for unmanned weighing |
| **Mobile OTP Backup** | Alternative weighing via mobile OTP when LPR recognition fails |
| **Electronic Weighing Slips** | Automatic generation and sharing of electronic weighing slips to replace paper slips |
| **Real-time Monitoring** | WebSocket-based real-time monitoring of equipment status and weighing progress |
| **Statistics/Analysis** | Weighing statistics by day, month, item type, and company with Excel download |

### 1.3 System Components

This system provides two access methods:

- **Web Management System**: Management interface accessed via a PC browser (primarily for managers)
- **Mobile App**: Application installed on smartphones (primarily for drivers)

### 1.4 User Roles

The system supports three user roles. This manual focuses on the **Driver** and **Manager** roles.

| Role | Description | Primary Environment |
|------|-------------|---------------------|
| **Administrator (ADMIN)** | Overall system configuration and user management | Web |
| **Manager (MANAGER)** | Dispatch, weighing, gate pass, master data, and statistics management | Web |
| **Driver (DRIVER)** | Dispatch review, weighing, and electronic weighing slip review | Mobile App |

### 1.5 Weighing Workflow

The complete weighing workflow proceeds through the following steps:

```
Dispatch Registration -> Vehicle Entry -> 1st Weighing (Tare) -> Loading/Unloading -> 2nd Weighing (Gross) -> Electronic Weighing Slip Issuance -> Gate Exit
```

**Note**: Depending on the item type, a 3rd weighing may be added. The net weight is automatically calculated as |1st Weight - 2nd Weight|.

---

## 2. Getting Started

### 2.1 System Requirements

#### Web Management System (For Managers)

| Item | Requirement |
|------|-------------|
| **Operating System** | Windows 10 or later, macOS 12 or later |
| **Browser** | Chrome 90 or later (recommended), Edge 90 or later, Firefox 90 or later |
| **Screen Resolution** | 1920x1080 or higher recommended |
| **Network** | Internal network connection required |
| **Other** | JavaScript enabled, pop-up blocker disabled |

#### Mobile App (For Drivers)

| Item | Requirement |
|------|-------------|
| **Android** | Android 10 (API 29) or later |
| **iOS** | iOS 14.0 or later |
| **Storage** | At least 100MB of free space |
| **Network** | Mobile data or Wi-Fi connection |
| **Permissions** | Camera and notification permissions required |

### 2.2 Login

#### 2.2.1 Web Login (Manager)

1. Open a browser and navigate to the system URL.
2. The **Login** screen (`/login`) is displayed.
3. Enter your assigned user ID in the **User ID** field.
4. Enter your password in the **Password** field.
5. Click the **Login** button.
6. Upon successful authentication, you will be redirected to the **Dashboard** (`/dashboard`).

> **Important**
> - After 5 consecutive incorrect password attempts, your account will be **locked for 30 minutes**.
> - A locked account is automatically unlocked after 30 minutes. If immediate unlocking is needed, contact the administrator.
> - After logging in, you will be automatically logged out after **30 minutes** of inactivity (access token expiration). If you reconnect within 7 days, the session is automatically renewed.

#### 2.2.2 Mobile App Login (Driver)

1. Launch the **Busan Smart Weighing** app on your smartphone.
2. Enter your **User ID** and **Password**.
3. Tap the **Login** button.
4. Upon successful authentication, you will be redirected to the main screen.

> **Note**: The mobile app supports automatic login. After the initial login, automatic login is maintained for 7 days (based on the refresh token).

#### 2.2.3 Mobile OTP Login

When LPR automatic recognition fails, you can perform weighing using the OTP (One-Time Password) displayed on the on-site LED display board.

1. After entering the weighbridge, check the **6-digit OTP code** displayed on the LED display board.
2. Select the **OTP Weighing** menu in the mobile app.
3. Enter the 6-digit OTP code.
4. Tap the **Confirm** button.

> **Important**
> - The OTP must be entered within **5 minutes** of issuance. A new OTP will be issued upon expiration.
> - After **3 consecutive** incorrect OTP entries, the OTP is invalidated. Please contact the weighing manager.

### 2.3 Logout

#### Web Logout

1. Click the **User icon** in the upper right corner of the screen.
2. Select **Logout** from the dropdown menu.
3. You will be redirected to the login screen (`/login`).

#### Mobile App Logout

1. Select the **My Page** tab from the bottom navigation.
2. Tap the **Logout** button.
3. Tap **Confirm** on the confirmation popup.

### 2.4 Screen Layout Guide

#### 2.4.1 Web Screen Layout (Manager)

The web management system screen is organized as follows:

```
+------------------------------------------------------------------+
|  [Logo]     Busan Smart Weighing System     [Alerts] [User Menu]  |  <- Top Header
+----------+-------------------------------------------------------+
|          |                                                        |
| Dashboard|                                                        |
| Dispatch |              Main Content Area                         |
| Weighing |                                                        |
| e-Slip   |                                                        |
| Gate Pass|                                                        |
| Master  >|                                                        |
| Stats    |                                                        |
| Monitor  |                                                        |
| Notices  |                                                        |
| Help     |                                                        |
|          |                                                        |
+----------+-------------------------------------------------------+
```

| Area | Description |
|------|-------------|
| **Top Header** | System logo, notification icon (displays unread notification count), user menu (My Page, Logout) |
| **Left Navigation** | Main menu list. Master Data includes sub-menus (Company, Vehicle, Scale, Common Code) |
| **Main Content Area** | Area displaying detailed functions of the selected menu |

> **Note**: The web system supports **Dark Mode** and **Light Mode**. You can switch themes from the user menu in the upper right corner.

#### 2.4.2 Mobile App Screen Layout (Driver)

```
+---------------------------+
|  Busan Smart Weighing      |  <- Top Bar
+---------------------------+
|                           |
|    Main Content Area       |
|                           |
|                           |
+---------------------------+
| Home | Dispatch | Weigh | My |  <- Bottom Navigation
+---------------------------+
```

| Area | Description |
|------|-------------|
| **Top Bar** | Screen title, back button, notification icon |
| **Main Content Area** | Detailed content for each function |
| **Bottom Navigation** | Home, Dispatch, Weighing, My Page tabs |

#### 2.4.3 Page Structure Guide

| Page | URL | Function | Target |
|------|-----|----------|--------|
| Login | `/login` | User authentication | All |
| Dashboard | `/dashboard` | Overview and real-time data | Manager |
| Dispatch Management | `/dispatch` | Dispatch registration/view/edit/delete | Manager |
| Weighing Management | `/weighing` | Weighing record view and management | Manager |
| Weighing Station Status | `/weighing-station` | Real-time weighbridge monitoring | Manager |
| Electronic Weighing Slip | `/slip` | View and share electronic weighing slips | All |
| Gate Pass Management | `/gate-pass` | Gate pass approval/rejection | Manager |
| Statistics | `/statistics` | Reports and analysis | Manager |
| Monitoring | `/monitoring` | Equipment status monitoring | Manager |
| Notices | `/notice` | View announcements | All |
| Inquiries | `/inquiry` | Contact logistics control room/material warehouse | All |
| My Page | `/mypage` | Personal information and settings | All |
| Master Data - Company | `/master/company` | Company management | Manager |
| Master Data - Vehicle | `/master/vehicle` | Vehicle information management | Manager |
| Master Data - Scale | `/master/scale` | Scale management | Manager |
| Master Data - Common Code | `/master/code` | Common code management | Manager |
| Help/FAQ | `/help` | Frequently asked questions | All |

---

## 3. Driver Functions

Drivers primarily use the **mobile app** to interact with the system. The main functions are dispatch inquiry, weighing, and electronic weighing slip viewing and sharing.

### 3.1 Dispatch Inquiry

The dispatch inquiry function allows you to check dispatches (transport orders) assigned to you.

#### How to View Dispatches

1. Select the **Dispatch** tab from the bottom navigation of the mobile app.
2. The dispatch list is displayed. Each item contains the following information:

   | Item | Description |
   |------|-------------|
   | Dispatch Number | Unique dispatch identification number |
   | Status | Registered (REGISTERED), In Progress (IN_PROGRESS), Completed (COMPLETED), Cancelled (CANCELLED) |
   | Item Type | Byproduct, Waste, Sub-material, Export, General |
   | Loading/Unloading Location | Origin and destination information |
   | Dispatch Date | Scheduled dispatch date |
   | Vehicle Number | Assigned vehicle license plate number |

3. Tap a specific dispatch to navigate to the **Dispatch Detail** screen for detailed information.

#### Dispatch Status Description

```
Registered (REGISTERED) -> In Progress (IN_PROGRESS) -> Completed (COMPLETED)
                                                      -> Cancelled (CANCELLED)
```

| Status | Description |
|--------|-------------|
| **Registered (REGISTERED)** | Initial state after the manager registers the dispatch. Weighing has not yet started |
| **In Progress (IN_PROGRESS)** | The 1st weighing has started and the weighing process is underway |
| **Completed (COMPLETED)** | All weighing is complete and an electronic weighing slip has been issued |
| **Cancelled (CANCELLED)** | The dispatch has been cancelled. The reason can be viewed on the detail screen |

> **Note**: The dispatch list is sorted by most recent dispatch date by default. You can use the filter at the top to search by status.

### 3.2 LPR Automated Weighing

LPR (License Plate Recognition) automated weighing is the core function where the AI automatically recognizes the license plate when a vehicle enters the weighbridge and performs weighing. No manual intervention is required from the driver.

#### Automated Weighing Procedure

The following process occurs automatically after a vehicle enters the weighbridge:

**Step 1: Vehicle Entry Detection**
- When a vehicle enters the weighbridge, a **LiDAR sensor** automatically detects the vehicle's presence.
- The LED display board shows the message "Detecting vehicle...".

**Step 2: License Plate Recognition**
- The LPR camera photographs the vehicle's license plate.
- The AI engine analyzes the license plate image and recognizes the vehicle number (accuracy over 95%).

**Step 3: Automatic Dispatch Matching**
- Based on the recognized vehicle number, the system automatically searches for and matches registered dispatch information.
- Upon successful matching, the LED display board shows the vehicle number and dispatch information.

**Step 4: Weight Measurement**
- The weighing indicator reads weight data via RS-232C communication.
- Once the weight value stabilizes, the measurement is automatically confirmed.

**Step 5: Data Storage and Slip Issuance**
- The measured weight is automatically saved to the server.
- If the weighing is the final measurement (2nd or 3rd), an **electronic weighing slip** is automatically generated.
- A notification is sent to the mobile app.

**Step 6: Vehicle Exit**
- The LED display board shows the "Weighing Complete" message and the measured weight.
- When the barrier opens, exit the weighbridge safely.

#### Driver Checklist

To ensure smooth automated weighing, please verify the following:

1. Ensure the vehicle **license plate is clean** (recognition accuracy decreases with dirt, obstruction).
2. **Park the vehicle accurately** on the weighbridge.
3. **Keep the engine running and wait** during weighing.
4. Follow the instructions on the LED display board.
5. Exit only after the "Weighing Complete" message is displayed.

> **Caution**: If you exit the vehicle or move the vehicle during weighing, accurate weight measurement will not be possible. Always confirm the weighing completion message before exiting.

#### Weighing Order Guide

| Weighing Order | Description | Typical State |
|----------------|-------------|---------------|
| **1st Weighing** | First weight measurement (typically tare weight) | Before loading or before unloading |
| **2nd Weighing** | Second weight measurement (typically gross weight) | After loading or after unloading |
| **3rd Weighing** | Additional weighing if required | Special items or re-weighing |

> **Note**: **Net Weight** = |1st Weight - 2nd Weight|, calculated automatically.

### 3.3 Mobile OTP Weighing

When LPR automatic recognition fails or under special circumstances, you can use mobile OTP to perform weighing.

#### When OTP Weighing Is Needed

- When LPR recognition fails due to dirty, damaged, or obstructed license plates
- When the vehicle uses a temporary license plate
- When automatic recognition is unavailable due to a temporary system error

#### OTP Weighing Procedure

**Step 1: Check the OTP Code**
1. After the vehicle enters the weighbridge and LPR recognition fails, a **6-digit OTP code** is displayed on the LED display board.
2. Note or memorize the displayed OTP code.

**Step 2: Enter OTP in the Mobile App**
1. Launch the mobile app.
2. Tap the **OTP Weighing** button on the main screen or the weighing tab.
3. Enter the 6-digit OTP code.
4. Tap the **Confirm** button.

**Step 3: Identity Verification**
1. Identity verification is performed using the registered mobile phone number.
2. Once verification is complete, the process automatically proceeds to the next step.

**Step 4: Select Dispatch**
1. The list of dispatches assigned to you is displayed.
2. Select the **dispatch** to be weighed.
3. Tap the **Start Weighing** button.

**Step 5: Perform Weighing**
1. The system measures the weight in conjunction with the weighing indicator.
2. The measured weight is displayed on the app screen.
3. Verify the weight value and tap the **Confirm** button.

**Step 6: Weighing Complete**
1. The weighing data is saved to the server.
2. If this is the final weighing, an electronic weighing slip is automatically generated.
3. The LED display board shows the "Weighing Complete" message.
4. Exit the weighbridge safely.

> **Important**
> - The OTP code is valid for **5 minutes** after issuance. If expired, check the new OTP on the LED display board.
> - After **3 consecutive** incorrect OTP entries, the OTP is invalidated. In this case, contact the weighing manager (logistics control room) to request manual processing.
> - If the app closes during OTP weighing, you must start the process over from the beginning.

### 3.4 Viewing and Sharing Electronic Weighing Slips

An electronic weighing slip is automatically generated when weighing is completed. Drivers can view and share the slip through the mobile app.

#### 3.4.1 Viewing Electronic Weighing Slips

1. Select the **Weighing** tab from the bottom navigation of the mobile app.
2. The list of completed weighings is displayed.
3. Tap the weighing record you want to view.
4. Tap the **View Electronic Weighing Slip** button.
5. The electronic weighing slip is displayed on the screen.

The electronic weighing slip contains the following information:

| Item | Description |
|------|-------------|
| Slip Number | Unique identification number |
| Dispatch Number | Related dispatch number |
| Vehicle Number | Vehicle number for the weighing |
| Driver Information | Driver name, affiliated company |
| Item Type | Byproduct, Waste, Sub-material, Export, General |
| 1st Weight | First weighing value (kg) |
| 2nd Weight | Second weighing value (kg) |
| 3rd Weight | Third weighing value (if applicable, kg) |
| Net Weight | Automatically calculated net weight (kg) |
| Weighing Date/Time | Date and time of each weighing order |
| Weighing Mode | LPR (automatic), Mobile (OTP), Manual |
| Remarks | Special notes field |

#### 3.4.2 Sharing Electronic Weighing Slips

Electronic weighing slips can be shared with business partners or relevant parties.

**Sharing via KakaoTalk**

1. Tap the **Share** button on the electronic weighing slip detail screen.
2. Select **KakaoTalk** on the sharing method selection screen.
3. The KakaoTalk app launches and you can select the recipient (friend or chat room).
4. Tap the **Send** button.

**Sharing via SMS**

1. Tap the **Share** button on the electronic weighing slip detail screen.
2. Select **SMS** on the sharing method selection screen.
3. Enter the recipient's phone number or select from contacts.
4. Tap the **Send** button.

> **Note**: When sharing, key information from the electronic weighing slip (slip number, vehicle number, net weight, weighing date/time) and a link to view the full details are included.

### 3.5 Favorites

You can register frequently checked dispatches or companies as favorites for quick access.

#### Adding Favorites

1. Navigate to the detail screen of the desired item in the dispatch list, company information, etc.
2. Tap the **Star icon** (☆) on the screen.
3. When the icon changes to a filled star (★), the item has been added to favorites.
4. Tap again to remove from favorites.

#### Viewing Favorites List

1. Check the **Favorites** section on the mobile app main screen (Home).
2. Registered favorites are displayed by type (dispatch, company, vehicle).
3. Tap an item to navigate directly to its detail screen.

> **Note**: You can change the order of the favorites list. Long-press an item in the list and drag to rearrange the order.

### 3.6 Checking Notifications

You can receive key weighing-related information in real-time through system notifications.

1. Tap the **Notification icon** (bell shape) at the top of the mobile app.
2. The notification list is displayed.
3. Unread notifications are highlighted.
4. Tap a notification to navigate to the corresponding screen (dispatch detail, weighing slip, etc.).

Main notification types:

| Notification Type | Content |
|-------------------|---------|
| Dispatch Notification | When a new dispatch is assigned |
| Weighing Complete | When automated weighing is completed |
| Slip Issuance | When an electronic weighing slip is generated |
| Announcement | When a new announcement is posted |
| System Notification | Account-related notices, system maintenance notices, etc. |

### 3.7 Making Inquiries (Phone Inquiries)

If you encounter issues or need assistance while using the system, you can use the inquiry function to make a direct phone call to the responsible department.

1. Tap **Make Inquiry** on the mobile app main screen or My Page.
2. Select the inquiry target.

   | Inquiry Target | Responsibilities |
   |----------------|------------------|
   | **Logistics Control Room** | Weighing-related inquiries, OTP errors, equipment issues, etc. |
   | **Material Warehouse** | Item-related inquiries, loading/unloading inquiries, etc. |

3. Tapping the **Call** button for the selected department launches the phone app.
4. After the call ends, you return to the app, and the **call record** is automatically saved.
5. Previous call history can be viewed in the **My Call Records** list on the inquiry screen.

> **Note**: In urgent situations (equipment failure, safety issues, etc.), call the logistics control room immediately.

### 3.8 Viewing Weighing History

You can view your past weighing and dispatch history through the mobile app.

1. Select the **Home** tab from the bottom navigation of the mobile app.
2. Tap the **View History** menu.
3. Weighing completion counts and dispatch history by period are displayed.
4. Tap a specific record to view detailed information about the weighing or dispatch.

---

## 4. Manager Functions

Managers use the **web management system** to manage overall weighing operations including dispatch, weighing, gate passes, master data, statistics, and monitoring.

### 4.1 Dashboard

The Dashboard (`/dashboard`) is the first screen displayed after login and provides an at-a-glance overview of the overall weighing operations.

#### Dashboard Components

| Component | Description |
|-----------|-------------|
| **Today's Weighing Status** | Displays today's total weighing count, completed count, and in-progress count |
| **Monthly Weighing Status** | Monthly cumulative weighing count and total weight |
| **Real-time Chart** | Displays weighing trends by time period in a real-time graph (ECharts-based) |
| **Company Statistics** | Summary of weighing results by company (count, weight) |
| **In-Progress Weighing** | List of weighings currently in progress at the weighbridge (real-time updates) |
| **Announcements** | Summary of recently posted announcements (up to 5, pinned announcements shown first) |
| **Equipment Status Summary** | Status of key equipment: weighing indicators, LPR cameras, LiDAR, etc. |

#### How to Use the Dashboard

1. Click **Dashboard** in the left menu, or it is automatically displayed after login.
2. Click each status card to navigate to the corresponding detail screen.
   - Clicking "Today's Weighing Status" navigates to the Weighing Management (`/weighing`) screen
   - Clicking an "In-Progress Weighing" item displays the detailed weighing information
   - Clicking "Announcements" navigates to the Announcements (`/notice`) screen
3. The real-time chart is automatically updated via **WebSocket**, so no manual refresh is needed.

> **Note**: Dashboard data is refreshed in real-time. If the display appears abnormal, refresh the browser (F5).

### 4.2 Dispatch Management

Dispatch Management (`/dispatch`) is the function for registering and managing vehicle transport orders.

#### 4.2.1 Dispatch Registration

1. Click **Dispatch Management** in the left menu.
2. Click the **New Registration** button in the upper right of the dispatch list screen.
3. Fill in the following information on the dispatch registration form:

   | Input Field | Required | Description |
   |-------------|----------|-------------|
   | Dispatch Date | Required | Scheduled dispatch date (calendar selection) |
   | Item Type | Required | Select from: Byproduct, Waste, Sub-material, Export, General |
   | Company | Required | Select a company registered in master data |
   | Vehicle Number | Required | Select a vehicle registered in master data |
   | Driver | Required | Select the driver assigned to the vehicle |
   | Loading Location | Required | Origin (loading site) |
   | Unloading Location | Required | Destination (unloading site) |
   | Remarks | Optional | Special notes |

4. After verifying the input, click the **Save** button.
5. A confirmation message "Dispatch has been registered." is displayed.
6. The registered dispatch is added to the list in **Registered (REGISTERED)** status.

> **Caution**: If there is an in-progress dispatch for the same vehicle on the same date, a duplicate warning message may appear. Verify before proceeding with registration.

#### 4.2.2 Dispatch Search and Inquiry

1. Click **Dispatch Management** in the left menu.
2. The dispatch list is displayed in table format.
3. Use the search criteria at the top to find the desired dispatch.

   | Search Criteria | Description |
   |-----------------|-------------|
   | Period | Start date to end date based on dispatch date |
   | Status | Registered, In Progress, Completed, Cancelled (multiple selection available) |
   | Item Type | Byproduct, Waste, Sub-material, Export, General |
   | Company | Search by company name |
   | Vehicle Number | Search by vehicle number |
   | Driver | Search by driver name |

4. Click the **Search** button after entering the criteria.
5. Click the **Reset** button to clear all search criteria.

#### 4.2.3 Dispatch Modification

1. Click on the dispatch to modify in the dispatch list to navigate to the detail screen.
2. Click the **Edit** button.
3. Change the values of the fields that need modification.
4. Click the **Save** button.

> **Caution**: Dispatches in **In Progress (IN_PROGRESS)** or **Completed (COMPLETED)** status cannot be modified. If modification is needed, contact the administrator.

#### 4.2.4 Dispatch Deletion

1. Click on the dispatch to delete in the dispatch list to navigate to the detail screen.
2. Click the **Delete** button.
3. A confirmation popup "Are you sure you want to delete?" is displayed.
4. Click **Confirm** to complete the deletion.

> **Caution**: Only dispatches in **Registered (REGISTERED)** status can be deleted. Deleted data cannot be recovered, so proceed with caution.

#### 4.2.5 Dispatch Status Change

Dispatch status is automatically changed as weighing progresses. Manual status changes can be made when necessary.

| Current Status | Available Status Change | Description |
|----------------|------------------------|-------------|
| Registered (REGISTERED) | Cancelled (CANCELLED) | Cancel the dispatch |
| In Progress (IN_PROGRESS) | Cancelled (CANCELLED) | Cancel an in-progress dispatch (reason required) |

1. Click the **Change Status** button on the dispatch detail screen.
2. Select the status to change to.
3. When cancelling, enter the **cancellation reason**.
4. Click the **Confirm** button.

> **Note**: The change from Registered to In Progress status is automatically handled by the system when the 1st weighing starts. The change to Completed status is also automatically handled when the final weighing is completed.

### 4.3 Weighing Management

Weighing Management (`/weighing`) is the function for viewing and managing all weighing records.

#### 4.3.1 Weighing Record Inquiry

1. Click **Weighing Management** in the left menu.
2. The weighing record list is displayed.
3. Use the search criteria at the top to query records.

   | Search Criteria | Description |
   |-----------------|-------------|
   | Period | Start date to end date based on weighing date/time |
   | Weighing Mode | LPR (automatic), Mobile (OTP), Manual |
   | Item Type | Byproduct, Waste, Sub-material, Export, General |
   | Vehicle Number | Search by vehicle number |
   | Company | Search by company name |
   | Status | In Progress, Completed |

4. Click the **Search** button.

#### 4.3.2 Weighing Detail Information

Click a specific record in the weighing list to view its details.

| Information Category | Detail Items |
|---------------------|--------------|
| **Basic Information** | Weighing number, dispatch number, vehicle number, driver, company, item type |
| **1st Weighing** | 1st weight (kg), weighing date/time, weighing mode (LPR/Mobile/Manual), scale information |
| **2nd Weighing** | 2nd weight (kg), weighing date/time, weighing mode, scale information |
| **3rd Weighing** | 3rd weight (kg), weighing date/time, weighing mode, scale information (if applicable) |
| **Result** | Net weight (|1st - 2nd|), weighing completion status |
| **LPR Information** | Recognized license plate image, recognition confidence (%), AI model version |
| **Remarks** | Special notes, modification history |

#### 4.3.3 Manual Weighing

When both automatic (LPR) and mobile (OTP) weighing are unavailable, the manager can manually enter weighing data.

1. Click the **Manual Weighing** button on the weighing management screen.
2. Enter the following information:

   | Input Field | Required | Description |
   |-------------|----------|-------------|
   | Select Dispatch | Required | Select from the in-progress dispatch list |
   | Weighing Order | Required | Select from 1st, 2nd, or 3rd |
   | Weight | Required | Manually enter the weight shown on the weighing indicator (kg) |
   | Scale | Required | Select the scale used |
   | Reason | Required | Enter the reason for manual weighing |

3. After verifying the input, click the **Save** button.

> **Caution**: Manual weighing should only be used when automatic/mobile weighing is unavailable. Manual weighing records are marked as "Manual" along with the recorded reason.

#### 4.3.4 Re-weighing

If re-weighing is needed for a previously measured weighing, proceed as follows:

1. Click the **Re-weigh** button on the weighing detail screen.
2. Enter the reason for re-weighing.
3. Click the **Confirm** button.
4. The weighing for that order is reset, and a new weighing will be performed when the vehicle re-enters the weighbridge.

> **Note**: Re-weighing history is recorded in the system, and previous weighing data is also preserved as history.

### 4.4 Electronic Weighing Slip Management

In Electronic Weighing Slip Management (`/slip`), you can view all issued electronic weighing slips and manage sharing history.

#### 4.4.1 Viewing Electronic Weighing Slips

1. Click **Electronic Weighing Slip** in the left menu.
2. The list of issued electronic weighing slips is displayed.
3. Use the search criteria (period, vehicle number, company, item type) to query.
4. Click a specific slip in the list to view its details.

#### 4.4.2 Sharing History

1. Click the **Sharing History** tab on the electronic weighing slip detail screen.
2. The sharing history for the slip is displayed.

   | History Item | Description |
   |-------------|-------------|
   | Sharing Date/Time | Date and time of sharing |
   | Sharing Method | KakaoTalk, SMS, etc. |
   | Recipient | Recipient information |
   | Shared By | User who performed the sharing |

### 4.5 Gate Pass Management

Gate Pass Management (`/gate-pass`) is the function for approving or rejecting vehicle exit (departure) from the steelworks.

#### 4.5.1 Gate Pass Approval

1. Click **Gate Pass Management** in the left menu.
2. The list of pending gate pass requests is displayed.
3. Click the gate pass to approve to navigate to the detail screen.
4. Verify the following information:

   | Verification Item | Description |
   |-------------------|-------------|
   | Dispatch Information | Dispatch number, item type, company |
   | Vehicle Information | Vehicle number, driver |
   | Weighing Information | Weighing completion status, net weight |
   | Electronic Weighing Slip | Slip issuance status |

5. Once verification is complete, click the **Approve** button.
6. A confirmation message "Gate pass has been approved." is displayed.

#### 4.5.2 Gate Pass Rejection

1. Click the **Reject** button on the gate pass detail screen.
2. Enter the **rejection reason** (required).
3. Click the **Confirm** button.
4. A rejection notification is sent to the relevant driver.

> **Caution**: Gate pass approval is only possible for records where weighing has been completed normally and an electronic weighing slip has been issued. Gate passes for incomplete weighings must be rejected.

### 4.6 Master Data Management

Master Data Management is the function for managing foundational data required for system operations. Click **Master Data** in the left menu to display the sub-menus.

#### 4.6.1 Company Management

In Company Management (`/master/company`), you register and manage business partner information.

**Company Registration**

1. Click **Master Data > Company** in the left menu.
2. Click the **New Registration** button.
3. Enter the following information:

   | Input Field | Required | Description |
   |-------------|----------|-------------|
   | Company Code | Required | Unique company identification code |
   | Company Name | Required | Company name |
   | Business Registration Number | Required | 10-digit business registration number |
   | Representative Name | Optional | Name of the company representative |
   | Phone Number | Optional | Company contact number |
   | Address | Optional | Company address |
   | Active Status | Required | Select Active/Inactive |

4. Click the **Save** button.

**Company Search/Edit/Delete**

- **Search**: Search by company name or company code in the company list.
- **Edit**: Click a company to enter the detail screen, then click the **Edit** button to modify information.
- **Delete**: Click the **Delete** button on the detail screen. If the company has linked dispatch/weighing records, it cannot be deleted; instead, set it to **Inactive**.

#### 4.6.2 Vehicle Management

In Vehicle Management (`/master/vehicle`), you register and manage information about vehicles subject to weighing.

**Vehicle Registration**

1. Click **Master Data > Vehicle** in the left menu.
2. Click the **New Registration** button.
3. Enter the following information:

   | Input Field | Required | Description |
   |-------------|----------|-------------|
   | Vehicle Number | Required | Vehicle license plate number (e.g., 12GA3456) |
   | Vehicle Type | Required | Dump truck, Cargo truck, Tank lorry, etc. |
   | Affiliated Company | Required | Select a company registered in master data |
   | Driver | Optional | Default assigned driver |
   | Tare Weight | Optional | Base weight of the empty vehicle (kg) |
   | Active Status | Required | Select Active/Inactive |

4. Click the **Save** button.

> **Note**: The vehicle number is critical information used for dispatch matching during LPR automatic recognition. Enter it accurately.

**Vehicle Search/Edit/Delete**

- **Search**: Search by vehicle number, company name, or vehicle type in the vehicle list.
- **Edit**: Click a vehicle to enter the detail screen, then click the **Edit** button.
- **Delete**: Click the **Delete** button on the detail screen. If linked data exists, deletion is not possible; instead, set it to **Inactive**.

#### 4.6.3 Scale Management

In Scale Management (`/master/scale`), you register and manage information about on-site weighing equipment.

**Scale Registration**

1. Click **Master Data > Scale** in the left menu.
2. Click the **New Registration** button.
3. Enter the following information:

   | Input Field | Required | Description |
   |-------------|----------|-------------|
   | Scale Code | Required | Unique scale identification code |
   | Scale Name | Required | Scale name (e.g., Scale #1, Dock Scale) |
   | Installation Location | Required | Physical installation location |
   | Maximum Capacity | Required | Maximum weighable weight (kg) |
   | Minimum Graduation | Required | Minimum measurement unit (kg) |
   | Communication Port | Required | RS-232C connection port information |
   | Calibration Expiry Date | Required | Scale calibration certificate expiry date |
   | Active Status | Required | Select Active/Inactive |

4. Click the **Save** button.

> **Caution**: If the scale's calibration certificate has expired, weighings performed on that scale may have no legal validity. Be sure to manage the calibration expiry date.

#### 4.6.4 Common Code Management

In Common Code Management (`/master/code`), you manage classification codes used throughout the system.

1. Click **Master Data > Common Code** in the left menu.
2. The code group list is displayed.
3. Select a code group to display the detail code list for that group.

Main common code groups:

| Code Group | Description | Examples |
|------------|-------------|----------|
| ITEM_TYPE | Item Type | Byproduct, Waste, Sub-material, Export, General |
| WEIGH_MODE | Weighing Mode | LPR (automatic), Mobile (OTP), Manual |
| DISPATCH_STATUS | Dispatch Status | Registered, In Progress, Completed, Cancelled |
| VEHICLE_TYPE | Vehicle Type | Dump truck, Cargo truck, Tank lorry, etc. |
| GATE_STATUS | Gate Pass Status | Pending, Approved, Rejected |

> **Caution**: Changes to common codes affect the entire system. Before making changes, be sure to verify the scope of impact and consult with the administrator if necessary.

### 4.7 Statistics

The Statistics (`/statistics`) function analyzes weighing data by various criteria and presents them in report format.

#### 4.7.1 Daily Statistics

1. Click **Statistics** in the left menu.
2. Select the **Daily** tab at the top.
3. Set the query period (start date to end date).
4. Click the **Search** button.
5. Daily weighing count, total weight, and item type ratios are displayed in tables and charts.

#### 4.7.2 Monthly Statistics

1. Select the **Monthly** tab at the top.
2. Set the query year and month range.
3. Click the **Search** button.
4. Monthly trend charts are displayed along with detailed data.

#### 4.7.3 Statistics by Item Type

1. Select the **By Item Type** tab at the top.
2. Set the query period and item types (multiple selection available).
3. Click the **Search** button.
4. Weighing count, weight totals, and ratios by item type are displayed.

#### 4.7.4 Statistics by Company

1. Select the **By Company** tab at the top.
2. Set the query period and companies (multiple selection available).
3. Click the **Search** button.
4. Weighing results by company are displayed.

#### 4.7.5 Excel Download

Query results can be downloaded as an Excel file from all statistics screens.

1. Query the desired statistics.
2. Click the **Excel Download** button in the upper right of the screen.
3. Data matching the current query criteria is downloaded as an `.xlsx` file.
4. The filename follows the format `WeighingStatistics_[Type]_[QueryPeriod].xlsx`.

> **Note**: Downloading large amounts of data may take time. Check the browser's download progress.

### 4.8 Weighing Station Real-time Status

The Weighing Station Real-time Status (`/weighing-station`) screen provides real-time monitoring of weighing operations currently in progress at the weighbridge.

#### Viewing Weighing Station Status

1. Click **Weighing Station Status** under **Weighing Management** in the left menu.
2. The current status of each weighbridge is displayed in card format.

| Display Item | Description |
|-------------|-------------|
| **Weighbridge Name** | Weighbridge name and location |
| **Current Status** | Standby, Weighing In Progress, Error, etc. |
| **Vehicle Information** | Vehicle number and company name of the vehicle currently being weighed |
| **Weight Value** | Real-time weight display (WebSocket-based) |
| **Weighing Mode** | LPR (automatic), OTP (mobile), Manual |
| **Progress Step** | 1st/2nd/3rd weighing distinction |

3. When weighing is completed, the status is automatically updated.

> **Note**: This screen is updated in real-time via WebSocket. The latest status is reflected without any manual refresh.

### 4.9 Monitoring

The Monitoring (`/monitoring`) function allows you to check the real-time status of on-site equipment.

#### Checking Equipment Status

1. Click **Monitoring** in the left menu.
2. The list of on-site equipment and their statuses is displayed.

| Status | Display | Description |
|--------|---------|-------------|
| **Online** | Green indicator | Equipment is operating normally |
| **Offline** | Gray indicator | Communication with the equipment is disconnected |
| **Error** | Red indicator | An issue has occurred with the equipment |

Monitored equipment:

| Equipment Type | Description |
|---------------|-------------|
| **Weighing Indicator** | Equipment that reads weight data via RS-232C communication |
| **LPR Camera** | Camera that photographs vehicle license plates |
| **LiDAR Sensor** | Sensor that detects vehicle entry |
| **LED Display Board** | Electronic display device that shows guide messages to drivers |
| **Barrier** | Weighbridge entrance/exit barrier |

3. Click a specific piece of equipment to view detailed status information.

   | Detail Information | Description |
   |-------------------|-------------|
   | Equipment Name | Equipment name and code |
   | Installation Location | Physical location |
   | Current Status | Online/Offline/Error |
   | Last Communication | Time of last successful communication |
   | Error Details | Error description when in error status |

> **Caution**: If equipment status is **Offline** or **Error**, automated weighing at that weighbridge may not function properly. Contact the administrator or equipment technician immediately.

> **Note**: The monitoring screen is updated in real-time via WebSocket. The latest status is reflected without any manual refresh.

---

## 5. Common Functions

These are common functions available to both drivers and managers.

### 5.1 My Page

On My Page (`/mypage`), you can view personal information and change your password.

#### 5.1.1 Viewing Personal Information

1. Web: Click the **User icon** in the upper right, then select **My Page**
   Mobile: Select the **My** tab from the bottom navigation
2. Your personal information is displayed.

   | Item | Description |
   |------|-------------|
   | User ID | Login ID (cannot be changed) |
   | Name | User name |
   | Role | Administrator, Manager, Driver |
   | Affiliation | Affiliated company or department |
   | Contact | Registered phone number |
   | Email | Registered email address |

#### 5.1.2 Notification Settings

You can configure notification preferences on My Page.

1. Check the **Notification Settings** section on the My Page screen.
2. You can configure reception for each notification type:

   | Notification Type | Description |
   |-------------------|-------------|
   | Dispatch Notification | Receive notifications when a new dispatch is assigned |
   | Weighing Complete | Receive notifications when weighing is completed |
   | Slip Issuance | Receive notifications when an electronic weighing slip is generated |
   | Announcement | Receive notifications when a new announcement is posted |
   | System Notification | Receive notifications for account-related and system maintenance notices |

3. Toggle the switch on or off for the desired notification types.
4. Changes are saved immediately.

> **Note**: To receive push notifications on the mobile app, notification permissions for the app must be enabled in your smartphone settings. The push notification token is automatically registered when the app is first launched.

#### 5.1.3 Changing Password

1. Click (tap) the **Change Password** button on the My Page screen.
2. Enter the following information:

   | Input Field | Description |
   |-------------|-------------|
   | Current Password | Enter your current password |
   | New Password | Enter your desired new password |
   | Confirm New Password | Re-enter the new password |

3. Click (tap) the **Change** button.
4. When the message "Password has been changed." appears, the change is complete.

Password rules:

| Rule | Description |
|------|-------------|
| Minimum Length | 8 characters or more |
| Character Combination | Must include at least 3 of the following: uppercase letters, lowercase letters, numbers, special characters |
| Previous Passwords | Cannot reuse any of the last 3 passwords |
| Change Cycle | Recommended to change every 90 days (notification sent 7 days before expiration) |

> **Caution**: After changing your password, your current session will be terminated. Please log in again with your new password.

### 5.2 Notifications

The notification function allows you to check important notices sent by the system.

#### How to Check Notifications

**Web**:
1. Click the **Notification icon** (bell shape) in the top header.
2. The number of unread notifications is displayed as a badge on the icon.
3. The notification list is displayed as a dropdown.
4. Click a notification to navigate to the corresponding screen.

**Mobile**:
1. Tap the **Notification icon** on the top bar.
2. You are navigated to the notification list screen.
3. Tap a notification to navigate to the corresponding detail screen.

> **Note**: For the mobile app, you can receive important notifications via push notifications even when the app is in the background. Make sure to allow app notifications in your smartphone settings.

### 5.3 Announcements

In Announcements (`/notice`), you can check system-related notices, maintenance schedules, and operational changes.

1. Web: Click **Announcements** in the left menu
   Mobile: Access via the **Announcements** banner or menu on the main screen
2. The announcement list is displayed in chronological order. **Pinned (📌) announcements** are always shown at the top of the list.
3. Use **category filters** to view only specific types of announcements.
4. Enter keywords in the **search bar** at the top to search announcements.
5. Click (tap) an announcement to view its details.
6. Attachments can be downloaded if available.

### 5.4 Help/FAQ

In Help (`/help`), you can find frequently asked questions and answers about system usage.

1. Web: Click **Help** in the left menu
   Mobile: **Help** tab in My Page
2. The FAQ list is categorized and displayed.
3. Click (tap) a question to expand the answer.
4. Enter keywords in the search bar at the top to search for related questions.

---

## 6. Frequently Asked Questions (FAQ)

### Login/Account

**Q1. I forgot my password. What should I do?**

A. Click **Forgot Password** on the login screen to receive a temporary password via your registered email or mobile phone. After logging in with the temporary password, be sure to change your password on My Page. If you are unable to resolve the issue yourself, contact the administrator to request a password reset.

**Q2. I see a message saying my account is locked.**

A. Your account is locked for 30 minutes after 5 consecutive incorrect password attempts. Please wait 30 minutes and try again with the correct password. In urgent cases, contact the administrator to request immediate unlocking.

**Q3. Automatic login is not working on the mobile app.**

A. Automatic login is valid for 7 days after the last login. If 7 days have passed, you need to log in again. Re-login is also required if you delete and reinstall the app.

### Dispatch

**Q4. My dispatch does not appear in the dispatch list.**

A. Please check the following:
- Verify that the logged-in account is the driver account assigned to the dispatch.
- Verify that the filters (status, date) on the dispatch list are set correctly.
- The dispatch may have been cancelled (CANCELLED), so try searching with the cancelled status included.
- If the problem persists, contact the weighing manager.

**Q5. The dispatch status is not changing.**

A. Dispatch status is automatically changed by the system based on weighing progress. It automatically changes to "In Progress" when the 1st weighing starts and to "Completed" when the final weighing is complete. If manual changes are needed, the manager can change the status on the dispatch detail screen.

### Weighing

**Q6. I entered the weighbridge but automatic recognition did not work.**

A. Please check the following:
1. Verify that the vehicle license plate is clean and not obstructed.
2. Verify that the vehicle is parked accurately on the weighbridge.
3. Check the guide message on the LED display board.
4. If LPR recognition fails and an OTP code is displayed on the LED display board, proceed with mobile OTP weighing.
5. If OTP weighing is also unavailable, contact the logistics control room to request manual weighing.

**Q7. I entered the OTP code but it says "Invalid code."**

A. Please check the following:
- Verify that you entered the code displayed on the LED display board correctly (6 digits).
- The OTP expires 5 minutes after issuance. Check for a new OTP.
- After 3 consecutive errors, the OTP is invalidated. Contact the logistics control room.

**Q8. The net weight does not seem correct.**

A. The net weight is automatically calculated as |1st Weight - 2nd Weight|. Please check the weight for each weighing order on the weighing detail screen. If you believe there is an issue with the weight, request re-weighing from the manager.

### Electronic Weighing Slip

**Q9. The electronic weighing slip was not generated.**

A. The electronic weighing slip is automatically generated after the final weighing (typically the 2nd weighing) is completed. The slip is not issued while weighing is still in progress. If all weighings have been completed but the slip is missing, contact the weighing manager.

**Q10. I shared the electronic weighing slip via KakaoTalk but the recipient says they cannot view it.**

A. The shared link requires an internet connection. Verify that the recipient has an internet connection when clicking the link. If the problem persists, try sharing via SMS instead.

### Gate Pass

**Q11. My gate pass was rejected. What should I do?**

A. Check the rejection reason included in the gate pass rejection notification. Depending on the reason (incomplete weighing, missing documents, etc.), complete the required actions and then re-request the gate pass or contact the weighing manager.

### Other

**Q12. I am not receiving notifications on the mobile app.**

A. Check whether notification permissions for the Busan Smart Weighing app are enabled in your smartphone settings. For Android: "Settings > Apps > Busan Smart Weighing > Notifications"; for iOS: "Settings > Notifications > Busan Smart Weighing".

---

## 7. Troubleshooting Guide

This chapter provides guidance on common issues that may occur while using the system and their solutions.

### 7.1 Login Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| "User ID or password does not match" | Incorrect ID/password entry | Check Caps Lock and re-enter. Use the Forgot Password function |
| "Account is locked" | 5 consecutive incorrect password attempts | Wait 30 minutes and retry, or contact administrator for unlock |
| Logged out immediately after login | Access token issuance error | Clear browser cookies/cache and retry |
| "Session has expired" | Inactive for 30+ minutes | Log in again (if auto-renewal within 7 days failed) |
| Mobile app login failure | Poor network connection | Check Wi-Fi or mobile data connection and retry |

### 7.2 Weighing Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| LPR recognition failure | Dirty/damaged/obstructed license plate | Switch to mobile OTP weighing. Clean license plate and retry |
| OTP entry failure | Code expired or entered incorrectly | Check for new OTP within 5 minutes and re-enter. Contact logistics control room after 3 failures |
| Weight displays as 0 | Weighing indicator communication error | Request equipment inspection from logistics control room. Switch to manual weighing |
| "Dispatch not found" | Unregistered vehicle/no dispatch | Verify dispatch registration with manager. Verify vehicle number registration |
| Weighing data not saved | Server communication error | Check network connection. Retry. Contact logistics control room if issue persists |
| "Another dispatch is in progress" | Duplicate dispatch for the same vehicle | Complete or cancel the previous dispatch and retry |

### 7.3 Electronic Weighing Slip Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| Slip not generated | Weighing not completed | Verify that all weighing orders have been completed |
| Slip content error | Input data error | Request correction from the manager |
| KakaoTalk sharing failure | KakaoTalk not installed/insufficient permissions | Verify KakaoTalk app installation. Use SMS sharing as alternative |
| Shared link inaccessible | Network error | Check internet connection. Retry after some time |

### 7.4 Mobile App Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| App does not launch | OS version not met | Verify Android 10+ or iOS 14+. Update the app |
| App freezes during use | Insufficient memory | Force close and relaunch the app. Restart the device |
| Data loading failure | Unstable network | Check Wi-Fi or data connection. Retry in a stable environment |
| Notifications not received | Notification permission not granted | Enable app notification permission in device settings |
| Display issues | Outdated app version | Update to the latest version from the app store |

### 7.5 Web Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| Screen layout broken | Unsupported browser | Use Chrome 90 or later (recommended) |
| Real-time data not updating | WebSocket connection lost | Refresh the browser (F5). Contact administrator if issue persists |
| Excel download failure | Pop-up blocked | Check that the browser's pop-up blocker is disabled |
| Slow page loading | Large data query | Narrow the search criteria (date range) and retry |
| "You do not have permission" | Insufficient role permissions | Contact administrator to verify access permissions for the function |

### 7.6 Equipment Monitoring Issues

| Symptom | Cause | Solution |
|---------|-------|----------|
| Equipment status "Offline" | Communication failure | Request inspection from administrator or equipment technician |
| Equipment status "Error" | Equipment malfunction | Check error details and request repair from equipment technician |
| LiDAR not detecting | Sensor malfunction | Contact logistics control room. Switch to manual weighing |
| LED display board showing abnormally | Communication error | Contact logistics control room. Proceed with verbal instructions |
| Barrier not operating | Mechanical failure | Contact on-site safety personnel immediately |

### 7.7 Emergency Contacts

| Department | Contact | Responsibilities |
|------------|---------|-----------------|
| **Logistics Control Room** | Check internal extension | Weighing operations, OTP errors, equipment issues |
| **Material Warehouse** | Check internal extension | Item-related, loading/unloading-related |
| **System Administrator** | Check internal extension | Account management, system errors |
| **Equipment Technician** | Check internal extension | Equipment repair, hardware inspection |

> **Note**: Specific contact numbers can be found on the on-site bulletin board or in the announcements section.

---

## 8. Glossary

This section lists the key terms used in this system in alphabetical order.

| Term | Description |
|------|-------------|
| **Access Token** | A temporary key used for authentication when making requests to the server after login. Valid for 30 minutes |
| **Barrier** | Weighbridge entrance/exit barrier gate |
| **Byproduct** | Materials produced as a byproduct of the steelmaking process (slag, dust, etc.) |
| **Dashboard** | A comprehensive screen that presents key status information at a glance |
| **Dispatch** | Assigning a transport order to a vehicle |
| **Electronic Weighing Slip (e-Slip)** | An electronic document of weighing results, replacing paper weighing slips |
| **Export** | The transfer of materials from within the steelworks to the outside |
| **Gate Pass** | A permit document required when a vehicle exits the steelworks. Requires manager approval |
| **Gross Weight** | The total weight of the vehicle with cargo loaded |
| **JWT (JSON Web Token)** | A token standard used for user authentication. Consists of an access token (30 min) and refresh token (7 days) |
| **LED Display Board** | An electronic display device that shows guide messages to drivers at the weighbridge site |
| **LiDAR (Light Detection and Ranging)** | A sensor that measures distance to objects using lasers. Used for vehicle entry detection |
| **Load Cell** | A sensor that converts weight into electrical signals. Built into the weighbridge |
| **LPR (License Plate Recognition)** | Automatic vehicle license plate recognition technology. AI analyzes images captured by cameras to read license plates |
| **Master Data** | Foundational data for system operations, including companies, vehicles, scales, and common codes |
| **Measurement Order** | The weighing sequence: 1st (first measurement), 2nd (second measurement), 3rd (third measurement) |
| **Net Weight** | The actual weight of the cargo. Calculated as |1st Weight - 2nd Weight| |
| **OTP (One-Time Password)** | A one-time password consisting of 6 digits, valid for 5 minutes |
| **RS-232C** | A serial communication standard between the weighing indicator and the server. Used for weight data transmission |
| **Sub-material** | Auxiliary raw materials used in the steelmaking process |
| **Tare Weight** | The weight of an empty vehicle without cargo |
| **Waste** | Materials to be disposed of, generated during the steelmaking process |
| **WebSocket** | A protocol supporting bidirectional real-time communication between server and client |
| **Weighing Indicator** | Equipment that displays and transmits weight data measured by the weighbridge (load cell) |
| **Weighing Mode** | The method of performing weighing: LPR (automatic), Mobile (OTP), and Manual -- three modes |

---

**Document History**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2026-01-29 | System Management Team | Initial version |
| 1.1 | 2026-01-29 | System Management Team | Added favorites function, reflected phone call record method for inquiries, added weighing history inquiry, added My Page notification settings, added company statistics to dashboard, added weighing station real-time status page, reflected announcement category/search/pinning features, added dark/light theme support guide |
| 1.2 | 2026-01-29 | System Management Team | Reflected new features: onboarding guide, keyboard shortcuts, weighing station control screen details, equipment monitoring, weighing inquiry page, statistics/reports page, help page, mobile offline cache |

---

## Appendix B: New Feature Guide (v1.2)

### B.1 Onboarding Guide

An **onboarding guide** is automatically displayed when you first access the system. It provides step-by-step guidance on key menus and functions.

| Item | Description |
|------|-------------|
| **Display Timing** | On first login or re-launched from Help |
| **Content** | Sidebar menu, tab navigation, search, and favorites usage |
| **Skip** | Can be exited at any time with the "Skip" button |

### B.2 Keyboard Shortcuts

The web system supports keyboard shortcuts.

| Shortcut | Function |
|----------|----------|
| `Ctrl + N` | New Registration |
| `Ctrl + F` | Focus Search |
| `Escape` | Close Modal/Popup |

### B.3 Weighing Inquiry Page

The **Weighing Inquiry** menu allows you to search past weighing records with detailed conditions.

| Search Criteria | Description |
|-----------------|-------------|
| Period | Start date to end date |
| Vehicle Number | Partial match search |
| Item Type | Byproduct/Waste/Sub-material/Export/General |
| Weighing Method | LPR automatic/Mobile OTP/Manual |
| Weighing Status | In Progress/Completed/Cancelled |

> **Excel Download**: Search results can be downloaded as an Excel file.

### B.4 Statistics/Reports Page

The **Statistics** menu allows you to view various charts and reports.

| Statistics Type | Description |
|-----------------|-------------|
| Daily Weighing Trend | Line chart displaying daily weighing count/weight trends |
| Item Type Ratio | Pie chart displaying weighing ratio by item type |
| Company Performance | Bar chart of weighing results by carrier |
| Weighing Method Statistics | Ratio of LPR automatic/OTP/Manual |

### B.5 Equipment Monitoring Page

The **Equipment Monitoring** menu allows you to monitor the real-time status of equipment connected to the weighbridge.

| Equipment | Displayed Information |
|-----------|----------------------|
| Indicator | Connection status, current weight value |
| LPR Camera | Connection status, last capture time |
| Display Board | Connection status, current displayed message |
| Barrier | Connection status, open/close state |

### B.6 Mobile Offline Support

The mobile app allows you to view previously queried dispatch information and weighing history even when the network is unstable.

| Item | Description |
|------|-------------|
| **Cached Data** | Dispatch list, weighing history, electronic weighing slips |
| **Cache Validity** | Up to 1 hour (automatic refresh attempted after 1 hour) |
| **Limitations** | New dispatch registration/weighing cannot be performed offline |

---

*For inquiries about this manual, please contact the system administrator.*
