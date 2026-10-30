package com.libraryhub.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One Clock for the whole app, in the configured time zone. Tests replace it with a fixed one. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock(AppProperties properties) {
        return Clock.system(properties.zoneId());
    }
}
