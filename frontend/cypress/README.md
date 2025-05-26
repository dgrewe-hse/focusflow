# FocusFlow Cypress Test Suite

This directory contains comprehensive end-to-end tests for the FocusFlow application using Cypress.

## Test Structure

```
cypress/
├── e2e/
│   ├── auth/
│   │   └── login.cy.js           # Authentication tests
│   ├── e2e/
│   │   └── user-flow.cy.js       # Complete user journey tests
│   ├── tasks/
│   │   └── task-management.cy.js # Task CRUD operations
│   ├── main_page.cy.js          # Legacy main page tests
│   └── task_form_page.cy.js     # Legacy task form tests
├── support/
│   ├── commands.js              # Custom Cypress commands
│   └── e2e.js                   # Support file configuration
├── cypress.config.js            # Cypress configuration
└── README.md                    # This file
```

## Prerequisites

Before running the tests, ensure:

1. **Backend server** is running on `http://localhost:8080`
2. **Frontend server** is running on `http://localhost:8081`
3. **Test user** exists in the backend with credentials:
   - Email: `test@focusflow.com`
   - Password: `Test@123456`

## Running Tests

### Interactive Mode (Cypress Test Runner)

```bash
npm run test
```

Opens the Cypress Test Runner for interactive test execution and debugging.

### Headless Mode (CI/CD)

```bash
npm run test:headless
```

Runs all tests in headless mode, suitable for CI/CD pipelines.

### Specific Test Suites

```bash
# Run only authentication tests
npm run test:auth

# Run only end-to-end user flow tests
npm run test:e2e

# Run only task management tests
npm run test:tasks
```

## Test Categories

### 1. Authentication Tests (`auth/login.cy.js`)

**Purpose**: Test login functionality and form validation

**Test Cases**:

- ✅ UI element visibility and functionality
- ✅ Password show/hide toggle
- ✅ Form validation (empty fields, invalid email, short password)
- ✅ Successful login with valid credentials
- ✅ Error handling for invalid credentials
- ✅ Loading states during authentication
- ✅ Navigation to registration page
- ✅ Keyboard accessibility
- ✅ Form submission with Enter key

### 2. End-to-End User Flow (`e2e/user-flow.cy.js`)

**Purpose**: Test complete user journeys from login to task management

**Main User Flow**:

1. User visits app → redirected to login
2. User logs in with valid credentials
3. User is redirected to dashboard
4. User creates a new task
5. User sees the task in the dashboard
6. User can interact with the task (edit/delete)

**Additional Test Cases**:

- ✅ Task creation with minimal required fields
- ✅ Form validation preventing invalid submissions
- ✅ Task creation cancellation
- ✅ Authentication state management
- ✅ Route protection for unauthenticated users
- ✅ Authentication persistence across page refreshes
- ✅ Logout functionality
- ✅ Error handling for backend failures
- ✅ Network error handling
- ✅ Loading state verification
- ✅ Responsive design testing

### 3. Task Management Tests (`tasks/task-management.cy.js`)

**Purpose**: Comprehensive testing of task CRUD operations

**Task Creation**:

- ✅ Create task with all fields
- ✅ Create task with only required fields
- ✅ Form validation for required fields
- ✅ Different priority levels (Low, Medium, High, Urgent)

**Task Viewing**:

- ✅ Display tasks in dashboard table
- ✅ Show task action buttons (edit/delete)
- ✅ Handle empty task list gracefully

**Task Editing**:

- ✅ Open edit dialog
- ✅ Update task details
- ✅ Cancel edit without saving

**Task Deletion**:

- ✅ Delete task functionality

**Display and UI**:

- ✅ Priority chip colors and display
- ✅ Status chip display
- ✅ Form validation and error handling
- ✅ API error handling

## Custom Cypress Commands

The test suite includes several custom commands for improved test efficiency:

### Authentication Commands

```javascript
// Login via API (faster for setup)
cy.loginViaAPI(email, password);

// Login via UI (for testing login flow)
cy.loginViaUI(email, password);

// Logout and clear authentication
cy.logout();
```

### Task Management Commands

```javascript
// Create task via API (for test setup)
cy.createTaskViaAPI(taskData);

// Clear all user tasks (for cleanup)
cy.clearUserTasks();
```

### Utility Commands

```javascript
// Wait for element with timeout
cy.waitForElement(selector, timeout);
```

## Test Data and Environment

### Environment Variables

- `TEST_EMAIL`: Test user email (default: `test@focusflow.com`)
- `TEST_PASSWORD`: Test user password (default: `Test@123456`)
- `API_URL`: Backend API URL (default: `http://localhost:8080/api/v1`)

### Test Data Management

- Tests use the `cy.createTaskViaAPI()` command to set up test data
- Tests clean up created data using `cy.clearUserTasks()` in `afterEach` hooks
- Each test is isolated and doesn't depend on data from other tests

## Data-cy Attributes

The application uses `data-cy` attributes for reliable element selection:

### Login Page

- `data-cy="email-input"`: Email input field
- `data-cy="password-input"`: Password input field
- `data-cy="login-button"`: Login submit button
- `data-cy="register-link"`: Link to registration page

### Task Form Page

- `data-cy="task-title-input"`: Task title input
- `data-cy="task-short-description-input"`: Short description textarea
- `data-cy="task-long-description-input"`: Long description textarea
- `data-cy="task-priority-select"`: Priority dropdown
- `data-cy="task-due-date-input"`: Due date input
- `data-cy="create-task-button"`: Create task submit button
- `data-cy="cancel-task-button"`: Cancel button

### Task List Page

- `data-cy="create-task-button"`: Create new task button
- `data-cy="logout-button"`: Logout button
- `data-cy="tasks-table"`: Main tasks table
- `data-cy="task-row-{id}"`: Individual task rows
- `data-cy="task-title"`: Task title cells
- `data-cy="task-description"`: Task description cells
- `data-cy="task-status"`: Task status cells
- `data-cy="task-priority"`: Task priority cells
- `data-cy="edit-task-{id}"`: Edit task buttons
- `data-cy="delete-task-{id}"`: Delete task buttons

## Best Practices

### 1. Test Isolation

- Each test is independent and doesn't rely on state from other tests
- Use `beforeEach` and `afterEach` hooks for setup and cleanup
- Clear authentication and test data between tests

### 2. Reliable Selectors

- Use `data-cy` attributes instead of CSS classes or IDs
- Avoid selecting elements by text content when possible
- Use specific, descriptive attribute values

### 3. Async Handling

- Use appropriate timeouts for elements that may take time to load
- Wait for API calls to complete before making assertions
- Use `cy.intercept()` for API mocking and verification

### 4. Error Handling

- Test both success and failure scenarios
- Verify error messages and user feedback
- Test network failures and backend errors

### 5. Performance

- Use API commands for test setup when UI testing isn't required
- Group related tests in describe blocks
- Use efficient selectors and minimize unnecessary waits

## Debugging Tests

### Interactive Debugging

1. Run `npm run test` to open Cypress Test Runner
2. Click on a test file to run it
3. Use the time-travel debugging feature to inspect each step
4. Check the browser console for additional debugging information

### Headless Debugging

1. Add `cy.screenshot()` and `cy.debug()` commands in tests
2. Check the `cypress/screenshots` and `cypress/videos` directories
3. Use `console.log()` in custom commands for debugging

### Common Issues

- **Element not found**: Check if the element has the correct `data-cy` attribute
- **Timing issues**: Add appropriate waits or increase timeouts
- **Authentication failures**: Verify backend is running and test user exists
- **API errors**: Check network tab and backend logs

## CI/CD Integration

For continuous integration, use the headless mode:

```bash
# Install dependencies
npm install

# Start backend and frontend servers
# (implementation depends on your CI/CD setup)

# Run tests
npm run test:headless
```

The tests will generate JUnit XML reports and screenshots/videos for failed tests.

## Contributing

When adding new tests:

1. Follow the existing file structure and naming conventions
2. Add appropriate `data-cy` attributes to new UI elements
3. Use custom commands for common operations
4. Include both positive and negative test cases
5. Add proper cleanup in `afterEach` hooks
6. Update this documentation for new test categories or commands

## Troubleshooting

### Backend Connection Issues

- Verify backend is running on `http://localhost:8080`
- Check that the test user exists in the database
- Ensure CORS is properly configured

### Frontend Issues

- Verify frontend is running on `http://localhost:8081`
- Check that the Vite proxy is configured correctly
- Ensure all required dependencies are installed

### Test Failures

- Check if the application UI has changed (update selectors)
- Verify test data is being cleaned up properly
- Check for timing issues with async operations
- Review browser console for JavaScript errors
