package com.aronalvarenga.rtts.modules.assessment.domain;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(
    name = "online_assessment_options",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_online_assessment_option_order",
            columnNames = {"question_id", "display_order"}
        ),
        @UniqueConstraint(
            name = "uq_online_assessment_option_question",
            columnNames = {"id", "question_id"}
        )
    }
)
public class AssessmentOption extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, updatable = false)
    private AssessmentQuestion question;

    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, length = 2000)
    private String text;

    @Positive
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    protected AssessmentOption() {
    }

    public AssessmentOption(AssessmentQuestion question, String text, int displayOrder, boolean correct) {
        this.question = question;
        this.text = text;
        this.displayOrder = displayOrder;
        this.correct = correct;
    }

    public void updateContent(String text, int displayOrder, boolean correct) {
        question.getAssessment().ensureDraft();
        this.text = text;
        this.displayOrder = displayOrder;
        this.correct = correct;
    }

    public UUID getId() { return id; }
    public AssessmentQuestion getQuestion() { return question; }
    public String getText() { return text; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isCorrect() { return correct; }
}
