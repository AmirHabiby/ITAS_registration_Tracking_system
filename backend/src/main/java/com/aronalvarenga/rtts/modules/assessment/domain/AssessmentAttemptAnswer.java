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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "online_assessment_attempt_answers",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_online_assessment_attempt_question",
        columnNames = {"attempt_id", "question_id"}
    )
)
public class AssessmentAttemptAnswer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, updatable = false)
    private AssessmentAttempt attempt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private AssessmentQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id")
    private AssessmentOption selectedOption;

    @Column(name = "response_text", length = 4000)
    private String responseText;

    @NotNull
    @Column(name = "assessment_id", nullable = false, updatable = false)
    private UUID assessmentId;

    @NotNull
    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt = Instant.now();

    protected AssessmentAttemptAnswer() {
    }

    public AssessmentAttemptAnswer(
        AssessmentAttempt attempt,
        AssessmentQuestion question,
        AssessmentOption selectedOption
    ) {
        if (!attempt.getAssessment().getId().equals(question.getAssessment().getId())) {
            throw new IllegalArgumentException("The question must belong to the attempt's assessment version");
        }
        if (selectedOption != null && !selectedOption.getQuestion().getId().equals(question.getId())) {
            throw new IllegalArgumentException("The selected option must belong to the answered question");
        }
        this.attempt = attempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.assessmentId = attempt.getAssessment().getId();
        attempt.recordAnswer(this);
    }

    public void update(AssessmentOption selectedOption, String responseText, Instant answeredAt) {
        if (attempt.getStatus() != AssessmentAttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Answers cannot be changed after an attempt is closed");
        }
        this.selectedOption = selectedOption;
        this.responseText = responseText;
        this.answeredAt = answeredAt;
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public AssessmentQuestion getQuestion() { return question; }
    public AssessmentOption getSelectedOption() { return selectedOption; }
    public String getResponseText() { return responseText; }
    public UUID getAssessmentId() { return assessmentId; }
    public Instant getAnsweredAt() { return answeredAt; }
}
