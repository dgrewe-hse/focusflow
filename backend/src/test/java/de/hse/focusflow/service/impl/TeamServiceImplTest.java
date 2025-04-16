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

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamServiceImpl teamService;

    private User testUser;
    private Team testTeam;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testTeam = new Team();
        testTeam.setId(UUID.randomUUID());
        testTeam.setName("Test Team");
        testTeam.setDescription("Test Description");
        testTeam.setTeamLead(testUser);
        testTeam.setMembers(new HashSet<>(Collections.singletonList(testUser)));
    }

    @Test
    void createTeam_WithValidData_ShouldCreateTeam() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(teamRepository.existsByName(anyString())).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // Act
        Team result = teamService.createTeam("Test Team", "Test Description", testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testTeam.getName(), result.getName());
        assertEquals(testTeam.getDescription(), result.getDescription());
        assertEquals(testUser, result.getTeamLead());
        assertTrue(result.getMembers().contains(testUser));
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void createTeam_WithExistingName_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));
        when(teamRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.createTeam("Test Team", "Test Description", testUser.getId());
        });
    }

    @Test
    void createTeam_WithInvalidName_ShouldThrowException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.createTeam("", "Test Description", testUser.getId());
        });
    }

    @Test
    void getTeamById_WithValidId_ShouldReturnTeam() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));

        // Act
        Team result = teamService.getTeamById(testTeam.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testTeam.getId(), result.getId());
    }

    @Test
    void getTeamById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            teamService.getTeamById(UUID.randomUUID());
        });
    }

    @Test
    void updateTeam_WithValidData_ShouldUpdateTeam() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        when(teamRepository.existsByName(anyString())).thenReturn(false);
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // Act
        Team result = teamService.updateTeam(testTeam.getId(), "Updated Name", "Updated Description");

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void updateTeam_WithExistingName_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        when(teamRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.updateTeam(testTeam.getId(), "Existing Name", "Test Description");
        });
    }

    @Test
    void deleteTeam_WithValidId_ShouldDeleteTeam() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        doNothing().when(teamRepository).delete(any(Team.class));

        // Act
        teamService.deleteTeam(testTeam.getId());

        // Assert
        verify(teamRepository).delete(any(Team.class));
    }

    @Test
    void addUserToTeam_WithValidData_ShouldAddUser() {
        // Arrange
        User newUser = new User();
        newUser.setId(UUID.randomUUID());
        newUser.setEmail("new@example.com");
        newUser.setFirstName("New");
        newUser.setLastName("User");

        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(newUser));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // Act
        teamService.addUserToTeam(testTeam.getId(), newUser.getId(), "MEMBER");

        // Assert
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void addUserToTeam_WithExistingMember_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.addUserToTeam(testTeam.getId(), testUser.getId(), "MEMBER");
        });
    }

    @Test
    void removeUserFromTeam_WithValidData_ShouldRemoveUser() {
        // Arrange
        User memberToRemove = new User();
        memberToRemove.setId(UUID.randomUUID());
        memberToRemove.setEmail("remove@example.com");
        memberToRemove.setFirstName("Remove");
        memberToRemove.setLastName("User");
        testTeam.getMembers().add(memberToRemove);

        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(memberToRemove));
        when(teamRepository.save(any(Team.class))).thenReturn(testTeam);

        // Act
        teamService.removeUserFromTeam(testTeam.getId(), memberToRemove.getId());

        // Assert
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void removeUserFromTeam_WithTeamLead_ShouldThrowException() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            teamService.removeUserFromTeam(testTeam.getId(), testUser.getId());
        });
    }

    @Test
    void getTeamMembers_WithValidTeamId_ShouldReturnMembers() {
        // Arrange
        when(teamRepository.findById(any(UUID.class))).thenReturn(Optional.of(testTeam));

        // Act
        List<User> members = teamService.getTeamMembers(testTeam.getId());

        // Assert
        assertNotNull(members);
        assertEquals(1, members.size());
        assertEquals(testUser, members.get(0));
    }

    @Test
    void getTeamsByCreator_WithValidUserId_ShouldReturnTeams() {
        // Arrange
        List<Team> teams = Collections.singletonList(testTeam);
        when(teamRepository.findAllByTeamLeadId(any(UUID.class))).thenReturn(teams);

        // Act
        List<Team> result = teamService.getTeamsByCreator(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTeam, result.get(0));
    }
}