package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.ComplianceFlag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ComplianceResultTest {

    @Test
    void rejectsNullFlag() {
        assertThrows(IllegalArgumentException.class, () -> new ComplianceResult(null, java.util.Set.of()));
    }

    @Test
    void rejectsNullReasons() {
        assertThrows(IllegalArgumentException.class, () -> new ComplianceResult(ComplianceFlag.OK, null));
    }

    @Test
    void acceptsAbnormalFlagWithReasonsAndKeepsThemUnmodifiable() {
        var result = new ComplianceResult(ComplianceFlag.ABNORMAL, java.util.Set.of(AbnormalReason.SHORT_DURATION));

        assertThrows(UnsupportedOperationException.class, () -> result.reasons().add(AbnormalReason.FAR_FROM_HOSPITAL));
    }
}
