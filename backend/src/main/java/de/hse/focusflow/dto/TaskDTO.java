package de.hse.focusflow.dto;

import de.hse.focusflow.model.TaskPriority;
import de.hse.focusflow.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
  private UUID id;

  @NotBlank(message = "Title is required")
  @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
  private String title;

  @NotBlank(message = "Short description is required")
  @Size(max = 200, message = "Short description must be less than 200 characters")
  private String shortDescription;

  private String longDescription;

  @NotNull(message = "Due date is required")
  private LocalDateTime dueDate;

  @NotNull(message = "Priority is required")
  private TaskPriority priority;

  private TaskStatus status;
  private UUID assigneeId;
  private UUID teamId;
  private UUID createdById;
  private Set<UUID> tagIds;
}
