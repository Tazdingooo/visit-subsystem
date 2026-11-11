package com.example.visitsubsystem.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Compliance thresholds, configurable via {@code visit.compliance.*}.
 *
 * <p>The {@link DefaultValue}s mirror the values shipped in {@code application.yml},
 * so the app still boots if the properties are removed. Validation turns a
 * nonsensical configuration (e.g. a negative minimum duration, which would
 * silently disable the short-duration rule) into a startup failure.</p>
 */
@Validated
@ConfigurationProperties(prefix = "visit.compliance")
public record ComplianceProperties(
        @PositiveOrZero @DefaultValue("300") long minDurationSeconds,
        @DecimalMin("0.0") @DefaultValue("500.0") double maxHospitalDistanceMeters) {
}
