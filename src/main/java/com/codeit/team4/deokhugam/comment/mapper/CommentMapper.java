package com.codeit.team4.deokhugam.comment.mapper;

import com.codeit.team4.deokhugam.comment.dto.CommentResponse;
import com.codeit.team4.deokhugam.comment.entity.Comment;
import com.codeit.team4.deokhugam.comment.model.CommentModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "review.id", target = "reviewId")
    @Mapping(source = "user.nickname", target = "userNickname")
    CommentResponse toResponse(Comment comment);

    CommentResponse toResponse(CommentModel model);

}
