package com.example.visitsubsystem.web.api;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.ComplianceFlag;
import com.example.visitsubsystem.domain.VisitStatus;
import com.example.visitsubsystem.service.VisitDetail;
import com.example.visitsubsystem.service.VisitSummary;

import java.time.Instant;
import java.util.List;

public record VisitResponse(
        long id,
        String hcpName,
        String hospitalName,
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
        List<AbnormalReason> abnormalReasons,
        CallReportResponse report) {

    public record CallReportResponse(String talkingPoints, String feedback, boolean materialsDistributed) {
    }

    public static VisitResponse from(VisitDetail v) {
        return new VisitResponse(
                v.id(), v.hcpName(), v.hospitalName(), v.productName(), v.departmentName(),
                v.plannedAt(), v.checkInAt(), v.checkInLat(), v.checkInLng(),
                v.checkOutAt(), v.checkOutLat(), v.checkOutLng(), v.durationSeconds(),
                v.status(), v.complianceFlag(),
                v.abnormalReasons().stream().sorted().toList(),
                v.report() == null ? null
                        : new CallReportResponse(v.report().talkingPoints(), v.report().feedback(),
                                v.report().materialsDistributed()));
    }

    public static VisitResponse from(VisitSummary s) {
        return new VisitResponse(
                s.id(), s.hcpName(), s.hospitalName(), s.productName(), s.departmentName(),
                s.plannedAt(), null, null, null, null, null, null, null,
                s.status(), s.complianceFlag(), List.of(), null);
    }
}
