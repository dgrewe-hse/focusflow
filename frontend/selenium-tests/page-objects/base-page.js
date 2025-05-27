/**
 * Base Page Object
 * Contains common functionality for all page objects
 */

const config = require("../config/test-config");

class BasePage {
  constructor(webDriverManager) {
    this.driver = webDriverManager;
    this.config = config;
  }

  /**
   * Navigate to a specific path
   * @param {string} path - Path to navigate to
   * @returns {Promise<void>}
   */
  async navigateTo(path = "") {
    const url = `${this.config.baseUrl}${path}`;
    await this.driver.navigateTo(url);
  }

  /**
   * Wait for page to load by checking for a specific element
   * @param {string} selector - CSS selector to wait for
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<void>}
   */
  async waitForPageLoad(selector, timeout = this.config.timeouts.explicit) {
    await this.driver.findElement(selector, timeout);
  }

  /**
   * Check if user is logged in by looking for common authenticated elements
   * @returns {Promise<boolean>}
   */
  async isLoggedIn() {
    try {
      // Look for logout button or user profile elements
      const loggedInElements = [
        this.config.selectors.logoutButton,
        this.config.selectors.profileLink,
        ".user-menu",
        ".profile-dropdown",
        '[data-testid="user-avatar"]',
      ].join(", ");

      return await this.driver.elementExists(loggedInElements, 3000);
    } catch (error) {
      return false;
    }
  }

  /**
   * Wait for loading to complete
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<void>}
   */
  async waitForLoadingToComplete(timeout = this.config.timeouts.explicit) {
    try {
      // Wait for loading spinner to appear and then disappear
      const loadingSelectors = this.config.selectors.loadingSpinner;

      // First check if loading spinner exists
      if (await this.driver.elementExists(loadingSelectors, 2000)) {
        // Wait for it to disappear
        await this.driver.waitForElementToDisappear(loadingSelectors, timeout);
      }
    } catch (error) {
      // Loading spinner might not exist, which is fine
      console.log("No loading spinner found or already completed");
    }
  }

  /**
   * Check for success message
   * @param {string} expectedMessage - Expected success message (optional)
   * @returns {Promise<boolean>}
   */
  async hasSuccessMessage(expectedMessage = null) {
    try {
      const messageExists = await this.driver.elementExists(
        this.config.selectors.successMessage,
        5000
      );

      if (messageExists && expectedMessage) {
        const actualMessage = await this.driver.getText(
          this.config.selectors.successMessage
        );
        return actualMessage
          .toLowerCase()
          .includes(expectedMessage.toLowerCase());
      }

      return messageExists;
    } catch (error) {
      return false;
    }
  }

  /**
   * Check for error message
   * @param {string} expectedMessage - Expected error message (optional)
   * @returns {Promise<boolean>}
   */
  async hasErrorMessage(expectedMessage = null) {
    try {
      const messageExists = await this.driver.elementExists(
        this.config.selectors.errorMessage,
        5000
      );

      if (messageExists && expectedMessage) {
        const actualMessage = await this.driver.getText(
          this.config.selectors.errorMessage
        );
        return actualMessage
          .toLowerCase()
          .includes(expectedMessage.toLowerCase());
      }

      return messageExists;
    } catch (error) {
      return false;
    }
  }

  /**
   * Close modal or dialog
   * @returns {Promise<void>}
   */
  async closeModal() {
    try {
      // Try to find and click close button
      const closeButtons = [
        this.config.selectors.cancelButton,
        ".modal-close",
        ".dialog-close",
        '[data-testid="close-button"]',
        ".close-btn",
      ].join(", ");

      if (await this.driver.elementExists(closeButtons, 2000)) {
        await this.driver.clickElement(closeButtons);
      }
    } catch (error) {
      console.log("Could not close modal:", error.message);
    }
  }

  /**
   * Confirm action in modal
   * @returns {Promise<void>}
   */
  async confirmAction() {
    await this.driver.clickElement(this.config.selectors.confirmButton);
  }

  /**
   * Cancel action in modal
   * @returns {Promise<void>}
   */
  async cancelAction() {
    await this.driver.clickElement(this.config.selectors.cancelButton);
  }

  /**
   * Get current page title
   * @returns {Promise<string>}
   */
  async getPageTitle() {
    return await this.driver.getTitle();
  }

  /**
   * Get current URL
   * @returns {Promise<string>}
   */
  async getCurrentUrl() {
    return await this.driver.getCurrentUrl();
  }

  /**
   * Take screenshot with descriptive name
   * @param {string} description - Description for screenshot
   * @returns {Promise<void>}
   */
  async takeScreenshot(description) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, "-");
    const filename = `${description}-${timestamp}`;
    await this.driver.takeScreenshot(filename);
  }

  /**
   * Scroll to element
   * @param {string} selector - CSS selector
   * @returns {Promise<void>}
   */
  async scrollToElement(selector) {
    const element = await this.driver.findElement(selector);
    await this.driver.executeScript(
      "arguments[0].scrollIntoView(true);",
      element
    );
  }

  /**
   * Wait for element to be visible
   * @param {string} selector - CSS selector
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<void>}
   */
  async waitForElementVisible(
    selector,
    timeout = this.config.timeouts.explicit
  ) {
    await this.driver.findElement(selector, timeout);
  }

  /**
   * Refresh the page
   * @returns {Promise<void>}
   */
  async refreshPage() {
    await this.driver.executeScript("window.location.reload();");
    await this.driver.waitForPageLoad();
  }

  /**
   * Navigate back
   * @returns {Promise<void>}
   */
  async navigateBack() {
    await this.driver.executeScript("window.history.back();");
    await this.driver.waitForPageLoad();
  }

  /**
   * Clear all form inputs on the page
   * @returns {Promise<void>}
   */
  async clearAllInputs() {
    const inputs = await this.driver.findElements(
      'input[type="text"], input[type="email"], input[type="password"], textarea'
    );

    for (const input of inputs) {
      try {
        await input.clear();
      } catch (error) {
        // Input might not be editable
        continue;
      }
    }
  }

  /**
   * Wait for any of multiple elements to appear
   * @param {Array<string>} selectors - Array of CSS selectors
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<string>} The selector that was found
   */
  async waitForAnyElement(selectors, timeout = this.config.timeouts.explicit) {
    const startTime = Date.now();

    while (Date.now() - startTime < timeout) {
      for (const selector of selectors) {
        if (await this.driver.elementExists(selector, 1000)) {
          return selector;
        }
      }
      await new Promise((resolve) => setTimeout(resolve, 500));
    }

    throw new Error(`None of the elements were found: ${selectors.join(", ")}`);
  }

  /**
   * Type text slowly (character by character)
   * Useful for inputs with validation or auto-complete
   * @param {string} selector - CSS selector
   * @param {string} text - Text to type
   * @param {number} delay - Delay between characters in milliseconds
   * @returns {Promise<void>}
   */
  async typeSlowly(selector, text, delay = 100) {
    const element = await this.driver.waitForClickableElement(selector);
    await element.clear();

    for (const char of text) {
      await element.sendKeys(char);
      await new Promise((resolve) => setTimeout(resolve, delay));
    }
  }
}

module.exports = BasePage;
