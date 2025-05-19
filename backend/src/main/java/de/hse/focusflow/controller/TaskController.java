package de.hse.focusflow.controller;

import de.hse.focusflow.dto.TaskDTO;
import de.hse.focusflow.model.Task;
import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import de.hse.focusflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Task Management", description = "APIs for managing tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

  @Autowired
  private TaskService taskService;

  /**
   * Maps Task entity to TaskDTO
   */
  private TaskDTO mapToDTO(Task task) {
    return TaskDTO.builder()
        .id(task.getId())
        .title(task.getTitle())
        .shortDescription(task.getShortDescription())
        .longDescription(task.getLongDescription())
        .dueDate(task.getDueDate())
        .priority(task.getPriority())
        .status(task.getStatus())
        .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
        .teamId(task.getTeam() != null ? task.getTeam().getId() : null)
        .createdById(task.getCreatedBy() != null ? task.getCreatedBy().getId() : null)
        .tagIds(
            task.getTags() != null ? task.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toSet()) : null)
        .build();
  }

  /**
   * Maps a list of Task entities to TaskDTOs
   */
  private List<TaskDTO> mapToDTOList(List<Task> tasks) {
    return tasks.stream()
        .map(this::mapToDTO)
        .collect(Collectors.toList());
  }

  @GetMapping
  @Operation(summary = "Get all tasks")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved all tasks", content = @Content(schema = @Schema(implementation = TaskDTO.class)))
  })
  @Cacheable(value = "tasks")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<List<TaskDTO>>> getAllTasks() {
    List<Task> tasks = taskService.searchTasks(null, null, null, null);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTOList(tasks)));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get task by ID")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved task", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
  })
  @Cacheable(value = "task", key = "#id")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<TaskDTO>> getTaskById(@PathVariable UUID id) {
    Task task = taskService.getTaskById(id);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTO(task)));
  }

  @PostMapping
  @Operation(summary = "Create a new task")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(value = { "tasks", "task" }, allEntries = true)
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<TaskDTO>> createTask(@Valid @RequestBody TaskDTO taskDTO) {
    Task task = taskService.createTask(
        taskDTO.getTitle(),
        taskDTO.getShortDescription(),
        taskDTO.getLongDescription(),
        taskDTO.getDueDate(),
        taskDTO.getPriority(),
        taskDTO.getAssigneeId(),
        taskDTO.getTeamId(),
        taskDTO.getCreatedById(),
        taskDTO.getTagIds());

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(de.hse.focusflow.dto.ApiResponse.success(mapToDTO(task)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing task")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(value = { "tasks", "task" }, allEntries = true)
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<TaskDTO>> updateTask(
      @PathVariable UUID id,
      @Valid @RequestBody TaskDTO taskDTO) {

    Task task = taskService.updateTask(
        id,
        taskDTO.getTitle(),
        taskDTO.getShortDescription(),
        taskDTO.getLongDescription(),
        taskDTO.getDueDate(),
        taskDTO.getPriority(),
        taskDTO.getStatus(),
        taskDTO.getAssigneeId(),
        taskDTO.getTeamId(),
        taskDTO.getTagIds());

    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTO(task)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a task")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Task deleted successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
  })
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(value = { "tasks", "task" }, allEntries = true)
  public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
    taskService.deleteTask(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/search")
  @Operation(summary = "Search tasks with filters")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved filtered tasks", content = @Content(schema = @Schema(implementation = TaskDTO.class)))
  })
  @Cacheable(value = "filteredTasks", key = "{#title, #status, #assigneeId, #priority}")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<List<TaskDTO>>> searchTasks(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) UUID assigneeId,
      @RequestParam(required = false) TaskPriority priority) {

    List<Task> tasks = taskService.searchTasks(title, status, assigneeId, priority);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTOList(tasks)));
  }

  @GetMapping("/assignee/{userId}")
  @Operation(summary = "Get tasks by assignee")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved tasks by assignee", content = @Content(schema = @Schema(implementation = TaskDTO.class)))
  })
  @Cacheable(value = "tasksByAssignee", key = "#userId")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<List<TaskDTO>>> getTasksByAssignee(@PathVariable UUID userId) {
    List<Task> tasks = taskService.getTasksByAssignee(userId);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTOList(tasks)));
  }

  @GetMapping("/team/{teamId}")
  @Operation(summary = "Get tasks by team")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved tasks by team", content = @Content(schema = @Schema(implementation = TaskDTO.class)))
  })
  @Cacheable(value = "tasksByTeam", key = "#teamId")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<List<TaskDTO>>> getTasksByTeam(@PathVariable UUID teamId) {
    List<Task> tasks = taskService.getTasksByTeam(teamId);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTOList(tasks)));
  }

  @GetMapping("/creator/{userId}")
  @Operation(summary = "Get tasks by creator")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved tasks by creator", content = @Content(schema = @Schema(implementation = TaskDTO.class)))
  })
  @Cacheable(value = "tasksByCreator", key = "#userId")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<List<TaskDTO>>> getTasksByCreator(@PathVariable UUID userId) {
    List<Task> tasks = taskService.getTasksByCreator(userId);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTOList(tasks)));
  }

  @GetMapping("/upcoming/{days}")
  @Operation(summary = "Get upcoming tasks")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved upcoming tasks", content = @Content(schema = @Schema(implementation = TaskDTO.class)))
  })
  @Cacheable(value = "upcomingTasks", key = "#days")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<List<TaskDTO>>> getUpcomingTasks(@PathVariable int days) {
    List<Task> tasks = taskService.getUpcomingTasks(days);
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTOList(tasks)));
  }

  @GetMapping("/stats/user/{userId}")
  @Operation(summary = "Get task statistics by user")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved task statistics by user")
  })
  @Cacheable(value = "userTaskStats", key = "#userId")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<Map<TaskStatus, Long>>> getTaskStatsByUser(
      @PathVariable UUID userId) {
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(taskService.getTaskStatsByUser(userId)));
  }

  @GetMapping("/stats/team/{teamId}")
  @Operation(summary = "Get task statistics by team")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved task statistics by team")
  })
  @Cacheable(value = "teamTaskStats", key = "#teamId")
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<Map<TaskStatus, Long>>> getTaskStatsByTeam(
      @PathVariable UUID teamId) {
    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(taskService.getTaskStatsByTeam(teamId)));
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Update task status")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task status updated successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid status")
  })
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(value = { "tasks", "task" }, allEntries = true)
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<TaskDTO>> updateTaskStatus(
      @PathVariable UUID id,
      @RequestParam TaskStatus status) {

    Task task = taskService.getTaskById(id);
    task = taskService.updateTask(
        id,
        task.getTitle(),
        task.getShortDescription(),
        task.getLongDescription(),
        task.getDueDate(),
        task.getPriority(),
        status,
        task.getAssignee() != null ? task.getAssignee().getId() : null,
        task.getTeam() != null ? task.getTeam().getId() : null,
        task.getTags() != null ? task.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toSet()) : null);

    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTO(task)));
  }

  @PatchMapping("/{id}/assignee")
  @Operation(summary = "Update task assignee")
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task assignee updated successfully", content = @Content(schema = @Schema(implementation = TaskDTO.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
  })
  @PreAuthorize("hasRole('USER')")
  @CacheEvict(value = { "tasks", "task" }, allEntries = true)
  public ResponseEntity<de.hse.focusflow.dto.ApiResponse<TaskDTO>> updateTaskAssignee(
      @PathVariable UUID id,
      @RequestParam(required = false) UUID assigneeId) {

    Task task = taskService.getTaskById(id);
    task = taskService.updateTask(
        id,
        task.getTitle(),
        task.getShortDescription(),
        task.getLongDescription(),
        task.getDueDate(),
        task.getPriority(),
        task.getStatus(),
        assigneeId,
        task.getTeam() != null ? task.getTeam().getId() : null,
        task.getTags() != null ? task.getTags().stream().map(tag -> tag.getId()).collect(Collectors.toSet()) : null);

    return ResponseEntity.ok(de.hse.focusflow.dto.ApiResponse.success(mapToDTO(task)));
  }
}
