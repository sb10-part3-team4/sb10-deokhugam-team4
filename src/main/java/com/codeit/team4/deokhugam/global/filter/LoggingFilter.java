package com.codeit.team4.deokhugam.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "request_id";
    public static final String CLIENT_IP_KEY = "client_ip";

    private final List<String> ipHeaderCandidates;

    public LoggingFilter(@Value("${app.client-ip-headers}") List<String> ipHeaderCandidates) {
        this.ipHeaderCandidates = ipHeaderCandidates;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 고유 요청 ID 생성
        String requestId = UUID.randomUUID().toString();
        // 클라이언트 IP 추출
        String clientIp = extractClientIp(request);

        // Request Attribute 및 MDC에 저장
        request.setAttribute(REQUEST_ID_KEY, requestId);
        MDC.put(REQUEST_ID_KEY, requestId);
        MDC.put(CLIENT_IP_KEY, clientIp);

        try {
            // 다음 필터 또는 컨트롤러로 요청 전달
            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리가 완료되면 MDC 컨텍스트 초기화
            MDC.remove(REQUEST_ID_KEY);
            MDC.remove(CLIENT_IP_KEY);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        // 상수화된 프록시 헤더 목록을 순회하며 유효 IP 발견 시 파싱후 반환
        for (String header : ipHeaderCandidates) {
            String headerValue = request.getHeader(header);

            if (StringUtils.hasText(headerValue) && !"unknown".equalsIgnoreCase(headerValue)) {
                // 쉼표로 분리한 후, 빈 문자열이나 unknown이 아닌 '최초의 유효한 IP'를 탐색
                String[] parsedIps = headerValue.split(",");
                for (String parsedIp : parsedIps) {
                    parsedIp = parsedIp.trim();
                    if (StringUtils.hasText(parsedIp) && !"unknown".equalsIgnoreCase(parsedIp)) {
                        return parsedIp;
                    }
                }
            }
        }
        return request.getRemoteAddr();
    }
}
