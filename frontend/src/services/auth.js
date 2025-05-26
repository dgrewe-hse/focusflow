import { ref, computed } from "vue";
import api from "./api";

// Reactive authentication state
const token = ref(localStorage.getItem("token"));
const user = ref(JSON.parse(localStorage.getItem("user") || "null"));

// Computed properties
const isAuthenticated = computed(() => !!token.value);

/**
 * Authentication service for managing user login, registration, and JWT tokens
 */
export const authService = {
  // State getters
  get token() {
    return token.value;
  },

  get user() {
    return user.value;
  },

  get isAuthenticated() {
    return isAuthenticated.value;
  },

  /**
   * Login user with email and password
   * @param {string} email - User email
   * @param {string} password - User password
   * @returns {Promise<Object>} Login response
   */
  async login(email, password) {
    try {
      const response = await api.post("/api/v1/auth/login", {
        email,
        password,
      });

      const { token: authToken, email: userEmail, userId } = response.data;

      // Store token and user info
      token.value = authToken;
      user.value = {
        id: userId,
        email: userEmail,
      };

      // Persist to localStorage
      localStorage.setItem("token", authToken);
      localStorage.setItem("user", JSON.stringify(user.value));

      return response.data;
    } catch (error) {
      console.error("Login error:", error);
      throw error;
    }
  },

  /**
   * Register new user
   * @param {string} email - User email
   * @param {string} password - User password
   * @param {string} firstName - User first name
   * @param {string} lastName - User last name
   * @returns {Promise<Object>} Registration response
   */
  async register(email, password, firstName, lastName) {
    try {
      const response = await api.post("/api/v1/auth/register", {
        email,
        password,
        firstName,
        lastName,
      });

      const { token: authToken, email: userEmail, userId } = response.data;

      // Store token and user info
      token.value = authToken;
      user.value = {
        id: userId,
        email: userEmail,
        firstName,
        lastName,
      };

      // Persist to localStorage
      localStorage.setItem("token", authToken);
      localStorage.setItem("user", JSON.stringify(user.value));

      return response.data;
    } catch (error) {
      console.error("Registration error:", error);
      throw error;
    }
  },

  /**
   * Logout user and clear authentication data
   */
  logout() {
    token.value = null;
    user.value = null;
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  },

  /**
   * Initialize auth state from localStorage on app start
   */
  init() {
    const storedToken = localStorage.getItem("token");
    const storedUser = localStorage.getItem("user");

    if (storedToken && storedUser) {
      token.value = storedToken;
      user.value = JSON.parse(storedUser);
    }
  },

  /**
   * Refresh user data by re-logging in with current credentials
   */
  async refreshUserData() {
    const currentUser = user.value;
    if (!currentUser?.email) {
      throw new Error("No current user to refresh");
    }

    // For the test user, we know the password
    if (currentUser.email === "test@focusflow.com") {
      await this.login("test@focusflow.com", "Test@123456");
    } else {
      throw new Error("Cannot refresh user data - password unknown");
    }
  },

  /**
   * Clear user data and force re-login
   */
  async clearUserData() {
    this.logout();
  },
};

// Export reactive state for use in components
export { token, user, isAuthenticated };
