package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.CallReport;
import com.example.visitsubsystem.domain.Hcp;
import com.example.visitsubsystem.domain.Hospital;
import com.example.visitsubsystem.domain.Product;
import com.example.visitsubsystem.domain.Visit;
import com.example.visitsubsystem.domain.VisitStatus;
import com.example.visitsubsystem.repository.CallReportRepository;
import com.example.visitsubsystem.repository.HcpRepository;
import com.example.visitsubsystem.repository.ProductRepository;
import com.example.visitsubsystem.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@Transactional
public class VisitService {

    private final VisitRepository visitRepository;
    private final HcpRepository hcpRepository;
    private final ProductRepository productRepository;
    private final CallReportRepository callReportRepository;
    private final ComplianceEvaluator complianceEvaluator;
    private final Clock clock;

    public VisitService(VisitRepository visitRepository,
                        HcpRepository hcpRepository,
                        ProductRepository productRepository,
                        CallReportRepository callReportRepository,
                        ComplianceEvaluator complianceEvaluator,
                        Clock clock) {
        this.visitRepository = visitRepository;
        this.hcpRepository = hcpRepository;
        this.productRepository = productRepository;
        this.callReportRepository = callReportRepository;
        this.complianceEvaluator = complianceEvaluator;
        this.clock = clock;
    }

    public Visit planVisit(long hcpId, long productId, String departmentName, Instant plannedAt) {
        Hcp hcp = hcpRepository.findById(hcpId)
                .orElseThrow(() -> new VisitNotFoundException("No HCP with id " + hcpId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new VisitNotFoundException("No product with id " + productId));
        if (departmentName == null || departmentName.isBlank()) {
            throw new IllegalArgumentException("departmentName must not be blank");
        }
        if (plannedAt == null) {
            throw new IllegalArgumentException("plannedAt must not be null");
        }

        Visit visit = new Visit();
        visit.setHcp(hcp);
        visit.setProduct(product);
        visit.setDepartmentName(departmentName.trim());
        visit.setPlannedAt(plannedAt);
        visit.setStatus(VisitStatus.PLANNED);
        return visitRepository.save(visit);
    }

    public Visit checkIn(long visitId, double lat, double lng) {
        Visit visit = requireVisit(visitId);
        requireStatus(visit, VisitStatus.PLANNED, "check in");
        requireValidCoordinates(lat, lng);

        visit.setCheckInAt(clock.instant());
        visit.setCheckInLat(lat);
        visit.setCheckInLng(lng);
        visit.setStatus(VisitStatus.CHECKED_IN);
        return visit;
    }

    public Visit checkOut(long visitId, double lat, double lng) {
        Visit visit = requireVisit(visitId);
        requireStatus(visit, VisitStatus.CHECKED_IN, "check out");
        requireValidCoordinates(lat, lng);

        Instant checkOutAt = clock.instant();
        Duration elapsed = Duration.between(visit.getCheckInAt(), checkOutAt);
        if (elapsed.isNegative()) {
            throw new InconsistentVisitTimestampsException(
                    "Visit " + visit.getId() + " has check-out time " + checkOutAt
                            + " before check-in time " + visit.getCheckInAt());
        }
        long durationSeconds = elapsed.toSeconds();
        Hospital hospital = visit.getHcp().getHospital();
        ComplianceResult compliance = complianceEvaluator.evaluate(
                durationSeconds, hospital.getLat(), hospital.getLng(), lat, lng);

        visit.setCheckOutAt(checkOutAt);
        visit.setCheckOutLat(lat);
        visit.setCheckOutLng(lng);
        visit.setDurationSeconds(durationSeconds);
        visit.setComplianceFlag(compliance.flag());
        visit.setAbnormalReasons(compliance.reasons());
        visit.setStatus(VisitStatus.CHECKED_OUT);
        return visit;
    }

    public CallReport submitReport(long visitId, String talkingPoints, String feedback, boolean materialsDistributed) {
        Visit visit = requireVisit(visitId);
        requireStatus(visit, VisitStatus.CHECKED_OUT, "submit a call report");
        if (callReportRepository.existsByVisitId(visitId)) {
            throw new InvalidVisitTransitionException("Visit " + visitId + " already has a call report");
        }

        CallReport report = new CallReport();
        report.setVisit(visit);
        report.setTalkingPoints(talkingPoints);
        report.setFeedback(feedback);
        report.setMaterialsDistributed(materialsDistributed);
        callReportRepository.save(report);

        visit.setStatus(VisitStatus.REPORTED);
        return report;
    }

    @Transactional(readOnly = true)
    public Visit getVisit(long visitId) {
        return requireVisit(visitId);
    }

    private Visit requireVisit(long visitId) {
        return visitRepository.findById(visitId)
                .orElseThrow(() -> new VisitNotFoundException("No visit with id " + visitId));
    }

    private static void requireStatus(Visit visit, VisitStatus expected, String action) {
        if (visit.getStatus() != expected) {
            throw new InvalidVisitTransitionException(
                    "Cannot " + action + " for visit " + visit.getId()
                            + " while status is " + visit.getStatus() + " (expected " + expected + ")");
        }
    }

    private static void requireValidCoordinates(double lat, double lng) {
        if (!Double.isFinite(lat) || !Double.isFinite(lng)
                || lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("Invalid GPS coordinates: lat=" + lat + ", lng=" + lng);
        }
    }
}
