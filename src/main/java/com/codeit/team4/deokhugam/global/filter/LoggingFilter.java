package com.codeit.team4.deokhugam.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "REQUEST_ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 고유 요청 ID 생성
        String requestId = UUID.randomUUID().toString();

        // Request Attribute 및 MDC에 저장
        request.setAttribute(REQUEST_ID_KEY, requestId);
        MDC.put(REQUEST_ID_KEY, requestId);

        try {
            // 다음 필터 또는 컨트롤러로 요청 전달
            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리가 완료되면 MDC 컨텍스트 초기화
            MDC.remove(REQUEST_ID_KEY);
        }
    }
}
