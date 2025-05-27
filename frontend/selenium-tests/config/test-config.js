/**
 * Selenium Test Configuration
 * Contains all configuration settings for the Selenium test suite
 */

// Function to detect available browser in container environment
function detectAvailableBrowser() {
  const os = require("os");
  const fs = require("fs");
  const { execSync } = require("child_process");

  const platform = os.platform();

  if (platform === "darwin") {
    // macOS - check for application bundles
    if (fs.existsSync("/Applications/Google Chrome.app")) {
      return "chrome";
    }
    if (fs.existsSync("/Applications/Chromium.app")) {
      return "chrome";
    }
    if (fs.existsSync("/Applications/Firefox.app")) {
      return "firefox";
    }
  } else {
    // Linux/Windows - check command line tools
    // Check for Chrome/Chromium
    try {
      execSync("which google-chrome", { stdio: "ignore" });
      return "chrome";
    } catch (e) {
      try {
        execSync("which chromium-browser", { stdio: "ignore" });
        return "chrome";
      } catch (e) {
        try {
          execSync("which chromium", { stdio: "ignore" });
          return "chrome";
        } catch (e) {
          // Chrome not found
        }
      }
    }

    // Check for Firefox
    try {
      execSync("which firefox", { stdio: "ignore" });
      return "firefox";
    } catch (e) {
      try {
        execSync("which firefox-esr", { stdio: "ignore" });
        return "firefox";
      } catch (e) {
        // Firefox not found
      }
    }
  }

  // Default to chrome (will be handled by WebDriver manager)
  return "chrome";
}

// Detect browser or use environment override
const defaultBrowser =
  process.env.BROWSER && !process.env.BROWSER.includes("browser.sh")
    ? process.env.BROWSER
    : detectAvailableBrowser();

const config = {
  // Application URLs
  baseUrl: process.env.BASE_URL || "http://localhost:8081",
  apiBaseUrl: process.env.API_BASE_URL || "http://localhost:8080/api/v1",

  // Test user credentials (from backend test script)
  testUser: {
    email: process.env.TEST_EMAIL || "test@focusflow.com",
    password: process.env.TEST_PASSWORD || "Test@123456",
  },

  // Browser configuration - fixed for container environment
  browser: defaultBrowser,
  headless: process.env.HEADLESS === "true" || process.env.CI === "true",

  // Timeouts (in milliseconds)
  timeouts: {
    implicit: 10000, // Implicit wait for elements
    explicit: 15000, // Explicit wait for specific conditions
    pageLoad: 30000, // Page load timeout
    script: 15000, // Script execution timeout
  },

  // Chrome specific options - different for host vs container
  chromeOptions: (() => {
    const baseOptions = [
      "--window-size=1920,1080",
      "--no-default-browser-check",
      "--no-first-run",
      "--disable-web-security",
      "--allow-running-insecure-content",
    ];

    // Container-specific settings
    const containerOptions = [
      // Core sandbox and security disabling for containers
      "--no-sandbox",
      "--disable-setuid-sandbox",
      "--disable-dev-shm-usage",
      "--disable-gpu",
      "--disable-gpu-sandbox",
      "--disable-software-rasterizer",

      // Process model for containers
      "--single-process",
      "--no-zygote",

      // Memory and performance
      "--memory-pressure-off",
      "--max_old_space_size=4096",

      // Disable unnecessary features
      "--disable-background-timer-throttling",
      "--disable-backgrounding-occluded-windows",
      "--disable-renderer-backgrounding",
      "--disable-features=TranslateUI",
      "--disable-features=BlinkGenPropertyTrees",
      "--disable-ipc-flooding-protection",
      "--disable-extensions",
      "--disable-plugins",
      "--disable-default-apps",
      "--disable-sync",
      "--disable-translate",

      // Stability improvements
      "--disable-hang-monitor",
      "--disable-prompt-on-repost",
      "--disable-client-side-phishing-detection",
      "--disable-component-extensions-with-background-pages",
      "--disable-background-networking",
      "--disable-breakpad",
      "--disable-component-update",
      "--disable-domain-reliability",
      "--disable-features=AudioServiceOutOfProcess",
      "--disable-features=VizDisplayCompositor",

      // Display and rendering
      "--force-color-profile=srgb",

      // Testing optimizations
      "--metrics-recording-only",
      "--no-crash-upload",
      "--remote-debugging-port=9222",
      "--user-data-dir=/tmp/chrome-test-profile",

      // Additional container-specific options
      "--ignore-certificate-errors",
      "--ignore-ssl-errors",
      "--ignore-certificate-errors-spki-list",
    ];

    // Host-specific settings (macOS, Windows, Linux desktop)
    const hostOptions = [
      "--disable-dev-shm-usage", // Still useful on host for stability
      "--disable-extensions", // Disable extensions for testing
      "--disable-plugins", // Disable plugins for testing
      "--no-sandbox", // Sometimes needed for automation
      "--remote-debugging-port=9222", // Enable debugging
    ];

    // Check if we're in container mode
    const isContainer =
      process.env.CONTAINER === "true" ||
      process.env.CODESPACES === "true" ||
      process.env.GITPOD_WORKSPACE_ID !== undefined ||
      process.env.CI === "true";

    return isContainer
      ? [...baseOptions, ...containerOptions]
      : [...baseOptions, ...hostOptions];
  })(),

  // Firefox specific options - enhanced for container environment
  firefoxOptions: ["--width=1920", "--height=1080", "--no-sandbox"],

  // Container-specific settings
  containerMode:
    process.env.CONTAINER === "true" ||
    process.env.CODESPACES === "true" ||
    process.env.GITPOD_WORKSPACE_ID !== undefined ||
    process.env.CI === "true",

  // Test data
  testData: {
    task: {
      title: "Selenium Test Task",
      shortDescription: "This is a test task created by Selenium",
      longDescription:
        "This is a detailed description of the test task created by automated Selenium tests",
      priority: "MID",
      status: "OPEN",
    },
    updatedTask: {
      title: "Updated Selenium Test Task",
      shortDescription: "This task has been updated by Selenium",
      priority: "HIGH",
      status: "PENDING",
    },
  },

  // Element selectors (commonly used)
  selectors: {
    // Authentication - Updated for Vuetify structure where data-cy is on wrapper
    loginEmailInput:
      '[data-cy="email-input"] input, input[type="email"], #input-1',
    loginPasswordInput:
      '[data-cy="password-input"] input, input[type="password"], #input-3',
    loginButton:
      '[data-cy="login-button"], button[type="submit"], .v-btn[type="submit"]',
    logoutButton:
      '[data-cy="logout-button"], [data-testid="logout-button"], .logout-btn',
    registerLink:
      '[data-cy="register-link"], [data-testid="register-link"], .register-link',

    // Navigation
    dashboardLink:
      '[data-cy="dashboard-link"], [data-testid="dashboard-link"], .nav-dashboard',
    tasksLink: '[data-cy="tasks-link"], [data-testid="tasks-link"], .nav-tasks',
    profileLink:
      '[data-cy="profile-link"], [data-testid="profile-link"], .nav-profile',

    // Dashboard/Task List page elements
    dashboardContainer:
      '[data-cy="dashboard-container"], [data-testid="dashboard"], .dashboard, .task-list',
    dashboardTitle:
      '[data-cy="dashboard-title"], [data-testid="dashboard-title"], h2, .page-title',
    tasksTable:
      '[data-cy="tasks-table"], [data-testid="tasks-table"], #tasks-table',

    // Task management
    createTaskButton:
      '[data-cy="create-task-button"], [data-testid="create-task-btn"], #create-task-btn, .create-task-btn',
    taskTitle:
      '[data-cy="task-title"] input, [data-testid="task-title"], .task-title',
    taskDescription:
      '[data-cy="task-description"] input, [data-cy="task-description"] textarea, [data-testid="task-description"], .task-description',
    taskPriority:
      '[data-cy="task-priority"], [data-testid="task-priority"], .task-priority',
    taskStatus:
      '[data-cy="task-status"], [data-testid="task-status"], .task-status',
    saveTaskButton:
      '[data-cy="save-task-btn"], [data-testid="save-task-btn"], .save-btn',
    editTaskButton:
      '[data-cy="edit-task-btn"], [data-testid="edit-task-btn"], .edit-btn',
    deleteTaskButton:
      '[data-cy="delete-task-btn"], [data-testid="delete-task-btn"], .delete-btn',

    // Common elements - Vue/Vuetify specific
    successMessage:
      ".v-snackbar--variant-elevated, .success-message, .alert-success",
    errorMessage:
      ".v-snackbar .v-snackbar__content, .error-message, .alert-error",
    loadingSpinner: ".v-progress-circular, .loading, .spinner",
    modal: ".v-dialog, .modal, .dialog",
    confirmButton: ".confirm-btn, .ok-btn",
    cancelButton: ".cancel-btn, .close-btn",
  },
};

module.exports = config;
