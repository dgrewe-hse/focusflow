# Simple Tasks Test - JMeter Instructions

This document explains how to use the provided JMeter test plan `simple-tasks-test.jmx` to test the "Get All Tasks" functionality of your FocusFlow application.

## Test Overview

This simple test plan performs the following operations:

1. Authenticates a user (login)
2. Retrieves the authentication token
3. Retrieves all tasks using the API

## Prerequisites

- JMeter 5.6.3 installed
- FocusFlow application running on localhost:8080
- Test user account available in the system

## Running the Test

### Option 1: Using JMeter GUI

1. Start JMeter GUI:

   ```bash
   cd <jmeter_directory>/bin
   ./jmeter.sh     # Linux/macOS
   jmeter.bat      # Windows
   ```

2. Open the test plan file:

   - Click `File` → `Open`
   - Navigate to `/workspace/backend/src/test/java/de/hse/focusflow/jmeter/simple-tasks-test.jmx`
   - Click `Open`

3. Configure variables if needed:

   - In the `Test Plan` element, review the `User Defined Variables`
   - Modify `TEST_EMAIL` and `TEST_PASSWORD` to match a valid user in your system
   - If your application runs on a different host/port, update `BASE_URL` and `PORT`

4. Run the test:
   - Click the green "Start" button (▶️) in the toolbar
   - Monitor the results in the "View Results Tree" and "Summary Report" listeners

### Option 2: Using Command Line

1. Run the test without GUI:

   ```bash
   cd <jmeter_directory>/bin
   ./jmeter.sh -n -t /workspace/backend/src/test/java/de/hse/focusflow/jmeter/simple-tasks-test.jmx -l results.jtl
   ```

2. Generate an HTML report:
   ```bash
   ./jmeter.sh -g results.jtl -o report-folder
   ```

## Test Plan Structure

### Thread Group Configuration

- Number of Threads (Users): 10
- Ramp-up Period: 5 seconds
- Loop Count: 1

You can adjust these values based on your testing needs.

### HTTP Request Defaults

- Server: `${BASE_URL}` (default: localhost)
- Port: `${PORT}` (default: 8080)
- Protocol: `${PROTOCOL}` (default: http)
- Content encoding: UTF-8
- Connection and response timeouts: 10s and 30s

### Test Flow

1. **Login Request**:

   - HTTP POST to `/api/v1/auth/login`
   - JSON body with test user credentials
   - Extracts auth token and user ID using JSON extractors

2. **Pause**: 1-1.5 second delay between requests

3. **Get All Tasks Request**:
   - HTTP GET to `/api/v1/tasks`
   - Includes auth token in headers
   - Validates:
     - Response code is 200
     - Response JSON has `success: true`
     - Response time is under 2000ms

## Modifying the Test

### Adding More Requests

To add more API operations:

1. Right-click on the Thread Group → Add → Sampler → HTTP Request
2. Configure the request details (path, method, etc.)
3. Add needed extractors and assertions
4. Don't forget to add the Authorization header:
   - Right-click on the new request → Add → Config Element → HTTP Header Manager
   - Add header: `Authorization: ${AUTH_HEADER}`

### Increasing Load

To increase the load:

1. Modify the Thread Group settings:

   - Increase "Number of Threads" (users)
   - Decrease "Ramp-up Period"
   - Increase "Loop Count"

2. Consider adding a Timer (e.g., Constant Timer or Gaussian Random Timer) to simulate realistic user behavior

## Troubleshooting

### Authentication Errors

If you see 401 Unauthorized errors:

- Verify that your test user exists in the database
- Check that the email and password match
- Examine the login response in View Results Tree to see any error messages

### Connection Issues

If you see connection errors:

- Verify that the Spring Boot application is running
- Check the BASE_URL and PORT settings
- Ensure there are no firewall rules blocking the connection

### Data Validation Issues

If JSON assertions fail:

- Verify the API response format
- Check if your API sends `success: true` in the response
- Examine the actual response in the View Results Tree

## Next Steps

Once you're comfortable with this simple test, you can expand it to include:

1. Creating new tasks
2. Updating existing tasks
3. Deleting tasks
4. Running more complex scenarios with multiple operations
5. Adding more realistic think times between actions
