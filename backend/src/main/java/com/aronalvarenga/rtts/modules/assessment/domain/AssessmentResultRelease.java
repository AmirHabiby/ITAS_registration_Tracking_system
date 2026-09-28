package com.aronalvarenga.rtts.modules.assessment.domain;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "online_assessment_result_releases",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_assessment_result_release_version",
        columnNames = {"attempt_id", "result_id"}
    )
)
public class AssessmentResultRelease extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, updatable = false)
    private AssessmentAttempt attempt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false, updatable = false)
    private AssessmentResultRecord result;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "released_by_user_id", nullable = false, updatable = false)
    private UserAccount releasedBy;

    @NotNull
    @Column(name = "released_at", nullable = false, updatable = false)
    private Instant releasedAt;

    @Column(name = "release_number", nullable = false, updatable = false)
    private int releaseNumber;

    protected AssessmentResultRelease() {
    }

    public AssessmentResultRelease(
        AssessmentAttempt attempt,
        AssessmentResultRecord result,
        UserAccount releasedBy,
        Instant releasedAt,
        int releaseNumber
    ) {
        if (releaseNumber < 1) {
            throw new IllegalArgumentException("Release number must be positive");
        }
        this.attempt = attempt;
        this.result = result;
        this.releasedBy = releasedBy;
        this.releasedAt = releasedAt;
        this.releaseNumber = releaseNumber;
    }

    public UUID getId() { return id; }
    public AssessmentAttempt getAttempt() { return attempt; }
    public AssessmentResultRecord getResult() { return result; }
    public UserAccount getReleasedBy() { return releasedBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public int getReleaseNumber() { return releaseNumber; }
}
