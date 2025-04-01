package de.hse.focusflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Team team;
    private Role role;
    private Task task;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        team = new Team();
        role = new Role();
        task = new Task();
        notification = new Notification();
    }

    @Test
    void testUserCreation() {
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123!");

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("password123!", user.getPassword());
    }

    @Test
    void testUserTeamRelationship() {
        Set<Team> teams = new HashSet<>();
        teams.add(team);
        user.setTeams(teams);

        assertEquals(1, user.getTeams().size());
        assertTrue(user.getTeams().contains(team));
    }

    @Test
    void testUserRoleRelationship() {
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void testUserTaskRelationship() {
        Set<Task> tasks = new HashSet<>();
        tasks.add(task);
        user.setAssignedTasks(tasks);

        assertEquals(1, user.getAssignedTasks().size());
        assertTrue(user.getAssignedTasks().contains(task));
    }

    @Test
    void testUserNotificationRelationship() {
        Set<Notification> notifications = new HashSet<>();
        notifications.add(notification);
        user.setNotifications(notifications);

        assertEquals(1, user.getNotifications().size());
        assertTrue(user.getNotifications().contains(notification));
    }
}