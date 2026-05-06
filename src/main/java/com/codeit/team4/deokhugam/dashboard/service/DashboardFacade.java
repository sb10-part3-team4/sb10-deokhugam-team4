package com.codeit.team4.deokhugam.dashboard.service;

import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularReviewResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PowerUserResponse;
import com.codeit.team4.deokhugam.dashboard.mapper.DashboardMapper;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookViewModel;
import com.codeit.team4.deokhugam.dashboard.model.PopularReviewViewModel;
import com.codeit.team4.deokhugam.dashboard.model.PowerUserViewModel;
import com.codeit.team4.deokhugam.dashboard.model.RankedViewModel;
import static com.codeit.team4.deokhugam.global.cache.RedisCacheKey.POPULAR_BOOKS;
import static com.codeit.team4.deokhugam.global.cache.RedisCacheKey.POPULAR_REVIEWS;
import static com.codeit.team4.deokhugam.global.cache.RedisCacheKey.POWER_USERS;

import com.codeit.team4.deokhugam.dashboard.service.reader.PopularBookReader;
import com.codeit.team4.deokhugam.dashboard.service.reader.PopularReviewReader;
import com.codeit.team4.deokhugam.dashboard.service.reader.PowerUserReader;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardFacade {

    private final PopularBookReader popularBookReader;
    private final PopularReviewReader popularReviewReader;
    private final PowerUserReader powerUserReader;
    private final DashboardMapper dashboardMapper;

    @Cacheable(
            value = POPULAR_BOOKS,
            key = "#param.period() + ':' + #param.direction() + ':' + #param.cursor() + ':' + #param.after() + ':' + #param.limit()"
    )
    public PageResponse<PopularBookResponse> getPopularBooks(DashboardSearchRequestParam param) {
        LocalDate latestSnapshotDate = popularBookReader.findLatestSnapshotDate(param.period());
        if (latestSnapshotDate == null) {
            return new PageResponse<>(List.of(), null, null, param.limit(), null, false);
        }

        List<PopularBookViewModel> results = popularBookReader.findPopularBooks(param, latestSnapshotDate);

        List<PopularBookViewModel> content = trimToLimit(results, param.limit());
        boolean hasNext = results.size() > param.limit();

        List<PopularBookResponse> responses = content.stream()
                .map(dashboardMapper::toPopularBookResponse)
                .toList();

        return new PageResponse<>(
                responses,
                extractNextCursor(content, hasNext),
                extractNextAfter(content, hasNext),
                param.limit(),
                null,
                hasNext
        );
    }

    @Cacheable(
            value = POPULAR_REVIEWS,
            key = "#param.period() + ':' + #param.direction() + ':' + #param.cursor() + ':' + #param.after() + ':' + #param.limit()"
    )
    public PageResponse<PopularReviewResponse> getPopularReviews(DashboardSearchRequestParam param) {
        LocalDate latestSnapshotDate = popularReviewReader.findLatestSnapshotDate(param.period());
        if (latestSnapshotDate == null) {
            return new PageResponse<>(List.of(), null, null, param.limit(), null, false);
        }

        List<PopularReviewViewModel> results = popularReviewReader.findPopularReviews(param, latestSnapshotDate);

        List<PopularReviewViewModel> content = trimToLimit(results, param.limit());
        boolean hasNext = results.size() > param.limit();

        List<PopularReviewResponse> responses = content.stream()
                .map(dashboardMapper::toPopularReviewResponse)
                .toList();

        return new PageResponse<>(
                responses,
                extractNextCursor(content, hasNext),
                extractNextAfter(content, hasNext),
                param.limit(),
                null,
                hasNext
        );
    }

    @Cacheable(
            value = POWER_USERS,
            key = "#param.period() + ':' + #param.direction() + ':' + #param.cursor() + ':' + #param.after() + ':' + #param.limit()"
    )
    public PageResponse<PowerUserResponse> getPowerUsers(DashboardSearchRequestParam param) {
        LocalDate latestSnapshotDate = powerUserReader.findLatestSnapshotDate(param.period());
        if (latestSnapshotDate == null) {
            return new PageResponse<>(List.of(), null, null, param.limit(), null, false);
        }
        List<PowerUserViewModel> results = powerUserReader.findPowerUsers(param, latestSnapshotDate);

        List<PowerUserViewModel> content = trimToLimit(results, param.limit());
        boolean hasNext = results.size() > param.limit();

        List<PowerUserResponse> responses = content.stream()
                .map(dashboardMapper::toPowerUserResponse)
                .toList();

        return new PageResponse<>(
                responses,
                extractNextCursor(content, hasNext),
                extractNextAfter(content, hasNext),
                param.limit(),
                null,
                hasNext
        );
    }

    @CacheEvict(value = POPULAR_BOOKS, allEntries = true)
    public void evictPopularBooksCache() {
    }

    @CacheEvict(value = POPULAR_REVIEWS, allEntries = true)
    public void evictPopularReviewsCache() {
    }

    @CacheEvict(value = POWER_USERS, allEntries = true)
    public void evictPowerUsersCache() {
    }

    private <T> List<T> trimToLimit(List<T> results, int limit) {
        return results.subList(0, Math.min(results.size(), limit));
    }

    private <T extends RankedViewModel> String extractNextCursor(List<T> content, boolean hasNext) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        return String.valueOf(content.get(content.size() - 1).rank());
    }

    private <T extends RankedViewModel> Instant extractNextAfter(List<T> content, boolean hasNext) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1).createdAt();
    }
}
