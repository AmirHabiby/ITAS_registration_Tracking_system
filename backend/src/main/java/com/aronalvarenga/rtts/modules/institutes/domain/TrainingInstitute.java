package com.aronalvarenga.rtts.modules.institutes.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "training_institute_profiles")
public class TrainingInstitute extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "user_id", unique = true)
    private UUID userId;

    @Column(nullable = false)
    private boolean active = true;

    protected TrainingInstitute() {
    }

    public TrainingInstitute(String name, String contactEmail) {
        this.name = name;
        this.contactEmail = contactEmail;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isActive() {
        return active;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
