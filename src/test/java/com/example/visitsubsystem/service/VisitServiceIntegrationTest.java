package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.AbnormalReason;
import com.example.visitsubsystem.domain.CallReport;
import com.example.visitsubsystem.domain.ComplianceFlag;
import com.example.visitsubsystem.domain.Hcp;
import com.example.visitsubsystem.domain.Hospital;
import com.example.visitsubsystem.domain.Product;
import com.example.visitsubsystem.domain.Visit;
import com.example.visitsubsystem.domain.VisitStatus;
import com.example.visitsubsystem.repository.CallReportRepository;
import com.example.visitsubsystem.repository.HcpRepository;
import com.example.visitsubsystem.repository.HospitalRepository;
import com.example.visitsubsystem.repository.ProductRepository;
import com.example.visitsubsystem.repository.VisitRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:visit-service-test;DB_CLOSE_DELAY=-1")
@Transactional
class VisitServiceIntegrationTest {

    private static final Instant START = Instant.parse("2026-03-05T02:00:00Z");
    private static final double HOSPITAL_LAT = 39.908692;
    private static final double HOSPITAL_LNG = 116.397477;

    @TestConfiguration
    static class MutableClockConfig {

        static final MutableClock CLOCK = new MutableClock(START);

        @Bean
        @Primary
        Clock testClock() {
            return CLOCK;
        }
    }

    @Autowired
    private VisitService visitService;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private CallReportRepository callReportRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HcpRepository hcpRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private EntityManager entityManager;

    private Hcp hcp;
    private Product product;

    @BeforeEach
    void setUp() {
        MutableClockConfig.CLOCK.set(START);
        Hospital hospital = hospitalRepository.save(new Hospital("Test Hospital", HOSPITAL_LAT, HOSPITAL_LNG));
        hcp = hcpRepository.save(new Hcp("Dr. Test", hospital));
        product = productRepository.save(new Product("Test Product"));
    }

    @Test
    void plannedVisitHasPlannedStatus() {
        Visit visit = planVisit();

        assertEquals(VisitStatus.PLANNED, visit.getStatus());
        assertEquals(START, visit.getPlannedAt());
    }

    @Test
    void compliantLifecyclePersistsOkFlagAndReportedStatus() {
        Visit visit = planVisit();

        visitService.checkIn(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);
        assertEquals(VisitStatus.CHECKED_IN, visit.getStatus());
        assertEquals(START, visit.getCheckInAt());

        MutableClockConfig.CLOCK.advance(Duration.ofMinutes(10));
        visitService.checkOut(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);

        Visit reloaded = reload(visit.getId());
        assertEquals(VisitStatus.CHECKED_OUT, reloaded.getStatus());
        assertEquals(600L, reloaded.getDurationSeconds());
        assertEquals(ComplianceFlag.OK, reloaded.getComplianceFlag());
        assertEquals(Set.of(), reloaded.getAbnormalReasons());

        CallReport report = visitService.submitReport(visit.getId(), "Efficacy data", "Very positive", true);

        Visit afterReport = reload(visit.getId());
        assertEquals(VisitStatus.REPORTED, afterReport.getStatus());
        assertTrue(report.isMaterialsDistributed());
        assertEquals(visit.getId(), callReportRepository.findByVisitId(visit.getId()).orElseThrow().getVisit().getId());
    }

    @Test
    void shortVisitIsFlaggedAsAbnormal() {
        Visit visit = planVisit();
        visitService.checkIn(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);
        MutableClockConfig.CLOCK.advance(Duration.ofSeconds(299));
        visitService.checkOut(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);

        Visit reloaded = reload(visit.getId());
        assertEquals(ComplianceFlag.ABNORMAL, reloaded.getComplianceFlag());
        assertEquals(Set.of(AbnormalReason.SHORT_DURATION), reloaded.getAbnormalReasons());
    }

    @Test
    void exactFiveMinuteVisitAtHospitalIsCompliant() {
        Visit visit = planVisit();
        visitService.checkIn(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);
        MutableClockConfig.CLOCK.advance(Duration.ofSeconds(300));
        visitService.checkOut(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);

        Visit reloaded = reload(visit.getId());
        assertEquals(ComplianceFlag.OK, reloaded.getComplianceFlag());
        assertEquals(300L, reloaded.getDurationSeconds());
    }

    @Test
    void farCheckOutLocationIsFlaggedAsAbnormal() {
        Visit visit = planVisit();
        visitService.checkIn(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);
        MutableClockConfig.CLOCK.advance(Duration.ofMinutes(10));
        visitService.checkOut(visit.getId(), HOSPITAL_LAT + 0.01, HOSPITAL_LNG);

        Visit reloaded = reload(visit.getId());
        assertEquals(ComplianceFlag.ABNORMAL, reloaded.getComplianceFlag());
        assertEquals(Set.of(AbnormalReason.FAR_FROM_HOSPITAL), reloaded.getAbnormalReasons());
    }

    @Test
    void checkOutBeforeCheckInIsRejected() {
        Visit visit = planVisit();

        assertThrows(InvalidVisitTransitionException.class,
                () -> visitService.checkOut(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG));
    }

    @Test
    void reportBeforeCheckOutIsRejected() {
        Visit visit = planVisit();
        visitService.checkIn(visit.getId(), HOSPITAL_LAT, HOSPITAL_LNG);

        assertThrows(InvalidVisitTransitionException.class,
                () -> visitService.submitReport(visit.getId(), "points", "feedback", false));
    }

    @Test
    void unknownVisitIsRejected() {
        assertThrows(VisitNotFoundException.class,
                () -> visitService.checkIn(9999L, HOSPITAL_LAT, HOSPITAL_LNG));
    }

    private Visit planVisit() {
        return visitService.planVisit(hcp.getId(), product.getId(), "Cardiology", START);
    }

    private Visit reload(Long visitId) {
        entityManager.flush();
        entityManager.clear();
        return visitRepository.findById(visitId).orElseThrow();
    }

    static final class MutableClock extends Clock {

        private Instant current;

        MutableClock(Instant start) {
            this.current = start;
        }

        void set(Instant instant) {
            this.current = instant;
        }

        void advance(Duration duration) {
            this.current = this.current.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException("withZone is not supported by MutableClock");
        }

        @Override
        public Instant instant() {
            return current;
        }
    }
}
