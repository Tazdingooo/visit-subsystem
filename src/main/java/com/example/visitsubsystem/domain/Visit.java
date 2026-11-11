package com.example.visitsubsystem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "visit")
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hcp_id", nullable = false)
    private Hcp hcp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "department_name", nullable = false)
    private String departmentName;

    @Column(name = "planned_at", nullable = false)
    private Instant plannedAt;

    @Column(name = "check_in_at")
    private Instant checkInAt;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lng")
    private Double checkInLng;

    @Column(name = "check_out_at")
    private Instant checkOutAt;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VisitStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_flag", length = 20)
    private ComplianceFlag complianceFlag;

    @Convert(converter = AbnormalReasonSetConverter.class)
    @Column(name = "abnormal_reasons", length = 200)
    private Set<AbnormalReason> abnormalReasons = EnumSet.noneOf(AbnormalReason.class);

    public Visit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Hcp getHcp() {
        return hcp;
    }

    public void setHcp(Hcp hcp) {
        this.hcp = hcp;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Instant getPlannedAt() {
        return plannedAt;
    }

    public void setPlannedAt(Instant plannedAt) {
        this.plannedAt = plannedAt;
    }

    public Instant getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(Instant checkInAt) {
        this.checkInAt = checkInAt;
    }

    public Double getCheckInLat() {
        return checkInLat;
    }

    public void setCheckInLat(Double checkInLat) {
        this.checkInLat = checkInLat;
    }

    public Double getCheckInLng() {
        return checkInLng;
    }

    public void setCheckInLng(Double checkInLng) {
        this.checkInLng = checkInLng;
    }

    public Instant getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(Instant checkOutAt) {
        this.checkOutAt = checkOutAt;
    }

    public Double getCheckOutLat() {
        return checkOutLat;
    }

    public void setCheckOutLat(Double checkOutLat) {
        this.checkOutLat = checkOutLat;
    }

    public Double getCheckOutLng() {
        return checkOutLng;
    }

    public void setCheckOutLng(Double checkOutLng) {
        this.checkOutLng = checkOutLng;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public VisitStatus getStatus() {
        return status;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }

    public ComplianceFlag getComplianceFlag() {
        return complianceFlag;
    }

    public void setComplianceFlag(ComplianceFlag complianceFlag) {
        this.complianceFlag = complianceFlag;
    }

    public Set<AbnormalReason> getAbnormalReasons() {
        return abnormalReasons;
    }

    public void setAbnormalReasons(Set<AbnormalReason> abnormalReasons) {
        this.abnormalReasons = abnormalReasons;
    }
}
