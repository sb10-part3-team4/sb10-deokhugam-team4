package com.codeit.team4.deokhugam.dashboard.service;

import static com.codeit.team4.deokhugam.global.cache.RedisCacheKey.POPULAR_BOOKS;
import static com.codeit.team4.deokhugam.global.cache.RedisCacheKey.POPULAR_REVIEWS;
import static com.codeit.team4.deokhugam.global.cache.RedisCacheKey.POWER_USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.repository.BookRepository;
import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.dashboard.dto.DashboardSearchRequestParam;
import com.codeit.team4.deokhugam.dashboard.dto.PopularBookResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PopularReviewResponse;
import com.codeit.team4.deokhugam.dashboard.dto.PowerUserResponse;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.service.reader.PopularBookReader;
import com.codeit.team4.deokhugam.dashboard.service.reader.PopularReviewReader;
import com.codeit.team4.deokhugam.dashboard.service.reader.PowerUserReader;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.global.response.SortDirection;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.repository.ReviewRepository;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@Import(TestContainerConfig.class)
@ActiveProfiles("test")
class DashboardCacheTest {

    @Autowired
    private DashboardFacade dashboardFacade;

    @Autowired
    private DashboardBatchService dashboardBatchService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private PopularBookReader popularBookReader;

    @MockitoSpyBean
    private PopularReviewReader popularReviewReader;

    @MockitoSpyBean
    private PowerUserReader powerUserReader;

    @Value("${dashboard.batch.zone}")
    private String zone;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.saveAndFlush(new User("cache@test.com", "캐시테스터", "password123"));
        Book book = bookRepository.saveAndFlush(
                new Book("캐시 테스트 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "9999999999", null)
        );
        reviewRepository.saveAndFlush(new Review(book, user, "캐시 테스트 리뷰", 5));

        LocalDate today = LocalDate.now(ZoneId.of(zone));
        dashboardBatchService.updatePopularBooks(today);
        dashboardBatchService.updatePopularReviews(today);
        dashboardBatchService.updatePowerUsers(today);
    }

    @AfterEach
    void tearDown() {
        if (cacheManager.getCache(POPULAR_BOOKS) != null) {
            cacheManager.getCache(POPULAR_BOOKS).clear();
        }
        if (cacheManager.getCache(POPULAR_REVIEWS) != null) {
            cacheManager.getCache(POPULAR_REVIEWS).clear();
        }
        if (cacheManager.getCache(POWER_USERS) != null) {
            cacheManager.getCache(POWER_USERS).clear();
        }
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 요청 두 번째 호출 시 캐시 히트 성공")
    void cacheHit_success() {
        DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );

        // 첫 번째 호출 → DB 조회
        dashboardFacade.getPopularBooks(param);
        // 두 번째 호출 → 캐시에서 반환
        dashboardFacade.getPopularBooks(param);

        // Reader는 1번만 호출되어야 함
        verify(popularBookReader, times(1)).findLatestSnapshotDate(PeriodType.DAILY);
    }

    @Test
    @DisplayName("배치 실행 후 캐시 evict되어 새 데이터 조회 성공")
    void cacheEvictAfterBatch_success() {
        DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );

        // 첫 번째 호출 → DB 조회 + 캐시 저장
        PageResponse<PopularBookResponse> firstResult = dashboardFacade.getPopularBooks(param);
        assertThat(firstResult.content()).hasSize(1);

        // 새 데이터 추가 후 배치 실행 → 캐시 evict
        Book newBook = bookRepository.saveAndFlush(
                new Book("새 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "8888888888", null)
        );
        reviewRepository.saveAndFlush(new Review(newBook, user, "새 리뷰", 4));
        LocalDate today = LocalDate.now(ZoneId.of(zone));
        dashboardBatchService.updatePopularBooks(today);

        // 배치 후 조회 → 새 데이터 반영
        PageResponse<PopularBookResponse> secondResult = dashboardFacade.getPopularBooks(param);
        assertThat(secondResult.content()).hasSize(2);
    }

    @Test
    @DisplayName("다른 파라미터는 별도 캐시 키로 저장 성공")
    void differentParamsDifferentCacheKey_success() {
        DashboardSearchRequestParam dailyParam = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );
        DashboardSearchRequestParam allTimeParam = new DashboardSearchRequestParam(
                PeriodType.ALL_TIME, SortDirection.ASC, null, null, 50
        );

        // DAILY 조회
        dashboardFacade.getPopularBooks(dailyParam);
        // ALL_TIME 조회 → 별도 캐시 키이므로 DB 조회 발생
        dashboardFacade.getPopularBooks(allTimeParam);

        verify(popularBookReader, times(1)).findLatestSnapshotDate(PeriodType.DAILY);
        verify(popularBookReader, times(1)).findLatestSnapshotDate(PeriodType.ALL_TIME);
    }

    @Test
    @DisplayName("인기 리뷰 캐시 히트 성공")
    void popularReviewsCacheHit_success() {
        DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );

        dashboardFacade.getPopularReviews(param);
        dashboardFacade.getPopularReviews(param);

        verify(popularReviewReader, times(1)).findLatestSnapshotDate(PeriodType.DAILY);
    }

    @Test
    @DisplayName("인기 리뷰 배치 후 캐시 evict 성공")
    void popularReviewsCacheEvict_success() {
        DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );

        PageResponse<PopularReviewResponse> first = dashboardFacade.getPopularReviews(param);
        assertThat(first.content()).hasSize(1);

        Book newBook = bookRepository.saveAndFlush(
                new Book("새 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "7777777777", null)
        );
        reviewRepository.saveAndFlush(new Review(newBook, user, "새 리뷰", 4));
        dashboardBatchService.updatePopularReviews(LocalDate.now(ZoneId.of(zone)));

        PageResponse<PopularReviewResponse> second = dashboardFacade.getPopularReviews(param);
        assertThat(second.content()).hasSize(2);
    }

    @Test
    @DisplayName("파워 유저 캐시 히트 성공")
    void powerUsersCacheHit_success() {
        DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );

        dashboardFacade.getPowerUsers(param);
        dashboardFacade.getPowerUsers(param);

        verify(powerUserReader, times(1)).findLatestSnapshotDate(PeriodType.DAILY);
    }

    @Test
    @DisplayName("파워 유저 배치 후 캐시 evict 성공")
    void powerUsersCacheEvict_success() {
        DashboardSearchRequestParam param = new DashboardSearchRequestParam(
                PeriodType.DAILY, SortDirection.ASC, null, null, 50
        );

        PageResponse<PowerUserResponse> first = dashboardFacade.getPowerUsers(param);
        assertThat(first.content()).hasSize(1);

        User newUser = userRepository.saveAndFlush(new User("new@test.com", "새유저", "password123"));
        Book newBook = bookRepository.saveAndFlush(
                new Book("새 책", "저자", "설명", "출판사", LocalDate.of(2024, 1, 1), "6666666666", null)
        );
        reviewRepository.saveAndFlush(new Review(newBook, newUser, "새 리뷰", 4));
        dashboardBatchService.updatePowerUsers(LocalDate.now(ZoneId.of(zone)));

        PageResponse<PowerUserResponse> second = dashboardFacade.getPowerUsers(param);
        assertThat(second.content()).hasSize(2);
    }
}
