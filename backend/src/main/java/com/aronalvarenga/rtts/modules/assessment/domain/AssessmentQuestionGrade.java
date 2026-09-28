package com.aronalvarenga.rtts.modules.assessment.domain;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "online_assessment_question_grades")
public class AssessmentQuestionGrade extends AuditableEntity {

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

    @NotNull
    @Column(name = "assessment_id", nullable = false, updatable = false)
    private UUID assessmentId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grader_user_id", nullable = false, updatable = false)
    private UserAccount grader;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 5, fraction = 2)
    @Column(name = "awarded_marks", nullable = false, precision = 7, scale = 2, updatable = false)
    private BigDecimal awardedMarks;

    @Column(length = 4000, updatable = false)
    private String feedback;

    @NotNull
    @Column(name = "graded_at", nullable = false, updatable = false)
    private Instant gradedAt;

    @Column(name = "grade_revision", nullable = false, updatable = false)
    private long gradeRevision;

    protected AssessmentQuestionGrade() {
    }

    public AssessmentQuestionGrade(
        AssessmentAttempt attempt,
        AssessmentQuestion question,
        UserAccount grader,
        BigDecimal awardedMarks,
        String feedback,
        Instant gradedAt,
        long gradeRevision
    ) {
        if (attempt == null || question == null || grader == null || awardedMarks == null || gradedAt == null
            || gradeRevision < 1 || awardedMarks.compareTo(BigDecimal.ZERO) < 0
            || awardedMarks.compareTo(question.getPoints()) > 0
            || question.getQuestionType() != AssessmentQuestionType.WRITTEN_RESPONSE
            || !attempt.getAssessment().getId().equals(question.getAssessment().getId())) {
            throw new IllegalArgumentException("Invalid written-question grade");
        }
        this.attempt = attempt;
        this.question = question;
        this.assessmentId = attempt.getAssessment().getId();
        this.grader = grader;
        this.awardedMarks = awardedMarks;
        this.feedback = feedback;
        this.gradedAt = gradedAt;
        this.gradeRevision = gradeRevision;
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public AssessmentQuestion getQuestion() { return question; }
    public UUID getAssessmentId() { return assessmentId; }
    public UserAccount getGrader() { return grader; }
    public BigDecimal getAwardedMarks() { return awardedMarks; }
    public String getFeedback() { return feedback; }
    public Instant getGradedAt() { return gradedAt; }
    public long getGradeRevision() { return gradeRevision; }
}
