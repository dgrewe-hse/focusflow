#!/usr/bin/env node

/**
 * Selenium Test Runner
 * Orchestrates the execution of Selenium test suites
 */

const { spawn } = require("child_process");
const path = require("path");
const fs = require("fs");

// Test configuration
const testConfig = {
  suites: {
    auth: {
      name: "Authentication Tests (Core)",
      files: ["tests/auth-core.test.js"],
      description: "Fast core authentication tests (login, validation, API)",
    },
    "auth-core": {
      name: "Authentication Tests (Core)",
      files: ["tests/auth-core.test.js"],
      description: "Fast core authentication tests (login, validation, API)",
    },
    "auth-comprehensive": {
      name: "Authentication Tests (Comprehensive)",
      files: ["tests/auth-comprehensive.test.js"],
      description: "Complete auth tests (security, flows, edge cases)",
    },
    tasks: {
      name: "Task Management Tests",
      files: ["tests/tasks.test.js"],
      description: "Tests CRUD operations, search, and filtering",
    },
    integration: {
      name: "Integration Tests",
      files: ["tests/integration.test.js"],
      description: "Tests complete workflows and UI/API integration",
    },
    all: {
      name: "All Tests",
      files: ["tests/**/*.test.js"],
      description: "Runs all test suites",
    },
    "all-core": {
      name: "All Core Tests",
      files: [
        "tests/auth-core.test.js",
        "tests/tasks.test.js",
        "tests/integration.test.js",
      ],
      description: "Runs core test suites (faster execution)",
    },
  },
  defaultTimeout: 300000, // 5 minutes
  retries: 1,
};

// Colors for console output
const colors = {
  reset: "\x1b[0m",
  bright: "\x1b[1m",
  red: "\x1b[31m",
  green: "\x1b[32m",
  yellow: "\x1b[33m",
  blue: "\x1b[34m",
  magenta: "\x1b[35m",
  cyan: "\x1b[36m",
};

function colorize(text, color) {
  return `${colors[color]}${text}${colors.reset}`;
}

function printBanner() {
  console.log(colorize("\n" + "=".repeat(80), "cyan"));
  console.log(
    colorize("                    🧪 SELENIUM TEST RUNNER 🧪", "cyan")
  );
  console.log(
    colorize("                   FocusFlow UI & API Testing", "cyan")
  );
  console.log(colorize("=".repeat(80) + "\n", "cyan"));
}

function printUsage() {
  console.log(colorize("Usage:", "bright"));
  console.log("  npm run test:selenium [suite] [options]");
  console.log("  node run-tests.js [suite] [options]\n");

  console.log(colorize("Available Test Suites:", "bright"));
  Object.entries(testConfig.suites).forEach(([key, suite]) => {
    console.log(
      `  ${colorize(key.padEnd(12), "green")} - ${suite.description}`
    );
  });

  console.log(colorize("\nOptions:", "bright"));
  console.log("  --browser=<name>     Browser to use (chrome, firefox)");
  console.log("  --headless           Run in headless mode");
  console.log("  --timeout=<ms>       Test timeout in milliseconds");
  console.log("  --retries=<n>        Number of retries for failed tests");
  console.log("  --grep=<pattern>     Run only tests matching pattern");
  console.log("  --reporter=<name>    Test reporter (spec, json, html)");
  console.log("  --help               Show this help message\n");

  console.log(colorize("Environment Variables:", "bright"));
  console.log(
    "  BASE_URL             Frontend URL (default: http://localhost:8081)"
  );
  console.log(
    "  API_BASE_URL         Backend API URL (default: http://localhost:8080/api/v1)"
  );
  console.log(
    "  TEST_EMAIL           Test user email (default: test@focusflow.com)"
  );
  console.log(
    "  TEST_PASSWORD        Test user password (default: Test@123456)"
  );
  console.log("  BROWSER              Browser to use (default: chrome)");
  console.log("  HEADLESS             Run headless (default: false)\n");

  console.log(colorize("Examples:", "bright"));
  console.log("  npm run test:selenium auth");
  console.log("  npm run test:selenium tasks --headless");
  console.log("  npm run test:selenium all --browser=firefox");
  console.log('  node run-tests.js integration --grep="workflow"');
}

function parseArguments() {
  const args = process.argv.slice(2);
  const options = {
    suite: "all",
    browser: process.env.BROWSER || "chrome",
    headless: process.env.HEADLESS === "true",
    timeout: testConfig.defaultTimeout,
    retries: testConfig.retries,
    grep: null,
    reporter: "spec",
    help: false,
  };

  // Parse suite argument
  if (args.length > 0 && !args[0].startsWith("--")) {
    options.suite = args[0];
  }

  // Parse options
  args.forEach((arg) => {
    if (arg.startsWith("--")) {
      const [key, value] = arg.substring(2).split("=");
      switch (key) {
        case "browser":
          options.browser = value || "chrome";
          break;
        case "headless":
          options.headless = true;
          break;
        case "timeout":
          options.timeout = parseInt(value) || testConfig.defaultTimeout;
          break;
        case "retries":
          options.retries = parseInt(value) || testConfig.retries;
          break;
        case "grep":
          options.grep = value;
          break;
        case "reporter":
          options.reporter = value || "spec";
          break;
        case "help":
          options.help = true;
          break;
      }
    }
  });

  return options;
}

function validateSuite(suite) {
  if (!testConfig.suites[suite]) {
    console.error(colorize(`❌ Invalid test suite: ${suite}`, "red"));
    console.log(colorize("\nAvailable suites:", "yellow"));
    Object.keys(testConfig.suites).forEach((key) => {
      console.log(`  - ${key}`);
    });
    process.exit(1);
  }
}

function checkPrerequisites() {
  console.log(colorize("🔍 Checking prerequisites...", "blue"));

  // Check if Node.js modules are installed
  const packageJsonPath = path.join(__dirname, "..", "package.json");
  const nodeModulesPath = path.join(__dirname, "..", "node_modules");

  if (!fs.existsSync(packageJsonPath)) {
    console.error(
      colorize(
        "❌ package.json not found. Please run this from the frontend directory.",
        "red"
      )
    );
    process.exit(1);
  }

  if (!fs.existsSync(nodeModulesPath)) {
    console.error(
      colorize(
        '❌ node_modules not found. Please run "npm install" first.',
        "red"
      )
    );
    process.exit(1);
  }

  // Check if required test dependencies are available
  const requiredPackages = ["selenium-webdriver", "mocha", "chai"];
  const packageJson = require(packageJsonPath);
  const allDeps = {
    ...packageJson.dependencies,
    ...packageJson.devDependencies,
  };

  const missingPackages = requiredPackages.filter((pkg) => !allDeps[pkg]);
  if (missingPackages.length > 0) {
    console.error(
      colorize(
        `❌ Missing required packages: ${missingPackages.join(", ")}`,
        "red"
      )
    );
    console.log(colorize("Please install them with: npm install", "yellow"));
    process.exit(1);
  }

  console.log(colorize("✅ Prerequisites check passed", "green"));
}

function setupEnvironment(options) {
  console.log(colorize("⚙️  Setting up test environment...", "blue"));

  // Set environment variables
  process.env.BROWSER = options.browser;
  process.env.HEADLESS = options.headless.toString();

  // Create screenshots directory
  const screenshotsDir = path.join(__dirname, "screenshots");
  if (!fs.existsSync(screenshotsDir)) {
    fs.mkdirSync(screenshotsDir, { recursive: true });
    console.log(colorize("📁 Created screenshots directory", "green"));
  }

  // Create reports directory
  const reportsDir = path.join(__dirname, "reports");
  if (!fs.existsSync(reportsDir)) {
    fs.mkdirSync(reportsDir, { recursive: true });
    console.log(colorize("📁 Created reports directory", "green"));
  }

  console.log(
    colorize(
      `🌐 Browser: ${options.browser}${options.headless ? " (headless)" : ""}`,
      "green"
    )
  );
  console.log(colorize(`⏱️  Timeout: ${options.timeout}ms`, "green"));
  console.log(colorize(`🔄 Retries: ${options.retries}`, "green"));

  if (options.grep) {
    console.log(colorize(`🔍 Filter: ${options.grep}`, "green"));
  }
}

function runTestSuite(suite, options) {
  return new Promise((resolve, reject) => {
    console.log(
      colorize(`\n🚀 Running ${testConfig.suites[suite].name}...`, "blue")
    );
    console.log(colorize(`📝 ${testConfig.suites[suite].description}`, "cyan"));

    const mochaArgs = [
      "--timeout",
      options.timeout.toString(),
      "--retries",
      options.retries.toString(),
      "--reporter",
      options.reporter,
    ];

    // Add grep filter if specified
    if (options.grep) {
      mochaArgs.push("--grep", options.grep);
    }

    // Add test files
    const testFiles = testConfig.suites[suite].files;
    mochaArgs.push(...testFiles);

    // Set up output file for reports
    if (options.reporter === "json") {
      const reportFile = path.join(
        __dirname,
        "reports",
        `${suite}-results.json`
      );
      mochaArgs.push("--reporter-options", `output=${reportFile}`);
    } else if (options.reporter === "html") {
      const reportFile = path.join(
        __dirname,
        "reports",
        `${suite}-results.html`
      );
      mochaArgs.push("--reporter-options", `output=${reportFile}`);
    }

    console.log(
      colorize(`\n📋 Command: npx mocha ${mochaArgs.join(" ")}`, "yellow")
    );
    console.log(colorize("─".repeat(80), "cyan"));

    const startTime = Date.now();
    const mochaProcess = spawn("npx", ["mocha", ...mochaArgs], {
      cwd: __dirname,
      stdio: "inherit",
      env: { ...process.env },
    });

    mochaProcess.on("close", (code) => {
      const duration = Date.now() - startTime;
      const durationStr = `${Math.round(duration / 1000)}s`;

      console.log(colorize("─".repeat(80), "cyan"));

      if (code === 0) {
        console.log(
          colorize(
            `✅ ${testConfig.suites[suite].name} completed successfully in ${durationStr}`,
            "green"
          )
        );
        resolve({ suite, success: true, duration, code });
      } else {
        console.log(
          colorize(
            `❌ ${testConfig.suites[suite].name} failed in ${durationStr} (exit code: ${code})`,
            "red"
          )
        );
        resolve({ suite, success: false, duration, code });
      }
    });

    mochaProcess.on("error", (error) => {
      console.error(
        colorize(`❌ Failed to start tests: ${error.message}`, "red")
      );
      reject(error);
    });
  });
}

function printResults(results) {
  console.log(colorize("\n" + "=".repeat(80), "cyan"));
  console.log(
    colorize("                        📊 TEST RESULTS SUMMARY", "cyan")
  );
  console.log(colorize("=".repeat(80), "cyan"));

  let totalTests = 0;
  let passedTests = 0;
  let totalDuration = 0;

  results.forEach((result) => {
    totalTests++;
    totalDuration += result.duration;

    const status = result.success
      ? colorize("✅ PASSED", "green")
      : colorize("❌ FAILED", "red");
    const duration = colorize(
      `${Math.round(result.duration / 1000)}s`,
      "yellow"
    );
    const suiteName = colorize(testConfig.suites[result.suite].name, "bright");

    console.log(`  ${status} ${suiteName} (${duration})`);

    if (result.success) {
      passedTests++;
    }
  });

  console.log(colorize("\n" + "─".repeat(80), "cyan"));

  const overallStatus =
    passedTests === totalTests
      ? colorize("✅ ALL TESTS PASSED", "green")
      : colorize(
          `❌ ${totalTests - passedTests}/${totalTests} TESTS FAILED`,
          "red"
        );

  const totalDurationStr = colorize(
    `${Math.round(totalDuration / 1000)}s`,
    "yellow"
  );

  console.log(`  ${overallStatus}`);
  console.log(`  📈 Total execution time: ${totalDurationStr}`);
  console.log(
    `  📊 Pass rate: ${Math.round((passedTests / totalTests) * 100)}%`
  );

  console.log(colorize("─".repeat(80), "cyan"));

  // Print additional information
  console.log(colorize("\n📁 Output files:", "bright"));
  console.log("  🖼️  Screenshots: ./selenium-tests/screenshots/");
  console.log("  📋 Reports: ./selenium-tests/reports/");

  if (passedTests < totalTests) {
    console.log(colorize("\n💡 Tips for debugging failures:", "yellow"));
    console.log("  1. Check screenshots in ./selenium-tests/screenshots/");
    console.log("  2. Verify backend is running on correct port");
    console.log("  3. Verify frontend is running and accessible");
    console.log("  4. Check browser compatibility");
    console.log("  5. Try running in non-headless mode for debugging");
  }

  console.log(colorize("\n=".repeat(80) + "\n", "cyan"));

  return passedTests === totalTests;
}

async function main() {
  try {
    printBanner();

    const options = parseArguments();

    if (options.help) {
      printUsage();
      process.exit(0);
    }

    validateSuite(options.suite);
    checkPrerequisites();
    setupEnvironment(options);

    console.log(colorize("\n🎯 Starting test execution...", "blue"));

    const results = [];

    if (options.suite === "all") {
      // Run all test suites
      const suites = ["auth", "tasks", "integration"];
      for (const suite of suites) {
        const result = await runTestSuite(suite, options);
        results.push(result);
      }
    } else {
      // Run single test suite
      const result = await runTestSuite(options.suite, options);
      results.push(result);
    }

    const allPassed = printResults(results);

    process.exit(allPassed ? 0 : 1);
  } catch (error) {
    console.error(colorize(`💥 Unexpected error: ${error.message}`, "red"));
    console.error(error.stack);
    process.exit(1);
  }
}

// Handle process signals
process.on("SIGINT", () => {
  console.log(colorize("\n\n⚠️  Test execution interrupted by user", "yellow"));
  process.exit(130);
});

process.on("SIGTERM", () => {
  console.log(colorize("\n\n⚠️  Test execution terminated", "yellow"));
  process.exit(143);
});

// Run the test runner
if (require.main === module) {
  main();
}

module.exports = { main, testConfig };
