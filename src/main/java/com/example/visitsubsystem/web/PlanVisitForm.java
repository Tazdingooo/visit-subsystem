package com.example.visitsubsystem.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form-backing object for planning a visit. {@code plannedAtLocal} binds to the
 * value of an {@code <input type="datetime-local">} and is converted to an
 * {@link java.time.Instant} relative to the server's default zone.
 */
public class PlanVisitForm {

    @NotNull(message = "Choose an HCP")
    private Long hcpId;

    @NotNull(message = "Choose a product")
    private Long productId;

    @NotBlank(message = "Department is required")
    @Size(max = 200, message = "Department must be at most 200 characters")
    private String departmentName;

    @NotBlank(message = "Planned time is required")
    private String plannedAtLocal;

    public Long getHcpId() {
        return hcpId;
    }

    public void setHcpId(Long hcpId) {
        this.hcpId = hcpId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getPlannedAtLocal() {
        return plannedAtLocal;
    }

    public void setPlannedAtLocal(String plannedAtLocal) {
        this.plannedAtLocal = plannedAtLocal;
    }
}
