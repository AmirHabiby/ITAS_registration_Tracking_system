package com.aronalvarenga.rtts.modules.assessment.web;

import com.aronalvarenga.rtts.modules.assessment.domain.AssessmentQuestionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AssessmentQuestionRequest(
    @NotBlank @Size(max = 4000) String prompt,
    @NotNull AssessmentQuestionType questionType,
    @Min(1) @Max(1000) int displayOrder,
    @NotNull @DecimalMin("0.01") @Digits(integer = 5, fraction = 2) BigDecimal points,
    @Size(max = 4000) String gradingRubric
) {}
