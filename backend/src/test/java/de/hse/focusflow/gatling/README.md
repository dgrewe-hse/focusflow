# FocusFlow Gatling Load Tests

This directory contains Gatling load tests for the FocusFlow Spring Boot application. These tests can be used to:

1. Verify the application handles expected load properly
2. Identify performance bottlenecks
3. Determine system capacity limits
4. Test system stability under sustained load

## Test Profiles

The following test profiles are available:

- **Basic Load Test** (`basic-load`): Gradual ramp-up to moderate load for baseline testing
- **Stress Test** (`stress-test`): High-intensity testing to identify system limits
- **Realistic User Simulation** (`realistic-user`): Mimics real user behavior patterns

## Running the Tests

### Online Mode (with real server)

In online mode, Gatling will send real HTTP requests to a running Spring Boot server.

1. Start the Spring Boot application in one terminal:

   ```
   ./mvnw spring-boot:run
   ```

2. Set `OFFLINE_MODE = false` in the `Configuration.java` file.

3. Run a Gatling test in another terminal:
   ```
   ./mvnw gatling:test -Pbasic-load
   ```

### Offline Mode (without server)

Offline mode is designed for test development and validation without requiring a running server. It simulates HTTP responses internally, making it ideal for:

- Developing and debugging test scenarios
- Validating test flows without server dependencies
- Checking test structure and progression
- CI/CD pipelines where starting a server is impractical

To use offline mode:

1. Set `OFFLINE_MODE = true` in the `Configuration.java` file.

2. Run a Gatling test:
   ```
   ./mvnw gatling:test -Pbasic-load
   ```

In offline mode:

- No actual HTTP requests are sent
- HTTP responses are simulated with appropriate status codes and JSON structures
- Random delays are added to mimic real server behavior
- Success rates should always be 100% as errors are not simulated

## Available Maven Profiles

- Run basic load test:

  ```
  ./mvnw gatling:test -Pbasic-load
  ```

- Run stress test:

  ```
  ./mvnw gatling:test -Pstress-test
  ```

- Run realistic user simulation:
  ```
  ./mvnw gatling:test -Prealistic-user
  ```

## Viewing Results

After a test run completes, Gatling generates HTML reports in the `target/gatling` directory. Open the HTML report in your browser to view detailed test results and metrics.

```
open target/gatling/basictaskloadsimulation-xxxxxxx/index.html
```

Replace `basictaskloadsimulation-xxxxxxx` with the actual generated directory name.

## Adjusting Test Parameters

To modify test parameters, check the following files:

- **Configuration.java**: Environment settings, API paths, and test parameters
- **TaskScenarios.java**: Reusable user behavior patterns
- **Simulation files**: Load profiles and test durations

Some key parameters you can adjust:

- **User counts**: Change the number of simulated users in the simulation files
- **Ramp-up periods**: Adjust how quickly users are added to the system
- **Think times**: Modify the pauses between user actions to simulate real behavior
- **Test duration**: Change how long tests run

## Troubleshooting

### Connection Refused Error

If you see "Connection refused" errors in online mode, check that:

1. The Spring Boot server is running
2. The server URL in `Configuration.java` matches your server address
3. Required services (database, etc.) are available

### Authentication Errors

If you see authentication errors:

1. Verify the test credentials in `Configuration.java`
2. Check that the AUTH_PATH is correct
3. Ensure your authentication implementation is working

### JSON Parsing Errors

If you see JSON parsing errors:

1. Check that the response format in your server matches what the tests expect
2. Verify that the jsonPath expressions used in the checks are correct
3. Consider updating the checks to be more lenient or match your actual response format

## Test Implementation Notes

- **Authentication**: Tests use a mock JWT token for simplified testing
- **Test Data**: Random test data is generated in `TestData.java`
- **Assertions**: Performance requirements are defined in each simulation file
- **Offline Mode**: Simulated responses are generated in the offline implementations in `TaskScenarios.java`

## Understanding Gatling Output

When running a Gatling test, you'll see console output similar to:

```
Simulation de.hse.focusflow.gatling.simulations.BasicTaskLoadSimulation started...
Starting Task CRUD scenario
Using mock authentication with token: eyJhbGciOi...
Authentication completed, auth header: present

================================================================================
2025-05-19 21:17:35                                           5s elapsed
---- Requests ------------------------------------------------------------------
> Global                                                   (OK=0      KO=0     )


---- Task CRUD Operations ------------------------------------------------------
[                                                                          ]  0%
          waiting: 117    / active: 0      / done: 0
================================================================================
```

This output provides real-time information about:

- **OK/KO requests**: Number of successful (OK) and failed (KO) requests
- **Request types**: Metrics for different request types (Get All Tasks, Create Task, etc.)
- **Progress bar**: Visual indicator of test completion percentage
- **User states**: How many users are waiting, active, or completed
- **Elapsed time**: Duration the test has been running

The output updates periodically until the test completes, at which point Gatling generates the detailed HTML report.

## Project Structure

The Gatling tests are organized as follows:

```
gatling/
├── README.md                   # This documentation file
├── package-info.java           # Package documentation
├── simulations/                # Test simulation definitions
│   ├── BasicTaskLoadSimulation.java
│   ├── RealisticUserSimulation.java
│   └── TaskStressTestSimulation.java
├── scenarios/                  # Reusable user behavior patterns
│   └── TaskScenarios.java      # CRUD operations for tasks
└── utils/                      # Utility classes
    ├── Authentication.java     # JWT token handling
    ├── Configuration.java      # Environment and test settings
    └── TestData.java           # Test data generation
```

The key files to understand are:

1. **Simulation files** (in `/simulations`): Define test execution parameters like user counts and ramp-up periods
2. **Scenarios** (in `/scenarios`): Define user behavior patterns and API interactions
3. **Utility classes** (in `/utils`): Provide supporting functionality for tests

## Key Metrics in Reports

The Gatling HTML reports provide detailed performance metrics, including:

- **Response time distribution**: Min, max, mean, median, percentiles
- **Request counts**: Total, successful, and failed requests
- **Throughput**: Requests per second over time
- **Response time over time**: How response times evolve during the test
- **User count over time**: Active virtual user count throughout the test

Pay particular attention to:

- **95th percentile response time**: Indicates performance for most users
- **Error percentage**: Should be at or near 0% in production systems
- **Response time consistency**: Sharp increases can indicate degradation under load
