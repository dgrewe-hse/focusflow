#!/bin/bash

# Script to set up JMeter tests for FocusFlow API
# Author: FocusFlow Team
# Version: 1.0

# Define colors for output
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Define variables
JMETER_VERSION="5.6.3"
JMETER_DOWNLOAD_URL="https://dlcdn.apache.org//jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_DIR="$(cd "$SCRIPT_DIR/../../../../.." && pwd)"
DOWNLOAD_DIR="/tmp"
JMETER_HOME="${DOWNLOAD_DIR}/apache-jmeter-${JMETER_VERSION}"
TEST_PLAN_DIR="${SCRIPT_DIR}"
TEST_PLAN="${TEST_PLAN_DIR}/focusflow_test_plan.jmx"

# Function to print header
print_header() {
    echo -e "\n${YELLOW}======================================${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}======================================${NC}"
}

# Check if JMeter is already installed
check_jmeter() {
    print_header "Checking for JMeter installation"

    if [ -d "$JMETER_HOME" ]; then
        echo -e "${GREEN}JMeter ${JMETER_VERSION} found at $JMETER_HOME${NC}"
        return 0
    else
        echo -e "${YELLOW}JMeter not found. Will download and install.${NC}"
        return 1
    fi
}

# Download and install JMeter
install_jmeter() {
    print_header "Downloading and installing JMeter ${JMETER_VERSION}"

    if [ ! -f "${DOWNLOAD_DIR}/apache-jmeter-${JMETER_VERSION}.tgz" ]; then
        echo "Downloading JMeter ${JMETER_VERSION}..."
        wget -q --show-progress -P ${DOWNLOAD_DIR} ${JMETER_DOWNLOAD_URL}

        if [ $? -ne 0 ]; then
            echo -e "${RED}Failed to download JMeter. Please check your internet connection.${NC}"
            exit 1
        fi
    else
        echo "JMeter archive already downloaded."
    fi

    echo "Extracting JMeter archive..."
    tar -xzf ${DOWNLOAD_DIR}/apache-jmeter-${JMETER_VERSION}.tgz -C ${DOWNLOAD_DIR}

    if [ $? -ne 0 ]; then
        echo -e "${RED}Failed to extract JMeter archive.${NC}"
        exit 1
    fi

    echo -e "${GREEN}JMeter installed successfully at $JMETER_HOME${NC}"
}

# Set up test plan from template
setup_test_plan() {
    print_header "Setting up JMeter test plan"

    # Check if test plan already exists
    if [ -f "$TEST_PLAN" ]; then
        echo -e "${YELLOW}Test plan already exists at $TEST_PLAN${NC}"
        read -p "Do you want to overwrite it? (y/n): " OVERWRITE
        if [[ $OVERWRITE != "y" && $OVERWRITE != "Y" ]]; then
            echo "Keeping existing test plan."
            return 0
        fi
    fi

    # Create basic JMeter test plan template (normally we'd use a .jmx file)
    echo "Creating test plan template..."
    echo "To create a proper JMeter test plan, please follow these steps:"
    echo "1. Open JMeter GUI: $JMETER_HOME/bin/jmeter.sh"
    echo "2. Create a new Test Plan following the documentation"
    echo "3. Save the test plan to: $TEST_PLAN"

    echo -e "${GREEN}Test plan setup instructions provided${NC}"
}

# Run JMeter test
run_test() {
    print_header "Running JMeter tests"

    if [ ! -f "$TEST_PLAN" ]; then
        echo -e "${RED}Test plan not found at $TEST_PLAN${NC}"
        echo "Please create a test plan first using the JMeter GUI."
        return 1
    fi

    # Make sure Spring Boot app is running
    echo "Checking if FocusFlow backend is running..."
    curl -s http://localhost:8080/actuator/health > /dev/null

    if [ $? -ne 0 ]; then
        echo -e "${YELLOW}FocusFlow backend doesn't seem to be running. Start it before running tests.${NC}"
        echo "You can start it with: cd $WORKSPACE_DIR && ./mvnw spring-boot:run"
        return 1
    fi

    # Create results directory
    TIMESTAMP=$(date +%Y%m%d_%H%M%S)
    RESULTS_DIR="${TEST_PLAN_DIR}/results_${TIMESTAMP}"
    mkdir -p "$RESULTS_DIR"

    echo "Running JMeter tests..."
    $JMETER_HOME/bin/jmeter -n -t "$TEST_PLAN" -l "${RESULTS_DIR}/results.jtl" -e -o "${RESULTS_DIR}/report"

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Tests completed successfully. Results available at:${NC}"
        echo "$RESULTS_DIR"
    else
        echo -e "${RED}Tests failed.${NC}"
        return 1
    fi
}

# Main execution
main() {
    print_header "FocusFlow JMeter Test Setup"

    # Check and install JMeter if needed
    check_jmeter || install_jmeter

    # Set up test plan
    setup_test_plan

    # Ask if user wants to run tests
    read -p "Do you want to run the tests now? (y/n): " RUN_TESTS
    if [[ $RUN_TESTS == "y" || $RUN_TESTS == "Y" ]]; then
        run_test
    else
        echo -e "${GREEN}Setup completed. Run the script with --run-test flag to execute tests.${NC}"
    fi
}

# Handle command line arguments
if [[ "$1" == "--run-test" ]]; then
    run_test
else
    main
fi

echo -e "\n${GREEN}Script completed.${NC}"
