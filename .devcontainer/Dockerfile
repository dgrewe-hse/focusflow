FROM mcr.microsoft.com/devcontainers/java:17-bullseye

# Install Maven (specific version)
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends maven

# Set JAVA_HOME and PATH explicitly
ENV JAVA_HOME=/usr/lib/jvm/msopenjdk-current
ENV PATH=$JAVA_HOME/bin:$PATH

# Install additional tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    curl \
    git \
    gnupg2 \
    unzip \
    wget \
    jq \
    vim \
    postgresql-client \
    python3 \
    python3-pip

# Clean up
RUN apt-get autoremove -y \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

# Ensure vscode user has sudo access
RUN apt-get update \
    && apt-get install -y sudo \
    && echo "vscode ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/vscode \
    && chmod 0440 /etc/sudoers.d/vscode

# Set the default user
USER vscode

# Set up Maven configuration
RUN mkdir -p /home/vscode/.m2 \
    && chown -R vscode:vscode /home/vscode/.m2


# Set up workspace directory
WORKDIR /workspace
