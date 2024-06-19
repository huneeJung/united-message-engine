package com.message.unitedmessageengine;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@EnableAsync
@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor
public class UnitedMessageEngineApplication {

    private final Environment env;

    public static void main(String[] args) {
        SpringApplication.run(UnitedMessageEngineApplication.class, args);
    }

    @PostConstruct
    public void started() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Locale.setDefault(Locale.KOREA);
    }

//    @EventListener(ApplicationReadyEvent.class)
//    public void run(String... strings) {
//        log.info("**********************************************************************************");
//        log.info(" Application Name   : " + env.getProperty("spring.application.name"));
//        log.info(" Active Profile     : " + env.getProperty("spring.profiles.active"));
//        log.info(" Listen Port        : " + env.getProperty("server.port"));
//        log.info(" Tomcat Max Threads : " + env.getProperty("server.tomcat.max-threads"));
//        log.info(" DB Pool Size       : " + env.getProperty("spring.datasource.maximum-pool-size"));
//        log.info(" Default Timezone   : " + TimeZone.getDefault().toZoneId());
//        log.info(" Default Locale     : " + Locale.getDefault());
//        log.info("**********************************************************************************");
//    }

}
