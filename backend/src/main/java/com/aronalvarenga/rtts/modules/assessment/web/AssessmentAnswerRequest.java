package com.aronalvarenga.rtts.modules.assessment.web;

import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AssessmentAnswerRequest(
    UUID selectedOptionId,
    @Size(max = 4000) String responseText
) {}
