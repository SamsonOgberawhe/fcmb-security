package com.fcmb.sampleapplication;

import com.fcmb.sampleapplication.entity.User;
import com.fcmb.sampleapplication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing database with test users...");

            // Create regular user
            User regularUser = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("password"))
                    .email("user@example.com")
                    .roles(Set.of("ROLE_USER"))
                    .enabled(true)
                    .build();

            // Create admin user
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@example.com")
                    .roles(Set.of("ROLE_USER", "ROLE_ADMIN"))
                    .enabled(true)
                    .build();

            userRepository.save(regularUser);
            userRepository.save(adminUser);

            log.info("Test users created successfully:");
            log.info("Regular User - username: 'user', password: 'password'");
            log.info("Admin User - username: 'admin', password: 'admin123'");
        }
    }
}
