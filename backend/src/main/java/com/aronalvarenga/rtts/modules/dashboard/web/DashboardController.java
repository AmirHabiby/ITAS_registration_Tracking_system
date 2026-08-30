package com.aronalvarenga.rtts.modules.dashboard.web;

import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import com.aronalvarenga.rtts.modules.dashboard.application.DashboardService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserAccountRepository userAccountRepository;

    public DashboardController(DashboardService dashboardService, UserAccountRepository userAccountRepository) {
        this.dashboardService = dashboardService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public AdminDashboardResponseDto adminDashboard() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/delegator")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','DELEGATOR')")
    public DelegatorDashboardResponseDto delegatorDashboard() {
        return dashboardService.delegatorDashboard();
    }

    @GetMapping("/institute")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','TRAINING_INSTITUTE')")
    public InstituteDashboardResponseDto instituteDashboard() {
        return dashboardService.instituteDashboard();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','REPRESENTATIVE','DELEGATOR','TRAINING_INSTITUTE')")
    public Object me(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        com.aronalvarenga.rtts.identity.domain.UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        return switch(user.getRole()) {
            case SYSTEM_ADMIN -> adminDashboard();
            case DELEGATOR -> delegatorDashboard();
            case TRAINING_INSTITUTE -> instituteDashboard();
            case REPRESENTATIVE -> dashboardService.representativeDashboard(userId);
        };
    }

    // Keep legacy endpoint for backward compatibility
    @GetMapping("/representative")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN','REPRESENTATIVE')")
    public RepresentativeDashboardResponseDto representativeDashboard(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
            .getId();
        return dashboardService.representativeDashboard(userId);
    }
}