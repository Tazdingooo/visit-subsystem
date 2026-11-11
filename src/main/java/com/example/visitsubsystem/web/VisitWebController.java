package com.example.visitsubsystem.web;

import com.example.visitsubsystem.domain.Visit;
import com.example.visitsubsystem.repository.HcpRepository;
import com.example.visitsubsystem.repository.ProductRepository;
import com.example.visitsubsystem.service.InconsistentVisitTimestampsException;
import com.example.visitsubsystem.service.InvalidVisitTransitionException;
import com.example.visitsubsystem.service.VisitNotFoundException;
import com.example.visitsubsystem.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.function.Supplier;

@Controller
public class VisitWebController {

    private final VisitService visitService;
    private final HcpRepository hcpRepository;
    private final ProductRepository productRepository;
    private final ZoneId zone;

    public VisitWebController(VisitService visitService,
                              HcpRepository hcpRepository,
                              ProductRepository productRepository,
                              ZoneId zone) {
        this.visitService = visitService;
        this.hcpRepository = hcpRepository;
        this.productRepository = productRepository;
        this.zone = zone;
    }

    @GetMapping("/visits")
    public String list(Model model) {
        model.addAttribute("visits", visitService.listVisits());
        return "visits/list";
    }

    @GetMapping("/visits/new")
    public String newVisitForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new PlanVisitForm());
        }
        addPlanFormOptions(model);
        return "visits/new";
    }

    @PostMapping("/visits")
    public String createVisit(@Valid @ModelAttribute("form") PlanVisitForm form,
                              BindingResult binding,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Instant plannedAt = null;
        if (form.getPlannedAtLocal() != null && !form.getPlannedAtLocal().isBlank()) {
            try {
                plannedAt = LocalDateTime.parse(form.getPlannedAtLocal())
                        .atZone(zone)
                        .toInstant();
            } catch (DateTimeParseException ex) {
                binding.rejectValue("plannedAtLocal", "invalid", "Enter a valid date and time");
            }
        }
        if (binding.hasErrors()) {
            addPlanFormOptions(model);
            return "visits/new";
        }

        Visit visit = visitService.planVisit(form.getHcpId(), form.getProductId(),
                form.getDepartmentName(), plannedAt);
        redirectAttributes.addFlashAttribute("message", "Visit planned.");
        return "redirect:/visits/" + visit.getId();
    }

    @GetMapping("/visits/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("v", visitService.getVisitDetail(id));
        if (!model.containsAttribute("gpsForm")) {
            model.addAttribute("gpsForm", new GpsForm());
        }
        if (!model.containsAttribute("reportForm")) {
            model.addAttribute("reportForm", new ReportForm());
        }
        return "visits/detail";
    }

    @PostMapping("/visits/{id}/check-in")
    public String checkIn(@PathVariable long id,
                          @ModelAttribute GpsForm gpsForm,
                          RedirectAttributes redirectAttributes) {
        return runAction(redirectAttributes, id, "Checked in.",
                () -> visitService.checkIn(id, gpsLat(gpsForm), gpsLng(gpsForm)));
    }

    @PostMapping("/visits/{id}/check-out")
    public String checkOut(@PathVariable long id,
                           @ModelAttribute GpsForm gpsForm,
                           RedirectAttributes redirectAttributes) {
        return runAction(redirectAttributes, id, "Checked out and compliance evaluated.",
                () -> visitService.checkOut(id, gpsLat(gpsForm), gpsLng(gpsForm)));
    }

    @PostMapping("/visits/{id}/report")
    public String submitReport(@PathVariable long id,
                               @ModelAttribute ReportForm reportForm,
                               RedirectAttributes redirectAttributes) {
        return runAction(redirectAttributes, id, "Call report saved.",
                () -> visitService.submitReport(id, reportForm.getTalkingPoints(),
                        reportForm.getFeedback(), reportForm.isMaterialsDistributed()));
    }

    private String runAction(RedirectAttributes redirectAttributes, long id, String successMessage,
                             Supplier<?> action) {
        try {
            action.get();
        } catch (VisitNotFoundException ex) {
            return "redirect:/visits";
        } catch (InvalidVisitTransitionException | IllegalArgumentException
                 | InconsistentVisitTimestampsException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/visits/" + id;
        }
        redirectAttributes.addFlashAttribute("message", successMessage);
        return "redirect:/visits/" + id;
    }

    private static double gpsLat(GpsForm form) {
        return requireCoordinate(form.getLat(), "latitude");
    }

    private static double gpsLng(GpsForm form) {
        return requireCoordinate(form.getLng(), "longitude");
    }

    private static double requireCoordinate(Double value, String name) {
        if (value == null) {
            throw new IllegalArgumentException("Enter both latitude and longitude (missing " + name + ").");
        }
        return value;
    }

    private void addPlanFormOptions(Model model) {
        model.addAttribute("hcps", hcpRepository.findAllByOrderByNameAsc());
        model.addAttribute("products", productRepository.findAll());
    }
}
