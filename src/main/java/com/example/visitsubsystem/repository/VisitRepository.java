package com.example.visitsubsystem.repository;

import com.example.visitsubsystem.domain.Visit;
import com.example.visitsubsystem.domain.VisitStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @EntityGraph(attributePaths = {"hcp", "hcp.hospital", "product"})
    List<Visit> findAllByOrderByPlannedAtDesc();

    @Query("""
            select v.product.name as productName, count(v) as visits
            from Visit v
            where v.plannedAt >= :from and v.plannedAt < :to
            group by v.product.name
            order by count(v) desc, v.product.name asc
            """)
    List<ProductCountRow> countByProduct(@Param("from") Instant from,
                                         @Param("to") Instant to);

    @Query("""
            select v.product.name as productName, count(v) as visits
            from Visit v
            where v.plannedAt >= :from and v.plannedAt < :to
              and v.status = :status
            group by v.product.name
            order by count(v) desc, v.product.name asc
            """)
    List<ProductCountRow> countByProductAndStatus(@Param("from") Instant from,
                                                  @Param("to") Instant to,
                                                  @Param("status") VisitStatus status);

    @Query("""
            select v.status as status, count(v) as visits
            from Visit v
            where v.plannedAt >= :from and v.plannedAt < :to
            group by v.status
            """)
    List<StatusCountRow> countByStatus(@Param("from") Instant from,
                                       @Param("to") Instant to);
}
