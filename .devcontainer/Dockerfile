FROM mcr.microsoft.com/devcontainers/java:17-bullseye

# Install Maven (specific version)
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends maven

# Set JAVA_HOME and PATH explicitly
ENV JAVA_HOME=/usr/lib/jvm/msopenjdk-current
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Node.js and npm (LTS version 20.x for Vue.js development)
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get update \
    && apt-get install -y nodejs \
    && npm install -g npm@latest \
    && npm install -g @vue/cli@latest \
    && npm install -g vite@latest \
    && npm install -g cypress

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

# Set up Node.js and npm configuration for vscode user
RUN mkdir -p /home/vscode/.npm-global \
    && chown -R vscode:vscode /home/vscode/.npm-global \
    && echo 'export PATH=/home/vscode/.npm-global/bin:$PATH' >> /home/vscode/.bashrc

# Set npm global directory for vscode user
ENV NPM_CONFIG_PREFIX=/home/vscode/.npm-global
ENV PATH=/home/vscode/.npm-global/bin:$PATH

# Set up workspace directory
WORKDIR /workspace
