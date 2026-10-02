package com.aronalvarenga.rtts.modules.firm.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "firm_admin_profiles")
public class FirmAdminProfile extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "firm_id", nullable = false, unique = true)
    private UUID firmId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    protected FirmAdminProfile() {}

    public FirmAdminProfile(UUID userId, UUID firmId, String fullName, String email) {
        this.userId = userId;
        this.firmId = firmId;
        this.fullName = fullName;
        this.email = email;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getFirmId() { return firmId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}