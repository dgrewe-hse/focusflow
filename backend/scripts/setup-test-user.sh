#!/bin/bash

# Setup Test User Script
# This script tests if the automatically created test user is available and working

# Configuration
BASE_URL="http://localhost:8080/api/v1"
AUTH_LOGIN_URL="$BASE_URL/auth/login"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default test user credentials
TEST_EMAIL="test@focusflow.com"
TEST_PASSWORD="Test@123456"

# Function to print section header
print_header() {
    echo -e "\n${BLUE}======================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}======================================${NC}"
}

# Check if server is running
check_server() {
    print_header "Checking if server is running"

    # Try to connect to the server
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null)

    if [ $? -ne 0 ] || [ "$HTTP_CODE" != "200" ]; then
        echo -e "${RED}Server does not appear to be running at http://localhost:8080${NC}"
        echo -e "${YELLOW}Please start the server with:${NC}"
        echo -e "cd /workspace/backend && ./mvnw spring-boot:run"
        exit 1
    else
        echo -e "${GREEN}Server is running!${NC}"
    fi
}

# Test login with the test user
test_login() {
    print_header "Testing Login with Test User"

    echo -e "A test user should be automatically created when the Spring Boot application starts."
    echo -e "Credentials:"
    echo -e "Email: ${YELLOW}$TEST_EMAIL${NC}"
    echo -e "Password: ${YELLOW}$TEST_PASSWORD${NC}"
    echo
    echo -e "Attempting to login with test user credentials..."

    # Try to login
    RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$AUTH_LOGIN_URL" \
        -H "$CONTENT_TYPE" \
        -d '{
            "email": "'"$TEST_EMAIL"'",
            "password": "'"$TEST_PASSWORD"'"
        }')

    # Extract the status code
    STATUS_CODE=$(echo "$RESPONSE" | tail -n1)
    # Extract the response body without the status code
    RESPONSE_BODY=$(echo "$RESPONSE" | sed '$d')

    # Check login result
    if [ "$STATUS_CODE" -eq 200 ]; then
        echo -e "${GREEN}Login successful!${NC}"
        # Try to extract token
        if echo "$RESPONSE_BODY" | grep -q "token"; then
            TOKEN=$(echo "$RESPONSE_BODY" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
            echo -e "${GREEN}Authentication token: ${YELLOW}$TOKEN${NC}"
            echo -e "You can use this token for testing or use the credentials to login."
        fi
    else
        echo -e "${RED}Login failed. Status code: $STATUS_CODE${NC}"
        echo -e "Response: $RESPONSE_BODY"

        # Provide troubleshooting advice
        print_header "Troubleshooting"
        echo -e "Login failed. Here are some possible solutions:"
        echo -e "1. Ensure the Spring Boot application has fully started"
        echo -e "2. Check if the TestDataInitializer is working correctly"
        echo -e "3. Check server logs for any errors during initialization"
        echo -e "4. Verify that the authentication endpoints are working"
        echo -e ""
        echo -e "Common issues:"
        echo -e "- Database connection problems"
        echo -e "- TestDataInitializer not being executed"
        echo -e "- Authentication service not properly configured"
        echo -e "- Password encoding issues"
    fi
}

# Display test user info
display_user_info() {
    print_header "Test User Information"

    echo -e "When running the test scripts, use these credentials:"
    echo -e "${YELLOW}Email:${NC} $TEST_EMAIL"
    echo -e "${YELLOW}Password:${NC} $TEST_PASSWORD"
    echo
    echo -e "This test user is automatically created by the TestDataInitializer class"
    echo -e "when the Spring Boot application starts."
}

# Main execution
echo -e "${BLUE}FocusFlow API Test User Verification${NC}"

# Check if server is running
check_server

# Test login
test_login

# Display user info
display_user_info

# Instructions for next steps
print_header "Next Steps"
echo -e "1. Make the test scripts executable:"
echo -e "   ${YELLOW}chmod +x scripts/test-task-api.sh${NC}"
echo -e "   ${YELLOW}chmod +x scripts/test-task-api-no-jq.sh${NC}"
echo -e ""
echo -e "2. Run the test script:"
echo -e "   ${YELLOW}./scripts/test-task-api.sh${NC} (if you have jq installed)"
echo -e "   ${YELLOW}./scripts/test-task-api-no-jq.sh${NC} (if you don't have jq installed)"
echo -e ""
echo -e "3. When prompted for authentication, select 'y' to use the login endpoint"
echo -e "   and enter the test user credentials shown above."
echo -e ""
echo -e "If you continue to have authentication issues, check the server logs for more details."
