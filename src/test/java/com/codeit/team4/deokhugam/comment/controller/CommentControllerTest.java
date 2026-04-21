package com.codeit.team4.deokhugam.comment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.dto.CommentUpdateRequest;
import com.codeit.team4.deokhugam.comment.service.CommentService;
import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@EnableConfigurationProperties(AppProperties.class)
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

        CommentResponse response = new CommentResponse(
                commentId, updatedContent, userId, reviewId,
                "테스트닉네임", Instant.now(), Instant.now()
        );

        given(commentService.updateComment(any(UUID.class), any(UUID.class),
                any(CommentUpdateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch(
                        "/api/comments/{commentId}", commentId)
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

        // when & then
        mockMvc.perform(patch(
                        "/api/comments/{commentId}", commentId)
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
        UUID requesterId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정하려는 내용");

        given(commentService.updateComment(any(UUID.class), any(UUID.class),
                any(CommentUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED_COMMENT_ACCESS));

        // when & then
        mockMvc.perform(patch(
                        "/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", requesterId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED_COMMENT_ACCESS"));
    }

    @Test
    @DisplayName("댓글이 없으면 댓글 수정 실패")
    void updateComment_Fail_404_CommentNotFound() throws Exception {
        // given
        UUID commentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        CommentUpdateRequest request = new CommentUpdateRequest("수정하려는 내용");

        given(commentService.updateComment(any(UUID.class), any(UUID.class),
                any(CommentUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // when & then
        mockMvc.perform(patch(
                        "/api/comments/{commentId}", commentId)
                        .header("Deokhugam-Request-User-ID", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("COMMENT_NOT_FOUND"));
    }
}
