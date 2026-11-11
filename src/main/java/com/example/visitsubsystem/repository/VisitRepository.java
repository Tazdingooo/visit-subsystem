package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {
}
