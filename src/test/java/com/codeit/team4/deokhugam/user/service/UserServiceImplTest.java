package com.codeit.team4.deokhugam.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.dto.UserLoginRequest;
import com.codeit.team4.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.team4.deokhugam.user.dto.UserResponse;
import com.codeit.team4.deokhugam.user.dto.UserUpdateRequest;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.mapper.UserMapper;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("회원가입 성공")
    void registerUser_success() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@test.com",
                "user1",
                "password1!"
        );
        User user = new User("test@test.com", "user1", "password1!");
        User savedUser = new User("test@test.com", "user1", "password1!");

        when(userRepository.existsByEmailAndDeletedAtIsNull(request.email()))
                .thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(any(User.class)))
                .thenReturn(new UserResponse(null, "test@test.com", "user1", null));

        // when
        UserResponse result = userService.registerUser(request);

        // then
        assertThat(result)
                .extracting(UserResponse::email, UserResponse::nickname)
                .contains(request.email(), request.nickname());

        verify(userMapper).toEntity(request);
        verify(userRepository).save(user);
        verify(userMapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("이메일 중복으로 인한 회원가입 실패")
    void registerUser_fail_duplicate_email() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@test.com",
                "user1",
                "password1!"
        );

        when(userRepository.existsByEmailAndDeletedAtIsNull(request.email()))
                .thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("DB 무결성 위반으로 회원가입 실패")
    void registerUser_fail_dataIntegrityViolation() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@test.com",
                "user1",
                "password1!"
        );

        User user = mock(User.class);

        when(userRepository.existsByEmailAndDeletedAtIsNull(request.email()))
                .thenReturn(false);

        when(userMapper.toEntity(request)).thenReturn(user);

        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        // when & then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);

        verify(userMapper).toEntity(request);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("로그인 성공")
    void loginUser_success() {
        // given
        UserLoginRequest request = new UserLoginRequest("test@test.com", "password1!");
        User user = new User("test@test.com", "user1", "password1!");

        when(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(new UserResponse(user.getId(), user.getEmail(), user.getNickname(), null));

        // when
        UserResponse result = userService.loginUser(request);

        // then
        assertThat(result.email()).isEqualTo(request.email());

        verify(userRepository).findByEmailAndDeletedAtIsNull(request.email());
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 실패")
    void loginUser_fail_user_not_found() {
        // given
        UserLoginRequest request = new UserLoginRequest("test@test.com", "password1!");

        when(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("비밀번호 불일치로 로그인 실패")
    void loginUser_fail_invalid_password() {
        // given
        UserLoginRequest request = new UserLoginRequest("test@test.com", "wrongPassword");
        User user = new User("test@test.com", "user1", "password1!");

        when(userRepository.findByEmailAndDeletedAtIsNull(request.email()))
                .thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD);

        verify(userRepository).findByEmailAndDeletedAtIsNull(request.email());
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("유저 조회 성공")
    void findById_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = new User("test@test.com", "user1", "password1!");

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        // when
        User result = userService.findById(userId);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    @DisplayName("유저 조회 실패")
    void findById_fail() {
        // given
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("유저 닉네임 수정 성공")
    void updateUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("newNick");

        User user = new User("test@test.com", "oldNick", "password1!");

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(new UserResponse(userId, user.getEmail(), "newNick", null));

        // when
        UserResponse result = userService.updateUser(userId, request);

        // then
        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(result.nickname()).isEqualTo("newNick");

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
        verify(userMapper).toResponse(user);
    }

    @Test
    @DisplayName("유저가 존재하지 않아 수정 실패")
    void updateUser_fail_user_not_found() {
        // given
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("newNick");

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userMapper, never()).toResponse(any());
        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }

    @Test
    @DisplayName("유저 삭제 성공")
    void deleteUser_success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = new User("test@test.com", "user1", "password1!");

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.of(user));

        // when
        userService.deleteUser(userId);

        // then
        assertThat(user.isDeleted()).isTrue();

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }

    @Test
    @DisplayName("유저가 존재하지 않아 삭제 실패")
    void deleteUser_fail_user_not_found() {
        // given
        UUID userId = UUID.randomUUID();

        when(userRepository.findByIdAndDeletedAtIsNull(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(userRepository).findByIdAndDeletedAtIsNull(userId);
    }

    @Test
    @DisplayName("삭제 대상 사용자 물리 삭제 성공")
    void deleteExpiredUsers_success() {
        // given
        User user1 = new User("old1@test.com", "user1", "pw");
        User user2 = new User("old2@test.com", "user2", "pw");

        when(userRepository.findAllByDeletedAtBefore(any()))
                .thenReturn(List.of(user1, user2));

        // when
        userService.deleteExpiredUsers();

        // then
        verify(userRepository).deleteAll(List.of(user1, user2));
    }

    @Test
    @DisplayName("삭제 대상 사용자가 없어 빈 리스트 삭제")
    void deleteExpiredUsers_no_target() {
        // given
        when(userRepository.findAllByDeletedAtBefore(any()))
                .thenReturn(Collections.emptyList());

        // when
        userService.deleteExpiredUsers();

        // then
        verify(userRepository).deleteAll(Collections.emptyList());
    }
}