package com.codeit.team4.deokhugam.review.dto;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;
import static com.codeit.team4.deokhugam.jooq.tables.Reviews.REVIEWS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jooq.Condition;
import org.jooq.SortField;
import org.jooq.impl.DSL;

@Schema(description = "리뷰 검색 요청")
public record ReviewSearchRequestParam(

        @Schema(description = "작성자 ID (완전 일치)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "도서 ID (완전 일치)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID bookId,

        @Schema(description = "검색 키워드 (작성자 닉네임 | 내용 | 도서제목)", example = "홍길동")
        String keyword,

        @Schema(description = "정렬 기준 (createdAt | rating)", example = "createdAt", defaultValue = "createdAt")
        ReviewOrderBy orderBy,

        @Schema(description = "정렬 방향 (ASC | DESC)", example = "DESC", defaultValue = "DESC")
        SortDirection direction,

        @Schema(description = "커서 페이지네이션 커서")
        String cursor,

        @Schema(description = "보조 커서 (createdAt)")
        Instant after,

        @Schema(description = "페이지 크기", example = "50", defaultValue = "50")
        @Min(1)
        @Max(100)
        int limit,

        @Schema(description = "요청자 ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        UUID requestUserId
) {

    public Condition toCondition() {
        Condition condition = REVIEWS.DELETED_AT.isNull();

        if (keyword != null && !keyword.isBlank()) {
            condition = condition.and(
                    USERS.NICKNAME.containsIgnoreCase(keyword)
                            .or(REVIEWS.CONTENT.containsIgnoreCase(keyword))
                            .or(BOOKS.TITLE.containsIgnoreCase(keyword))
            );
        }
        if (userId != null) {
            condition = condition.and(REVIEWS.USER_ID.eq(userId));
        }
        if (bookId != null) {
            condition = condition.and(REVIEWS.BOOK_ID.eq(bookId));
        }
        if (cursor != null && after != null) {
            condition = condition.and(toCursorCondition());
        }

        return condition;
    }

    public List<SortField<?>> toOrderBy() {
        boolean isAsc = SortDirection.ASC == direction;
        List<SortField<?>> orderBy = new ArrayList<>();

        if (ReviewOrderBy.rating == this.orderBy) {
            orderBy.add(isAsc ? REVIEWS.RATING.asc() : REVIEWS.RATING.desc());
        }
        orderBy.add(isAsc ? REVIEWS.CREATED_AT.asc() : REVIEWS.CREATED_AT.desc());
        orderBy.add(isAsc ? REVIEWS.ID.asc() : REVIEWS.ID.desc());

        return orderBy;
    }

    private Condition toCursorCondition() {
        boolean isAsc = SortDirection.ASC == direction;
        OffsetDateTime afterTime = after.atOffset(ZoneOffset.UTC);

        if (ReviewOrderBy.rating == orderBy) {
            int cursorValue;
            try {
                cursorValue = Integer.parseInt(cursor);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "rating 정렬에서 cursor는 정수여야 합니다");
            }
            return isAsc
                    ? DSL.row(REVIEWS.RATING, REVIEWS.CREATED_AT).gt(cursorValue, afterTime)
                    : DSL.row(REVIEWS.RATING, REVIEWS.CREATED_AT).lt(cursorValue, afterTime);
        } else {
            return isAsc
                    ? REVIEWS.CREATED_AT.gt(afterTime)
                    : REVIEWS.CREATED_AT.lt(afterTime);
        }
    }
}
