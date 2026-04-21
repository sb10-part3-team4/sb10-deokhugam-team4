package com.codeit.team4.deokhugam.review.controller;

import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.review.service.ReviewQueryService;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import com.codeit.team4.deokhugam.review.controller.api.ReviewApi;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final ReviewQueryService reviewQueryService;

    @GetMapping
    public ResponseEntity<PageResponse<ReviewResponse>> searchReviews(
            @Valid @ParameterObject ReviewSearchRequestParam param,
            @Parameter(description = "요청자 ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        log.info("리뷰 목록 조회 요청: orderBy={}, direction={}, limit={}", param.orderBy(), param.direction(), param.limit());
        return ResponseEntity.ok(reviewQueryService.searchReviews(param));
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        log.info("리뷰 단건 조회 요청: reviewId={}, userId={}", reviewId, userId);
        ReviewResponse response = reviewService.getReview(reviewId, userId);

        return ResponseEntity.ok(response);
    }

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

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> softDeleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        log.info("리뷰 논리 삭제 요청: reviewId={}, userId={}", reviewId, userId);
        reviewService.softDeleteReview(reviewId, userId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{reviewId}/hard")
    public ResponseEntity<Void> hardDeleteReview(
            @PathVariable UUID reviewId,
            @RequestHeader("Deokhugam-Request-User-ID") UUID userId
    ) {
        log.info("리뷰 물리 삭제 요청: reviewId={}, userId={}", reviewId, userId);
        reviewService.hardDeleteReview(reviewId, userId);

        return ResponseEntity.noContent().build();
    }
}
