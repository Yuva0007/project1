package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedAdminUser() {
        return args -> {
            final String adminEmail = "admin@example.com";
            final String adminPasswordPlain = "password";
            userRepository.findByEmail(adminEmail).ifPresentOrElse(user -> {
                // Ensure role and a valid bcrypt password
                boolean changed = false;
                if (user.getRole() != User.UserRole.ADMIN) {
                    user.setRole(User.UserRole.ADMIN);
                    changed = true;
                }
                // If the stored hash does not match the known password, reset it
                try {
                    if (!passwordEncoder.matches(adminPasswordPlain, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(adminPasswordPlain));
                        changed = true;
                    }
                } catch (Exception e) {
                    user.setPassword(passwordEncoder.encode(adminPasswordPlain));
                    changed = true;
                }
                if (changed) {
                    userRepository.save(user);
                    log.info("Admin user updated: {}", adminEmail);
                }
            }, () -> {
                User user = new User();
                user.setName("System Admin");
                user.setEmail(adminEmail);
                user.setPassword(passwordEncoder.encode(adminPasswordPlain));
                user.setRole(User.UserRole.ADMIN);
                userRepository.save(user);
                log.info("Admin user created: {}", adminEmail);
            });
        };
    }
}


