package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.mapper.CommentMapper;
import com.codeit.team4.deokhugam.comment.repository.CommentRepository;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.lock.DistributedLock;
import com.codeit.team4.deokhugam.notification.event.CommentEvent;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        User user = userService.findById(request.userId());
        Review review = reviewService.findById(request.reviewId());

        Comment comment = new Comment(user, review, request.content());
        Comment savedComment = commentRepository.save(comment);

        reviewRepository.increaseCommentCount(review.getId());

        eventPublisher.publishEvent(
                new CommentEvent(
                        review.getId(),
                        review.getUser().getId(),
                        user.getId()
                )
        );

        log.info("댓글 생성 완료: commentId={}, userId={}, reviewId={}", savedComment.getId(),
                user.getId(), review.getId());

        return commentMapper.toResponse(savedComment);
    }

    @Transactional
    @DistributedLock(key = "deokhugam:comment", lockParam = {"commentId"})
    public CommentResponse updateComment(UUID commentId, UUID userId,
            CommentUpdateRequest request) {
        Comment comment = findCommentById(commentId);

        validateCommentOwner(comment, userId, "수정");

        comment.updateContent(request.content());

        log.info("댓글 수정 완료: commentId={}, userId={}", commentId, userId);
        return commentMapper.toResponse(comment);
    }

    @Transactional
    @DistributedLock(key = "deokhugam:comment", lockParam = {"commentId"})
    public void softDeleteComment(UUID commentId, UUID userId) {
        Comment comment = findCommentById(commentId);
        validateCommentOwner(comment, userId, "논리 삭제");

        int updatedRows = commentRepository.softDeleteWithCondition(commentId, Instant.now());

        if (updatedRows == 1) {
            reviewRepository.decreaseCommentCount(comment.getReview().getId());
            log.info("댓글 논리 삭제 완료: commentId={}, userId={}", commentId, userId);
        } else {
            throw new BusinessException(
                    ErrorCode.COMMENT_NOT_FOUND,
                    String.format("이미 처리된 요청입니다: commentId=%s, userId=%s", commentId, userId));
        }
    }

    @Transactional
    @DistributedLock(key = "deokhugam:comment", lockParam = {"commentId"})
    public void hardDeleteComment(UUID commentId, UUID userId) {
        // 이미 논리 삭제된 것도 물리 삭제 가능해야 하므로 findById 사용
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMENT_NOT_FOUND, "commentId: " + commentId));

        validateCommentOwner(comment, userId, "물리 삭제");

        // 논리 삭제 여부에 따른 분기 처리
        if (comment.getDeletedAt() == null) {
            int updatedRows = commentRepository.hardDeleteWithCondition(commentId);
            if (updatedRows == 1) {
                reviewRepository.decreaseCommentCount(comment.getReview().getId());
            }
        } else {
            commentRepository.hardDeleteForce(commentId);
        }

        log.info("댓글 물리 삭제 완료: commentId={}, userId={}", commentId, userId);
    }

    // ========== Private Methods ==========

    private Comment findCommentById(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMENT_NOT_FOUND, "commentId: " + commentId));

        if (comment.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND,
                    "이미 삭제된 댓글입니다. commentId: " + commentId);
        }

        return comment;
    }

    private void validateCommentOwner(Comment comment, UUID userId, String action) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(
                    ErrorCode.COMMENT_NOT_OWNER,
                    String.format("댓글 %s 권한 없음 - commentId=%s, 요청자=%s", action, comment.getId(),
                            userId)
            );
        }
    }
}
