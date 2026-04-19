package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.mapper.CommentMapper;
import com.codeit.team4.deokhugam.comment.repository.CommentRepository;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ReviewService reviewService;
    private final UserService userService;

    @Override
    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {
        User user = userService.findById(request.userId());
        Review review = reviewService.findById(request.reviewId());

        Comment comment = new Comment(user, review, request.content());
        Comment savedComment = commentRepository.save(comment);
        review.increaseCommentCount();

        log.info("댓글 생성 완료: commentId={}, userId={}, reviewId={}", savedComment.getId(),
                user.getId(), review.getId());

        return commentMapper.toResponse(savedComment);
    }


}
