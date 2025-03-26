#!/bin/bash
set -e

echo "Initializing FocusFlow project..."

# Make Maven wrapper executable
echo "Setting up Maven wrapper..."
chmod +x /workspace/backend/mvnw

# Create Maven wrapper directory structure if it doesn't exist
mkdir -p /workspace/backend/.mvn/wrapper

# Copy Maven wrapper properties if they don't exist
if [ ! -f /workspace/backend/.mvn/wrapper/maven-wrapper.properties ]; then
  echo "Creating Maven wrapper properties..."
  echo "distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" > /workspace/backend/.mvn/wrapper/maven-wrapper.properties
  echo "wrapperUrl=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar" >> /workspace/backend/.mvn/wrapper/maven-wrapper.properties
fi

# Verify Maven installation
echo "Verifying Maven installation..."
mvn --version

echo "Project initialization complete!"
