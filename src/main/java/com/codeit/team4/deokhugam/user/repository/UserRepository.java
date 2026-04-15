package com.codeit.team4.deokhugam.user.repository;

import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

}
