package com.trecapps.sm;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication
@ComponentScan({
        "com.trecapps.sm.common.*",                     // Scan this app

        "com.trecapps.sm.common",
        "com.trecauth.common.*",               // Authentication library
        "com.trecauth.webflux.*",
        "com.trecapps.sm.profile.*",                // Enable Profile Features
        "com.trecapps.sm.content",                   // Enable Content Features
        "com.trecapps.sm.content.*"
})
@EnableReactiveMongoRepositories(basePackages = {
        "com.trecapps.sm.content.repos",
        "com.trecapps.sm.profile.repos"
}, reactiveMongoTemplateRef = "trecappsSMMongoTemplate")
@EnableWebFlux
public class BaseMonoDriver {
    public static void main(String[] args) {
        ApplicationInsights.attach();
        SpringApplication.run(BaseMonoDriver.class, args);
    }
}

