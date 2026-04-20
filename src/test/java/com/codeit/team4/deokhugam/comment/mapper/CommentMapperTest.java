package com.codeit.team4.deokhugam.comment.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Nested
    @DisplayName("toResponse 매핑 테스트")
    class ToResponse {

        @Test
        @DisplayName("댓글에서 응답 DTO로 전체 필드 매핑 성공")
        void toResponse_Success() {
            // given
            UUID userId = UUID.randomUUID();
            UUID reviewId = UUID.randomUUID();

            User mockUser = mock(User.class);
            given(mockUser.getId()).willReturn(userId);

            Review mockReview = mock(Review.class);
            given(mockReview.getId()).willReturn(reviewId);

            Comment comment = new Comment(mockUser, mockReview, "정말 좋은 리뷰입니다");

            // when
            CommentResponse response = commentMapper.toResponse(comment);

            // then
            assertThat(response).isNotNull();
            assertThat(response.content()).isEqualTo("정말 좋은 리뷰입니다");
            assertThat(response.userId()).isEqualTo(userId);
            assertThat(response.reviewId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("내용이 다른 두 댓글을 독립적으로 매핑 성공")
        void toResponse_DifferentContent_Success() {
            // given
            User mockUser = mock(User.class);
            Review mockReview = mock(Review.class);

            Comment comment1 = new Comment(mockUser, mockReview, "내용 A");
            Comment comment2 = new Comment(mockUser, mockReview, "내용 B");

            // when
            CommentResponse response1 = commentMapper.toResponse(comment1);
            CommentResponse response2 = commentMapper.toResponse(comment2);

            // then
            assertThat(response1.content()).isEqualTo("내용 A");
            assertThat(response2.content()).isEqualTo("내용 B");
        }

        @Test
        @DisplayName("다른 작성자의 ID가 정확하게 매핑 성공")
        void toResponse_DifferentUser_Success() {
            // given
            UUID userId1 = UUID.randomUUID();
            UUID userId2 = UUID.randomUUID();

            User mockUser1 = mock(User.class);
            given(mockUser1.getId()).willReturn(userId1);

            User mockUser2 = mock(User.class);
            given(mockUser2.getId()).willReturn(userId2);

            Review mockReview = mock(Review.class);

            Comment comment1 = new Comment(mockUser1, mockReview, "내용");
            Comment comment2 = new Comment(mockUser2, mockReview, "내용");

            // when & then
            assertThat(commentMapper.toResponse(comment1).userId()).isEqualTo(userId1);
            assertThat(commentMapper.toResponse(comment2).userId()).isEqualTo(userId2);
        }

        @Test
        @DisplayName("다른 리뷰의 ID가 정확하게 매핑 성공")
        void toResponse_DifferentReview_Success() {
            // given
            UUID reviewId1 = UUID.randomUUID();
            UUID reviewId2 = UUID.randomUUID();

            User mockUser = mock(User.class);

            Review mockReview1 = mock(Review.class);
            given(mockReview1.getId()).willReturn(reviewId1);

            Review mockReview2 = mock(Review.class);
            given(mockReview2.getId()).willReturn(reviewId2);

            Comment comment1 = new Comment(mockUser, mockReview1, "내용");
            Comment comment2 = new Comment(mockUser, mockReview2, "내용");

            // when & then
            assertThat(commentMapper.toResponse(comment1).reviewId()).isEqualTo(reviewId1);
            assertThat(commentMapper.toResponse(comment2).reviewId()).isEqualTo(reviewId2);
        }

        @Test
        @DisplayName("최대 길이(1000자) 내용의 댓글 매핑 성공")
        void toResponse_MaxLengthContent_Success() {
            // given
            String maxContent = "가".repeat(1000);
            User mockUser = mock(User.class);
            Review mockReview = mock(Review.class);
            Comment comment = new Comment(mockUser, mockReview, maxContent);

            // when
            CommentResponse response = commentMapper.toResponse(comment);

            // then
            assertThat(response.content()).hasSize(1000).isEqualTo(maxContent);
        }

        @Test
        @DisplayName("입력 엔티티가 null일 경우 null을 반환 성공")
        void toResponse_NullInput_Success() {
            // given
            Comment nullComment = null;

            // when
            CommentResponse response = commentMapper.toResponse(nullComment);

            // then
            assertThat(response).isNull();
        }

        @Test
        @DisplayName("연관된 User가 null일 경우 매핑 시 NPE 없이 userId가 null로 반환 성공")
        void toResponse_NullUser_Success() {
            // given
            Review mockReview = mock(Review.class);
            UUID reviewId = UUID.randomUUID();
            given(mockReview.getId()).willReturn(reviewId);

            Comment comment = new Comment(null, mockReview, "유저가 없는 댓글");

            // when
            CommentResponse response = commentMapper.toResponse(comment);

            // then
            assertThat(response).isNotNull();
            assertThat(response.userId()).isNull();
            assertThat(response.reviewId()).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("연관된 Review가 null일 경우 매핑 시 NPE 없이 reviewId가 null로 반환 성공")
        void toResponse_NullReview_Success() {
            // given
            User mockUser = mock(User.class);
            UUID userId = UUID.randomUUID();
            given(mockUser.getId()).willReturn(userId);

            Comment comment = new Comment(mockUser, null, "리뷰가 없는 댓글");

            // when
            CommentResponse response = commentMapper.toResponse(comment);

            // then
            assertThat(response).isNotNull();
            assertThat(response.reviewId()).isNull();
            assertThat(response.userId()).isEqualTo(userId);
        }
    }
}