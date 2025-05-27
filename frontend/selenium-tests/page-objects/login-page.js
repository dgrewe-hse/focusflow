/**
 * Login Page Object
 * Handles all login-related interactions
 */

const BasePage = require("./base-page");

class LoginPage extends BasePage {
  constructor(webDriverManager) {
    super(webDriverManager);

    // Page-specific selectors
    this.selectors = {
      // Login form elements - Updated to match Vue component data-cy attributes
      emailInput: this.config.selectors.loginEmailInput,
      passwordInput: this.config.selectors.loginPasswordInput,
      loginButton: this.config.selectors.loginButton,

      // Additional login-specific elements
      forgotPasswordLink:
        '[data-cy="forgot-password"], [data-testid="forgot-password"], .forgot-password-link',
      registerLink:
        '[data-cy="register-link"], [data-testid="register-link"], .register-link',
      loginForm:
        '[data-cy="login-form"], [data-testid="login-form"], .v-form, .login-form, form',

      // Validation messages - Vuetify specific
      emailError:
        '[data-cy="email-error"], .v-messages__message, .email-error, .field-error',
      passwordError:
        '[data-cy="password-error"], .v-messages__message, .password-error, .field-error',

      // Page elements
      pageTitle: "h1, h2, .v-card-title, .page-title",
      logo: '.logo, [data-cy="logo"], [data-testid="logo"]',
    };
  }

  /**
   * Navigate to login page
   * @returns {Promise<void>}
   */
  async open() {
    await this.navigateTo("/login");
    await this.waitForLoginFormToLoad();
  }

  /**
   * Wait for login form to load
   * @returns {Promise<void>}
   */
  async waitForLoginFormToLoad() {
    // Use faster, more efficient approach
    console.log("⏳ Waiting for login form to load...");

    // Check for any of the key elements with reduced timeout
    await this.driver.elementExists(this.selectors.emailInput, 5000);

    // Brief wait for Vue/Vuetify to stabilize
    await new Promise((resolve) => setTimeout(resolve, 500));

    console.log("✅ Login form loaded");
  }

  /**
   * Perform login with credentials
   * @param {string} email - User email
   * @param {string} password - User password
   * @returns {Promise<void>}
   */
  async login(email, password) {
    console.log(`🔐 Logging in with email: ${email}`);

    // Clear and enter email
    await this.driver.typeText(this.selectors.emailInput, email);

    // Clear and enter password
    await this.driver.typeText(this.selectors.passwordInput, password);

    // Click login button
    await this.driver.clickElement(this.selectors.loginButton);

    // Wait for navigation or error message
    await this.waitForLoginResult();
  }

  /**
   * Login with test user credentials
   * @returns {Promise<void>}
   */
  async loginWithTestUser() {
    await this.login(this.config.testUser.email, this.config.testUser.password);
  }

  /**
   * Wait for login result (success or failure)
   * @returns {Promise<boolean>} True if login successful, false otherwise
   */
  async waitForLoginResult() {
    try {
      // Wait for either success (redirect) or error message
      const resultSelectors = [
        this.config.selectors.errorMessage,
        this.config.selectors.successMessage,
        ".dashboard", // Assuming successful login redirects to dashboard
        '[data-testid="dashboard"]',
      ];

      const foundSelector = await this.waitForAnyElement(
        resultSelectors,
        10000
      );

      // Check if it's an error message
      if (foundSelector.includes("error")) {
        console.log("❌ Login failed");
        return false;
      }

      // Check if we're on a different page (successful login)
      const currentUrl = await this.getCurrentUrl();
      if (!currentUrl.includes("/login")) {
        console.log("✅ Login successful");
        return true;
      }

      return false;
    } catch (error) {
      console.log("⚠️ Login result unclear:", error.message);
      return false;
    }
  }

  /**
   * Check if login form is displayed
   * @returns {Promise<boolean>}
   */
  async isLoginFormDisplayed() {
    return await this.driver.elementExists(this.selectors.loginForm, 5000);
  }

  /**
   * Get login form validation errors
   * @returns {Promise<Object>} Object containing validation errors
   */
  async getValidationErrors() {
    const errors = {};

    try {
      if (await this.driver.elementExists(this.selectors.emailError, 2000)) {
        errors.email = await this.driver.getText(this.selectors.emailError);
      }
    } catch (error) {
      // No email error
    }

    try {
      if (await this.driver.elementExists(this.selectors.passwordError, 2000)) {
        errors.password = await this.driver.getText(
          this.selectors.passwordError
        );
      }
    } catch (error) {
      // No password error
    }

    return errors;
  }

  /**
   * Clear login form
   * @returns {Promise<void>}
   */
  async clearLoginForm() {
    console.log("🧹 Clearing login form...");

    try {
      // Clear email field
      const emailElement = await this.driver.findElement(
        this.selectors.emailInput
      );
      await this.driver.clearFormField(emailElement);

      // Clear password field
      const passwordElement = await this.driver.findElement(
        this.selectors.passwordInput
      );
      await this.driver.clearFormField(passwordElement);

      // Brief wait for Vue reactivity
      await new Promise((resolve) => setTimeout(resolve, 500));

      console.log("✅ Login form cleared");
    } catch (error) {
      console.log("⚠️  Could not clear form completely:", error.message);
      // Fallback: refresh the page
      await this.refreshPage();
    }
  }

  /**
   * Refresh the current page
   * @returns {Promise<void>}
   */
  async refreshPage() {
    console.log("🔄 Refreshing page...");
    await this.driver.executeScript("window.location.reload();");
    await this.waitForLoginFormToLoad();
    console.log("✅ Page refreshed");
  }

  /**
   * Click forgot password link
   * @returns {Promise<void>}
   */
  async clickForgotPassword() {
    await this.driver.clickElement(this.selectors.forgotPasswordLink);
  }

  /**
   * Click register link
   * @returns {Promise<void>}
   */
  async clickRegisterLink() {
    await this.driver.clickElement(this.selectors.registerLink);
  }

  /**
   * Get page title text
   * @returns {Promise<string>}
   */
  async getPageTitleText() {
    try {
      return await this.driver.getText(this.selectors.pageTitle);
    } catch (error) {
      return "";
    }
  }

  /**
   * Check if logo is displayed
   * @returns {Promise<boolean>}
   */
  async isLogoDisplayed() {
    return await this.driver.elementExists(this.selectors.logo, 3000);
  }

  /**
   * Attempt login with invalid credentials
   * @param {string} email - Invalid email
   * @param {string} password - Invalid password
   * @returns {Promise<boolean>} True if error message is shown
   */
  async attemptInvalidLogin(email, password) {
    await this.login(email, password);

    // Check for error message
    return await this.hasErrorMessage();
  }

  /**
   * Check if login button is enabled
   * @returns {Promise<boolean>}
   */
  async isLoginButtonEnabled() {
    try {
      const button = await this.driver.findElement(this.selectors.loginButton);
      const isEnabled = await button.isEnabled();
      return isEnabled;
    } catch (error) {
      return false;
    }
  }

  /**
   * Get email input value
   * @returns {Promise<string>}
   */
  async getEmailValue() {
    try {
      const emailInput = await this.driver.findElement(
        this.selectors.emailInput
      );
      return await emailInput.getAttribute("value");
    } catch (error) {
      return "";
    }
  }

  /**
   * Get password input value
   * @returns {Promise<string>}
   */
  async getPasswordValue() {
    try {
      const passwordInput = await this.driver.findElement(
        this.selectors.passwordInput
      );
      return await passwordInput.getAttribute("value");
    } catch (error) {
      return "";
    }
  }

  /**
   * Test various login scenarios
   * @returns {Promise<Object>} Test results
   */
  async runLoginValidationTests() {
    const results = {
      emptyForm: false,
      invalidEmail: false,
      invalidPassword: false,
      validCredentials: false,
    };

    try {
      // Test empty form submission
      await this.clearLoginForm();
      await this.driver.clickElement(this.selectors.loginButton);
      const errors = await this.getValidationErrors();
      results.emptyForm = Object.keys(errors).length > 0;

      // Test invalid email format
      await this.clearLoginForm();
      await this.driver.typeText(this.selectors.emailInput, "invalid-email");
      await this.driver.typeText(this.selectors.passwordInput, "password123");
      await this.driver.clickElement(this.selectors.loginButton);
      results.invalidEmail = await this.hasErrorMessage();

      // Test invalid credentials
      await this.clearLoginForm();
      await this.driver.typeText(
        this.selectors.emailInput,
        "invalid@example.com"
      );
      await this.driver.typeText(this.selectors.passwordInput, "wrongpassword");
      await this.driver.clickElement(this.selectors.loginButton);
      results.invalidPassword = await this.hasErrorMessage();

      // Test valid credentials
      await this.clearLoginForm();
      await this.loginWithTestUser();
      results.validCredentials = await this.waitForLoginResult();
    } catch (error) {
      console.error("Error during login validation tests:", error.message);
    }

    return results;
  }

  /**
   * Check if remember me checkbox exists and interact with it
   * @param {boolean} check - Whether to check the checkbox
   * @returns {Promise<boolean>} True if checkbox exists and was interacted with
   */
  async handleRememberMe(check = true) {
    const rememberMeSelector =
      '[data-testid="remember-me"], input[type="checkbox"], .remember-me';

    try {
      if (await this.driver.elementExists(rememberMeSelector, 2000)) {
        const checkbox = await this.driver.findElement(rememberMeSelector);
        const isChecked = await checkbox.isSelected();

        if (check !== isChecked) {
          await this.driver.clickElement(rememberMeSelector);
        }

        return true;
      }
    } catch (error) {
      console.log("Remember me checkbox not found or not interactable");
    }

    return false;
  }
}

module.exports = LoginPage;
