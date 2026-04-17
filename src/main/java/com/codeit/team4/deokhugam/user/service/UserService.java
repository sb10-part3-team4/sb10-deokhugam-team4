package com.codeit.team4.deokhugam.user.service;

import com.codeit.team4.deokhugam.user.dto.UserRegisterRequest;
import com.codeit.team4.deokhugam.user.dto.UserResponse;
import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;

public interface UserService {

    UserResponse registerUser(UserRegisterRequest request);

    User findById(UUID userId);
}
