package de.hse.focusflow.repository;

import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.User;
import de.hse.focusflow.model.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findByAssigneeId_ShouldReturnTasks() {
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

        // Act
        List<Task> tasks = taskRepository.findByAssigneeId(user.getId());

        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void findByTeamId_ShouldReturnTasks() {
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

        // Act
        List<Task> tasks = taskRepository.findByTeamId(team.getId());

        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void findByDueDateBefore_ShouldReturnTasks() {
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
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(task);

        // Act
        List<Task> tasks = taskRepository.findByDueDateBefore(LocalDateTime.now().plusDays(2));

        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void findUpcomingTasks_ShouldReturnTasks() {
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
        task.setCreatedBy(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(task);

        // Act
        List<Task> tasks = taskRepository.findUpcomingTasks(LocalDateTime.now(), LocalDateTime.now().plusDays(2));

        // Assert
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(task.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void countTasksByStatusForUser_ShouldReturnCorrectCounts() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(user);

        Task openTask = new Task();
        openTask.setTitle("Open Task");
        openTask.setShortDescription("Test Description");
        openTask.setDueDate(LocalDateTime.now().plusDays(1));
        openTask.setPriority(TaskPriority.HIGH);
        openTask.setStatus(TaskStatus.OPEN);
        openTask.setAssignee(user);
        openTask.setCreatedBy(user);
        openTask.setCreatedAt(LocalDateTime.now());
        openTask.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(openTask);

        Task closedTask = new Task();
        closedTask.setTitle("Closed Task");
        closedTask.setShortDescription("Test Description");
        closedTask.setDueDate(LocalDateTime.now().plusDays(1));
        closedTask.setPriority(TaskPriority.HIGH);
        closedTask.setStatus(TaskStatus.CLOSED);
        closedTask.setAssignee(user);
        closedTask.setCreatedBy(user);
        closedTask.setCreatedAt(LocalDateTime.now());
        closedTask.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(closedTask);

        // Act
        List<Object[]> results = taskRepository.countTasksByStatusForUser(user.getId());

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void countTasksByStatusForTeam_ShouldReturnCorrectCounts() {
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

        Task openTask = new Task();
        openTask.setTitle("Open Task");
        openTask.setShortDescription("Test Description");
        openTask.setDueDate(LocalDateTime.now().plusDays(1));
        openTask.setPriority(TaskPriority.HIGH);
        openTask.setStatus(TaskStatus.OPEN);
        openTask.setTeam(team);
        openTask.setCreatedBy(user);
        openTask.setCreatedAt(LocalDateTime.now());
        openTask.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(openTask);

        Task closedTask = new Task();
        closedTask.setTitle("Closed Task");
        closedTask.setShortDescription("Test Description");
        closedTask.setDueDate(LocalDateTime.now().plusDays(1));
        closedTask.setPriority(TaskPriority.HIGH);
        closedTask.setStatus(TaskStatus.CLOSED);
        closedTask.setTeam(team);
        closedTask.setCreatedBy(user);
        closedTask.setCreatedAt(LocalDateTime.now());
        closedTask.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(closedTask);

        // Act
        List<Object[]> results = taskRepository.countTasksByStatusForTeam(team.getId());

        // Assert
        assertNotNull(results);
        assertEquals(2, results.size());
    }
}