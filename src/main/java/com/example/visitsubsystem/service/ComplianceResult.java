package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.ComplianceFlag;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record ComplianceResult(ComplianceFlag flag, Set<AbnormalReason> reasons) {

    public ComplianceResult {
        if (flag == null) {
            throw new IllegalArgumentException("flag must not be null");
        }
        if (reasons == null) {
            throw new IllegalArgumentException("reasons must not be null; use an empty set when compliant");
        }
        reasons = reasons.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(reasons));
    }
}
