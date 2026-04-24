package com.codeit.team4.deokhugam.global.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 분산 락을 적용하는 어노테이션.
 * 메서드에 붙이면 AOP가 실행 전 락을 획득하고, 실행 후 자동으로 해제합니다.
 *
 * <h3>사용 예시</h3>
 * <pre>{@code
 * // 1. 리소스별 락 (bookId마다 독립된 락)
 * //    락 키: "deokhugam:book:123"
 * @DistributedLock(key = "deokhugam:book", lockParam = "bookId")
 * public void updateBook(Long bookId, ...) { ... }
 *
 * // 2. 복합 키 락 (request 객체의 필드 조합)
 * //    락 키: "deokhugam:review:userId값:bookId값"
 * @DistributedLock(key = "deokhugam:review", lockParam = {"request.userId", "request.bookId"})
 * public ReviewResponse createReview(ReviewCreateRequest request) { ... }
 *
 * // 3. 전역 락 (스케줄러 중복 실행 방지)
 * //    락 키: "dashboard-batch"
 * @DistributedLock(key = "dashboard-batch", waitTime = 0, leaseTime = 30)
 * public void runDashboardBatch() { ... }
 * }</pre>
 *
 * <h3>파라미터</h3>
 * <ul>
 *   <li>{@code key} - 락 키 prefix (필수)</li>
 *   <li>{@code lockParam} - 메서드 파라미터 이름 배열. 지정하면 {@code key + ":" + 값1 + ":" + 값2}로 키 생성.
 *       중첩 접근도 가능 (예: {@code "request.userId"})</li>
 *   <li>{@code waitTime} - 락 대기 시간 (기본 5초). 0이면 즉시 실패</li>
 *   <li>{@code leaseTime} - 락 자동 해제 시간 (기본 30초). 메서드 최대 실행 시간보다 길게 설정 필요</li>
 *   <li>{@code timeUnit} - waitTime, leaseTime의 시간 단위 (기본 SECONDS)</li>
 * </ul>
 *
 * <p>락 획득 실패 시 {@code BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED)}가 발생합니다.</p>
 *
 * <h3>주의 사항</h3>
 * <ul>
 *   <li><b>Self-invocation 불가</b> - Spring AOP 프록시 기반이므로, 같은 빈 내부에서
 *       {@code @DistributedLock} 메서드를 직접 호출하면 락이 적용되지 않습니다.
 *       반드시 다른 빈을 통해 호출해야 합니다.</li>
 *   <li><b>중첩 접근은 record 전용</b> - {@code "request.userId"} 같은 중첩 접근은
 *       record의 accessor 메서드({@code userId()})를 호출합니다.
 *       JavaBeans 스타일 getter({@code getUserId()})는 지원하지 않습니다.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    String[] lockParam() default {};

    long waitTime() default 5;

    long leaseTime() default 30;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
