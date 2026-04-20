package com.codeit.team4.deokhugam.review.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.mapper.ReviewMapper;
import com.codeit.team4.deokhugam.review.repository.ReviewLikeRepository;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import java.util.UUID;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserService userService;
    private final BookService bookService;
    private final ReviewMapper reviewMapper;

    @Override
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

    @Override
    public Review findById(UUID reviewId) {
        return reviewRepository.findByIdAndDeletedAtIsNull(reviewId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REVIEW_NOT_FOUND, "reviewId=" + reviewId));
    }

    @Override
    public Review findWithDeletedById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REVIEW_NOT_FOUND, "reviewId=" + reviewId));
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UUID userId, ReviewUpdateRequest request) {
        User user = userService.findById(userId);
        Review review = findById(reviewId);

        validateReviewOwner(review, user);

        review.update(request.content(), request.rating());
        log.info("리뷰 수정 완료: reviewId={}", review.getId());

        boolean likedByMe = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);
        return reviewMapper.toResponse(review, likedByMe);
    }

    @Override
    @Transactional
    public void softDeleteReview(UUID reviewId, UUID userId) {
        User user = userService.findById(userId);
        Review review = findById(reviewId);

        validateReviewOwner(review, user);
        review.softDelete();
        log.info("리뷰 논리 삭제 완료: reviewId={}", review.getId());
    }

    @Override
    @Transactional
    public void hardDeleteReview(UUID reviewId, UUID userId) {
        User user = userService.findById(userId);
        Review review = findWithDeletedById(reviewId);

        validateReviewOwner(review, user);
        reviewRepository.delete(review);
        log.info("리뷰 물리 삭제 완료: reviewId={}", review.getId());
    }

    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_REVIEW, "bookId=" + bookId + ", userId=" + userId);
        }
    }

    private static void validateReviewOwner(Review review, User user) {
        if (!review.isOwner(user)) {
            throw new BusinessException(
                    ErrorCode.REVIEW_NOT_OWNER, "reviewId=" + review.getId() + ", userId=" + user.getId());
        }
    }
}
