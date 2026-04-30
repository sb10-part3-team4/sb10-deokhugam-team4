package com.codeit.team4.deokhugam.review.mapper;

import com.codeit.team4.deokhugam.review.dto.ReviewResponse;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.model.ReviewSearchModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(source = "review.book.id", target = "bookId")
    @Mapping(source = "review.book.title", target = "bookTitle")
    @Mapping(source = "review.book.thumbnailUrl", target = "bookThumbnailUrl")
    @Mapping(source = "review.user.id", target = "userId")
    @Mapping(source = "review.user.nickname", target = "userNickname")
    ReviewResponse toResponse(Review review, boolean likedByMe, int commentCount);

    default ReviewResponse toResponse(Review review) {
        return toResponse(review, false, 0);
    }

    ReviewResponse toResponse(ReviewSearchModel model);
}
