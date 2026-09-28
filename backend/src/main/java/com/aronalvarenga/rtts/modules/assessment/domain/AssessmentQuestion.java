package com.aronalvarenga.rtts.modules.assessment.domain;

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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "online_assessment_questions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_online_assessment_question_order",
            columnNames = {"assessment_id", "display_order"}
        ),
        @UniqueConstraint(
            name = "uq_online_assessment_question_version",
            columnNames = {"id", "assessment_id"}
        )
    }
)
public class AssessmentQuestion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false, updatable = false)
    private OnlineAssessment assessment;

    @NotBlank
    @Size(max = 4000)
    @Column(nullable = false, length = 4000)
    private String prompt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private AssessmentQuestionType questionType = AssessmentQuestionType.MULTIPLE_CHOICE;

    @Positive
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @NotNull
    @DecimalMin(value = "0.01")
    @Digits(integer = 5, fraction = 2)
    @Column(nullable = false, precision = 7, scale = 2)
    private BigDecimal points;

    @Size(max = 4000)
    @Column(name = "grading_rubric", length = 4000)
    private String gradingRubric;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<AssessmentOption> options = new ArrayList<>();

    protected AssessmentQuestion() {
    }

    public AssessmentQuestion(OnlineAssessment assessment, String prompt, int displayOrder, BigDecimal points) {
        this(assessment, prompt, AssessmentQuestionType.MULTIPLE_CHOICE, displayOrder, points, null);
    }

    public AssessmentQuestion(
        OnlineAssessment assessment,
        String prompt,
        AssessmentQuestionType questionType,
        int displayOrder,
        BigDecimal points,
        String gradingRubric
    ) {
        this.assessment = assessment;
        this.prompt = prompt;
        this.displayOrder = displayOrder;
        this.points = points;
        this.questionType = questionType;
        this.gradingRubric = gradingRubric;
    }

    public void addOption(AssessmentOption option) {
        assessment.ensureDraft();
        options.add(option);
    }

    public void updateContent(String prompt, int displayOrder, BigDecimal points) {
        updateContent(prompt, questionType, displayOrder, points, gradingRubric);
    }

    public void updateContent(
        String prompt,
        AssessmentQuestionType questionType,
        int displayOrder,
        BigDecimal points,
        String gradingRubric
    ) {
        assessment.ensureDraft();
        this.prompt = prompt;
        this.questionType = questionType;
        this.displayOrder = displayOrder;
        this.points = points;
        this.gradingRubric = gradingRubric;
    }

    public void validateForPublication() {
        if (questionType == AssessmentQuestionType.MULTIPLE_CHOICE) {
            long correctOptions = options.stream().filter(AssessmentOption::isCorrect).count();
            if (options.size() < 2 || correctOptions != 1 || gradingRubric != null) {
                throw new IllegalStateException("Each multiple-choice question needs at least two options and exactly one correct answer");
            }
        } else if (questionType == AssessmentQuestionType.WRITTEN_RESPONSE) {
            if (gradingRubric == null || gradingRubric.isBlank() || !options.isEmpty()) {
                throw new IllegalStateException("Written questions need a grading rubric and cannot have MCQ options");
            }
        } else {
            throw new IllegalStateException("Unsupported assessment question type");
        }
    }

    public UUID getId() { return id; }
    public OnlineAssessment getAssessment() { return assessment; }
    public String getPrompt() { return prompt; }
    public AssessmentQuestionType getQuestionType() { return questionType; }
    public int getDisplayOrder() { return displayOrder; }
    public BigDecimal getPoints() { return points; }
    public String getGradingRubric() { return gradingRubric; }
    public List<AssessmentOption> getOptions() { return List.copyOf(options); }
}
