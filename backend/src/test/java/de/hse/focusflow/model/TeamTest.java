package de.hse.focusflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private Team team;
    private User teamLead;
    private User member;
    private Task task;

    @BeforeEach
    void setUp() {
        team = new Team();
        teamLead = new User();
        member = new User();
        task = new Task();
    }

    @Test
    void testTeamCreation() {
        team.setName("Development Team");
        team.setDescription("Main development team");
        team.setTeamLead(teamLead);

        assertEquals("Development Team", team.getName());
        assertEquals("Main development team", team.getDescription());
        assertEquals(teamLead, team.getTeamLead());
    }

    @Test
    void testTeamMembersRelationship() {
        Set<User> members = new HashSet<>();
        members.add(member);
        team.setMembers(members);

        assertEquals(1, team.getMembers().size());
        assertTrue(team.getMembers().contains(member));
    }

    @Test
    void testTeamTasksRelationship() {
        Set<Task> tasks = new HashSet<>();
        tasks.add(task);
        team.setTasks(tasks);

        assertEquals(1, team.getTasks().size());
        assertTrue(team.getTasks().contains(task));
    }
}