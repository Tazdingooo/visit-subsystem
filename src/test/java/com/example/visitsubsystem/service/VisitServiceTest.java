package com.example.visitsubsystem.service;

import com.example.visitsubsystem.config.ComplianceProperties;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    private static final Instant CHECK_IN_TIME = Instant.parse("2026-01-10T09:00:00Z");
    private static final Instant CHECK_OUT_TIME = Instant.parse("2026-01-10T09:05:00Z");

    @Mock
    private VisitRepository visitRepository;
    @Mock
    private HcpRepository hcpRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CallReportRepository callReportRepository;

    private VisitService service;
    private Visit visit;

    @BeforeEach
    void setUp() {
        service = new VisitService(
                visitRepository,
                hcpRepository,
                productRepository,
                callReportRepository,
                new ComplianceEvaluator(new ComplianceProperties(300, 500.0)),
                Clock.fixed(CHECK_OUT_TIME, ZoneOffset.UTC));

        Hospital hospital = new Hospital("Test Hospital", 0.0, 0.0);
        Hcp hcp = new Hcp("Dr. Test", hospital);
        Product product = new Product("Test Product");
        visit = new Visit();
        visit.setId(7L);
        visit.setHcp(hcp);
        visit.setProduct(product);
        visit.setDepartmentName("Cardiology");
        visit.setPlannedAt(CHECK_IN_TIME.minus(Duration.ofDays(1)));
        visit.setStatus(VisitStatus.PLANNED);
    }

    @Test
    void checkInUsesServerClockAndMovesPlannedVisitToCheckedIn() {
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));

        Visit result = service.checkIn(7L, 0.0, 0.0);

        assertEquals(CHECK_OUT_TIME, result.getCheckInAt());
        assertEquals(0.0, result.getCheckInLat());
        assertEquals(0.0, result.getCheckInLng());
        assertEquals(VisitStatus.CHECKED_IN, result.getStatus());
    }

    @Test
    void checkOutCalculatesDurationAndPersistsCompliance() {
        visit.setStatus(VisitStatus.CHECKED_IN);
        visit.setCheckInAt(CHECK_IN_TIME);
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));

        Visit result = service.checkOut(7L, 0.0, 0.0);

        assertEquals(CHECK_OUT_TIME, result.getCheckOutAt());
        assertEquals(300L, result.getDurationSeconds());
        assertEquals(VisitStatus.CHECKED_OUT, result.getStatus());
        assertEquals("OK", result.getComplianceFlag().name());
        verify(visitRepository, never()).save(any(Visit.class));
    }

    @Test
    void reportIsOnlyAllowedAfterCheckOutAndMovesVisitToReported() {
        visit.setStatus(VisitStatus.CHECKED_OUT);
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));
        when(callReportRepository.existsByVisitId(7L)).thenReturn(false);
        when(callReportRepository.save(any(CallReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CallReport result = service.submitReport(7L, "Efficacy", "Positive", true);

        assertEquals(visit, result.getVisit());
        assertEquals("Efficacy", result.getTalkingPoints());
        assertEquals("Positive", result.getFeedback());
        assertEquals(true, result.isMaterialsDistributed());
        assertEquals(VisitStatus.REPORTED, visit.getStatus());
    }

    @Test
    void invalidTransitionIsRejected() {
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));

        assertThrows(InvalidVisitTransitionException.class, () -> service.checkOut(7L, 0.0, 0.0));
    }

    @Test
    void checkOutBeforeCheckInTimestampFailsInsteadOfReportingShortDuration() {
        visit.setStatus(VisitStatus.CHECKED_IN);
        // Clock returns CHECK_OUT_TIME (09:05) but check-in was 09:10: timestamps are broken.
        visit.setCheckInAt(CHECK_OUT_TIME.plus(Duration.ofMinutes(5)));
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));

        assertThrows(InconsistentVisitTimestampsException.class, () -> service.checkOut(7L, 0.0, 0.0));
        assertEquals(VisitStatus.CHECKED_IN, visit.getStatus());
        assertEquals(null, visit.getDurationSeconds());
        assertEquals(null, visit.getComplianceFlag());
    }

    @Test
    void checkInRejectsNaNGpsCoordinates() {
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalArgumentException.class, () -> service.checkIn(7L, Double.NaN, 0.0));
        assertEquals(VisitStatus.PLANNED, visit.getStatus());
        assertEquals(null, visit.getCheckInAt());
    }

    @Test
    void checkOutRejectsNaNGpsCoordinates() {
        visit.setStatus(VisitStatus.CHECKED_IN);
        visit.setCheckInAt(CHECK_IN_TIME);
        when(visitRepository.findById(7L)).thenReturn(Optional.of(visit));

        assertThrows(IllegalArgumentException.class, () -> service.checkOut(7L, 0.0, Double.NaN));
        assertEquals(VisitStatus.CHECKED_IN, visit.getStatus());
        assertEquals(null, visit.getComplianceFlag());
    }
}
