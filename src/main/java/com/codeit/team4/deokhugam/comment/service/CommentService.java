package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.mapper.CommentMapper;
import com.codeit.team4.deokhugam.comment.repository.CommentRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ReviewService reviewService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;


    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        User user = userService.findById(request.userId());
        Review review = reviewService.findById(request.reviewId());

        Comment comment = new Comment(user, review, request.content());
        Comment savedComment = commentRepository.save(comment);

        reviewRepository.increaseCommentCount(review.getId());

        log.info("댓글 생성 완료: commentId={}, userId={}, reviewId={}", savedComment.getId(),
                user.getId(), review.getId());

        return commentMapper.toResponse(savedComment);
    }

    @Transactional
    public CommentResponse updateComment(UUID commentId, UUID userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMENT_NOT_FOUND, "commentId: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS, "userId: " + userId);
        }

        comment.updateContent(request.content());

        log.info("댓글 수정 완료: commentId: {}, userId: {}", commentId, userId);
        return commentMapper.toResponse(comment);
    }

    @Transactional
    public void softDeleteComment(UUID commentId, UUID userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMENT_NOT_FOUND, "commentId: " + commentId));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS,
                    String.format("댓글 삭제 권한 없음 - commentId: %s, 요청자: %s", commentId, userId));
        }

        comment.softDelete();

        log.info("댓글 논리 삭제 완료: commentId={}, userId={}", commentId, userId);
    }



}
