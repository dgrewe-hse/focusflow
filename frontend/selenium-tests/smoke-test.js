/**
 * Selenium Smoke Test
 * Simple test to verify browser initialization works in container environment
 */

const WebDriverManager = require("./utils/webdriver-manager");
const config = require("./config/test-config");

async function smokeTest() {
  console.log("🧪 Starting Selenium Smoke Test...");
  console.log(`📦 Container mode: ${config.containerMode}`);
  console.log(`🌐 Browser: ${config.browser}`);
  console.log(`👁️  Headless: ${config.headless}`);

  let driver = null;

  try {
    // Initialize WebDriver
    console.log("🚀 Initializing WebDriver...");
    driver = new WebDriverManager();
    await driver.initializeDriver();

    // Test basic navigation
    console.log("🌐 Testing navigation to a simple page...");
    await driver.navigateTo(
      "data:text/html,<html><body><h1>Selenium Test</h1></body></html>"
    );

    // Get page title
    const title = await driver.getTitle();
    console.log(`📄 Page title: "${title}"`);

    // Take a screenshot
    console.log("📸 Taking screenshot...");
    await driver.takeScreenshot("smoke-test");

    console.log("✅ Smoke test passed!");
    return true;
  } catch (error) {
    console.error("❌ Smoke test failed:", error.message);
    console.error("Stack trace:", error.stack);
    return false;
  } finally {
    if (driver) {
      console.log("🧹 Cleaning up...");
      await driver.quitDriver();
    }
  }
}

// Run smoke test if called directly
if (require.main === module) {
  smokeTest()
    .then((success) => {
      process.exit(success ? 0 : 1);
    })
    .catch((error) => {
      console.error("💥 Unexpected error:", error);
      process.exit(1);
    });
}

module.exports = smokeTest;
