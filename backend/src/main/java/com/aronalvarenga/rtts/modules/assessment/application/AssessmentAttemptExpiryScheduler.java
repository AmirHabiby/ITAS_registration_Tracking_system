package com.aronalvarenga.rtts.modules.assessment.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AssessmentAttemptExpiryScheduler {

    private final AssessmentAttemptService attemptService;

    public AssessmentAttemptExpiryScheduler(AssessmentAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Scheduled(fixedDelayString = "${assessment.expiration-scan-delay-ms:15000}")
    public void expireDueAttempts() {
        attemptService.expireDueAttempts();
    }
}
