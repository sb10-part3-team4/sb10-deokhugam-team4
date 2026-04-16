package com.codeit.team4.deokhugam.review.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.mapper.ReviewMapper;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import java.util.UUID;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
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
        reviewRepository.save(review);

        return reviewMapper.toResponse(review, false);
    }

    @Override
    public Review findById(UUID reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.REVIEW_NOT_FOUND, "reviewId=" + reviewId));
    }

    private void validateDuplicateReview(UUID bookId, UUID userId) {
        if (reviewRepository.existsByBookIdAndUserIdAndDeletedAtIsNull(bookId, userId)) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_REVIEW, "bookId=" + bookId + ", userId=" + userId);
        }
    }
}
