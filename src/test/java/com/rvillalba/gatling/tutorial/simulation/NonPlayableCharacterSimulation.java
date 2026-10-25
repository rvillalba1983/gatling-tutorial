package com.rvillalba.gatling.tutorial.simulation;

import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

/**
 * Simulación de carga sobre GET /characters/randoms/{quantity}.
 *
 * <p>Parámetros (propiedades de sistema, con valores por defecto):
 * <ul>
 *   <li>{@code baseUrl}: URL de la API (http://localhost:8023)</li>
 *   <li>{@code users}: usuarios que entran en cada escenario (1000)</li>
 *   <li>{@code rampSeconds}: segundos durante los que van entrando esos usuarios (10)</li>
 * </ul>
 * Ejemplo: {@code mvn gatling:test -Dusers=200 -DrampSeconds=5}
 */
public class NonPlayableCharacterSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8023");
    private static final int USERS = Integer.getInteger("users", 1000);
    private static final int RAMP_SECONDS = Integer.getInteger("rampSeconds", 10);

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL + "/characters")
            .acceptHeader("application/json");

    // Chain: grupo de peticiones que se ejecutan en orden
    private final ChainBuilder progressiveRandoms =
            exec(http("1 result").get("/randoms/1").check(status().is(200)))
                    .exec(http("50 results").get("/randoms/50").check(status().is(200)))
                    .exec(http("500 results").get("/randoms/500").check(status().is(200)));

    private final ChainBuilder massiveRandoms =
            exec(http("1000 results").get("/randoms/1000").check(status().is(200)))
                    .exec(http("800 results").get("/randoms/800").check(status().is(200)))
                    .exec(http("1300 results").get("/randoms/1300").check(status().is(200)));

    // Scenario: cada uno se lanza en paralelo con su propia inyección de usuarios
    private final ScenarioBuilder progressiveCharactersScenario =
            scenario("Progressive Randoms").exec(progressiveRandoms);

    private final ScenarioBuilder massiveCharactersScenario =
            scenario("Massive Randoms").exec(massiveRandoms);

    {
        setUp(
                progressiveCharactersScenario.injectOpen(rampUsers(USERS).during(RAMP_SECONDS)),
                massiveCharactersScenario.injectOpen(rampUsers(USERS).during(RAMP_SECONDS)))
                .protocols(httpProtocol)
                // Assertions: si no se cumplen, la ejecución termina en error (útil en CI)
                .assertions(
                        global().failedRequests().percent().lt(10.0),
                        global().responseTime().percentile(95.0).lt(30_000));
    }
}
