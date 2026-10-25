package com.rvillalba.gatling.tutorial.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rvillalba.gatling.tutorial.dto.NonPlayableCharactersResponse;
import com.rvillalba.gatling.tutorial.service.NonPlayableCharacterService;

@RestController
@RequestMapping("/characters")
public class NonPlayableCharacterController {

    private final NonPlayableCharacterService nonPlayableCharacterService;

    public NonPlayableCharacterController(NonPlayableCharacterService nonPlayableCharacterService) {
        this.nonPlayableCharacterService = nonPlayableCharacterService;
    }

    /**
     * Devuelve {@code quantity} personajes aleatorios (mínimo 1).
     */
    @GetMapping("/randoms/{quantity}")
    public NonPlayableCharactersResponse getRandomNonPlayableCharacters(@PathVariable int quantity) {
        return new NonPlayableCharactersResponse(
                nonPlayableCharacterService.getRandomNonPlayableCharacters(Math.max(1, quantity)));
    }
}
