package com.dongkuk.weighing.global.config;

import com.dongkuk.weighing.dispatch.domain.Dispatch;
import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.dispatch.domain.ItemType;
import com.dongkuk.weighing.gatepass.domain.GatePass;
import com.dongkuk.weighing.gatepass.domain.GatePassRepository;
import com.dongkuk.weighing.help.domain.Faq;
import com.dongkuk.weighing.help.domain.FaqCategory;
import com.dongkuk.weighing.help.domain.FaqRepository;
import com.dongkuk.weighing.monitoring.domain.ConnectionStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceStatusRepository;
import com.dongkuk.weighing.monitoring.domain.DeviceType;
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
import com.dongkuk.weighing.notice.domain.Notice;
import com.dongkuk.weighing.notice.domain.NoticeCategory;
import com.dongkuk.weighing.notice.domain.NoticeRepository;
import com.dongkuk.weighing.notification.domain.Notification;
import com.dongkuk.weighing.notification.domain.NotificationRepository;
import com.dongkuk.weighing.notification.domain.NotificationType;
import com.dongkuk.weighing.setting.domain.SettingCategory;
import com.dongkuk.weighing.setting.domain.SettingType;
import com.dongkuk.weighing.setting.domain.SystemSetting;
import com.dongkuk.weighing.setting.domain.SystemSettingRepository;
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

/**
 * 개발 환경 전용 초기 데이터 로더
 *
 * 개발(dev) 프로필에서만 활성화되며, 애플리케이션 시작 시
 * 테스트/개발용 초기 데이터를 데이터베이스에 자동 생성한다.
 * 회사, 차량, 계량대, 공통코드, 사용자, 배차, 계량실적,
 * 출문관리, 계량전표, LPR 캡처, 문의, 알림, 시스템설정,
 * 공지사항, 장비상태, FAQ 등 전체 도메인의 샘플 데이터를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
            NotificationRepository notificationRepository,
            SystemSettingRepository systemSettingRepository,
            NoticeRepository noticeRepository,
            DeviceStatusRepository deviceStatusRepository,
            FaqRepository faqRepository
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
                    // 품목 유형
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("BY_PRODUCT").codeName("부산물").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("WASTE").codeName("폐기물").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("SUB_MATERIAL").codeName("부재료").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("EXPORT").codeName("반출").sortOrder(4).build(),
                    CommonCode.builder().codeGroup("ITEM_TYPE").codeValue("GENERAL").codeName("일반").sortOrder(5).build(),
                    // 차량 유형
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("CARGO").codeName("카고").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("DUMP").codeName("덤프").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("TANK").codeName("탱크로리").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("WING").codeName("윙바디").sortOrder(4).build(),
                    CommonCode.builder().codeGroup("VEHICLE_TYPE").codeValue("TRAILER").codeName("트레일러").sortOrder(5).build(),
                    // 계량 모드
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("LPR_AUTO").codeName("LPR 자동").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("MOBILE_OTP").codeName("모바일 OTP").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("MANUAL").codeName("수동").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("WEIGHING_MODE").codeValue("RE_WEIGH").codeName("재계량").sortOrder(4).build(),
                    // 회사 유형
                    CommonCode.builder().codeGroup("COMPANY_TYPE").codeValue("TRANSPORT").codeName("운송").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("COMPANY_TYPE").codeValue("LOGISTICS").codeName("물류").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("COMPANY_TYPE").codeValue("CONSTRUCTION").codeName("건설").sortOrder(3).build(),
                    // 배차 상태
                    CommonCode.builder().codeGroup("DISPATCH_STATUS").codeValue("REGISTERED").codeName("등록").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("DISPATCH_STATUS").codeValue("IN_PROGRESS").codeName("진행중").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("DISPATCH_STATUS").codeValue("COMPLETED").codeName("완료").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("DISPATCH_STATUS").codeValue("CANCELLED").codeName("취소").sortOrder(4).build(),
                    // 계량 상태
                    CommonCode.builder().codeGroup("WEIGHING_STATUS").codeValue("IN_PROGRESS").codeName("진행중").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("WEIGHING_STATUS").codeValue("COMPLETED").codeName("완료").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("WEIGHING_STATUS").codeValue("RE_WEIGHING").codeName("재계량").sortOrder(3).build(),
                    CommonCode.builder().codeGroup("WEIGHING_STATUS").codeValue("CANCELLED").codeName("취소").sortOrder(4).build(),
                    // 출문 상태
                    CommonCode.builder().codeGroup("GATE_PASS_STATUS").codeValue("PENDING").codeName("대기").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("GATE_PASS_STATUS").codeValue("PASSED").codeName("통과").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("GATE_PASS_STATUS").codeValue("REJECTED").codeName("거부").sortOrder(3).build(),
                    // 계량대 상태
                    CommonCode.builder().codeGroup("SCALE_STATUS").codeValue("ACTIVE").codeName("가동중").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("SCALE_STATUS").codeValue("MAINTENANCE").codeName("점검중").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("SCALE_STATUS").codeValue("INACTIVE").codeName("중지").sortOrder(3).build(),
                    // 사용자 역할
                    CommonCode.builder().codeGroup("USER_ROLE").codeValue("ADMIN").codeName("관리자").sortOrder(1).build(),
                    CommonCode.builder().codeGroup("USER_ROLE").codeValue("MANAGER").codeName("담당자").sortOrder(2).build(),
                    CommonCode.builder().codeGroup("USER_ROLE").codeValue("DRIVER").codeName("운전자").sortOrder(3).build()
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
            // 7-1. 통계용 과거 60일 계량 데이터 생성
            // ========================================
            Company[] companies = {company1, company2, company3, company4};
            Vehicle[] vehicles = {v1, v2, v3, v4, v5, v6, v7, v8};
            Scale[] scales = {scale1, scale2, scale3};
            ItemType[] itemTypes = ItemType.values();
            String[] itemNames = {"슬래그", "더스트", "폐유", "스케일", "코크스", "석회석", "열연코일", "냉연코일", "H빔", "철스크랩"};
            WeighingMode[] modes = {WeighingMode.LPR_AUTO, WeighingMode.MOBILE_OTP, WeighingMode.MANUAL};

            java.util.Random random = new java.util.Random(42); // 재현 가능한 랜덤
            int historicalCount = 0;

            for (int daysAgo = 60; daysAgo >= 1; daysAgo--) {
                LocalDate targetDate = today.minusDays(daysAgo);
                LocalDateTime targetDateTime = targetDate.atTime(9, 0); // 오전 9시 기준

                // 하루에 3~8건의 계량 데이터 생성
                int dailyRecords = 3 + random.nextInt(6);

                for (int r = 0; r < dailyRecords; r++) {
                    Company company = companies[random.nextInt(companies.length)];
                    Vehicle vehicle = vehicles[random.nextInt(vehicles.length)];
                    Scale scale = scales[random.nextInt(scales.length)];
                    ItemType itemType = itemTypes[random.nextInt(itemTypes.length)];
                    String itemName = itemNames[random.nextInt(itemNames.length)];
                    WeighingMode mode = modes[random.nextInt(modes.length)];

                    // 시간을 랜덤하게 분산 (오전 8시 ~ 오후 5시)
                    LocalDateTime recordTime = targetDate.atTime(8 + random.nextInt(9), random.nextInt(60));

                    // 배차 생성
                    Dispatch histDispatch = Dispatch.builder()
                            .vehicleId(vehicle.getVehicleId())
                            .companyId(company.getCompanyId())
                            .itemType(itemType)
                            .itemName(itemName)
                            .dispatchDate(targetDate)
                            .originLocation("공장")
                            .destination("처리장")
                            .createdBy(manager.getUserId())
                            .build();
                    histDispatch.startProgress();
                    histDispatch.complete();
                    histDispatch = dispatchRepository.save(histDispatch);

                    // 중량 랜덤 생성 (15000 ~ 40000 kg)
                    BigDecimal grossWeight = new BigDecimal(15000 + random.nextInt(25000));
                    BigDecimal tareWeight = new BigDecimal(7000 + random.nextInt(6000));

                    // 계량 레코드 생성
                    WeighingRecord histWeighing = WeighingRecord.builder()
                            .dispatchId(histDispatch.getDispatchId())
                            .scaleId(scale.getScaleId())
                            .weighingMode(mode)
                            .weighingStep(WeighingStep.FIRST)
                            .grossWeight(grossWeight)
                            .build();

                    if (mode == WeighingMode.LPR_AUTO) {
                        // LPR 데이터 설정 (reflection 또는 별도 처리 필요 시)
                    }

                    histWeighing.recordTareWeight(tareWeight);
                    histWeighing.complete();

                    // createdAt을 과거 날짜로 설정 (저장 전에 설정해야 함)
                    histWeighing.setCreatedAtForDevData(recordTime);
                    weighingRepository.save(histWeighing);

                    historicalCount++;
                }
            }

            log.info("통계용 과거 데이터 {}건 생성 완료 (60일치)", historicalCount);

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
            // 13. 시스템 설정 (SystemSetting)
            // ========================================
            List<SystemSetting> settings = List.of(
                    // GENERAL
                    SystemSetting.builder()
                            .settingKey("SYSTEM_NAME")
                            .settingValue("부산 스마트 계량 시스템")
                            .settingType(SettingType.STRING)
                            .category(SettingCategory.GENERAL)
                            .description("시스템 명칭")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("COMPANY_NAME")
                            .settingValue("동국제강 부산공장")
                            .settingType(SettingType.STRING)
                            .category(SettingCategory.GENERAL)
                            .description("회사명")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("DATA_RETENTION_DAYS")
                            .settingValue("365")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.GENERAL)
                            .description("데이터 보관 기간 (일)")
                            .isEditable(true)
                            .build(),

                    // WEIGHING
                    SystemSetting.builder()
                            .settingKey("WEIGHING_AUTO_COMPLETE")
                            .settingValue("true")
                            .settingType(SettingType.BOOLEAN)
                            .category(SettingCategory.WEIGHING)
                            .description("계량 자동 완료 여부")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("WEIGHING_TOLERANCE_KG")
                            .settingValue("50")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.WEIGHING)
                            .description("계량 허용 오차 (kg)")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("LPR_CONFIDENCE_THRESHOLD")
                            .settingValue("0.85")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.WEIGHING)
                            .description("LPR 인식 신뢰도 임계값")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("RE_WEIGH_ALLOWED")
                            .settingValue("true")
                            .settingType(SettingType.BOOLEAN)
                            .category(SettingCategory.WEIGHING)
                            .description("재계량 허용 여부")
                            .isEditable(true)
                            .build(),

                    // NOTIFICATION
                    SystemSetting.builder()
                            .settingKey("NOTIFICATION_ENABLED")
                            .settingValue("true")
                            .settingType(SettingType.BOOLEAN)
                            .category(SettingCategory.NOTIFICATION)
                            .description("알림 기능 활성화")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("PUSH_NOTIFICATION_ENABLED")
                            .settingValue("true")
                            .settingType(SettingType.BOOLEAN)
                            .category(SettingCategory.NOTIFICATION)
                            .description("푸시 알림 활성화")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("EMAIL_NOTIFICATION_ENABLED")
                            .settingValue("false")
                            .settingType(SettingType.BOOLEAN)
                            .category(SettingCategory.NOTIFICATION)
                            .description("이메일 알림 활성화")
                            .isEditable(true)
                            .build(),

                    // SECURITY
                    SystemSetting.builder()
                            .settingKey("MAX_LOGIN_ATTEMPTS")
                            .settingValue("5")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.SECURITY)
                            .description("최대 로그인 시도 횟수")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("ACCOUNT_LOCK_DURATION_MINUTES")
                            .settingValue("30")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.SECURITY)
                            .description("계정 잠금 시간 (분)")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("SESSION_TIMEOUT_MINUTES")
                            .settingValue("60")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.SECURITY)
                            .description("세션 타임아웃 (분)")
                            .isEditable(true)
                            .build(),
                    SystemSetting.builder()
                            .settingKey("PASSWORD_MIN_LENGTH")
                            .settingValue("8")
                            .settingType(SettingType.NUMBER)
                            .category(SettingCategory.SECURITY)
                            .description("비밀번호 최소 길이")
                            .isEditable(false)
                            .build()
            );
            systemSettingRepository.saveAll(settings);
            log.info("시스템 설정 {}건 생성 완료", settings.size());

            // ========================================
            // 14. 공지사항 (Notice) 4건
            // ========================================
            Notice notice1 = noticeRepository.save(Notice.builder()
                    .title("동국씨엠 스마트 계량 시스템 오픈 안내")
                    .content("안녕하세요.\n\n동국씨엠 스마트 계량 시스템이 정식 오픈되었습니다.\n\n주요 기능:\n- LPR 자동 차량 인식\n- 모바일 OTP 인증\n- 실시간 계량 현황 모니터링\n- 전자 계량표 발행\n\n많은 이용 부탁드립니다.\n\n감사합니다.")
                    .category(NoticeCategory.SYSTEM)
                    .authorId(admin.getUserId())
                    .authorName("관리자")
                    .isPublished(true)
                    .isPinned(true)
                    .build());

            Notice notice2 = noticeRepository.save(Notice.builder()
                    .title("[정기점검] 1월 30일 시스템 점검 안내")
                    .content("안녕하세요.\n\n아래와 같이 시스템 정기점검이 진행될 예정입니다.\n\n■ 점검 일시: 2026년 1월 30일(목) 22:00 ~ 23:00\n■ 점검 내용: 서버 보안 업데이트 및 성능 최적화\n■ 영향 범위: 점검 시간 동안 시스템 이용 불가\n\n점검 시간 동안 불편을 드려 죄송합니다.\n\n감사합니다.")
                    .category(NoticeCategory.MAINTENANCE)
                    .authorId(admin.getUserId())
                    .authorName("관리자")
                    .isPublished(true)
                    .isPinned(false)
                    .build());

            Notice notice3 = noticeRepository.save(Notice.builder()
                    .title("[업데이트] 모바일 앱 v2.0 업데이트 안내")
                    .content("안녕하세요.\n\n모바일 앱 v2.0 업데이트가 진행되었습니다.\n\n■ 주요 변경사항\n1. UI/UX 개선 - 더 직관적인 화면 구성\n2. OTP 인증 속도 개선\n3. 푸시 알림 안정화\n4. 즐겨찾기 기능 추가\n5. 다크모드 지원\n\n앱 스토어에서 최신 버전으로 업데이트해 주세요.\n\n감사합니다.")
                    .category(NoticeCategory.UPDATE)
                    .authorId(admin.getUserId())
                    .authorName("관리자")
                    .isPublished(true)
                    .isPinned(false)
                    .build());

            Notice notice4 = noticeRepository.save(Notice.builder()
                    .title("2026년 설 연휴 운영 안내")
                    .content("안녕하세요.\n\n2026년 설 연휴 계량장 운영 안내드립니다.\n\n■ 연휴 기간: 1월 28일(화) ~ 1월 30일(목)\n■ 운영 시간\n  - 1월 28일(화): 정상 운영 (08:00~18:00)\n  - 1월 29일(수): 휴무\n  - 1월 30일(목): 휴무\n  - 1월 31일(금): 정상 운영\n\n연휴 기간 중 긴급 문의: 051-123-4567\n\n즐거운 명절 보내세요.\n\n감사합니다.")
                    .category(NoticeCategory.GENERAL)
                    .authorId(admin.getUserId())
                    .authorName("관리자")
                    .isPublished(true)
                    .isPinned(false)
                    .build());

            log.info("공지사항 4건 생성 완료");

            // ========================================
            // 15. 장비 상태 (DeviceStatus)
            // ========================================
            DeviceStatus device1 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("SCALE-001")
                    .deviceName("1번 계량대")
                    .deviceType(DeviceType.SCALE)
                    .location("정문 입구")
                    .ipAddress("192.168.1.101")
                    .build());
            device1.setOnline();
            deviceStatusRepository.save(device1);

            DeviceStatus device2 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("SCALE-002")
                    .deviceName("2번 계량대")
                    .deviceType(DeviceType.SCALE)
                    .location("후문 출구")
                    .ipAddress("192.168.1.102")
                    .build());
            device2.setOnline();
            deviceStatusRepository.save(device2);

            DeviceStatus device3 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("SCALE-003")
                    .deviceName("3번 계량대")
                    .deviceType(DeviceType.SCALE)
                    .location("부산물 처리장")
                    .ipAddress("192.168.1.103")
                    .build());
            device3.setOffline();
            deviceStatusRepository.save(device3);

            DeviceStatus device4 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("SCALE-004")
                    .deviceName("4번 계량대 (예비)")
                    .deviceType(DeviceType.SCALE)
                    .location("예비 구역")
                    .ipAddress("192.168.1.104")
                    .build());
            device4.setOffline();
            deviceStatusRepository.save(device4);

            DeviceStatus device5 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("LPR-001")
                    .deviceName("정문 LPR 카메라")
                    .deviceType(DeviceType.LPR_CAMERA)
                    .location("정문 입구")
                    .ipAddress("192.168.1.201")
                    .build());
            device5.setOnline();
            deviceStatusRepository.save(device5);

            DeviceStatus device6 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("LPR-002")
                    .deviceName("후문 LPR 카메라")
                    .deviceType(DeviceType.LPR_CAMERA)
                    .location("후문 출구")
                    .ipAddress("192.168.1.202")
                    .build());
            device6.setOnline();
            deviceStatusRepository.save(device6);

            DeviceStatus device7 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("IND-001")
                    .deviceName("1번 계량 지시기")
                    .deviceType(DeviceType.INDICATOR)
                    .location("정문 계량실")
                    .ipAddress("192.168.1.301")
                    .build());
            device7.setOnline();
            deviceStatusRepository.save(device7);

            DeviceStatus device8 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("IND-002")
                    .deviceName("2번 계량 지시기")
                    .deviceType(DeviceType.INDICATOR)
                    .location("후문 계량실")
                    .ipAddress("192.168.1.302")
                    .build());
            device8.setError("통신 오류 - 연결 재시도 필요");
            deviceStatusRepository.save(device8);

            DeviceStatus device9 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("GATE-001")
                    .deviceName("정문 차단기")
                    .deviceType(DeviceType.BARRIER_GATE)
                    .location("정문 입구")
                    .ipAddress("192.168.1.401")
                    .build());
            device9.setOnline();
            deviceStatusRepository.save(device9);

            DeviceStatus device10 = deviceStatusRepository.save(DeviceStatus.builder()
                    .deviceCode("GATE-002")
                    .deviceName("후문 차단기")
                    .deviceType(DeviceType.BARRIER_GATE)
                    .location("후문 출구")
                    .ipAddress("192.168.1.402")
                    .build());
            device10.setOnline();
            deviceStatusRepository.save(device10);

            log.info("장비 상태 10건 생성 완료");

            // ========================================
            // 16. FAQ
            // ========================================
            faqRepository.save(Faq.builder()
                    .question("계량 시 차량번호가 인식되지 않으면 어떻게 하나요?")
                    .answer("LPR 카메라가 차량번호를 인식하지 못하는 경우, 다음 방법으로 계량을 진행할 수 있습니다:\n\n1. 모바일 OTP 인증: 모바일 앱에서 OTP 인증을 통해 계량을 진행합니다.\n2. 수동 계량: 계량실 담당자에게 요청하여 수동으로 차량번호를 입력하여 계량합니다.\n\n차량번호판이 오염되었거나 손상된 경우, 세차 후 재시도하시면 인식률이 높아집니다.")
                    .category(FaqCategory.WEIGHING)
                    .sortOrder(1)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("계량 결과가 실제와 다른 것 같습니다. 재계량이 가능한가요?")
                    .answer("네, 재계량이 가능합니다.\n\n재계량 요청 절차:\n1. 계량실 담당자에게 재계량 요청\n2. 재계량 사유 확인 (계량대 이상, 차량 적재 상태 변경 등)\n3. 담당자 승인 후 재계량 진행\n\n재계량 시 기존 계량 기록은 '재계량' 상태로 변경되며, 새로운 계량 기록이 생성됩니다.")
                    .category(FaqCategory.WEIGHING)
                    .sortOrder(2)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("배차 등록은 어떻게 하나요?")
                    .answer("배차 등록 방법:\n\n1. 웹 관리 시스템에서 '배차 관리' 메뉴로 이동\n2. '배차 등록' 버튼 클릭\n3. 필수 정보 입력\n   - 차량 선택\n   - 운송사 선택\n   - 품목 유형 및 품목명\n   - 배차일\n   - 출발지/목적지\n4. '등록' 버튼 클릭\n\n등록된 배차는 해당 운전자에게 자동으로 알림이 발송됩니다.")
                    .category(FaqCategory.DISPATCH)
                    .sortOrder(1)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("배차 상태는 어떻게 변경되나요?")
                    .answer("배차 상태 흐름:\n\n1. 등록: 배차가 처음 생성된 상태\n2. 진행중: 차량이 첫 번째 계량을 시작한 상태\n3. 완료: 모든 계량이 완료되고 출문 처리된 상태\n4. 취소: 배차가 취소된 상태 (등록 상태에서만 취소 가능)\n\n상태 변경은 계량 진행 상황에 따라 자동으로 업데이트됩니다.")
                    .category(FaqCategory.DISPATCH)
                    .sortOrder(2)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("비밀번호를 잊어버렸습니다. 어떻게 해야 하나요?")
                    .answer("비밀번호 분실 시 처리 방법:\n\n1. 시스템 관리자에게 비밀번호 초기화 요청\n2. 관리자가 임시 비밀번호로 초기화\n3. 임시 비밀번호로 로그인 후 '마이페이지'에서 비밀번호 변경\n\n보안을 위해 비밀번호는 8자 이상으로 설정해 주세요.\n\n문의: 051-123-4567 (시스템 관리자)")
                    .category(FaqCategory.ACCOUNT)
                    .sortOrder(1)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("계정이 잠겼습니다. 어떻게 해제하나요?")
                    .answer("계정 잠금 해제 방법:\n\n비밀번호를 5회 이상 잘못 입력하면 계정이 30분간 잠깁니다.\n\n1. 30분 후 자동 해제 대기\n2. 또는 시스템 관리자에게 즉시 해제 요청\n\n잦은 잠금이 발생하는 경우, 비밀번호 변경을 권장합니다.")
                    .category(FaqCategory.ACCOUNT)
                    .sortOrder(2)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("시스템 이용 시간은 언제인가요?")
                    .answer("시스템 이용 시간:\n\n- 웹 관리 시스템: 24시간 이용 가능\n- 계량장 운영: 평일 08:00 ~ 18:00\n- 정기 점검: 매월 마지막 주 목요일 22:00 ~ 23:00\n\n정기 점검 시간에는 시스템 이용이 제한될 수 있습니다.\n점검 일정은 공지사항을 통해 사전 안내됩니다.")
                    .category(FaqCategory.SYSTEM)
                    .sortOrder(1)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("전자 계량표는 어디서 확인할 수 있나요?")
                    .answer("전자 계량표 확인 방법:\n\n1. 웹 관리 시스템: '전자 계량표' 메뉴에서 조회/다운로드\n2. 모바일 앱: '계량 이력'에서 해당 건의 계량표 확인\n3. 이메일: 계량 완료 시 등록된 이메일로 자동 발송 (설정 시)\n\n계량표에는 QR 코드가 포함되어 있어 진위 여부를 확인할 수 있습니다.")
                    .category(FaqCategory.WEIGHING)
                    .sortOrder(3)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("문제가 발생했을 때 누구에게 연락하나요?")
                    .answer("문의처 안내:\n\n■ 계량 관련 문의\n- 계량실: 051-123-4567 (내선 101)\n- 운영시간: 평일 08:00 ~ 18:00\n\n■ 시스템 관련 문의\n- 시스템 관리자: 051-123-4567 (내선 102)\n- 이메일: support@dongkuk.com\n\n■ 긴급 상황\n- 야간/휴일 비상연락: 010-1234-5678")
                    .category(FaqCategory.OTHER)
                    .sortOrder(1)
                    .build());

            faqRepository.save(Faq.builder()
                    .question("모바일 앱은 어디서 다운로드하나요?")
                    .answer("모바일 앱 다운로드:\n\n■ Android\n- Google Play Store에서 '동국씨엠 스마트 계량' 검색\n- 또는 QR 코드 스캔 (계량실 게시판 참조)\n\n■ iOS\n- App Store에서 '동국씨엠 스마트 계량' 검색\n- 또는 QR 코드 스캔 (계량실 게시판 참조)\n\n앱 설치 후 기존 계정으로 로그인하시면 됩니다.")
                    .category(FaqCategory.SYSTEM)
                    .sortOrder(2)
                    .build());

            log.info("FAQ 10건 생성 완료");

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
            log.info("  공지사항: 4건 | 시스템 설정: {}건", settings.size());
            log.info("  장비 상태: 10건 | FAQ: 10건");
            log.info("============================================");
        };
    }
}
