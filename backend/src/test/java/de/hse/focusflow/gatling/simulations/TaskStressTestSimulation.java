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
 * Stress test simulation for TaskController.
 *
 * This simulation aims to find the breaking point of the system by:
 * - Gradually increasing load to high levels
 * - Using a mix of read-intensive and write-intensive scenarios
 * - Running for an extended period to detect memory leaks or degradation
 *
 * The simulation is suitable for:
 * - Finding system limits and bottlenecks
 * - Testing behavior under extreme load
 * - Identifying failure points before they occur in production
 */
public class TaskStressTestSimulation extends Simulation {

    // HTTP protocol configuration
    private final io.gatling.javaapi.http.HttpProtocolBuilder httpProtocol = Configuration.httpProtocol();

    // Define mixed workload with read and write operations
    private PopulationBuilder readScenario = TaskScenarios.readIntensiveScenario().injectOpen(
            rampUsers(50).during(30), // Ramp up to 50 users over 30 seconds
            constantUsersPerSec(10).during(180).randomized() // Maintain 10 new users per second for 3 minutes
    );

    private PopulationBuilder writeScenario = TaskScenarios.writeIntensiveScenario().injectOpen(
            rampUsers(20).during(30), // Ramp up to 20 users over 30 seconds
            constantUsersPerSec(5).during(180).randomized() // Maintain 5 new users per second for 3 minutes
    );

    {
        // Configure the simulation with multiple scenarios
        setUp(
                readScenario.protocols(httpProtocol),
                writeScenario.protocols(httpProtocol)).assertions(
                        // Less strict assertions for stress testing
                        global().responseTime().percentile3().lt(3000), // 95th percentile under 3 seconds
                        global().successfulRequests().percent().gt(85.0) // At least 85% successful requests
        ).maxDuration(600); // Maximum duration of 10 minutes
    }
}
