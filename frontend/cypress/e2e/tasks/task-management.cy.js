/**
 * Task Management Tests
 * Tests for task creation, editing, deletion, and viewing
 */
describe("Task Management", () => {
  beforeEach(() => {
    // Login before each test
    cy.loginViaAPI();
    cy.visit("/");
  });

  afterEach(() => {
    // Clean up created tasks after each test
    cy.clearUserTasks();
  });

  describe("Task Creation", () => {
    it("should create a task with all fields filled", () => {
      cy.get('[data-cy="create-task-button"]').click();
      cy.url().should("include", "/task/create");

      const taskData = {
        title: "Complete Task Test",
        shortDescription: "A comprehensive test task",
        longDescription:
          "This task includes all possible fields to test the complete functionality",
        priority: "URGENT",
        dueDate: "2025-12-25T15:30",
      };

      // Fill all form fields
      cy.get('[data-cy="task-title-input"]').type(taskData.title);
      cy.get('[data-cy="task-short-description-input"]').type(
        taskData.shortDescription
      );
      cy.get('[data-cy="task-long-description-input"]').type(
        taskData.longDescription
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Urgent").click();
      cy.get('[data-cy="task-due-date-input"]').type(taskData.dueDate);

      // Submit form
      cy.get('[data-cy="create-task-button"]').click();

      // Verify success
      cy.contains("Task created successfully!").should("be.visible");
      cy.url().should("eq", Cypress.config().baseUrl + "/");

      // Verify task appears in dashboard
      cy.get('[data-cy="tasks-table"]').should("be.visible");
      cy.contains(taskData.title, { timeout: 10000 }).should("be.visible");
      cy.contains(taskData.shortDescription).should("be.visible");
      cy.contains("URGENT").should("be.visible");
    });

    it("should create a task with only required fields", () => {
      cy.get('[data-cy="create-task-button"]').click();

      const minimalTask = {
        title: "Minimal Task",
        shortDescription: "Only required fields",
        priority: "LOW",
        dueDate: "2025-06-15T09:00",
      };

      cy.get('[data-cy="task-title-input"]').type(minimalTask.title);
      cy.get('[data-cy="task-short-description-input"]').type(
        minimalTask.shortDescription
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Low").click();
      cy.get('[data-cy="task-due-date-input"]').type(minimalTask.dueDate);

      cy.get('[data-cy="create-task-button"]').click();

      cy.contains("Task created successfully!").should("be.visible");
      cy.url().should("eq", Cypress.config().baseUrl + "/");
      cy.contains(minimalTask.title, { timeout: 10000 }).should("be.visible");
    });

    it("should validate required fields", () => {
      cy.get('[data-cy="create-task-button"]').click();

      // Try to submit empty form
      cy.get('[data-cy="create-task-button"]').click();

      // Check validation messages
      cy.contains("Task title is required").should("be.visible");
      cy.contains("Short description is required").should("be.visible");
      cy.contains("Priority is required").should("be.visible");
      cy.contains("Due date is required").should("be.visible");

      // Should remain on form page
      cy.url().should("include", "/task/create");
    });

    it("should handle different priority levels", () => {
      const priorities = ["Low", "Medium", "High", "Urgent"];
      const priorityValues = ["LOW", "MID", "HIGH", "URGENT"];

      priorities.forEach((priority, index) => {
        cy.get('[data-cy="create-task-button"]').click();

        cy.get('[data-cy="task-title-input"]').type(
          `${priority} Priority Task`
        );
        cy.get('[data-cy="task-short-description-input"]').type(
          `Testing ${priority} priority`
        );
        cy.get('[data-cy="task-priority-select"]').click();
        cy.contains(priority).click();
        cy.get('[data-cy="task-due-date-input"]').type("2025-07-01T12:00");

        cy.get('[data-cy="create-task-button"]').click();
        cy.contains("Task created successfully!").should("be.visible");
        cy.url().should("eq", Cypress.config().baseUrl + "/");

        // Verify priority is displayed correctly
        cy.contains(`${priority} Priority Task`, { timeout: 10000 }).should(
          "be.visible"
        );
        cy.contains(priorityValues[index]).should("be.visible");
      });
    });
  });

  describe("Task Viewing", () => {
    beforeEach(() => {
      // Create test tasks via API for viewing tests
      cy.createTaskViaAPI({
        title: "View Test Task 1",
        shortDescription: "First test task for viewing",
        priority: "HIGH",
      });
      cy.createTaskViaAPI({
        title: "View Test Task 2",
        shortDescription: "Second test task for viewing",
        priority: "LOW",
      });
    });

    it("should display tasks in the dashboard table", () => {
      cy.visit("/");

      // Wait for tasks to load
      cy.get('[data-cy="tasks-table"]').should("be.visible");

      // Check that both tasks are visible
      cy.contains("View Test Task 1", { timeout: 10000 }).should("be.visible");
      cy.contains("View Test Task 2").should("be.visible");

      // Check task details
      cy.contains("First test task for viewing").should("be.visible");
      cy.contains("Second test task for viewing").should("be.visible");
      cy.contains("HIGH").should("be.visible");
      cy.contains("LOW").should("be.visible");
    });

    it("should show task actions (edit/delete) for each task", () => {
      cy.visit("/");
      cy.get('[data-cy="tasks-table"]').should("be.visible");

      // Wait for tasks to load
      cy.contains("View Test Task 1", { timeout: 10000 }).should("be.visible");

      // Check that action buttons are present
      cy.get('[data-cy^="edit-task-"]').should("have.length.at.least", 2);
      cy.get('[data-cy^="delete-task-"]').should("have.length.at.least", 2);
    });

    it("should handle empty task list gracefully", () => {
      // Clear all tasks first
      cy.clearUserTasks();
      cy.visit("/");

      // Should show appropriate message
      cy.get("#no-tasks-alert").should("be.visible");
      cy.contains("No tasks available at the moment").should("be.visible");
    });
  });

  describe("Task Editing", () => {
    let taskId;

    beforeEach(() => {
      // Create a test task for editing
      cy.createTaskViaAPI({
        title: "Task to Edit",
        shortDescription: "This task will be edited",
        priority: "MID",
      }).then((task) => {
        taskId = task.id;
      });
      cy.visit("/");
    });

    it("should open edit dialog when edit button is clicked", () => {
      cy.get('[data-cy="tasks-table"]').should("be.visible");
      cy.contains("Task to Edit", { timeout: 10000 }).should("be.visible");

      // Click edit button
      cy.get(`[data-cy="edit-task-${taskId}"]`).click();

      // Edit dialog should open
      cy.get("#edit-task-form").should("be.visible");
      cy.get('[data-cy="edit-task-title-input"]').should(
        "have.value",
        "Task to Edit"
      );
      cy.get('[data-cy="edit-task-description-input"]').should(
        "have.value",
        "This task will be edited"
      );
    });

    it("should update task when edit form is submitted", () => {
      cy.get('[data-cy="tasks-table"]').should("be.visible");
      cy.contains("Task to Edit", { timeout: 10000 }).should("be.visible");

      // Open edit dialog
      cy.get(`[data-cy="edit-task-${taskId}"]`).click();

      // Modify task details
      cy.get('[data-cy="edit-task-title-input"]')
        .clear()
        .type("Updated Task Title");
      cy.get('[data-cy="edit-task-description-input"]')
        .clear()
        .type("Updated task description");

      // Save changes
      cy.get("#save-edit-btn").click();

      // Verify changes are reflected
      cy.contains("Updated Task Title", { timeout: 10000 }).should(
        "be.visible"
      );
      cy.contains("Updated task description").should("be.visible");
    });

    it("should cancel edit without saving changes", () => {
      cy.get('[data-cy="tasks-table"]').should("be.visible");
      cy.contains("Task to Edit", { timeout: 10000 }).should("be.visible");

      // Open edit dialog
      cy.get(`[data-cy="edit-task-${taskId}"]`).click();

      // Modify task details
      cy.get('[data-cy="edit-task-title-input"]')
        .clear()
        .type("Should Not Save");

      // Cancel edit
      cy.get("#cancel-edit-btn").click();

      // Original title should still be visible
      cy.contains("Task to Edit").should("be.visible");
      cy.contains("Should Not Save").should("not.exist");
    });
  });

  describe("Task Deletion", () => {
    let taskId;

    beforeEach(() => {
      // Create a test task for deletion
      cy.createTaskViaAPI({
        title: "Task to Delete",
        shortDescription: "This task will be deleted",
        priority: "LOW",
      }).then((task) => {
        taskId = task.id;
      });
      cy.visit("/");
    });

    it("should delete task when delete button is clicked", () => {
      cy.get('[data-cy="tasks-table"]').should("be.visible");
      cy.contains("Task to Delete", { timeout: 10000 }).should("be.visible");

      // Click delete button
      cy.get(`[data-cy="delete-task-${taskId}"]`).click();

      // Task should be removed from the list
      cy.contains("Task to Delete").should("not.exist");
    });
  });

  describe("Task Status and Priority Display", () => {
    beforeEach(() => {
      // Create tasks with different statuses and priorities
      cy.createTaskViaAPI({
        title: "High Priority Task",
        shortDescription: "High priority test",
        priority: "HIGH",
      });
      cy.createTaskViaAPI({
        title: "Low Priority Task",
        shortDescription: "Low priority test",
        priority: "LOW",
      });
    });

    it("should display priority chips with correct colors", () => {
      cy.visit("/");
      cy.get('[data-cy="tasks-table"]').should("be.visible");

      // Wait for tasks to load
      cy.contains("High Priority Task", { timeout: 10000 }).should(
        "be.visible"
      );

      // Check priority chips are displayed
      cy.get('[data-cy="task-priority"]').should("contain", "HIGH");
      cy.get('[data-cy="task-priority"]').should("contain", "LOW");
    });

    it("should display status chips correctly", () => {
      cy.visit("/");
      cy.get('[data-cy="tasks-table"]').should("be.visible");

      // Wait for tasks to load
      cy.contains("High Priority Task", { timeout: 10000 }).should(
        "be.visible"
      );

      // New tasks should have OPEN status
      cy.get('[data-cy="task-status"]').should("contain", "OPEN");
    });
  });

  describe("Form Validation and Error Handling", () => {
    it("should prevent submission with invalid data", () => {
      cy.get('[data-cy="create-task-button"]').click();

      // Fill with invalid data
      cy.get('[data-cy="task-title-input"]').type("   "); // Only spaces
      cy.get('[data-cy="task-short-description-input"]').type(
        "Valid description"
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Medium").click();
      cy.get('[data-cy="task-due-date-input"]').type("2025-06-01T12:00");

      cy.get('[data-cy="create-task-button"]').click();

      // Should show validation error for empty title
      cy.contains("Task title is required").should("be.visible");
      cy.url().should("include", "/task/create");
    });

    it("should handle API errors gracefully", () => {
      // Intercept API call and force error
      cy.intercept("POST", "**/api/v1/tasks", {
        statusCode: 400,
        body: {
          success: false,
          message: "Validation failed",
          data: null,
        },
      }).as("createTaskError");

      cy.get('[data-cy="create-task-button"]').click();

      cy.get('[data-cy="task-title-input"]').type("API Error Test");
      cy.get('[data-cy="task-short-description-input"]').type(
        "Testing API error handling"
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Medium").click();
      cy.get('[data-cy="task-due-date-input"]').type("2025-06-01T12:00");

      cy.get('[data-cy="create-task-button"]').click();

      cy.wait("@createTaskError");

      // Should show error message
      cy.get(".v-alert").should("be.visible");
      cy.url().should("include", "/task/create");
    });
  });
});
