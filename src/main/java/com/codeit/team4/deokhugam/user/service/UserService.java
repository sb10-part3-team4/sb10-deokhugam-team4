package com.codeit.team4.deokhugam.user.service;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.lock.DistributedLock;
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
public class UserService {

    private final UserRepository userRepository;
    private final UserQueryService userQueryService;
    private final UserMapper userMapper;

    public UserResponse getUser(UUID userId) {

        User user = findById(userId);

        return userMapper.toResponse(user);
    }

    @Transactional
    @DistributedLock(key = "deokhugam:user:email", lockParam = {"request.email"})
    public UserResponse registerUser(UserRegisterRequest request) {

        validateEmailNotExists(request.email());

        try {
            User user = new User(request.email(), request.nickname(), request.password());
            User savedUser = userRepository.save(user);

            log.info("회원가입 성공: userId={}", savedUser.getId());

            return userMapper.toResponse(savedUser);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_EMAIL, "email=" + request.email()
            );

        } catch (Exception e) {
            log.error("회원가입 처리 중 예상치 못한 예외 발생: email={}", request.email(), e);
            throw new BusinessException(
                    ErrorCode.INTERNAL_SERVER_ERROR, "email=" + request.email()
            );
        }
    }

    public UserResponse loginUser(UserLoginRequest request) {

        User user = findActiveUserByEmail(request.email());
        validatePassword(user, request.password());

        log.info("로그인 성공: userId={}", user.getId());

        return userMapper.toResponse(user);
    }

    public User findById(UUID userId) {
        return userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND, "userId=" + userId));
    }

    public List<User> findAllByIds(List<UUID> userIds) {
        return userRepository.findAllById(userIds);
    }

    @Transactional
    @DistributedLock(key = "deokhugam:user", lockParam = {"userId"})
    public UserResponse updateUser(UUID userId, UUID loginUserId, UserUpdateRequest request) {

        User user = findById(userId);
        validateOwner(userId, loginUserId);

        user.updateNickname(request.nickname());

        log.info("유저 수정 성공: userId={}", userId);

        return userMapper.toResponse(user);
    }

    @Transactional
    @DistributedLock(key = "deokhugam:user", lockParam = {"userId"})
    public void softDeleteUser(UUID userId, UUID loginUserId) {

        User user = findById(userId);
        validateOwner(userId, loginUserId);

        user.softDelete();

        log.info("유저 삭제 완료: userId={}, loginUserId={}", userId, loginUserId);
    }

    @Transactional
    @DistributedLock(key = "deokhugam:user", lockParam = {"userId"})
    public void hardDeleteUser(UUID userId, UUID loginUserId) {

        User user = findById(userId);
        validateOwner(userId, loginUserId);

        userRepository.delete(user);

        log.info("유저 단건 물리 삭제 완료: userId={}, loginUserId={}", userId, loginUserId);
    }

    @Transactional
    public void deleteExpiredSoftDeletedUsers() {

        Instant threshold = Instant.now().minus(1, ChronoUnit.DAYS);

        int deletedCount = userQueryService.deleteExpiredUsers(threshold);

        log.info("유저 물리 삭제 완료: count={}, threshold={}", deletedCount, threshold);
    }

    // 헬퍼 메서드
    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL, "email=" + email);
        }
    }

    private void validatePassword(User user, String password) {
        if (!Objects.equals(user.getPassword(), password)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD, "userId=" + user.getId());
        }
    }

    private void validateOwner(UUID targetUserId, UUID loginUserId) {
        if (!Objects.equals(targetUserId, loginUserId)) {
            throw new BusinessException(
                    ErrorCode.USER_FORBIDDEN,
                    "targetUserId=" + targetUserId + ", loginUserId=" + loginUserId
            );
        }
    }

    private User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.USER_NOT_FOUND, "email=" + email));
    }
}
