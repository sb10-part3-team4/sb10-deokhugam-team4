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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CommentResponse createComment(UUID userId, UUID reviewId, CommentCreateRequest request){
        User user = userService.findById(userId);
        Review review = reviewService.findById(reviewId);

        Comment comment = new Comment(user, review, request.content());
        Comment savedComment = commentRepository.save(comment);
        
        return commentMapper.toResponse(savedComment);
    }


}
