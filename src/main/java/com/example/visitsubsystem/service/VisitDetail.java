package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.ComplianceFlag;
import com.example.visitsubsystem.domain.VisitStatus;

import java.time.Instant;
import java.util.Set;

public record VisitDetail(
        long id,
        String hcpName,
        String hospitalName,
        double hospitalLat,
        double hospitalLng,
        String productName,
        String departmentName,
        Instant plannedAt,
        Instant checkInAt,
        Double checkInLat,
        Double checkInLng,
        Instant checkOutAt,
        Double checkOutLat,
        Double checkOutLng,
        Long durationSeconds,
        VisitStatus status,
        ComplianceFlag complianceFlag,
        Set<AbnormalReason> abnormalReasons,
        CallReportView report) {

    public record CallReportView(String talkingPoints, String feedback, boolean materialsDistributed) {
    }
}
