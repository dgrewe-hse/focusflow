/*
 * Copyright (c) 2025 FocusFlow Project Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.hse.focusflow.gatling.utils;

import io.gatling.javaapi.http.HttpDsl;
import io.gatling.javaapi.http.HttpProtocolBuilder;

/**
 * Configuration settings for Gatling load tests.
 * Provides environment-specific configurations and common HTTP settings.
 */
public class Configuration {

    // Flag for offline mode - when true, tests will skip actual HTTP calls
    // and simulate responses for development/debugging purposes
    public static final boolean OFFLINE_MODE = false;

    // Base URLs for different environments
    public static final String DEV_BASE_URL = "http://localhost:8080";
    public static final String TEST_BASE_URL = "http://test-server:8080";
    public static final String STAGING_BASE_URL = "http://staging-server:8080";

    // API paths
    public static final String API_BASE_PATH = "/api/v1";
    public static final String TASKS_PATH = API_BASE_PATH + "/tasks";
    public static final String AUTH_PATH = API_BASE_PATH + "/auth";

    // Test user credentials - Update these to match a valid user in your test
    // environment
    public static final String TEST_USERNAME = "admin@focusflow.de";
    public static final String TEST_PASSWORD = "password";

    /**
     * Creates the default HTTP protocol builder for the development environment
     */
    public static HttpProtocolBuilder httpProtocol() {
        return httpProtocol(DEV_BASE_URL);
    }

    /**
     * Creates an HTTP protocol builder for the specified base URL
     */
    public static HttpProtocolBuilder httpProtocol(String baseUrl) {
        return HttpDsl.http
                .baseUrl(baseUrl)
                .acceptHeader("application/json")
                .contentTypeHeader("application/json")
                .userAgentHeader("Gatling Load Test")
                .shareConnections(); // Reuse connections for better performance
    }

    // Test data sizes
    public static final int SMALL_TEST_USERS = 10;
    public static final int MEDIUM_TEST_USERS = 50;
    public static final int LARGE_TEST_USERS = 200;

    // Test durations in seconds
    public static final int SHORT_TEST_DURATION = 60;
    public static final int MEDIUM_TEST_DURATION = 300;
    public static final int LONG_TEST_DURATION = 600;

    // Simulated response delays (milliseconds) for offline mode
    public static final int MIN_RESPONSE_DELAY = 50;
    public static final int MAX_RESPONSE_DELAY = 200;
}
