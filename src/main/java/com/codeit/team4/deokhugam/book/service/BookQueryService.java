package com.codeit.team4.deokhugam.book.service;

import static com.codeit.team4.deokhugam.jooq.tables.Books.BOOKS;

import com.codeit.team4.deokhugam.book.dto.BookResponse;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import com.codeit.team4.deokhugam.global.response.PageResponse;
import com.codeit.team4.deokhugam.jooq.tables.Books;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookQueryService {

    private final DSLContext dsl;

    public PageResponse<BookResponse> getBooks(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit
    ) {

        if ((cursor == null) != (after == null)) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT,
                    "cursor=" + cursor + ", after=" + after
            );
        }

        // 정렬값 검증
        validateSort(orderBy, direction);

        // 문자열로 들어온 커서 값을 실제 DB 비교가 가능한 타입으로 변환
        Object cursorValue = parseCursor(cursor, orderBy);

        // 레포지토리에 조회 요청
        List<BookResponse> books = findBooks(
                keyword, orderBy, direction, cursorValue, after, limit
        );

        // 다음 페이지 존재 여부 확인
        boolean hasNext = books.size() > limit;

        // 다음 페이지 있으면 마지막 데이터 리스트에서 제거
        if (hasNext) {
            books = new ArrayList<>(books.subList(0, limit));
        }

        String nextCursor = null;
        Instant nextAfter = null;

        // 다음 페이지 존재 시, 다음 커서 정보 추출
        if (hasNext && !books.isEmpty()) {
            BookResponse last = books.get(books.size() - 1);
            nextCursor = switch (orderBy) {
                case "publishedDate" -> last.publishedDate().toString();
                case "rating" -> last.rating().toString();
                case "reviewCount" -> String.valueOf(last.reviewCount());
                default -> last.title();
            };
            nextAfter = last.createdAt();
        }

        log.info("도서 목록 조회 완료: size={}", books.size());

        long totalElements = Optional.ofNullable(
                        dsl.selectCount()
                                .from(BOOKS)
                                .where(buildKeywordCondition(keyword).and(BOOKS.DELETED_AT.isNull()))
                                .fetchOne(0, Long.class))
                .orElse(0L);

        return new PageResponse<>(books, nextCursor, nextAfter, limit, totalElements, hasNext);

    }

    private List<BookResponse> findBooks(
            String keyword,
            String orderBy,
            String direction,
            Object cursorValue,
            Instant after,
            int limit
    ) {
        // 기본 조건 : 삭제되지 않은 데이터만 + 키워드 검색
        Condition condition = BOOKS.DELETED_AT.isNull().and(buildKeywordCondition(keyword));

        // 정렬 방향 설정
        Field<? extends Comparable<?>> sortField = getSortField(orderBy);
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        // 메인 정렬 조건(제목, 출판일, 평점, 리뷰수 중 1)
        SortField<?> primarySort = isAsc ? sortField.asc() : sortField.desc();
        // 보조 정렬 조건(생성 시간)
        SortField<?> secondarySort = isAsc ? BOOKS.CREATED_AT.asc() : BOOKS.CREATED_AT.desc();

        // 커서 기반 페이징 조건 적용
        if (cursorValue != null && after != null) {
            OffsetDateTime afterOffset = after.atOffset(ZoneOffset.UTC);

            // jOOQ의 Row 표현식을 사용하기 위해 정렬 기준 필드를 Comparable 타입으로 캐스팅
            @SuppressWarnings({"unchecked", "rawtypes"})
            Field<Comparable> primary = (Field<Comparable>) (Field) sortField;

            condition = condition.and(isAsc
                    ? DSL.row(primary, BOOKS.CREATED_AT)
                    .gt((Comparable<?>) cursorValue, afterOffset)
                    : DSL.row(primary, BOOKS.CREATED_AT)
                            .lt((Comparable<?>) cursorValue, afterOffset));
        }

        // 최종 쿼리 조립 및 실행
        return dsl
                .selectFrom(BOOKS)
                .where(condition)
                .orderBy(primarySort, secondarySort)
                .limit(limit + 1)
                .fetch()
                .map(record -> new BookResponse(
                        record.get(BOOKS.ID),
                        record.get(BOOKS.TITLE),
                        record.get(BOOKS.AUTHOR),
                        record.get(BOOKS.DESCRIPTION),
                        record.get(BOOKS.PUBLISHER),
                        record.get(BOOKS.PUBLISHED_DATE),
                        record.get(BOOKS.ISBN),
                        record.get(BOOKS.THUMBNAIL_URL),
                        record.get(BOOKS.REVIEW_COUNT),
                        record.get(BOOKS.RATING),
                        record.get(BOOKS.CREATED_AT).toInstant(),    // DB 시간을 Java 표준 Instant로 변환
                        record.get(BOOKS.UPDATED_AT).toInstant()
                ));
    }

    // String 커서를 정렬 필드 타입에 맞게 변환하는 헬퍼 메서드
    private Object parseCursor(String cursor, String orderBy) {
        if (cursor == null) {
            return null;
        }
        try {
            return switch (orderBy) {
                case "publishedDate" -> LocalDate.parse(cursor);
                case "rating" -> new BigDecimal(cursor);
                case "reviewCount" -> Integer.parseInt(cursor);
                default -> cursor;
            };
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "cursor=" + cursor);
        }
    }

    // 입력받은 값의 실제 DB 컬럼 필드를 매핑하는 헬퍼 메서드
    private Field<? extends Comparable<?>> getSortField(String orderBy) {
        return switch (orderBy) {
            case "publishedDate" -> BOOKS.PUBLISHED_DATE;
            case "rating" -> BOOKS.RATING;
            case "reviewCount" -> BOOKS.REVIEW_COUNT;
            case "title" -> BOOKS.TITLE;
            default -> throw new IllegalStateException("지원하지 않는 정렬 기준: " + orderBy);
        };
    }

    private Condition buildKeywordCondition(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return DSL.trueCondition();
        }
        return BOOKS.TITLE.containsIgnoreCase(keyword)
                .or(BOOKS.AUTHOR.containsIgnoreCase(keyword))
                .or(BOOKS.ISBN.containsIgnoreCase(keyword));
    }

    private void validateSort(String orderBy, String direction) {
        if (!List.of("title", "publishedDate", "rating", "reviewCount").contains(orderBy)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "orderBy=" + orderBy);
        }
        if (!List.of("ASC", "DESC").contains(direction)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "direction=" + direction);
        }
    }
}