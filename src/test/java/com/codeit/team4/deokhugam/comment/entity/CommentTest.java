package com.codeit.team4.deokhugam.comment.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("Comment 엔티티 단위 테스트")
class CommentTest {

    @Nested
    @DisplayName("생성자 테스트")
    class Constructor {

        @Test
        @DisplayName("유효한 인자로 댓글 생성 성공")
        void createComment_Success() {
            // given
            User user = mock(User.class);
            Review review = mock(Review.class);
            String content = "좋은 리뷰입니다";

            // when
            Comment comment = new Comment(user, review, content);

            // then
            assertThat(comment.getUser()).isEqualTo(user);
            assertThat(comment.getReview()).isEqualTo(review);
            assertThat(comment.getContent()).isEqualTo(content);
            assertThat(comment.getDeletedAt()).isNull();
        }

        @Test
        @DisplayName("공백 content를 가진 댓글 생성 성공")
        void createComment_BlankContent_Success() {
            User user = mock(User.class);
            Review review = mock(Review.class);

            Comment comment = new Comment(user, review, "   ");

            assertThat(comment.getContent()).isEqualTo("   ");
        }

        @Test
        @DisplayName("최대 길이(1000자) content를 가진 댓글 생성 성공")
        void createComment_MaxLengthContent_Success() {
            User user = mock(User.class);
            Review review = mock(Review.class);
            String maxContent = "가".repeat(1000);

            Comment comment = new Comment(user, review, maxContent);

            assertThat(comment.getContent()).hasSize(1000);
        }
    }


    @Nested
    @DisplayName("equals 및 hashCode 테스트")
    class EqualsAndHashCode {

        @Test
        @DisplayName("동일한 객체 참조일 경우 동등성 비교 성공")
        void equals_SameReference_Success() {
            Comment comment = new Comment(mock(User.class), mock(Review.class), "content");

            assertThat(comment).isEqualTo(comment);
        }

        @Test
        @DisplayName("동일한 식별자(ID)를 가진 영속화된 엔티티 동등성 비교 성공")
        void equals_SameId_Success() {
            UUID commentId = UUID.randomUUID();

            Comment comment1 = new Comment(mock(User.class), mock(Review.class), "내용 A");
            ReflectionTestUtils.setField(comment1, "id", commentId);

            Comment comment2 = new Comment(mock(User.class), mock(Review.class), "내용 B");
            ReflectionTestUtils.setField(comment2, "id", commentId);

            // ID가 같으면 내용이 달라도 동일한 객체로 취급
            assertThat(comment1).isEqualTo(comment2);
        }

        @Test
        @DisplayName("다른 식별자(ID)를 가진 영속화된 엔티티 동등성 비교 실패")
        void equals_DifferentId_Fail() {
            Comment comment1 = new Comment(mock(User.class), mock(Review.class), "동일한 내용");
            ReflectionTestUtils.setField(comment1, "id", UUID.randomUUID());

            Comment comment2 = new Comment(mock(User.class), mock(Review.class), "동일한 내용");
            ReflectionTestUtils.setField(comment2, "id", UUID.randomUUID());

            // 내용이 같아도 ID가 다르면 다른 객체로 취급
            assertThat(comment1).isNotEqualTo(comment2);
        }

        @Test
        @DisplayName("식별자(ID)가 null인 영속화 전 엔티티 동등성 비교 실패")
        void equals_NullId_Fail() {
            Comment comment1 = new Comment(mock(User.class), mock(Review.class), "내용");
            Comment comment2 = new Comment(mock(User.class), mock(Review.class), "내용");

            // ID가 null인 객체(저장 전)는 설령 내용이 같더라도 다른 객체로 취급해야 함
            assertThat(comment1).isNotEqualTo(comment2);
        }

        @Test
        @DisplayName("다른 타입 또는 null과의 동등성 비교 실패")
        void equals_DifferentTypeOrNull_Fail() {
            Comment comment = new Comment(mock(User.class), mock(Review.class), "내용");
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());

            //noinspection AssertBetweenInconvertibleTypes
            assertThat(comment).isNotEqualTo("문자열 타입");
            assertThat(comment).isNotEqualTo(null);
        }

        @Test
        @DisplayName("동일한 식별자를 가진 엔티티는 동일한 해시코드 반환에 성공")
        void hashCode_SameId_Success() {
            // given
            UUID commentId = UUID.randomUUID();

            Comment comment1 = new Comment(mock(User.class), mock(Review.class), "내용 A");
            ReflectionTestUtils.setField(comment1, "id", commentId);

            Comment comment2 = new Comment(mock(User.class), mock(Review.class), "내용 B");
            ReflectionTestUtils.setField(comment2, "id", commentId);

            // when & then
            assertThat(comment1.hashCode()).isEqualTo(comment2.hashCode());
        }

        @Test
        @DisplayName("다른 식별자를 가진 엔티티는 다른 해시코드 반환에 성공")
        void hashCode_DifferentId_Success() {
            // given
            Comment comment1 = new Comment(mock(User.class), mock(Review.class), "내용");
            ReflectionTestUtils.setField(comment1, "id", UUID.randomUUID());

            Comment comment2 = new Comment(mock(User.class), mock(Review.class), "내용");
            ReflectionTestUtils.setField(comment2, "id", UUID.randomUUID());

            // when & then
            // 내용이 같아도 ID가 다르면 해시코드가 달라야 함
            assertThat(comment1.hashCode()).isNotEqualTo(comment2.hashCode());
        }
    }

    @Nested
    @DisplayName("deletedAt 상태 검증")
    class DeletedAt {

        @Test
        @DisplayName("새로 생성한 댓글의 deletedAt 초기화 확인 성공")
        void checkDeletedAt_NewComment_Success() {
            Comment comment = new Comment(mock(User.class), mock(Review.class), "내용");

            assertThat(comment.getDeletedAt()).isNull();
        }
    }
}