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
import java.util.UUID;

@Entity
@Table(
    name = "online_assessment_attempt_option_order",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_attempt_option_snapshot_position",
            columnNames = {"attempt_id", "question_id", "display_order"}
        ),
        @UniqueConstraint(
            name = "uq_attempt_option_snapshot_option",
            columnNames = {"attempt_id", "question_id", "option_id"}
        )
    }
)
public class AttemptOptionSnapshot extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, updatable = false)
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private AssessmentQuestion question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", nullable = false, updatable = false)
    private AssessmentOption option;

    @Column(name = "assessment_id", nullable = false, updatable = false)
    private UUID assessmentId;

    @Column(name = "display_order", nullable = false, updatable = false)
    private int displayOrder;

    protected AttemptOptionSnapshot() {
    }

    public AttemptOptionSnapshot(
        AssessmentAttempt attempt,
        AssessmentQuestion question,
        AssessmentOption option,
        int displayOrder
    ) {
        this.attempt = attempt;
        this.question = question;
        this.option = option;
        this.assessmentId = attempt.getAssessment().getId();
        this.displayOrder = displayOrder;
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public AssessmentQuestion getQuestion() { return question; }
    public AssessmentOption getOption() { return option; }
    public UUID getAssessmentId() { return assessmentId; }
    public int getDisplayOrder() { return displayOrder; }
}
