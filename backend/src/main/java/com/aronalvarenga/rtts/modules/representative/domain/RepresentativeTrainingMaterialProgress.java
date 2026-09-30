package com.aronalvarenga.rtts.modules.representative.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "representative_training_material_progress",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_representative_training_material_progress",
        columnNames = {"representative_id", "material_id"}))
public class RepresentativeTrainingMaterialProgress extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "representative_id", nullable = false)
    private UUID representativeId;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    protected RepresentativeTrainingMaterialProgress() {
    }

    public RepresentativeTrainingMaterialProgress(UUID representativeId, UUID trainingId, UUID materialId) {
        this.representativeId = representativeId;
        this.trainingId = trainingId;
        this.materialId = materialId;
        this.completedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getRepresentativeId() {
        return representativeId;
    }

    public UUID getTrainingId() {
        return trainingId;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
