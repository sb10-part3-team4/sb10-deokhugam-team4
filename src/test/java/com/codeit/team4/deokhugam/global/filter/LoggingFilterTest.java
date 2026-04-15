package com.codeit.team4.deokhugam.global.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoggingFilter 단위 테스트")
class LoggingFilterTest {

    private static final String REQUEST_ID_KEY = "REQUEST_ID";

    @Test
    @DisplayName("필터를 거치면 Request attribute에 고유한 REQUEST_ID가 저장되고, 동작 후 MDC가 초기화되어야 한다.")
    void doFilterInternal_generatesRequestId_and_clearsMDC() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        // Request Attribute에 고유 요청 ID가 잘 담겼는지 검증
        String requestId = (String) request.getAttribute(REQUEST_ID_KEY);
        assertThat(requestId).isNotNull();

        // UUID 형식으로 정상 생성되었는지 검증 (예외가 발생하지 않으면 통과)
        assertThat(UUID.fromString(requestId)).isNotNull();

        // FilterChain 종료 후 finally 블록을 통해 MDC가 정상적으로 비워졌는지 검증
        assertThat(MDC.get(REQUEST_ID_KEY)).isNull();
    }
}