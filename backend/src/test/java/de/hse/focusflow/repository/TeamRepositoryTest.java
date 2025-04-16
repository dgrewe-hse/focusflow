package de.hse.focusflow.repository;

import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TeamRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void findAllByMemberId_ShouldReturnTeams() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        Team team = new Team();
        team.setName("Test Team");
        team.setDescription("Test Description");
        team.setTeamLead(user);
        team.setMembers(new HashSet<>(Collections.singletonList(user)));
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(team);

        // Act
        List<Team> teams = teamRepository.findAllByMemberId(user.getId());

        // Assert
        assertNotNull(teams);
        assertEquals(1, teams.size());
        assertEquals(team.getName(), teams.get(0).getName());
    }

    @Test
    void findAllByTeamLeadId_ShouldReturnTeams() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        Team team = new Team();
        team.setName("Test Team");
        team.setDescription("Test Description");
        team.setTeamLead(user);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(team);

        // Act
        List<Team> teams = teamRepository.findAllByTeamLeadId(user.getId());

        // Assert
        assertNotNull(teams);
        assertEquals(1, teams.size());
        assertEquals(team.getName(), teams.get(0).getName());
    }

    @Test
    void existsByName_ShouldReturnTrueForExistingName() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        Team team = new Team();
        team.setName("Test Team");
        team.setDescription("Test Description");
        team.setTeamLead(user);
        team.setCreatedAt(LocalDateTime.now());
        team.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(team);

        // Act
        boolean exists = teamRepository.existsByName("Test Team");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalseForNonExistingName() {
        // Act
        boolean exists = teamRepository.existsByName("Non Existing Team");

        // Assert
        assertFalse(exists);
    }
}