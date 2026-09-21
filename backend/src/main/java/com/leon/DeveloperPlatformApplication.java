package com.leon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * A modular monolith, deliberately (spec section 94). The modules below are
 * package boundaries rather than deployment boundaries: project, blog, github,
 * analytics, auth, contact. Scheduling is enabled here because the GitHub sync
 * job in a later phase depends on it.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class DeveloperPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeveloperPlatformApplication.class, args);
    }
}
