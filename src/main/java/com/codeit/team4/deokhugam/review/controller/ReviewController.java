package com.codeit.team4.deokhugam.review.controller;

import com.codeit.team4.deokhugam.review.controller.api.ReviewApi;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewApi {

    private final ReviewService reviewService;

}
