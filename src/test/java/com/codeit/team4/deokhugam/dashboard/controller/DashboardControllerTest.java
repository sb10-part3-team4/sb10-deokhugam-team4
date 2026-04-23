package com.codeit.team4.deokhugam.dashboard.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.service.DashboardFacade;
import com.codeit.team4.deokhugam.global.config.AppProperties;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DashboardController.class)
@Import(AppProperties.class)
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardFacade dashboardService;

    @MockitoBean
    private UserService userService;

    private PopularBookResponse createResponse(int rank) {
        return new PopularBookResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "책" + rank,
                "저자" + rank,
                null,
                PeriodType.DAILY,
                rank,
                new BigDecimal("6.7000"),
                10,
                new BigDecimal("4.50"),
                Instant.now()
        );
    }

    @Test
    @DisplayName("인기 도서 목록 조회 성공")
    void getPopularBooks_success() throws Exception {
        PageResponse<PopularBookResponse> response = new PageResponse<>(
                List.of(createResponse(1), createResponse(2)),
                null, null, 50, null, false
        );
        given(dashboardService.getPopularBooks(any(DashboardSearchRequestParam.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/books/popular")
                        .param("period", "DAILY")
                        .param("direction", "ASC")
                        .param("limit", "50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].rank").value(1))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("기본값으로 인기 도서 목록 조회 성공")
    void getPopularBooks_defaultParams_success() throws Exception {
        PageResponse<PopularBookResponse> response = new PageResponse<>(
                List.of(), null, null, 50, null, false
        );
        given(dashboardService.getPopularBooks(any(DashboardSearchRequestParam.class)))
                .willReturn(response);

        mockMvc.perform(get("/api/books/popular")
                        .param("limit", "50"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @Test
    @DisplayName("잘못된 period로 조회 실패")
    void getPopularBooks_invalidPeriod_fail() throws Exception {
        mockMvc.perform(get("/api/books/popular")
                        .param("period", "INVALID"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("limit 상한 초과로 조회 실패")
    void getPopularBooks_limitExceeded_fail() throws Exception {
        mockMvc.perform(get("/api/books/popular")
                        .param("limit", "101"))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
