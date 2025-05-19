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
 * Realistic user behavior simulation for TaskController.
 *
 * This simulation mimics real user behavior patterns with:
 * - Variable think times between actions
 * - Mix of different operations with realistic frequency
 * - Gradual increase and decrease of users (simulating workday patterns)
 *
 * The simulation is suitable for:
 * - Performance testing close to real-world conditions
 * - Understanding system behavior under typical loads
 * - Validating performance SLAs (Service Level Agreements)
 */
public class RealisticUserSimulation extends Simulation {

    // HTTP protocol configuration
    private final io.gatling.javaapi.http.HttpProtocolBuilder httpProtocol = Configuration.httpProtocol();

    // Define realistic user scenario with natural pauses and mixed operations
    private PopulationBuilder userPopulation = TaskScenarios.realisticUserScenario().injectOpen(
            // Ramp up users gradually (like morning hours)
            rampUsers(5).during(60), // 5 users over 1 minute

            // Peak usage time (like mid-day)
            rampUsersPerSec(1).to(3).during(120), // Ramp to 3 users per second over 2 minutes
            constantUsersPerSec(3).during(300), // Maintain 3 users per second for 5 minutes

            // Decline in usage (like afternoon/evening)
            rampUsersPerSec(3).to(1).during(120), // Ramp down to 1 user per second over 2 minutes
            constantUsersPerSec(1).during(120) // Maintain 1 user per second for 2 minutes
    );

    {
        setUp(userPopulation.protocols(httpProtocol))
                .assertions(
                        // Realistic SLA assertions
                        global().responseTime().mean().lt(800), // Average response time under 800ms
                        global().responseTime().percentile3().lt(1500), // 95th percentile under 1.5 seconds
                        global().failedRequests().percent().lt(5.0), // Failed requests under 5%
                        global().requestsPerSec().gt(10.0) // At least 10 requests per second throughput
                );
    }
}
