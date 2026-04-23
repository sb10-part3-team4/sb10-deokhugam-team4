package com.codeit.team4.deokhugam.dashboard.service;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.mapper.DashboardMapper;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookViewModel;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final PopularBookReader popularBookReader;
    private final DashboardMapper dashboardMapper;

    public PageResponse<PopularBookResponse> getPopularBooks(PopularBookSearchRequestParam param) {
        LocalDate latestSnapshotDate = popularBookReader.findLatestSnapshotDate(param.period());
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

    private List<PopularBookViewModel> trimToLimit(List<PopularBookViewModel> results, int limit) {
        return results.size() > limit
                ? results.subList(0, limit) : results;
    }

    private String extractNextCursor(List<PopularBookViewModel> content, boolean hasNext) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        return String.valueOf(content.get(content.size() - 1).rank());
    }

    private Instant extractNextAfter(List<PopularBookViewModel> content, boolean hasNext) {
        if (!hasNext || content.isEmpty()) {
            return null;
        }
        return content.get(content.size() - 1).createdAt();
    }
}
