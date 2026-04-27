package com.codeit.team4.deokhugam.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.team4.deokhugam.config.TestContainerConfig;
import com.codeit.team4.deokhugam.global.config.JpaAuditingConfig;
import com.codeit.team4.deokhugam.user.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import({TestContainerConfig.class, JpaAuditingConfig.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager em;

    // --- 성공 케이스 ---

    @Test
    @DisplayName("ID로 삭제되지 않은 유저 조회 성공")
    void findByIdAndDeletedAtIsNull_success() {
        // given
        User user = saveUser("user1@test.com", "user1");

        // when
        Optional<User> result = userRepository.findByIdAndDeletedAtIsNull(user.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("이메일로 삭제되지 않은 유저 조회 성공")
    void findByEmailAndDeletedAtIsNull_success() {
        // given
        User user = saveUser("user2@test.com", "user2");

        // when
        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull(user.getEmail());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("이메일로 삭제되지 않은 유저 존재 여부 조회 성공")
    void existsByEmailAndDeletedAtIsNull_success() {
        // given
        User user = saveUser("user3@test.com", "user3");

        // when
        boolean result = userRepository.existsByEmailAndDeletedAtIsNull(user.getEmail());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("삭제되지 않은 전체 유저 조회 성공")
    void findAllByDeletedAtIsNull_success() {
        // given
        saveUser("user4@test.com", "user4");
        saveUser("user5@test.com", "user5");

        // when
        Page<User> result = userRepository.findAllByDeletedAtIsNull(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        "user4@test.com", "user5@test.com"
                );
    }

    // --- 실패 케이스 ---

    @Test
    @DisplayName("ID로 삭제되지 않은 유저 조회 실패")
    void findByIdAndDeletedAtIsNull_fail() {
        // given
        User user = saveUser("user6@test.com", "user6");
        markAsDeleted(user);

        // when
        Optional<User> result = userRepository.findByIdAndDeletedAtIsNull(user.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일로 삭제되지 않은 유저 조회 실패")
    void findByEmailAndDeletedAtIsNull_fail() {
        // given
        User user = saveUser("user7@test.com", "user7");
        markAsDeleted(user);

        // when
        Optional<User> result = userRepository.findByEmailAndDeletedAtIsNull(user.getEmail());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("이메일로 삭제되지 않은 유저 존재 여부 조회 실패")
    void existsByEmailAndDeletedAtIsNull_fail() {
        // given
        User user = saveUser("user8@test.com", "user8");
        markAsDeleted(user);

        // when
        boolean result = userRepository.existsByEmailAndDeletedAtIsNull(user.getEmail());

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("삭제되지 않은 전체 유저 조회 실패")
    void findAllByDeletedAtIsNull_fail() {
        // given
        User deletedUser1 = saveUser("deleted1@test.com", "deleted1");
        User deletedUser2 = saveUser("deleted2@test.com", "deleted2");
        markAsDeleted(deletedUser1);
        markAsDeleted(deletedUser2);

        // when
        Page<User> result = userRepository.findAllByDeletedAtIsNull(PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // --- 헬퍼 메소드 ---

    /**
     * 유저를 생성하고 저장하는 헬퍼 메서드
     */
    private User saveUser(String email, String nickname) {
        return userRepository.save(new User(email, nickname, "password1!"));
    }

    /**
     * 영속성 컨텍스트를 동기화하고 특정 유저를 소프트 딜리트 상태로 만드는 헬퍼 메서드
     */
    private void markAsDeleted(User user) {
        em.flush();
        em.getEntityManager()
                .createQuery("update User u set u.deletedAt = :now where u.id = :id")
                .setParameter("now", Instant.now())
                .setParameter("id", user.getId())
                .executeUpdate();
        em.clear();
    }

    /**
     * 영속성 컨텍스트를 동기화하고,
     * 지정한 시간으로 deletedAt을 설정하여 소프트 딜리트 상태로 변경한다
     */
    private void markAsDeleted(User user, Instant deletedAt) {
        em.flush();
        em.getEntityManager()
                .createQuery("update User u set u.deletedAt = :time where u.id = :id")
                .setParameter("time", deletedAt)
                .setParameter("id", user.getId())
                .executeUpdate();
        em.clear();
    }
}