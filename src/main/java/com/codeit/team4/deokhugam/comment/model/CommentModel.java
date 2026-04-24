package com.codeit.team4.deokhugam.comment.model;

import static com.codeit.team4.deokhugam.jooq.tables.Comments.COMMENTS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.jooq.Field;
import org.jooq.Record;

public record CommentModel(
        UUID id,
        UUID reviewId,
        UUID userId,
        String userNickname,
        String content,
        Instant createdAt,
        Instant updatedAt
) {
    public static List<Field<?>> toSelectedFields() {
        return List.of(
                COMMENTS.ID,
                COMMENTS.REVIEW_ID,
                COMMENTS.USER_ID,
                USERS.NICKNAME,
                COMMENTS.CONTENT,
                COMMENTS.CREATED_AT,
                COMMENTS.UPDATED_AT
        );
    }

    public static CommentModel fromRecord(Record rec) {
        return new CommentModel(
                rec.get(COMMENTS.ID),
                rec.get(COMMENTS.REVIEW_ID),
                rec.get(COMMENTS.USER_ID),
                rec.get(USERS.NICKNAME),
                rec.get(COMMENTS.CONTENT),
                rec.get(COMMENTS.CREATED_AT).toInstant(),
                rec.get(COMMENTS.UPDATED_AT).toInstant()
        );
    }
}