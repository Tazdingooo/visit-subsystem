package com.example.visitsubsystem.web.api;

import com.example.visitsubsystem.repository.HcpRepository;
import com.example.visitsubsystem.repository.ProductRepository;
import com.example.visitsubsystem.service.VisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class VisitApiController {

    private final VisitService visitService;
    private final HcpRepository hcpRepository;
    private final ProductRepository productRepository;

    public VisitApiController(VisitService visitService,
                              HcpRepository hcpRepository,
                              ProductRepository productRepository) {
        this.visitService = visitService;
        this.hcpRepository = hcpRepository;
        this.productRepository = productRepository;
    }

    @GetMapping("/visits")
    public List<VisitResponse> list() {
        return visitService.listVisits().stream().map(VisitResponse::from).toList();
    }

    @GetMapping("/visits/{id}")
    public VisitResponse get(@PathVariable long id) {
        return VisitResponse.from(visitService.getVisitDetail(id));
    }

    @PostMapping("/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitResponse plan(@Valid @RequestBody PlanVisitRequest request) {
        var visit = visitService.planVisit(request.hcpId(), request.productId(),
                request.departmentName(), request.plannedAt());
        return VisitResponse.from(visitService.getVisitDetail(visit.getId()));
    }

    @PostMapping("/visits/{id}/check-in")
    public VisitResponse checkIn(@PathVariable long id, @Valid @RequestBody GpsRequest request) {
        visitService.checkIn(id, request.lat(), request.lng());
        return VisitResponse.from(visitService.getVisitDetail(id));
    }

    @PostMapping("/visits/{id}/check-out")
    public VisitResponse checkOut(@PathVariable long id, @Valid @RequestBody GpsRequest request) {
        visitService.checkOut(id, request.lat(), request.lng());
        return VisitResponse.from(visitService.getVisitDetail(id));
    }

    @PostMapping("/visits/{id}/report")
    public VisitResponse report(@PathVariable long id, @RequestBody ReportRequest request) {
        visitService.submitReport(id, request.talkingPoints(), request.feedback(),
                request.materialsDistributed());
        return VisitResponse.from(visitService.getVisitDetail(id));
    }

    @GetMapping("/hcps")
    public List<HcpResponse> hcps() {
        return hcpRepository.findAllByOrderByNameAsc().stream().map(HcpResponse::from).toList();
    }

    @GetMapping("/products")
    public List<ProductResponse> products() {
        return productRepository.findAll().stream()
                .map(p -> new ProductResponse(p.getId(), p.getName()))
                .toList();
    }
}
