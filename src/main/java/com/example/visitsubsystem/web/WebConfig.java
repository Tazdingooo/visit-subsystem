package com.example.visitsubsystem.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
public class WebConfig {

    @Bean
    DisplayFormats displayFormats(ZoneId zone) {
        return new DisplayFormats(zone);
    }
}
