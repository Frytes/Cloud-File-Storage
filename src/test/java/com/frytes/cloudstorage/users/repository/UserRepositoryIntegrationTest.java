package com.frytes.cloudstorage.users.repository;

import com.frytes.cloudstorage.TestcontainersConfiguration;
import com.frytes.cloudstorage.users.model.Provider;
import com.frytes.cloudstorage.users.model.Role;
import com.frytes.cloudstorage.users.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@Transactional
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUserByUsername() {
        User user = User.builder()
                .username("integration_test_user")
                .password("secret123")
                .role(Role.USER)
                .provider(Provider.LOCAL)
                .build();

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("integration_test_user");
        boolean exists = userRepository.existsByUsername("integration_test_user");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("integration_test_user");
        assertThat(exists).isTrue();
    }
}