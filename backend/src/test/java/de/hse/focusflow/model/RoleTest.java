package de.hse.focusflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;

@DataJpaTest
@ActiveProfiles("test")
class RoleTest {

    @Autowired
    private TestEntityManager entityManager;

    private Role role;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create role
        role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator role");
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(role);

        // Create and persist users
        user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");
        user1.setPassword("Test123!@#12");
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        user1.setRoles(new HashSet<>());
        entityManager.persist(user1);

        user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setPassword("Test123!@#12");
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        user2.setRoles(new HashSet<>());
        entityManager.persist(user2);

        entityManager.flush();
    }

    @Test
    void testRoleCreation() {
        Role persistedRole = entityManager.find(Role.class, role.getId());
        assertNotNull(persistedRole);
        assertEquals("ADMIN", persistedRole.getName());
        assertEquals("Administrator role", persistedRole.getDescription());
    }

    @Test
    void testAddUserToRole() {
        // Add role to users (since the relationship is managed from User side)
        user1.getRoles().add(role);
        user2.getRoles().add(role);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();
        entityManager.clear();

        // Reload the role
        Role persistedRole = entityManager.find(Role.class, role.getId());
        assertEquals(2, persistedRole.getUsers().size());

        // Check if the users are in the role
        boolean containsUser1 = false;
        boolean containsUser2 = false;
        for (User user : persistedRole.getUsers()) {
            if (user.getId().equals(user1.getId())) {
                containsUser1 = true;
            }
            if (user.getId().equals(user2.getId())) {
                containsUser2 = true;
            }
        }
        assertTrue(containsUser1);
        assertTrue(containsUser2);
    }

    @Test
    void testRemoveUserFromRole() {
        // Add role to users
        user1.getRoles().add(role);
        user2.getRoles().add(role);
        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.flush();

        // Remove role from user1
        user1.getRoles().remove(role);
        entityManager.persist(user1);
        entityManager.flush();
        entityManager.clear();

        // Reload the role
        Role persistedRole = entityManager.find(Role.class, role.getId());
        assertEquals(1, persistedRole.getUsers().size());

        // Check if only user2 is in the role
        boolean containsUser1 = false;
        boolean containsUser2 = false;
        for (User user : persistedRole.getUsers()) {
            if (user.getId().equals(user1.getId())) {
                containsUser1 = true;
            }
            if (user.getId().equals(user2.getId())) {
                containsUser2 = true;
            }
        }
        assertFalse(containsUser1);
        assertTrue(containsUser2);
    }

    @Test
    void testRoleNameValidation() {
        assertThrows(IllegalArgumentException.class, () -> role.setName(null));
        assertThrows(IllegalArgumentException.class, () -> role.setName(""));
        assertThrows(IllegalArgumentException.class, () -> role.setName(" "));
        assertThrows(IllegalArgumentException.class, () -> role.setName("A"));
        assertThrows(IllegalArgumentException.class,
                () -> role.setName("This is a very long role name that exceeds the maximum length of 50 characters"));
    }

    @Test
    void testRoleDescriptionCanBeNull() {
        role.setDescription(null);
        entityManager.persist(role);
        entityManager.flush();

        Role persistedRole = entityManager.find(Role.class, role.getId());
        assertNull(persistedRole.getDescription());
    }

    @Test
    void testRoleDescriptionCanBeEmpty() {
        role.setDescription("");
        entityManager.persist(role);
        entityManager.flush();

        Role persistedRole = entityManager.find(Role.class, role.getId());
        assertEquals("", persistedRole.getDescription());
    }

    @Test
    void testRoleInheritsBaseEntity() {
        Role persistedRole = entityManager.find(Role.class, role.getId());
        assertNotNull(persistedRole.getId());
        assertNotNull(persistedRole.getCreatedAt());
        assertNotNull(persistedRole.getUpdatedAt());
    }
}
