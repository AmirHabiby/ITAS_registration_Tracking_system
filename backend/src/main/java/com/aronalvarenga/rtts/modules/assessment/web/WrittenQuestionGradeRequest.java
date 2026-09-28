package com.aronalvarenga.rtts.modules.assessment.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record WrittenQuestionGradeRequest(
    @NotNull @DecimalMin("0.00") @Digits(integer = 5, fraction = 2) BigDecimal awardedMarks,
    @Size(max = 4000) String feedback
) {}
