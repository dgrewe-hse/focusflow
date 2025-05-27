/**
 * Tasks Page Object
 * Handles all task management interactions
 */

const BasePage = require("./base-page");

class TasksPage extends BasePage {
  constructor(webDriverManager) {
    super(webDriverManager);

    // Page-specific selectors
    this.selectors = {
      // Navigation
      tasksPageLink: this.config.selectors.tasksLink,

      // Task list elements
      tasksList: '[data-testid="tasks-list"], .tasks-list, .task-container',
      taskItem: '[data-testid="task-item"], .task-item, .task-card',
      noTasksMessage: '[data-testid="no-tasks"], .no-tasks, .empty-state',

      // Create task elements
      createTaskButton: this.config.selectors.createTaskButton,
      createTaskModal:
        '[data-testid="create-task-modal"], .create-task-modal, .task-modal',

      // Task form elements
      taskTitleInput: this.config.selectors.taskTitle,
      taskShortDescriptionInput:
        '[data-testid="short-description"], .short-description, textarea',
      taskLongDescriptionInput: this.config.selectors.taskDescription,
      taskPrioritySelect: this.config.selectors.taskPriority,
      taskStatusSelect: this.config.selectors.taskStatus,
      taskDueDateInput:
        '[data-testid="due-date"], .due-date, input[type="date"]',
      saveTaskButton: this.config.selectors.saveTaskButton,
      cancelTaskButton: '[data-testid="cancel-task"], .cancel-btn',

      // Task actions
      editTaskButton: this.config.selectors.editTaskButton,
      deleteTaskButton: this.config.selectors.deleteTaskButton,
      viewTaskButton: '[data-testid="view-task"], .view-btn',

      // Search and filter
      searchInput:
        '[data-testid="search-tasks"], .search-input, input[placeholder*="search"]',
      filterButton: '[data-testid="filter-btn"], .filter-btn',
      statusFilter: '[data-testid="status-filter"], .status-filter',
      priorityFilter: '[data-testid="priority-filter"], .priority-filter',

      // Task details
      taskDetailsModal: '[data-testid="task-details"], .task-details-modal',
      taskDetailsTitle:
        '[data-testid="task-details-title"], .task-details h1, .task-details h2',
      taskDetailsDescription:
        '[data-testid="task-details-description"], .task-details .description',
      taskDetailsPriority:
        '[data-testid="task-details-priority"], .task-details .priority',
      taskDetailsStatus:
        '[data-testid="task-details-status"], .task-details .status',
      taskDetailsDueDate:
        '[data-testid="task-details-due-date"], .task-details .due-date',

      // Pagination
      paginationNext: '[data-testid="pagination-next"], .pagination-next',
      paginationPrev: '[data-testid="pagination-prev"], .pagination-prev',
      paginationInfo: '[data-testid="pagination-info"], .pagination-info',
    };
  }

  /**
   * Navigate to tasks page
   * @returns {Promise<void>}
   */
  async open() {
    await this.navigateTo("/tasks");
    await this.waitForTasksPageToLoad();
  }

  /**
   * Navigate to tasks page via navigation menu
   * @returns {Promise<void>}
   */
  async navigateViaMenu() {
    await this.driver.clickElement(this.selectors.tasksPageLink);
    await this.waitForTasksPageToLoad();
  }

  /**
   * Wait for tasks page to load
   * @returns {Promise<void>}
   */
  async waitForTasksPageToLoad() {
    await this.waitForLoadingToComplete();

    // Wait for either task list or no tasks message
    const pageElements = [
      this.selectors.tasksList,
      this.selectors.noTasksMessage,
      this.selectors.createTaskButton,
    ];

    await this.waitForAnyElement(pageElements);
  }

  /**
   * Click create task button
   * @returns {Promise<void>}
   */
  async clickCreateTask() {
    await this.driver.clickElement(this.selectors.createTaskButton);
    await this.waitForTaskFormToLoad();
  }

  /**
   * Wait for task form (create/edit) to load
   * @returns {Promise<void>}
   */
  async waitForTaskFormToLoad() {
    await this.waitForElementVisible(this.selectors.taskTitleInput);
    await this.waitForElementVisible(this.selectors.saveTaskButton);
  }

  /**
   * Create a new task
   * @param {Object} taskData - Task data
   * @returns {Promise<void>}
   */
  async createTask(taskData) {
    console.log(`📝 Creating task: ${taskData.title}`);

    await this.clickCreateTask();
    await this.fillTaskForm(taskData);
    await this.saveTask();
    await this.waitForTaskCreationResult();
  }

  /**
   * Fill task form with data
   * @param {Object} taskData - Task data
   * @returns {Promise<void>}
   */
  async fillTaskForm(taskData) {
    // Fill title
    if (taskData.title) {
      await this.driver.typeText(this.selectors.taskTitleInput, taskData.title);
    }

    // Fill short description
    if (taskData.shortDescription) {
      if (
        await this.driver.elementExists(
          this.selectors.taskShortDescriptionInput,
          2000
        )
      ) {
        await this.driver.typeText(
          this.selectors.taskShortDescriptionInput,
          taskData.shortDescription
        );
      }
    }

    // Fill long description
    if (taskData.longDescription) {
      await this.driver.typeText(
        this.selectors.taskLongDescriptionInput,
        taskData.longDescription
      );
    }

    // Set priority
    if (taskData.priority) {
      await this.selectOptionByValue(
        this.selectors.taskPrioritySelect,
        taskData.priority
      );
    }

    // Set status
    if (taskData.status) {
      await this.selectOptionByValue(
        this.selectors.taskStatusSelect,
        taskData.status
      );
    }

    // Set due date
    if (taskData.dueDate) {
      if (
        await this.driver.elementExists(this.selectors.taskDueDateInput, 2000)
      ) {
        await this.driver.typeText(
          this.selectors.taskDueDateInput,
          taskData.dueDate
        );
      }
    }
  }

  /**
   * Select option in dropdown by value
   * @param {string} selector - Dropdown selector
   * @param {string} value - Option value
   * @returns {Promise<void>}
   */
  async selectOptionByValue(selector, value) {
    try {
      await this.driver.clickElement(selector);

      // Wait a bit for dropdown to open
      await new Promise((resolve) => setTimeout(resolve, 500));

      const optionSelector = `${selector} option[value="${value}"], [data-value="${value}"], .option[data-value="${value}"]`;
      await this.driver.clickElement(optionSelector);
    } catch (error) {
      console.log(
        `Could not select option ${value} in dropdown:`,
        error.message
      );
    }
  }

  /**
   * Save task (click save button)
   * @returns {Promise<void>}
   */
  async saveTask() {
    await this.driver.clickElement(this.selectors.saveTaskButton);
  }

  /**
   * Cancel task creation/editing
   * @returns {Promise<void>}
   */
  async cancelTask() {
    await this.driver.clickElement(this.selectors.cancelTaskButton);
  }

  /**
   * Wait for task creation result
   * @returns {Promise<boolean>} True if task created successfully
   */
  async waitForTaskCreationResult() {
    try {
      // Wait for success message or return to task list
      const resultSelectors = [
        this.config.selectors.successMessage,
        this.selectors.tasksList,
        this.config.selectors.errorMessage,
      ];

      const foundSelector = await this.waitForAnyElement(
        resultSelectors,
        10000
      );

      if (foundSelector.includes("error")) {
        console.log("❌ Task creation failed");
        return false;
      }

      console.log("✅ Task created successfully");
      return true;
    } catch (error) {
      console.log("⚠️ Task creation result unclear:", error.message);
      return false;
    }
  }

  /**
   * Get all task items on the page
   * @returns {Promise<Array>} Array of task elements
   */
  async getTaskItems() {
    try {
      return await this.driver.findElements(this.selectors.taskItem);
    } catch (error) {
      return [];
    }
  }

  /**
   * Get number of visible tasks
   * @returns {Promise<number>} Number of tasks
   */
  async getTaskCount() {
    const tasks = await this.getTaskItems();
    return tasks.length;
  }

  /**
   * Find task by title
   * @param {string} title - Task title to search for
   * @returns {Promise<WebElement|null>} Task element or null
   */
  async findTaskByTitle(title) {
    try {
      const tasks = await this.getTaskItems();

      for (const task of tasks) {
        const taskText = await task.getText();
        if (taskText.includes(title)) {
          return task;
        }
      }

      return null;
    } catch (error) {
      return null;
    }
  }

  /**
   * Click edit button for a specific task
   * @param {string} taskTitle - Title of task to edit
   * @returns {Promise<void>}
   */
  async editTask(taskTitle) {
    const taskElement = await this.findTaskByTitle(taskTitle);

    if (taskElement) {
      // Look for edit button within the task element
      const editButton = await taskElement.findElement({
        css: this.selectors.editTaskButton.split(",")[0],
      });
      await editButton.click();
      await this.waitForTaskFormToLoad();
    } else {
      throw new Error(`Task with title "${taskTitle}" not found`);
    }
  }

  /**
   * Click delete button for a specific task
   * @param {string} taskTitle - Title of task to delete
   * @returns {Promise<void>}
   */
  async deleteTask(taskTitle) {
    const taskElement = await this.findTaskByTitle(taskTitle);

    if (taskElement) {
      // Look for delete button within the task element
      const deleteButton = await taskElement.findElement({
        css: this.selectors.deleteTaskButton.split(",")[0],
      });
      await deleteButton.click();

      // Handle confirmation if it appears
      await this.handleDeleteConfirmation();
    } else {
      throw new Error(`Task with title "${taskTitle}" not found`);
    }
  }

  /**
   * Handle delete confirmation dialog
   * @returns {Promise<void>}
   */
  async handleDeleteConfirmation() {
    try {
      // Wait for confirmation dialog
      if (await this.driver.elementExists(this.config.selectors.modal, 3000)) {
        await this.confirmAction();
      }
    } catch (error) {
      console.log("No delete confirmation dialog found");
    }
  }

  /**
   * View task details
   * @param {string} taskTitle - Title of task to view
   * @returns {Promise<void>}
   */
  async viewTaskDetails(taskTitle) {
    const taskElement = await this.findTaskByTitle(taskTitle);

    if (taskElement) {
      // Try clicking the task element or view button
      try {
        const viewButton = await taskElement.findElement({
          css: this.selectors.viewTaskButton.split(",")[0],
        });
        await viewButton.click();
      } catch (error) {
        // If no specific view button, click the task element itself
        await taskElement.click();
      }

      await this.waitForTaskDetailsToLoad();
    } else {
      throw new Error(`Task with title "${taskTitle}" not found`);
    }
  }

  /**
   * Wait for task details modal to load
   * @returns {Promise<void>}
   */
  async waitForTaskDetailsToLoad() {
    await this.waitForElementVisible(this.selectors.taskDetailsModal);
  }

  /**
   * Get task details from the details modal
   * @returns {Promise<Object>} Task details
   */
  async getTaskDetailsFromModal() {
    const details = {};

    try {
      details.title = await this.driver.getText(
        this.selectors.taskDetailsTitle
      );
    } catch (error) {
      details.title = "";
    }

    try {
      details.description = await this.driver.getText(
        this.selectors.taskDetailsDescription
      );
    } catch (error) {
      details.description = "";
    }

    try {
      details.priority = await this.driver.getText(
        this.selectors.taskDetailsPriority
      );
    } catch (error) {
      details.priority = "";
    }

    try {
      details.status = await this.driver.getText(
        this.selectors.taskDetailsStatus
      );
    } catch (error) {
      details.status = "";
    }

    try {
      details.dueDate = await this.driver.getText(
        this.selectors.taskDetailsDueDate
      );
    } catch (error) {
      details.dueDate = "";
    }

    return details;
  }

  /**
   * Search for tasks
   * @param {string} searchTerm - Search term
   * @returns {Promise<void>}
   */
  async searchTasks(searchTerm) {
    if (await this.driver.elementExists(this.selectors.searchInput, 3000)) {
      await this.driver.typeText(this.selectors.searchInput, searchTerm);

      // Press Enter or wait for auto-search
      await this.driver.executeScript(
        `
        const event = new KeyboardEvent('keypress', { key: 'Enter' });
        arguments[0].dispatchEvent(event);
      `,
        await this.driver.findElement(this.selectors.searchInput)
      );

      await this.waitForLoadingToComplete();
    }
  }

  /**
   * Filter tasks by status
   * @param {string} status - Status to filter by
   * @returns {Promise<void>}
   */
  async filterByStatus(status) {
    if (await this.driver.elementExists(this.selectors.statusFilter, 3000)) {
      await this.selectOptionByValue(this.selectors.statusFilter, status);
      await this.waitForLoadingToComplete();
    }
  }

  /**
   * Filter tasks by priority
   * @param {string} priority - Priority to filter by
   * @returns {Promise<void>}
   */
  async filterByPriority(priority) {
    if (await this.driver.elementExists(this.selectors.priorityFilter, 3000)) {
      await this.selectOptionByValue(this.selectors.priorityFilter, priority);
      await this.waitForLoadingToComplete();
    }
  }

  /**
   * Check if tasks page is empty
   * @returns {Promise<boolean>}
   */
  async isTasksPageEmpty() {
    return await this.driver.elementExists(this.selectors.noTasksMessage, 3000);
  }

  /**
   * Update an existing task
   * @param {string} taskTitle - Current task title
   * @param {Object} updates - Updated task data
   * @returns {Promise<void>}
   */
  async updateTask(taskTitle, updates) {
    console.log(`✏️ Updating task: ${taskTitle}`);

    await this.editTask(taskTitle);
    await this.fillTaskForm(updates);
    await this.saveTask();
    await this.waitForTaskCreationResult();
  }

  /**
   * Get all task titles currently visible
   * @returns {Promise<Array<string>>} Array of task titles
   */
  async getAllTaskTitles() {
    const tasks = await this.getTaskItems();
    const titles = [];

    for (const task of tasks) {
      try {
        const titleElement = await task.findElement({
          css: ".task-title, h3, h4, .title",
        });
        const title = await titleElement.getText();
        titles.push(title);
      } catch (error) {
        // Could not find title in this task
        continue;
      }
    }

    return titles;
  }

  /**
   * Verify task exists in the list
   * @param {string} taskTitle - Task title to verify
   * @returns {Promise<boolean>}
   */
  async verifyTaskExists(taskTitle) {
    const task = await this.findTaskByTitle(taskTitle);
    return task !== null;
  }
}

module.exports = TasksPage;
