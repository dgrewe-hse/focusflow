# RON Backend

This is the backend service for the RON (Route Optimization Network) application, a route planning and field-service tool designed for companies installing water meters, heat cost allocators, water filters, and similar systems.

## Repository Structure

The backend codebase is organized as follows:

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/ron/
│   │   │   ├── config/       # Application configuration
│   │   │   ├── controller/   # REST API controllers
│   │   │   ├── dto/          # Data Transfer Objects
│   │   │   ├── exception/    # Custom exceptions and error handling
│   │   │   ├── model/        # Entity models
│   │   │   ├── repository/   # Data access repositories
│   │   │   ├── service/      # Business logic services
│   │   │   └── util/         # Utility classes
│   │   └── resources/
│   │       ├── application.properties  # Common properties
│   │       ├── application-dev.properties
│   │       ├── application-test.properties
│   │       └── db/migration/  # Flyway migration scripts
│   └── test/
│       └── java/com/ron/
│           ├── repository/   # Repository tests
│           ├── service/      # Service tests
│           └── controller/   # Controller tests
└── pom.xml                   # Maven configuration
```

## Database Schema

The application uses a PostgreSQL database with the following entity structure:

### Core Entities

- **Employee**: Represents users of the system with different roles (Admin, Manager, Office Staff, Worker)
- **WorkerProfile**: Extends Employee for field workers with additional attributes like skill level and work hours
- **Address**: Represents physical addresses with geocoding information
- **CustomerOrder**: Represents customer orders for installations
- **Task**: Represents tasks derived from customer orders assigned to workers
- **Route**: Represents daily routes for workers
- **RouteStop**: Represents stops in a route, linking to tasks
- **Photo**: Stores proof-of-visit photos with automatic expiry for GDPR compliance
- **DistanceMatrix**: Caches distances and travel times between addresses for route optimization

### Entity Relationships

- An **Employee** can have one **WorkerProfile** (if they are a field worker)
- A **WorkerProfile** belongs to one **Employee** and can have many **Tasks** and **Routes**
- A **CustomerOrder** has one delivery **Address** and can have many **Tasks**
- A **Task** belongs to one **CustomerOrder** and can be assigned to one **WorkerProfile**
- A **Route** belongs to one **WorkerProfile** and has many **RouteStops**
- A **RouteStop** belongs to one **Route** and one **Task**
- A **Photo** belongs to one **Task**
- A **DistanceMatrix** entry connects two **Addresses** (origin and destination)

## Technology Stack

- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway for database migrations
- JUnit and Mockito for testing

## Development Setup

### Prerequisites

- JDK 17 or higher
- Maven
- PostgreSQL

### Configuration

The application uses Spring profiles for different environments:

- `dev`: Development environment
- `test`: Testing environment
- `prod`: Production environment

Database configuration is stored in `application-{profile}.properties` files.

#### Development Profile

The development profile (`dev`) is configured for local development with:

- Local PostgreSQL database (`ron_dev`)
- Debug-level logging
- CSRF security disabled for easier API testing
- Swagger UI enabled
- Local storage paths for uploaded files

To run with the development profile:

```bash
# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Profile

The production profile (`prod`) is configured for deployment with:

- Database configuration from environment variables
- Info-level logging
- CSRF security enabled
- Swagger UI disabled for security
- Configurable storage paths via environment variables

Required environment variables for production:

- `JDBC_DATABASE_URL`: JDBC URL for the PostgreSQL database
- `JDBC_DATABASE_USERNAME`: Database username
- `JDBC_DATABASE_PASSWORD`: Database password
- `RON_PDF_STORAGE_LOCATION`: Storage location for PDF files (defaults to `/var/ron/storage/pdf`)
- `RON_PHOTO_STORAGE_LOCATION`: Storage location for photo files (defaults to `/var/ron/storage/photos`)

To run with the production profile:

```bash
# Set environment variables first
export JDBC_DATABASE_URL=jdbc:postgresql://your-db-host:5432/ron_prod
export JDBC_DATABASE_USERNAME=your_username
export JDBC_DATABASE_PASSWORD=your_password
export RON_PDF_STORAGE_LOCATION=/path/to/pdf/storage
export RON_PHOTO_STORAGE_LOCATION=/path/to/photo/storage

# Run with production profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Running the Application

```bash
# Run with development profile (default for local development)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with production profile (for deployment)
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Run tests
./mvnw test
```

### Accessing the API Documentation

The application uses SpringDoc OpenAPI for API documentation. Once the application is running, you can access the API documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

This provides an interactive UI where you can:

- Browse all available endpoints
- See request/response models
- Test API endpoints directly from the browser
- Download the OpenAPI specification in JSON or YAML format

You can also access the raw OpenAPI specification at:

```
http://localhost:8080/v3/api-docs
```

For a more specific API group, you can use:

```
http://localhost:8080/v3/api-docs/ron-api
```

### API Authentication

Most API endpoints require authentication. In the Swagger UI:

1. Click the "Authorize" button at the top of the page
2. Enter your credentials (for development, use the default test accounts)
3. After authorization, you can test secured endpoints

## Common Development Tasks

### Adding a New Entity

1. Create the entity class in `src/main/java/com/ron/model/`
2. Create a repository interface in `src/main/java/com/ron/repository/`
3. Create a Flyway migration script in `src/main/resources/db/migration/`
4. Add repository tests in `src/test/java/com/ron/repository/`

### Repository Naming Conventions

When creating query methods in repositories, follow these naming conventions:

- For direct field access: `findByFieldName()`
- For nested entity fields: `findByEntityFieldName()`
- For example, to find tasks by worker profile ID: `findByAssignedWorkerWorkerProfileId(UUID id)`

This naming convention is critical for Spring Data JPA to correctly generate queries.

### Database Migrations

The application uses Flyway for database migrations. Migration scripts are located in `src/main/resources/db/migration`.

To apply migrations manually:

```bash
./mvnw flyway:migrate
```

## API Documentation

API documentation is available at `/swagger-ui.html` when the application is running.

## Testing

The application has comprehensive test coverage:

- Unit tests for services and utilities
- Integration tests for repositories and controllers
- End-to-end tests for complete workflows

Run tests with:

```bash
./mvnw test
```

## Troubleshooting

### Common Issues

#### Repository Query Method Issues

If you encounter errors like `No property found for type`, ensure that:

- The method name correctly references the entity structure
- For nested properties, use the full path (e.g., `findByTaskTaskId` instead of `findByTaskId`)
- Entity relationships are properly defined with JPA annotations

#### Test Database Issues

- Tests use an in-memory H2 database with the `test` profile
- Ensure that `application-test.properties` has the correct H2 configuration
- For spatial data tests, verify that H2 is configured with the GIS extension

#### JaCoCo Coverage Issues

If you encounter "Unknown block type" errors with JaCoCo:

- Clean the project with `./mvnw clean`
- Ensure JaCoCo plugin version is compatible with Java 17
- Try running tests without the JaCoCo agent if debugging specific tests

## Code Style and Conventions

This project follows these conventions:

- **Naming**:

  - Classes: PascalCase (e.g., `CustomerOrder`)
  - Methods and variables: camelCase (e.g., `findByOrderId`)
  - Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)

- **Documentation**:

  - All public classes and methods should have JavaDoc comments
  - Complex logic should be explained with inline comments

- **Testing**:
  - Repository tests should verify all custom query methods
  - Use meaningful test data that represents real-world scenarios
  - Test edge cases and error conditions

## Performance Considerations

### Database Optimization

- The `DistanceMatrix` table caches travel distances to avoid repeated calculations
- Use indexed queries when searching large datasets
- Consider pagination for endpoints that return large collections

### Spatial Data

- Spatial queries use PostGIS extensions in PostgreSQL
- For complex spatial operations, consider using native queries with the `@Query` annotation
- Test spatial queries with realistic coordinate data
