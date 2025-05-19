#!/bin/bash

# Task Management REST API Testing Script (No JQ Version)
# This script tests all endpoints of the Task Management REST API without requiring jq

# Configuration
BASE_URL="http://localhost:8080/api/v1"
API_URL="$BASE_URL/tasks"
AUTH_URL="$BASE_URL/auth/login"
AUTH_TOKEN=""
CONTENT_TYPE="Content-Type: application/json"
AUTH_HEADER="Authorization: Bearer "

# Test user credentials
TEST_EMAIL="test@focusflow.com"
TEST_PASSWORD="Test@123456"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print section header
print_header() {
    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}======================================${NC}"
}

# Function to print test result
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ $2${NC}"
    else
        echo -e "${RED}✗ $2 (Status code: $3)${NC}"
    fi
}

# Simple function to extract value from JSON response
# This is a very basic implementation and won't work for complex JSON
extract_value() {
    local json="$1"
    local key="$2"
    # Very simple pattern matching - will only work for simple JSON structures
    echo "$json" | grep -o "\"$key\":[^,}]*" | cut -d':' -f2- | tr -d ' "' | tr -d '}'
}

# Function to authenticate and get token
authenticate() {
    print_header "Authenticating with test user"

    echo -e "${BLUE}Attempting to login with test user (${TEST_EMAIL})...${NC}"

    RESPONSE=$(curl -s -X POST "$AUTH_URL" \
        -H "$CONTENT_TYPE" \
        -d '{
            "email": "'"$TEST_EMAIL"'",
            "password": "'"$TEST_PASSWORD"'"
        }')

    # Extract token using grep and cut
    AUTH_TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

    if [ -n "$AUTH_TOKEN" ]; then
        AUTH_HEADER="Authorization: Bearer $AUTH_TOKEN"
        echo -e "${GREEN}Authentication successful!${NC}"

        # Extract user ID using grep and cut
        USER_ID=$(echo "$RESPONSE" | grep -o '"userId":"[^"]*' | cut -d'"' -f4)
        echo -e "${GREEN}User ID: $USER_ID${NC}"

        # Get user teams
        get_user_teams
    else
        echo -e "${RED}Authentication failed.${NC}"
        echo "$RESPONSE"
    fi
}

# Function to get user teams
get_user_teams() {
    if [ -z "$USER_ID" ]; then
        echo -e "${YELLOW}No user ID available. Can't fetch teams.${NC}"
        return
    fi

    echo -e "${BLUE}Fetching teams for user...${NC}"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL/users/$USER_ID/teams" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$STATUS_CODE" -eq 200 ]; then
        echo -e "${GREEN}Teams retrieved successfully.${NC}"
        # Attempt to extract team ID using a simple pattern matching
        TEAM_ID=$(echo "$RESPONSE_BODY" | grep -o '"id":"[^"]*' | head -1 | cut -d'"' -f4)
        if [ -n "$TEAM_ID" ]; then
            echo -e "${GREEN}Found team ID: $TEAM_ID${NC}"
        else
            echo -e "${YELLOW}No teams found for user.${NC}"
        fi
    else
        echo -e "${YELLOW}Failed to fetch teams: $STATUS_CODE${NC}"
    fi
}

# Test variables
TASK_ID=""
USER_ID=""
TEAM_ID=""

# Calculate a date 7 days in the future in the required format
FUTURE_DATE=$(date -u -d "+7 days" "+%Y-%m-%dT%H:%M:%S.000Z" 2>/dev/null || date -u -v+7d "+%Y-%m-%dT%H:%M:%S.000Z")

# 1. Authentication
authenticate

# 2. Create a task
test_create_task() {
    print_header "Testing Create Task"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL" \
        -H "$CONTENT_TYPE" \
        -H "$AUTH_HEADER" \
        -d '{
            "title": "Test Task",
            "shortDescription": "This is a test task",
            "longDescription": "This is a detailed description of the test task",
            "dueDate": "'"$FUTURE_DATE"'",
            "priority": "MID",
            "status": "OPEN",
            "createdById": "'"$USER_ID"'"
        }')

    # Extract the status code
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    # Extract the response body without the status code
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    if [ "$STATUS_CODE" -eq 201 ]; then
        # Extract task ID from response
        TASK_ID=$(extract_value "$RESPONSE_BODY" "id")
        echo -e "Created task with ID: $TASK_ID"
    fi

    print_result $([[ "$STATUS_CODE" -eq 201 ]]; echo $?) "Create Task" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 3. Get all tasks
test_get_all_tasks() {
    print_header "Testing Get All Tasks"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL?includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get All Tasks" "$STATUS_CODE"
    echo "Response received successfully. Length: ${#RESPONSE_BODY} characters"
}

# 4. Get task by ID
test_get_task_by_id() {
    print_header "Testing Get Task by ID"

    if [ -z "$TASK_ID" ]; then
        echo -e "${YELLOW}No task ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/$TASK_ID?includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Task by ID" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 5. Update task
test_update_task() {
    print_header "Testing Update Task"

    if [ -z "$TASK_ID" ]; then
        echo -e "${YELLOW}No task ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X PUT "$API_URL/$TASK_ID" \
        -H "$CONTENT_TYPE" \
        -H "$AUTH_HEADER" \
        -d '{
            "title": "Updated Test Task",
            "shortDescription": "This is an updated test task",
            "longDescription": "This is an updated detailed description",
            "dueDate": "'"$FUTURE_DATE"'",
            "priority": "HIGH",
            "status": "IN_PROGRESS",
            "createdById": "'"$USER_ID"'"
        }')

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Update Task" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 6. Search tasks
test_search_tasks() {
    print_header "Testing Search Tasks"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/search?title=Test&status=IN_PROGRESS&includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Search Tasks" "$STATUS_CODE"
    echo "Response received successfully. Length: ${#RESPONSE_BODY} characters"
}

# 7. Get tasks by assignee
test_tasks_by_assignee() {
    print_header "Testing Get Tasks by Assignee"

    if [ -z "$USER_ID" ]; then
        echo -e "${YELLOW}No user ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/assignee/$USER_ID?includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Tasks by Assignee" "$STATUS_CODE"
    echo "Response received successfully. Length: ${#RESPONSE_BODY} characters"
}

# 8. Get tasks by team
test_tasks_by_team() {
    print_header "Testing Get Tasks by Team"

    if [ -z "$TEAM_ID" ]; then
        echo -e "${YELLOW}No team ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/team/$TEAM_ID?includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Tasks by Team" "$STATUS_CODE"
    echo "Response received successfully. Length: ${#RESPONSE_BODY} characters"
}

# 9. Get tasks by creator
test_tasks_by_creator() {
    print_header "Testing Get Tasks by Creator"

    if [ -z "$USER_ID" ]; then
        echo -e "${YELLOW}No user ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/creator/$USER_ID?includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Tasks by Creator" "$STATUS_CODE"
    echo "Response received successfully. Length: ${#RESPONSE_BODY} characters"
}

# 10. Get upcoming tasks
test_upcoming_tasks() {
    print_header "Testing Get Upcoming Tasks"

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/upcoming/7?includeTags=false" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Upcoming Tasks" "$STATUS_CODE"
    echo "Response received successfully. Length: ${#RESPONSE_BODY} characters"
}

# 11. Get task statistics by user
test_task_stats_by_user() {
    print_header "Testing Get Task Statistics by User"

    if [ -z "$USER_ID" ]; then
        echo -e "${YELLOW}No user ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/stats/user/$USER_ID" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Task Statistics by User" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 12. Get task statistics by team
test_task_stats_by_team() {
    print_header "Testing Get Task Statistics by Team"

    if [ -z "$TEAM_ID" ]; then
        echo -e "${YELLOW}No team ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "$API_URL/stats/team/$TEAM_ID" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Get Task Statistics by Team" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 13. Update task status
test_update_task_status() {
    print_header "Testing Update Task Status"

    if [ -z "$TASK_ID" ]; then
        echo -e "${YELLOW}No task ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$API_URL/$TASK_ID/status?status=IN_REVIEW" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Update Task Status" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 14. Update task assignee
test_update_task_assignee() {
    print_header "Testing Update Task Assignee"

    if [ -z "$TASK_ID" ]; then
        echo -e "${YELLOW}No task ID available. Skipping test.${NC}"
        return
    fi

    if [ -z "$USER_ID" ]; then
        echo -e "${YELLOW}No user ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X PATCH "$API_URL/$TASK_ID/assignee?assigneeId=$USER_ID" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    print_result $([[ "$STATUS_CODE" -eq 200 ]]; echo $?) "Update Task Assignee" "$STATUS_CODE"
    echo "Response: $RESPONSE_BODY"
}

# 15. Delete task
test_delete_task() {
    print_header "Testing Delete Task"

    if [ -z "$TASK_ID" ]; then
        echo -e "${YELLOW}No task ID available. Skipping test.${NC}"
        return
    fi

    RESPONSE=$(curl -s -w "\n%{http_code}" -X DELETE "$API_URL/$TASK_ID" \
        -H "$AUTH_HEADER")

    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)

    print_result $([[ "$STATUS_CODE" -eq 204 ]]; echo $?) "Delete Task" "$STATUS_CODE"
}

# Main execution
echo -e "${BLUE}Task Management REST API Testing Script (No JQ Version)${NC}"
echo -e "${YELLOW}This script tests all endpoints of the Task Management REST API.${NC}"
echo -e "${YELLOW}Please make sure the API server is running at $API_URL${NC}"
echo -e "${YELLOW}Using test user: $TEST_EMAIL${NC}"
echo

# Run tests
test_create_task
test_get_all_tasks
test_get_task_by_id
test_update_task
test_search_tasks
test_tasks_by_assignee
test_tasks_by_team
test_tasks_by_creator
test_upcoming_tasks
test_task_stats_by_user
test_task_stats_by_team
test_update_task_status
test_update_task_assignee

# Ask before deleting
echo -e "${YELLOW}Do you want to test deleting the task? (y/n)${NC}"
read DELETE_CONFIRMATION
if [[ $DELETE_CONFIRMATION == "y" || $DELETE_CONFIRMATION == "Y" ]]; then
    test_delete_task
else
    echo -e "${YELLOW}Skipping delete test.${NC}"
fi

echo
echo -e "${GREEN}Testing completed!${NC}"
