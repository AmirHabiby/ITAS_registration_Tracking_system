package com.aronalvarenga.rtts.modules.agentdelegation.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "firms")
public class Firm extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    protected Firm() {
    }

    public Firm(String name, String email, String description) {
        this.name = name;
        this.email = email;
        this.description = description;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { this.active = active; }
}
