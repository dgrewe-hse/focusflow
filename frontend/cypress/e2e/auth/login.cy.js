/**
 * Login Page Tests
 * Tests for the authentication login functionality
 */
describe("Login Page", () => {
  beforeEach(() => {
    // Clear any existing authentication
    cy.clearAuth();

    // Visit login page
    cy.visit("/login");
  });

  describe("UI Elements", () => {
    it("should display all login form elements", () => {
      // Check header elements
      cy.get(".header-logo").should("be.visible");
      cy.get(".header-title").should("contain", "FocusFlow");

      // Check form elements
      cy.get('[data-cy="email-input"]').should("be.visible");
      cy.get('[data-cy="password-input"]').should("be.visible");
      cy.get('[data-cy="login-button"]')
        .should("be.visible")
        .and("contain", "Sign In");
      cy.get('[data-cy="register-link"]')
        .should("be.visible")
        .and("contain", "Sign Up");
    });

    it("should show/hide password when eye icon is clicked", () => {
      cy.get('[data-cy="password-input"]').type("testpassword");

      // Password should be hidden by default
      cy.get('[data-cy="password-input"] input').should(
        "have.attr",
        "type",
        "password"
      );

      // Click eye icon to show password
      cy.get('[data-cy="password-input"] .mdi-eye-off').click();
      cy.get('[data-cy="password-input"] input').should(
        "have.attr",
        "type",
        "text"
      );

      // Click eye icon again to hide password
      cy.get('[data-cy="password-input"] .mdi-eye').click();
      cy.get('[data-cy="password-input"] input').should(
        "have.attr",
        "type",
        "password"
      );
    });
  });

  describe("Form Validation", () => {
    it("should show validation errors for empty fields", () => {
      // Try to submit empty form
      cy.get('[data-cy="login-button"]').click();

      // Should show validation errors
      cy.contains("Email is required").should("be.visible");
      cy.contains("Password is required").should("be.visible");
    });

    it("should show validation error for invalid email format", () => {
      cy.get('[data-cy="email-input"]').type("invalid-email");
      cy.get('[data-cy="password-input"]').type("password123");
      cy.get('[data-cy="login-button"]').click();

      // Should show email validation error
      cy.contains("Email must be valid").should("be.visible");
    });

    it("should show validation error for short password", () => {
      cy.get('[data-cy="email-input"]').type("test@example.com");
      cy.get('[data-cy="password-input"]').type("123");
      cy.get('[data-cy="login-button"]').click();

      // Should show password validation error
      cy.contains("Password must be at least 6 characters").should(
        "be.visible"
      );
    });
  });

  describe("Authentication Flow", () => {
    it("should successfully login with valid credentials", () => {
      // Fill in valid credentials
      cy.get('[data-cy="email-input"]').type("test@focusflow.com");
      cy.get('[data-cy="password-input"]').type("Test@123456");

      // Submit form
      cy.get('[data-cy="login-button"]').click();

      // Should redirect to tasks page after successful login
      cy.url().should("include", "/tasks");
      cy.checkAuthenticated();
    });

    it("should show error message for invalid credentials", () => {
      // Fill in invalid credentials
      cy.get('[data-cy="email-input"]').type("invalid@example.com");
      cy.get('[data-cy="password-input"]').type("wrongpassword");

      // Submit form
      cy.get('[data-cy="login-button"]').click();

      // Should stay on login page and show error
      cy.url().should("include", "/login");
      cy.contains("Invalid credentials").should("be.visible");
    });

    it("should show loading state during login", () => {
      // Fill in credentials
      cy.get('[data-cy="email-input"]').type(Cypress.env("TEST_EMAIL"));
      cy.get('[data-cy="password-input"]').type(Cypress.env("TEST_PASSWORD"));

      // Submit form and check loading state
      cy.get('[data-cy="login-button"]').click();
      cy.get('[data-cy="login-button"]').should("have.class", "v-btn--loading");
    });
  });

  describe("Navigation", () => {
    it("should navigate to register page when sign up link is clicked", () => {
      cy.get('[data-cy="register-link"]').click();
      cy.url().should("include", "/register");
    });
  });

  describe("Accessibility", () => {
    it("should be accessible via keyboard navigation", () => {
      // Tab through form elements
      cy.get("body").tab();
      cy.focused().should("have.attr", "data-cy", "email-input");

      cy.focused().tab();
      cy.focused().should("have.attr", "data-cy", "password-input");

      cy.focused().tab();
      cy.focused().should("have.attr", "data-cy", "login-button");
    });

    it("should submit form when Enter is pressed", () => {
      cy.get('[data-cy="email-input"]').type(Cypress.env("TEST_EMAIL"));
      cy.get('[data-cy="password-input"]')
        .type(Cypress.env("TEST_PASSWORD"))
        .type("{enter}");

      // Should redirect to dashboard
      cy.url().should("eq", Cypress.config().baseUrl + "/");
    });
  });
});
