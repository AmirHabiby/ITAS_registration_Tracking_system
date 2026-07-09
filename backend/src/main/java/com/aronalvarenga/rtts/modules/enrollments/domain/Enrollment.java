package com.aronalvarenga.rtts.modules.enrollments.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "training_enrollments")
public class Enrollment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "representative_id", nullable = false)
    private UUID representativeId;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(name = "training_request_id")
    private UUID trainingRequestId;

    @Column(name = "assessment_score", precision = 5, scale = 2)
    private BigDecimal assessmentScore;

    @Column
    private Boolean passed;

    @Column(name = "assessment_note", length = 500)
    private String assessmentNote;

    @Column(name = "assessed_at")
    private Instant assessedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    protected Enrollment() {
    }

    public Enrollment(UUID representativeId, UUID trainingId) {
        this.representativeId = representativeId;
        this.trainingId = trainingId;
    }

    public UUID getId() { return id; }
    public UUID getRepresentativeId() { return representativeId; }
    public UUID getTrainingId() { return trainingId; }
    public UUID getTrainingRequestId() { return trainingRequestId; }
    public BigDecimal getAssessmentScore() { return assessmentScore; }
    public Boolean getPassed() { return passed; }
    public String getAssessmentNote() { return assessmentNote; }
    public Instant getAssessedAt() { return assessedAt; }
    public EnrollmentStatus getStatus() { return status; }

    public void setId(UUID id) { this.id = id; }
    public void setRepresentativeId(UUID representativeId) { this.representativeId = representativeId; }
    public void setTrainingId(UUID trainingId) { this.trainingId = trainingId; }
    public void setTrainingRequestId(UUID trainingRequestId) { this.trainingRequestId = trainingRequestId; }
    public void setAssessmentScore(BigDecimal assessmentScore) { this.assessmentScore = assessmentScore; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public void setAssessmentNote(String assessmentNote) { this.assessmentNote = assessmentNote; }
    public void setAssessedAt(Instant assessedAt) { this.assessedAt = assessedAt; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
}
