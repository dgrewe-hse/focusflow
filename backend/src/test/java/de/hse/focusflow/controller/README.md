# FocusFlow Controller Test Suite Documentation

## Overview

This directory contains comprehensive test suites for the FocusFlow API controllers. The tests are organized into different categories to ensure complete test coverage of the API endpoints.

## Test Structure

### Unit Tests

Unit tests focus on testing individual controller methods in isolation with mocked dependencies.

- **TaskControllerTest.java**: Tests all API endpoints of the TaskController with mocked service dependencies.

### Validation Tests

Validation tests focus on testing input validation and boundary values.

- **TaskControllerValidationTest.java**: Tests validation constraints such as required fields, string length limitations, etc.

### Security Tests

Security tests focus on testing authentication and authorization requirements.

- **TaskControllerSecurityTest.java**: Tests access control for different user roles and authentication requirements.

## Testing Approach

### Equivalence Class Partitioning

The tests use equivalence class partitioning to reduce the number of test cases while maintaining good coverage. For example:

- **Valid task data**: All required fields present with valid values
- **Invalid task data**: Missing or invalid required fields
- **Different authorization levels**: Unauthenticated, unauthorized, authorized users

### Boundary Value Analysis

The tests check boundary conditions for inputs:

- **String length limits**: Maximum length for title (100 chars) and description (200 chars)
- **Required fields**: Empty vs. null values
- **Enumeration values**: All possible values in TaskStatus and TaskPriority
- **Date constraints**: Past, present, and future dates for due dates

## Test Helper Classes

To make testing easier and reduce code duplication, we provide base test classes:

- **BaseUnitTest.java**: Base class for unit tests, sets up WebMvcTest configuration
- **BaseControllerTest.java**: Base class for integration tests, sets up SpringBootTest configuration

## Running the Tests

To run all controller tests:

```bash
./mvnw test -Dtest="*ControllerTest"
```

To run a specific test class:

```bash
./mvnw test -Dtest=TaskControllerTest
```

To run a specific test method:

```bash
./mvnw test -Dtest=TaskControllerTest#shouldCreateTaskWithValidData
```
