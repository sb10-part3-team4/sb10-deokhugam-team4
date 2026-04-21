package com.codeit.team4.deokhugam.global.resolver;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.codeit.team4.deokhugam.global.dto.DeokhugamUser;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

class LoginUserArgumentResolverTest {

    private final UserService userService = mock(UserService.class);
    private final LoginUserArgumentResolver resolver = new LoginUserArgumentResolver(userService);

    private final MethodParameter parameter = mock(MethodParameter.class);
    private final ModelAndViewContainer mavContainer = mock(ModelAndViewContainer.class);
    private final WebDataBinderFactory binderFactory = mock(WebDataBinderFactory.class);

    private static final String HEADER_NAME = "Deokhugam-Request-User-ID";

    @Test
    @DisplayName("헤더가 없으면 UNAUTHORIZED 예외 발생")
    void resolveArgument_noHeader_throwUnauthorized() {
        // given
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn(null);

        // when
        BusinessException ex = assertThrows(BusinessException.class, () ->
                resolver.resolveArgument(parameter, mavContainer, request, binderFactory)
        );

        // then
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    @Test
    @DisplayName("UUID 파싱 실패 시 INVALID_INPUT 예외 발생")
    void resolveArgument_invalidUUID_throwInvalidInput() {
        // given
        NativeWebRequest request = mock(NativeWebRequest.class);
        when(request.getHeader(HEADER_NAME)).thenReturn("invalid-uuid");

        // when
        BusinessException ex = assertThrows(BusinessException.class, () ->
                resolver.resolveArgument(parameter, mavContainer, request, binderFactory)
        );

        // then
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
    }

    @Test
    @DisplayName("정상적인 userId면 DeokhugamUser 반환")
    void resolveArgument_success() {
        // given
        NativeWebRequest request = mock(NativeWebRequest.class);
        UUID userId = UUID.randomUUID();

        when(request.getHeader(HEADER_NAME)).thenReturn(userId.toString());

        User user = mock(User.class);
        when(userService.findById(userId)).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // when
        Object result = resolver.resolveArgument(parameter, mavContainer, request, binderFactory);

        // then
        assertInstanceOf(DeokhugamUser.class, result);
        DeokhugamUser dto = (DeokhugamUser) result;
        assertEquals(userId, dto.userId());

        verify(userService).findById(userId);
    }
}