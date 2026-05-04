package com.codeit.team4.deokhugam.global.cache;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RedisCacheKey {

    // dashboard
    public static final String POPULAR_BOOKS = "popularBooks";
    public static final String POPULAR_REVIEWS = "popularReviews";
    public static final String POWER_USERS = "powerUsers";
}
