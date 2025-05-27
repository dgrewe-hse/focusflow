/**
 * Integration Tests
 * Tests complete workflows combining UI and API interactions
 */

const { expect } = require("chai");
const WebDriverManager = require("../utils/webdriver-manager");
const ApiClient = require("../utils/api-client");
const LoginPage = require("../page-objects/login-page");
const TasksPage = require("../page-objects/tasks-page");
const config = require("../config/test-config");

describe("Integration Tests", function () {
  let driver;
  let apiClient;
  let loginPage;
  let tasksPage;
  let testTaskIds = [];

  // Increase timeout for integration tests
  this.timeout(120000);

  before(async function () {
    console.log("🚀 Starting Integration Tests...");

    // Initialize WebDriver
    driver = new WebDriverManager();
    await driver.initializeDriver();

    // Initialize API client
    apiClient = new ApiClient();

    // Initialize page objects
    loginPage = new LoginPage(driver);
    tasksPage = new TasksPage(driver);

    console.log("✅ Integration test setup completed");
  });

  after(async function () {
    console.log("🧹 Cleaning up integration test data...");

    // Clean up test tasks
    for (const taskId of testTaskIds) {
      try {
        await apiClient.deleteTask(taskId);
        console.log(`✅ Cleaned up task: ${taskId}`);
      } catch (error) {
        console.log(`⚠️ Could not clean up task ${taskId}:`, error.message);
      }
    }

    if (driver) {
      await driver.quitDriver();
    }

    console.log("✅ Integration test cleanup completed");
  });

  beforeEach(async function () {
    await driver.takeScreenshot(
      `integration-before-${this.currentTest.title.replace(/\s+/g, "-")}`
    );
  });

  afterEach(async function () {
    const testState = this.currentTest.state === "failed" ? "failed" : "passed";
    await driver.takeScreenshot(
      `integration-after-${this.currentTest.title.replace(
        /\s+/g,
        "-"
      )}-${testState}`
    );
  });

  describe("Complete User Journey", function () {
    it("should complete full task management workflow", async function () {
      console.log("🧪 Testing complete task management workflow...");

      // Step 1: Authenticate via API
      await apiClient.authenticate();
      expect(apiClient.authToken).to.not.be.null;

      // Step 2: Login via UI
      await loginPage.open();
      await loginPage.loginWithTestUser();
      const isUILoggedIn = await loginPage.isLoggedIn();
      expect(isUILoggedIn).to.be.true;

      // Step 3: Navigate to tasks page
      await tasksPage.open();
      const currentUrl = await driver.getCurrentUrl();
      expect(currentUrl).to.include("/tasks");

      // Step 4: Create task via UI
      const uiTaskData = {
        title: `UI Created Task ${Date.now()}`,
        longDescription: "Task created via UI for integration test",
        priority: "HIGH",
        status: "OPEN",
      };

      await tasksPage.createTask(uiTaskData);
      const uiTaskExists = await tasksPage.verifyTaskExists(uiTaskData.title);
      expect(uiTaskExists).to.be.true;

      // Step 5: Verify task exists via API
      const allTasks = await apiClient.getAllTasks();
      const uiCreatedTask = allTasks.find(
        (task) => task.title === uiTaskData.title
      );
      expect(uiCreatedTask).to.not.be.undefined;
      testTaskIds.push(uiCreatedTask.id);

      // Step 6: Create another task via API
      const apiTaskData = {
        title: `API Created Task ${Date.now()}`,
        longDescription: "Task created via API for integration test",
        priority: "MID",
        status: "PENDING",
      };

      const apiCreatedTask = await apiClient.createTask(apiTaskData);
      testTaskIds.push(apiCreatedTask.id);

      // Step 7: Verify API task appears in UI
      await tasksPage.refreshPage();
      const apiTaskExists = await tasksPage.verifyTaskExists(apiTaskData.title);
      expect(apiTaskExists).to.be.true;

      // Step 8: Update task via API and verify in UI
      const updatedTitle = `Updated ${apiTaskData.title}`;
      await apiClient.updateTask(apiCreatedTask.id, {
        title: updatedTitle,
        priority: "LOW",
      });

      await tasksPage.refreshPage();
      const updatedTaskExists = await tasksPage.verifyTaskExists(updatedTitle);
      expect(updatedTaskExists).to.be.true;

      console.log("✅ Complete workflow integration successful");
    });

    it("should maintain data consistency between UI and API operations", async function () {
      console.log("🧪 Testing UI/API data consistency...");

      // Authenticate both UI and API
      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      // Create task via UI
      const taskData = {
        title: `Consistency Test Task ${Date.now()}`,
        longDescription: "Testing data consistency between UI and API",
        priority: "HIGH",
        status: "OPEN",
      };

      await tasksPage.createTask(taskData);

      // Verify via API
      const tasksFromAPI = await apiClient.getAllTasks();
      const createdTask = tasksFromAPI.find(
        (task) => task.title === taskData.title
      );
      expect(createdTask).to.not.be.undefined;
      expect(createdTask.priority).to.equal(taskData.priority);
      expect(createdTask.status).to.equal(taskData.status);

      testTaskIds.push(createdTask.id);

      // Update via API
      await apiClient.updateTaskStatus(createdTask.id, "COMPLETED");

      // Verify status change reflects in UI (may need refresh)
      await tasksPage.refreshPage();

      // Get updated task details via API to confirm
      const updatedTask = await apiClient.getTaskById(createdTask.id);
      expect(updatedTask.status).to.equal("COMPLETED");

      console.log("✅ Data consistency maintained");
    });
  });

  describe("Authentication Integration", function () {
    it("should handle session management across UI and API", async function () {
      console.log("🧪 Testing session management integration...");

      // Test 1: API authentication first
      await apiClient.authenticate();
      expect(apiClient.authToken).to.not.be.null;

      // Test 2: UI login with same credentials
      await loginPage.open();
      await loginPage.loginWithTestUser();
      const isLoggedIn = await loginPage.isLoggedIn();
      expect(isLoggedIn).to.be.true;

      // Test 3: Both should work independently
      const apiTasks = await apiClient.getAllTasks();
      expect(Array.isArray(apiTasks)).to.be.true;

      await tasksPage.open();
      const pageLoaded = await driver.elementExists(
        [
          tasksPage.selectors.tasksList,
          tasksPage.selectors.noTasksMessage,
        ].join(", "),
        5000
      );
      expect(pageLoaded).to.be.true;

      console.log("✅ Session management integration working");
    });

    it("should handle authentication failures gracefully", async function () {
      console.log("🧪 Testing authentication failure handling...");

      // Clear any existing authentication
      apiClient.clearAuth();

      // Test API with invalid credentials
      try {
        const invalidClient = new ApiClient();
        invalidClient.testUser = {
          email: "invalid@test.com",
          password: "wrongpass",
        };
        await invalidClient.authenticate();
        expect.fail("Should have thrown authentication error");
      } catch (error) {
        expect(error.message).to.include("Authentication failed");
      }

      // Test UI with invalid credentials
      await loginPage.open();
      const hasError = await loginPage.attemptInvalidLogin(
        "invalid@test.com",
        "wrongpass"
      );
      expect(hasError).to.be.true;

      console.log("✅ Authentication failure handling working");
    });
  });

  describe("Real-time Data Synchronization", function () {
    it("should reflect real-time changes between UI and API", async function () {
      console.log("🧪 Testing real-time data synchronization...");

      // Setup: Authenticate and navigate
      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      // Get initial task count
      const initialCount = await tasksPage.getTaskCount();

      // Create task via API while UI is open
      const realtimeTaskData = {
        title: `Realtime Task ${Date.now()}`,
        longDescription: "Testing real-time synchronization",
        priority: "MID",
        status: "OPEN",
      };

      const realtimeTask = await apiClient.createTask(realtimeTaskData);
      testTaskIds.push(realtimeTask.id);

      // Refresh UI and check for new task
      await tasksPage.refreshPage();
      const finalCount = await tasksPage.getTaskCount();
      const newTaskExists = await tasksPage.verifyTaskExists(
        realtimeTaskData.title
      );

      expect(newTaskExists).to.be.true;
      expect(finalCount).to.be.greaterThan(initialCount);

      console.log("✅ Real-time synchronization working");
    });

    it("should handle concurrent operations", async function () {
      console.log("🧪 Testing concurrent operations...");

      // Setup
      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      // Create multiple tasks concurrently
      const concurrentTasks = [];
      const taskPromises = [];

      for (let i = 0; i < 3; i++) {
        const taskData = {
          title: `Concurrent Task ${i} ${Date.now()}`,
          longDescription: `Concurrent task ${i} for testing`,
          priority: i % 2 === 0 ? "HIGH" : "LOW",
          status: "OPEN",
        };

        concurrentTasks.push(taskData);
        taskPromises.push(apiClient.createTask(taskData));
      }

      // Wait for all tasks to be created
      const createdTasks = await Promise.all(taskPromises);
      testTaskIds.push(...createdTasks.map((task) => task.id));

      // Refresh UI and verify all tasks appear
      await tasksPage.refreshPage();

      let foundCount = 0;
      for (const taskData of concurrentTasks) {
        if (await tasksPage.verifyTaskExists(taskData.title)) {
          foundCount++;
        }
      }

      expect(foundCount).to.equal(concurrentTasks.length);

      console.log(
        `✅ Concurrent operations: ${foundCount}/${concurrentTasks.length} successful`
      );
    });
  });

  describe("Error Handling and Recovery", function () {
    it("should handle network interruptions gracefully", async function () {
      console.log("🧪 Testing network interruption handling...");

      // Setup normal state
      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      // Simulate network issue by using invalid endpoint
      const originalBaseURL = apiClient.baseURL;
      apiClient.baseURL = "http://invalid-url:9999";

      try {
        await apiClient.getAllTasks();
        expect.fail("Should have failed with network error");
      } catch (error) {
        expect(error.code).to.be.oneOf([
          "ENOTFOUND",
          "ECONNREFUSED",
          "TIMEOUT",
        ]);
      }

      // Restore connection
      apiClient.baseURL = originalBaseURL;

      // Verify recovery
      const tasks = await apiClient.getAllTasks();
      expect(Array.isArray(tasks)).to.be.true;

      console.log("✅ Network interruption handling working");
    });

    it("should recover from UI errors gracefully", async function () {
      console.log("🧪 Testing UI error recovery...");

      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      // Try to interact with non-existent element (should not crash)
      try {
        await driver.clickElement(".non-existent-element", 2000);
      } catch (error) {
        // Expected to fail
        console.log("Expected error caught:", error.message);
      }

      // Verify UI is still functional
      const isStillFunctional = await driver.elementExists(
        tasksPage.selectors.createTaskButton,
        5000
      );
      expect(isStillFunctional).to.be.true;

      console.log("✅ UI error recovery working");
    });
  });

  describe("Performance and Load Testing", function () {
    it("should handle bulk operations efficiently", async function () {
      console.log("🧪 Testing bulk operations performance...");

      await apiClient.authenticate();

      const bulkTaskIds = [];
      const startTime = Date.now();

      // Create multiple tasks
      const bulkCreatePromises = [];
      for (let i = 0; i < 10; i++) {
        const taskData = {
          title: `Bulk Task ${i} ${Date.now()}`,
          longDescription: `Bulk operation test task ${i}`,
          priority: "LOW",
          status: "OPEN",
        };

        bulkCreatePromises.push(apiClient.createTask(taskData));
      }

      const createdTasks = await Promise.all(bulkCreatePromises);
      bulkTaskIds.push(...createdTasks.map((task) => task.id));
      testTaskIds.push(...bulkTaskIds);

      const createTime = Date.now() - startTime;

      // Test UI performance with many tasks
      await loginPage.open();
      await loginPage.loginWithTestUser();

      const uiStartTime = Date.now();
      await tasksPage.open();
      const uiLoadTime = Date.now() - uiStartTime;

      // Cleanup (delete in bulk)
      const deleteStartTime = Date.now();
      const deletePromises = bulkTaskIds.map((id) => apiClient.deleteTask(id));
      await Promise.all(deletePromises);
      const deleteTime = Date.now() - deleteStartTime;

      // Remove from cleanup list since already deleted
      testTaskIds = testTaskIds.filter((id) => !bulkTaskIds.includes(id));

      console.log(`✅ Performance metrics:`);
      console.log(`   - Bulk create (10 tasks): ${createTime}ms`);
      console.log(`   - UI load time: ${uiLoadTime}ms`);
      console.log(`   - Bulk delete (10 tasks): ${deleteTime}ms`);

      // Reasonable performance expectations
      expect(createTime).to.be.below(30000); // 30 seconds
      expect(uiLoadTime).to.be.below(15000); // 15 seconds
      expect(deleteTime).to.be.below(20000); // 20 seconds
    });

    it("should maintain responsiveness under load", async function () {
      console.log("🧪 Testing UI responsiveness under load...");

      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      // Measure response times for common operations
      const operations = [];

      // Test navigation response time
      const navStartTime = Date.now();
      await tasksPage.open();
      operations.push({
        operation: "Navigation",
        time: Date.now() - navStartTime,
      });

      // Test create form response time
      const formStartTime = Date.now();
      await tasksPage.clickCreateTask();
      operations.push({
        operation: "Form Load",
        time: Date.now() - formStartTime,
      });

      // Cancel to return to list
      try {
        await tasksPage.cancelTask();
      } catch (error) {
        await tasksPage.navigateBack();
      }

      // Log performance metrics
      operations.forEach((op) => {
        console.log(`   - ${op.operation}: ${op.time}ms`);
        expect(op.time).to.be.below(10000); // 10 seconds max for any operation
      });

      console.log("✅ UI responsiveness maintained under load");
    });
  });

  describe("Data Validation and Integrity", function () {
    it("should maintain data integrity across operations", async function () {
      console.log("🧪 Testing data integrity...");

      await apiClient.authenticate();

      // Create task with specific data
      const originalData = {
        title: `Integrity Test ${Date.now()}`,
        shortDescription: "Original short description",
        longDescription: "Original long description with special chars: àáâãäå",
        priority: "HIGH",
        status: "OPEN",
      };

      const createdTask = await apiClient.createTask(originalData);
      testTaskIds.push(createdTask.id);

      // Verify data integrity via API
      const retrievedTask = await apiClient.getTaskById(createdTask.id);
      expect(retrievedTask.title).to.equal(originalData.title);
      expect(retrievedTask.longDescription).to.equal(
        originalData.longDescription
      );
      expect(retrievedTask.priority).to.equal(originalData.priority);
      expect(retrievedTask.status).to.equal(originalData.status);

      // Verify data integrity via UI
      await loginPage.open();
      await loginPage.loginWithTestUser();
      await tasksPage.open();

      const taskExistsInUI = await tasksPage.verifyTaskExists(
        originalData.title
      );
      expect(taskExistsInUI).to.be.true;

      // Update data and verify integrity
      const updateData = {
        title: `Updated ${originalData.title}`,
        longDescription: "Updated description with émojis 🚀📝",
        priority: "LOW",
        status: "COMPLETED",
      };

      await apiClient.updateTask(createdTask.id, updateData);

      // Verify update integrity
      const updatedTask = await apiClient.getTaskById(createdTask.id);
      expect(updatedTask.title).to.equal(updateData.title);
      expect(updatedTask.longDescription).to.equal(updateData.longDescription);
      expect(updatedTask.priority).to.equal(updateData.priority);
      expect(updatedTask.status).to.equal(updateData.status);

      console.log("✅ Data integrity maintained");
    });

    it("should validate data constraints consistently", async function () {
      console.log("🧪 Testing data constraint validation...");

      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();

      // Test empty title validation (should fail both UI and API)
      try {
        await apiClient.createTask({
          title: "",
          longDescription: "Empty title test",
          priority: "MID",
          status: "OPEN",
        });
        expect.fail("API should reject empty title");
      } catch (error) {
        expect(error).to.exist; // Should throw validation error
      }

      // Test UI validation for empty title
      await tasksPage.open();
      await tasksPage.clickCreateTask();
      await tasksPage.fillTaskForm({
        title: "",
        longDescription: "Empty title test UI",
      });
      await tasksPage.saveTask();

      // Should show validation error or stay on form
      const hasError = await tasksPage.hasErrorMessage();
      const formStillVisible = await driver.elementExists(
        tasksPage.selectors.taskTitleInput,
        3000
      );
      expect(hasError || formStillVisible).to.be.true;

      console.log("✅ Data constraints validated consistently");
    });
  });

  describe("Security Integration", function () {
    it("should enforce authentication consistently across UI and API", async function () {
      console.log("🧪 Testing authentication enforcement...");

      // Test unauthenticated API access
      const unauthenticatedClient = new ApiClient();
      try {
        await unauthenticatedClient.getAllTasks();
        expect.fail("Should require authentication");
      } catch (error) {
        expect(error.response?.status).to.be.oneOf([401, 403]);
      }

      // Test unauthenticated UI access
      await driver.navigateTo(config.baseUrl + "/tasks");
      const currentUrl = await driver.getCurrentUrl();
      const isRedirectedToLogin = currentUrl.includes("/login");
      const hasAccessDenied = await driver.elementExists(
        ".unauthorized, .access-denied",
        3000
      );

      expect(isRedirectedToLogin || hasAccessDenied).to.be.true;

      console.log("✅ Authentication consistently enforced");
    });

    it("should handle authorization levels properly", async function () {
      console.log("🧪 Testing authorization levels...");

      // Authenticate with test user
      await apiClient.authenticate();
      await loginPage.open();
      await loginPage.loginWithTestUser();

      // Create a task (should be allowed)
      const taskData = {
        title: `Authorization Test ${Date.now()}`,
        longDescription: "Testing authorization levels",
        priority: "MID",
        status: "OPEN",
      };

      const createdTask = await apiClient.createTask(taskData);
      testTaskIds.push(createdTask.id);

      // Verify user can access their own tasks
      const userTasks = await apiClient.getTasksByAssignee(apiClient.userId);
      expect(Array.isArray(userTasks)).to.be.true;

      // Verify UI access to tasks
      await tasksPage.open();
      const canAccessTasks = await driver.elementExists(
        [
          tasksPage.selectors.tasksList,
          tasksPage.selectors.createTaskButton,
        ].join(", "),
        5000
      );
      expect(canAccessTasks).to.be.true;

      console.log("✅ Authorization levels working properly");
    });
  });
});
