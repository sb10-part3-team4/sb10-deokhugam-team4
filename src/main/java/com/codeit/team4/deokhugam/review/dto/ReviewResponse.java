package com.codeit.team4.deokhugam.review.dto;

import com.codeit.team4.deokhugam.review.model.ReviewSearchModel;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "리뷰 응답")
public record ReviewResponse(

        @Schema(description = "리뷰 ID")
        UUID id,

        @Schema(description = "도서 ID")
        UUID bookId,

        @Schema(description = "도서 제목")
        String bookTitle,

        @Schema(description = "도서 썸네일 URL")
        String bookThumbnailUrl,

        @Schema(description = "작성자 ID")
        UUID userId,

        @Schema(description = "작성자 닉네임")
        String userNickname,

        @Schema(description = "리뷰 내용")
        String content,

        @Schema(description = "평점 (1~5)")
        int rating,

        @Schema(description = "좋아요 수")
        int likeCount,

        @Schema(description = "댓글 수")
        int commentCount,

        @Schema(description = "현재 사용자의 좋아요 여부")
        boolean likedByMe,

        @Schema(description = "생성일시")
        Instant createdAt,

        @Schema(description = "수정일시")
        Instant updatedAt
) {

    public static ReviewResponse from(ReviewSearchModel model) {
        return new ReviewResponse(
                model.id(),
                model.bookId(),
                model.bookTitle(),
                model.bookThumbnailUrl(),
                model.userId(),
                model.userNickname(),
                model.content(),
                model.rating(),
                model.likeCount(),
                model.commentCount(),
                model.likedByMe(),
                model.createdAt(),
                model.updatedAt()
        );
    }
}
