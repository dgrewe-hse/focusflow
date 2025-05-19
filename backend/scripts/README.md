# Task Management API Testing Script

This directory contains bash scripts for testing the Task Management REST API endpoints of the FocusFlow application.

## Prerequisites

Before running the test script, ensure you have the following:

1. The FocusFlow backend server is running (typically at http://localhost:8080) `./mvnw spring-boot:run -Dspring-boot.run.profiles=test`
2. `curl` is installed on your system
   `curl -v -X POST "http://localhost:8080/api/v1/auth/login" -H "Content-Type: application/json" -d '{"email": "test@focusflow.com", "password": "Test@123456"}'`
3. Valid user credentials or a JWT token for authentication

## Test User

A test user is automatically created when the Spring Boot application starts:

- **Email**: `test@focusflow.com`
- **Password**: `Test@123`

This user is created by the `TestDataInitializer` class and can be used for all API testing.

## Available Scripts

There are two versions of the testing script:

1. **test-task-api.sh** - Full version with JSON parsing using `jq`
2. **test-task-api-no-jq.sh** - Simplified version that doesn't require `jq`

### When to use each version:

- Use **test-task-api.sh** if you have `jq` installed and want nicely formatted JSON output
- Use **test-task-api-no-jq.sh** if you don't have `jq` installed or prefer a simpler script

## Installation

### Installing jq (Optional)

If you want to use the full version of the script, you'll need jq installed:

- **Ubuntu/Debian**: `sudo apt-get install jq`
- **macOS**: `brew install jq`
- **Windows**: Download from [stedolan.github.io/jq/](https://stedolan.github.io/jq/)

## Running the Tests

1. Make the script executable (if not already):

   ```bash
   chmod +x test-task-api.sh
   # or for the no-jq version
   chmod +x test-task-api-no-jq.sh
   ```

2. Run the script:
   ```bash
   ./test-task-api.sh
   # or for the no-jq version
   ./test-task-api-no-jq.sh
   ```

## Authentication

Both scripts offer two authentication methods:

1. **Login with Email/Password**: The script can call the `/api/v1/auth/login` endpoint to obtain a JWT token. Use the test user credentials mentioned above.
2. **Manual Token Entry**: You can manually enter a JWT token if you already have one.

## Test Flow

The scripts will test the following endpoints in sequence:

1. Authentication
2. Create Task
3. Get All Tasks
4. Get Task by ID
5. Update Task
6. Search Tasks
7. Get Tasks by Assignee
8. Get Tasks by Team
9. Get Tasks by Creator
10. Get Upcoming Tasks
11. Get Task Statistics by User
12. Get Task Statistics by Team
13. Update Task Status
14. Update Task Assignee
15. Delete Task (optional)

## Required Inputs

During the test, you'll be prompted to enter:

- Authentication details (email/password or token)
- User ID for testing (used for task creation, assignment, etc.)
- Team ID for testing team-related endpoints

## Output

The scripts provide colored output showing:

- Test progress and results
- HTTP status codes
- JSON responses from the API (formatted with jq in the full version)
- Success/failure indicators for each endpoint test

## Differences Between Versions

### test-task-api.sh (with jq)

- Requires jq to be installed
- Provides nicely formatted JSON output
- Better JSON parsing capabilities
- Can extract nested values from JSON responses

### test-task-api-no-jq.sh

- No external dependencies beyond curl
- Uses basic text processing for JSON parsing
- Limited JSON parsing capabilities
- Shows raw JSON responses or response length

## Troubleshooting

If you encounter issues:

1. **Authentication Failures**: Ensure the Spring Boot server is properly started and the test user is created
2. **Connection Errors**: Verify the backend server is running at the expected URL
3. **JSON Parsing Errors**: If using the jq version, ensure jq is properly installed
4. **Permission Denied**: Make sure the script is executable

## Customization

You can modify the scripts to change:

- The base URL (currently set to `http://localhost:8080/api/v1`)
- Test data (task titles, descriptions, etc.)
- Test flow (comment out tests you don't want to run)

## Security Note

These scripts handle authentication credentials. Do not share your credentials or tokens, and use these scripts in a secure environment.
