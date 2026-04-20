package com.codeit.team4.deokhugam.review.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReviewMapperTest {

    private final ReviewMapper reviewMapper = new ReviewMapperImpl();

    @Test
    @DisplayName("리뷰 엔티티를 응답 DTO로 매핑 성공")
    void toResponse_success() {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Book book = mock(Book.class);
        User user = mock(User.class);

        given(book.getId()).willReturn(bookId);
        given(book.getTitle()).willReturn("클린 코드");
        given(book.getThumbnailUrl()).willReturn("https://example.com/image.jpg");
        given(user.getId()).willReturn(userId);
        given(user.getNickname()).willReturn("테스터");

        Review review = new Review(book, user, "좋은 책입니다", 5);

        ReviewResponse response = reviewMapper.toResponse(review, true);

        assertThat(response.bookId()).isEqualTo(bookId);
        assertThat(response.bookTitle()).isEqualTo("클린 코드");
        assertThat(response.bookThumbnailUrl()).isEqualTo("https://example.com/image.jpg");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.userNickname()).isEqualTo("테스터");
        assertThat(response.content()).isEqualTo("좋은 책입니다");
        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.likeCount()).isZero();
        assertThat(response.commentCount()).isZero();
        assertThat(response.likedByMe()).isTrue();
    }

    @Test
    @DisplayName("좋아요 안 한 리뷰 매핑 시 likedByMe false 반환 성공")
    void toResponse_notLiked_success() {
        Book book = mock(Book.class);
        User user = mock(User.class);
        Review review = new Review(book, user, "좋은 책입니다", 5);

        ReviewResponse response = reviewMapper.toResponse(review, false);

        assertThat(response.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("좋아요 한 리뷰 매핑 시 likedByMe true 반환 성공")
    void toResponse_liked_success() {
        Book book = mock(Book.class);
        User user = mock(User.class);
        Review review = new Review(book, user, "좋은 책입니다", 5);

        ReviewResponse response = reviewMapper.toResponse(review, true);

        assertThat(response.likedByMe()).isTrue();
    }
}
