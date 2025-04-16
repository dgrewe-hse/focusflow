package de.hse.focusflow.repository;

import de.hse.focusflow.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(testUser);
        entityManager.flush();
    }

    // @Test
    // void findByEmail_ShouldReturnUser() {
    // // Arrange
    // User user = new User();
    // user.setEmail("test@example.com");
    // user.setFirstName("Test");
    // user.setLastName("User");
    // user.setPassword("password123");
    // user.setCreatedAt(LocalDateTime.now());
    // user.setUpdatedAt(LocalDateTime.now());
    // entityManager.persist(user);

    // // Act
    // Optional<User> foundUser = userRepository.findByEmail("test@example.com");

    // // Assert
    // assertTrue(foundUser.isPresent());
    // assertEquals(user.getEmail(), foundUser.get().getEmail());
    // assertEquals(user.getFirstName(), foundUser.get().getFirstName());
    // assertEquals(user.getLastName(), foundUser.get().getLastName());
    // }

    @Test
    void findByEmail_ShouldReturnEmptyForNonExistingEmail() {
        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexisting@example.com");

        // Assert
        assertFalse(foundUser.isPresent());
    }

    // @Test
    // void existsByEmail_ShouldReturnTrueForExistingEmail() {
    // // Arrange
    // User user = new User();
    // user.setEmail("test@example.com");
    // user.setFirstName("Test");
    // user.setLastName("User");
    // user.setPassword("password123");
    // user.setCreatedAt(LocalDateTime.now());
    // user.setUpdatedAt(LocalDateTime.now());
    // entityManager.persist(user);

    // // Act
    // boolean exists = userRepository.existsByEmail("test@example.com");

    // // Assert
    // assertTrue(exists);
    // }

    @Test
    void existsByEmail_ShouldReturnFalseForNonExistingEmail() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexisting@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        // Act
        Optional<User> found = userRepository.findById(testUser.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(testUser.getId(), found.get().getId());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // Act
        Optional<User> found = userRepository.findById(UUID.randomUUID());

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void save_WithNewUser_ShouldCreateUser() {
        // Arrange
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setPassword("newpassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        // Act
        User saved = userRepository.save(newUser);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(newUser.getEmail(), saved.getEmail());
    }

    @Test
    void save_WithExistingUser_ShouldUpdateUser() {
        // Arrange
        testUser.setEmail("updated@example.com");

        // Act
        User updated = userRepository.save(testUser);

        // Assert
        assertEquals(testUser.getId(), updated.getId());
        assertEquals("updated@example.com", updated.getEmail());
    }

    @Test
    void delete_WithExistingUser_ShouldRemoveUser() {
        // Act
        userRepository.delete(testUser);

        // Assert
        Optional<User> found = userRepository.findById(testUser.getId());
        assertFalse(found.isPresent());
    }
}