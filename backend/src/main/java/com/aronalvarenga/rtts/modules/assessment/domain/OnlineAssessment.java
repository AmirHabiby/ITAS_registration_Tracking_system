package com.aronalvarenga.rtts.modules.assessment.domain;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.modules.trainings.domain.Training;
import com.aronalvarenga.rtts.shared.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "online_assessments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_online_assessment_version",
            columnNames = {"assessment_series_id", "version_number"}
        ),
        @UniqueConstraint(
            name = "uq_online_assessment_id_training",
            columnNames = {"id", "training_id"}
        )
    }
)
public class OnlineAssessment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "assessment_series_id", nullable = false, updatable = false)
    private UUID assessmentSeriesId;

    @Positive
    @Column(name = "version_number", nullable = false, updatable = false)
    private int versionNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_id", nullable = false, updatable = false)
    private Training training;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_user_id", nullable = false, updatable = false)
    private UserAccount createdBy;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String title;

    @Size(max = 2000)
    @Column(length = 2000)
    private String instructions;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "passing_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal passingScore;

    @Positive
    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes = 60;

    @Positive
    @Column(name = "attempt_limit", nullable = false)
    private int attemptLimit = 1;

    @Column(name = "available_from")
    private Instant availableFrom;

    @Column(name = "available_until")
    private Instant availableUntil;

    @Column(name = "randomize_questions", nullable = false)
    private boolean randomizeQuestions;

    @Column(name = "randomize_options", nullable = false)
    private boolean randomizeOptions;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<AssessmentQuestion> questions = new ArrayList<>();

    protected OnlineAssessment() {
    }

    public OnlineAssessment(
        UUID assessmentSeriesId,
        int versionNumber,
        Training training,
        UserAccount createdBy,
        String title,
        String instructions,
        BigDecimal passingScore
    ) {
        this.assessmentSeriesId = assessmentSeriesId;
        this.versionNumber = versionNumber;
        this.training = training;
        this.createdBy = createdBy;
        this.title = title;
        this.instructions = instructions;
        this.passingScore = passingScore;
    }

    public void addQuestion(AssessmentQuestion question) {
        ensureDraft();
        questions.add(question);
    }

    public void updateDetails(String title, String instructions, BigDecimal passingScore) {
        ensureDraft();
        this.title = title;
        this.instructions = instructions;
        this.passingScore = passingScore;
    }

    public void configure(
        int durationMinutes,
        int attemptLimit,
        Instant availableFrom,
        Instant availableUntil,
        boolean randomizeQuestions,
        boolean randomizeOptions
    ) {
        ensureDraft();
        if (durationMinutes < 1 || attemptLimit < 1) {
            throw new IllegalArgumentException("Duration and attempt limit must be positive");
        }
        if (availableFrom != null && availableUntil != null && !availableUntil.isAfter(availableFrom)) {
            throw new IllegalArgumentException("Availability end must be after availability start");
        }
        this.durationMinutes = durationMinutes;
        this.attemptLimit = attemptLimit;
        this.availableFrom = availableFrom;
        this.availableUntil = availableUntil;
        this.randomizeQuestions = randomizeQuestions;
        this.randomizeOptions = randomizeOptions;
    }

    public void publish() {
        ensureDraft();
        if (questions.isEmpty()) {
            throw new IllegalStateException("An assessment must contain at least one question");
        }
        BigDecimal totalPoints = questions.stream()
            .map(AssessmentQuestion::getPoints)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPoints.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Assessment must have positive total points");
        }
        if (availableFrom != null && availableUntil != null && !availableUntil.isAfter(availableFrom)) {
            throw new IllegalStateException("Availability end must be after availability start");
        }
        for (AssessmentQuestion question : questions) {
            question.validateForPublication();
        }
        status = AssessmentStatus.PUBLISHED;
    }

    public void ensureDraft() {
        if (status != AssessmentStatus.DRAFT) {
            throw new IllegalStateException("Published assessment versions are immutable");
        }
    }

    public UUID getId() { return id; }
    public UUID getAssessmentSeriesId() { return assessmentSeriesId; }
    public int getVersionNumber() { return versionNumber; }
    public Training getTraining() { return training; }
    public UserAccount getCreatedBy() { return createdBy; }
    public String getTitle() { return title; }
    public String getInstructions() { return instructions; }
    public BigDecimal getPassingScore() { return passingScore; }
    public int getDurationMinutes() { return durationMinutes; }
    public int getAttemptLimit() { return attemptLimit; }
    public Instant getAvailableFrom() { return availableFrom; }
    public Instant getAvailableUntil() { return availableUntil; }
    public boolean isRandomizeQuestions() { return randomizeQuestions; }
    public boolean isRandomizeOptions() { return randomizeOptions; }
    public AssessmentStatus getStatus() { return status; }
    public List<AssessmentQuestion> getQuestions() { return List.copyOf(questions); }
}
