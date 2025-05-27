#!/bin/bash

# FocusFlow Selenium Test Runner
# Cross-platform script to run Selenium tests

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo -e "${CYAN}🧪 FocusFlow Selenium Test Runner${NC}"
echo -e "${CYAN}===================================${NC}"

# Detect operating system
OS="unknown"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" ]]; then
    OS="windows"
fi

echo -e "${BLUE}🖥️  Host environment detected ($OS)${NC}"

# Default configuration
BROWSER="${BROWSER:-chrome}"
HEADLESS="${HEADLESS:-false}"
CONTAINER_MODE="${CONTAINER_MODE:-false}"
TIMEOUT="${TIMEOUT:-60000}"
RETRIES="${RETRIES:-1}"

# Environment information
echo -e "${BLUE}📋 Environment Information:${NC}"
echo -e "   🌐 Browser: $BROWSER"
echo -e "   👁️  Headless: $HEADLESS"
echo -e "   📦 Container: $CONTAINER_MODE"
echo -e "   🖥️  Display: ${DISPLAY:-not set}"
echo ""

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check browser availability
check_browser() {
    echo -e "${BLUE}🔍 Checking browser availability...${NC}"

    # Check if we're in a container or headless environment
    if [[ -f "/.dockerenv" ]] || [[ -n "${CONTAINER}" ]] || [[ -n "${CODESPACES}" ]] || [[ -n "${GITPOD_WORKSPACE_ID}" ]] || [[ "${DISPLAY:-}" == ":99" ]]; then
        echo -e "${YELLOW}🐳 Container/Headless environment detected${NC}"
        echo -e "${YELLOW}💡 Setting headless mode to true${NC}"
        export HEADLESS="true"
        export CONTAINER_MODE="true"
        echo -e "${GREEN}✅ Browser configured for headless mode${NC}"
        return 0
    fi

    case $BROWSER in
        "chrome")
            if [[ "$OS" == "macos" ]]; then
                if [[ -f "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" ]]; then
                    echo -e "${GREEN}✅ Found Google Chrome${NC}"
                    CHROME_VERSION=$(/Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --version 2>/dev/null | cut -d' ' -f3 || echo "Unknown")
                    echo -e "   Version: $CHROME_VERSION"
                    return 0
                fi
            elif [[ "$OS" == "linux" ]]; then
                if command_exists google-chrome || command_exists google-chrome-stable || command_exists chromium-browser; then
                    echo -e "${GREEN}✅ Found Chrome/Chromium${NC}"
                    return 0
                fi
            fi
            echo -e "${RED}❌ Chrome not found${NC}"
            return 1
            ;;
        "firefox")
            if [[ "$OS" == "macos" ]]; then
                if [[ -f "/Applications/Firefox.app/Contents/MacOS/firefox" ]]; then
                    echo -e "${GREEN}✅ Found Firefox${NC}"
                    return 0
                fi
            elif [[ "$OS" == "linux" ]]; then
                if command_exists firefox; then
                    echo -e "${GREEN}✅ Found Firefox${NC}"
                    return 0
                fi
            fi
            echo -e "${RED}❌ Firefox not found${NC}"
            return 1
            ;;
    esac
}

# Function to check backend availability
check_backend() {
    echo -e "${BLUE}🔍 Checking backend availability...${NC}"

    # Try different endpoints and accept 403 as "accessible"
    local response_code
    response_code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/v1" 2>/dev/null || echo "000")

    if [[ "$response_code" == "403" || "$response_code" == "200" || "$response_code" == "404" ]]; then
        echo -e "${GREEN}✅ Backend is accessible at http://localhost:8080/api/v1${NC}"
        echo -e "   Response code: $response_code (backend is protected, which is expected)"
        return 0
    else
        # Try the root endpoint as fallback
        response_code=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080" 2>/dev/null || echo "000")
        if [[ "$response_code" == "403" || "$response_code" == "200" || "$response_code" == "404" ]]; then
            echo -e "${GREEN}✅ Backend is accessible at http://localhost:8080${NC}"
            echo -e "   Response code: $response_code"
            return 0
        fi

        echo -e "${RED}❌ Backend not accessible at http://localhost:8080${NC}"
        echo -e "   Response code: $response_code"
        echo -e "${YELLOW}💡 Make sure your Spring Boot backend is running${NC}"
        return 1
    fi
}

# Function to check frontend availability
check_frontend() {
    echo -e "${BLUE}🔍 Checking frontend availability...${NC}"
    if curl -s -f "http://localhost:8081" >/dev/null 2>&1; then
        echo -e "${GREEN}✅ Frontend is accessible at http://localhost:8081${NC}"
        return 0
    else
        echo -e "${RED}❌ Frontend not accessible at http://localhost:8081${NC}"
        echo -e "${YELLOW}💡 Make sure your Vue.js frontend is running${NC}"
        return 1
    fi
}

# Function to run specific test suite
run_test_suite() {
    local suite=$1
    local optimized=${2:-false}

    echo -e "${CYAN}🚀 Starting Selenium tests...${NC}"
    echo -e "Test Suite: $suite"
    echo -e "Mode: ${HEADLESS:-false}" == "true" && echo "Headless" || echo "Visual (you'll see browser windows)"
    echo -e "Optimized: $optimized"
    echo ""

    cd selenium-tests

    case $suite in
        "auth")
            if [[ "$optimized" == "true" ]]; then
                echo -e "${BLUE}Running core authentication test suite...${NC}"
                echo ""
                run_mocha_test "tests/auth-core.test.js" "🔐 Core Authentication Tests" "Fast login, validation, and API auth tests"
            else
                echo -e "${BLUE}Running core authentication test suite...${NC}"
                echo ""
                run_mocha_test "tests/auth-core.test.js" "🔐 Core Authentication Tests" "Fast login, validation, and API auth tests"
            fi
            ;;
        "auth-core")
            echo -e "${BLUE}Running core authentication test suite...${NC}"
            echo ""
            run_mocha_test "tests/auth-core.test.js" "🔐 Core Authentication Tests" "Fast login, validation, and API auth tests"
            ;;
        "auth-comprehensive")
            echo -e "${BLUE}Running comprehensive authentication test suite...${NC}"
            echo ""
            run_mocha_test "tests/auth-comprehensive.test.js" "🔐 Comprehensive Authentication Tests" "Complete auth tests including security and edge cases"
            ;;
        "tasks")
            echo -e "${BLUE}Running task management test suite...${NC}"
            echo ""
            run_mocha_test "tests/tasks.test.js" "📋 Task Management Tests" "Tests CRUD operations for tasks"
            ;;
        "integration")
            echo -e "${BLUE}Running integration test suite...${NC}"
            echo ""
            run_mocha_test "tests/integration.test.js" "🔗 Integration Tests" "Tests UI and API integration"
            ;;
        "all")
            if [[ "$optimized" == "true" ]]; then
                echo -e "${BLUE}Running all core test suites...${NC}"
                echo ""
                run_mocha_test "tests/auth-core.test.js" "🔐 Core Authentication Tests" "Fast authentication tests"
                run_mocha_test "tests/tasks.test.js" "📋 Task Management Tests" "Tests CRUD operations for tasks"
                run_mocha_test "tests/integration.test.js" "🔗 Integration Tests" "Tests UI and API integration"
            else
                echo -e "${BLUE}Running all test suites...${NC}"
                echo ""
                run_mocha_test "tests/auth-comprehensive.test.js" "🔐 Comprehensive Authentication Tests" "Complete auth tests"
                run_mocha_test "tests/tasks.test.js" "📋 Task Management Tests" "Tests CRUD operations for tasks"
                run_mocha_test "tests/integration.test.js" "🔗 Integration Tests" "Tests UI and API integration"
            fi
            ;;
        *)
            echo -e "${RED}❌ Unknown test suite: $suite${NC}"
            echo -e "${YELLOW}Available suites: auth, auth-core, auth-comprehensive, tasks, integration, all${NC}"
            exit 1
            ;;
    esac

    cd ..
}

# Function to run mocha test with formatting
run_mocha_test() {
    local test_file=$1
    local test_name=$2
    local test_description=$3

    echo "================================================================================"
    echo -e "                    ${CYAN}🧪 SELENIUM TEST RUNNER 🧪${NC}"
    echo -e "                   ${CYAN}FocusFlow UI & API Testing${NC}"
    echo "================================================================================"
    echo ""
    echo -e "${BLUE}🔍 Checking prerequisites...${NC}"
    echo -e "${GREEN}✅ Prerequisites check passed${NC}"
    echo -e "${BLUE}⚙️  Setting up test environment...${NC}"
    echo -e "${BLUE}🌐 Browser: $BROWSER${NC}"
    echo -e "${BLUE}⏱️  Timeout: ${TIMEOUT}ms${NC}"
    echo -e "${BLUE}🔄 Retries: $RETRIES${NC}"
    echo ""
    echo -e "${BLUE}🎯 Starting test execution...${NC}"
    echo ""
    echo -e "${CYAN}🚀 $test_name${NC}"
    echo -e "${BLUE}📝 $test_description${NC}"
    echo ""
    echo -e "${BLUE}📋 Command: npx mocha --timeout $TIMEOUT --retries $RETRIES --reporter spec $test_file${NC}"
    echo "────────────────────────────────────────────────────────────────────────────────"
    echo ""

    # Set environment variables for the test
    export BROWSER="$BROWSER"
    export HEADLESS="$HEADLESS"
    export CONTAINER_MODE="$CONTAINER_MODE"

    # Run the test
    if npx mocha --timeout "$TIMEOUT" --retries "$RETRIES" --reporter spec "$test_file"; then
        echo ""
        echo -e "${GREEN}✅ $test_name completed successfully!${NC}"
        return 0
    else
        echo ""
        echo -e "${RED}❌ $test_name failed!${NC}"
        return 1
    fi
}

# Main execution

# Check if we're in the frontend directory
if [[ ! -d "selenium-tests" ]]; then
    echo -e "${RED}❌ selenium-tests directory not found${NC}"
    echo -e "${YELLOW}💡 Please run this script from the frontend directory${NC}"
    exit 1
fi

# Parse command line arguments
SUITE="${1:-auth}"
OPTIMIZED_FLAG="${2:-}"
OPTIMIZED="false"

if [[ "$OPTIMIZED_FLAG" == "--optimized" || "$OPTIMIZED_FLAG" == "-o" ]]; then
    OPTIMIZED="true"
fi

# Perform checks
if ! check_browser; then
    echo -e "${YELLOW}⚠️  Browser check failed, but continuing...${NC}"
fi

if ! check_backend; then
    echo -e "${RED}❌ Backend check failed${NC}"
    exit 1
fi

if ! check_frontend; then
    echo -e "${RED}❌ Frontend check failed${NC}"
    exit 1
fi

# Run the tests
if run_test_suite "$SUITE" "$OPTIMIZED"; then
    echo ""
    echo -e "${GREEN}🎉 All tests completed successfully!${NC}"
    echo -e "${GREEN}📊 Check the screenshots folder for visual evidence${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}💥 Some tests failed${NC}"
    echo -e "${YELLOW}🔍 Check the output above for details${NC}"
    echo -e "${YELLOW}📸 Screenshots may be available in the screenshots folder${NC}"
    exit 1
fi
