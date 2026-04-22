package com.codeit.team4.deokhugam.review.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewLikeResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import com.codeit.team4.deokhugam.review.entity.ReviewLike;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.mapper.ReviewMapper;
import com.codeit.team4.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserService userService;
    private final BookService bookService;
    private final ReviewMapper reviewMapper;

    public ReviewResponse getReview(UUID reviewId, UUID userId) {
        Review review = findById(reviewId);
        return reviewMapper.toResponse(review, isLikedByUser(reviewId, userId));
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request) {
        User user = userService.findById(request.userId());
        Book book = bookService.findById(request.bookId());

        validateDuplicateReview(book.getId(), user.getId());

        Review review = new Review(book, user, request.content(), request.rating());

        try {
            reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_REVIEW, "bookId=" + book.getId() + ", userId=" + user.getId());
        }
        log.info("리뷰 생성 완료: reviewId={}", review.getId());

        return reviewMapper.toResponse(review, false);
    }

    public Review findById(UUID reviewId) {
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REVIEW_NOT_FOUND, "reviewId=" + reviewId));
    }

    public Review findWithDeletedById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REVIEW_NOT_FOUND, "reviewId=" + reviewId));
    }

    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UUID userId, ReviewUpdateRequest request) {
        Review review = findById(reviewId);

        validateReviewOwner(review, userId);

        review.update(request.content(), request.rating());
        log.info("리뷰 수정 완료: reviewId={}", review.getId());

        return reviewMapper.toResponse(review, isLikedByUser(reviewId, userId));
    }

    @Transactional
    public void softDeleteReview(UUID reviewId, UUID userId) {
        Review review = findById(reviewId);

        validateReviewOwner(review, userId);
        review.softDelete();
        log.info("리뷰 논리 삭제 완료: reviewId={}", review.getId());
    }

    @Transactional
    public void hardDeleteReview(UUID reviewId, UUID userId) {
        Review review = findWithDeletedById(reviewId);

        validateReviewOwner(review, userId);
        reviewRepository.delete(review);
        log.info("리뷰 물리 삭제 완료: reviewId={}", review.getId());
    }

    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_REVIEW, "bookId=" + bookId + ", userId=" + userId);
        }
    }

    @Transactional
    public ReviewLikeResponse toggleLike(UUID reviewId, UUID userId) {
        Review review = findById(reviewId);

        if (reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId)) {
            unlikeReview(reviewId, userId);
            return new ReviewLikeResponse(reviewId, userId, false);
        }

        likeReview(review, userId);
        return new ReviewLikeResponse(reviewId, userId, true);
    }

    private void likeReview(Review review, UUID userId) {
        try {
            reviewLikeRepository.save(new ReviewLike(review, userService.findById(userId)));
            reviewRepository.increaseLikeCount(review.getId());
        } catch (DataIntegrityViolationException e) {
            log.debug("이미 좋아요 상태: reviewId={}, userId={}", review.getId(), userId);
            return;
        }
        log.info("리뷰 좋아요 추가: reviewId={}, userId={}", review.getId(), userId);
    }

    private void unlikeReview(UUID reviewId, UUID userId) {
        long deleted = reviewLikeRepository.deleteByReviewIdAndUserId(reviewId, userId);
        if (deleted == 0) {
            log.debug("이미 좋아요 취소 상태: reviewId={}, userId={}", reviewId, userId);
            return;
        }
        reviewRepository.decreaseLikeCount(reviewId);
        log.info("리뷰 좋아요 취소: reviewId={}, userId={}", reviewId, userId);
    }

    private boolean isLikedByUser(UUID reviewId, UUID userId) {
        return reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);
    }

    private static void validateReviewOwner(Review review, UUID userId) {
        if (!review.isOwner(userId)) {
            throw new BusinessException(
                    ErrorCode.REVIEW_NOT_OWNER, "reviewId=" + review.getId() + ", userId=" + userId);
        }
    }
}
