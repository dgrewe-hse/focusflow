package de.hse.focusflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TaskDTO {
  private Long id;

  @NotBlank(message = "Title is required")
  private String title;

  private String description;

  @NotNull(message = "Due date is required")
  private LocalDate dueDate;

  @NotNull(message = "Priority is required")
  private String priority;

  private String status;
  private Long assigneeId;
  private Long teamId;
}
