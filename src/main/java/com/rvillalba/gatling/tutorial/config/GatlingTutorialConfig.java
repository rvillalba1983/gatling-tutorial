package com.rvillalba.gatling.tutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.datafaker.Faker;

@Configuration
public class GatlingTutorialConfig {

    @Bean
    public Faker faker() {
        return new Faker();
    }
}
