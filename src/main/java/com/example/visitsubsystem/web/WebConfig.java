package com.example.visitsubsystem.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    DisplayFormats displayFormats() {
        return new DisplayFormats();
    }
}
