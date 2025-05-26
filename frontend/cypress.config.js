const { defineConfig } = require("cypress");

module.exports = defineConfig({
  e2e: {
    baseUrl: "http://localhost:8081",
    supportFile: "cypress/support/e2e.js",
    specPattern: "cypress/e2e/**/*.cy.{js,jsx,ts,tsx}",
    viewportWidth: 1280,
    viewportHeight: 720,
    video: false,
    screenshotOnRunFailure: true,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 10000,
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
    env: {
      // Test user credentials
      TEST_EMAIL: "test@focusflow.com",
      TEST_PASSWORD: "Test@123456",
      // Backend API URL
      API_URL: "http://localhost:8080/api/v1",
    },
  },
  component: {
    devServer: {
      framework: "vue",
      bundler: "vite",
    },
  },
});
