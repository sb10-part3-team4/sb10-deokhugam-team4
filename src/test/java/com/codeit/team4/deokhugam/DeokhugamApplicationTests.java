package com.codeit.team4.deokhugam;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.config.TestContainerConfig;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.codeit.team4.deokhugam.global.filter.LoggingFilter;

@Slf4j
@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
class DeokhugamApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    @DisplayName("로그 출력 시 MDC 식별자와 IP가 지정된 전역 포맷으로 포함 성공")
    void logFormat_containsMdcInformation(CapturedOutput output) {
        // given
        String testRequestId = UUID.randomUUID().toString();
        String testClientIp = "192.168.0.1";

        MDC.put(LoggingFilter.REQUEST_ID_KEY, testRequestId);
        MDC.put(LoggingFilter.CLIENT_IP_KEY, testClientIp);

        // when
        log.info("요청 단위 로그 추적을 위한 테스트 메시지입니다.");

        // then
        MDC.clear(); // 검증 전 MDC 안전하게 초기화

        // logback-spring.xml 설정에 의해 [request_id] [client_ip] 포맷이 적용되었는지 검증
        assertThat(output.getOut()).contains("요청 단위 로그 추적을 위한 테스트 메시지입니다.");
        assertThat(output.getOut()).contains("[" + testRequestId + "]");
        assertThat(output.getOut()).contains("[" + testClientIp + "]");
    }

    // Spring Boot 처음 실행 시, 혹은 백그라운드 스케줄러(배치) 구동 시
    // HTTP 요청이 없어 필터를 거치지 않아 MDC가 비어있는 경우 테스트
    @Test
    @DisplayName("MDC 값이 없을 때 fallback 포맷(-SYSTEM, -NONE) 적용 성공")
    void logFormat_appliesFallback_whenMdcIsEmpty(CapturedOutput output) {
        // given
        MDC.clear(); // HTTP 요청이 아닌 내부 스케줄러/시스템 로직 상황을 가정하여 MDC를 완전히 비움

        // when
        log.info("MDC가 없는 백그라운드 배치의 로그 메시지입니다.");

        // then
        // logback-spring.xml의 `:-SYSTEM`, `:-NONE` 설정이 정상 작동하여 기본값이 찍히는지 검증
        assertThat(output.getOut()).contains("MDC가 없는 백그라운드 배치의 로그 메시지입니다.");
        assertThat(output.getOut()).contains("[SYSTEM]");
        assertThat(output.getOut()).contains("[NONE]");
    }

}

