package com.aronalvarenga.rtts.modules.publictraining.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "public_training_enrollments")
public class PublicTrainingEnrollment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "public_trainee_id", nullable = false)
    private UUID publicTraineeId;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(name = "enrolled_at", nullable = false)
    private Instant enrolledAt;

    protected PublicTrainingEnrollment() {
    }

    public PublicTrainingEnrollment(UUID publicTraineeId, UUID trainingId) {
        this.publicTraineeId = publicTraineeId;
        this.trainingId = trainingId;
        this.enrolledAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getPublicTraineeId() {
        return publicTraineeId;
    }

    public UUID getTrainingId() {
        return trainingId;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }
}
