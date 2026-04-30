package com.codeit.team4.deokhugam.comment.service.query;

import static com.codeit.team4.deokhugam.jooq.tables.Comments.COMMENTS;
import static com.codeit.team4.deokhugam.jooq.tables.Users.USERS;

import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentSearchRequestParam;
import com.codeit.team4.deokhugam.comment.mapper.CommentMapper;
import com.codeit.team4.deokhugam.comment.model.CommentModel;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentQueryService {

    private final DSLContext dsl;
    private final CommentMapper commentMapper;

    // 댓글 단건 조회
    public CommentResponse getComment(UUID commentId) {
        CommentModel model = dsl
                .select(CommentModel.toSelectedFields())
                .from(COMMENTS)
                .join(USERS).on(COMMENTS.USER_ID.eq(USERS.ID))
                .where(COMMENTS.ID.eq(commentId)
                        .and(COMMENTS.DELETED_AT.isNull()))
                .fetchOne(CommentModel::fromRecord);

        if (model == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND, "commentId=" + commentId);
        }

        log.info("댓글 단건 조회 완료: commentId={}", commentId);
        return commentMapper.toResponse(model);
    }

    // 댓글 목록 조회
    public PageResponse<CommentResponse> getComments(CommentSearchRequestParam param) {
        validateCursorParam(param);

        boolean isAsc = SortDirection.ASC == param.direction();

        Condition condition = COMMENTS.REVIEW_ID.eq(param.reviewId())
                .and(COMMENTS.DELETED_AT.isNull())
                .and(buildCursorCondition(param, isAsc));

        SortField<?> sortField = isAsc
                ? COMMENTS.CREATED_AT.asc() : COMMENTS.CREATED_AT.desc();
        SortField<?> tieBreaker = isAsc
                ? COMMENTS.ID.asc() : COMMENTS.ID.desc();

        List<CommentModel> results = dsl
                .select(CommentModel.toSelectedFields())
                .from(COMMENTS)
                .join(USERS).on(COMMENTS.USER_ID.eq(USERS.ID))
                .where(condition)
                .orderBy(sortField, tieBreaker)
                .limit(param.limit() + 1)   // hasNext 판단을 위해 +1 조회
                .fetch(CommentModel::fromRecord);

        boolean hasNext = results.size() > param.limit();
        List<CommentModel> content = hasNext
                ? results.subList(0, param.limit())
                : results;

        // 다음 페이지 커서 추출
        String nextCursor = null;
        Instant nextAfter = null;
        if (hasNext && !content.isEmpty()) {
            CommentModel last = content.get(content.size() - 1);
            nextCursor = last.id().toString();
            nextAfter = last.createdAt();
        }

        List<CommentResponse> responses = content.stream()
                .map(commentMapper::toResponse)
                .toList();

        log.info("댓글 목록 조회 완료: reviewId={}, size={}", param.reviewId(), responses.size());

        return new PageResponse<>(responses, nextCursor, nextAfter, param.limit(), null, hasNext);
    }

    // ===== private =====

    // cursor와 after는 반드시 함께 제공되거나 함께 null
    private void validateCursorParam(CommentSearchRequestParam param) {
        if ((param.cursor() == null) != (param.after() == null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "cursor와 after는 함께 제공되어야 합니다. cursor=" + param.cursor() + ", after="
                            + param.after()
            );
        }
    }

    private Condition buildCursorCondition(CommentSearchRequestParam param, boolean isAsc) {
        if (param.after() == null || param.cursor() == null) {
            return DSL.noCondition();
        }
        OffsetDateTime afterOffset = param.after().atOffset(ZoneOffset.UTC);
        UUID cursorId;
        try {
            cursorId = UUID.fromString(param.cursor());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "cursor는 UUID 형식이어야 합니다. cursor=" + param.cursor()
            );
        }

        return isAsc
                ? DSL.row(COMMENTS.CREATED_AT, COMMENTS.ID).gt(afterOffset, cursorId)
                : DSL.row(COMMENTS.CREATED_AT, COMMENTS.ID).lt(afterOffset, cursorId);
    }
}
