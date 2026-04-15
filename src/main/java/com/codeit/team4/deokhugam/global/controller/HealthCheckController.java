package com.codeit.team4.deokhugam.global.controller;

import com.codeit.team4.deokhugam.global.controller.api.HealthCheckApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthCheckController implements HealthCheckApi {

    @Override
    @GetMapping
    public ResponseEntity<String> checkHealth() {
        // LoggingFilter가 MDC에 넣어둔 request_id와 client_ip가 함께 출력되어야 함
        log.info("헬스 체크 요청이 성공적으로 수신되었습니다.");
        return ResponseEntity.ok("OK");
    }
}