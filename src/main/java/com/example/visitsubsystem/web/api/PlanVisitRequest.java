package com.example.visitsubsystem.web.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record PlanVisitRequest(
        @NotNull Long hcpId,
        @NotNull Long productId,
        @NotBlank @Size(max = 200) String departmentName,
        @NotNull Instant plannedAt) {
}
