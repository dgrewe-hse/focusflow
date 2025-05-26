#!/bin/bash

# Cypress Runner Script for Dev Container
# This script sets up the virtual display and runs Cypress

# Function to start Xvfb
start_xvfb() {
    echo "Starting Xvfb virtual display..."
    Xvfb :99 -screen 0 1024x768x24 > /dev/null 2>&1 &
    export DISPLAY=:99
    sleep 2
    echo "Xvfb started on display :99"
}

# Function to stop Xvfb
stop_xvfb() {
    echo "Stopping Xvfb..."
    pkill Xvfb
}

# Function to run Cypress open
run_cypress_open() {
    echo "Starting Cypress Test Runner..."
    npx cypress open
}

# Function to run Cypress headless
run_cypress_headless() {
    echo "Running Cypress tests in headless mode..."
    npx cypress run
}

# Function to show help
show_help() {
    echo "Cypress Runner for Dev Container"
    echo ""
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  open      Start Cypress Test Runner (interactive)"
    echo "  run       Run Cypress tests in headless mode"
    echo "  headless  Same as 'run'"
    echo "  help      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 open     # Start interactive test runner"
    echo "  $0 run      # Run all tests headless"
    echo "  $0 headless # Run all tests headless"
}

# Main script logic
case "$1" in
    "open")
        start_xvfb
        trap stop_xvfb EXIT
        run_cypress_open
        ;;
    "run"|"headless")
        start_xvfb
        trap stop_xvfb EXIT
        run_cypress_headless
        ;;
    "help"|"--help"|"-h")
        show_help
        ;;
    "")
        echo "No command specified. Use 'help' for usage information."
        show_help
        exit 1
        ;;
    *)
        echo "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
