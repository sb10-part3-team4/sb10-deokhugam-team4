package com.codeit.team4.deokhugam.notification.repository;

import com.codeit.team4.deokhugam.notification.entity.Notification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
