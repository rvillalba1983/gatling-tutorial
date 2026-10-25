package com.rvillalba.gatling.tutorial;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestClient;

import com.rvillalba.gatling.tutorial.dto.NonPlayableCharactersResponse;

/**
 * Test de integración: arranca la aplicación en un puerto aleatorio y llama al endpoint por HTTP.
 * Se desactivan los errores aleatorios y la latencia simulada para que el test sea determinista.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.simulation.error-rate=0", "app.simulation.latency-millis=0"})
class NonPlayableCharacterControllerIT {

    @Value("${local.server.port}")
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.create("http://localhost:" + port);
    }

    @Test
    void whenGetRandomCharactersWithQuantityFive_thenReturnsFiveCharacters() {
        var response = getRandoms(5);

        assertThat(response.characters())
                .hasSize(5)
                .allSatisfy(character -> {
                    assertThat(character.name()).isNotBlank();
                    assertThat(character.type()).isNotNull();
                    assertThat(character.hitPoints()).isBetween(40, 99);
                    assertThat(character.manaPotions()).isBetween(0, 9);
                });
    }

    @Test
    void whenGetRandomCharactersWithZeroQuantity_thenReturnsOneCharacter() {
        assertThat(getRandoms(0).characters()).hasSize(1);
    }

    private NonPlayableCharactersResponse getRandoms(int quantity) {
        return restClient.get()
                .uri("/characters/randoms/{quantity}", quantity)
                .retrieve()
                .body(NonPlayableCharactersResponse.class);
    }
}
