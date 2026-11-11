package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long> {
}
