package com.example.visitsubsystem.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form-backing object for planning a visit. The planned time is captured as a
 * date plus separately selectable hour and minute, which pick more cleanly than
 * a {@code datetime-local} control (especially on Safari/Chrome). The pieces are
 * combined into an {@link java.time.Instant} relative to the configured zone in
 * {@link VisitWebController}.
 */
public class PlanVisitForm {

    @NotNull(message = "Choose an HCP")
    private Long hcpId;

    @NotNull(message = "Choose a product")
    private Long productId;

    @NotBlank(message = "Department is required")
    @Size(max = 200, message = "Department must be at most 200 characters")
    private String departmentName;

    @NotBlank(message = "Planned date is required")
    private String plannedDate;

    @NotNull(message = "Select an hour")
    @Min(value = 0, message = "Hour must be between 0 and 23")
    @Max(value = 23, message = "Hour must be between 0 and 23")
    private Integer plannedHour;

    @NotNull(message = "Select a minute")
    @Min(value = 0, message = "Minute must be between 0 and 59")
    @Max(value = 59, message = "Minute must be between 0 and 59")
    private Integer plannedMinute;

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

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }

    public Integer getPlannedHour() {
        return plannedHour;
    }

    public void setPlannedHour(Integer plannedHour) {
        this.plannedHour = plannedHour;
    }

    public Integer getPlannedMinute() {
        return plannedMinute;
    }

    public void setPlannedMinute(Integer plannedMinute) {
        this.plannedMinute = plannedMinute;
    }
}
