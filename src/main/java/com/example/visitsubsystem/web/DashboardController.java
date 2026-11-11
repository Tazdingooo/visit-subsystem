package com.example.visitsubsystem.web;

import com.example.visitsubsystem.domain.VisitStatus;
import com.example.visitsubsystem.service.DashboardData;
import com.example.visitsubsystem.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.time.ZoneId;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final ZoneId zone;

    public DashboardController(DashboardService dashboardService, ZoneId zone) {
        this.dashboardService = dashboardService;
        this.zone = zone;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            @RequestParam(required = false) VisitStatus status,
                            Model model) {
        // Default to "this month" in the configured app zone (UTC+8), not the
        // JVM default (UTC inside the container), so the default view agrees
        // with how DashboardService buckets months.
        YearMonth now = YearMonth.now(zone);
        int y = year == null ? now.getYear() : year;
        int m = (month == null || month < 1 || month > 12) ? now.getMonthValue() : month;

        DashboardData data = dashboardService.monthly(y, m, status);
        YearMonth current = YearMonth.of(y, m);

        model.addAttribute("data", data);
        model.addAttribute("statuses", VisitStatus.values());
        model.addAttribute("prev", current.minusMonths(1));
        model.addAttribute("next", current.plusMonths(1));
        model.addAttribute("current", current);
        return "dashboard";
    }
}
