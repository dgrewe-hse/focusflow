# FocusFlow JMeter Test Scenarios

This document outlines recommended test scenarios for load testing the FocusFlow API using JMeter.

## Test Scenarios

### 1. Basic Load Test

**Objective**: Validate that the system can handle expected normal load with acceptable response times.

**Configuration**:

- **Users**: 20-50 concurrent users
- **Ramp-up**: 30 seconds
- **Duration**: 5 minutes
- **Think Time**: 1-3 seconds between requests

**Test Flow**:

1. User authentication (login)
2. Get all tasks
3. Create a new task
4. Get task by ID
5. Update task
6. Search for tasks
7. Delete task

**Success Criteria**:

- Average response time < 500ms
- 95th percentile response time < 1000ms
- Error rate < 1%

### 2. Stress Test

**Objective**: Determine the maximum load the system can handle and identify breaking points.

**Configuration**:

- **Users**: Start with 50 users, increase by 50 every minute until failure
- **Ramp-up**: 30 seconds for each increment
- **Duration**: Until system fails or 30 minutes maximum
- **Think Time**: 0.5-1 second between requests (reduced to increase load)

**Test Flow**:

- Same flow as Basic Load Test but with shorter think times

**Success Criteria**:

- Identify the breaking point (users/transactions per second)
- Document system behavior under extreme load
- Error patterns at the breaking point

### 3. Soak Test

**Objective**: Verify system stability over extended periods and identify memory leaks or performance degradation.

**Configuration**:

- **Users**: 70% of maximum load identified in stress test
- **Ramp-up**: 2 minutes
- **Duration**: 2-4 hours
- **Think Time**: 2-5 seconds between requests

**Test Flow**:

- Authentication and task operations in a realistic mix

**Success Criteria**:

- No degradation in response times over time
- Memory usage stable
- No errors due to resource exhaustion

### 4. Spike Test

**Objective**: Test system's ability to handle sudden surges in traffic.

**Configuration**:

- **Baseline Users**: 20 concurrent users
- **Spike**: Sudden increase to 200 users for 2 minutes
- **Return to Baseline**: Drop back to 20 users
- **Total Duration**: 15 minutes

**Test Flow**:

- Focus on read operations during spike (get all tasks, search tasks)
- Mix of operations during baseline

**Success Criteria**:

- System recovers after spike
- No cascading failures
- Error rate during spike < 5%

### 5. Task Creation Throughput Test

**Objective**: Measure the system's capacity for handling task creation operations.

**Configuration**:

- **Users**: 50 concurrent users
- **Ramp-up**: 1 minute
- **Duration**: 10 minutes
- **Focus**: Task creation operations only

**Test Flow**:

1. Login
2. Create tasks repeatedly

**Success Criteria**:

- Task creation rate > 20 per second
- Average response time < 700ms
- Error rate < 2%

### 6. Mixed Operations Test

**Objective**: Simulate realistic user behavior with a mix of operations.

**Configuration**:

- **Users**: 100 concurrent users
- **Ramp-up**: 2 minutes
- **Duration**: 15 minutes
- **Operation Mix**:
  - 40% read operations
  - 30% search operations
  - 20% create operations
  - 10% update/delete operations

**Success Criteria**:

- Average response time < 600ms
- 95th percentile response time < 1200ms
- Error rate < 1%

## Implementation Best Practices

### 1. Thread Group Structure

Organize your thread groups by user behavior rather than endpoint. For example:

- Admin users (creating and managing tasks)
- Regular users (viewing and updating tasks)
- Reporting users (searching and viewing statistics)

### 2. Test Data Management

- Use CSV Data Set Config to load test data from external files
- Implement proper test data cleanup after test runs
- Generate unique data for each test run to avoid conflicts

Example CSV structure for tasks:

```
title,description,priority,status
Task 1,Description 1,HIGH,OPEN
Task 2,Description 2,MID,PENDING
...
```

### 3. Assertions and Validations

Add these assertions to validate responses:

- Response Assertion to verify HTTP status codes
- JSON Assertion to validate response structure
- Duration Assertion to check response time thresholds

Example assertions:

```
Response Assertion:
- Pattern to test: 200|201|202
- Test field: Response code

JSON Assertion:
- Assert JSON Path: $.success
- Expected value: true
```

### 4. Monitoring and Results

Configure these listeners for monitoring:

- View Results Tree (for debugging)
- Summary Report (for test results overview)
- Aggregate Report (for detailed statistics)
- Response Time Graph (for visual analysis)

### 5. JMeter Properties

Create a user.properties file with these settings:

```properties
# Increase JMeter performance
httpclient.timeout=60000
httpclient.socket.http.cps=0
httpclient.socket.https.cps=0

# Connection pooling
httpsampler.max_connections=1000
httpsampler.max_connections_per_host=500

# Reduce resource usage
view.results.tree.max_size=10000
```

### 6. Correlation and Dynamic Values

Implement proper correlation for:

- Authentication tokens
- CSRF tokens (if used)
- Task IDs between related requests

Example correlation with the JSON Extractor:

```
JSON Path: $.token
Reference Name: AUTH_TOKEN
Match No: 1
```

## Database Impact Considerations

- Consider running tests against a dedicated test database
- Reset database state between major test runs
- Monitor database performance metrics during tests:
  - Connection pool utilization
  - Query execution times
  - Transaction rates
  - Table locks

## Reporting Templates

Create custom dashboards with:

- Response time metrics over time
- Throughput (transactions per second)
- Error rates by endpoint
- User concurrency vs. response time correlation

## CI/CD Integration

Example Jenkins pipeline stage:

```groovy
stage('Performance Testing') {
    steps {
        sh '''
            cd /path/to/jmeter
            ./jmeter -n -t /workspace/backend/src/test/java/de/hse/focusflow/jmeter/focusflow_test_plan.jmx -l results.jtl -e -o report
        '''
    }
    post {
        always {
            perfReport sourceDataFiles: 'results.jtl', errorFailedThreshold: 5, errorUnstableThreshold: 3,
                      errorUnstableResponseTimeThreshold: 'average:500'
        }
    }
}
```
