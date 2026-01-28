package com.dongkuk.weighing.global.config;

import com.dongkuk.weighing.dispatch.domain.Dispatch;
import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.dispatch.domain.ItemType;
import com.dongkuk.weighing.gatepass.domain.GatePass;
import com.dongkuk.weighing.gatepass.domain.GatePassRepository;
import com.dongkuk.weighing.inquiry.domain.InquiryCall;
import com.dongkuk.weighing.inquiry.domain.InquiryCallRepository;
import com.dongkuk.weighing.inquiry.domain.InquiryType;
import com.dongkuk.weighing.lpr.domain.LprCapture;
import com.dongkuk.weighing.lpr.domain.LprCaptureRepository;
import com.dongkuk.weighing.master.domain.CommonCode;
import com.dongkuk.weighing.master.domain.CommonCodeRepository;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.master.domain.Scale;
import com.dongkuk.weighing.master.domain.ScaleRepository;
import com.dongkuk.weighing.master.domain.Vehicle;
import com.dongkuk.weighing.master.domain.VehicleRepository;
import com.dongkuk.weighing.notification.domain.Notification;
import com.dongkuk.weighing.notification.domain.NotificationRepository;
import com.dongkuk.weighing.notification.domain.NotificationType;
import com.dongkuk.weighing.slip.domain.WeighingSlip;
import com.dongkuk.weighing.slip.domain.WeighingSlipRepository;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import com.dongkuk.weighing.weighing.domain.WeighingMode;
import com.dongkuk.weighing.weighing.domain.WeighingRecord;
import com.dongkuk.weighing.weighing.domain.WeighingRepository;
import com.dongkuk.weighing.weighing.domain.WeighingStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadDevData(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            VehicleRepository vehicleRepository,
            ScaleRepository scaleRepository,
            CommonCodeRepository commonCodeRepository,
            DispatchRepository dispatchRepository,
            WeighingRepository weighingRepository,
            GatePassRepository gatePassRepository,
            WeighingSlipRepository weighingSlipRepository,
            LprCaptureRepository lprCaptureRepository,
            InquiryCallRepository inquiryCallRepository,
            NotificationRepository notificationRepository
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                log.info("=== DEV 데이터 이미 존재 - 스킵 ===");
                return;
            }

            LocalDate today = LocalDate.now();

            // ========================================
            // 1. 기준정보: Company (4개)
            // ========================================
            Company company1 = companyRepository.save(Company.builder()
                    .companyName("동국운송")
                    .companyType("운송")
                    .businessNumber("123-45-67890")
                    .representative("김동국")
                    .phoneNumber("051-123-4567")
                    .address("부산광역시 남구 감만동 123")
                    .build());

            Company company2 = companyRepository.save(Company.builder()
                    .companyName("부산물류")
                    .companyType("물류")
                    .businessNumber("234-56-78901")
                    .representative("이부산")
                    .phoneNumber("051-234-5678")
                    .address("부산광역시 사하구 다대동 456")
                    .build());

            Company company3 = companyRepository.save(Company.builder()
                    .companyName("한진운수")
                    .companyType("운송")
                    .businessNumber("345-67-89012")
                    .representative("박한진")
                    .phoneNumber("051-345-6789")
                    .address("부산광역시 강서구 녹산동 789")
                    .build());

            Company company4 = companyRepository.save(Company.builder()
                    .companyName("삼성물산")
                    .companyType("건설")
                    .businessNumber("456-78-90123")
                    .representative("최삼성")
                    .phoneNumber("02-456-7890")
                    .address("서울특별시 강남구 삼성동 100")
                    .build());

            log.info("회사 4개 생성 완료");

            // ========================================
            // 2. 기준정보: Vehicle (8대)
            // ========================================
            Vehicle v1 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("부산12가3456")
                    .vehicleType("카고")
                    .companyId(company1.getCompanyId())
                    .defaultTareWeight(new BigDecimal("8500.00"))
                    .maxLoadWeight(new BigDecimal("25000.00"))
                    .driverName("운전자")
                    .driverPhone("010-2222-2222")
                    .build());

            Vehicle v2 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("부산34나5678")
                    .vehicleType("덤프")
                    .companyId(company1.getCompanyId())
                    .defaultTareWeight(new BigDecimal("12000.00"))
                    .maxLoadWeight(new BigDecimal("35000.00"))
                    .driverName("김운전")
                    .driverPhone("010-3333-3333")
                    .build());

            Vehicle v3 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("경남56다7890")
                    .vehicleType("탱크로리")
                    .companyId(company2.getCompanyId())
                    .defaultTareWeight(new BigDecimal("10000.00"))
                    .maxLoadWeight(new BigDecimal("30000.00"))
                    .driverName("박기사")
                    .driverPhone("010-4444-4444")
                    .build());

            Vehicle v4 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("부산78라1234")
                    .vehicleType("카고")
                    .companyId(company2.getCompanyId())
                    .defaultTareWeight(new BigDecimal("7500.00"))
                    .maxLoadWeight(new BigDecimal("20000.00"))
                    .driverName("이기사")
                    .driverPhone("010-5555-5555")
                    .build());

            Vehicle v5 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("경남90마5678")
                    .vehicleType("윙바디")
                    .companyId(company3.getCompanyId())
                    .defaultTareWeight(new BigDecimal("9000.00"))
                    .maxLoadWeight(new BigDecimal("28000.00"))
                    .driverName("최운전")
                    .driverPhone("010-6666-6666")
                    .build());

            Vehicle v6 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("부산11바9012")
                    .vehicleType("덤프")
                    .companyId(company3.getCompanyId())
                    .defaultTareWeight(new BigDecimal("13000.00"))
                    .maxLoadWeight(new BigDecimal("38000.00"))
                    .driverName("정기사")
                    .driverPhone("010-7777-7777")
                    .build());

            Vehicle v7 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("서울22사3456")
                    .vehicleType("카고")
                    .companyId(company4.getCompanyId())
                    .defaultTareWeight(new BigDecimal("8000.00"))
                    .maxLoadWeight(new BigDecimal("22000.00"))
                    .driverName("강기사")
                    .driverPhone("010-8888-8888")
                    .build());

            Vehicle v8 = vehicleRepository.save(Vehicle.builder()
                    .plateNumber("서울44아7890")
                    .vehicleType("트레일러")
                    .companyId(company4.getCompanyId())
                    .defaultTareWeight(new BigDecimal("15000.00"))
                    .maxLoadWeight(new BigDecimal("40000.00"))
                    .driverName("임기사")
                    .driverPhone("010-9999-9999")
                    .build());

            log.info("차량 8대 생성 완료");

            // ========================================
            // 3. 기준정보: Scale (3대)
            // ========================================
            Scale scale1 = scaleRepository.save(Scale.builder()
                    .scaleName("1번 계량대 (입구)")
                    .location("정문 입구")
                    .maxCapacity(new BigDecimal("80000.00"))
                    .minCapacity(new BigDecimal("500.00"))
                    .build());

            Scale scale2 = scaleRepository.save(Scale.builder()
                    .scaleName("2번 계량대 (출구)")
                    .location("후문 출구")
                    .maxCapacity(new BigDecimal("80000.00"))
                    .minCapacity(new BigDecimal("500.00"))
                    .build());

            Scale scale3 = scaleRepository.save(Scale.builder()
                    .scaleName("3번 계량대 (부산물)")
                    .location("부산물 처리장")
                    .maxCapacity(new BigDecimal("60000.00"))
                    .minCapacity(new BigDecimal("200.00"))
                    .build());

            log.info("계량대 3대 생성 완료");

            // ========================================
            // 4. 기준정보: CommonCode
            // ========================================
            List<CommonCode> codes = List.of(
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("BY_PRODUCT").codeName("부산물").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("WASTE").codeName("폐기물").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("SUB_MATERIAL").codeName("부재료").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("EXPORT").codeName("반출").sortOrder(4).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("GENERAL").codeName("일반").sortOrder(5).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("CARGO").codeName("카고").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("DUMP").codeName("덤프").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("TANK").codeName("탱크로리").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("WING").codeName("윙바디").sortOrder(4).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("TRAILER").codeName("트레일러").sortOrder(5).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("LPR_AUTO").codeName("LPR 자동").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("MOBILE_OTP").codeName("모바일 OTP").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("MANUAL").codeName("수동").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("RE_WEIGH").codeName("재계량").sortOrder(4).build(),
                    CommonCode.builder().codeGroup("COMPANY_TYPE").codeValue("TRANSPORT").codeName("운송").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("COMPANY_TYPE").codeValue("LOGISTICS").codeName("물류").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("COMPANY_TYPE").codeValue("CONSTRUCTION").codeName("건설").sortOrder(3).build()
            );
            commonCodeRepository.saveAll(codes);
            log.info("공통코드 {}건 생성 완료", codes.size());

            // ========================================
            // 5. 사용자: 기존 3명 + 추가 3명
            // ========================================
            User admin = userRepository.save(User.builder()
                    .userName("관리자")
                    .phoneNumber("010-0000-0000")
                    .userRole(UserRole.ADMIN)
                    .loginId("admin")
                    .passwordHash(passwordEncoder.encode("admin1234"))
                    .build());

            User manager = userRepository.save(User.builder()
                    .userName("담당자")
                    .phoneNumber("010-1111-1111")
                    .userRole(UserRole.MANAGER)
                    .loginId("manager")
                    .passwordHash(passwordEncoder.encode("manager1234"))
                    .companyId(company1.getCompanyId())
                    .build());

            User driver1 = userRepository.save(User.builder()
                    .userName("운전자")
                    .phoneNumber("010-2222-2222")
                    .userRole(UserRole.DRIVER)
                    .loginId("driver")
                    .passwordHash(passwordEncoder.encode("driver1234"))
                    .companyId(company1.getCompanyId())
                    .build());

            User driver2 = userRepository.save(User.builder()
                    .userName("박기사")
                    .phoneNumber("010-4444-4444")
                    .userRole(UserRole.DRIVER)
                    .loginId("driver2")
                    .passwordHash(passwordEncoder.encode("driver1234"))
                    .companyId(company2.getCompanyId())
                    .build());

            User driver3 = userRepository.save(User.builder()
                    .userName("최운전")
                    .phoneNumber("010-6666-6666")
                    .userRole(UserRole.DRIVER)
                    .loginId("driver3")
                    .passwordHash(passwordEncoder.encode("driver1234"))
                    .companyId(company3.getCompanyId())
                    .build());

            User operator = userRepository.save(User.builder()
                    .userName("운영자")
                    .phoneNumber("010-1234-5678")
                    .userRole(UserRole.MANAGER)
                    .loginId("operator")
                    .passwordHash(passwordEncoder.encode("operator1234"))
                    .build());

            log.info("사용자 6명 생성 완료");

            // ========================================
            // 6. 배차 (Dispatch) 15건
            // ========================================
            // REGISTERED (3건)
            Dispatch d1 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v1.getVehicleId()).companyId(company1.getCompanyId())
                    .itemType(ItemType.BY_PRODUCT).itemName("슬래그")
                    .dispatchDate(today).originLocation("제강공장").destination("슬래그처리장")
                    .remarks("오전 배차").createdBy(manager.getUserId()).build());

            Dispatch d2 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v3.getVehicleId()).companyId(company2.getCompanyId())
                    .itemType(ItemType.WASTE).itemName("폐유")
                    .dispatchDate(today).originLocation("압연공장").destination("폐유처리업체")
                    .remarks("위험물 운송 주의").createdBy(manager.getUserId()).build());

            Dispatch d3 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v7.getVehicleId()).companyId(company4.getCompanyId())
                    .itemType(ItemType.GENERAL).itemName("철근")
                    .dispatchDate(today.plusDays(1)).originLocation("제품창고").destination("건설현장A")
                    .remarks("내일 배차").createdBy(operator.getUserId()).build());

            // IN_PROGRESS (4건)
            Dispatch d4 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v2.getVehicleId()).companyId(company1.getCompanyId())
                    .itemType(ItemType.BY_PRODUCT).itemName("더스트")
                    .dispatchDate(today).originLocation("소결공장").destination("더스트처리장")
                    .createdBy(manager.getUserId()).build());
            d4.startProgress();
            dispatchRepository.save(d4);

            Dispatch d5 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v4.getVehicleId()).companyId(company2.getCompanyId())
                    .itemType(ItemType.SUB_MATERIAL).itemName("석회석")
                    .dispatchDate(today).originLocation("원료야드").destination("소결공장")
                    .createdBy(manager.getUserId()).build());
            d5.startProgress();
            dispatchRepository.save(d5);

            Dispatch d6 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v5.getVehicleId()).companyId(company3.getCompanyId())
                    .itemType(ItemType.EXPORT).itemName("열연코일")
                    .dispatchDate(today).originLocation("열연공장").destination("부산신항")
                    .remarks("수출용 / 선적기한 엄수").createdBy(operator.getUserId()).build());
            d6.startProgress();
            dispatchRepository.save(d6);

            Dispatch d7 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v8.getVehicleId()).companyId(company4.getCompanyId())
                    .itemType(ItemType.GENERAL).itemName("H빔")
                    .dispatchDate(today).originLocation("제품창고").destination("건설현장B")
                    .createdBy(operator.getUserId()).build());
            d7.startProgress();
            dispatchRepository.save(d7);

            // COMPLETED (6건)
            Dispatch d8 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v1.getVehicleId()).companyId(company1.getCompanyId())
                    .itemType(ItemType.BY_PRODUCT).itemName("슬래그")
                    .dispatchDate(today.minusDays(1)).originLocation("제강공장").destination("슬래그처리장")
                    .createdBy(manager.getUserId()).build());
            d8.startProgress();
            d8.complete();
            dispatchRepository.save(d8);

            Dispatch d9 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v2.getVehicleId()).companyId(company1.getCompanyId())
                    .itemType(ItemType.WASTE).itemName("스케일")
                    .dispatchDate(today.minusDays(1)).originLocation("압연공장").destination("스크랩처리업체")
                    .createdBy(manager.getUserId()).build());
            d9.startProgress();
            d9.complete();
            dispatchRepository.save(d9);

            Dispatch d10 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v3.getVehicleId()).companyId(company2.getCompanyId())
                    .itemType(ItemType.SUB_MATERIAL).itemName("코크스")
                    .dispatchDate(today.minusDays(2)).originLocation("코크스공장").destination("고로")
                    .createdBy(manager.getUserId()).build());
            d10.startProgress();
            d10.complete();
            dispatchRepository.save(d10);

            Dispatch d11 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v5.getVehicleId()).companyId(company3.getCompanyId())
                    .itemType(ItemType.EXPORT).itemName("냉연코일")
                    .dispatchDate(today.minusDays(2)).originLocation("냉연공장").destination("부산신항")
                    .createdBy(operator.getUserId()).build());
            d11.startProgress();
            d11.complete();
            dispatchRepository.save(d11);

            Dispatch d12 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v6.getVehicleId()).companyId(company3.getCompanyId())
                    .itemType(ItemType.BY_PRODUCT).itemName("고로슬래그")
                    .dispatchDate(today.minusDays(3)).originLocation("고로").destination("시멘트공장")
                    .createdBy(manager.getUserId()).build());
            d12.startProgress();
            d12.complete();
            dispatchRepository.save(d12);

            Dispatch d13 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v4.getVehicleId()).companyId(company2.getCompanyId())
                    .itemType(ItemType.GENERAL).itemName("철스크랩")
                    .dispatchDate(today.minusDays(3)).originLocation("스크랩야드").destination("전기로")
                    .createdBy(manager.getUserId()).build());
            d13.startProgress();
            d13.complete();
            dispatchRepository.save(d13);

            // CANCELLED (2건)
            Dispatch d14 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v7.getVehicleId()).companyId(company4.getCompanyId())
                    .itemType(ItemType.GENERAL).itemName("형강")
                    .dispatchDate(today.minusDays(1)).originLocation("제품창고").destination("건설현장C")
                    .remarks("고객사 요청으로 취소").createdBy(operator.getUserId()).build());
            d14.cancel();
            dispatchRepository.save(d14);

            Dispatch d15 = dispatchRepository.save(Dispatch.builder()
                    .vehicleId(v6.getVehicleId()).companyId(company3.getCompanyId())
                    .itemType(ItemType.WASTE).itemName("분진")
                    .dispatchDate(today.minusDays(2)).originLocation("집진기").destination("매립장")
                    .remarks("기상악화로 취소").createdBy(manager.getUserId()).build());
            d15.cancel();
            dispatchRepository.save(d15);

            log.info("배차 15건 생성 완료");

            // ========================================
            // 7. 계량 실적 (WeighingRecord) 12건
            // ========================================
            // IN_PROGRESS 계량 (3건) - 진행중 배차에 연결
            WeighingRecord w1 = weighingRepository.save(WeighingRecord.builder()
                    .dispatchId(d4.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.LPR_AUTO).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("25300.00"))
                    .lprPlateNumber("부산34나5678").aiConfidence(new BigDecimal("0.9650"))
                    .build());

            WeighingRecord w2 = weighingRepository.save(WeighingRecord.builder()
                    .dispatchId(d5.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.MOBILE_OTP).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("18500.00"))
                    .build());

            WeighingRecord w3 = weighingRepository.save(WeighingRecord.builder()
                    .dispatchId(d6.getDispatchId()).scaleId(scale2.getScaleId())
                    .weighingMode(WeighingMode.LPR_AUTO).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("32100.00"))
                    .lprPlateNumber("경남90마5678").aiConfidence(new BigDecimal("0.9820"))
                    .build());

            // COMPLETED 계량 (8건) - 완료된 배차에 연결
            WeighingRecord w4 = WeighingRecord.builder()
                    .dispatchId(d8.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.LPR_AUTO).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("27500.00"))
                    .lprPlateNumber("부산12가3456").aiConfidence(new BigDecimal("0.9510"))
                    .build();
            w4.recordTareWeight(new BigDecimal("8500.00"));
            w4.complete();
            w4 = weighingRepository.save(w4);

            WeighingRecord w5 = WeighingRecord.builder()
                    .dispatchId(d9.getDispatchId()).scaleId(scale2.getScaleId())
                    .weighingMode(WeighingMode.LPR_AUTO).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("23800.00"))
                    .lprPlateNumber("부산34나5678").aiConfidence(new BigDecimal("0.9380"))
                    .build();
            w5.recordTareWeight(new BigDecimal("12000.00"));
            w5.complete();
            w5 = weighingRepository.save(w5);

            WeighingRecord w6 = WeighingRecord.builder()
                    .dispatchId(d10.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.MOBILE_OTP).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("28200.00"))
                    .build();
            w6.recordTareWeight(new BigDecimal("10000.00"));
            w6.complete();
            w6 = weighingRepository.save(w6);

            WeighingRecord w7 = WeighingRecord.builder()
                    .dispatchId(d11.getDispatchId()).scaleId(scale2.getScaleId())
                    .weighingMode(WeighingMode.MANUAL).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("35600.00"))
                    .build();
            w7.recordTareWeight(new BigDecimal("9000.00"));
            w7.complete();
            w7 = weighingRepository.save(w7);

            WeighingRecord w8 = WeighingRecord.builder()
                    .dispatchId(d12.getDispatchId()).scaleId(scale3.getScaleId())
                    .weighingMode(WeighingMode.LPR_AUTO).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("38200.00"))
                    .lprPlateNumber("부산11바9012").aiConfidence(new BigDecimal("0.8920"))
                    .build();
            w8.recordTareWeight(new BigDecimal("13000.00"));
            w8.complete();
            w8 = weighingRepository.save(w8);

            WeighingRecord w9 = WeighingRecord.builder()
                    .dispatchId(d13.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.MOBILE_OTP).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("19800.00"))
                    .build();
            w9.recordTareWeight(new BigDecimal("7500.00"));
            w9.complete();
            w9 = weighingRepository.save(w9);

            WeighingRecord w10 = WeighingRecord.builder()
                    .dispatchId(d8.getDispatchId()).scaleId(scale2.getScaleId())
                    .weighingMode(WeighingMode.MANUAL).weighingStep(WeighingStep.SECOND)
                    .grossWeight(new BigDecimal("27200.00"))
                    .build();
            w10.recordTareWeight(new BigDecimal("8500.00"));
            w10.complete();
            w10 = weighingRepository.save(w10);

            WeighingRecord w11 = WeighingRecord.builder()
                    .dispatchId(d9.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.MANUAL).weighingStep(WeighingStep.SECOND)
                    .grossWeight(new BigDecimal("24100.00"))
                    .build();
            w11.recordTareWeight(new BigDecimal("12000.00"));
            w11.complete();
            w11 = weighingRepository.save(w11);

            // RE_WEIGHING (1건)
            WeighingRecord w12 = WeighingRecord.builder()
                    .dispatchId(d7.getDispatchId()).scaleId(scale1.getScaleId())
                    .weighingMode(WeighingMode.RE_WEIGH).weighingStep(WeighingStep.FIRST)
                    .grossWeight(new BigDecimal("33500.00"))
                    .build();
            w12.markReWeighing("초기 계량값 이상 - 재계량 요청");
            w12 = weighingRepository.save(w12);

            log.info("계량 실적 12건 생성 완료");

            // ========================================
            // 8. 출문 관리 (GatePass) 8건
            // ========================================
            // PENDING (2건)
            gatePassRepository.save(GatePass.builder()
                    .weighingId(w4.getWeighingId()).dispatchId(d8.getDispatchId()).build());
            gatePassRepository.save(GatePass.builder()
                    .weighingId(w5.getWeighingId()).dispatchId(d9.getDispatchId()).build());

            // PASSED (5건)
            GatePass gp3 = gatePassRepository.save(GatePass.builder()
                    .weighingId(w6.getWeighingId()).dispatchId(d10.getDispatchId()).build());
            gp3.pass(operator.getUserId());
            gatePassRepository.save(gp3);

            GatePass gp4 = gatePassRepository.save(GatePass.builder()
                    .weighingId(w7.getWeighingId()).dispatchId(d11.getDispatchId()).build());
            gp4.pass(operator.getUserId());
            gatePassRepository.save(gp4);

            GatePass gp5 = gatePassRepository.save(GatePass.builder()
                    .weighingId(w8.getWeighingId()).dispatchId(d12.getDispatchId()).build());
            gp5.pass(manager.getUserId());
            gatePassRepository.save(gp5);

            GatePass gp6 = gatePassRepository.save(GatePass.builder()
                    .weighingId(w9.getWeighingId()).dispatchId(d13.getDispatchId()).build());
            gp6.pass(manager.getUserId());
            gatePassRepository.save(gp6);

            GatePass gp7 = gatePassRepository.save(GatePass.builder()
                    .weighingId(w10.getWeighingId()).dispatchId(d8.getDispatchId()).build());
            gp7.pass(operator.getUserId());
            gatePassRepository.save(gp7);

            // REJECTED (1건)
            GatePass gp8 = gatePassRepository.save(GatePass.builder()
                    .weighingId(w11.getWeighingId()).dispatchId(d9.getDispatchId()).build());
            gp8.reject(manager.getUserId(), "중량 초과 - 적재량 재확인 필요");
            gatePassRepository.save(gp8);

            log.info("출문 관리 8건 생성 완료");

            // ========================================
            // 9. 계량전표 (WeighingSlip) 5건
            // ========================================
            String datePrefix = today.toString().replace("-", "").substring(2);

            weighingSlipRepository.save(WeighingSlip.builder()
                    .weighingId(w6.getWeighingId()).dispatchId(d10.getDispatchId())
                    .slipNumber("WS" + datePrefix + "-0001")
                    .slipData("{\"company\":\"부산물류\",\"vehicle\":\"경남56다7890\",\"item\":\"코크스\",\"grossWeight\":28200,\"tareWeight\":10000,\"netWeight\":18200}")
                    .vehiclePlateNumber("경남56다7890").companyName("부산물류")
                    .itemName("코크스")
                    .grossWeightKg("28,200 kg").tareWeightKg("10,000 kg").netWeightKg("18,200 kg")
                    .build());

            weighingSlipRepository.save(WeighingSlip.builder()
                    .weighingId(w7.getWeighingId()).dispatchId(d11.getDispatchId())
                    .slipNumber("WS" + datePrefix + "-0002")
                    .slipData("{\"company\":\"한진운수\",\"vehicle\":\"경남90마5678\",\"item\":\"냉연코일\",\"grossWeight\":35600,\"tareWeight\":9000,\"netWeight\":26600}")
                    .vehiclePlateNumber("경남90마5678").companyName("한진운수")
                    .itemName("냉연코일")
                    .grossWeightKg("35,600 kg").tareWeightKg("9,000 kg").netWeightKg("26,600 kg")
                    .build());

            weighingSlipRepository.save(WeighingSlip.builder()
                    .weighingId(w8.getWeighingId()).dispatchId(d12.getDispatchId())
                    .slipNumber("WS" + datePrefix + "-0003")
                    .slipData("{\"company\":\"한진운수\",\"vehicle\":\"부산11바9012\",\"item\":\"고로슬래그\",\"grossWeight\":38200,\"tareWeight\":13000,\"netWeight\":25200}")
                    .vehiclePlateNumber("부산11바9012").companyName("한진운수")
                    .itemName("고로슬래그")
                    .grossWeightKg("38,200 kg").tareWeightKg("13,000 kg").netWeightKg("25,200 kg")
                    .build());

            weighingSlipRepository.save(WeighingSlip.builder()
                    .weighingId(w9.getWeighingId()).dispatchId(d13.getDispatchId())
                    .slipNumber("WS" + datePrefix + "-0004")
                    .slipData("{\"company\":\"부산물류\",\"vehicle\":\"부산78라1234\",\"item\":\"철스크랩\",\"grossWeight\":19800,\"tareWeight\":7500,\"netWeight\":12300}")
                    .vehiclePlateNumber("부산78라1234").companyName("부산물류")
                    .itemName("철스크랩")
                    .grossWeightKg("19,800 kg").tareWeightKg("7,500 kg").netWeightKg("12,300 kg")
                    .build());

            WeighingSlip slip5 = weighingSlipRepository.save(WeighingSlip.builder()
                    .weighingId(w10.getWeighingId()).dispatchId(d8.getDispatchId())
                    .slipNumber("WS" + datePrefix + "-0005")
                    .slipData("{\"company\":\"동국운송\",\"vehicle\":\"부산12가3456\",\"item\":\"슬래그\",\"grossWeight\":27200,\"tareWeight\":8500,\"netWeight\":18700}")
                    .vehiclePlateNumber("부산12가3456").companyName("동국운송")
                    .itemName("슬래그")
                    .grossWeightKg("27,200 kg").tareWeightKg("8,500 kg").netWeightKg("18,700 kg")
                    .build());
            slip5.markShared("EMAIL");
            weighingSlipRepository.save(slip5);

            log.info("계량전표 5건 생성 완료");

            // ========================================
            // 10. LPR 캡처 (LprCapture) 6건
            // ========================================
            LocalDateTime now = LocalDateTime.now();

            // CONFIRMED (3건)
            LprCapture lpr1 = lprCaptureRepository.save(LprCapture.builder()
                    .scaleId(scale1.getScaleId())
                    .rawPlateNumber("부산12가3456")
                    .captureTimestamp(now.minusHours(5))
                    .sensorEvent("VEHICLE_DETECTED")
                    .build());
            lpr1.applyAiVerification("부산12가3456", new BigDecimal("0.9510"));
            lpr1.applyDispatchMatch(d8.getDispatchId(), v1.getVehicleId());
            lprCaptureRepository.save(lpr1);

            LprCapture lpr2 = lprCaptureRepository.save(LprCapture.builder()
                    .scaleId(scale1.getScaleId())
                    .rawPlateNumber("부산34나5678")
                    .captureTimestamp(now.minusHours(3))
                    .sensorEvent("VEHICLE_DETECTED")
                    .build());
            lpr2.applyAiVerification("부산34나5678", new BigDecimal("0.9650"));
            lpr2.applyDispatchMatch(d4.getDispatchId(), v2.getVehicleId());
            lprCaptureRepository.save(lpr2);

            LprCapture lpr3 = lprCaptureRepository.save(LprCapture.builder()
                    .scaleId(scale2.getScaleId())
                    .rawPlateNumber("경남90마5678")
                    .captureTimestamp(now.minusHours(2))
                    .sensorEvent("VEHICLE_DETECTED")
                    .build());
            lpr3.applyAiVerification("경남90마5678", new BigDecimal("0.9820"));
            lpr3.applyDispatchMatch(d6.getDispatchId(), v5.getVehicleId());
            lprCaptureRepository.save(lpr3);

            // LOW_CONFIDENCE (2건)
            LprCapture lpr4 = lprCaptureRepository.save(LprCapture.builder()
                    .scaleId(scale1.getScaleId())
                    .rawPlateNumber("부산11바9O12")
                    .captureTimestamp(now.minusHours(8))
                    .sensorEvent("VEHICLE_DETECTED")
                    .build());
            lpr4.applyAiVerification("부산11바9012", new BigDecimal("0.8500"));
            lprCaptureRepository.save(lpr4);

            LprCapture lpr5 = lprCaptureRepository.save(LprCapture.builder()
                    .scaleId(scale3.getScaleId())
                    .rawPlateNumber("서울22사34S6")
                    .captureTimestamp(now.minusHours(6))
                    .sensorEvent("VEHICLE_DETECTED")
                    .build());
            lpr5.applyAiVerification("서울22사3456", new BigDecimal("0.7800"));
            lprCaptureRepository.save(lpr5);

            // FAILED (1건)
            LprCapture lpr6 = lprCaptureRepository.save(LprCapture.builder()
                    .scaleId(scale2.getScaleId())
                    .rawPlateNumber("??44??7890")
                    .captureTimestamp(now.minusHours(10))
                    .sensorEvent("VEHICLE_DETECTED")
                    .build());
            lpr6.applyAiVerification("서울44아7890", new BigDecimal("0.4500"));
            lprCaptureRepository.save(lpr6);

            log.info("LPR 캡처 6건 생성 완료");

            // ========================================
            // 11. 문의/호출 (InquiryCall) 4건
            // ========================================
            inquiryCallRepository.save(InquiryCall.builder()
                    .callerId(driver1.getUserId()).callerName("운전자").callerPhone("010-2222-2222")
                    .inquiryType(InquiryType.WEIGHING_ISSUE)
                    .subject("계량값 이상 문의")
                    .content("1번 계량대에서 계량 시 공차 중량이 평소보다 1톤 이상 높게 측정됩니다. 확인 부탁드립니다.")
                    .weighingId(w12.getWeighingId()).dispatchId(d7.getDispatchId())
                    .build());

            inquiryCallRepository.save(InquiryCall.builder()
                    .callerId(driver2.getUserId()).callerName("박기사").callerPhone("010-4444-4444")
                    .inquiryType(InquiryType.DISPATCH_ISSUE)
                    .subject("배차 일정 변경 요청")
                    .content("내일 예정된 코크스 운송 건이 차량 정비로 인해 하루 연기 요청합니다.")
                    .dispatchId(d5.getDispatchId())
                    .build());

            inquiryCallRepository.save(InquiryCall.builder()
                    .callerId(driver3.getUserId()).callerName("최운전").callerPhone("010-6666-6666")
                    .inquiryType(InquiryType.GENERAL_INQUIRY)
                    .subject("모바일 앱 OTP 인증 문의")
                    .content("모바일 앱에서 OTP 인증 시 번호가 오지 않습니다. 확인 부탁드립니다.")
                    .build());

            inquiryCallRepository.save(InquiryCall.builder()
                    .callerId(manager.getUserId()).callerName("담당자").callerPhone("010-1111-1111")
                    .inquiryType(InquiryType.SYSTEM_ERROR)
                    .subject("대시보드 통계 오류")
                    .content("대시보드의 일별 계량 현황 그래프가 어제 데이터를 포함하지 않고 있습니다.")
                    .build());

            log.info("문의/호출 4건 생성 완료");

            // ========================================
            // 12. 알림 (Notification) 6건
            // ========================================
            Notification n1 = notificationRepository.save(Notification.builder()
                    .userId(driver1.getUserId())
                    .notificationType(NotificationType.DISPATCH_ASSIGNED)
                    .title("배차가 배정되었습니다")
                    .message("슬래그 운송 배차가 배정되었습니다. 배차일: " + today)
                    .referenceId(d1.getDispatchId())
                    .build());

            notificationRepository.save(Notification.builder()
                    .userId(driver2.getUserId())
                    .notificationType(NotificationType.DISPATCH_ASSIGNED)
                    .title("배차가 배정되었습니다")
                    .message("석회석 운송 배차가 배정되었습니다. 배차일: " + today)
                    .referenceId(d5.getDispatchId())
                    .build());

            Notification n3 = notificationRepository.save(Notification.builder()
                    .userId(driver1.getUserId())
                    .notificationType(NotificationType.WEIGHING_COMPLETED)
                    .title("계량이 완료되었습니다")
                    .message("슬래그 운송 건 계량 완료. 순중량: 19,000 kg")
                    .referenceId(w4.getWeighingId())
                    .build());
            n3.markAsRead();
            notificationRepository.save(n3);

            notificationRepository.save(Notification.builder()
                    .userId(driver3.getUserId())
                    .notificationType(NotificationType.GATE_PASS_ISSUED)
                    .title("출문증이 발행되었습니다")
                    .message("냉연코일 운송 건 출문 승인 완료")
                    .referenceId(w7.getWeighingId())
                    .build());

            Notification n5 = notificationRepository.save(Notification.builder()
                    .userId(admin.getUserId())
                    .notificationType(NotificationType.SYSTEM_NOTICE)
                    .title("시스템 정기 점검 안내")
                    .message("금일 22:00~23:00 시스템 정기 점검이 예정되어 있습니다.")
                    .build());
            n5.markAsRead();
            notificationRepository.save(n5);

            notificationRepository.save(Notification.builder()
                    .userId(manager.getUserId())
                    .notificationType(NotificationType.SYSTEM_NOTICE)
                    .title("계량대 정기 교정 안내")
                    .message("1번 계량대 정기 교정이 내일 오전 중 진행 예정입니다.")
                    .build());

            log.info("알림 6건 생성 완료");

            // ========================================
            // 로그인 정보 출력
            // ========================================
            log.info("============================================");
            log.info("=== DEV 초기 데이터 로드 완료 ===");
            log.info("============================================");
            log.info("사용자 계정 정보:");
            log.info("  Admin:    admin / admin1234");
            log.info("  Manager:  manager / manager1234");
            log.info("  Operator: operator / operator1234");
            log.info("  Driver1:  driver / driver1234");
            log.info("  Driver2:  driver2 / driver1234");
            log.info("  Driver3:  driver3 / driver1234");
            log.info("--------------------------------------------");
            log.info("데이터 현황:");
            log.info("  회사: 4개 | 차량: 8대 | 계량대: 3대");
            log.info("  공통코드: {}건 | 배차: 15건", codes.size());
            log.info("  계량: 12건 | 출문: 8건 | 전표: 5건");
            log.info("  LPR: 6건 | 문의: 4건 | 알림: 6건");
            log.info("============================================");
        };
    }
}
