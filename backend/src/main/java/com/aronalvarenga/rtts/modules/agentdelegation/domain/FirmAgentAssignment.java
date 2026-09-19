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
@Table(name = "firm_agent_assignments")
public class FirmAgentAssignment extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "firm_delegation_id", nullable = false)
    private UUID firmDelegationId;
    @Column(name = "representative_profile_id", nullable = false)
    private UUID representativeProfileId;
    @Column(name = "assigned_by_user_id", nullable = false)
    private UUID assignedByUserId;
    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt = Instant.now();
    @Column(name = "revoked_at")
    private Instant revokedAt;
    @Column(length = 500)
    private String reason;

    protected FirmAgentAssignment() {}

    public FirmAgentAssignment(UUID firmDelegationId, UUID representativeProfileId, UUID assignedByUserId, String reason) {
        this.firmDelegationId = firmDelegationId;
        this.representativeProfileId = representativeProfileId;
        this.assignedByUserId = assignedByUserId;
        this.reason = reason;
    }

    public UUID getId() { return id; }
    public UUID getFirmDelegationId() { return firmDelegationId; }
    public UUID getRepresentativeProfileId() { return representativeProfileId; }
    public UUID getAssignedByUserId() { return assignedByUserId; }
    public Instant getAssignedAt() { return assignedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}