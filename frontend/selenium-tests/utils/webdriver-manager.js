/**
 * WebDriver Manager
 * Manages WebDriver instances and provides common browser operations
 */

const { Builder, By, until, Key } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");
const firefox = require("selenium-webdriver/firefox");
const config = require("../config/test-config");

class WebDriverManager {
  constructor() {
    this.driver = null;
  }

  /**
   * Initialize WebDriver with the specified browser
   * @param {string} browserName - Browser name (chrome, firefox)
   * @returns {Promise<void>}
   */
  async initializeDriver(browserName = config.browser) {
    console.log(`🌐 Initializing ${browserName} WebDriver...`);
    console.log(`📦 Container mode: ${config.containerMode}`);
    console.log(`🖥️  Headless mode: ${config.headless}`);

    try {
      const options = this.getBrowserOptions(browserName);

      // Set up the builder with proper browser options
      const builder = new Builder().forBrowser(browserName);

      if (browserName === "chrome") {
        builder.setChromeOptions(options.chrome);
      } else if (browserName === "firefox") {
        builder.setFirefoxOptions(options.firefox);
      }

      this.driver = await builder.build();

      // Configure timeouts
      await this.driver.manage().setTimeouts({
        implicit: config.timeouts.implicit,
        pageLoad: config.timeouts.pageLoad,
        script: config.timeouts.script,
      });

      console.log(`✅ ${browserName} WebDriver initialized successfully`);
    } catch (error) {
      console.error(
        `❌ Failed to initialize ${browserName} WebDriver:`,
        error.message
      );
      console.log(`🔧 Attempting to install browser drivers...`);

      // Try to install drivers and retry
      await this.installDrivers(browserName);
      throw error;
    }
  }

  /**
   * Install browser drivers if missing
   * @param {string} browserName - Browser name
   */
  async installDrivers(browserName) {
    console.log(`⚠️  WebDriver initialization failed for ${browserName}`);
    console.log("💡 Possible solutions:");
    console.log("   1. Make sure your browser is installed and up to date");
    console.log("   2. Try running in headless mode: HEADLESS=true");
    console.log("   3. Try a different browser: BROWSER=firefox");

    const os = require("os");
    const platform = os.platform();

    if (platform === "darwin") {
      console.log("🍎 macOS detected - Browser installation tips:");
      console.log("   • Chrome: Download from https://www.google.com/chrome/");
      console.log(
        "   • Firefox: Download from https://www.mozilla.org/firefox/"
      );
      console.log("   • Or use Homebrew: brew install --cask google-chrome");
    } else if (platform === "linux") {
      console.log("🐧 Linux detected - You may need to install browsers:");
      console.log("   • Chrome: sudo apt-get install google-chrome-stable");
      console.log("   • Firefox: sudo apt-get install firefox");
    } else {
      console.log(
        "🪟 Windows detected - Download browsers from official sites"
      );
    }

    // Don't attempt automatic installation - let user handle it
  }

  /**
   * Get browser-specific options
   * @param {string} browserName - Browser name
   * @returns {object} Browser options
   */
  getBrowserOptions(browserName) {
    const chromeOptions = new chrome.Options();
    const firefoxOptions = new firefox.Options();

    // Chrome options
    config.chromeOptions.forEach((option) =>
      chromeOptions.addArguments(option)
    );

    // Always run headless in container environments
    if (config.headless || config.containerMode) {
      chromeOptions.addArguments("--headless=new");
    }

    // Set Chrome binary path if available - OS-specific paths
    const os = require("os");
    const platform = os.platform();

    let chromeBinaryPaths = [];

    if (platform === "darwin") {
      // macOS paths
      chromeBinaryPaths = [
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        "/Applications/Chromium.app/Contents/MacOS/Chromium",
        "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary",
      ];
    } else if (platform === "linux") {
      // Linux paths
      chromeBinaryPaths = [
        "/usr/bin/google-chrome",
        "/usr/bin/google-chrome-stable",
        "/usr/bin/chromium-browser",
        "/usr/bin/chromium",
        "/opt/google/chrome/chrome",
        "/snap/bin/chromium",
      ];
    } else if (platform === "win32") {
      // Windows paths
      chromeBinaryPaths = [
        "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
        "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
        "C:\\Users\\%USERNAME%\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe",
      ];
    }

    for (const path of chromeBinaryPaths) {
      try {
        const fs = require("fs");
        if (fs.existsSync(path)) {
          chromeOptions.setChromeBinaryPath(path);
          console.log(`🔍 Found Chrome binary at: ${path}`);
          break;
        }
      } catch (e) {
        // Continue checking
      }
    }

    // Firefox options
    config.firefoxOptions.forEach((option) =>
      firefoxOptions.addArguments(option)
    );

    if (config.headless || config.containerMode) {
      firefoxOptions.addArguments("--headless");
    }

    // Set Firefox binary path if available - OS-specific paths
    let firefoxBinaryPaths = [];

    if (platform === "darwin") {
      // macOS paths
      firefoxBinaryPaths = [
        "/Applications/Firefox.app/Contents/MacOS/firefox",
        "/Applications/Firefox Developer Edition.app/Contents/MacOS/firefox",
      ];
    } else if (platform === "linux") {
      // Linux paths
      firefoxBinaryPaths = [
        "/usr/bin/firefox",
        "/usr/bin/firefox-esr",
        "/opt/firefox/firefox",
        "/snap/bin/firefox",
      ];
    } else if (platform === "win32") {
      // Windows paths
      firefoxBinaryPaths = [
        "C:\\Program Files\\Mozilla Firefox\\firefox.exe",
        "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe",
      ];
    }

    for (const path of firefoxBinaryPaths) {
      try {
        const fs = require("fs");
        if (fs.existsSync(path)) {
          firefoxOptions.setBinary(path);
          console.log(`🔍 Found Firefox binary at: ${path}`);
          break;
        }
      } catch (e) {
        // Continue checking
      }
    }

    return {
      chrome: chromeOptions,
      firefox: firefoxOptions,
    };
  }

  /**
   * Navigate to a URL
   * @param {string} url - URL to navigate to
   * @returns {Promise<void>}
   */
  async navigateTo(url) {
    if (!this.driver) {
      throw new Error(
        "WebDriver not initialized. Call initializeDriver() first."
      );
    }

    console.log(`🌐 Navigating to: ${url}`);
    await this.driver.get(url);
    await this.waitForPageLoad();
  }

  /**
   * Wait for page to load completely
   * @returns {Promise<void>}
   */
  async waitForPageLoad() {
    await this.driver.wait(
      async () =>
        (await this.driver.executeScript("return document.readyState")) ===
        "complete",
      config.timeouts.pageLoad
    );
  }

  /**
   * Find element with multiple selector strategies
   * @param {string} selectors - CSS selectors (comma-separated)
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<WebElement>}
   */
  async findElement(selectors, timeout = config.timeouts.explicit) {
    const selectorArray = selectors.split(",").map((s) => s.trim());

    for (const selector of selectorArray) {
      try {
        const element = await this.driver.wait(
          until.elementLocated(By.css(selector)),
          timeout
        );
        return element;
      } catch (error) {
        // Continue to next selector
        continue;
      }
    }

    throw new Error(
      `Could not locate element with any of these selectors: ${selectors}`
    );
  }

  /**
   * Find multiple elements with selector strategies
   * @param {string} selectors - CSS selectors (comma-separated)
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<Array<WebElement>>}
   */
  async findElements(selectors, timeout = config.timeouts.explicit) {
    const selectorArray = selectors.split(",").map((s) => s.trim());

    for (const selector of selectorArray) {
      try {
        await this.driver.wait(until.elementLocated(By.css(selector)), timeout);
        const elements = await this.driver.findElements(By.css(selector));
        if (elements.length > 0) {
          return elements;
        }
      } catch (error) {
        // Continue to next selector
        continue;
      }
    }

    return [];
  }

  /**
   * Wait for element to be visible and clickable
   * @param {string} selectors - CSS selectors
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<WebElement>}
   */
  async waitForClickableElement(selectors, timeout = config.timeouts.explicit) {
    const element = await this.findElement(selectors, timeout);
    await this.driver.wait(until.elementIsEnabled(element), timeout);
    await this.driver.wait(until.elementIsVisible(element), timeout);
    return element;
  }

  /**
   * Click element with retry mechanism
   * @param {string} selectors - CSS selectors
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<void>}
   */
  async clickElement(selectors, timeout = config.timeouts.explicit) {
    const element = await this.findElement(selectors, timeout);

    // Wait for element to be enabled
    await this.driver.wait(until.elementIsEnabled(element), timeout);

    try {
      await element.click();
    } catch (error) {
      // Fallback: try JavaScript click
      await this.driver.executeScript("arguments[0].click();", element);
    }
  }

  /**
   * Type text into element
   * @param {string} selectors - CSS selectors
   * @param {string} text - Text to type
   * @param {boolean} clearFirst - Whether to clear existing text first
   * @returns {Promise<void>}
   */
  async typeText(selectors, text, clearFirst = true) {
    // For Vuetify components, use a more direct approach
    const element = await this.findElement(selectors);

    // Wait for element to be present and enabled
    await this.driver.wait(
      until.elementIsEnabled(element),
      config.timeouts.explicit
    );

    // Try to click first to focus (works better with Vuetify)
    try {
      await element.click();
    } catch (error) {
      // Fallback: JavaScript click
      await this.driver.executeScript("arguments[0].click();", element);
    }

    if (clearFirst && text !== "") {
      // More robust clearing for Vuetify components
      await this.clearFormField(element);
    }

    if (text !== "") {
      await element.sendKeys(text);
    }
  }

  /**
   * Clear form field with multiple strategies for Vuetify compatibility
   * @param {WebElement} element - The element to clear
   * @returns {Promise<void>}
   */
  async clearFormField(element) {
    try {
      // Strategy 1: Standard clear
      await element.clear();

      // Strategy 2: Select all and delete (for stubborn inputs)
      const { Key } = require("selenium-webdriver");
      await element.sendKeys(Key.CONTROL + "a");
      await element.sendKeys(Key.DELETE);

      // Strategy 3: JavaScript clear (fallback for Vuetify)
      await this.driver.executeScript("arguments[0].value = '';", element);

      // Trigger input event for Vue reactivity
      await this.driver.executeScript(
        `
        const event = new Event('input', { bubbles: true });
        arguments[0].dispatchEvent(event);
      `,
        element
      );
    } catch (error) {
      console.log("⚠️  Could not clear field:", error.message);
    }
  }

  /**
   * Get text content of element
   * @param {string} selectors - CSS selectors
   * @returns {Promise<string>}
   */
  async getText(selectors) {
    const element = await this.findElement(selectors);
    return await element.getText();
  }

  /**
   * Check if element exists
   * @param {string} selectors - CSS selectors
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<boolean>}
   */
  async elementExists(selectors, timeout = 5000) {
    try {
      await this.findElement(selectors, timeout);
      return true;
    } catch (error) {
      return false;
    }
  }

  /**
   * Wait for element to disappear
   * @param {string} selectors - CSS selectors
   * @param {number} timeout - Timeout in milliseconds
   * @returns {Promise<void>}
   */
  async waitForElementToDisappear(
    selectors,
    timeout = config.timeouts.explicit
  ) {
    const selectorArray = selectors.split(",").map((s) => s.trim());

    for (const selector of selectorArray) {
      try {
        await this.driver.wait(
          until.stalenessOf(await this.driver.findElement(By.css(selector))),
          timeout
        );
        return;
      } catch (error) {
        // Element might not exist, which is what we want
        return;
      }
    }
  }

  /**
   * Take screenshot
   * @param {string} filename - Screenshot filename
   * @returns {Promise<void>}
   */
  async takeScreenshot(filename) {
    if (!this.driver) return;

    try {
      const screenshot = await this.driver.takeScreenshot();
      const fs = require("fs");
      const path = require("path");

      const screenshotDir = path.join(__dirname, "../screenshots");
      if (!fs.existsSync(screenshotDir)) {
        fs.mkdirSync(screenshotDir, { recursive: true });
      }

      const filepath = path.join(
        screenshotDir,
        `${filename}-${Date.now()}.png`
      );
      fs.writeFileSync(filepath, screenshot, "base64");
      console.log(`📸 Screenshot saved: ${filepath}`);
    } catch (error) {
      console.log(`⚠️  Could not take screenshot: ${error.message}`);
    }
  }

  /**
   * Execute JavaScript in browser
   * @param {string} script - JavaScript code to execute
   * @param {Array} args - Arguments to pass to script
   * @returns {Promise<any>}
   */
  async executeScript(script, ...args) {
    return await this.driver.executeScript(script, ...args);
  }

  /**
   * Get current URL
   * @returns {Promise<string>}
   */
  async getCurrentUrl() {
    return await this.driver.getCurrentUrl();
  }

  /**
   * Get page title
   * @returns {Promise<string>}
   */
  async getTitle() {
    return await this.driver.getTitle();
  }

  /**
   * Close current browser window
   * @returns {Promise<void>}
   */
  async closeBrowser() {
    if (this.driver) {
      await this.driver.close();
    }
  }

  /**
   * Quit WebDriver (close all windows)
   * @returns {Promise<void>}
   */
  async quitDriver() {
    if (this.driver) {
      try {
        await this.driver.quit();
        console.log("✅ WebDriver quit successfully");
      } catch (error) {
        console.error("⚠️  Error quitting WebDriver:", error.message);
      } finally {
        this.driver = null;
      }
    }
  }
}

module.exports = WebDriverManager;
