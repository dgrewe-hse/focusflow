/**
 * Optimized Authentication Tests
 * Faster version with reduced redundancy and improved efficiency
 */

const { expect } = require("chai");
const WebDriverManager = require("../utils/webdriver-manager");
const ApiClient = require("../utils/api-client");
const LoginPage = require("../page-objects/login-page");
const config = require("../config/test-config");

describe("Authentication Tests (Optimized)", function () {
  let driver;
  let apiClient;
  let loginPage;

  // Increase timeout for Selenium tests
  this.timeout(30000); // Reduced from 60s

  before(async function () {
    console.log("🚀 Starting Authentication Tests...");

    // Initialize WebDriver
    driver = new WebDriverManager();
    await driver.initializeDriver();

    // Initialize API client
    apiClient = new ApiClient();

    // Initialize page objects
    loginPage = new LoginPage(driver);

    console.log("✅ Test setup completed");
  });

  after(async function () {
    console.log("🧹 Cleaning up...");
    if (driver) {
      await driver.quitDriver();
    }
    console.log("✅ Cleanup completed");
  });

  describe("Login Page Elements", function () {
    it("should display and interact with login form", async function () {
      console.log("🧪 Testing login form display and interaction...");

      // Navigate to login page once
      await loginPage.open();

      // Verify form is displayed
      const isFormDisplayed = await loginPage.isLoginFormDisplayed();
      expect(isFormDisplayed).to.be.true;

      // Test form interaction (combines element existence and interaction)
      console.log("📧 Testing email input...");
      await driver.typeText(
        config.selectors.loginEmailInput,
        "test@example.com",
        true // Clear first
      );

      console.log("🔒 Testing password input...");
      await driver.typeText(
        config.selectors.loginPasswordInput,
        "testpassword",
        true // Clear first
      );

      console.log("🔘 Testing login button...");
      const buttonExists = await driver.elementExists(
        config.selectors.loginButton,
        3000
      );
      expect(buttonExists).to.be.true;

      // Verify values were entered correctly (no concatenation)
      const emailValue = await loginPage.getEmailValue();
      const passwordValue = await loginPage.getPasswordValue();
      expect(emailValue).to.equal("test@example.com");
      expect(passwordValue).to.equal("testpassword");

      console.log("✅ Login form elements and interaction verified");

      // Take screenshot once for this combined test
      await driver.takeScreenshot("login-form-interaction-test");
    });
  });

  describe("Form Validation", function () {
    beforeEach(async function () {
      // Always refresh the page to ensure clean state
      console.log("🔄 Ensuring clean page state...");
      await loginPage.open();
    });

    it("should validate empty form submission", async function () {
      console.log("🧪 Testing empty form validation...");

      // Form should already be empty after page refresh
      await driver.clickElement(config.selectors.loginButton);

      // Quick check - should stay on login page or show errors
      await new Promise((resolve) => setTimeout(resolve, 1000)); // Brief wait for validation

      const currentUrl = await driver.getCurrentUrl();
      const stillOnLoginPage =
        currentUrl.includes("/login") || currentUrl === config.baseUrl + "/";
      expect(stillOnLoginPage).to.be.true;

      console.log("✅ Empty form validation working");
    });

    it("should validate invalid email format", async function () {
      console.log("🧪 Testing invalid email validation...");

      await driver.typeText(
        config.selectors.loginEmailInput,
        "invalid-email",
        true
      );
      await driver.typeText(
        config.selectors.loginPasswordInput,
        "password123",
        true
      );
      await driver.clickElement(config.selectors.loginButton);

      // Brief wait for validation response
      await new Promise((resolve) => setTimeout(resolve, 1000));

      const currentUrl = await driver.getCurrentUrl();
      const stillOnLoginPage = currentUrl.includes("/login");
      expect(stillOnLoginPage).to.be.true;

      console.log("✅ Invalid email validation working");
    });

    it("should reject invalid credentials", async function () {
      console.log("🧪 Testing invalid credentials...");

      await driver.typeText(
        config.selectors.loginEmailInput,
        "invalid@example.com",
        true // Clear first
      );
      await driver.typeText(
        config.selectors.loginPasswordInput,
        "wrongpassword",
        true // Clear first
      );
      await driver.clickElement(config.selectors.loginButton);

      // Wait for server response
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // Should show error or stay on login page
      const hasError = await loginPage.hasErrorMessage();
      const currentUrl = await driver.getCurrentUrl();
      const stillOnLoginPage = currentUrl.includes("/login");

      expect(hasError || stillOnLoginPage).to.be.true;

      console.log("✅ Invalid credentials properly rejected");
    });
  });

  describe("Successful Authentication", function () {
    it("should successfully login with valid credentials", async function () {
      console.log("🧪 Testing valid credentials login...");

      // Ensure we start with a fresh page
      await loginPage.open();

      // Perform login with proper credentials
      console.log("🔐 Attempting login...");
      await driver.typeText(
        config.selectors.loginEmailInput,
        config.testUser.email,
        true
      );
      await driver.typeText(
        config.selectors.loginPasswordInput,
        config.testUser.password,
        true
      );

      // Take screenshot before clicking login
      await driver.takeScreenshot("before-login-click");

      await driver.clickElement(config.selectors.loginButton);

      // Wait a moment for any response
      await new Promise((resolve) => setTimeout(resolve, 3000));

      // Take screenshot after login attempt
      await driver.takeScreenshot("after-login-attempt");

      // Check current URL - should no longer be on login page
      const currentUrl = await driver.getCurrentUrl();
      console.log(`📍 Current URL after login: ${currentUrl}`);

      // Verify we're no longer on the login page
      const notOnLoginPage = !currentUrl.includes("/login");
      expect(notOnLoginPage).to.be.true;

      // Check if we can find the dashboard elements
      console.log("🔍 Looking for dashboard container...");
      const dashboardExists = await driver.elementExists(
        config.selectors.dashboardContainer,
        3000
      );
      console.log(`📊 Dashboard container found: ${dashboardExists}`);

      // Verify we can see dashboard elements
      expect(dashboardExists).to.be.true;

      console.log("✅ Valid credentials login successful");

      // Take screenshot of successful login
      await driver.takeScreenshot("successful-login");
    });
  });

  describe("API Integration (Quick)", function () {
    it("should authenticate with API using same credentials", async function () {
      console.log("🧪 Testing API authentication integration...");

      try {
        await apiClient.authenticate();
        expect(apiClient.authToken).to.not.be.null;
        expect(apiClient.userId).to.not.be.null;
        console.log("✅ API authentication successful");
      } catch (error) {
        console.error("❌ API authentication failed:", error.message);
        throw error;
      }
    });
  });
});
