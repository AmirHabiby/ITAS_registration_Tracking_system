package com.aronalvarenga.rtts.notifications;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/email")
public class EmailTestController {

    private final EmailService emailService;

    public EmailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping
    public String sendTestEmail() {
        emailService.sendEmail(
                "test@example.com",
                "Test Email",
                "This is a test email from RTTS."
        );

        return "Test email sent successfully";
    }
}