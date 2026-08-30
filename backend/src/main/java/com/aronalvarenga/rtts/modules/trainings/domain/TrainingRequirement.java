package com.aronalvarenga.rtts.modules.trainings.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "training_requirements")
public class TrainingRequirement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @Column(name = "training_id", insertable = false, updatable = false, nullable = false)
    private UUID trainingId;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean required = true;

    protected TrainingRequirement() {
    }

    public TrainingRequirement(UUID trainingId, String name, String description, boolean required) {
        this.trainingId = trainingId;
        this.name = name;
        this.description = description;
        this.required = required;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTrainingId() {
        return trainingId;
    }

    public Training getTraining() {
        return training;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTraining(Training training) {
        this.training = training;
    }

    public void setTrainingId(UUID trainingId) {
        this.trainingId = trainingId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
