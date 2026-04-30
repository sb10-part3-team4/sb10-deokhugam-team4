package com.codeit.team4.deokhugam.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewCreateRequest;
import com.codeit.team4.deokhugam.review.dto.ReviewLikeResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.dto.ReviewSearchRequestParam;
import com.codeit.team4.deokhugam.review.dto.ReviewUpdateRequest;
import com.codeit.team4.deokhugam.review.service.query.ReviewQueryService;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@Import(AppProperties.class)
@ActiveProfiles("test")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private ReviewQueryService reviewQueryService;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("리뷰 목록 조회")
    class SearchReviews {

        private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

        @Test
        @DisplayName("리뷰 목록 조회 성공")
        void searchReviews_success() throws Exception {
            UUID requestUserId = UUID.randomUUID();
            PageResponse<ReviewResponse> response = new PageResponse<>(
                    List.of(), null, null, 50, null, false
            );

            given(reviewQueryService.searchReviews(any(ReviewSearchRequestParam.class)))
                    .willReturn(response);

            mockMvc.perform(get("/api/reviews")
                            .header(USER_ID_HEADER, requestUserId.toString())
                            .param("orderBy", "createdAt")
                            .param("direction", "DESC")
                            .param("limit", "50")
                            .param("requestUserId", requestUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.hasNext").value(false));
        }

        @Test
        @DisplayName("잘못된 정렬 기준이면 목록 조회 실패")
        void searchReviews_invalidOrderBy_fail() throws Exception {
            UUID requestUserId = UUID.randomUUID();
            mockMvc.perform(get("/api/reviews")
                            .header(USER_ID_HEADER, requestUserId.toString())
                            .param("orderBy", "invalid")
                            .param("direction", "DESC")
                            .param("limit", "50")
                            .param("requestUserId", requestUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 정렬 방향이면 목록 조회 실패")
        void searchReviews_invalidDirection_fail() throws Exception {
            UUID requestUserId = UUID.randomUUID();
            mockMvc.perform(get("/api/reviews")
                            .header(USER_ID_HEADER, requestUserId.toString())
                            .param("orderBy", "createdAt")
                            .param("direction", "INVALID")
                            .param("limit", "50")
                            .param("requestUserId", requestUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("헤더 요청자 ID 누락 시 목록 조회 실패")
        void searchReviews_missingHeader_fail() throws Exception {
            mockMvc.perform(get("/api/reviews")
                            .param("orderBy", "createdAt")
                            .param("direction", "DESC")
                            .param("limit", "50")
                            .param("requestUserId", UUID.randomUUID().toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("커서 포함 다음 페이지 조회 성공")
        void searchReviews_withCursor_success() throws Exception {
            UUID requestUserId = UUID.randomUUID();
            Instant nextAfter = Instant.parse("2025-04-06T15:04:05Z");
            ReviewResponse review = new ReviewResponse(
                    UUID.randomUUID(), UUID.randomUUID(), "클린 코드", null,
                    requestUserId, "테스터", "좋은 책", 5, 0, 0, false,
                    Instant.now(), Instant.now()
            );
            PageResponse<ReviewResponse> response = new PageResponse<>(
                    List.of(review), nextAfter.toString(), nextAfter, 1, null, true
            );

            given(reviewQueryService.searchReviews(any(ReviewSearchRequestParam.class)))
                    .willReturn(response);

            mockMvc.perform(get("/api/reviews")
                            .header(USER_ID_HEADER, requestUserId.toString())
                            .param("orderBy", "createdAt")
                            .param("direction", "DESC")
                            .param("limit", "1")
                            .param("cursor", "2025-04-06T15:04:05Z")
                            .param("after", "2025-04-06T15:04:05Z")
                            .param("requestUserId", requestUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isNotEmpty())
                    .andExpect(jsonPath("$.nextCursor").isNotEmpty())
                    .andExpect(jsonPath("$.nextAfter").isNotEmpty())
                    .andExpect(jsonPath("$.hasNext").value(true));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, 101})
        @DisplayName("limit 경계값이면 목록 조회 실패")
        void searchReviews_invalidLimit_fail(int limit) throws Exception {
            UUID requestUserId = UUID.randomUUID();
            mockMvc.perform(get("/api/reviews")
                            .header(USER_ID_HEADER, requestUserId.toString())
                            .param("orderBy", "createdAt")
                            .param("direction", "DESC")
                            .param("limit", String.valueOf(limit))
                            .param("requestUserId", requestUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("keyword 파라미터가 서비스에 올바르게 전달 성공")
        void searchReviews_withKeyword_success() throws Exception {
            UUID requestUserId = UUID.randomUUID();
            PageResponse<ReviewResponse> response = new PageResponse<>(
                    List.of(), null, null, 50, null, false
            );

            given(reviewQueryService.searchReviews(any(ReviewSearchRequestParam.class)))
                    .willReturn(response);

            mockMvc.perform(get("/api/reviews")
                            .header(USER_ID_HEADER, requestUserId.toString())
                            .param("orderBy", "createdAt")
                            .param("direction", "DESC")
                            .param("limit", "50")
                            .param("keyword", "홍길동")
                            .param("requestUserId", requestUserId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("리뷰 단건 조회")
    class GetReview {

        private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

        @Test
        @DisplayName("리뷰 단건 조회 성공")
        void getReview_success() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewResponse response = new ReviewResponse(
                    reviewId,
                    UUID.randomUUID(),
                    "클린 코드",
                    null,
                    userId,
                    "테스터",
                    "좋은 책입니다",
                    5,
                    0,
                    0,
                    true,
                    Instant.now(),
                    Instant.now()
            );

            given(reviewService.getReview(reviewId, userId)).willReturn(response);

            mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("좋은 책입니다"))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.likedByMe").value(true));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 단건 조회 실패")
        void getReview_notFound_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewService.getReview(reviewId, userId))
                    .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

            mockMvc.perform(get("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"));
        }

        @Test
        @DisplayName("요청자 ID 누락 시 리뷰 단건 조회 실패")
        void getReview_missingUserId_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();

            mockMvc.perform(get("/api/reviews/{reviewId}", reviewId))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("리뷰 생성")
    class CreateReview {

        @Test
        @DisplayName("리뷰 생성 성공")
        void createReview_success() throws Exception {
            UUID bookId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewResponse response = new ReviewResponse(
                    UUID.randomUUID(),
                    bookId,
                    "클린 코드",
                    null,
                    userId,
                    "테스터",
                    "좋은 책입니다",
                    5,
                    0,
                    0,
                    false,
                    Instant.now(),
                    Instant.now()
            );

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
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("rating")));
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
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("content")));
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

    @Nested
    @DisplayName("리뷰 수정")
    class UpdateReview {

        private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

        @Test
        @DisplayName("리뷰 수정 성공")
        void updateReview_success() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewResponse response = new ReviewResponse(
                    reviewId,
                    UUID.randomUUID(),
                    "클린 코드",
                    null,
                    userId,
                    "테스터",
                    "수정된 내용",
                    3,
                    0,
                    0,
                    true,
                    Instant.now(),
                    Instant.now()
            );

            given(reviewService.updateReview(eq(reviewId), eq(userId), any(ReviewUpdateRequest.class)))
                    .willReturn(response);

            Map<String, Object> request = Map.of(
                    "content", "수정된 내용",
                    "rating", 3
            );

            mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("수정된 내용"))
                    .andExpect(jsonPath("$.rating").value(3))
                    .andExpect(jsonPath("$.likedByMe").value(true));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 6})
        @DisplayName("평점이 범위를 벗어나면 리뷰 수정 실패")
        void updateReview_invalidRating_fail(int rating) throws Exception {
            Map<String, Object> request = Map.of(
                    "content", "수정된 내용",
                    "rating", rating
            );

            mockMvc.perform(patch("/api/reviews/{reviewId}", UUID.randomUUID())
                            .header(USER_ID_HEADER, UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("rating")));
        }

        @Test
        @DisplayName("내용이 빈 값이면 리뷰 수정 실패")
        void updateReview_blankContent_fail() throws Exception {
            Map<String, Object> request = Map.of(
                    "content", "",
                    "rating", 3
            );

            mockMvc.perform(patch("/api/reviews/{reviewId}", UUID.randomUUID())
                            .header(USER_ID_HEADER, UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                    .andExpect(jsonPath("$.message").value(Matchers.containsString("content")));
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 수정 실패")
        void updateReview_notOwner_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewService.updateReview(eq(reviewId), eq(userId), any(ReviewUpdateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_OWNER));

            Map<String, Object> request = Map.of(
                    "content", "수정된 내용",
                    "rating", 3
            );

            mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_OWNER"));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 수정 실패")
        void updateReview_notFound_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewService.updateReview(eq(reviewId), eq(userId), any(ReviewUpdateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

            Map<String, Object> request = Map.of(
                    "content", "수정된 내용",
                    "rating", 3
            );

            mockMvc.perform(patch("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class DeleteReview {

        private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

        @Test
        @DisplayName("리뷰 삭제 성공")
        void softDeleteReview_success() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doNothing().when(reviewService).softDeleteReview(reviewId, userId);

            mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 삭제 실패")
        void softDeleteReview_notOwner_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new BusinessException(ErrorCode.REVIEW_NOT_OWNER))
                    .when(reviewService).softDeleteReview(reviewId, userId);

            mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_OWNER"));
        }

        @Test
        @DisplayName("리뷰 물리 삭제 성공")
        void hardDeleteReview_success() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doNothing().when(reviewService).hardDeleteReview(reviewId, userId);

            mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("본인의 리뷰가 아니면 물리 삭제 실패")
        void hardDeleteReview_notOwner_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new BusinessException(ErrorCode.REVIEW_NOT_OWNER))
                    .when(reviewService).hardDeleteReview(reviewId, userId);

            mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_OWNER"));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 물리 삭제 실패")
        void hardDeleteReview_notFound_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND))
                    .when(reviewService).hardDeleteReview(reviewId, userId);

            mockMvc.perform(delete("/api/reviews/{reviewId}/hard", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 삭제 실패")
        void softDeleteReview_notFound_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            doThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND))
                    .when(reviewService).softDeleteReview(reviewId, userId);

            mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("리뷰 좋아요 토글")
    class ToggleLike {

        private static final String USER_ID_HEADER = "Deokhugam-Request-User-ID";

        @Test
        @DisplayName("좋아요 추가 성공")
        void toggleLike_like_success() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewLikeResponse response = new ReviewLikeResponse(reviewId, userId, true);

            given(reviewService.toggleLike(reviewId, userId)).willReturn(response);

            mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.liked").value(true));
        }

        @Test
        @DisplayName("좋아요 취소 성공")
        void toggleLike_unlike_success() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            ReviewLikeResponse response = new ReviewLikeResponse(reviewId, userId, false);

            given(reviewService.toggleLike(reviewId, userId)).willReturn(response);

            mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.liked").value(false));
        }

        @Test
        @DisplayName("존재하지 않는 리뷰 좋아요 토글 실패")
        void toggleLike_reviewNotFound_fail() throws Exception {
            UUID reviewId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(reviewService.toggleLike(reviewId, userId))
                    .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

            mockMvc.perform(post("/api/reviews/{reviewId}/like", reviewId)
                            .header(USER_ID_HEADER, userId.toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"));
        }

        @Test
        @DisplayName("요청자 ID 누락 시 좋아요 토글 실패")
        void toggleLike_missingHeader_fail() throws Exception {
            mockMvc.perform(post("/api/reviews/{reviewId}/like", UUID.randomUUID()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
}
