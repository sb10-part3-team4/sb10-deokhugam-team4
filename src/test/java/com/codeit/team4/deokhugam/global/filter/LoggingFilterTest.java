package com.codeit.team4.deokhugam.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LoggingFilter 단위 테스트")
class LoggingFilterTest {

    @Test
    @DisplayName("요청 필터링 성공")
    void doFilterInternal_Success() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        String requestId = (String) request.getAttribute(LoggingFilter.REQUEST_ID_KEY);
        assertThat(requestId).isNotNull();
        assertThat(UUID.fromString(requestId)).isNotNull();

        assertThat(MDC.get(LoggingFilter.REQUEST_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("필터 체인 예외 발생 실패")
    void doFilterInternal_clearsMDC_whenFilterChainThrows() {
        // given
        LoggingFilter loggingFilter = new LoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain throwingChain = (req, res) -> {
            throw new ServletException("boom");
        };

        // when & then
        // 필터 체인에서 발생한 예외가 정상적으로 밖으로 던져지는지 검증
        assertThatThrownBy(() -> loggingFilter.doFilter(request, response, throwingChain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("boom");

        // 예외가 발생했음에도 finally 블록에 의해 MDC가 안전하게 초기화되었는지 검증
        assertThat(MDC.get(LoggingFilter.REQUEST_ID_KEY)).isNull();
    }
}