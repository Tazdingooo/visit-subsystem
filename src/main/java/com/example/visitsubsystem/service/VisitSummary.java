package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.ComplianceFlag;
import com.example.visitsubsystem.domain.VisitStatus;

import java.time.Instant;

public record VisitSummary(
        long id,
        String hcpName,
        String hospitalName,
        String productName,
        String departmentName,
        Instant plannedAt,
        VisitStatus status,
        ComplianceFlag complianceFlag) {
}
