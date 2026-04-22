package com.codeit.team4.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.mapper.CommentMapper;
import com.codeit.team4.deokhugam.comment.repository.CommentRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserService userService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    // ===== createComment() =====
    @Test
    @DisplayName("댓글 등록 성공")
    void createComment_Success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "좋습니다");

        User mockUser = mock(User.class);
        Review mockReview = mock(Review.class);
        given(mockReview.getId()).willReturn(reviewId);
        CommentResponse mockResponse = new CommentResponse(
                UUID.randomUUID(),
                request.content(),
                userId,
                reviewId,
                "독후감러버",
                Instant.now(),
                Instant.now()
        );

        given(userService.findById(userId)).willReturn(mockUser);
        given(reviewService.findById(reviewId)).willReturn(mockReview);
        given(commentRepository.save(any(Comment.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(commentMapper.toResponse(any(Comment.class))).willReturn(mockResponse);

        // when
        CommentResponse response = commentService.createComment(request);

        // then
        assertThat(response).isEqualTo(mockResponse);
        then(reviewRepository).should(times(1)).increaseCommentCount(reviewId);

        // ArgumentCaptor로 mapper나 repository에 전달된 객체를 잡아서 확인
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        then(commentRepository).should().save(commentCaptor.capture());

        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getContent()).isEqualTo(request.content());
        assertThat(savedComment.getUser()).isEqualTo(mockUser);
        assertThat(savedComment.getReview()).isEqualTo(mockReview);

        then(userService).should().findById(userId);
        then(reviewService).should().findById(reviewId);
    }

    @Test
    @DisplayName("존재하지 않는 유저 - 댓글 등록 실패")
    void createComment_Fail_UserNotFound() {
        // given
        UUID invalidUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(reviewId, invalidUserId,
                "유익한 리뷰네요!");

        given(userService.findById(invalidUserId))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 - 댓글 등록 실패")
    void createComment_Fail_ReviewNotFound() {
        UUID userId = UUID.randomUUID();
        UUID invalidReviewId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(invalidReviewId, userId, "내용");

        given(userService.findById(userId)).willReturn(mock(User.class));
        given(reviewService.findById(invalidReviewId))
                .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        assertThatThrownBy(() -> commentService.createComment(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REVIEW_NOT_FOUND);
    }

    // ===== updateComment() =====
    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정된 내용입니다.");

        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(userId);

        Comment mockComment = mock(Comment.class);
        given(mockComment.getUser()).willReturn(mockUser);

        CommentResponse mockResponse = new CommentResponse(
                commentId, request.content(), userId, UUID.randomUUID(),
                "독후감러버", Instant.now(), Instant.now()
        );

        given(commentRepository.findById(commentId)).willReturn(java.util.Optional.of(mockComment));
        given(commentMapper.toResponse(mockComment)).willReturn(mockResponse);

        // when
        CommentResponse response = commentService.updateComment(commentId, userId, request);

        // then
        assertThat(response.content()).isEqualTo("수정된 내용입니다.");
        verify(mockComment).updateContent("수정된 내용입니다.");
    }

    @Test
    @DisplayName("작성자 불일치로 인한 댓글 수정 실패")
    void updateComment_Fail_Unauthorized() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID(); // 작성자와 다른 요청자 ID
        CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");

        User mockAuthor = mock(User.class);
        given(mockAuthor.getId()).willReturn(authorId);

        Comment mockComment = mock(Comment.class);
        given(mockComment.getUser()).willReturn(mockAuthor);

        given(commentRepository.findById(commentId)).willReturn(java.util.Optional.of(mockComment));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, requesterId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 댓글 수정은 실패")
    void updateComment_Fail_CommentNotFound() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정 내용");

        given(commentRepository.findById(commentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(commentId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("권한이 일치하는 경우 댓글 논리 삭제와 댓글 카운트 감소 성공")
    void softDeleteComment_Success() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(userId);

        Review mockReview = mock(Review.class);
        given(mockReview.getId()).willReturn(reviewId);

        Comment mockComment = mock(Comment.class);
        given(mockComment.getUser()).willReturn(mockUser);
        given(mockComment.getReview()).willReturn(mockReview);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

        // when
        commentService.softDeleteComment(commentId, userId);

        // then
        verify(mockComment).softDelete();
        verify(reviewRepository).decreaseCommentCount(reviewId);
    }

    @Test
    @DisplayName("작성자가 일치하지 않으면 댓글 논리 삭제 실패")
    void softDeleteComment_Fail_Unauthorized() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        User mockAuthor = mock(User.class);
        given(mockAuthor.getId()).willReturn(authorId);

        Comment mockComment = mock(Comment.class);
        given(mockComment.getUser()).willReturn(mockAuthor);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

        // when & then
        assertThatThrownBy(() -> commentService.softDeleteComment(commentId, requesterId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED_COMMENT_ACCESS);
    }

    @Test
    @DisplayName("이미 삭제된 댓글에 접근할 경우 실패")
    void softDeleteComment_Fail_AlreadyDeleted() {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Comment mockComment = mock(Comment.class);
        given(mockComment.getDeletedAt()).willReturn(Instant.now());

        given(commentRepository.findById(commentId)).willReturn(Optional.of(mockComment));

        // when & then
        assertThatThrownBy(() -> commentService.softDeleteComment(commentId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.COMMENT_NOT_FOUND);
    }
}