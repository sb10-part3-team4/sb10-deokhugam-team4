package com.codeit.team4.deokhugam.global.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.error.BusinessException;
import com.codeit.team4.deokhugam.global.error.ErrorCode;
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
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig.class)
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

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

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

            for (int i = 0; i < threadCount; i++) {
                long id = i + 1;
                executor.submit(() -> {
                    try {
                        testLockService.doSlowWork(id);
                        successCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

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

            Thread holder = new Thread(() -> {
                try {
                    testLockService.doWorkWithCallback(99L, lockAcquired);
                    testDone.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                }
            });
            holder.start();

            lockAcquired.await(5, TimeUnit.SECONDS);

            assertThatThrownBy(() -> testLockService.doSlowWork(99L))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                            .isEqualTo(ErrorCode.LOCK_ACQUISITION_FAILED));

            testDone.countDown();
            holder.join(5000);
        }
    }

    @Service
    static class TestLockService {

        @DistributedLock(key = "test:work", lockParam = "id")
        public String doWork(Long id) {
            return "done";
        }

        @DistributedLock(key = "test:param", lockParam = "bookId")
        public String doWorkWithParam(Long bookId) {
            return "done:" + bookId;
        }

        @DistributedLock(key = "test:slow", lockParam = "id", waitTime = 0, leaseTime = 5)
        public String doSlowWork(Long id) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }

        @DistributedLock(key = "test:slow", lockParam = "id", waitTime = 0, leaseTime = 10)
        public String doWorkWithCallback(Long id, CountDownLatch lockAcquired) {
            lockAcquired.countDown();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "done";
        }
    }
}
