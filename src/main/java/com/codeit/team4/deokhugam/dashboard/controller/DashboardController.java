package com.codeit.team4.deokhugam.dashboard.controller;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.controller.api.DashboardApi;
import com.codeit.team4.deokhugam.dashboard.service.DashboardService;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController implements DashboardApi {

    private final DashboardService dashboardService;

    @GetMapping("/books/popular")
    public ResponseEntity<PageResponse<PopularBookResponse>> getPopularBooks(
            @Valid @ParameterObject PopularBookSearchRequestParam param
    ) {
        log.info("인기 도서 목록 조회 요청: period={}, direction={}, limit={}",
                param.period(), param.direction(), param.limit());

        return ResponseEntity.ok(dashboardService.getPopularBooks(param));
    }
}
