package com.codeit.team4.deokhugam.user.service;

import com.codeit.team4.deokhugam.user.dto.UserLoginRequest;
import com.codeit.team4.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.team4.deokhugam.user.dto.UserResponse;
import com.codeit.team4.deokhugam.user.dto.UserUpdateRequest;
import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;

public interface UserService {

    UserResponse getUser(UUID userId);

    UserResponse registerUser(UserRegisterRequest request);

    UserResponse loginUser(UserLoginRequest request);

    User findById(UUID userId);

    UserResponse updateUser(UUID userId, UUID loginUserId, UserUpdateRequest request);

    void softDeleteUser(UUID userId, UUID loginUserId);

    void hardDeleteUser(UUID userId, UUID loginUserId);

    void deleteExpiredSoftDeletedUsers();
}
