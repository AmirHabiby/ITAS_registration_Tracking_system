package com.aronalvarenga.rtts.modules.trainings.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "trainings")
public class Training extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "training_institute_profile_id", nullable = false)
    private UUID trainingInstituteProfileId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "passing_score", nullable = false, precision = 5, scale = 2)
    private java.math.BigDecimal passingScore = new java.math.BigDecimal("70.00");

    @Column(name = "allowed_retake_attempts", nullable = false)
    private int allowedRetakeAttempts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status = TrainingStatus.DRAFT;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false)
    private TrainingAccessType accessType = TrainingAccessType.PRIVATE;

    @Column(name = "staff_access_password_hash")
    private String staffAccessPasswordHash;

    protected Training() {
    }

    public Training(UUID trainingInstituteProfileId, String title, String description, LocalDate startDate, LocalDate endDate, int capacity) {
        this.trainingInstituteProfileId = trainingInstituteProfileId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTrainingInstituteProfileId() {
        return trainingInstituteProfileId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getCapacity() {
        return capacity;
    }

    public java.math.BigDecimal getPassingScore() {
        return passingScore;
    }

    public int getAllowedRetakeAttempts() {
        return allowedRetakeAttempts;
    }

    public TrainingStatus getStatus() {
        return status;
    }

    public boolean isActive() {
        return active;
    }

    public TrainingAccessType getAccessType() {
        return accessType;
    }

    public String getStaffAccessPasswordHash() {
        return staffAccessPasswordHash;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTrainingInstituteProfileId(UUID trainingInstituteProfileId) {
        this.trainingInstituteProfileId = trainingInstituteProfileId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setPassingScore(java.math.BigDecimal passingScore) {
        this.passingScore = passingScore;
    }

    public void setAllowedRetakeAttempts(int allowedRetakeAttempts) {
        this.allowedRetakeAttempts = allowedRetakeAttempts;
    }

    public void setStatus(TrainingStatus status) {
        this.status = status;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAccessType(TrainingAccessType accessType) {
        this.accessType = accessType;
    }

    public void setStaffAccessPasswordHash(String staffAccessPasswordHash) {
        this.staffAccessPasswordHash = staffAccessPasswordHash;
    }
}
