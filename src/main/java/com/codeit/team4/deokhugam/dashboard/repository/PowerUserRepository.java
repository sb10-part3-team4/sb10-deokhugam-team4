package com.codeit.team4.deokhugam.dashboard.repository;

import com.codeit.team4.deokhugam.dashboard.entity.PowerUser;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

}
