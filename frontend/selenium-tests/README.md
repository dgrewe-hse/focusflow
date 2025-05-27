# Selenium Test Suite for FocusFlow Application

## 🎯 Overview

This comprehensive Selenium test suite demonstrates modern end-to-end testing practices for web applications. It's designed to test the FocusFlow Vue.js frontend in combination with its Spring Boot backend API, providing a complete testing solution that covers:

- **Authentication flows** (login, logout, session management)
- **CRUD operations** (Create, Read, Update, Delete tasks)
- **UI interactions** (forms, navigation, user feedback)
- **API integration** (frontend-backend synchronization)
- **Security testing** (input validation, access controls)
- **Performance validation** (load times, responsiveness)

## ⚠️ **Important: Running Tests Outside Dev Container**

**Recommended Approach:** Run Selenium tests on your **host machine** rather than inside the dev container to avoid browser sandboxing and display issues.

### Why Run Tests on Host?

- ✅ **Native browser support** - No sandboxing issues
- ✅ **Better performance** - Direct hardware access
- ✅ **Easier debugging** - Can see browser windows
- ✅ **No container setup** - Use system browsers
- ✅ **More stable** - Fewer environment constraints

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Installation & Setup](#installation--setup)
3. [Running Tests (Host Machine)](#running-tests-host-machine)
4. [Running Tests (Dev Container)](#running-tests-dev-container)
5. [Test Structure](#test-structure)
6. [Configuration](#configuration)
7. [Test Cases Overview](#test-cases-overview)
8. [Best Practices](#best-practices)
9. [Troubleshooting](#troubleshooting)
10. [Educational Notes](#educational-notes)

## 🔧 Prerequisites

### For Host Machine Testing (Recommended)

- **Node.js** (v16 or higher)
- **npm** (v8 or higher)
- **Chrome** or **Firefox** browser (installed normally on your system)
- **Git** (for version control)

### Application Requirements

1. **Backend**: Spring Boot API running on `http://localhost:8080`
2. **Frontend**: Vue.js application running on `http://localhost:8081`
3. **Test User**: The backend should have a test user with credentials:
   - Email: `test@focusflow.com`
   - Password: `Test@123456`

## 🚀 Installation & Setup

### Option A: Host Machine Setup (Recommended)

1. **Clone the repository on your host machine:**

```bash
git clone <your-repo-url>
cd focusflow/frontend
```

2. **Install dependencies:**

```bash
npm install
```

3. **Verify browser installation:**

```bash
# Chrome/Chromium
google-chrome --version
# or
chromium --version

# Firefox
firefox --version
```

### Option B: Dev Container Setup

If you must run in the container (not recommended), follow the container-specific setup in the [Dev Container section](#running-tests-dev-container).

## 🏃‍♂️ Running Tests (Host Machine)

### Quick Start

```bash
# Navigate to frontend directory on your host
cd /path/to/focusflow/frontend

# Install dependencies (if not already done)
npm install

# Run all tests
npm run test:selenium

# Run specific test suite
npm run test:selenium:auth
npm run test:selenium:tasks
npm run test:selenium:integration
```

### Advanced Usage

```bash
# Run in headless mode
npm run test:selenium:headless

# Run with specific browser
npm run test:selenium:chrome
npm run test:selenium:firefox

# Run with custom environment
BASE_URL=http://localhost:3000 npm run test:selenium
API_BASE_URL=http://localhost:9090/api/v1 npm run test:selenium
```

### Using the Test Runner Script

```bash
# Make script executable
chmod +x run-selenium-tests.sh

# Run tests (automatically detects host environment)
./run-selenium-tests.sh all
./run-selenium-tests.sh auth
./run-selenium-tests.sh tasks --grep="login"
```

## 🐳 Running Tests (Dev Container)

> **⚠️ Not Recommended:** Browser tests in containers can be unstable due to sandboxing issues.

If you must run tests in the dev container:

### Container Prerequisites

- Install browser and dependencies (automatically handled by scripts)
- Set proper environment variables
- Run in headless mode only

### Container Commands

```bash
# Inside the dev container
cd /workspace/frontend

# Run with container-specific settings
CONTAINER=true HEADLESS=true npm run test:selenium

# Or use the container-optimized script
./run-selenium-tests.sh auth
```

### Container Issues & Troubleshooting

Common container issues:

- **SIGTRAP errors**: Browser sandboxing conflicts
- **Display issues**: No X11 server available
- **Permission errors**: Container security restrictions
- **Resource limits**: Memory/CPU constraints

## 📁 Test Structure

The test suite follows the **Page Object Model** pattern for maintainability:

```
selenium-tests/
├── config/
│   └── test-config.js          # Test configuration and environment settings
├── utils/
│   ├── webdriver-manager.js    # WebDriver setup and management
│   └── api-client.js           # Backend API interaction utilities
├── page-objects/
│   ├── base-page.js            # Common page functionality
│   ├── login-page.js           # Login page interactions
│   └── tasks-page.js           # Task management page interactions
├── tests/
│   ├── auth.test.js            # Authentication test suite
│   ├── tasks.test.js           # Task management test suite
│   └── integration.test.js     # End-to-end integration tests
├── screenshots/                # Test screenshots (auto-generated)
├── reports/                    # Test reports (auto-generated)
├── run-tests.js               # Test runner script
├── smoke-test.js              # Browser verification test
└── README.md                  # This documentation
```

### Architecture Explanation

1. **Config Layer**: Centralized configuration for easy environment management
2. **Utils Layer**: Reusable utilities for WebDriver and API interactions
3. **Page Objects**: Encapsulated page interactions following DRY principles
4. **Test Layer**: Actual test cases organized by functionality
5. **Reporting**: Automated screenshot capture and test reporting

## ⚙️ Configuration

### Environment Variables

```bash
# Application URLs
BASE_URL=http://localhost:8081          # Frontend URL
API_BASE_URL=http://localhost:8080/api/v1  # Backend API URL

# Test credentials
TEST_EMAIL=test@focusflow.com
TEST_PASSWORD=Test@123456

# Browser settings
BROWSER=chrome                          # chrome, firefox
HEADLESS=false                         # true for headless mode
CONTAINER=false                        # true if running in container

# Debugging
DEBUG=true                             # Enable debug logging
```

### Test Configuration (`config/test-config.js`)

The configuration automatically detects the environment and adjusts settings:

- **Host Machine**: Uses system browsers, allows non-headless mode
- **Container**: Forces headless mode, container-specific options
- **CI/CD**: Optimized for automated testing environments

## 📊 Test Cases Overview

### Authentication Tests (`auth.test.js`)

| Test Case             | Description                         | Validates              |
| --------------------- | ----------------------------------- | ---------------------- |
| Login Form Display    | Verifies login form elements        | UI structure           |
| Empty Form Validation | Tests form validation               | Client-side validation |
| Invalid Email Format  | Tests email format validation       | Input validation       |
| Invalid Credentials   | Tests wrong credentials             | Server-side validation |
| Successful Login      | Tests valid login flow              | Authentication flow    |
| Session Persistence   | Tests login state across pages      | Session management     |
| Logout Functionality  | Tests logout process                | Session termination    |
| Security Tests        | Tests SQL injection, XSS protection | Security validation    |

### Task Management Tests (`tasks.test.js`)

| Test Case             | Description                     | Validates         |
| --------------------- | ------------------------------- | ----------------- |
| Tasks Page Navigation | Verifies page accessibility     | Routing           |
| Task Creation Form    | Tests task creation UI          | Form handling     |
| Basic Task Creation   | Tests creating simple tasks     | CRUD operations   |
| Task Validation       | Tests required field validation | Data validation   |
| Task Viewing          | Tests task list and details     | Data display      |
| Task Editing          | Tests task modification         | Update operations |
| Task Search/Filter    | Tests search functionality      | Query operations  |
| Task Deletion         | Tests task removal              | Delete operations |
| UI Responsiveness     | Tests performance under load    | Performance       |

### Integration Tests (`integration.test.js`)

| Test Case                 | Description                 | Validates                  |
| ------------------------- | --------------------------- | -------------------------- |
| Complete User Journey     | Full workflow testing       | End-to-end flows           |
| UI/API Data Consistency   | Tests data synchronization  | Data integrity             |
| Session Management        | Cross-system authentication | Authentication integration |
| Real-time Synchronization | Tests live data updates     | Real-time features         |
| Error Handling            | Tests error recovery        | Resilience                 |
| Performance Under Load    | Tests bulk operations       | Scalability                |
| Security Integration      | Tests access controls       | Security integration       |

## 🛠️ Best Practices Demonstrated

### 1. **Environment Detection**

```javascript
// Automatically detects and configures for different environments
const isContainer = process.env.CONTAINER === "true";
const isCI = process.env.CI === "true";
const headless = isContainer || isCI || process.env.HEADLESS === "true";
```

### 2. **Robust Element Location**

```javascript
// Multiple selector strategies for resilience
const selectors = [
  '[data-testid="login-button"]', // Preferred: test-specific
  'button[type="submit"]', // Fallback: semantic
  ".login-btn", // Last resort: class-based
].join(", ");
```

### 3. **Cross-Platform Browser Paths**

```javascript
// Automatic browser detection across different systems
const chromePaths = [
  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome", // macOS
  "/usr/bin/google-chrome", // Linux
  "/usr/bin/chromium", // Linux alternative
  "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe", // Windows
];
```

## 🔍 Troubleshooting

### Host Machine Issues

#### 1. **WebDriver Version Mismatch**

```bash
# Update browser drivers
npm update chromedriver geckodriver
```

#### 2. **Browser Not Found**

```bash
# Install Chrome (Ubuntu/Debian)
wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | sudo apt-key add -
sudo sh -c 'echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list'
sudo apt update && sudo apt install google-chrome-stable

# Install Firefox
sudo apt install firefox
```

#### 3. **Port Conflicts**

```bash
# Check if ports are in use
lsof -i :8080  # Backend
lsof -i :8081  # Frontend
```

### Container Issues

#### 1. **SIGTRAP Errors**

- **Solution**: Run tests on host machine instead
- **Workaround**: Use Firefox instead of Chrome in container

#### 2. **Display/X11 Issues**

- **Solution**: Run in headless mode only
- **Alternative**: Use host machine for visual debugging

#### 3. **Permission Errors**

- **Check**: Container has necessary permissions
- **Fix**: Run container with appropriate privileges

### Debugging Tips

#### 1. **Visual Debugging**

```bash
# Run without headless mode (host only)
HEADLESS=false npm run test:selenium:auth
```

#### 2. **Screenshot Analysis**

- Check `selenium-tests/screenshots/` for visual state
- Screenshots are automatically taken before/after each test

#### 3. **Verbose Logging**

```bash
# Enable debug logging
DEBUG=true npm run test:selenium
```

## 🚦 CI/CD Integration

### GitHub Actions Example

```yaml
name: E2E Tests
on: [push, pull_request]
jobs:
  e2e-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: "18"

      # Install browsers
      - name: Install Chrome
        uses: browser-actions/setup-chrome@latest

      # Install dependencies
      - name: Install dependencies
        run: cd frontend && npm ci

      # Start services
      - name: Start backend
        run: cd backend && ./mvnw spring-boot:run &

      - name: Start frontend
        run: cd frontend && npm run serve &

      - name: Wait for services
        run: sleep 30

      # Run tests
      - name: Run E2E tests
        run: cd frontend && npm run test:selenium:headless
        env:
          HEADLESS: true
          CI: true
```

### Docker Compose for Testing

```yaml
version: "3.8"
services:
  test-runner:
    build: .
    environment:
      - HEADLESS=true
      - BASE_URL=http://frontend:8081
      - API_BASE_URL=http://backend:8080/api/v1
    depends_on:
      - frontend
      - backend
    volumes:
      - ./selenium-tests/screenshots:/app/screenshots
      - ./selenium-tests/reports:/app/reports
```

## 📈 Extending the Test Suite

### Adding New Test Cases

1. **Create new test file**:

```javascript
// tests/new-feature.test.js
describe("New Feature Tests", function () {
  // Test implementation
});
```

2. **Update test runner**:

```javascript
// run-tests.js - Add to testConfig.suites
newfeature: {
  name: 'New Feature Tests',
  files: ['tests/new-feature.test.js'],
  description: 'Tests for new feature'
}
```

3. **Add npm script**:

```json
{
  "scripts": {
    "test:selenium:newfeature": "./run-selenium-tests.sh newfeature"
  }
}
```

## 📚 Additional Resources

### Selenium Documentation

- [Selenium WebDriver](https://selenium-webdriver.js.org/)
- [Mocha Testing Framework](https://mochajs.org/)
- [Chai Assertion Library](https://chaijs.com/)

### Testing Best Practices

- [Test Automation Pyramid](https://martinfowler.com/articles/practical-test-pyramid.html)
- [Page Object Model](https://selenium.dev/documentation/test_practices/encouraged/page_object_models/)
- [Testing Strategies](https://testing.googleblog.com/2015/04/just-say-no-to-more-end-to-end-tests.html)

### Vue.js Testing

- [Vue Test Utils](https://vue-test-utils.vuejs.org/)
- [Testing Vue Applications](https://vuejs.org/guide/scaling-up/testing.html)

---

## 🎉 Conclusion

This Selenium test suite provides a comprehensive foundation for testing modern web applications. For the best experience and stability:

**✅ Run tests on your host machine**
**⚠️ Avoid running in dev containers when possible**
**🚀 Use CI/CD for automated testing**

The suite is designed to be:

- **Maintainable**: Clean code structure with good separation of concerns
- **Reliable**: Robust element location and proper wait strategies
- **Educational**: Well-documented with clear examples
- **Extensible**: Easy to add new tests and features
- **Professional**: Industry-standard practices and patterns
- **Cross-platform**: Works on Windows, macOS, and Linux

For questions or contributions, please refer to the project documentation or contact the development team.

**Happy Testing! 🚀**

## 🧪 Test Suites

### Authentication Tests

- **`auth` or `auth-core`**: Core authentication functionality (⚡ **Fast** - ~30 seconds)

  - Login form interaction and validation
  - Basic authentication flows
  - API authentication integration
  - **Use for**: Development, quick feedback, CI/CD core tests

- **`auth-comprehensive`**: Complete authentication test suite (🔍 **Thorough** - ~2 minutes)
  - All core tests plus:
  - Security testing (SQL injection, XSS protection)
  - Authentication state persistence
  - Logout functionality
  - Protected route access control
  - Brute force protection
  - **Use for**: Release testing, comprehensive validation

### Other Test Suites

- **`tasks`**: Task management CRUD operations
- **`integration`**: End-to-end workflow testing
- **`all`**: All test suites
  - With `--optimized`: Runs core versions (faster)
  - Without `--optimized`: Runs comprehensive versions (thorough)

## 🚀 Quick Reference

### Common Commands

```bash
# Fast development testing (recommended)
./run-selenium-tests.sh auth                    # Core auth tests
./run-selenium-tests.sh auth-core               # Same as above

# Comprehensive testing
./run-selenium-tests.sh auth-comprehensive      # Full auth test suite

# All tests
./run-selenium-tests.sh all --optimized         # All core tests (fast)
./run-selenium-tests.sh all                     # All comprehensive tests

# Headless mode (for CI/CD)
HEADLESS=true ./run-selenium-tests.sh auth
```

### Test File Structure

```
tests/
├── auth-core.test.js           # ⚡ Fast core auth tests (~30s)
├── auth-comprehensive.test.js  # 🔍 Complete auth tests (~2m)
├── tasks.test.js              # 📋 Task management tests
└── integration.test.js        # 🔗 End-to-end tests
```
