package com.aronalvarenga.rtts.modules.assessment.application;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AssessmentAttemptRuntimeConfiguration {

    @Bean
    Clock assessmentClock() {
        return Clock.systemUTC();
    }
}
