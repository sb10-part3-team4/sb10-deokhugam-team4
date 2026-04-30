package com.codeit.team4.deokhugam.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentSearchRequestParam;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.comment.service.query.CommentQueryService;
import com.codeit.team4.deokhugam.comment.service.CommentService;
import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@Import(AppProperties.class)
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CommentQueryService commentQueryService;

    @Test
    @DisplayName("댓글 등록 성공")
    void createComment_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String content = "좋은 리뷰입니다";
        CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, content);

        UUID newCommentId = UUID.randomUUID();
        CommentResponse response = new CommentResponse(
                newCommentId,
                content,
                userId,
                reviewId,
                "테스트닉네임",
                Instant.now(),
                Instant.now()
        );

        given(commentService.createComment(any(CommentCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(newCommentId.toString()))
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()));
    }

    @Test
    @DisplayName("잘못된 요청(입력값 검증 실패)으로 인한 댓글 등록 실패")
    void createComment_Fail_400_InvalidInput() throws Exception {
        // given
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        CommentCreateRequest invalidRequest = new CommentCreateRequest(reviewId, userId, "");

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("리뷰 정보가 없어 댓글 등록 실패")
    void createComment_Fail_404_ReviewNotFound() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "좋은 리뷰네요!");

        given(commentService.createComment(any(CommentCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_FOUND"));
    }

    @Test
    @DisplayName("서버 내부 오류로 인한 댓글 등록 실패")
    void createComment_Fail_500_InternalServerError() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "좋은 리뷰네요!");

        // DB 장애 등 예상치 못한 런타임 에러가 터지는 상황 가정
        given(commentService.createComment(any(CommentCreateRequest.class)))
                .willThrow(new RuntimeException("DB Connection Timeout"));

        // when & then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("댓글 수정 API 성공")
    void updateComment_Success() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        String updatedContent = "수정된 내용입니다";
        CommentUpdateRequest request = new CommentUpdateRequest(updatedContent);

        given(userService.findById(userId)).willReturn(mock(User.class));

        CommentResponse response = new CommentResponse(
                commentId, updatedContent, userId, reviewId,
                "테스트닉네임", Instant.now(), Instant.now()
        );

        given(commentService.updateComment(any(UUID.class), any(UUID.class),
                any(CommentUpdateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value(updatedContent));
    }

    @Test
    @DisplayName("빈 content로 인한 댓글 수정 API 실패")
    void updateComment_Fail_400_InvalidInput() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest invalidRequest = new CommentUpdateRequest(""); // 빈 문자열

        given(userService.findById(userId)).willReturn(mock(User.class));

        // when & then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("작성자가 동일하지 않으면 댓글 수정 실패")
    void updateComment_Fail_403_Unauthorized() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정하려는 내용");

        given(userService.findById(userId)).willReturn(mock(User.class));
        given(commentService.updateComment(any(UUID.class), any(UUID.class),
                any(CommentUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_OWNER));

        // when & then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_OWNER"));
    }

    @Test
    @DisplayName("댓글이 없으면 댓글 수정 실패")
    void updateComment_Fail_404_CommentNotFound() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정하려는 내용");

        given(userService.findById(userId)).willReturn(mock(User.class));
        given(commentService.updateComment(any(UUID.class), any(UUID.class),
                any(CommentUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(patch("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("댓글 논리 삭제 API 검증 성공")
    void softDeleteComment_Success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));
        willDoNothing().given(commentService).softDeleteComment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("작성자가 일치하지 않아 댓글 논리 삭제 API 검증 실패")
    void softDeleteComment_Fail_Unauthorized() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));
        willThrow(new BusinessException(ErrorCode.COMMENT_NOT_OWNER))
                .given(commentService).softDeleteComment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_OWNER"));
    }

    @Test
    @DisplayName("요청자 ID 헤더가 누락되어 댓글 논리 삭제 API 검증 실패")
    void softDeleteComment_Fail_400_MissingHeader() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_HEADER"));
    }

    @Test
    @DisplayName("존재하지 않는 댓글로 인해 댓글 논리 삭제 API 검증 실패")
    void softDeleteComment_Fail_404_NotFound() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));

        willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
                .given(commentService).softDeleteComment(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound()) // 404 검증
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("서버 내부 오류로 인해 댓글 논리 삭제 API 검증 실패")
    void softDeleteComment_Fail_500_InternalServerError() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));

        willThrow(new RuntimeException("Unexpected Database Error"))
                .given(commentService).softDeleteComment(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("댓글 물리 삭제 API 검증 성공")
    void hardDeleteComment_Api_Success() throws Exception {
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));
        willDoNothing().given(commentService).hardDeleteComment(any(UUID.class), any(UUID.class));

        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("요청자 ID 헤더가 누락되어 댓글 물리 삭제 API 검증 실패")
    void hardDeleteComment_Fail_400_MissingHeader() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("MISSING_HEADER"));
    }

    @Test
    @DisplayName("댓글 작성자가 일치하지 않아 물리 삭제 API 권한 검증 실패")
    void hardDeleteComment_Fail_403_Forbidden() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));
        willThrow(new BusinessException(ErrorCode.COMMENT_NOT_OWNER))
                .given(commentService).hardDeleteComment(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_OWNER"));
    }

    @Test
    @DisplayName("존재하지 않는 댓글 식별자로 물리 삭제 API 조회 실패")
    void hardDeleteComment_Fail_404_NotFound() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));
        willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND))
                .given(commentService).hardDeleteComment(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("서버 내부 오류 발생 시 물리 삭제 API 응답 실패")
    void hardDeleteComment_Fail_500_InternalServerError() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(userService.findById(userId)).willReturn(mock(User.class));
        willThrow(new RuntimeException("Unexpected DB Error"))
                .given(commentService).hardDeleteComment(any(UUID.class), any(UUID.class));

        // when & then
        mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("댓글 단건 조회 성공")
    void getComment_Success() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        CommentResponse response = new CommentResponse(
                commentId,
                "좋은 리뷰입니다",
                userId,
                reviewId,
                "테스트닉네임",
                now,
                now
        );

        given(commentQueryService.getComment(commentId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.userNickname").value("테스트닉네임"))
                .andExpect(jsonPath("$.content").value("좋은 리뷰입니다"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    @DisplayName("존재하지 않는 댓글 단건 조회 실패")
    void getComment_Fail_404_NotFound() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();

        given(commentQueryService.getComment(commentId))
                .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("서버 내부 오류로 인한 댓글 단건 조회 실패")
    void getComment_Fail_500_InternalServerError() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();

        given(commentQueryService.getComment(commentId))
                .willThrow(new RuntimeException("DB Connection Timeout"));

        // when & then
        mockMvc.perform(get("/api/comments/{commentId}", commentId))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }


    @Test
    @DisplayName("댓글 목록 조회 성공")
    void getComments_Success() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        CommentResponse commentResponse = new CommentResponse(
                commentId, "좋은 리뷰입니다", userId, reviewId, "테스트닉네임", now, now
        );

        PageResponse<CommentResponse> pageResponse = new PageResponse<>(
                List.of(commentResponse),
                null,
                null,
                50,
                null,
                false
        );

        given(commentQueryService.getComments(any(CommentSearchRequestParam.class)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("reviewId", reviewId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$.content[0].reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.content[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.content[0].userNickname").value("테스트닉네임"))
                .andExpect(jsonPath("$.content[0].content").value("좋은 리뷰입니다"))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.totalElements").isEmpty())
                .andExpect(jsonPath("$.size").value(50));
    }

    @Test
    @DisplayName("커서와 페이지 정보가 있는 댓글 목록 조회 성공")
    void getComments_Success_WithNextCursor() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();
        Instant now = Instant.now();

        // 다음 페이지가 있는 상황
        PageResponse<CommentResponse> pageResponse = new PageResponse<>(
                List.of(
                        new CommentResponse(UUID.randomUUID(), "댓글1", UUID.randomUUID(), reviewId,
                                "닉네임1", now, now),
                        new CommentResponse(UUID.randomUUID(), "댓글2", UUID.randomUUID(), reviewId,
                                "닉네임2", now, now)
                ),
                now.toString(),  // nextCursor
                now,             // nextAfter
                2,
                null,
                true
        );

        given(commentQueryService.getComments(any(CommentSearchRequestParam.class)))
                .willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("reviewId", reviewId.toString())
                        .param("limit", "2")
                        .param("direction", "DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andExpect(jsonPath("$.nextAfter").exists())
                .andExpect(jsonPath("$.totalElements").isEmpty());
    }

    @Test
    @DisplayName("리뷰 ID 누락으로 댓글 목록 조회 실패")
    void getComments_Fail_400_MissingReviewId() throws Exception {
        // given - reviewId 없이 요청

        // when & then
        mockMvc.perform(get("/api/comments"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    @DisplayName("잘못된 정렬 방향으로 댓글 목록 조회 실패")
    void getComments_Fail_400_InvalidDirection() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("reviewId", reviewId.toString())
                        .param("direction", "INVALID_SORT"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("잘못된 페이징 파라미터로 댓글 목록 조회 실패")
    void getComments_Fail_400_InvalidLimit() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("reviewId", reviewId.toString())
                        // @Min(1) 제약조건 위반 데이터 주입
                        .param("limit", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_INPUT.name()));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰 ID로 댓글 목록 조회 실패")
    void getComments_Fail_404_ReviewNotFound() throws Exception {
        // given
        UUID nonExistentReviewId = UUID.randomUUID();

        given(commentQueryService.getComments(any(CommentSearchRequestParam.class)))
                .willThrow(new BusinessException(ErrorCode.REVIEW_NOT_FOUND, "리뷰를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("reviewId", nonExistentReviewId.toString()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.REVIEW_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("서버 내부 오류로 인한 댓글 목록 조회 실패")
    void getComments_Fail_500_InternalServerError() throws Exception {
        // given
        UUID reviewId = UUID.randomUUID();

        given(commentQueryService.getComments(any(CommentSearchRequestParam.class)))
                .willThrow(new RuntimeException("DB Connection Timeout"));

        // when & then
        mockMvc.perform(get("/api/comments")
                        .param("reviewId", reviewId.toString()))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("INTERNAL_SERVER_ERROR"));
    }
}
