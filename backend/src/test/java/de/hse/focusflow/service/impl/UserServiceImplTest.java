package de.hse.focusflow.service.impl;

import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.User;
import de.hse.focusflow.repository.TeamRepository;
import de.hse.focusflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Team testTeam;
    private final String TEST_PASSWORD = "Test123!@#12"; // 12 characters, meets all requirements
    private final String NEW_PASSWORD = "A1b2C3d4E5!@"; // 12 characters, meets all requirements

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("newuser@example.com");
        testUser.setPassword(TEST_PASSWORD);

        testTeam = new Team();
        testTeam.setId(UUID.randomUUID());
        testTeam.setName("Test Team");
    }

    @Test
    void registerUser_WithValidData_ShouldCreateUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(testUser);

        // Act
        User createdUser = userService.registerUser(testUser.getEmail(), TEST_PASSWORD);

        // Assert
        assertNotNull(createdUser);
        assertEquals(testUser.getEmail(), createdUser.getEmail());
        verify(userRepository).save(any());
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(testUser.getEmail(), TEST_PASSWORD));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginUser_WithValidCredentials_ShouldReturnUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPassword())).thenReturn(true);

        // Act
        User loggedInUser = userService.loginUser(testUser.getEmail(), TEST_PASSWORD);

        // Assert
        assertNotNull(loggedInUser);
        assertEquals(testUser.getEmail(), loggedInUser.getEmail());
    }

    @Test
    void loginUser_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.loginUser(testUser.getEmail(), TEST_PASSWORD));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        User foundUser = userService.getUserById(testUser.getId());

        // Assert
        assertNotNull(foundUser);
        assertEquals(testUser.getId(), foundUser.getId());
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getUserById(UUID.randomUUID()));
    }

    @Test
    void updatePassword_WithValidData_ShouldUpdatePassword() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn("encodedNewPassword");
        when(userRepository.save(any())).thenReturn(testUser);

        // Act
        userService.updatePassword(testUser.getId(), TEST_PASSWORD, NEW_PASSWORD);

        // Assert
        verify(userRepository).save(any());
    }

    @Test
    void updatePassword_WithInvalidCurrentPassword_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(TEST_PASSWORD, testUser.getPassword())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> userService.updatePassword(testUser.getId(), TEST_PASSWORD, NEW_PASSWORD));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserTeams_ShouldReturnTeams() {
        // Arrange
        when(teamRepository.findAllByMemberId(testUser.getId())).thenReturn(List.of(testTeam));

        // Act
        var teams = userService.getUserTeams(testUser.getId());

        // Assert
        assertNotNull(teams);
        assertEquals(1, teams.size());
        assertEquals(testTeam.getId(), teams.get(0).getId());
    }
}