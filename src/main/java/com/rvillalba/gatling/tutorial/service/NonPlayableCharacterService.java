package com.rvillalba.gatling.tutorial.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.rvillalba.gatling.tutorial.config.SimulationProperties;
import com.rvillalba.gatling.tutorial.dto.CharacterType;
import com.rvillalba.gatling.tutorial.dto.NonPlayableCharacter;

import net.datafaker.Faker;

@Service
public class NonPlayableCharacterService {

    private static final CharacterType[] TYPES = CharacterType.values();

    private final Faker faker;
    private final SimulationProperties properties;

    public NonPlayableCharacterService(Faker faker, SimulationProperties properties) {
        this.faker = faker;
        this.properties = properties;
    }

    public List<NonPlayableCharacter> getRandomNonPlayableCharacters(int quantity) {
        failRandomly();
        return IntStream.range(0, quantity)
                .mapToObj(i -> generateRandom())
                .toList();
    }

    private NonPlayableCharacter generateRandom() {
        simulateDatabaseLatency();
        return new NonPlayableCharacter(
                faker.dragonBall().character(),
                faker.name().lastName(),
                TYPES[faker.number().numberBetween(0, TYPES.length)],
                faker.number().numberBetween(40, 100),
                faker.number().numberBetween(0, 10));
    }

    /**
     * Falla de forma aleatoria para que el informe de Gatling muestre también peticiones KO.
     */
    private void failRandomly() {
        if (ThreadLocalRandom.current().nextDouble() < properties.errorRate()) {
            throw new IllegalStateException("Random exception");
        }
    }

    private void simulateDatabaseLatency() {
        if (properties.latencyMillis() <= 0) {
            return;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(properties.latencyMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
