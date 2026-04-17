package com.codeit.team4.deokhugam.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.review.entity.Review;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentService 단위 테스트")
class CommentServiceTest {

    @Mock private CommentRepository commentRepository;
    @Mock private UserService userService;
    @Mock private ReviewService reviewService;

    // 💡 변경점: Service 구현체에 추가된 Mapper를 모킹
    @Mock private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    @DisplayName("댓글 등록 성공")
    void createComment_Success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest("좋습니다");

        User mockUser = new User("test@test.com", "닉네임", "pwd");
        Review mockReview = mock(Review.class);
        Comment mockComment = new Comment(mockUser, mockReview, request.content());
        CommentResponse mockResponse = new CommentResponse(UUID.randomUUID(), request.content(), userId, reviewId);

        given(userService.findById(userId)).willReturn(mockUser);
        given(reviewService.findById(reviewId)).willReturn(mockReview);
        given(commentRepository.save(any(Comment.class))).willReturn(mockComment);

        // 💡 변경점: Mapper의 변환 동작을 가정
        given(commentMapper.toResponse(any(Comment.class))).willReturn(mockResponse);

        // when
        CommentResponse response = commentService.createComment(userId, reviewId, request);

        // then
        assertThat(response.content()).isEqualTo("좋습니다");
        verify(userService).findById(userId);
        verify(reviewService).findById(reviewId);
        verify(commentRepository).save(any(Comment.class));
        verify(commentMapper).toResponse(any(Comment.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저 - 댓글 등록 실패")
    void createComment_Fail_UserNotFound() {
        // given
        UUID invalidUserId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest("유익한 리뷰네요!");

        given(userService.findById(invalidUserId))
                .willThrow(new BusinessException(ErrorCode.USER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> commentService.createComment(invalidUserId, reviewId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }
}