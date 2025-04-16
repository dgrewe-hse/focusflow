package de.hse.focusflow.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import de.hse.focusflow.config.TestConfig;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

@DataJpaTest
@Import(TestConfig.class)
class RoleTest {

    @Autowired
    private TestEntityManager entityManager;

    private Role role;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setName("ADMIN");
        role.setDescription("Administrator role");

        user1 = new User();
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john.doe@example.com");
        user1.setPassword("password1");

        user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setPassword("password2");
    }

    @Test
    void testRoleCreation() {
        assertNotNull(role);
        assertEquals("ADMIN", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertNotNull(role.getUsers());
        assertTrue(role.getUsers().isEmpty());
    }

    @Test
    void testAddUser() {
        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(user2);
        role.setUsers(users);

        assertEquals(2, role.getUsers().size());
        assertTrue(role.getUsers().contains(user1));
        assertTrue(role.getUsers().contains(user2));
    }

    @Test
    void testRemoveUser() {
        Set<User> users = new HashSet<>();
        users.add(user1);
        users.add(user2);
        role.setUsers(users);

        users.remove(user1);
        role.setUsers(users);

        assertEquals(1, role.getUsers().size());
        assertFalse(role.getUsers().contains(user1));
        assertTrue(role.getUsers().contains(user2));
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
        assertNull(role.getDescription());
    }

    @Test
    void testRoleDescriptionCanBeEmpty() {
        role.setDescription("");
        assertEquals("", role.getDescription());
    }

    @Test
    void testRoleInheritsBaseEntity() {
        // Persist the role to get the JPA-generated fields
        Role persistedRole = entityManager.persist(role);
        entityManager.flush();

        assertNotNull(persistedRole.getId());
        assertNotNull(persistedRole.getCreatedAt());
        assertNotNull(persistedRole.getUpdatedAt());
    }
}