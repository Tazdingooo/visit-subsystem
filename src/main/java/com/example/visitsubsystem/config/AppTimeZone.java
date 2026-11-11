package com.example.visitsubsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * Single source of truth for the zone used to bucket months and render times.
 *
 * <p>Defaults to {@code Asia/Shanghai} (UTC+8) because the target deployment runs
 * inside a container whose JVM defaults to UTC, which would otherwise shift
 * month boundaries and displayed timestamps.</p>
 */
@Configuration
public class AppTimeZone {

    @Bean
    ZoneId appZone(@Value("${visit.time-zone:Asia/Shanghai}") String zone) {
        return ZoneId.of(zone);
    }
}
