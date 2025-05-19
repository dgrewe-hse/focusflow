package de.hse.focusflow.config;

import de.hse.focusflow.model.Role;
import de.hse.focusflow.model.User;
import de.hse.focusflow.repository.RoleRepository;
import de.hse.focusflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

/**
 * Configuration for setting up test entities
 */
@TestConfiguration
public class TestEntitySetup {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Sets up test data for the database
     */
    @Bean
    public boolean setupTestData() {
        // Create user role
        Role userRole = new Role();
        userRole.setId(UUID.randomUUID());
        userRole.setName("ROLE_USER");
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(userRole);

        // Create admin role
        Role adminRole = new Role();
        adminRole.setId(UUID.randomUUID());
        adminRole.setName("ROLE_ADMIN");
        adminRole.setCreatedAt(LocalDateTime.now());
        adminRole.setUpdatedAt(LocalDateTime.now());
        roleRepository.save(adminRole);

        // Create test user
        User testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRoles(new HashSet<>());
        testUser.getRoles().add(userRole);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(testUser);

        // Create admin user
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        adminUser.setRoles(new HashSet<>());
        adminUser.getRoles().add(userRole);
        adminUser.getRoles().add(adminRole);
        adminUser.setCreatedAt(LocalDateTime.now());
        adminUser.setUpdatedAt(LocalDateTime.now());
        userRepository.save(adminUser);

        return true;
    }
}
