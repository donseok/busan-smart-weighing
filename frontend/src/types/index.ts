export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  error?: { code: string; message: string };
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// Dispatch
export interface Dispatch {
  dispatchId: number;
  vehicleId: number;
  companyId: number;
  itemType: string;
  itemName: string;
  dispatchDate: string;
  originLocation?: string;
  destination?: string;
  remarks?: string;
  dispatchStatus: string;
  createdBy?: number;
  createdAt: string;
  updatedAt: string;
}

// Weighing
export interface WeighingRecord {
  weighingId: number;
  dispatchId: number;
  scaleId: number;
  weighingMode: string;
  weighingStep: string;
  grossWeight?: number;
  tareWeight?: number;
  netWeight?: number;
  lprPlateNumber?: string;
  aiConfidence?: number;
  weighingStatus: string;
  reWeighReason?: string;
  createdAt: string;
  updatedAt: string;
}

// Company
export interface Company {
  companyId: number;
  companyName: string;
  companyType: string;
  businessNumber?: string;
  representative?: string;
  phoneNumber?: string;
  address?: string;
  isActive: boolean;
  createdAt: string;
}

// Vehicle
export interface Vehicle {
  vehicleId: number;
  plateNumber: string;
  vehicleType: string;
  companyId?: number;
  defaultTareWeight?: number;
  maxLoadWeight?: number;
  driverName?: string;
  driverPhone?: string;
  isActive: boolean;
  createdAt: string;
}

// Scale
export interface Scale {
  scaleId: number;
  scaleName: string;
  location?: string;
  maxCapacity?: number;
  minCapacity?: number;
  scaleStatus: string;
  isActive: boolean;
  createdAt: string;
}

// Weighing Slip
export interface WeighingSlip {
  slipId: number;
  weighingId: number;
  dispatchId: number;
  slipNumber: string;
  vehiclePlateNumber?: string;
  companyName?: string;
  itemName?: string;
  grossWeightKg?: string;
  tareWeightKg?: string;
  netWeightKg?: string;
  sharedVia?: string;
  createdAt: string;
}

// GatePass
export interface GatePass {
  gatePassId: number;
  weighingId: number;
  dispatchId: number;
  passStatus: string;
  passedAt?: string;
  processedBy?: number;
  rejectReason?: string;
  createdAt: string;
}

// Weighing Statistics
export interface DailyStatistic {
  date: string;
  totalCount: number;
  totalNetWeightTon: number;
}

export interface WeighingStatistics {
  todayTotalCount: number;
  todayCompletedCount: number;
  todayInProgressCount: number;
  todayTotalNetWeightTon: number;
  monthTotalCount: number;
  monthTotalNetWeightTon: number;
  countByItemType: Record<string, number>;
  countByWeighingMode: Record<string, number>;
  dailyStatistics: DailyStatistic[];
}

// Login
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
}

// Notification
export interface NotificationItem {
  notificationId: number;
  notificationType: string;
  title: string;
  message: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

// User
export interface User {
  userId: number;
  loginId: string;
  userName: string;
  phoneNumber: string;
  userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';
  companyId?: number;
  companyName?: string;
  isActive: boolean;
  failedLoginCount: number;
  lockedUntil?: string;
  createdAt: string;
  updatedAt: string;
}

// Audit Log
export interface AuditLog {
  auditLogId: number;
  actorId?: number;
  actorName?: string;
  actionType: string;
  actionTypeDesc: string;
  entityType: string;
  entityTypeDesc: string;
  entityId?: number;
  oldValue?: string;
  newValue?: string;
  ipAddress?: string;
  userAgent?: string;
  createdAt: string;
}

// System Setting
export interface SystemSetting {
  settingId: number;
  settingKey: string;
  settingValue: string;
  settingType: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON';
  category: 'GENERAL' | 'WEIGHING' | 'NOTIFICATION' | 'SECURITY';
  categoryDesc: string;
  description?: string;
  isEditable: boolean;
  updatedAt: string;
}

// Common Code
export interface CommonCode {
  codeId: number;
  codeGroup: string;
  codeValue: string;
  codeName: string;
  sortOrder?: number;
  isActive: boolean;
}

// Dashboard Summary
export interface DashboardSummary {
  dispatchRegistered: number;
  dispatchInProgress: number;
  dispatchCompleted: number;
  dispatchCancelled: number;
  gatePassPending: number;
  gatePassPassed: number;
  gatePassRejected: number;
  weighingInProgress: number;
  weighingCompleted: number;
  weighingReWeighing: number;
}

// Company Statistics
export interface CompanyStatistics {
  companyId: number;
  companyName: string;
  weighingCount: number;
  totalNetWeightTon: number;
}

// Notice
export interface Notice {
  noticeId: number;
  title: string;
  content: string;
  category: 'GENERAL' | 'SYSTEM' | 'UPDATE' | 'MAINTENANCE';
  categoryDesc: string;
  authorId: number;
  authorName: string;
  isPublished: boolean;
  isPinned: boolean;
  viewCount: number;
  publishedAt?: string;
  createdAt: string;
  updatedAt: string;
}

// Favorite
export interface Favorite {
  favorite_id: number;
  favorite_type: 'MENU' | 'DISPATCH' | 'VEHICLE' | 'COMPANY' | 'SCALE';
  favorite_type_desc: string;
  target_id?: number;
  target_path?: string;
  display_name: string;
  icon?: string;
  sort_order?: number;
  created_at: string;
}

// Device Monitoring
export interface DeviceStatus {
  deviceId: number;
  deviceCode: string;
  deviceName: string;
  deviceType: 'SCALE' | 'LPR_CAMERA' | 'INDICATOR' | 'BARRIER_GATE';
  deviceTypeDesc: string;
  location: string;
  connectionStatus: 'ONLINE' | 'OFFLINE' | 'ERROR';
  connectionStatusDesc: string;
  lastConnectedAt?: string;
  ipAddress?: string;
  errorMessage?: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface DeviceSummary {
  totalDevices: number;
  onlineCount: number;
  offlineCount: number;
  errorCount: number;
  countByTypeAndStatus: Record<string, Record<string, number>>;
}

// FAQ
export interface Faq {
  faqId: number;
  question: string;
  answer: string;
  category: 'WEIGHING' | 'DISPATCH' | 'ACCOUNT' | 'SYSTEM' | 'OTHER';
  categoryDesc: string;
  sortOrder: number;
  isPublished: boolean;
  viewCount: number;
  createdAt: string;
  updatedAt: string;
}

// MyPage
export interface MyProfile {
  userId: number;
  loginId: string;
  userName: string;
  phoneNumber: string;
  email?: string;
  userRole: 'ADMIN' | 'MANAGER' | 'DRIVER';
  userRoleDesc: string;
  companyId?: number;
  companyName?: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  createdAt: string;
  lastLoginAt?: string;
}
