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
    name = "online_assessment_attempt_question_order",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_attempt_question_snapshot_position", columnNames = {"attempt_id", "display_order"}),
        @UniqueConstraint(name = "uq_attempt_question_snapshot_question", columnNames = {"attempt_id", "question_id"})
    }
)
public class AttemptQuestionSnapshot extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, updatable = false)
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private AssessmentQuestion question;

    @Column(name = "assessment_id", nullable = false, updatable = false)
    private UUID assessmentId;

    @Column(name = "display_order", nullable = false, updatable = false)
    private int displayOrder;

    protected AttemptQuestionSnapshot() {
    }

    public AttemptQuestionSnapshot(AssessmentAttempt attempt, AssessmentQuestion question, int displayOrder) {
        this.attempt = attempt;
        this.question = question;
        this.assessmentId = attempt.getAssessment().getId();
        this.displayOrder = displayOrder;
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public AssessmentQuestion getQuestion() { return question; }
    public UUID getAssessmentId() { return assessmentId; }
    public int getDisplayOrder() { return displayOrder; }
}
