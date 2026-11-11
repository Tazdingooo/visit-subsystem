package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.VisitStatus;
import com.example.visitsubsystem.repository.ProductCountRow;
import com.example.visitsubsystem.repository.VisitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final VisitRepository visitRepository;

    public DashboardService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public DashboardData monthly(int year, int month, VisitStatus statusFilter) {
        YearMonth yearMonth = YearMonth.of(year, month);
        ZoneId zone = ZoneId.systemDefault();
        Instant from = yearMonth.atDay(1).atStartOfDay(zone).toInstant();
        Instant to = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

        List<ProductCountRow> productRows = statusFilter == null
                ? visitRepository.countByProduct(from, to)
                : visitRepository.countByProductAndStatus(from, to, statusFilter);

        List<DashboardData.ProductCount> productCounts = productRows.stream()
                .map(r -> new DashboardData.ProductCount(r.getProductName(), r.getVisits()))
                .toList();

        List<DashboardData.StatusCount> statusCounts = visitRepository.countByStatus(from, to).stream()
                .map(r -> new DashboardData.StatusCount(r.getStatus(), r.getVisits()))
                .sorted(Comparator.comparingInt(c -> c.status().ordinal()))
                .toList();

        long total = productCounts.stream().mapToLong(DashboardData.ProductCount::visits).sum();
        return new DashboardData(year, month, statusFilter, total, productCounts, statusCounts);
    }
}
