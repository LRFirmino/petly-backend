package com.pawsclaws.petshop.web;

import com.pawsclaws.petshop.service.ReportsService;
import com.pawsclaws.petshop.web.dto.ReportsSummary;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    private final ReportsService reportsService;

    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    @GetMapping("/summary")
    public ReportsSummary summary() {
        return reportsService.summary();
    }
}
