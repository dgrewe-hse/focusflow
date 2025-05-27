/**
 * API Client for Selenium Tests
 * Handles authentication and API interactions with the backend
 */

const axios = require("axios");
const config = require("../config/test-config");

class ApiClient {
  constructor() {
    this.baseURL = config.apiBaseUrl;
    this.authToken = null;
    this.userId = null;
    this.teamId = null;

    // Create axios instance with base configuration
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 10000,
      headers: {
        "Content-Type": "application/json",
      },
    });

    // Add request interceptor to include auth token
    this.client.interceptors.request.use(
      (config) => {
        if (this.authToken) {
          config.headers.Authorization = `Bearer ${this.authToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );
  }

  /**
   * Authenticate with the test user
   * @returns {Promise<Object>} Authentication response
   */
  async authenticate() {
    try {
      console.log(`🔐 Authenticating with test user: ${config.testUser.email}`);

      const response = await this.client.post("/auth/login", {
        email: config.testUser.email,
        password: config.testUser.password,
      });

      if (response.status === 200 && response.data.token) {
        this.authToken = response.data.token;
        this.userId = response.data.userId;

        console.log("✓ Authentication successful");
        console.log(`✓ User ID: ${this.userId}`);

        // Get user teams
        await this.getUserTeams();

        return response.data;
      } else {
        throw new Error("Authentication failed: No token received");
      }
    } catch (error) {
      console.error(
        "✗ Authentication failed:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Get teams for the authenticated user
   * @returns {Promise<Array>} User teams
   */
  async getUserTeams() {
    if (!this.userId) {
      console.log("⚠️ No user ID available. Cannot fetch teams.");
      return [];
    }

    try {
      const response = await this.client.get(`/users/${this.userId}/teams`);

      if (response.status === 200 && response.data.data?.length > 0) {
        this.teamId = response.data.data[0].id;
        console.log(`✓ Found team ID: ${this.teamId}`);
        return response.data.data;
      } else {
        console.log("⚠️ No teams found for user");
        return [];
      }
    } catch (error) {
      console.log(
        "⚠️ Failed to fetch teams:",
        error.response?.data || error.message
      );
      return [];
    }
  }

  /**
   * Create a new task
   * @param {Object} taskData - Task data
   * @returns {Promise<Object>} Created task
   */
  async createTask(taskData) {
    try {
      const task = {
        title: taskData.title,
        shortDescription: taskData.shortDescription,
        longDescription: taskData.longDescription,
        dueDate: taskData.dueDate || this.getFutureDate(),
        priority: taskData.priority || "MID",
        status: taskData.status || "OPEN",
        createdById: this.userId,
      };

      const response = await this.client.post("/tasks", task);

      if (response.status === 201) {
        console.log(`✓ Task created with ID: ${response.data.data.id}`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to create task:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Get all tasks
   * @returns {Promise<Array>} All tasks
   */
  async getAllTasks() {
    try {
      const response = await this.client.get("/tasks/fetch");

      if (response.status === 200) {
        console.log(`✓ Retrieved ${response.data.data.length} tasks`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to get tasks:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Get task by ID
   * @param {string} taskId - Task ID
   * @returns {Promise<Object>} Task data
   */
  async getTaskById(taskId) {
    try {
      const response = await this.client.get(`/tasks/fetch/${taskId}`);

      if (response.status === 200) {
        console.log(`✓ Retrieved task: ${taskId}`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to get task:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Update task
   * @param {string} taskId - Task ID
   * @param {Object} updates - Task updates
   * @returns {Promise<Object>} Updated task
   */
  async updateTask(taskId, updates) {
    try {
      const response = await this.client.put(`/tasks/${taskId}`, {
        ...updates,
        createdById: this.userId,
      });

      if (response.status === 200) {
        console.log(`✓ Task updated: ${taskId}`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to update task:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Update task status
   * @param {string} taskId - Task ID
   * @param {string} status - New status
   * @returns {Promise<Object>} Updated task
   */
  async updateTaskStatus(taskId, status) {
    try {
      const response = await this.client.patch(
        `/tasks/${taskId}/status?status=${status}`
      );

      if (response.status === 200) {
        console.log(`✓ Task status updated to: ${status}`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to update task status:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Update task assignee
   * @param {string} taskId - Task ID
   * @param {string} assigneeId - Assignee ID
   * @returns {Promise<Object>} Updated task
   */
  async updateTaskAssignee(taskId, assigneeId) {
    try {
      const response = await this.client.patch(
        `/tasks/${taskId}/assignee?assigneeId=${assigneeId}`
      );

      if (response.status === 200) {
        console.log(`✓ Task assignee updated`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to update task assignee:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Delete task
   * @param {string} taskId - Task ID
   * @returns {Promise<boolean>} Success status
   */
  async deleteTask(taskId) {
    try {
      const response = await this.client.delete(`/tasks/${taskId}`);

      if (response.status === 204) {
        console.log(`✓ Task deleted: ${taskId}`);
        return true;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to delete task:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Search tasks
   * @param {Object} searchParams - Search parameters
   * @returns {Promise<Array>} Search results
   */
  async searchTasks(searchParams) {
    try {
      const queryString = new URLSearchParams(searchParams).toString();
      const response = await this.client.get(`/tasks/search?${queryString}`);

      if (response.status === 200) {
        console.log(
          `✓ Found ${response.data.data.length} tasks matching search`
        );
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to search tasks:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Get tasks by assignee
   * @param {string} assigneeId - Assignee ID
   * @returns {Promise<Array>} Tasks assigned to user
   */
  async getTasksByAssignee(assigneeId) {
    try {
      const response = await this.client.get(
        `/tasks/assignee/${assigneeId}?includeTags=false`
      );

      if (response.status === 200) {
        console.log(`✓ Found ${response.data.data.length} tasks for assignee`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to get tasks by assignee:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Get task statistics by user
   * @param {string} userId - User ID
   * @returns {Promise<Object>} Task statistics
   */
  async getTaskStatsByUser(userId) {
    try {
      const response = await this.client.get(`/tasks/stats/user/${userId}`);

      if (response.status === 200) {
        console.log("✓ Retrieved task statistics for user");
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to get task statistics:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Get upcoming tasks
   * @param {number} days - Number of days to look ahead
   * @returns {Promise<Array>} Upcoming tasks
   */
  async getUpcomingTasks(days = 7) {
    try {
      const response = await this.client.get(
        `/tasks/upcoming/${days}?includeTags=false`
      );

      if (response.status === 200) {
        console.log(`✓ Found ${response.data.data.length} upcoming tasks`);
        return response.data.data;
      } else {
        throw new Error(`Unexpected response status: ${response.status}`);
      }
    } catch (error) {
      console.error(
        "✗ Failed to get upcoming tasks:",
        error.response?.data || error.message
      );
      throw error;
    }
  }

  /**
   * Generate a future date string in ISO format
   * @param {number} daysInFuture - Days in the future
   * @returns {string} ISO date string
   */
  getFutureDate(daysInFuture = 7) {
    const date = new Date();
    date.setDate(date.getDate() + daysInFuture);
    return date.toISOString();
  }

  /**
   * Check if API is accessible
   * @returns {Promise<boolean>} API accessibility status
   */
  async checkApiHealth() {
    try {
      // Try to access a basic endpoint (this might need adjustment based on your API)
      const response = await axios.get(`${this.baseURL}/health`, {
        timeout: 5000,
      });
      return response.status === 200;
    } catch (error) {
      console.warn("⚠️ API health check failed:", error.message);
      return false;
    }
  }

  /**
   * Clear authentication data
   */
  clearAuth() {
    this.authToken = null;
    this.userId = null;
    this.teamId = null;
    console.log("🔓 Authentication cleared");
  }
}

module.exports = ApiClient;
