package com.codeit.team4.deokhugam.notification.repository;

import com.codeit.team4.deokhugam.notification.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
    List<Notification> findByUserIdAndConfirmedFalse(UUID userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.confirmed = TRUE AND n.createdAt < :threshold")
    int deleteOldReadNotifications(@Param("threshold") Instant threshold);
}
