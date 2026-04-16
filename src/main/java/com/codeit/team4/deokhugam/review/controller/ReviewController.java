package com.codeit.team4.deokhugam.review.controller;

import com.codeit.team4.deokhugam.review.controller.api.ReviewApi;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        ReviewResponse response = reviewService.createReview(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
