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
package de.hse.focusflow.gatling.simulations;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.core.PopulationBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;

import de.hse.focusflow.gatling.scenarios.TaskScenarios;
import de.hse.focusflow.gatling.utils.Configuration;

/**
 * Basic load test simulation for TaskController.
 * Tests the system with a gradual ramp-up of users performing basic CRUD
 * operations.
 *
 * This simulation is suitable for:
 * - Basic performance testing
 * - Verifying the application works correctly under moderate load
 * - Establishing baseline performance metrics
 */
public class BasicTaskLoadSimulation extends Simulation {

    // HTTP protocol configuration for the simulation
    private final io.gatling.javaapi.http.HttpProtocolBuilder httpProtocol = Configuration.httpProtocol();

    // Define the load profile - gradual ramp up to moderate load
    private PopulationBuilder scn = TaskScenarios.basicTaskCrudScenario().injectOpen(
            nothingFor(5), // Wait for 5 seconds initially
            rampUsers(10).during(30), // Ramp up to 10 users over 30 seconds
            constantUsersPerSec(2).during(60).randomized() // Maintain 2 new users per second for 1 minute
    );

    // Setup the simulation
    {
        // Log if we're using offline mode
        if (Configuration.OFFLINE_MODE) {
            System.out.println("*************************************************************");
            System.out.println("RUNNING IN OFFLINE MODE - No actual HTTP requests will be made");
            System.out.println("*************************************************************");
        }

        // Configure the simulation with a name and the scenario to run
        setUp(scn)
                .protocols(httpProtocol)
                .assertions(getAssertions());
    }

    // Get appropriate assertions based on mode
    private io.gatling.javaapi.core.Assertion[] getAssertions() {
        if (Configuration.OFFLINE_MODE) {
            // In offline mode, we only need minimal assertions
            return new io.gatling.javaapi.core.Assertion[] {
                    global().successfulRequests().percent().is(100.0)
            };
        } else {
            // In online mode, use full performance assertions
            return new io.gatling.javaapi.core.Assertion[] {
                    global().responseTime().mean().lt(500), // Average response time under 500ms
                    global().successfulRequests().percent().gt(95.0) // At least 95% successful requests
            };
        }
    }
}
