package com.aronalvarenga.rtts.modules.dashboard.web;

import com.aronalvarenga.rtts.modules.dashboard.application.DashboardService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public AdminDashboardResponseDto adminDashboard() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/delegator/dashboard")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public DelegatorDashboardResponseDto delegatorDashboard() {
        return dashboardService.delegatorDashboard();
    }

    @GetMapping("/institute/dashboard")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
    public InstituteDashboardResponseDto instituteDashboard() {
        return dashboardService.instituteDashboard();
    }

    @GetMapping("/representative/dashboard/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','REPRESENTATIVE')")
    public RepresentativeDashboardResponseDto representativeDashboard(@PathVariable UUID id) {
        return dashboardService.representativeDashboard(id);
    }
}