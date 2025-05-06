package de.hse.focusflow.controller;

import de.hse.focusflow.dto.ApiResponse;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

  @Autowired private TaskService taskService;

  @GetMapping
  @Operation(summary = "Get all tasks")
  @Cacheable(value = "tasks")
  public ResponseEntity<ApiResponse<List<Task>>> getAllTasks() {
    return ResponseEntity.ok(ApiResponse.success(taskService.searchTasks(null, null, null, null)));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get task by ID")
  @Cacheable(value = "task", key = "#id")
  public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable UUID id) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getTaskById(id)));
  }

  @PostMapping
  @Operation(summary = "Create a new task")
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(
      value = {"tasks", "task"},
      allEntries = true)
  public ResponseEntity<ApiResponse<Task>> createTask(
      @RequestParam String title,
      @RequestParam String shortDescription,
      @RequestParam(required = false) String longDescription,
      @RequestParam LocalDateTime dueDate,
      @RequestParam TaskPriority priority,
      @RequestParam(required = false) UUID assigneeId,
      @RequestParam(required = false) UUID teamId,
      @RequestParam UUID createdById,
      @RequestParam(required = false) Set<UUID> tagIds) {

    Task task =
        taskService.createTask(
            title,
            shortDescription,
            longDescription,
            dueDate,
            priority,
            assigneeId,
            teamId,
            createdById,
            tagIds);
    return ResponseEntity.ok(ApiResponse.success(task));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing task")
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(
      value = {"tasks", "task"},
      allEntries = true)
  public ResponseEntity<ApiResponse<Task>> updateTask(
      @PathVariable UUID id,
      @RequestParam(required = false) String title,
      @RequestParam(required = false) String shortDescription,
      @RequestParam(required = false) String longDescription,
      @RequestParam(required = false) LocalDateTime dueDate,
      @RequestParam(required = false) TaskPriority priority,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) UUID assigneeId,
      @RequestParam(required = false) UUID teamId,
      @RequestParam(required = false) Set<UUID> tagIds) {

    Task task =
        taskService.updateTask(
            id,
            title,
            shortDescription,
            longDescription,
            dueDate,
            priority,
            status,
            assigneeId,
            teamId,
            tagIds);
    return ResponseEntity.ok(ApiResponse.success(task));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a task")
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(
      value = {"tasks", "task"},
      allEntries = true)
  public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
    taskService.deleteTask(id);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/search")
  @Operation(summary = "Search tasks with filters")
  @Cacheable(value = "filteredTasks", key = "{#title, #status, #assigneeId, #priority}")
  public ResponseEntity<ApiResponse<List<Task>>> searchTasks(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) UUID assigneeId,
      @RequestParam(required = false) TaskPriority priority) {

    return ResponseEntity.ok(
        ApiResponse.success(taskService.searchTasks(title, status, assigneeId, priority)));
  }

  @GetMapping("/assignee/{userId}")
  @Operation(summary = "Get tasks by assignee")
  @Cacheable(value = "tasksByAssignee", key = "#userId")
  public ResponseEntity<ApiResponse<List<Task>>> getTasksByAssignee(@PathVariable UUID userId) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByAssignee(userId)));
  }

  @GetMapping("/team/{teamId}")
  @Operation(summary = "Get tasks by team")
  @Cacheable(value = "tasksByTeam", key = "#teamId")
  public ResponseEntity<ApiResponse<List<Task>>> getTasksByTeam(@PathVariable UUID teamId) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByTeam(teamId)));
  }

  @GetMapping("/creator/{userId}")
  @Operation(summary = "Get tasks by creator")
  @Cacheable(value = "tasksByCreator", key = "#userId")
  public ResponseEntity<ApiResponse<List<Task>>> getTasksByCreator(@PathVariable UUID userId) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getTasksByCreator(userId)));
  }

  @GetMapping("/upcoming/{days}")
  @Operation(summary = "Get upcoming tasks")
  @Cacheable(value = "upcomingTasks", key = "#days")
  public ResponseEntity<ApiResponse<List<Task>>> getUpcomingTasks(@PathVariable int days) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getUpcomingTasks(days)));
  }

  @GetMapping("/stats/user/{userId}")
  @Operation(summary = "Get task statistics by user")
  @Cacheable(value = "userTaskStats", key = "#userId")
  public ResponseEntity<ApiResponse<Map<TaskStatus, Long>>> getTaskStatsByUser(
      @PathVariable UUID userId) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getTaskStatsByUser(userId)));
  }

  @GetMapping("/stats/team/{teamId}")
  @Operation(summary = "Get task statistics by team")
  @Cacheable(value = "teamTaskStats", key = "#teamId")
  public ResponseEntity<ApiResponse<Map<TaskStatus, Long>>> getTaskStatsByTeam(
      @PathVariable UUID teamId) {
    return ResponseEntity.ok(ApiResponse.success(taskService.getTaskStatsByTeam(teamId)));
  }
}
