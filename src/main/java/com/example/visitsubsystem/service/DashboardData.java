package com.example.visitsubsystem.service;

import com.example.visitsubsystem.domain.VisitStatus;

import java.util.List;

public record DashboardData(
        int year,
        int month,
        VisitStatus statusFilter,
        long totalVisits,
        List<ProductCount> productCounts,
        List<StatusCount> statusCounts) {

    public record ProductCount(String productName, long visits) {
    }

    public record StatusCount(VisitStatus status, long visits) {
    }
}
