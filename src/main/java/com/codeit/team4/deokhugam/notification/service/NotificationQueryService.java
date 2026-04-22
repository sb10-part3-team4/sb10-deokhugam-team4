package com.codeit.team4.deokhugam.notification.service;

import com.codeit.team4.deokhugam.notification.model.NotificationModel;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import static com.codeit.team4.deokhugam.jooq.tables.Notifications.NOTIFICATIONS;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private final DSLContext dsl;

    public List<NotificationModel> findNotifications(
            UUID userId,
            Instant cursor,
            int size
    ) {
        var condition = NOTIFICATIONS.USER_ID.eq(userId);

        if (cursor != null) {
            condition = condition.and(
                    NOTIFICATIONS.CREATED_AT.lt(cursor.atOffset(ZoneOffset.UTC))
            );
        }

        List<NotificationModel> result = dsl.selectFrom(NOTIFICATIONS)
                .where(condition)
                .orderBy(NOTIFICATIONS.CREATED_AT.desc())
                .limit(size + 1)
                .fetch(record -> new NotificationModel(
                        record.get(NOTIFICATIONS.ID),
                        record.get(NOTIFICATIONS.USER_ID),
                        record.get(NOTIFICATIONS.REVIEW_ID),
                        record.get(NOTIFICATIONS.REVIEW_CONTENT),
                        record.get(NOTIFICATIONS.MESSAGE),
                        record.get(NOTIFICATIONS.CONFIRMED),
                        record.get(NOTIFICATIONS.CREATED_AT).toInstant(),
                        record.get(NOTIFICATIONS.UPDATED_AT).toInstant()
                ));

        if (result.size() > size) {
            return result.subList(0, size);
        }

        return result;
    }
}