package de.hse.focusflow.repository;

import de.hse.focusflow.model.Tag;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.User;
import de.hse.focusflow.model.Team;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TagRepository tagRepository;

    @Test
    void findByName_ShouldReturnTag() {
        // Arrange
        Tag tag = new Tag();
        tag.setName("Test Tag");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag);

        // Act
        Optional<Tag> foundTag = tagRepository.findByName("Test Tag");

        // Assert
        assertTrue(foundTag.isPresent());
        assertEquals(tag.getName(), foundTag.get().getName());
    }

    @Test
    void findByNameContaining_ShouldReturnTags() {
        // Arrange
        Tag tag1 = new Tag();
        tag1.setName("Test Tag 1");
        tag1.setCreatedAt(LocalDateTime.now());
        tag1.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Another Test Tag");
        tag2.setCreatedAt(LocalDateTime.now());
        tag2.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag2);

        // Act
        List<Tag> tags = tagRepository.findByNameContaining("Test");

        // Assert
        assertNotNull(tags);
        assertEquals(2, tags.size());
    }

    @Test
    void findTagsUsedByUser_ShouldReturnTags() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setShortDescription("Test Description");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.OPEN);
        task.setAssignee(user);
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(task);

        Tag tag = new Tag();
        tag.setName("Test Tag");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag);

        // Set up bidirectional relationship
        task.setTags(Set.of(tag));
        tag.setTasks(Set.of(task));
        entityManager.persist(task);
        entityManager.persist(tag);
        entityManager.flush();

        // Act
        List<Tag> tags = tagRepository.findTagsUsedByUser(user.getId());

        // Assert
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals(tag.getName(), tags.get(0).getName());
    }

    @Test
    void findTagsUsedByTeam_ShouldReturnTags() {
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

        Task task = new Task();
        task.setTitle("Test Task");
        task.setShortDescription("Test Description");
        task.setDueDate(LocalDateTime.now().plusDays(1));
        task.setPriority(TaskPriority.HIGH);
        task.setStatus(TaskStatus.OPEN);
        task.setTeam(team);
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(task);

        Tag tag = new Tag();
        tag.setName("Test Tag");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag);

        // Set up bidirectional relationship
        task.setTags(Set.of(tag));
        tag.setTasks(Set.of(task));
        entityManager.persist(task);
        entityManager.persist(tag);
        entityManager.flush();

        // Act
        List<Tag> tags = tagRepository.findTagsUsedByTeam(team.getId());

        // Assert
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals(tag.getName(), tags.get(0).getName());
    }

    @Test
    void countTasksByTag_ShouldReturnCorrectCounts() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        Task task1 = new Task();
        task1.setTitle("Test Task 1");
        task1.setShortDescription("Test Description");
        task1.setDueDate(LocalDateTime.now().plusDays(1));
        task1.setPriority(TaskPriority.HIGH);
        task1.setStatus(TaskStatus.OPEN);
        task1.setCreatedBy(user);
        task1.setCreatedAt(LocalDateTime.now());
        task1.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(task1);

        Task task2 = new Task();
        task2.setTitle("Test Task 2");
        task2.setShortDescription("Test Description");
        task2.setDueDate(LocalDateTime.now().plusDays(1));
        task2.setPriority(TaskPriority.HIGH);
        task2.setStatus(TaskStatus.OPEN);
        task2.setCreatedBy(user);
        task2.setCreatedAt(LocalDateTime.now());
        task2.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(task2);

        Tag tag = new Tag();
        tag.setName("Test Tag");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag);

        // Set up bidirectional relationship
        task1.setTags(Set.of(tag));
        task2.setTags(Set.of(tag));
        tag.setTasks(Set.of(task1, task2));
        entityManager.persist(task1);
        entityManager.persist(task2);
        entityManager.persist(tag);
        entityManager.flush();

        // Act
        List<Object[]> results = tagRepository.countTasksByTag();

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Tag", results.get(0)[0]);
        assertEquals(2L, results.get(0)[1]);
    }

    @Test
    void existsByName_ShouldReturnTrueForExistingName() {
        // Arrange
        Tag tag = new Tag();
        tag.setName("Test Tag");
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(tag);

        // Act
        boolean exists = tagRepository.existsByName("Test Tag");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalseForNonExistingName() {
        // Act
        boolean exists = tagRepository.existsByName("Non Existing Tag");

        // Assert
        assertFalse(exists);
    }
}