package com.rvillalba.gatling.tutorial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros para que la API se comporte como un servicio "real" durante las pruebas de carga.
 *
 * @param errorRate     probabilidad (0.0 - 1.0) de que una petición falle con un error 500
 * @param latencyMillis latencia simulada por cada personaje generado (como si se consultara una base de datos)
 */
@ConfigurationProperties(prefix = "app.simulation")
public record SimulationProperties(
        @DefaultValue("0.05") double errorRate,
        @DefaultValue("5") long latencyMillis) {
}
