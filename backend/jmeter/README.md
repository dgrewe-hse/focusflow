# FocusFlow JMeter Load Testing Documentation

This documentation provides instructions for load testing the FocusFlow Spring Boot application using Apache JMeter 5.6.3.

## Table of Contents

1. [JMeter Installation](#jmeter-installation)
2. [Project Setup](#project-setup)
3. [Test Plan Structure](#test-plan-structure)
4. [Authentication Configuration](#authentication-configuration)
5. [Task API Testing](#task-api-testing)
6. [Common Headers and Settings](#common-headers-and-settings)
7. [Variable Definitions](#variable-definitions)
8. [Test Data Generation](#test-data-generation)
9. [Test Reports](#test-reports)
10. [Running from Command Line](#running-from-command-line)
11. [Common Issues and Troubleshooting](#common-issues-and-troubleshooting)

## JMeter Installation

1. Download Apache JMeter 5.6.3 from the [official website](https://jmeter.apache.org/download_jmeter.cgi)
2. Extract the archive to a directory of your choice
3. Run JMeter:
   ```bash
   cd <jmeter_directory>/bin
   ./jmeter.sh     # Linux/macOS
   jmeter.bat      # Windows
   ```

## Project Setup

### Creating a Test Plan

1. Open JMeter
2. Create a new Test Plan named "FocusFlow API Load Test"
3. Right-click on the Test Plan and add:
   - Thread Group (Users)
   - Config Elements → HTTP Request Defaults
   - Config Elements → HTTP Cookie Manager
   - Config Elements → HTTP Header Manager
   - Listeners (for results)

### HTTP Request Defaults

Set the following in the HTTP Request Defaults:

- Server Name or IP: `localhost` (or your server address)
- Port Number: `8080`
- Protocol: `http`
- Content encoding: `UTF-8`

### Thread Group Configuration

Configure the Thread Group:

- Number of Threads (users): Adjust based on your test requirements
- Ramp-up period (seconds): Time to start all threads
- Loop Count: Number of iterations or select "Forever"

## Test Plan Structure

Organize your test plan with the following structure:

```
Test Plan
├── User Variables
├── HTTP Request Defaults
├── HTTP Header Manager (Global)
├── HTTP Cookie Manager
├── Thread Group - Authentication & Tasks
│   ├── Login Request
│   ├── Create Task Requests
│   ├── Get All Tasks Requests
│   ├── Get Task By ID Requests
│   ├── Update Task Requests
│   ├── Search Tasks Requests
│   └── Delete Task Requests
└── Results Listeners
    ├── View Results Tree
    ├── Summary Report
    └── Aggregate Report
```

## Authentication Configuration

### Login Request

Create an HTTP Request sampler for authentication:

- Name: `Login Request`
- Method: `POST`
- Path: `/api/v1/auth/login`
- Body Data:
  ```json
  {
    "email": "${TEST_EMAIL}",
    "password": "${TEST_PASSWORD}"
  }
  ```

Add a JSON Extractor to extract the JWT token:

- Name: `JWT Token Extractor`
- JSON Path expressions: `$.token`
- Match Numbers: `1`
- Variable names: `AUTH_TOKEN`

### Auth Header Setup

Add a BeanShell PostProcessor to set the authentication header for subsequent requests:

```java
// Set Auth Header for subsequent requests
vars.put("AUTH_HEADER", "Bearer " + vars.get("AUTH_TOKEN"));
```

## Task API Testing

### Create Task Request

```
HTTP Request:
- Name: Create Task
- Method: POST
- Path: /api/v1/tasks
- Headers:
  - Content-Type: application/json
  - Authorization: ${AUTH_HEADER}
- Body:
  {
    "title": "Test Task ${__time()}",
    "shortDescription": "This is a test task",
    "longDescription": "This is a detailed description of the test task created by JMeter",
    "dueDate": "${FUTURE_DATE}",
    "priority": "MID",
    "status": "OPEN",
    "createdById": "${USER_ID}"
  }
```

Add a JSON Extractor to extract the task ID:

- JSON Path expressions: `$.data.id`
- Match Numbers: `1`
- Variable names: `TASK_ID`

### Get All Tasks Request

```
HTTP Request:
- Name: Get All Tasks
- Method: GET
- Path: /api/v1/tasks
- Headers:
  - Authorization: ${AUTH_HEADER}
```

### Get Task By ID Request

```
HTTP Request:
- Name: Get Task By ID
- Method: GET
- Path: /api/v1/tasks/${TASK_ID}
- Headers:
  - Authorization: ${AUTH_HEADER}
```

### Update Task Request

```
HTTP Request:
- Name: Update Task
- Method: PUT
- Path: /api/v1/tasks/${TASK_ID}
- Headers:
  - Content-Type: application/json
  - Authorization: ${AUTH_HEADER}
- Body:
  {
    "title": "Updated Task ${__time()}",
    "shortDescription": "This is an updated test task",
    "longDescription": "This is an updated description of the test task by JMeter",
    "dueDate": "${FUTURE_DATE}",
    "priority": "HIGH",
    "status": "PENDING",
    "createdById": "${USER_ID}"
  }
```

### Search Tasks Request

```
HTTP Request:
- Name: Search Tasks
- Method: GET
- Path: /api/v1/tasks/search
- Parameters:
  - title: Test
  - status: PENDING
- Headers:
  - Authorization: ${AUTH_HEADER}
```

### Update Task Status Request

```
HTTP Request:
- Name: Update Task Status
- Method: PATCH
- Path: /api/v1/tasks/${TASK_ID}/status
- Parameters:
  - status: COMPLETED
- Headers:
  - Authorization: ${AUTH_HEADER}
```

### Update Task Assignee Request

```
HTTP Request:
- Name: Update Task Assignee
- Method: PATCH
- Path: /api/v1/tasks/${TASK_ID}/assignee
- Parameters:
  - assigneeId: ${USER_ID}
- Headers:
  - Authorization: ${AUTH_HEADER}
```

### Delete Task Request

```
HTTP Request:
- Name: Delete Task
- Method: DELETE
- Path: /api/v1/tasks/${TASK_ID}
- Headers:
  - Authorization: ${AUTH_HEADER}
```

## Common Headers and Settings

Create an HTTP Header Manager at the Thread Group level with these headers:

```
Content-Type: application/json
Accept: application/json
User-Agent: JMeter Load Test
X-Requested-With: XMLHttpRequest
```

## Variable Definitions

Add a User Defined Variables config element with these variables:

```
TEST_EMAIL: test@focusflow.com
TEST_PASSWORD: Test@123456
BASE_URL: http://localhost:8080/api/v1
```

### Date Variables Setup

Add a BeanShell PreProcessor to calculate the future date for task deadlines:

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Calculate a date 7 days in the future
LocalDateTime futureDate = LocalDateTime.now().plusDays(7);
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
String formattedDate = futureDate.format(formatter);

// Store for use in requests
vars.put("FUTURE_DATE", formattedDate);
```

## Test Data Generation

For generating random test data, add a BeanShell PreProcessor with this code:

```java
import java.util.UUID;
import java.util.Random;

// Generate random task data
String[] priorities = {"LOW", "MID", "HIGH"};
String[] statuses = {"OPEN", "PENDING", "IN_PROGRESS", "COMPLETED"};

Random rand = new Random();

// Random title
String title = "JMeter Task " + System.currentTimeMillis();
vars.put("TASK_TITLE", title);

// Random priority
String priority = priorities[rand.nextInt(priorities.length)];
vars.put("TASK_PRIORITY", priority);

// Random status
String status = statuses[rand.nextInt(statuses.length)];
vars.put("TASK_STATUS", status);

// Random UUID for various IDs if needed
String randomId = UUID.randomUUID().toString();
vars.put("RANDOM_ID", randomId);
```

## Test Reports

Add these listeners to your test plan for result analysis:

1. **View Results Tree** - Visual inspection of individual requests
2. **Summary Report** - Overview of all requests
3. **Aggregate Report** - Statistical summary
4. **Response Time Graph** - Visual presentation of response times

## Running from Command Line

To execute tests from the command line:

```bash
jmeter -n -t /path/to/focusflow_test_plan.jmx -l /path/to/results.jtl -e -o /path/to/report_folder
```

Parameters:

- `-n`: Run in non-GUI mode
- `-t`: Path to test plan
- `-l`: Path to results file
- `-e`: Generate HTML report
- `-o`: Output directory for HTML report

## Common Issues and Troubleshooting

### Authentication Issues

If you encounter HTTP 401 (Unauthorized) errors:

- Verify that the token extraction is working correctly
- Check that the token is being correctly included in the Authorization header
- Ensure the test user account exists and has appropriate permissions

### Connection Issues

If you see connection errors:

- Verify the Spring Boot application is running
- Check that the host and port in HTTP Request Defaults are correct
- Ensure no firewall is blocking the connections

### Data Extraction Issues

If variables are not properly extracted:

- Use Debug Sampler to verify variable values
- Check JSON/RegEx extractors for correct path expressions
- Verify response formats match what extractors expect

### Sample Startup Script

Create a shell script to set up JMeter and run tests:

```bash
#!/bin/bash

# Define variables
JMETER_HOME=/path/to/apache-jmeter-5.6.3
TEST_PLAN=focusflow_test_plan.jmx
RESULTS_FILE=results_$(date +%Y%m%d_%H%M%S).jtl
REPORT_DIR=report_$(date +%Y%m%d_%H%M%S)

# Run JMeter test
$JMETER_HOME/bin/jmeter -n -t $TEST_PLAN -l $RESULTS_FILE -e -o $REPORT_DIR

echo "Test completed. Results in $RESULTS_FILE and report in $REPORT_DIR"
```

Save this file as `run_jmeter_tests.sh` and make it executable with `chmod +x run_jmeter_tests.sh`.
