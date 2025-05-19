package de.hse.focusflow.config;

import de.hse.focusflow.model.Role;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.User;
import de.hse.focusflow.repository.RoleRepository;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Configuration class to initialize test data when the application starts
 */
@Configuration
public class TestDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(TestDataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Initialize test data when the application starts
     * This will run in all environments (dev, test, prod)
     */
    @Bean
    public CommandLineRunner initTestData() {
        return args -> {
            logger.info("Initializing test data...");

            // Create roles if they don't exist
            Role userRole = createRoleIfNotExists("ROLE_USER", "Regular user with standard permissions");

            // Create test user if it doesn't exist
            User testUser = createTestUser(userRole);

            // Create test team if it doesn't exist
            createTestTeam(testUser);

            logger.info("Test data initialization completed.");
        };
    }

    /**
     * Create a role if it doesn't exist
     */
    private Role createRoleIfNotExists(String roleName, String description) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);

        if (existingRole.isPresent()) {
            logger.info("Role already exists: {}", roleName);
            return existingRole.get();
        }

        // Create new role with required fields
        Role role = new Role();
        role.setName(roleName);
        role.setDescription(description);

        // Set required audit fields manually since we're not using an HTTP request
        // context
        LocalDateTime now = LocalDateTime.now();
        role.setCreatedAt(now);
        role.setUpdatedAt(now);
        role.setVersion(0L);

        Role savedRole = roleRepository.save(role);
        logger.info("Created role: {}", roleName);
        return savedRole;
    }

    /**
     * Create a test user for API testing
     */
    private User createTestUser(Role userRole) {
        String testEmail = "test@focusflow.com";

        // Check if test user already exists
        Optional<User> existingUser = userRepository.findByEmail(testEmail);
        if (existingUser.isPresent()) {
            logger.info("Test user already exists: {}", testEmail);
            return existingUser.get();
        }

        // Create test user
        User testUser = new User();
        testUser.setEmail(testEmail);
        testUser.setPassword(passwordEncoder.encode("Test@123456"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Set required audit fields manually
        LocalDateTime now = LocalDateTime.now();
        testUser.setCreatedAt(now);
        testUser.setUpdatedAt(now);
        testUser.setVersion(0L);

        // Assign USER role to test user
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        // Save test user
        User savedUser = userRepository.save(testUser);
        logger.info("Created test user: {}", testEmail);
        return savedUser;
    }

    /**
     * Create a test team for the test user
     */
    private void createTestTeam(User teamLead) {
        String testTeamName = "Test Team";

        // Check if test team already exists
        Optional<Team> existingTeam = teamRepository.findByName(testTeamName);
        if (existingTeam.isPresent()) {
            logger.info("Test team already exists: {}", testTeamName);
            return;
        }

        // Create test team
        Team testTeam = new Team();
        testTeam.setName(testTeamName);
        testTeam.setDescription("A test team for API testing");
        testTeam.setTeamLead(teamLead);

        // Set required audit fields manually
        LocalDateTime now = LocalDateTime.now();
        testTeam.setCreatedAt(now);
        testTeam.setUpdatedAt(now);
        testTeam.setVersion(0L);

        // Add team lead as a member
        Set<User> members = new HashSet<>();
        members.add(teamLead);
        testTeam.setMembers(members);

        // Save test team
        teamRepository.save(testTeam);
        logger.info("Created test team: {}", testTeamName);
    }
}
