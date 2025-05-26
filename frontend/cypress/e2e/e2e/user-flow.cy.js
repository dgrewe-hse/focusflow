/**
 * End-to-End User Flow Tests
 * Complete user journey: Login -> Create Task -> View Task in Dashboard
 */
describe("Complete User Flow", () => {
  beforeEach(() => {
    // Clear any existing authentication and data
    cy.window().then((win) => {
      win.localStorage.clear();
    });
  });

  afterEach(() => {
    // Clean up created tasks after each test
    cy.clearUserTasks();
  });

  describe("Full User Journey", () => {
    it("should complete the full user flow: login -> create task -> view task", () => {
      // Step 1: User visits the app and gets redirected to login
      cy.visit("/");
      cy.url().should("include", "/login");

      // Step 2: User logs in
      cy.get('[data-cy="email-input"]').type(Cypress.env("TEST_EMAIL"));
      cy.get('[data-cy="password-input"]').type(Cypress.env("TEST_PASSWORD"));
      cy.get('[data-cy="login-button"]').click();

      // Step 3: User is redirected to dashboard
      cy.url().should("eq", Cypress.config().baseUrl + "/");

      // Verify user is logged in (check header)
      cy.get(".user-email").should("contain", Cypress.env("TEST_EMAIL"));
      cy.get('[data-cy="logout-button"]').should("be.visible");

      // Step 4: User clicks create task button
      cy.get('[data-cy="create-task-button"]').should("be.visible").click();
      cy.url().should("include", "/task/create");

      // Step 5: User fills out task creation form
      const taskData = {
        title: "E2E Test Task",
        shortDescription: "This is a test task created during E2E testing",
        longDescription:
          "This task was created to verify the complete user flow from login to task creation and viewing.",
        priority: "HIGH",
        dueDate: "2025-12-31T23:59",
      };

      cy.get('[data-cy="task-title-input"]').type(taskData.title);
      cy.get('[data-cy="task-short-description-input"]').type(
        taskData.shortDescription
      );
      cy.get('[data-cy="task-long-description-input"]').type(
        taskData.longDescription
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("High").click();
      cy.get('[data-cy="task-due-date-input"]').type(taskData.dueDate);

      // Step 6: User submits the task
      cy.get('[data-cy="create-task-button"]').click();

      // Step 7: User sees success message and gets redirected to dashboard
      cy.contains("Task created successfully!").should("be.visible");
      cy.url().should("eq", Cypress.config().baseUrl + "/", { timeout: 10000 });

      // Step 8: User sees the newly created task in the dashboard
      cy.get('[data-cy="tasks-table"]').should("be.visible");

      // Wait for the task to appear in the table
      cy.contains(taskData.title, { timeout: 10000 }).should("be.visible");

      // Verify task details are displayed correctly
      cy.get('[data-cy="tasks-table"]').within(() => {
        cy.contains(taskData.title).should("be.visible");
        cy.contains(taskData.shortDescription).should("be.visible");
        cy.contains("HIGH").should("be.visible");
        cy.contains("OPEN").should("be.visible"); // Default status
      });

      // Step 9: User can interact with the task (edit/delete buttons should be visible)
      cy.get('[data-cy="tasks-table"]').within(() => {
        cy.contains(taskData.title)
          .parents("tr")
          .within(() => {
            cy.get('[data-cy^="edit-task-"]').should("be.visible");
            cy.get('[data-cy^="delete-task-"]').should("be.visible");
          });
      });
    });

    it("should handle task creation with minimal required fields", () => {
      // Login first
      cy.loginViaUI();

      // Navigate to task creation
      cy.get('[data-cy="create-task-button"]').click();

      // Fill only required fields
      const minimalTask = {
        title: "Minimal Test Task",
        shortDescription: "Just the required fields",
        priority: "LOW",
        dueDate: "2025-06-01T12:00",
      };

      cy.get('[data-cy="task-title-input"]').type(minimalTask.title);
      cy.get('[data-cy="task-short-description-input"]').type(
        minimalTask.shortDescription
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Low").click();
      cy.get('[data-cy="task-due-date-input"]').type(minimalTask.dueDate);

      // Submit task
      cy.get('[data-cy="create-task-button"]').click();

      // Verify success and redirection
      cy.contains("Task created successfully!").should("be.visible");
      cy.url().should("eq", Cypress.config().baseUrl + "/");

      // Verify task appears in dashboard
      cy.contains(minimalTask.title, { timeout: 10000 }).should("be.visible");
    });

    it("should prevent task creation with invalid data", () => {
      // Login first
      cy.loginViaUI();

      // Navigate to task creation
      cy.get('[data-cy="create-task-button"]').click();

      // Try to submit empty form
      cy.get('[data-cy="create-task-button"]').click();

      // Should show validation errors
      cy.contains("Task title is required").should("be.visible");
      cy.contains("Short description is required").should("be.visible");
      cy.contains("Priority is required").should("be.visible");
      cy.contains("Due date is required").should("be.visible");

      // Should remain on task creation page
      cy.url().should("include", "/task/create");
    });

    it("should allow user to cancel task creation and return to dashboard", () => {
      // Login first
      cy.loginViaUI();

      // Navigate to task creation
      cy.get('[data-cy="create-task-button"]').click();

      // Fill some data
      cy.get('[data-cy="task-title-input"]').type("Task to be cancelled");

      // Cancel task creation
      cy.get('[data-cy="cancel-task-button"]').click();

      // Should return to dashboard
      cy.url().should("eq", Cypress.config().baseUrl + "/");

      // Task should not be created
      cy.contains("Task to be cancelled").should("not.exist");
    });
  });

  describe("Authentication State Management", () => {
    it("should redirect unauthenticated users to login", () => {
      // Try to access protected routes without authentication
      cy.visit("/task/create");
      cy.url().should("include", "/login");

      cy.visit("/");
      cy.url().should("include", "/login");
    });

    it("should maintain authentication state across page refreshes", () => {
      // Login
      cy.loginViaUI();
      cy.url().should("eq", Cypress.config().baseUrl + "/");

      // Refresh page
      cy.reload();

      // Should remain authenticated
      cy.url().should("eq", Cypress.config().baseUrl + "/");
      cy.get(".user-email").should("contain", Cypress.env("TEST_EMAIL"));
    });

    it("should handle logout correctly", () => {
      // Login first
      cy.loginViaUI();

      // Logout
      cy.get('[data-cy="logout-button"]').click();

      // Should redirect to login page
      cy.url().should("include", "/login");

      // Should clear authentication data
      cy.window().then((win) => {
        expect(win.localStorage.getItem("token")).to.be.null;
        expect(win.localStorage.getItem("user")).to.be.null;
      });

      // Trying to access protected routes should redirect to login
      cy.visit("/");
      cy.url().should("include", "/login");
    });
  });

  describe("Error Handling", () => {
    it("should handle backend errors gracefully during task creation", () => {
      // Login first
      cy.loginViaUI();

      // Navigate to task creation
      cy.get('[data-cy="create-task-button"]').click();

      // Intercept API call and force it to fail
      cy.intercept("POST", "**/api/v1/tasks", {
        statusCode: 500,
        body: {
          success: false,
          message: "Internal server error",
          data: null,
        },
      }).as("createTaskError");

      // Fill and submit form
      cy.get('[data-cy="task-title-input"]').type("Error Test Task");
      cy.get('[data-cy="task-short-description-input"]').type(
        "This should fail"
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Medium").click();
      cy.get('[data-cy="task-due-date-input"]').type("2025-06-01T12:00");
      cy.get('[data-cy="create-task-button"]').click();

      // Wait for API call
      cy.wait("@createTaskError");

      // Should show error message
      cy.get(".v-alert")
        .should("be.visible")
        .and("contain", "Failed to create task");

      // Should remain on task creation page
      cy.url().should("include", "/task/create");
    });

    it("should handle network errors during login", () => {
      cy.visit("/login");

      // Intercept login API call and force network error
      cy.intercept("POST", "**/api/v1/auth/login", {
        forceNetworkError: true,
      }).as("loginNetworkError");

      // Try to login
      cy.get('[data-cy="email-input"]').type(Cypress.env("TEST_EMAIL"));
      cy.get('[data-cy="password-input"]').type(Cypress.env("TEST_PASSWORD"));
      cy.get('[data-cy="login-button"]').click();

      // Wait for API call
      cy.wait("@loginNetworkError");

      // Should show error message
      cy.get(".v-snackbar").should("be.visible");

      // Should remain on login page
      cy.url().should("include", "/login");
    });
  });

  describe("Performance and UX", () => {
    it("should show loading states during async operations", () => {
      cy.visit("/login");

      // Check login loading state
      cy.get('[data-cy="email-input"]').type(Cypress.env("TEST_EMAIL"));
      cy.get('[data-cy="password-input"]').type(Cypress.env("TEST_PASSWORD"));
      cy.get('[data-cy="login-button"]').click();
      cy.get('[data-cy="login-button"]').should("have.class", "v-btn--loading");

      // Wait for login to complete
      cy.url().should("eq", Cypress.config().baseUrl + "/");

      // Check task creation loading state
      cy.get('[data-cy="create-task-button"]').click();
      cy.get('[data-cy="task-title-input"]').type("Loading Test Task");
      cy.get('[data-cy="task-short-description-input"]').type(
        "Testing loading states"
      );
      cy.get('[data-cy="task-priority-select"]').click();
      cy.contains("Medium").click();
      cy.get('[data-cy="task-due-date-input"]').type("2025-06-01T12:00");
      cy.get('[data-cy="create-task-button"]').click();
      cy.get('[data-cy="create-task-button"]').should(
        "have.class",
        "v-btn--loading"
      );
    });

    it("should be responsive on different screen sizes", () => {
      // Test on mobile viewport
      cy.viewport(375, 667);
      cy.loginViaUI();

      // Elements should be visible and usable on mobile
      cy.get('[data-cy="create-task-button"]').should("be.visible");
      cy.get(".header-logo").should("be.visible");
      cy.get('[data-cy="logout-button"]').should("be.visible");

      // Test on tablet viewport
      cy.viewport(768, 1024);
      cy.get('[data-cy="tasks-table"]').should("be.visible");

      // Test on desktop viewport
      cy.viewport(1280, 720);
      cy.get('[data-cy="tasks-table"]').should("be.visible");
    });
  });
});
