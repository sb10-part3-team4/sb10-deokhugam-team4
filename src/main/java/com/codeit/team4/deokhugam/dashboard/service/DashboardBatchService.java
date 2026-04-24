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
import com.codeit.team4.deokhugam.review.entity.Review;
import com.codeit.team4.deokhugam.review.service.ReviewService;
import com.codeit.team4.deokhugam.user.entity.User;
import com.codeit.team4.deokhugam.user.service.UserService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
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

    public void updatePopularBooks(LocalDate snapshotDate) {
        log.info("인기 도서 배치 시작: snapshotDate={}", snapshotDate);

        for (PeriodType period : PeriodType.values()) {
            updatePopularBooksByPeriod(period, snapshotDate);
        }

        log.info("인기 도서 배치 완료: snapshotDate={}", snapshotDate);
    }

    public void updatePopularReviews(LocalDate snapshotDate) {
        log.info("인기 리뷰 배치 시작: snapshotDate={}", snapshotDate);

        for (PeriodType period : PeriodType.values()) {
            updatePopularReviewsByPeriod(period, snapshotDate);
        }

        log.info("인기 리뷰 배치 완료: snapshotDate={}", snapshotDate);
    }

    public void updatePowerUsers(LocalDate snapshotDate) {
        log.info("파워 유저 배치 시작: snapshotDate={}", snapshotDate);

        for (PeriodType period : PeriodType.values()) {
            updatePowerUsersByPeriod(period, snapshotDate);
        }

        log.info("파워 유저 배치 완료: snapshotDate={}", snapshotDate);
    }

    private void updatePopularBooksByPeriod(PeriodType period, LocalDate snapshotDate) {
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
        log.info("인기 도서 {} 저장 완료: {}건", period, popularBooks.size());
    }

    private void updatePopularReviewsByPeriod(PeriodType period, LocalDate snapshotDate) {
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
        log.info("인기 리뷰 {} 저장 완료: {}건", period, popularReviews.size());
    }

    private void updatePowerUsersByPeriod(PeriodType period, LocalDate snapshotDate) {
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
        log.info("파워 유저 {} 저장 완료: {}건", period, powerUsers.size());
    }
}
