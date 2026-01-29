package com.dongkuk.weighing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 부산 스마트 계량 시스템 메인 애플리케이션
 *
 * Spring Boot 애플리케이션의 진입점이다.
 * 동국제강 부산공장의 차량 계량 업무를 자동화하는 시스템으로,
 * LPR(차량번호 자동인식), OTP 인증, 실시간 계량 모니터링,
 * 배차 관리, 출문 관리, 전자 계량표 발행 등의 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
@SpringBootApplication
public class WeighingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeighingApplication.class, args);
    }
}
