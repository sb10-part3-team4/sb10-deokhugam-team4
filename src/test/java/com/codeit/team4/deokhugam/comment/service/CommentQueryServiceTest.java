package com.codeit.team4.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentSearchRequestParam;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.repository.CommentRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.entity.ReviewStatistics;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.review.repository.ReviewStatisticsRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
@Transactional
class CommentQueryServiceTest {

    @Autowired
    private CommentQueryService commentQueryService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewStatisticsRepository reviewStatisticsRepository;

    private User user;
    private Review review;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("tester@codeit.com", "테스트유저", "password123"));
        Book book = bookRepository.saveAndFlush(
                new Book("테스트 도서", "작가", "설명", "출판사", LocalDate.now(), "ISBN-123", null));
        review = reviewRepository.saveAndFlush(new Review(book, user, "리뷰 내용", 5));
    }

    @Nested
    @DisplayName("댓글 단건 조회")
    class GetComment {

        @Test
        @DisplayName("존재하는 식별자로 댓글 상세 조회 성공")
        void getComment_Success() {
            // given
            Comment savedComment = commentRepository.saveAndFlush(
                    new Comment(user, review, "상세 조회용 댓글"));

            // when
            CommentResponse response = commentQueryService.getComment(savedComment.getId());

            // then
            assertThat(response.id()).isEqualTo(savedComment.getId());
            assertThat(response.content()).isEqualTo("상세 조회용 댓글");
            assertThat(response.userNickname()).isEqualTo("테스트유저");
        }

        @Test
        @DisplayName("존재하지 않는 식별자로 상세 조회 시 실패")
        void getComment_Fail_NotFound() {
            // given
            UUID randomId = UUID.randomUUID();

            // when & then
            assertThatThrownBy(() -> commentQueryService.getComment(randomId))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
        }

        @Test
        @DisplayName("논리 삭제된 댓글은 상세 조회 실패")
        void getComment_Fail_AlreadyDeleted() {
            // given
            Comment comment = commentRepository.saveAndFlush(new Comment(user, review, "삭제될 댓글"));
            comment.softDelete();
            commentRepository.saveAndFlush(comment);

            // when & then
            assertThatThrownBy(() -> commentQueryService.getComment(comment.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회 및 커서 페이지네이션")
    class GetCommentsPagination {

        @Test
        @DisplayName("댓글 논리 삭제 시 목록에서도 제외 성공")
        void getComments_Success_ExcludeSoftDeleted() {
            // given
            Comment comment1 = commentRepository.saveAndFlush(new Comment(user, review, "댓글 1"));
            Comment comment2 = commentRepository.saveAndFlush(new Comment(user, review, "댓글 2"));
            Comment comment3 = commentRepository.saveAndFlush(new Comment(user, review, "댓글 3"));

            comment2.softDelete();
            commentRepository.saveAndFlush(comment2);

            CommentSearchRequestParam param = new CommentSearchRequestParam(
                    review.getId(), SortDirection.DESC, null, null, 10
            );

            // when
            PageResponse<CommentResponse> result = commentQueryService.getComments(param);

            // then
            assertThat(result.content()).hasSize(2);

            List<UUID> resultIds = result.content().stream()
                    .map(CommentResponse::id)
                    .toList();

            assertThat(resultIds)
                    .contains(comment1.getId(), comment3.getId())
                    .doesNotContain(comment2.getId());
        }

        @Test
        @DisplayName("리뷰 ID 기준 첫 페이지 조회 성공")
        void getComments_FirstPage_Success() {
            // given: 댓글 3개 생성, 사이즈 2로 조회
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 1"));
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 2"));
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 3"));
            ReviewStatistics stats = new ReviewStatistics(review.getId());
            stats.onCommentCreated();
            stats.onCommentCreated();
            stats.onCommentCreated();
            reviewStatisticsRepository.saveAndFlush(stats);

            CommentSearchRequestParam param = new CommentSearchRequestParam(
                    review.getId(), SortDirection.DESC, null, null, 2
            );

            // when
            PageResponse<CommentResponse> result = commentQueryService.getComments(param);

            // then
            assertThat(result.content()).hasSize(2);
            assertThat(result.hasNext()).isTrue();
            assertThat(result.nextCursor()).isNotNull();
            assertThat(result.nextAfter()).isNotNull();
            assertThat(result.totalElements()).isEqualTo(3L);
        }

        @Test
        @DisplayName("커서를 이용한 다음 페이지 연계 조회 성공")
        void getComments_CursorPagination_Success() {
            // given
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 1"));
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 2"));
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 3"));
            ReviewStatistics stats = new ReviewStatistics(review.getId());
            stats.onCommentCreated();
            stats.onCommentCreated();
            stats.onCommentCreated();
            reviewStatisticsRepository.saveAndFlush(stats);

            CommentSearchRequestParam firstReq = new CommentSearchRequestParam(
                    review.getId(), SortDirection.DESC, null, null, 2
            );
            PageResponse<CommentResponse> firstRes = commentQueryService.getComments(firstReq);

            // when
            CommentSearchRequestParam secondReq = new CommentSearchRequestParam(
                    review.getId(),
                    SortDirection.DESC,
                    firstRes.nextCursor(),
                    firstRes.nextAfter(),
                    2
            );
            PageResponse<CommentResponse> secondRes = commentQueryService.getComments(secondReq);

            // then
            assertThat(secondRes.content()).hasSize(1);
            assertThat(secondRes.content().get(0).content()).isEqualTo("댓글 1");
            assertThat(secondRes.hasNext()).isFalse();
            assertThat(secondRes.totalElements()).isEqualTo(3L);
        }

        @Test
        @DisplayName("오름차순 정렬 및 페이지네이션 성공")
        void getComments_Ascending_Success() {
            // given
            commentRepository.saveAndFlush(new Comment(user, review, "가장 오래된 댓글"));
            commentRepository.saveAndFlush(new Comment(user, review, "최신 댓글"));

            CommentSearchRequestParam param = new CommentSearchRequestParam(
                    review.getId(), SortDirection.ASC, null, null, 10
            );

            // when
            PageResponse<CommentResponse> result = commentQueryService.getComments(param);

            // then
            assertThat(result.content().get(0).content()).isEqualTo("가장 오래된 댓글");
        }

        @Test
        @DisplayName("cursor와 after가 모두 null일 때 첫 페이지 조회 성공")
        void getComments_Success_FirstPage_BothCursorParamsNull() {
            // given: 초기 진입(첫 페이지) 상태
            commentRepository.saveAndFlush(new Comment(user, review, "댓글 1"));

            CommentSearchRequestParam param = new CommentSearchRequestParam(
                    review.getId(), SortDirection.DESC, null, null, 10
            );

            // when
            PageResponse<CommentResponse> result = commentQueryService.getComments(param);

            // then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).content()).isEqualTo("댓글 1");
        }

        @Test
        @DisplayName("커서 파라미터 불일치 시 예외 발생 실패")
        void getComments_Fail_InvalidCursorParams() {
            // given
            CommentSearchRequestParam param = new CommentSearchRequestParam(
                    review.getId(), SortDirection.DESC, UUID.randomUUID().toString(), null, 10
            );

            // when & then
            assertThatThrownBy(() -> commentQueryService.getComments(param))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INPUT);
        }
    }
}
