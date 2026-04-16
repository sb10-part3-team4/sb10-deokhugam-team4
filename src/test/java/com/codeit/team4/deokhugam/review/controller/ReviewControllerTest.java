package com.codeit.team4.deokhugam.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import org.springframework.context.annotation.Import;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@Import(AppProperties.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 생성 성공")
    void createReview_success() throws Exception {
        UUID bookId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ReviewResponse response = new ReviewResponse(
                UUID.randomUUID(), bookId, "클린 코드", null,
                userId, "테스터", "좋은 책입니다", 5, 0, 0, false,
                Instant.now(), Instant.now());

        given(reviewService.createReview(any(ReviewCreateRequest.class))).willReturn(response);

        Map<String, Object> request = Map.of(
                "bookId", bookId.toString(),
                "userId", userId.toString(),
                "content", "좋은 책입니다",
                "rating", 5
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("좋은 책입니다"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.likedByMe").value(false));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 6})
    @DisplayName("평점이 범위를 벗어나면 리뷰 생성 실패")
    void createReview_invalidRating_fail(int rating) throws Exception {
        Map<String, Object> request = Map.of(
                "bookId", UUID.randomUUID().toString(),
                "userId", UUID.randomUUID().toString(),
                "content", "좋은 책입니다",
                "rating", rating
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("rating")));
    }

    @Test
    @DisplayName("내용이 빈 값이면 리뷰 생성 실패")
    void createReview_blankContent_fail() throws Exception {
        Map<String, Object> request = Map.of(
                "bookId", UUID.randomUUID().toString(),
                "userId", UUID.randomUUID().toString(),
                "content", "",
                "rating", 3
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("content")));
    }

    @Test
    @DisplayName("중복 리뷰 생성 실패")
    void createReview_duplicate_fail() throws Exception {
        given(reviewService.createReview(any(ReviewCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.DUPLICATE_REVIEW));

        Map<String, Object> request = Map.of(
                "bookId", UUID.randomUUID().toString(),
                "userId", UUID.randomUUID().toString(),
                "content", "좋은 책입니다",
                "rating", 5
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_REVIEW"));
    }

    @ParameterizedTest
    @EnumSource(value = ErrorCode.class, names = {"USER_NOT_FOUND", "BOOK_NOT_FOUND"})
    @DisplayName("존재하지 않는 사용자 또는 도서로 리뷰 생성 실패")
    void createReview_notFound_fail(ErrorCode errorCode) throws Exception {
        given(reviewService.createReview(any(ReviewCreateRequest.class)))
                .willThrow(new BusinessException(errorCode));

        Map<String, Object> request = Map.of(
                "bookId", UUID.randomUUID().toString(),
                "userId", UUID.randomUUID().toString(),
                "content", "좋은 책입니다",
                "rating", 5
        );

        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(errorCode.name()));
    }
}
