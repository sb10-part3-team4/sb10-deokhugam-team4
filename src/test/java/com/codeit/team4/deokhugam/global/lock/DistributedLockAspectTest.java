package com.codeit.team4.deokhugam.global.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import({TestContainerConfig.class, DistributedLockAspectTest.TestLockConfig.class})
class DistributedLockAspectTest {

    @Autowired
    private TestLockService testLockService;

    @Nested
    @DisplayName("단일 요청 락 획득")
    class SingleLock {

        @Test
        @DisplayName("락 획득 성공")
        void acquireLockSuccess() {
            String result = testLockService.doWork(1L);

            assertThat(result).isEqualTo("done");
        }

        @Test
        @DisplayName("lockParam으로 키 조합 성공")
        void acquireLockWithParam() {
            String result = testLockService.doWorkWithParam(123L);

            assertThat(result).isEqualTo("done:123");
        }

        @Test
        @DisplayName("복수 lockParam으로 복합 키 조합 성공")
        void acquireLockWithMultipleParams() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();

            String result = testLockService.doWorkWithMultipleParams(userId, bookId);

            assertThat(result).isEqualTo("done:" + userId + ":" + bookId);
        }

        @Test
        @DisplayName("중첩 lockParam으로 객체 필드 키 조합 성공")
        void acquireLockWithNestedParam() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            TestRequest request = new TestRequest(userId, bookId);

            String result = testLockService.doWorkWithNestedParam(request);

            assertThat(result).isEqualTo("done:" + userId + ":" + bookId);
        }
    }

    @Nested
    @DisplayName("동시 요청 락 경합")
    class ConcurrentLock {

        @Test
        @DisplayName("같은 키로 동시 요청 시 하나만 실행 성공")
        void concurrentSameKeyOnlyOneSucceeds() throws InterruptedException {
            int threadCount = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        testLockService.doSlowWork(1L);
                        successCount.incrementAndGet();
                    } catch (BusinessException e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).as("모든 작업이 제한 시간 내 완료되어야 함").isTrue();
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failCount.get()).isEqualTo(threadCount - 1);
        }

        @Test
        @DisplayName("다른 키로 동시 요청 시 모두 실행 성공")
        void concurrentDifferentKeysAllSucceed() throws InterruptedException {
            int threadCount = 3;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

            for (int i = 0; i < threadCount; i++) {
                long id = i + 1;
                executor.submit(() -> {
                    try {
                        testLockService.doSlowWork(id);
                        successCount.incrementAndGet();
                    } catch (Throwable t) {
                        failures.add(t);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).as("모든 작업이 제한 시간 내 완료되어야 함").isTrue();
            assertThat(failures).as("예상치 못한 예외가 발생하지 않아야 함").isEmpty();
            assertThat(successCount.get()).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("락 획득 실패")
    class LockFail {

        @Test
        @DisplayName("waitTime 내에 락 획득 실패 시 BusinessException 발생")
        void throwsBusinessExceptionWhenLockFails() throws InterruptedException {
            CountDownLatch lockAcquired = new CountDownLatch(1);
            CountDownLatch testDone = new CountDownLatch(1);

            Thread holder = new Thread(() ->
                    testLockService.doWorkWithCallback(99L, lockAcquired, testDone));
            holder.start();

            boolean acquired = lockAcquired.await(5, TimeUnit.SECONDS);
            assertThat(acquired).as("락 획득이 제한 시간 내 완료되어야 함").isTrue();

            assertThatThrownBy(() -> testLockService.doSlowWork(99L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.LOCK_ACQUISITION_FAILED));

            testDone.countDown();
            holder.join(5000);
        }
    }

    @TestConfiguration
    static class TestLockConfig {

        @Bean
        public TestLockService testLockService() {
            return new TestLockService();
        }
    }

    record TestRequest(UUID userId, UUID bookId) {
    }

    static class TestLockService {

        @DistributedLock(key = "test:work", lockParam = {"id"})
        public String doWork(Long id) {
            return "done";
        }

        @DistributedLock(key = "test:param", lockParam = {"bookId"})
        public String doWorkWithParam(Long bookId) {
            return "done:" + bookId;
        }

        @DistributedLock(key = "test:multi", lockParam = {"userId", "bookId"})
        public String doWorkWithMultipleParams(UUID userId, UUID bookId) {
            return "done:" + userId + ":" + bookId;
        }

        @DistributedLock(key = "test:nested", lockParam = {"request.userId", "request.bookId"})
        public String doWorkWithNestedParam(TestRequest request) {
            return "done:" + request.userId() + ":" + request.bookId();
        }

        @DistributedLock(key = "test:slow", lockParam = {"id"}, waitTime = 0, leaseTime = 5)
        public String doSlowWork(Long id) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }

        @DistributedLock(key = "test:slow", lockParam = {"id"}, waitTime = 0, leaseTime = 30)
        public String doWorkWithCallback(Long id, CountDownLatch lockAcquired, CountDownLatch release) {
            lockAcquired.countDown();
            try {
                release.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }
    }
}
