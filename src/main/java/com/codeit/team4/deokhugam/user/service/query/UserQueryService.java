package com.codeit.team4.deokhugam.user.service.query;

import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import java.time.Instant;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryService {

    private final DSLContext dsl;

    public int deleteExpiredUsers(Instant threshold) {
        return dsl.deleteFrom(USERS)
                .where(USERS.DELETED_AT.lt(threshold.atOffset(ZoneOffset.UTC)))
                .execute();
    }
}
