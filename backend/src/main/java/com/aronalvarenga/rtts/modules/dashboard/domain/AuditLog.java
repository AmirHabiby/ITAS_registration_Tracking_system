package com.aronalvarenga.rtts.modules.dashboard.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(length = 2000)
    private String details;

    protected AuditLog() {
    }

    public AuditLog(UUID actorUserId, String action, String targetType, UUID targetId, String details) {
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
    }

    public UUID getId() { return id; }
    public UUID getActorUserId() { return actorUserId; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public String getDetails() { return details; }
    public void setId(UUID id) { this.id = id; }
    public void setActorUserId(UUID actorUserId) { this.actorUserId = actorUserId; }
    public void setAction(String action) { this.action = action; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public void setDetails(String details) { this.details = details; }
}