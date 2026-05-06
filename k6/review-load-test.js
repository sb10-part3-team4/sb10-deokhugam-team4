import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const USER_COUNT = parseInt(__ENV.USER_COUNT || '10');

// 커스텀 메트릭
const createSuccess = new Counter('review_create_success');
const createFail = new Counter('review_create_fail');
const deleteSuccess = new Counter('review_delete_success');
const deleteFail = new Counter('review_delete_fail');
const createDuration = new Trend('review_create_duration');
const deleteDuration = new Trend('review_delete_duration');

export const options = {
    scenarios: {
        review_crud: {
            executor: 'ramping-vus',
            startVUs: 1,
            stages: [
                { duration: '10s', target: 10 },
                { duration: '30s', target: 50 },
                { duration: '20s', target: 50 },
                { duration: '10s', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.3'],
    },
};

const jsonHeaders = { 'Content-Type': 'application/json' };

export function setup() {
    const users = [];
    const books = [];

    // 유저 생성
    for (let i = 0; i < USER_COUNT; i++) {
        const res = http.post(
            `${BASE_URL}/api/users`,
            JSON.stringify({
                email: `k6test${Date.now()}${i}@test.com`,
                nickname: `k6user${i}`,
                password: 'Test1234!',
            }),
            { headers: jsonHeaders },
        );

        if (res.status === 201) {
            const user = JSON.parse(res.body);
            users.push(user.id);
        } else {
            console.warn(`유저 생성 실패: ${res.status} ${res.body}`);
        }
    }

    // 도서 생성 (3권)
    for (let i = 0; i < 3; i++) {
        const isbn = `978${Date.now()}`.slice(0, 13);
        const bookJson = JSON.stringify({
            title: `K6 테스트 도서 ${i} - ${Date.now()}`,
            author: `테스트 저자 ${i}`,
            description: `K6 부하 테스트용 도서입니다 ${i}`,
            publisher: '테스트출판사',
            publishedDate: '2024-01-01',
            isbn: isbn,
        });

        const res = http.post(`${BASE_URL}/api/books`, {
            bookData: http.file(bookJson, 'bookData.json', 'application/json'),
        });

        if (res.status === 201) {
            const book = JSON.parse(res.body);
            books.push(book.id);
        } else {
            console.warn(`도서 생성 실패: ${res.status} ${res.body}`);
        }
    }

    console.log(`setup 완료 - 유저 ${users.length}명, 도서 ${books.length}권`);

    if (users.length === 0 || books.length === 0) {
        throw new Error('테스트 데이터 생성 실패. 서버 상태를 확인하세요.');
    }

    return { userIds: users, bookIds: books };
}

export default function (data) {
    const userId = data.userIds[__VU % data.userIds.length];
    const bookId = data.bookIds[Math.floor(Math.random() * data.bookIds.length)];
    const headers = {
        'Content-Type': 'application/json',
        'Deokhugam-Request-User-ID': userId,
    };

    // 1. 리뷰 생성
    const createRes = http.post(
        `${BASE_URL}/api/reviews`,
        JSON.stringify({
            bookId: bookId,
            userId: userId,
            content: `K6 부하 테스트 리뷰 - VU:${__VU} ITER:${__ITER} ${Date.now()}`,
            rating: Math.floor(Math.random() * 5) + 1,
        }),
        { headers },
    );
    createDuration.add(createRes.timings.duration);

    const created = check(createRes, {
        '리뷰 생성 201': (r) => r.status === 201,
    });

    if (!created) {
        createFail.add(1);
        sleep(0.5);
        return;
    }
    createSuccess.add(1);

    const reviewId = JSON.parse(createRes.body).id;
    sleep(Math.random() * 0.3);

    // 2. 리뷰 조회
    const getRes = http.get(`${BASE_URL}/api/reviews/${reviewId}`, { headers });
    check(getRes, {
        '리뷰 조회 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 0.3);

    // 3. 리뷰 수정
    const updateRes = http.patch(
        `${BASE_URL}/api/reviews/${reviewId}`,
        JSON.stringify({
            content: `수정된 리뷰 - ${Date.now()}`,
            rating: Math.floor(Math.random() * 5) + 1,
        }),
        { headers },
    );
    check(updateRes, {
        '리뷰 수정 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 0.3);

    // 4. 리뷰 삭제 (soft delete)
    const deleteRes = http.del(`${BASE_URL}/api/reviews/${reviewId}`, null, { headers });
    deleteDuration.add(deleteRes.timings.duration);

    const deleted = check(deleteRes, {
        '리뷰 삭제 204': (r) => r.status === 204,
    });

    if (deleted) {
        deleteSuccess.add(1);
    } else {
        deleteFail.add(1);
    }

    sleep(Math.random() * 0.5);
}

export function handleSummary(data) {
    const metrics = [
        ['총 요청 수', data.metrics.http_reqs.values.count],
        ['평균 응답 시간', `${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`],
        ['p95 응답 시간', `${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`],
        ['실패율', `${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`],
        ['리뷰 생성 성공', data.metrics.review_create_success?.values.count || 0],
        ['리뷰 생성 실패', data.metrics.review_create_fail?.values.count || 0],
        ['리뷰 삭제 성공', data.metrics.review_delete_success?.values.count || 0],
        ['리뷰 삭제 실패', data.metrics.review_delete_fail?.values.count || 0],
    ];

    console.log('\n========== 리뷰 부하 테스트 결과 ==========');
    metrics.forEach(([k, v]) => console.log(`  ${k}: ${v}`));
    console.log('==========================================\n');

    return {};
}
