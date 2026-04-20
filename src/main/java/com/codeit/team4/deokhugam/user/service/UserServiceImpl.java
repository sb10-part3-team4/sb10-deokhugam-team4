package com.codeit.team4.deokhugam.user.service;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.dto.UserLoginRequest;
import com.codeit.team4.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.team4.deokhugam.user.dto.UserResponse;
import com.codeit.team4.deokhugam.user.dto.UserUpdateRequest;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.mapper.UserMapper;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {

        validateEmailNotExists(request.email());

        try {
            User user = userMapper.toEntity(request);
            User savedUser = userRepository.save(user);

            log.info("회원가입 성공: userId={}", savedUser.getId());

            return userMapper.toResponse(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_EMAIL,
                    "email=" + request.email());
        }
    }

    @Override
    public UserResponse loginUser(UserLoginRequest request) {

        User user = findActiveUserByEmail(request.email());
        validatePassword(user, request.password());

        log.info("로그인 성공: userId={}", user.getId());

        return userMapper.toResponse(user);
    }

    @Override
    public User findById(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND, "userId=" + userId));
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {

        User user = findById(userId);

        user.updateNickname(request.nickname());

        log.info("유저 수정 성공: userId={}", userId);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {

        User user = findById(userId);

        user.softDelete();

        log.info("유저 삭제 완료: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteExpiredUsers() {

        Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);

        List<User> users = userRepository.findAllByDeletedAtBefore(threshold);

        userRepository.deleteAll(users);

        log.info("유저 물리 삭제 완료: count={}", users.size());
    }

    // 헬퍼 메서드
    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validatePassword(User user, String password) {
        if (!Objects.equals(user.getPassword(), password)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    private User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND, "email=" + email));
    }
}
