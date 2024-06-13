package com.message.unitedmessageengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class UnitedMessageEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnitedMessageEngineApplication.class, args);
    }

}
