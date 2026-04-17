package com.codeit.team4.deokhugam.user.service;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.dto.UserLoginRequest;
import com.codeit.team4.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.team4.deokhugam.user.dto.UserResponse;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.mapper.UserMapper;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
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
    @Transactional(readOnly = true)
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

    // 헬퍼 메서드
    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validatePassword(User user, String password) {
        if (!user.getPassword().equals(password)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    private User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND, "email=" + email));
    }
}
