package com.codeit.team4.deokhugam.user.service;

import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;

public interface UserService {

    User findById(UUID userId);
}
