package com.aronalvarenga.rtts.modules.agentdelegation.domain;

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
@Table(name = "firm_delegations")
public class FirmDelegation extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "firm_id", nullable = false)
    private UUID firmId;
    @Column(name = "delegator_profile_id", nullable = false)
    private UUID delegatorProfileId;
    @Column(name = "delegated_at", nullable = false)
    private Instant delegatedAt = Instant.now();
    @Column(name = "revoked_at")
    private Instant revokedAt;
    @Column(length = 500)
    private String reason;

    protected FirmDelegation() {}

    public FirmDelegation(UUID firmId, UUID delegatorProfileId, String reason) {
        this.firmId = firmId;
        this.delegatorProfileId = delegatorProfileId;
        this.reason = reason;
    }

    public UUID getId() { return id; }
    public UUID getFirmId() { return firmId; }
    public UUID getDelegatorProfileId() { return delegatorProfileId; }
    public Instant getDelegatedAt() { return delegatedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getReason() { return reason; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}