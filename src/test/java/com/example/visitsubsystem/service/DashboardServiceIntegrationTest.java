package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.Hcp;
import com.example.visitsubsystem.domain.Hospital;
import com.example.visitsubsystem.domain.Product;
import com.example.visitsubsystem.domain.Visit;
import com.example.visitsubsystem.domain.VisitStatus;
import com.example.visitsubsystem.repository.HcpRepository;
import com.example.visitsubsystem.repository.HospitalRepository;
import com.example.visitsubsystem.repository.ProductRepository;
import com.example.visitsubsystem.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:dashboard-test;DB_CLOSE_DELAY=-1")
@Transactional
class DashboardServiceIntegrationTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private VisitRepository visitRepository;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HcpRepository hcpRepository;
    @Autowired
    private ProductRepository productRepository;

    private Hcp hcp;
    private Product alpha;
    private Product beta;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(new Hospital("Dashboard Hospital", 39.9, 116.4));
        hcp = hcpRepository.save(new Hcp("Dr. Dashboard", hospital));
        alpha = productRepository.save(new Product("Alpha"));
        beta = productRepository.save(new Product("Beta"));

        // Bucketing is by the server's local zone, so express fixtures in local
        // wall-clock terms to make boundaries unambiguous.
        saveVisit(alpha, LocalDateTime.of(2026, 5, 1, 0, 0), VisitStatus.PLANNED);
        saveVisit(alpha, LocalDateTime.of(2026, 5, 15, 12, 0), VisitStatus.CHECKED_OUT);
        saveVisit(beta, LocalDateTime.of(2026, 5, 31, 23, 59, 59), VisitStatus.REPORTED);
        saveVisit(beta, LocalDateTime.of(2026, 6, 1, 0, 0), VisitStatus.PLANNED);
    }

    @Test
    void countsAllVisitsByProductForTheSelectedPlannedMonth() {
        DashboardData data = dashboardService.monthly(2026, 5, null);

        assertEquals(3, data.totalVisits());
        assertEquals(2, data.productCounts().get(0).visits());
        assertEquals("Alpha", data.productCounts().get(0).productName());
        assertEquals(1, data.productCounts().get(1).visits());
        assertEquals("Beta", data.productCounts().get(1).productName());
    }

    @Test
    void statusFilterChangesProductCountsButStatusBreakdownRemainsFullMonth() {
        DashboardData data = dashboardService.monthly(2026, 5, VisitStatus.REPORTED);

        assertEquals(1, data.totalVisits());
        assertEquals("Beta", data.productCounts().getFirst().productName());
        assertEquals(1, data.productCounts().getFirst().visits());
        assertEquals(3, data.statusCounts().stream().mapToLong(DashboardData.StatusCount::visits).sum());
    }

    @Test
    void monthBoundariesUseStartInclusiveAndNextMonthExclusive() {
        DashboardData may = dashboardService.monthly(2026, 5, null);
        DashboardData june = dashboardService.monthly(2026, 6, null);

        assertEquals(3, may.totalVisits());
        assertEquals(1, june.totalVisits());
    }

    private void saveVisit(Product product, LocalDateTime plannedAtLocal, VisitStatus status) {
        Visit visit = new Visit();
        visit.setHcp(hcp);
        visit.setProduct(product);
        visit.setDepartmentName("General");
        visit.setPlannedAt(plannedAtLocal.atZone(ZONE).toInstant());
        visit.setStatus(status);
        visitRepository.save(visit);
    }
}
