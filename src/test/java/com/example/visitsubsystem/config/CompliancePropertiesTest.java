package com.example.visitsubsystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class CompliancePropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(EnableProperties.class);

    @Test
    void bindsValidValues() {
        runner.withPropertyValues(
                        "visit.compliance.min-duration-seconds=600",
                        "visit.compliance.max-hospital-distance-meters=750")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    ComplianceProperties props = context.getBean(ComplianceProperties.class);
                    assertThat(props.minDurationSeconds()).isEqualTo(600);
                    assertThat(props.maxHospitalDistanceMeters()).isEqualTo(750.0);
                });
    }

    @Test
    void appliesDefaultsWhenPropertiesAreAbsent() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            ComplianceProperties props = context.getBean(ComplianceProperties.class);
            assertThat(props.minDurationSeconds()).isEqualTo(300);
            assertThat(props.maxHospitalDistanceMeters()).isEqualTo(500.0);
        });
    }

    @Test
    void failsStartupWhenMinDurationIsNegative() {
        runner.withPropertyValues("visit.compliance.min-duration-seconds=-1")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class);
                });
    }

    @Test
    void failsStartupWhenMaxDistanceIsNegative() {
        runner.withPropertyValues("visit.compliance.max-hospital-distance-meters=-1")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class);
                });
    }

    @Configuration
    @EnableConfigurationProperties(ComplianceProperties.class)
    static class EnableProperties {
    }
}
