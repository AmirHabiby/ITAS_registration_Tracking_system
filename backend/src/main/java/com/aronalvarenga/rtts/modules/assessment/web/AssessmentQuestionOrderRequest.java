package com.aronalvarenga.rtts.modules.assessment.web;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record AssessmentQuestionOrderRequest(
    @NotEmpty @Size(max = 1000) List<@NotNull UUID> questionIdsInOrder
) {}
