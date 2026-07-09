package com.aronalvarenga.rtts.modules.requests.domain;

import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "training_requests")
public class TrainingRequestEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "representative_id", nullable = false)
    private UUID representativeId;

    @Column(name = "training_id", nullable = false)
    private UUID trainingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingRequestStatus status = TrainingRequestStatus.PENDING;

    @Column(name = "approved_by_delegator_id")
    private UUID approvedByDelegatorId;

    @Column(name = "rejected_by_delegator_id")
    private UUID rejectedByDelegatorId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "reviewer_username")
    private String reviewerUsername;

    @Column(name = "reviewer_note", length = 500)
    private String reviewerNote;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected TrainingRequestEntity() {
    }

    public TrainingRequestEntity(UUID representativeId, UUID trainingId) {
        this.representativeId = representativeId;
        this.trainingId = trainingId;
    }

    public UUID getId() { return id; }
    public UUID getRepresentativeId() { return representativeId; }
    public UUID getTrainingId() { return trainingId; }
    public TrainingRequestStatus getStatus() { return status; }
    public String getReviewerUsername() { return reviewerUsername; }
    public String getReviewerNote() { return reviewerNote; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public UUID getApprovedByDelegatorId() { return approvedByDelegatorId; }
    public UUID getRejectedByDelegatorId() { return rejectedByDelegatorId; }
    public Instant getApprovedAt() { return approvedAt; }
    public Instant getRejectedAt() { return rejectedAt; }

    public void setId(UUID id) { this.id = id; }
    public void setRepresentativeId(UUID representativeId) { this.representativeId = representativeId; }
    public void setTrainingId(UUID trainingId) { this.trainingId = trainingId; }
    public void setStatus(TrainingRequestStatus status) { this.status = status; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public void setApprovedByDelegatorId(UUID approvedByDelegatorId) { this.approvedByDelegatorId = approvedByDelegatorId; }
    public void setRejectedByDelegatorId(UUID rejectedByDelegatorId) { this.rejectedByDelegatorId = rejectedByDelegatorId; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public void setRejectedAt(Instant rejectedAt) { this.rejectedAt = rejectedAt; }
}
