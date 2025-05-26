// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

/**
 * Custom command to login via API and set localStorage
 * This is faster than going through the UI for every test
 */
Cypress.Commands.add(
  "loginViaAPI",
  (
    email = Cypress.env("TEST_EMAIL"),
    password = Cypress.env("TEST_PASSWORD")
  ) => {
    cy.request({
      method: "POST",
      url: `${Cypress.env("API_URL")}/auth/login`,
      body: {
        email,
        password,
      },
    }).then((response) => {
      expect(response.status).to.eq(200);
      expect(response.body).to.have.property("token");
      expect(response.body).to.have.property("userId");
      expect(response.body).to.have.property("email");

      // Store authentication data in localStorage
      window.localStorage.setItem("token", response.body.token);
      window.localStorage.setItem(
        "user",
        JSON.stringify({
          id: response.body.userId,
          email: response.body.email,
        })
      );

      // Return the response for further use
      return cy.wrap(response.body);
    });
  }
);

/**
 * Custom command to login via UI
 * Use this when you want to test the actual login flow
 */
Cypress.Commands.add(
  "loginViaUI",
  (
    email = Cypress.env("TEST_EMAIL"),
    password = Cypress.env("TEST_PASSWORD")
  ) => {
    cy.visit("/login");

    // Fill in login form
    cy.get('[data-cy="email-input"]').type(email);
    cy.get('[data-cy="password-input"]').type(password);

    // Submit form
    cy.get('[data-cy="login-button"]').click();

    // Wait for successful login (redirect to dashboard)
    cy.url().should("not.include", "/login");
    cy.url().should("eq", Cypress.config().baseUrl + "/");
  }
);

/**
 * Custom command to logout
 */
Cypress.Commands.add("logout", () => {
  // Clear localStorage
  cy.window().then((win) => {
    win.localStorage.removeItem("token");
    win.localStorage.removeItem("user");
  });

  // Visit home page to trigger auth check
  cy.visit("/");

  // Should redirect to login
  cy.url().should("include", "/login");
});

/**
 * Custom command to create a task via API
 * Useful for setting up test data
 */
Cypress.Commands.add("createTaskViaAPI", (taskData) => {
  // Get token from localStorage
  cy.window().then((win) => {
    const token = win.localStorage.getItem("token");
    const user = JSON.parse(win.localStorage.getItem("user"));

    const defaultTaskData = {
      title: "Test Task",
      shortDescription: "Test Description",
      longDescription: "Test Long Description",
      dueDate: new Date(Date.now() + 24 * 60 * 60 * 1000)
        .toISOString()
        .slice(0, 16), // Tomorrow
      priority: "MID",
      createdById: user.id,
      ...taskData,
    };

    cy.request({
      method: "POST",
      url: `${Cypress.env("API_URL")}/tasks`,
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: defaultTaskData,
    }).then((response) => {
      expect(response.status).to.eq(201);
      expect(response.body.success).to.be.true;
      return cy.wrap(response.body.data);
    });
  });
});

/**
 * Custom command to wait for element and ensure it's visible
 */
Cypress.Commands.add("waitForElement", (selector, timeout = 10000) => {
  cy.get(selector, { timeout }).should("be.visible");
});

/**
 * Custom command to clear all tasks for the test user
 * Useful for test cleanup
 */
Cypress.Commands.add("clearUserTasks", () => {
  cy.window().then((win) => {
    const token = win.localStorage.getItem("token");
    const user = JSON.parse(win.localStorage.getItem("user"));

    if (token && user) {
      // Get all tasks for the user
      cy.request({
        method: "GET",
        url: `${Cypress.env("API_URL")}/tasks/creator/${user.id}`,
        headers: {
          Authorization: `Bearer ${token}`,
        },
        failOnStatusCode: false,
      }).then((response) => {
        if (
          response.status === 200 &&
          response.body.success &&
          response.body.data
        ) {
          // Delete each task
          response.body.data.forEach((task) => {
            cy.request({
              method: "DELETE",
              url: `${Cypress.env("API_URL")}/tasks/${task.id}`,
              headers: {
                Authorization: `Bearer ${token}`,
              },
              failOnStatusCode: false,
            });
          });
        }
      });
    }
  });
});

// Custom command for login
Cypress.Commands.add(
  "login",
  (email = "test@focusflow.com", password = "Test@123456") => {
    cy.visit("/login");
    cy.get('[data-cy="email-input"]').type(email);
    cy.get('[data-cy="password-input"]').type(password);
    cy.get('[data-cy="login-button"]').click();
    cy.url().should("not.include", "/login");
  }
);

// Custom command for logout
Cypress.Commands.add("logout", () => {
  cy.get('[data-cy="logout-button"]').click();
  cy.url().should("include", "/login");
});

// Custom command for register
Cypress.Commands.add("register", (userData) => {
  const defaultData = {
    firstName: "Test",
    lastName: "User",
    email: `test${Date.now()}@example.com`,
    password: "Test@123456",
  };
  const user = { ...defaultData, ...userData };

  cy.visit("/register");
  cy.get('[data-cy="first-name-input"]').type(user.firstName);
  cy.get('[data-cy="last-name-input"]').type(user.lastName);
  cy.get('[data-cy="email-input"]').type(user.email);
  cy.get('[data-cy="password-input"]').type(user.password);
  cy.get('[data-cy="register-button"]').click();
});

// Custom command to check if user is authenticated
Cypress.Commands.add("checkAuthenticated", () => {
  cy.window().its("localStorage").invoke("getItem", "token").should("exist");
});

// Custom command to clear authentication
Cypress.Commands.add("clearAuth", () => {
  cy.window().its("localStorage").invoke("removeItem", "token");
  cy.window().its("localStorage").invoke("removeItem", "user");
});

// Custom command to wait for API response
Cypress.Commands.add("waitForApi", (alias) => {
  cy.wait(alias).then((interception) => {
    expect(interception.response.statusCode).to.be.oneOf([200, 201, 204]);
  });
});
