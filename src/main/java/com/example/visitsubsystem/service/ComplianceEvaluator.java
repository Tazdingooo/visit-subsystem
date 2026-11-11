package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.ComplianceFlag;
import com.example.visitsubsystem.config.ComplianceProperties;
import com.example.visitsubsystem.geo.Haversine;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class ComplianceEvaluator {

    private final ComplianceProperties properties;

    public ComplianceEvaluator(ComplianceProperties properties) {
        this.properties = properties;
    }

    public ComplianceResult evaluate(long durationSeconds,
                                    double hospitalLat,
                                    double hospitalLng,
                                    double checkOutLat,
                                    double checkOutLng) {
        if (durationSeconds < 0) {
            throw new IllegalArgumentException("durationSeconds must not be negative");
        }
        EnumSet<AbnormalReason> reasons = EnumSet.noneOf(AbnormalReason.class);
        if (durationSeconds < properties.minDurationSeconds()) {
            reasons.add(AbnormalReason.SHORT_DURATION);
        }
        if (Haversine.distanceMeters(hospitalLat, hospitalLng, checkOutLat, checkOutLng)
                > properties.maxHospitalDistanceMeters()) {
            reasons.add(AbnormalReason.FAR_FROM_HOSPITAL);
        }
        return new ComplianceResult(reasons.isEmpty() ? ComplianceFlag.OK : ComplianceFlag.ABNORMAL, reasons);
    }
}
