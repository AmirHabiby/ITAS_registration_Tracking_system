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
@Table(name = "agent_delegations")
public class AgentDelegation extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "representative_profile_id", nullable = false)
    private UUID representativeProfileId;

    @Column(name = "delegator_profile_id", nullable = false)
    private UUID delegatorProfileId;

    @Column(name = "delegated_at", nullable = false)
    private Instant delegatedAt = Instant.now();

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(length = 500)
    private String reason;

    protected AgentDelegation() {
    }

    public AgentDelegation(UUID representativeProfileId, UUID delegatorProfileId, String reason) {
        this.representativeProfileId = representativeProfileId;
        this.delegatorProfileId = delegatorProfileId;
        this.reason = reason;
    }

    public UUID getId() { return id; }
    public UUID getRepresentativeProfileId() { return representativeProfileId; }
    public UUID getDelegatorProfileId() { return delegatorProfileId; }
    public Instant getDelegatedAt() { return delegatedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public String getReason() { return reason; }
    public void setId(UUID id) { this.id = id; }
    public void setRepresentativeProfileId(UUID representativeProfileId) { this.representativeProfileId = representativeProfileId; }
    public void setDelegatorProfileId(UUID delegatorProfileId) { this.delegatorProfileId = delegatorProfileId; }
    public void setDelegatedAt(Instant delegatedAt) { this.delegatedAt = delegatedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public void setReason(String reason) { this.reason = reason; }
}