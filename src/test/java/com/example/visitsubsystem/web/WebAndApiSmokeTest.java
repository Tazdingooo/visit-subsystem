package com.example.visitsubsystem.web;

import com.example.visitsubsystem.domain.Hcp;
import com.example.visitsubsystem.domain.Hospital;
import com.example.visitsubsystem.domain.Product;
import com.example.visitsubsystem.repository.HcpRepository;
import com.example.visitsubsystem.repository.HospitalRepository;
import com.example.visitsubsystem.repository.ProductRepository;
import com.example.visitsubsystem.service.VisitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:web-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Transactional
class WebAndApiSmokeTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private VisitService visitService;
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private HcpRepository hcpRepository;
    @Autowired
    private ProductRepository productRepository;

    private Hcp hcp;
    private Product product;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(new Hospital("Test Hospital", 39.9, 116.4));
        hcp = hcpRepository.save(new Hcp("Dr. Test", hospital));
        product = productRepository.save(new Product("Test Product"));
    }

    @Test
    void homeShowsDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Monthly dashboard")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/vendor/chart.umd.js")));
    }

    @Test
    void dashboardAcceptsMonthAndStatusFilters() throws Exception {
        visitService.planVisit(hcp.getId(), product.getId(), "Cardiology",
                java.time.Instant.parse("2026-07-15T09:00:00Z"));

        mockMvc.perform(get("/").param("year", "2026").param("month", "7"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Test Product")));

        mockMvc.perform(get("/").param("year", "2026").param("month", "7").param("status", "REPORTED"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No planned visits match")));
    }

    @Test
    void dashboardMonthNavigationLinksUseNumericMonth() throws Exception {
        // January 2026: previous link must be 2025-12, next must be 2026-02,
        // never Month enum names like "DECEMBER"/"FEBRUARY".
        mockMvc.perform(get("/").param("year", "2026").param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("year=2025")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("month=12")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("month=2")))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("DECEMBER"))))
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("FEBRUARY"))));
    }

    @Test
    void visitsListPageRenders() throws Exception {
        mockMvc.perform(get("/visits"))
                .andExpect(status().isOk())
                .andExpect(view().name("visits/list"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Visits")));
    }

    @Test
    void planFormRendersWithCatalog() throws Exception {
        mockMvc.perform(get("/visits/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("visits/new"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Dr. Test")));
    }

    @Test
    void detailPageRendersForPlannedVisit() throws Exception {
        var visit = visitService.planVisit(hcp.getId(), product.getId(), "Cardiology", Instant.parse("2026-12-01T09:00:00Z"));

        mockMvc.perform(get("/visits/" + visit.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("visits/detail"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Check in")));
    }

    @Test
    void planningVisitThroughTheFormRedirectsToDetail() throws Exception {
        mockMvc.perform(post("/visits")
                        .param("hcpId", String.valueOf(hcp.getId()))
                        .param("productId", String.valueOf(product.getId()))
                        .param("departmentName", "Neurology")
                        .param("plannedDate", "2026-12-02")
                        .param("plannedHour", "10")
                        .param("plannedMinute", "30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.startsWith("/visits/")));
    }

    @Test
    void plannedTimeIsComposedFromDateHourMinuteInAppZone() throws Exception {
        // Asia/Shanghai is UTC+8, so 2026-12-02 10:30 local == 02:30Z.
        mockMvc.perform(post("/visits")
                        .param("hcpId", String.valueOf(hcp.getId()))
                        .param("productId", String.valueOf(product.getId()))
                        .param("departmentName", "Neurology")
                        .param("plannedDate", "2026-12-02")
                        .param("plannedHour", "10")
                        .param("plannedMinute", "30"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/visits"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-12-02 10:30")));
    }

    @Test
    void planFormRendersHourAndMinuteSelects() throws Exception {
        mockMvc.perform(get("/visits/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("type=\"date\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<select id=\"plannedHour\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<select id=\"plannedMinute\"")));
    }

    @Test
    void formRejectsInvalidHour() throws Exception {
        mockMvc.perform(post("/visits")
                        .param("hcpId", String.valueOf(hcp.getId()))
                        .param("productId", String.valueOf(product.getId()))
                        .param("departmentName", "Neurology")
                        .param("plannedDate", "2026-12-02")
                        .param("plannedHour", "24")
                        .param("plannedMinute", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("visits/new"));
    }

    @Test
    void formRejectsMissingFields() throws Exception {
        mockMvc.perform(post("/visits")
                        .param("departmentName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("visits/new"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Department is required")));
    }

    @Test
    void apiListsSeededCatalog() throws Exception {
        mockMvc.perform(get("/api/hcps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Dr. Test')]").exists());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Test Product')]").exists());
    }

    @Test
    void apiPlansACheckInAndChecksOut() throws Exception {
        String createBody = """
                {"hcpId": %d, "productId": %d, "departmentName": "Cardiology",
                 "plannedAt": "2026-12-03T09:00:00Z"}
                """.formatted(hcp.getId(), product.getId());

        String location = mockMvc.perform(post("/api/visits")
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"id\":(\\d+).*", "$1");

        mockMvc.perform(post("/api/visits/" + location + "/check-in")
                        .contentType("application/json")
                        .content("{\"lat\": 39.9, \"lng\": 116.4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));

        mockMvc.perform(post("/api/visits/" + location + "/check-out")
                        .contentType("application/json")
                        .content("{\"lat\": 39.9, \"lng\": 116.4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"))
                .andExpect(jsonPath("$.complianceFlag").value("ABNORMAL"))
                .andExpect(jsonPath("$.abnormalReasons[0]").value("SHORT_DURATION"));
    }

    @Test
    void apiReturns404ForUnknownVisit() throws Exception {
        mockMvc.perform(get("/api/visits/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void apiRejectsInvalidTransitionWith409() throws Exception {
        var visit = visitService.planVisit(hcp.getId(), product.getId(), "Cardiology", Instant.parse("2026-12-04T09:00:00Z"));

        mockMvc.perform(post("/api/visits/" + visit.getId() + "/check-out")
                        .contentType("application/json")
                        .content("{\"lat\": 39.9, \"lng\": 116.4}"))
                .andExpect(status().isConflict());
    }

    @Test
    void apiRejectsMalformedBodyWith400() throws Exception {
        mockMvc.perform(post("/api/visits")
                        .contentType("application/json")
                        .content("{\"departmentName\": \"\"}"))
                .andExpect(status().isBadRequest());
    }
}
