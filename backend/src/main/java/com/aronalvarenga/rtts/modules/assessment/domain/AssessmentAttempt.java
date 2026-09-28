package com.aronalvarenga.rtts.modules.assessment.domain;

import com.aronalvarenga.rtts.modules.enrollments.domain.Enrollment;
import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "online_assessment_attempts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_online_assessment_attempt_number",
            columnNames = {"assessment_id", "training_enrollment_id", "attempt_number"}
        ),
        @UniqueConstraint(
            name = "uq_online_assessment_attempt_version",
            columnNames = {"id", "assessment_id"}
        )
    }
)
public class AssessmentAttempt extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, updatable = false)
    private OnlineAssessment assessment;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_enrollment_id", nullable = false, updatable = false)
    private Enrollment enrollment;

    @NotNull
    @Column(name = "training_id", nullable = false, updatable = false)
    private UUID trainingId;

    @Positive
    @Column(name = "attempt_number", nullable = false, updatable = false)
    private int attemptNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentAttemptStatus status = AssessmentAttemptStatus.IN_PROGRESS;

    @NotNull
    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @NotNull
    @Column(name = "deadline_at", nullable = false, updatable = false)
    private Instant deadlineAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "expired_at")
    private Instant expiredAt;

    @OneToMany(mappedBy = "attempt", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentAttemptAnswer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "attempt", fetch = FetchType.LAZY)
    private List<AssessmentResultRecord> results = new ArrayList<>();

    protected AssessmentAttempt() {
    }

    public AssessmentAttempt(OnlineAssessment assessment, Enrollment enrollment, int attemptNumber) {
        this(assessment, enrollment, attemptNumber, Instant.now());
    }

    public AssessmentAttempt(
        OnlineAssessment assessment,
        Enrollment enrollment,
        int attemptNumber,
        Instant startedAt
    ) {
        if (assessment.getStatus() != AssessmentStatus.PUBLISHED) {
            throw new IllegalArgumentException("Attempts can only be created for published assessments");
        }
        if (!assessment.getTraining().getId().equals(enrollment.getTrainingId())) {
            throw new IllegalArgumentException("Assessment and enrollment must refer to the same training");
        }
        this.assessment = assessment;
        this.enrollment = enrollment;
        this.trainingId = assessment.getTraining().getId();
        this.attemptNumber = attemptNumber;
        if (startedAt == null) {
            throw new IllegalArgumentException("Attempt start time is required");
        }
        this.startedAt = startedAt;
        this.deadlineAt = startedAt.plusSeconds((long) assessment.getDurationMinutes() * 60);
    }

    public void recordAnswer(AssessmentAttemptAnswer answer) {
        if (status != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Answers can only be recorded while an attempt is in progress");
        }
        UUID assessmentId = assessment.getId();
        UUID questionAssessmentId = answer.getQuestion().getAssessment().getId();
        if (answer.getAttempt() != this
            || (assessment != answer.getQuestion().getAssessment()
                && (assessmentId == null || !assessmentId.equals(questionAssessmentId)))) {
            throw new IllegalArgumentException("The answer question must belong to the attempt's assessment version");
        }
        answers.add(answer);
    }

    public void submit(Instant submittedAt) {
        if (status != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only an in-progress attempt can be submitted");
        }
        if (submittedAt == null || submittedAt.isAfter(deadlineAt)) {
            throw new IllegalArgumentException("Submission time is required");
        }
        this.status = AssessmentAttemptStatus.SUBMITTED;
        this.submittedAt = submittedAt;
    }

    public void expire(Instant expiredAt) {
        if (status != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only an in-progress attempt can expire");
        }
        if (expiredAt == null || expiredAt.isBefore(deadlineAt)) {
            throw new IllegalArgumentException("An attempt can only expire at or after its deadline");
        }
        this.status = AssessmentAttemptStatus.EXPIRED;
        this.expiredAt = expiredAt;
    }

    void attachResult(AssessmentResultRecord result) {
        if ((status != AssessmentAttemptStatus.SUBMITTED && status != AssessmentAttemptStatus.EXPIRED)
            || result == null || result.getAttempt() != this) {
            throw new IllegalStateException("A result requires a closed attempt");
        }
        results.add(result);
    }

    public UUID getId() { return id; }
    public OnlineAssessment getAssessment() { return assessment; }
    public Enrollment getEnrollment() { return enrollment; }
    public UUID getTrainingId() { return trainingId; }
    public int getAttemptNumber() { return attemptNumber; }
    public AssessmentAttemptStatus getStatus() { return status; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getDeadlineAt() { return deadlineAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getExpiredAt() { return expiredAt; }
    public List<AssessmentAttemptAnswer> getAnswers() { return List.copyOf(answers); }
    public List<AssessmentResultRecord> getResults() { return List.copyOf(results); }
}
