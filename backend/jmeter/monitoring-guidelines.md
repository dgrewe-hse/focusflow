# FocusFlow Performance Monitoring Guidelines

This document provides guidelines for monitoring the FocusFlow application during JMeter load testing.

## Monitoring Areas

When conducting load tests, it's important to monitor multiple aspects of the system:

1. **Application Performance**
2. **Database Performance**
3. **JVM Metrics**
4. **Server Resources**
5. **Network Performance**

## Application Monitoring

### Spring Boot Actuator

FocusFlow uses Spring Boot Actuator for exposing operational information. Ensure these endpoints are enabled in your test environment:

- `/actuator/health` - Application health information
- `/actuator/metrics` - Application metrics

To configure additional endpoints, modify `application.properties`:

```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

#### Using Spring Boot Actuator for Monitoring

Spring Boot Actuator provides a powerful way to monitor your application during load testing. Here's how to use it effectively:

1. **Setting Up Actuator**

   Ensure the Spring Boot Actuator dependency is in your `pom.xml`:

   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

2. **Accessing Health Information**

   The health endpoint provides basic health information about your application:

   ```
   curl http://localhost:8080/actuator/health
   ```

   This returns JSON output like:

   ```json
   {
     "status": "UP",
     "components": {
       "db": {
         "status": "UP",
         "details": {
           "database": "PostgreSQL",
           "validationQuery": "isValid()"
         }
       },
       "diskSpace": {
         "status": "UP",
         "details": {
           "total": 250685575168,
           "free": 86721339392
         }
       }
     }
   }
   ```

3. **Viewing Available Metrics**

   To see all available metrics:

   ```
   curl http://localhost:8080/actuator/metrics
   ```

   This returns a list of all available metric names:

   ```json
   {
     "names": [
       "jvm.memory.used",
       "jvm.memory.max",
       "http.server.requests",
       "system.cpu.usage",
       "process.cpu.usage",
       ...
     ]
   }
   ```

4. **Retrieving Specific Metrics**

   To get details for a specific metric:

   ```
   curl http://localhost:8080/actuator/metrics/http.server.requests
   ```

   Example output:

   ```json
   {
     "name": "http.server.requests",
     "measurements": [
       {"statistic": "COUNT", "value": 5424.0},
       {"statistic": "TOTAL_TIME", "value": 5265.512721},
       {"statistic": "MAX", "value": 0.11394426}
     ],
     "availableTags": [
       {
         "tag": "uri",
         "values": ["/api/v1/tasks", "/api/v1/auth/login", ...]
       },
       {
         "tag": "status",
         "values": ["200", "404", "500"]
       }
     ]
   }
   ```

5. **Filtering Metrics by Tags**

   You can filter metrics by tags for more detailed analysis:

   ```
   curl http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/api/v1/tasks&tag=status:200
   ```

6. **Thread Dumps for Troubleshooting**

   To capture thread dumps when you identify performance issues:

   ```
   curl http://localhost:8080/actuator/threaddump > thread-dump.txt
   ```

7. **Environment Information**

   To see configuration properties and environment variables:

   ```
   curl http://localhost:8080/actuator/env
   ```

8. **Collecting Metrics During Load Testing**

   Create a script to periodically collect metrics during JMeter test runs:

   ```bash
   #!/bin/bash

   TEST_NAME=$1
   DURATION=$2
   INTERVAL=$3
   ENDPOINT="http://localhost:8080/actuator"

   mkdir -p "metrics/$TEST_NAME"

   echo "Starting metrics collection for $TEST_NAME for $DURATION seconds"

   end=$((SECONDS + DURATION))

   while [ $SECONDS -lt $end ]; do
     timestamp=$(date +%Y%m%d%H%M%S)

     # Collect key metrics
     curl -s "$ENDPOINT/metrics/http.server.requests" > "metrics/$TEST_NAME/requests-$timestamp.json"
     curl -s "$ENDPOINT/metrics/system.cpu.usage" > "metrics/$TEST_NAME/cpu-$timestamp.json"
     curl -s "$ENDPOINT/metrics/jvm.memory.used" > "metrics/$TEST_NAME/memory-$timestamp.json"

     sleep $INTERVAL
   done

   echo "Metrics collection completed"
   ```

   Usage: `./collect_metrics.sh load-test-1 300 10` (collect for 300 seconds, every 10 seconds)

9. **Custom Metrics for Application-Specific Monitoring**

   Add custom metrics in your code to track business-specific operations:

   ```java
   @Component
   public class TaskMetrics {
       private final MeterRegistry meterRegistry;

       public TaskMetrics(MeterRegistry meterRegistry) {
           this.meterRegistry = meterRegistry;
       }

       public void recordTaskCreation(String priority) {
           meterRegistry.counter("app.tasks.created", "priority", priority).increment();
       }

       public Timer createTaskTimer() {
           return meterRegistry.timer("app.tasks.processing.time");
       }
   }
   ```

### Key Application Metrics to Monitor

During load testing, pay special attention to these metrics. The threshold values below are general recommendations based on industry standards and should be adjusted based on your specific application requirements and baseline performance tests:

| Metric                             | Description                  | Warning Threshold | Critical Threshold |
| ---------------------------------- | ---------------------------- | ----------------- | ------------------ |
| `http.server.requests`             | Request count and timing     | Avg > 500ms       | Avg > 1000ms       |
| `process.cpu.usage`                | CPU usage by the JVM process | > 70%             | > 90%              |
| `system.cpu.usage`                 | System-wide CPU usage        | > 80%             | > 95%              |
| `jvm.memory.used`                  | Memory usage                 | > 70% of max      | > 90% of max       |
| `http.server.requests.seconds.max` | Max request time             | > 2s              | > 5s               |
| `tomcat.threads.busy`              | Busy Tomcat threads          | > 70% of max      | > 90% of max       |

## Database Monitoring

### PostgreSQL Metrics

For the PostgreSQL database, monitor these key metrics. These threshold values should be considered as starting points and refined based on your database size, query patterns, and expected load:

| Metric             | Description                       | Warning Threshold   | Critical Threshold  |
| ------------------ | --------------------------------- | ------------------- | ------------------- |
| Active Connections | Number of active connections      | > 70% of max        | > 90% of max        |
| Transaction Rate   | Transactions per second           | Depends on baseline | 2x baseline         |
| Cache Hit Ratio    | Buffer cache efficiency           | < 90%               | < 80%               |
| Slow Queries       | Queries exceeding threshold       | > 1% of all queries | > 5% of all queries |
| Lock Contention    | Locks causing waits               | > 5 waits/min       | > 20 waits/min      |
| Index Usage        | Ratio of index scans to seq scans | < 80%               | < 60%               |

## JVM Monitoring

### Garbage Collection

Monitor garbage collection metrics to identify memory issues:

- GC Pause times
- GC Frequency
- Memory usage after GC

Use JDK Flight Recorder or VisualVM to capture detailed GC statistics.

### Thread Usage

Monitor thread states using JConsole or the `/actuator/threaddump` endpoint:

- Thread count
- Blocked threads
- Thread CPU usage

### Heap Usage

Watch for memory leaks by monitoring:

- Heap usage over time
- Object creation rates
- Largest object collections

## Server Resource Monitoring

### System-level Metrics

Use simple command-line tools to monitor:

**CPU Usage:**

```bash
top -b -n 1 | grep "Cpu(s)"
```

**Memory Usage:**

```bash
free -m
```

**Disk I/O:**

```bash
iostat -x 1 3
```

**Network Traffic:**

```bash
iftop -t
```

### Linux Server Metrics

On Linux servers, monitor these files under `/proc`:

- `/proc/loadavg` - System load
- `/proc/meminfo` - Memory information
- `/proc/net/dev` - Network statistics
- `/proc/diskstats` - Disk statistics

## Correlation with JMeter Results

To correlate JMeter test results with monitoring data:

1. Ensure all systems use synchronized time
2. Record test start/end times precisely
3. Tag test runs in your monitoring system
4. Export timing data in compatible formats

## Performance Bottleneck Analysis

When analyzing test results, look for these common bottlenecks:

1. **Database Issues:**

   - Slow queries
   - Connection pool exhaustion
   - Lock contention

2. **JVM Issues:**

   - Excessive GC pauses
   - Memory leaks
   - Thread contention

3. **Network Issues:**

   - High latency
   - Bandwidth saturation
   - Connection resets

4. **Application Issues:**
   - Inefficient code paths
   - Third-party service dependencies
   - Resource leaks

## Performance Test Reporting Template

For each load test, create a report containing:

1. **Test Summary:**

   - Test date and duration
   - Test scenario description
   - User load pattern

2. **Test Results:**

   - Response time statistics (min, max, avg, 95th percentile)
   - Throughput (TPS)
   - Error rates and types

3. **System Metrics:**

   - CPU usage peak/average
   - Memory usage peak/average
   - Database connection utilization
   - GC statistics

4. **Bottlenecks Identified:**

   - Resource limitations
   - Scalability issues
   - Component-specific problems

5. **Recommendations:**
   - Immediate fixes
   - Long-term improvements
   - Further testing needed

## Automatic Alerting

Consider setting up alerting thresholds for early detection of issues during testing. These values should be calibrated based on your application's specific performance characteristics:

1. **Response Time Alerts:**

   - Alert when 95th percentile exceeds 1000ms for 1 minute

2. **Error Rate Alerts:**

   - Alert when error rate exceeds 2% for 30 seconds

3. **Resource Alerts:**
   - Alert when CPU usage exceeds 85% for 2 minutes
   - Alert when memory usage exceeds 90% for 2 minutes
   - Alert when connection pool usage exceeds 80% for 1 minute

## Conclusion

Effective monitoring during load tests helps:

- Identify performance bottlenecks
- Validate system behavior under load
- Establish baseline performance metrics
- Guide optimization efforts

Always correlate application-level metrics with system-level metrics to get a complete picture of the system's behavior under load.

Remember to establish your own baseline metrics and adjust threshold values based on your specific application requirements and performance goals.
