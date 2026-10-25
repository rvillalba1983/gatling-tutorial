package com.rvillalba.gatling.tutorial.dto;

public record NonPlayableCharacter(
        String name,
        String surname,
        CharacterType type,
        Integer hitPoints,
        Integer manaPotions) {
}
