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

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.Session;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Authentication utilities for Gatling load tests.
 * Provides reusable authentication chains and token management.
 */
public class Authentication {

    // Mock JWT token for testing purposes
    private static final String MOCK_JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    /**
     * Creates a login chain that authenticates a user and stores the JWT token
     * in the session for subsequent requests.
     *
     * For testing purposes, this directly sets a mock JWT token instead of
     * making an actual API call.
     */
    public static ChainBuilder login() {
        return exec(session -> {
            // Set the mock token directly in the session
            Session updatedSession = session.set("authToken", MOCK_JWT_TOKEN);
            // Set the auth header in the same step
            updatedSession = updatedSession.set("authHeader", "Bearer " + MOCK_JWT_TOKEN);

            System.out.println("Using mock authentication with token: " + MOCK_JWT_TOKEN.substring(0, 10) + "...");

            return updatedSession;
        });
    }

    /**
     * Adds authorization header with JWT token to be used in authenticated requests
     */
    public static Session addAuthHeader(Session session) {
        String token = session.getString("authToken");
        if (token != null) {
            return session.set("authHeader", "Bearer " + token);
        }
        return session;
    }

    /**
     * Creates a ChainBuilder that adds the authorization header to the session
     */
    public static ChainBuilder withAuthHeader() {
        return exec(session -> {
            String token = session.getString("authToken");
            if (token != null) {
                return session.set("authHeader", "Bearer " + token);
            }
            return session;
        });
    }
}
