package com.rvillalba.gatling.tutorial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GatlingTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatlingTutorialApplication.class, args);
    }
}
