package com.codeit.team4.deokhugam.review.service;

import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.entity.Review;
import java.util.UUID;

public interface ReviewService {

    ReviewResponse createReview(ReviewCreateRequest request);

    Review findById(UUID reviewId);
}
