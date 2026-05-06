import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// API별 응답 시간 메트릭
const metrics = {
    // 도서
    getBooks: new Trend('api_GET_books'),
    getBookDetail: new Trend('api_GET_books_detail'),
    // 리뷰
    getReviews: new Trend('api_GET_reviews'),
    getReviewDetail: new Trend('api_GET_reviews_detail'),
    createReview: new Trend('api_POST_reviews'),
    updateReview: new Trend('api_PATCH_reviews'),
    deleteReview: new Trend('api_DELETE_reviews'),
    // 대시보드
    popularBooks: new Trend('api_GET_books_popular'),
    popularReviews: new Trend('api_GET_reviews_popular'),
    powerUsers: new Trend('api_GET_users_power'),
    // 유저
    getUser: new Trend('api_GET_users_detail'),
    // 알림
    getNotifications: new Trend('api_GET_notifications'),
    // 댓글
    getComments: new Trend('api_GET_comments'),
};

export const options = {
    scenarios: {
        benchmark: {
            executor: 'constant-vus',
            vus: 20,
            duration: '30s',
        },
    },
};

const jsonHeaders = { 'Content-Type': 'application/json' };

export function setup() {
    // 유저 생성
    const userRes = http.post(
        `${BASE_URL}/api/users`,
        JSON.stringify({
            email: `k6bench${Date.now()}@test.com`,
            nickname: `bench${Date.now()}`.slice(0, 15),
            password: 'Test1234!',
        }),
        { headers: jsonHeaders },
    );

    if (userRes.status !== 201) {
        throw new Error(`유저 생성 실패: ${userRes.status} ${userRes.body}`);
    }
    const userId = JSON.parse(userRes.body).id;

    // 도서 생성
    const bookJson = JSON.stringify({
        title: `벤치마크 도서 ${Date.now()}`,
        author: '테스트 저자',
        description: '벤치마크 테스트용 도서',
        publisher: '테스트출판사',
        publishedDate: '2024-01-01',
        isbn: `978${Date.now()}`.slice(0, 13),
    });

    const bookRes = http.post(`${BASE_URL}/api/books`, {
        bookData: http.file(bookJson, 'bookData.json', 'application/json'),
    });

    if (bookRes.status !== 201) {
        throw new Error(`도서 생성 실패: ${bookRes.status} ${bookRes.body}`);
    }
    const bookId = JSON.parse(bookRes.body).id;

    // 리뷰 생성
    const headers = { ...jsonHeaders, 'Deokhugam-Request-User-ID': userId };
    const reviewRes = http.post(
        `${BASE_URL}/api/reviews`,
        JSON.stringify({ bookId, userId, content: '벤치마크 리뷰', rating: 5 }),
        { headers },
    );

    let reviewId = null;
    if (reviewRes.status === 201) {
        reviewId = JSON.parse(reviewRes.body).id;
    }

    console.log(`setup 완료 - userId: ${userId}, bookId: ${bookId}, reviewId: ${reviewId}`);
    return { userId, bookId, reviewId };
}

export default function (data) {
    const headers = {
        'Content-Type': 'application/json',
        'Deokhugam-Request-User-ID': data.userId,
    };

    // === 대시보드 ===
    let res = http.get(`${BASE_URL}/api/books/popular`, { headers });
    metrics.popularBooks.add(res.timings.duration);
    check(res, { 'GET /books/popular': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/api/reviews/popular`, { headers });
    metrics.popularReviews.add(res.timings.duration);
    check(res, { 'GET /reviews/popular': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/api/users/power`, { headers });
    metrics.powerUsers.add(res.timings.duration);
    check(res, { 'GET /users/power': (r) => r.status === 200 });

    // === 도서 ===
    res = http.get(`${BASE_URL}/api/books?limit=20`, { headers });
    metrics.getBooks.add(res.timings.duration);
    check(res, { 'GET /books': (r) => r.status === 200 });

    res = http.get(`${BASE_URL}/api/books/${data.bookId}`, { headers });
    metrics.getBookDetail.add(res.timings.duration);
    check(res, { 'GET /books/:id': (r) => r.status === 200 });

    // === 리뷰 ===
    res = http.get(`${BASE_URL}/api/reviews?limit=20`, { headers });
    metrics.getReviews.add(res.timings.duration);
    check(res, { 'GET /reviews': (r) => r.status === 200 });

    if (data.reviewId) {
        res = http.get(`${BASE_URL}/api/reviews/${data.reviewId}`, { headers });
        metrics.getReviewDetail.add(res.timings.duration);
        check(res, { 'GET /reviews/:id': (r) => r.status === 200 });
    }

    // === 유저 ===
    res = http.get(`${BASE_URL}/api/users/${data.userId}`, { headers });
    metrics.getUser.add(res.timings.duration);
    check(res, { 'GET /users/:id': (r) => r.status === 200 });

    // === 알림 ===
    res = http.get(`${BASE_URL}/api/notifications`, { headers });
    metrics.getNotifications.add(res.timings.duration);
    check(res, { 'GET /notifications': (r) => r.status === 200 });

    // === 댓글 ===
    res = http.get(`${BASE_URL}/api/comments?limit=20`, { headers });
    metrics.getComments.add(res.timings.duration);
    check(res, { 'GET /comments': (r) => r.status === 200 });

    // === 리뷰 생성 → 수정 → 삭제 사이클 ===
    const createRes = http.post(
        `${BASE_URL}/api/reviews`,
        JSON.stringify({
            bookId: data.bookId,
            userId: data.userId,
            content: `벤치 리뷰 ${Date.now()}`,
            rating: Math.floor(Math.random() * 5) + 1,
        }),
        { headers },
    );
    metrics.createReview.add(createRes.timings.duration);

    if (createRes.status === 201) {
        const newReviewId = JSON.parse(createRes.body).id;

        const updateRes = http.patch(
            `${BASE_URL}/api/reviews/${newReviewId}`,
            JSON.stringify({ content: `수정됨 ${Date.now()}`, rating: 3 }),
            { headers },
        );
        metrics.updateReview.add(updateRes.timings.duration);

        const deleteRes = http.del(`${BASE_URL}/api/reviews/${newReviewId}`, null, { headers });
        metrics.deleteReview.add(deleteRes.timings.duration);
    }

    sleep(0.2);
}

export function handleSummary(data) {
    const apiMetrics = Object.keys(data.metrics)
        .filter((k) => k.startsWith('api_'))
        .map((k) => ({
            name: k.replace('api_', '').replaceAll('_', ' / '),
            avg: data.metrics[k].values.avg.toFixed(2),
            p95: data.metrics[k].values['p(95)'].toFixed(2),
            max: data.metrics[k].values.max.toFixed(2),
            count: data.metrics[k].values.count,
        }))
        .sort((a, b) => parseFloat(b.p95) - parseFloat(a.p95));

    console.log('\n============ API 응답 시간 벤치마크 (ms) ============');
    console.log('API'.padEnd(35) + 'avg'.padStart(10) + 'p95'.padStart(10) + 'max'.padStart(10) + 'count'.padStart(8));
    console.log('-'.repeat(73));

    for (const m of apiMetrics) {
        const avg = parseFloat(m.avg);
        const p95 = parseFloat(m.p95);
        const flag = p95 > 100 ? ' !!!' : p95 > 50 ? ' !' : '';
        console.log(
            m.name.padEnd(35) +
                m.avg.padStart(10) +
                m.p95.padStart(10) +
                m.max.padStart(10) +
                String(m.count).padStart(8) +
                flag,
        );
    }

    console.log('-'.repeat(73));
    console.log('!!! = p95 > 100ms (캐시 강력 권장)');
    console.log('!   = p95 > 50ms  (캐시 고려)');
    console.log('=====================================================\n');

    return {};
}
