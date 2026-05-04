package com.codeit.team4.deokhugam.dashboard.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import com.codeit.team4.deokhugam.dashboard.entity.PopularBook;
import com.codeit.team4.deokhugam.dashboard.entity.PopularReview;
import com.codeit.team4.deokhugam.dashboard.entity.PowerUser;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookSearchModel;
import com.codeit.team4.deokhugam.dashboard.model.PopularReviewSearchModel;
import com.codeit.team4.deokhugam.dashboard.model.PowerUserSearchModel;
import com.codeit.team4.deokhugam.dashboard.repository.PopularBookRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PopularReviewRepository;
import com.codeit.team4.deokhugam.dashboard.repository.PowerUserRepository;
import com.codeit.team4.deokhugam.dashboard.service.aggregator.PopularBookAggregator;
import com.codeit.team4.deokhugam.dashboard.service.aggregator.PopularReviewAggregator;
import com.codeit.team4.deokhugam.dashboard.service.aggregator.PowerUserAggregator;
import com.codeit.team4.deokhugam.notification.event.ReviewRankedEvent;
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardBatchService {

    private final PopularBookRepository popularBookRepository;
    private final PopularBookAggregator popularBookAggregator;

    private final PowerUserRepository powerUserRepository;
    private final PowerUserAggregator powerUserAggregator;

    private final PopularReviewRepository popularReviewRepository;
    private final PopularReviewAggregator popularReviewAggregator;

    private final ReviewService reviewService;
    private final UserService userService;
    private final BookService bookService;

    private final DashboardFacade dashboardFacade;

    private final ApplicationEventPublisher eventPublisher;

    public static LocalDate defaultSnapshotDate(String zone) {
        return LocalDate.now(ZoneId.of(zone)).minusDays(1);
    }

    @Transactional
    public void updatePopularBooksByPeriod(PeriodType period, LocalDate snapshotDate) {
        popularBookRepository.deleteByPeriodAndSnapshotDate(period, snapshotDate);

        List<PopularBookSearchModel> results = popularBookAggregator.findTopBooks(period, snapshotDate);

        List<UUID> bookIds = results.stream().map(PopularBookSearchModel::bookId).toList();
        Map<UUID, Book> bookMap = bookService.findAllByIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));

        List<PopularBook> popularBooks = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PopularBookSearchModel model = results.get(i);

            popularBooks.add(new PopularBook(
                    bookMap.get(model.bookId()),
                    model.title(),
                    model.author(),
                    model.thumbnailUrl(),
                    period,
                    i + 1,
                    model.reviewCount(),
                    model.avgRating(),
                    snapshotDate
            ));
        }

        popularBookRepository.saveAll(popularBooks);
        evictAfterCommit(dashboardFacade::evictPopularBooksCache);
        log.info("인기 도서 {} 저장 완료: {}건", period, popularBooks.size());
    }

    @Transactional
    public void updatePopularReviewsByPeriod(PeriodType period, LocalDate snapshotDate) {
        popularReviewRepository.deleteByPeriodAndSnapshotDate(period, snapshotDate);

        List<PopularReviewSearchModel> results = popularReviewAggregator.findTopReviews(period, snapshotDate);

        List<UUID> reviewIds = results.stream().map(PopularReviewSearchModel::reviewId).toList();
        List<UUID> bookIds = results.stream().map(PopularReviewSearchModel::bookId).toList();
        List<UUID> userIds = results.stream().map(PopularReviewSearchModel::userId).toList();

        Map<UUID, Review> reviewMap = reviewService.findAllByIds(reviewIds).stream()
                .collect(Collectors.toMap(Review::getId, Function.identity()));
        Map<UUID, Book> bookMap = bookService.findAllByIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, Function.identity()));
        Map<UUID, User> userMap = userService.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<PopularReview> popularReviews = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PopularReviewSearchModel model = results.get(i);

            popularReviews.add(new PopularReview(
                    reviewMap.get(model.reviewId()),
                    bookMap.get(model.bookId()),
                    userMap.get(model.userId()),
                    model.bookTitle(),
                    model.bookThumbnailUrl(),
                    model.userNickname(),
                    model.reviewContent(),
                    model.reviewRating(),
                    period,
                    i + 1,
                    model.likeCount(),
                    model.commentCount(),
                    snapshotDate
            ));
        }

        popularReviewRepository.saveAll(popularReviews);

        if (shouldPublish(period, snapshotDate)) {
            for (PopularReview p : popularReviews) {

                if (p.getReview() == null || p.getUser() == null) continue;
                if (p.getRank() > 10) continue;

                eventPublisher.publishEvent(
                        new ReviewRankedEvent(
                                p.getReview().getId(),
                                p.getUser().getId(),
                                period,
                                p.getRank(),
                                snapshotDate
                        )
                );
            }
        }

        evictAfterCommit(dashboardFacade::evictPopularReviewsCache);
        log.info("인기 리뷰 {} 저장 완료: {}건", period, popularReviews.size());
    }

    @Transactional
    public void updatePowerUsersByPeriod(PeriodType period, LocalDate snapshotDate) {
        powerUserRepository.deleteByPeriodAndSnapshotDate(period, snapshotDate);

        List<PowerUserSearchModel> results = powerUserAggregator.findTopPowerUsers(period, snapshotDate);

        List<UUID> userIds = results.stream().map(PowerUserSearchModel::userId).toList();
        Map<UUID, User> userMap = userService.findAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        List<PowerUser> powerUsers = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PowerUserSearchModel model = results.get(i);

            powerUsers.add(new PowerUser(
                    userMap.get(model.userId()),
                    model.nickname(),
                    period,
                    i + 1,
                    model.reviewScoreSum(),
                    model.likeCount(),
                    model.commentCount(),
                    snapshotDate
            ));
        }

        powerUserRepository.saveAll(powerUsers);
        evictAfterCommit(dashboardFacade::evictPowerUsersCache);
        log.info("파워 유저 {} 저장 완료: {}건", period, powerUsers.size());
    }

    private void evictAfterCommit(Runnable evict) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evict.run();
                }
            });
        } else {
            evict.run();
        }
    }

    private boolean shouldPublish(PeriodType period, LocalDate snapshotDate) {
        return switch (period) {
            case DAILY -> true;
            case WEEKLY -> snapshotDate.getDayOfWeek() == DayOfWeek.SUNDAY;
            case MONTHLY, ALL_TIME -> snapshotDate.getDayOfMonth() == 1;
        };
    }
}
