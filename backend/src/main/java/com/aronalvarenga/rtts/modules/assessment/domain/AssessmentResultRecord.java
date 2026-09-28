package com.aronalvarenga.rtts.modules.assessment.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "online_assessment_results")
public class AssessmentResultRecord extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, updatable = false)
    private AssessmentAttempt attempt;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 5, fraction = 2)
    @Column(name = "points_earned", nullable = false, precision = 7, scale = 2)
    private BigDecimal pointsEarned;

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 5, fraction = 2)
    @Column(name = "total_points", nullable = false, precision = 7, scale = 2)
    private BigDecimal totalPoints;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "score_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal scorePercent;

    @Column(nullable = false)
    private boolean passed;

    @Column(name = "is_final", nullable = false)
    private boolean finalResult = true;

    @Column(name = "grading_revision", nullable = false, updatable = false)
    private long gradingRevision;

    @NotNull
    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt = Instant.now();

    protected AssessmentResultRecord() {
    }

    public AssessmentResultRecord(
        AssessmentAttempt attempt,
        BigDecimal pointsEarned,
        BigDecimal totalPoints,
        BigDecimal scorePercent,
        boolean passed
    ) {
        this(attempt, pointsEarned, totalPoints, scorePercent, passed, Instant.now());
    }

    public AssessmentResultRecord(
        AssessmentAttempt attempt,
        BigDecimal pointsEarned,
        BigDecimal totalPoints,
        BigDecimal scorePercent,
        boolean passed,
        Instant evaluatedAt
    ) {
        this(attempt, pointsEarned, totalPoints, scorePercent, passed, evaluatedAt, true);
    }

    public AssessmentResultRecord(
        AssessmentAttempt attempt,
        BigDecimal pointsEarned,
        BigDecimal totalPoints,
        BigDecimal scorePercent,
        boolean passed,
        Instant evaluatedAt,
        boolean finalResult
    ) {
        this(attempt, pointsEarned, totalPoints, scorePercent, passed, evaluatedAt, finalResult, 0);
    }

    public AssessmentResultRecord(
        AssessmentAttempt attempt,
        BigDecimal pointsEarned,
        BigDecimal totalPoints,
        BigDecimal scorePercent,
        boolean passed,
        Instant evaluatedAt,
        boolean finalResult,
        long gradingRevision
    ) {
        if (pointsEarned == null || totalPoints == null || scorePercent == null
            || pointsEarned.compareTo(BigDecimal.ZERO) < 0
            || totalPoints.compareTo(BigDecimal.ZERO) <= 0
            || pointsEarned.compareTo(totalPoints) > 0
            || scorePercent.compareTo(BigDecimal.ZERO) < 0
            || scorePercent.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException("Assessment result values are outside their valid ranges");
        }
        this.attempt = attempt;
        this.pointsEarned = pointsEarned;
        this.totalPoints = totalPoints;
        this.scorePercent = scorePercent;
        this.passed = passed;
        this.finalResult = finalResult;
        if (gradingRevision < 0) {
            throw new IllegalArgumentException("Grading revision cannot be negative");
        }
        this.gradingRevision = gradingRevision;
        if (evaluatedAt == null) {
            throw new IllegalArgumentException("Result evaluation time is required");
        }
        this.evaluatedAt = evaluatedAt;
        attempt.attachResult(this);
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public BigDecimal getPointsEarned() { return pointsEarned; }
    public BigDecimal getTotalPoints() { return totalPoints; }
    public BigDecimal getScorePercent() { return scorePercent; }
    public boolean isPassed() { return passed; }
    public boolean isFinalResult() { return finalResult; }
    public long getGradingRevision() { return gradingRevision; }
    public Instant getEvaluatedAt() { return evaluatedAt; }
}
