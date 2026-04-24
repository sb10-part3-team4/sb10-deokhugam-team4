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
 * // 2. 전역 락 (스케줄러 중복 실행 방지)
 * //    락 키: "dashboard-batch"
 * @DistributedLock(key = "dashboard-batch", waitTime = 0, leaseTime = 30)
 * public void runDashboardBatch() { ... }
 * }</pre>
 *
 * <h3>파라미터</h3>
 * <ul>
 *   <li>{@code key} - 락 키 prefix (필수)</li>
 *   <li>{@code lockParam} - 메서드 파라미터 이름. 지정하면 {@code key + ":" + 파라미터값}으로 키 생성</li>
 *   <li>{@code waitTime} - 락 대기 시간 (기본 5초). 0이면 즉시 실패</li>
 *   <li>{@code leaseTime} - 락 자동 해제 시간 (기본 10초). 메서드 최대 실행 시간보다 길게 설정 필요</li>
 *   <li>{@code timeUnit} - waitTime, leaseTime의 시간 단위 (기본 SECONDS)</li>
 * </ul>
 *
 * <p>락 획득 실패 시 {@code BusinessException(ErrorCode.LOCK_ACQUISITION_FAILED)}가 발생합니다.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    String lockParam() default "";

    long waitTime() default 5;

    long leaseTime() default 10;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
