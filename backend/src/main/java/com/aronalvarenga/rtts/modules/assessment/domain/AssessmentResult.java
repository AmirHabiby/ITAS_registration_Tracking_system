package com.aronalvarenga.rtts.modules.assessment.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "assessment_results")
public class AssessmentResult extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "training_enrollment_id", nullable = false)
    private UUID trainingEnrollmentId;

    @Column(name = "submitted_by_user_id", nullable = false)
    private UUID submittedByUserId;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(nullable = false)
    private boolean passed;

    @Column(length = 500)
    private String remarks;

    @Column(name = "assessment_date", nullable = false)
    private Instant assessmentDate = Instant.now();

    protected AssessmentResult() {
    }

    public AssessmentResult(UUID trainingEnrollmentId, UUID submittedByUserId, BigDecimal score, boolean passed, String remarks) {
        this.trainingEnrollmentId = trainingEnrollmentId;
        this.submittedByUserId = submittedByUserId;
        this.score = score;
        this.passed = passed;
        this.remarks = remarks;
    }

    public UUID getId() { return id; }
    public UUID getTrainingEnrollmentId() { return trainingEnrollmentId; }
    public UUID getSubmittedByUserId() { return submittedByUserId; }
    public BigDecimal getScore() { return score; }
    public boolean isPassed() { return passed; }
    public String getRemarks() { return remarks; }
    public Instant getAssessmentDate() { return assessmentDate; }
    public void setId(UUID id) { this.id = id; }
    public void setTrainingEnrollmentId(UUID trainingEnrollmentId) { this.trainingEnrollmentId = trainingEnrollmentId; }
    public void setSubmittedByUserId(UUID submittedByUserId) { this.submittedByUserId = submittedByUserId; }
    public void setScore(BigDecimal score) { this.score = score; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public void setAssessmentDate(Instant assessmentDate) { this.assessmentDate = assessmentDate; }
}