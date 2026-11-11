package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.Hcp;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HcpRepository extends JpaRepository<Hcp, Long> {

    @EntityGraph(attributePaths = "hospital")
    List<Hcp> findAllByOrderByNameAsc();
}
