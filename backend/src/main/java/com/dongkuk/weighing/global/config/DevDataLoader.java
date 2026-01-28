package com.dongkuk.weighing.global.config;

import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DevDataLoader {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadDevData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .userName("관리자")
                        .phoneNumber("010-0000-0000")
                        .userRole(UserRole.ADMIN)
                        .loginId("admin")
                        .passwordHash(passwordEncoder.encode("admin1234"))
                        .build();
                userRepository.save(admin);

                User manager = User.builder()
                        .userName("담당자")
                        .phoneNumber("010-1111-1111")
                        .userRole(UserRole.MANAGER)
                        .loginId("manager")
                        .passwordHash(passwordEncoder.encode("manager1234"))
                        .build();
                userRepository.save(manager);

                User driver = User.builder()
                        .userName("운전자")
                        .phoneNumber("010-2222-2222")
                        .userRole(UserRole.DRIVER)
                        .loginId("driver")
                        .passwordHash(passwordEncoder.encode("driver1234"))
                        .build();
                userRepository.save(driver);

                log.info("=== DEV 초기 데이터 로드 완료 ===");
                log.info("Admin: admin / admin1234");
                log.info("Manager: manager / manager1234");
                log.info("Driver: driver / driver1234");
            }
        };
    }
}
