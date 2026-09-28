package com.aronalvarenga.rtts.modules.assessment.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class AssessmentTestClockConfiguration {

    @Bean
    @Primary
    MutableAssessmentClock testAssessmentClock() {
        return new MutableAssessmentClock(Instant.parse("2026-01-01T12:00:00Z"));
    }
}

class MutableAssessmentClock extends Clock {
    private volatile Instant instant;

    MutableAssessmentClock(Instant instant) {
        this.instant = instant;
    }

    void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
