package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.CallReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CallReportRepository extends JpaRepository<CallReport, Long> {

    boolean existsByVisitId(Long visitId);

    Optional<CallReport> findByVisitId(Long visitId);
}
