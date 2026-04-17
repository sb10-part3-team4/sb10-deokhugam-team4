package com.codeit.team4.deokhugam.review.controller;

import com.codeit.team4.deokhugam.review.controller.api.ReviewApi;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        log.info("리뷰 생성 요청: bookId={}, userId={}", request.bookId(), request.userId());
        ReviewResponse response = reviewService.createReview(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        log.info("리뷰 수정 요청: reviewId={}, userId={}", reviewId, userId);
        ReviewResponse response = reviewService.updateReview(reviewId, userId, request);

        return ResponseEntity.ok(response);
    }
}
