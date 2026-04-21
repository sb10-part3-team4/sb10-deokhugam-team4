package com.codeit.team4.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    private CommentServiceImpl commentService;

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
        CommentResponse mockResponse = new CommentResponse(UUID.randomUUID(), request.content(),
                userId, reviewId);

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
}
