# Task Management REST API

This document describes the REST API endpoints for managing tasks in the FocusFlow application.

## Base URL

All endpoints are prefixed with `/api/v1/tasks`

## Authentication

All endpoints require authentication using JWT Bearer token.

## Endpoints

### Get All Tasks

- **URL**: `/`
- **Method**: `GET`
- **Description**: Retrieves all tasks
- **Response**: 200 OK with list of tasks

### Get Task by ID

- **URL**: `/{id}`
- **Method**: `GET`
- **Description**: Retrieves a specific task by its ID
- **URL Parameters**: `id` - UUID of the task
- **Response**:
  - 200 OK with task details
  - 404 Not Found if task doesn't exist

### Create Task

- **URL**: `/`
- **Method**: `POST`
- **Description**: Creates a new task
- **Request Body**: TaskDTO object
- **Required Fields**:
  - `title` - Task title
  - `shortDescription` - Brief description
  - `dueDate` - Due date
  - `priority` - Task priority
  - `createdById` - ID of user creating the task
- **Optional Fields**:
  - `longDescription` - Detailed description
  - `assigneeId` - ID of user assigned to the task
  - `teamId` - ID of team assigned to the task
  - `tagIds` - Set of tag IDs
- **Response**:
  - 201 Created with the created task
  - 400 Bad Request if validation fails

### Update Task

- **URL**: `/{id}`
- **Method**: `PUT`
- **Description**: Updates an existing task
- **URL Parameters**: `id` - UUID of the task
- **Request Body**: TaskDTO object with fields to update
- **Response**:
  - 200 OK with updated task
  - 404 Not Found if task doesn't exist
  - 400 Bad Request if validation fails

### Delete Task

- **URL**: `/{id}`
- **Method**: `DELETE`
- **Description**: Deletes a task
- **URL Parameters**: `id` - UUID of the task
- **Response**:
  - 204 No Content on success
  - 404 Not Found if task doesn't exist

### Search Tasks

- **URL**: `/search`
- **Method**: `GET`
- **Description**: Searches tasks with optional filters
- **Query Parameters**:
  - `title` (optional) - Filter by title
  - `status` (optional) - Filter by status
  - `assigneeId` (optional) - Filter by assignee
  - `priority` (optional) - Filter by priority
- **Response**: 200 OK with list of matching tasks

### Get Tasks by Assignee

- **URL**: `/assignee/{userId}`
- **Method**: `GET`
- **Description**: Gets tasks assigned to a specific user
- **URL Parameters**: `userId` - UUID of the user
- **Response**: 200 OK with list of tasks

### Get Tasks by Team

- **URL**: `/team/{teamId}`
- **Method**: `GET`
- **Description**: Gets tasks assigned to a specific team
- **URL Parameters**: `teamId` - UUID of the team
- **Response**: 200 OK with list of tasks

### Get Tasks by Creator

- **URL**: `/creator/{userId}`
- **Method**: `GET`
- **Description**: Gets tasks created by a specific user
- **URL Parameters**: `userId` - UUID of the user
- **Response**: 200 OK with list of tasks

### Get Upcoming Tasks

- **URL**: `/upcoming/{days}`
- **Method**: `GET`
- **Description**: Gets tasks due within the specified number of days
- **URL Parameters**: `days` - Number of days to look ahead
- **Response**: 200 OK with list of upcoming tasks

### Get Task Statistics by User

- **URL**: `/stats/user/{userId}`
- **Method**: `GET`
- **Description**: Gets task statistics grouped by status for a user
- **URL Parameters**: `userId` - UUID of the user
- **Response**: 200 OK with map of status to count

### Get Task Statistics by Team

- **URL**: `/stats/team/{teamId}`
- **Method**: `GET`
- **Description**: Gets task statistics grouped by status for a team
- **URL Parameters**: `teamId` - UUID of the team
- **Response**: 200 OK with map of status to count

### Update Task Status

- **URL**: `/{id}/status`
- **Method**: `PATCH`
- **Description**: Updates only the status of a task
- **URL Parameters**: `id` - UUID of the task
- **Query Parameters**: `status` - New task status
- **Response**:
  - 200 OK with updated task
  - 404 Not Found if task doesn't exist
  - 400 Bad Request if status is invalid

### Update Task Assignee

- **URL**: `/{id}/assignee`
- **Method**: `PATCH`
- **Description**: Updates only the assignee of a task
- **URL Parameters**: `id` - UUID of the task
- **Query Parameters**: `assigneeId` - UUID of new assignee (or null to unassign)
- **Response**:
  - 200 OK with updated task
  - 404 Not Found if task doesn't exist

## Data Types

### TaskDTO

```json
{
  "id": "UUID",
  "title": "string",
  "shortDescription": "string",
  "longDescription": "string",
  "dueDate": "ISO-8601 datetime",
  "priority": "ENUM(LOW, MEDIUM, HIGH, URGENT)",
  "status": "ENUM(OPEN, IN_PROGRESS, REVIEW, BLOCKED, CLOSED)",
  "assigneeId": "UUID",
  "teamId": "UUID",
  "createdById": "UUID",
  "tagIds": ["UUID"]
}
```

### ApiResponse

```json
{
  "success": true,
  "message": "string",
  "data": "object"
}
```
