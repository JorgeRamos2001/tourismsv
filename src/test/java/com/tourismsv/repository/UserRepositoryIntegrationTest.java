package com.tourismsv.repository;

import com.tourismsv.config.TestcontainersConfiguration;
import com.tourismsv.domain.entity.User;
import com.tourismsv.domain.enums.Role;
import com.tourismsv.domain.enums.UserState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository repository;

    @Test
    void saveAndFindByEmail() {
        var user = em.persistFlushFind(User.builder()
                .name("Test User")
                .email("test@example.com")
                .password("encoded-pass")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .build());

        var found = repository.findByEmail("test@example.com").orElseThrow();

        assertThat(found.getId()).isEqualTo(user.getId());
        assertThat(found.getName()).isEqualTo("Test User");
        assertThat(found.getRole()).isEqualTo(Role.TOURIST);
        assertThat(found.isEnabled()).isTrue();
    }

    @Test
    void findById() {
        var user = em.persistFlushFind(User.builder()
                .name("Admin")
                .email("admin@example.com")
                .password("encoded-pass")
                .role(Role.ADMIN)
                .state(UserState.ACTIVE)
                .build());

        var found = repository.findById(user.getId()).orElseThrow();

        assertThat(found.getEmail()).isEqualTo("admin@example.com");
        assertThat(found.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void existsByEmail() {
        em.persist(User.builder()
                .name("Test User")
                .email("exists@example.com")
                .password("encoded-pass")
                .role(Role.TOURIST)
                .state(UserState.ACTIVE)
                .build());

        assertThat(repository.existsByEmail("exists@example.com")).isTrue();
        assertThat(repository.existsByEmail("missing@example.com")).isFalse();
    }

    @Test
    void findAll() {
        em.persist(User.builder()
                .name("User One").email("one@example.com")
                .password("pass").role(Role.TOURIST).state(UserState.ACTIVE)
                .build());
        em.persist(User.builder()
                .name("User Two").email("two@example.com")
                .password("pass").role(Role.ADMIN).state(UserState.ACTIVE)
                .build());

        var all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById() {
        var user = em.persistFlushFind(User.builder()
                .name("To Delete").email("delete@example.com")
                .password("pass").role(Role.TOURIST).state(UserState.ACTIVE)
                .build());

        assertThat(repository.count()).isEqualTo(1);
        repository.deleteById(user.getId());
        assertThat(repository.count()).isZero();
    }

    @Test
    void findByEmail_returnsEmptyWhenNotFound() {
        var result = repository.findByEmail("nonexistent@example.com");

        assertThat(result).isEmpty();
    }
}
