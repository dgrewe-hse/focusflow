// package de.hse.focusflow.service;

// import de.hse.focusflow.model.*;
// import de.hse.focusflow.repository.*;
// import de.hse.focusflow.service.impl.TaskServiceImpl;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.ActiveProfiles;

// import java.time.LocalDateTime;
// import java.util.Set;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest
// @Import(TaskServiceImpl.class)
// @ActiveProfiles("test")
// class TaskServiceIntegrationTest {

// @Autowired
// private TestEntityManager entityManager;

// @Autowired
// private TaskService taskService;

// @Autowired
// private TaskRepository taskRepository;

// @Autowired
// private UserRepository userRepository;

// @Autowired
// private TeamRepository teamRepository;

// @Autowired
// private TagRepository tagRepository;

// private User testUser;
// private User testAssignee;
// private Team testTeam;
// private Tag testTag;

// @BeforeEach
// void setUp() {
// // Create test user
// testUser = new User();
// testUser.setEmail("test@example.com");
// testUser.setFirstName("Test");
// testUser.setLastName("User");
// testUser = entityManager.persist(testUser);

// // Create test assignee
// testAssignee = new User();
// testAssignee.setEmail("assignee@example.com");
// testAssignee.setFirstName("Assignee");
// testAssignee.setLastName("User");
// testAssignee = entityManager.persist(testAssignee);

// // Create test team
// testTeam = new Team();
// testTeam.setName("Test Team");
// testTeam.setDescription("Test Description");
// testTeam.setTeamLead(testUser);
// testTeam = entityManager.persist(testTeam);

// // Create test tag
// testTag = new Tag();
// testTag.setName("Test Tag");
// testTag = entityManager.persist(testTag);
// }

// @Test
// void createTask_WithAssignee_ShouldCreateTask() {
// // Arrange
// String title = "Test Task";
// String shortDescription = "Test Description";
// LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
// TaskPriority priority = TaskPriority.HIGH;

// // Act
// Task createdTask = taskService.createTask(
// title,
// shortDescription,
// null,
// dueDate,
// priority,
// testAssignee.getId(),
// null,
// testUser.getId(),
// Set.of(testTag.getId()));

// // Assert
// assertNotNull(createdTask.getId());
// assertEquals(title, createdTask.getTitle());
// assertEquals(shortDescription, createdTask.getShortDescription());
// assertEquals(dueDate, createdTask.getDueDate());
// assertEquals(priority, createdTask.getPriority());
// assertEquals(TaskStatus.OPEN, createdTask.getStatus());
// assertEquals(testAssignee.getId(), createdTask.getAssignee().getId());
// assertEquals(testUser.getId(), createdTask.getCreatedBy().getId());
// assertEquals(1, createdTask.getTags().size());
// assertTrue(createdTask.getTags().stream().anyMatch(tag ->
// tag.getId().equals(testTag.getId())));
// }

// @Test
// void createTask_WithTeam_ShouldCreateTask() {
// // Arrange
// String title = "Test Task";
// String shortDescription = "Test Description";
// LocalDateTime dueDate = LocalDateTime.now().plusDays(1);
// TaskPriority priority = TaskPriority.HIGH;

// // Act
// Task createdTask = taskService.createTask(
// title,
// shortDescription,
// null,
// dueDate,
// priority,
// null,
// testTeam.getId(),
// testUser.getId(),
// Set.of(testTag.getId()));

// // Assert
// assertNotNull(createdTask.getId());
// assertEquals(title, createdTask.getTitle());
// assertEquals(shortDescription, createdTask.getShortDescription());
// assertEquals(dueDate, createdTask.getDueDate());
// assertEquals(priority, createdTask.getPriority());
// assertEquals(TaskStatus.OPEN, createdTask.getStatus());
// assertEquals(testTeam.getId(), createdTask.getTeam().getId());
// assertEquals(testUser.getId(), createdTask.getCreatedBy().getId());
// assertEquals(1, createdTask.getTags().size());
// assertTrue(createdTask.getTags().stream().anyMatch(tag ->
// tag.getId().equals(testTag.getId())));
// }

// @Test
// void updateTask_ShouldUpdateTask() {
// // Arrange
// Task task = new Task();
// task.setTitle("Original Title");
// task.setShortDescription("Original Description");
// task.setDueDate(LocalDateTime.now().plusDays(1));
// task.setPriority(TaskPriority.MID);
// task.setStatus(TaskStatus.OPEN);
// task.setAssignee(testAssignee);
// task.setCreatedBy(testUser);
// task = entityManager.persist(task);

// // Act
// Task updatedTask = taskService.updateTask(
// task.getId(),
// "Updated Title",
// "Updated Description",
// null,
// LocalDateTime.now().plusDays(2),
// TaskPriority.HIGH,
// TaskStatus.IN_REVIEW,
// testAssignee.getId(),
// null,
// Set.of(testTag.getId()));

// // Assert
// assertEquals("Updated Title", updatedTask.getTitle());
// assertEquals("Updated Description", updatedTask.getShortDescription());
// assertEquals(TaskPriority.HIGH, updatedTask.getPriority());
// assertEquals(TaskStatus.IN_REVIEW, updatedTask.getStatus());
// assertEquals(1, updatedTask.getTags().size());
// assertTrue(updatedTask.getTags().stream().anyMatch(tag ->
// tag.getId().equals(testTag.getId())));
// }

// @Test
// void getTaskById_ShouldReturnTask() {
// // Arrange
// Task task = new Task();
// task.setTitle("Test Task");
// task.setShortDescription("Test Description");
// task.setDueDate(LocalDateTime.now().plusDays(1));
// task.setPriority(TaskPriority.HIGH);
// task.setStatus(TaskStatus.OPEN);
// task.setAssignee(testAssignee);
// task.setCreatedBy(testUser);
// task = entityManager.persist(task);

// // Act
// Task foundTask = taskService.getTaskById(task.getId());

// // Assert
// assertNotNull(foundTask);
// assertEquals(task.getId(), foundTask.getId());
// assertEquals(task.getTitle(), foundTask.getTitle());
// }

// @Test
// void deleteTask_ShouldRemoveTask() {
// // Arrange
// Task task = new Task();
// task.setTitle("Test Task");
// task.setShortDescription("Test Description");
// task.setDueDate(LocalDateTime.now().plusDays(1));
// task.setPriority(TaskPriority.HIGH);
// task.setStatus(TaskStatus.OPEN);
// task.setAssignee(testAssignee);
// task.setCreatedBy(testUser);
// task = entityManager.persist(task);

// // Act
// taskService.deleteTask(task.getId());

// // Assert
// assertFalse(taskRepository.existsById(task.getId()));
// }

// @Test
// void getTasksByAssignee_ShouldReturnTasks() {
// // Arrange
// Task task1 = new Task();
// task1.setTitle("Task 1");
// task1.setShortDescription("Description 1");
// task1.setDueDate(LocalDateTime.now().plusDays(1));
// task1.setPriority(TaskPriority.HIGH);
// task1.setStatus(TaskStatus.OPEN);
// task1.setAssignee(testAssignee);
// task1.setCreatedBy(testUser);
// entityManager.persist(task1);

// Task task2 = new Task();
// task2.setTitle("Task 2");
// task2.setShortDescription("Description 2");
// task2.setDueDate(LocalDateTime.now().plusDays(2));
// task2.setPriority(TaskPriority.MID);
// task2.setStatus(TaskStatus.IN_REVIEW);
// task2.setAssignee(testAssignee);
// task2.setCreatedBy(testUser);
// entityManager.persist(task2);

// // Act
// var tasks = taskService.getTasksByAssignee(testAssignee.getId());

// // Assert
// assertEquals(2, tasks.size());
// assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
// assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 2")));
// }

// @Test
// void getTasksByTeam_ShouldReturnTasks() {
// // Arrange
// Task task1 = new Task();
// task1.setTitle("Task 1");
// task1.setShortDescription("Description 1");
// task1.setDueDate(LocalDateTime.now().plusDays(1));
// task1.setPriority(TaskPriority.HIGH);
// task1.setStatus(TaskStatus.OPEN);
// task1.setTeam(testTeam);
// task1.setCreatedBy(testUser);
// entityManager.persist(task1);

// Task task2 = new Task();
// task2.setTitle("Task 2");
// task2.setShortDescription("Description 2");
// task2.setDueDate(LocalDateTime.now().plusDays(2));
// task2.setPriority(TaskPriority.MID);
// task2.setStatus(TaskStatus.IN_REVIEW);
// task2.setTeam(testTeam);
// task2.setCreatedBy(testUser);
// entityManager.persist(task2);

// // Act
// var tasks = taskService.getTasksByTeam(testTeam.getId());

// // Assert
// assertEquals(2, tasks.size());
// assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 1")));
// assertTrue(tasks.stream().anyMatch(t -> t.getTitle().equals("Task 2")));
// }

// @Test
// void getTaskStatsByUser_ShouldReturnCorrectStats() {
// // Arrange
// Task openTask = new Task();
// openTask.setTitle("Open Task");
// openTask.setShortDescription("Description");
// openTask.setDueDate(LocalDateTime.now().plusDays(1));
// openTask.setPriority(TaskPriority.HIGH);
// openTask.setStatus(TaskStatus.OPEN);
// openTask.setAssignee(testAssignee);
// openTask.setCreatedBy(testUser);
// entityManager.persist(openTask);

// Task inProgressTask = new Task();
// inProgressTask.setTitle("In Progress Task");
// inProgressTask.setShortDescription("Description");
// inProgressTask.setDueDate(LocalDateTime.now().plusDays(2));
// inProgressTask.setPriority(TaskPriority.MID);
// inProgressTask.setStatus(TaskStatus.IN_REVIEW);
// inProgressTask.setAssignee(testAssignee);
// inProgressTask.setCreatedBy(testUser);
// entityManager.persist(inProgressTask);

// Task closedTask = new Task();
// closedTask.setTitle("Closed Task");
// closedTask.setShortDescription("Description");
// closedTask.setDueDate(LocalDateTime.now().plusDays(3));
// closedTask.setPriority(TaskPriority.LOW);
// closedTask.setStatus(TaskStatus.CLOSED);
// closedTask.setAssignee(testAssignee);
// closedTask.setCreatedBy(testUser);
// entityManager.persist(closedTask);

// // Act
// var stats = taskService.getTaskStatsByUser(testAssignee.getId());

// // Assert
// assertEquals(1L, stats.get(TaskStatus.OPEN));
// assertEquals(1L, stats.get(TaskStatus.IN_REVIEW));
// assertEquals(1L, stats.get(TaskStatus.CLOSED));
// }
// }