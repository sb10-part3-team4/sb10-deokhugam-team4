package com.codeit.team4.deokhugam.global.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LockKeyResolverTest {

    private final LockKeyResolver resolver = new LockKeyResolver();

    @Nested
    @DisplayName("키 생성 성공")
    class Resolve {

        @Test
        @DisplayName("lockParam 없으면 key만 반환")
        void keyOnly() {
            String result = resolver.resolve("dashboard-batch", new String[]{}, null, null);

            assertThat(result).isEqualTo("dashboard-batch");
        }

        @Test
        @DisplayName("단일 lockParam으로 키 조합 성공")
        void singleParam() {
            String result = resolver.resolve(
                    "deokhugam:book",
                    new String[]{"bookId"},
                    new String[]{"bookId"},
                    new Object[]{123L}
            );

            assertThat(result).isEqualTo("deokhugam:book:123");
        }

        @Test
        @DisplayName("복수 lockParam으로 복합 키 조합 성공")
        void multipleParams() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();

            String result = resolver.resolve(
                    "deokhugam:review",
                    new String[]{"userId", "bookId"},
                    new String[]{"userId", "bookId"},
                    new Object[]{userId, bookId}
            );

            assertThat(result).isEqualTo("deokhugam:review:" + userId + ":" + bookId);
        }

        @Test
        @DisplayName("중첩 lockParam으로 객체 필드 접근 성공")
        void nestedParam() {
            UUID userId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            TestRequest request = new TestRequest(userId, bookId);

            String result = resolver.resolve(
                    "deokhugam:review",
                    new String[]{"request.userId", "request.bookId"},
                    new String[]{"request"},
                    new Object[]{request}
            );

            assertThat(result).isEqualTo("deokhugam:review:" + userId + ":" + bookId);
        }
    }

    @Nested
    @DisplayName("키 생성 실패")
    class ResolveFail {

        @Test
        @DisplayName("존재하지 않는 파라미터 이름이면 DistributedLockException 발생")
        void paramNotFound() {
            assertThatThrownBy(() -> resolver.resolve(
                    "test",
                    new String[]{"invalid"},
                    new String[]{"bookId"},
                    new Object[]{1L}
            )).isInstanceOf(DistributedLockException.class);
        }

        @Test
        @DisplayName("파라미터 값이 null이면 DistributedLockException 발생")
        void paramValueNull() {
            assertThatThrownBy(() -> resolver.resolve(
                    "test",
                    new String[]{"bookId"},
                    new String[]{"bookId"},
                    new Object[]{null}
            )).isInstanceOf(DistributedLockException.class);
        }

        @Test
        @DisplayName("중첩 대상 객체가 null이면 DistributedLockException 발생")
        void nestedTargetNull() {
            assertThatThrownBy(() -> resolver.resolve(
                    "test",
                    new String[]{"request.userId"},
                    new String[]{"request"},
                    new Object[]{null}
            )).isInstanceOf(DistributedLockException.class);
        }

        @Test
        @DisplayName("존재하지 않는 필드 접근이면 DistributedLockException 발생")
        void nestedFieldNotFound() {
            TestRequest request = new TestRequest(UUID.randomUUID(), UUID.randomUUID());

            assertThatThrownBy(() -> resolver.resolve(
                    "test",
                    new String[]{"request.invalid"},
                    new String[]{"request"},
                    new Object[]{request}
            )).isInstanceOf(DistributedLockException.class);
        }

        @Test
        @DisplayName("paramNames가 null이면 DistributedLockException 발생")
        void paramNamesNull() {
            assertThatThrownBy(() -> resolver.resolve(
                    "test",
                    new String[]{"bookId"},
                    null,
                    new Object[]{1L}
            )).isInstanceOf(DistributedLockException.class);
        }
    }

    record TestRequest(UUID userId, UUID bookId) {
    }
}
