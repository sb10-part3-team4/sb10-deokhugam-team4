package com.codeit.team4.deokhugam.review.dto;

public enum ReviewOrderBy {
    CREATED_AT,
    RATING;

    public boolean isRating() {
        return this == RATING;
    }
}
