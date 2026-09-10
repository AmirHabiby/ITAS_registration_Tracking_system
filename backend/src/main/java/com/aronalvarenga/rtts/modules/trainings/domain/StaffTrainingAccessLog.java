package com.aronalvarenga.rtts.modules.trainings.domain;

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
@Table(name = "staff_training_access_logs")
public class StaffTrainingAccessLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(name = "full_name", nullable = false, length = 160)
    private String fullName;

    @Column(nullable = false, length = 160)
    private String email;

    @Column(nullable = false, length = 120)
    private String department;

    @Column(name = "assessment_score", precision = 5, scale = 2)
    private BigDecimal assessmentScore;

    private Boolean passed;

    @Column(name = "assessed_at")
    private Instant assessedAt;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "accessed_at", nullable = false)
    private Instant accessedAt = Instant.now();

    protected StaffTrainingAccessLog() {
    }

    public StaffTrainingAccessLog(UUID trainingId, String fullName, String email, String department) {
        this.trainingId = trainingId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
    }

    public UUID getId() { return id; }
    public UUID getTrainingId() { return trainingId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public BigDecimal getAssessmentScore() { return assessmentScore; }
    public Boolean getPassed() { return passed; }
    public Instant getAssessedAt() { return assessedAt; }
    public boolean isCompleted() { return completed; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getAccessedAt() { return accessedAt; }
    public void setId(UUID id) { this.id = id; }
    public void setTrainingId(UUID trainingId) { this.trainingId = trainingId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setDepartment(String department) { this.department = department; }
    public void setAssessmentScore(BigDecimal assessmentScore) { this.assessmentScore = assessmentScore; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public void setAssessedAt(Instant assessedAt) { this.assessedAt = assessedAt; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public void setAccessedAt(Instant accessedAt) { this.accessedAt = accessedAt; }
}
