package com.codeit.team4.deokhugam.dashboard.user;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PowerUserRepository extends JpaRepository<PowerUser, UUID> {

}
