package com.codeit.team4.deokhugam.global.filter;

import com.codeit.team4.deokhugam.global.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.util.List;
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

    // yaml파일값 대신 테스트용 더미 헤더 리스트
    private static final List<String> TEST_IP_HEADERS = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    @Test
    @DisplayName("기본 접속 시 IP 및 식별자 MDC 저장 성공")
    void doFilterInternal_withRemoteAddr_Success() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] mdcRequestId = new String[1];
        final String[] mdcClientIp = new String[1];

        FilterChain filterChain = (req, res) -> {
            mdcRequestId[0] = MDC.get(LoggingFilter.REQUEST_ID_KEY);
            mdcClientIp[0] = MDC.get(LoggingFilter.CLIENT_IP_KEY);
        };

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        assertThat(mdcRequestId[0]).isNotNull();
        assertThat(UUID.fromString(mdcRequestId[0])).isNotNull();
        assertThat(mdcClientIp[0]).isEqualTo("192.168.0.1");

        assertThat(MDC.get(LoggingFilter.REQUEST_ID_KEY)).isNull();
        assertThat(MDC.get(LoggingFilter.CLIENT_IP_KEY)).isNull();
    }

    @Test
    @DisplayName("프록시 환경 다중 헤더 시 실제 IP 추출 성공")
    void doFilterInternal_withXForwardedFor_Success() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        request.setRemoteAddr("192.168.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] mdcClientIp = new String[1];
        FilterChain filterChain = (req, res) -> mdcClientIp[0] = MDC.get(LoggingFilter.CLIENT_IP_KEY);

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        assertThat(mdcClientIp[0]).isEqualTo("10.0.0.1");
    }

    @Test
    @DisplayName("무효한 프록시 헤더 무시 및 대체 IP 추출 성공")
    void doFilterInternal_ignoresUnknownHeader_Success() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 첫 번째 검사 대상이 unknown일 때 다음 헤더(Proxy-Client-IP)를 참조하는지 검증
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("Proxy-Client-IP", "172.16.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] mdcClientIp = new String[1];
        FilterChain filterChain = (req, res) -> mdcClientIp[0] = MDC.get(LoggingFilter.CLIENT_IP_KEY);

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        assertThat(mdcClientIp[0]).isEqualTo("172.16.0.1");
    }

    // ------- 실패/예외 케이스 -------

    @Test
    @DisplayName("필터 체인 예외 발생 실패")
    void doFilterInternal_clearsMDC_whenFilterChainThrows() {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain throwingChain = (req, res) -> {
            throw new ServletException("boom");
        };

        // when & then
        assertThatThrownBy(() -> loggingFilter.doFilter(request, response, throwingChain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("boom");

        // 예외가 발생했어도 2개의 Key 모두 안전하게 지워졌는지 검증 (메모리 누수 방지 실패 케이스 검증)
        assertThat(MDC.get(LoggingFilter.REQUEST_ID_KEY)).isNull();
        assertThat(MDC.get(LoggingFilter.CLIENT_IP_KEY)).isNull();
    }

    @Test
    @DisplayName("런타임 예외 발생 시 MDC 초기화 성공")
    void doFilterInternal_clearsMDC_whenRuntimeExceptionThrows() {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // ServletException이 아닌 컨트롤러 로직 중 발생할 수 있는 일반 런타임 예외 시뮬레이션
        FilterChain throwingChain = (req, res) -> {
            throw new IllegalArgumentException("컨트롤러 내부 로직 에러");
        };

        // when & then
        assertThatThrownBy(() -> loggingFilter.doFilter(request, response, throwingChain))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("컨트롤러 내부 로직 에러");

        // 런타임 예외가 밖으로 던져졌음에도 finally 블록이 정상 작동하여 MDC를 비웠는지 검증
        assertThat(MDC.get(LoggingFilter.REQUEST_ID_KEY)).isNull();
        assertThat(MDC.get(LoggingFilter.CLIENT_IP_KEY)).isNull();
    }

    @Test
    @DisplayName("비정상적인 다중 프록시 헤더 파싱 성공")
    void doFilterInternal_withMalformedProxyHeader_Success() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();

        // 쉼표만 있거나 공백이 포함된 악의적/비정상적인 포맷
        request.addHeader("X-Forwarded-For", "  , unknown, 172.16.0.1 ");
        request.setRemoteAddr("192.168.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] mdcClientIp = new String[1];
        FilterChain filterChain = (req, res) -> mdcClientIp[0] = MDC.get(LoggingFilter.CLIENT_IP_KEY);

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        // 빈 문자열("")이나 unknown을 무시하고 정확히 유효한 IP를 찾아내는지 검증
        assertThat(mdcClientIp[0]).isEqualTo("172.16.0.1");
    }

    @Test
    @DisplayName("모든 프록시 헤더 무효 시 remoteAddr 폴백 성공")
    void doFilterInternal_allHeadersInvalid_fallsBackToRemoteAddr() throws Exception {
        // given
        LoggingFilter loggingFilter = new LoggingFilter(new AppProperties(TEST_IP_HEADERS));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.addHeader("Proxy-Client-IP", "");
        request.setRemoteAddr("192.168.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] mdcClientIp = new String[1];
        FilterChain filterChain = (req, res) -> mdcClientIp[0] = MDC.get(LoggingFilter.CLIENT_IP_KEY);

        // when
        loggingFilter.doFilter(request, response, filterChain);

        // then
        assertThat(mdcClientIp[0]).isEqualTo("192.168.0.1");
    }
}