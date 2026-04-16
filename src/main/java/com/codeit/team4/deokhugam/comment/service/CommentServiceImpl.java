package com.codeit.team4.deokhugam.comment.service;

import com.codeit.team4.deokhugam.comment.dto.CommentCreateRequest;
import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    @Override
    @Transactional
    public CommentResponse createComment(UUID userId, UUID reviewId, CommentCreateRequest request){
        return null;
    }


}
