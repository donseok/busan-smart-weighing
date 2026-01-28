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
