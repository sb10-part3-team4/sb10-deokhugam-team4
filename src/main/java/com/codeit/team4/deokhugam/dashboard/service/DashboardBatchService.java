package com.codeit.team4.deokhugam.dashboard.service;

import com.codeit.team4.deokhugam.book.entity.Book;
import com.codeit.team4.deokhugam.book.service.BookService;
import com.codeit.team4.deokhugam.dashboard.entity.PopularBook;
import com.codeit.team4.deokhugam.dashboard.model.PopularBookSearchModel;
import com.codeit.team4.deokhugam.dashboard.repository.PopularBookRepository;
import com.codeit.team4.deokhugam.dashboard.entity.PeriodType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
    private final PopularBookBatchQueryService popularBookBatchQueryService;
    private final BookService bookService;

    public void updatePopularBooks(LocalDate snapshotDate) {
        log.info("인기 도서 배치 시작: snapshotDate={}", snapshotDate);

        for (PeriodType period : PeriodType.values()) {
            updatePopularBooksByPeriod(period, snapshotDate);
        }

        log.info("인기 도서 배치 완료: snapshotDate={}", snapshotDate);
    }

    private void updatePopularBooksByPeriod(PeriodType period, LocalDate snapshotDate) {
        popularBookRepository.deleteByPeriodAndSnapshotDate(period, snapshotDate);

        List<PopularBookSearchModel> results = popularBookBatchQueryService.findTopBooks(period, snapshotDate);

        List<PopularBook> popularBooks = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            PopularBookSearchModel model = results.get(i);
            Book book = bookService.findById(model.bookId());

            popularBooks.add(new PopularBook(
                    book,
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
}
