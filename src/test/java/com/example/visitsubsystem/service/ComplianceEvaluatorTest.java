package com.example.visitsubsystem.service;

import com.example.visitsubsystem.config.ComplianceProperties;
import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.ComplianceFlag;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplianceEvaluatorTest {

    private final ComplianceEvaluator evaluator = new ComplianceEvaluator(new ComplianceProperties(300, 500.0));

    @Test
    void exactFiveMinutesAndExactlyFiveHundredMetersAreCompliant() {
        ComplianceResult result = evaluator.evaluate(300, 0, 0, 0.0044966, 0);

        assertEquals(ComplianceFlag.OK, result.flag());
        assertEquals(Set.of(), result.reasons());
    }

    @Test
    void durationJustBelowFiveMinutesIsAbnormal() {
        ComplianceResult result = evaluator.evaluate(299, 0, 0, 0, 0);

        assertEquals(ComplianceFlag.ABNORMAL, result.flag());
        assertEquals(Set.of(AbnormalReason.SHORT_DURATION), result.reasons());
    }

    @Test
    void distanceJustAboveFiveHundredMetersIsAbnormal() {
        ComplianceResult result = evaluator.evaluate(300, 0, 0, 0.0045, 0);

        assertEquals(ComplianceFlag.ABNORMAL, result.flag());
        assertEquals(Set.of(AbnormalReason.FAR_FROM_HOSPITAL), result.reasons());
    }

    @Test
    void bothReasonsAreReportedTogether() {
        ComplianceResult result = evaluator.evaluate(0, 0, 0, 0.01, 0);

        assertEquals(ComplianceFlag.ABNORMAL, result.flag());
        assertEquals(Set.of(AbnormalReason.SHORT_DURATION, AbnormalReason.FAR_FROM_HOSPITAL), result.reasons());
    }

    @Test
    void rejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.evaluate(-1, 0, 0, 0, 0));
    }

    @Test
    void thresholdsAreDrivenByConfiguration() {
        ComplianceEvaluator custom = new ComplianceEvaluator(new ComplianceProperties(600, 1000.0));

        ComplianceResult result = custom.evaluate(599, 0, 0, 0.0055, 0);

        assertEquals(ComplianceFlag.ABNORMAL, result.flag());
        assertEquals(Set.of(AbnormalReason.SHORT_DURATION), result.reasons());
    }
}
