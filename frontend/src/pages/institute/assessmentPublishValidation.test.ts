import { describe, expect, it } from "vitest";
import type { InstituteAssessment } from "../../services/assessmentService";
import { getAssessmentPublishIssues } from "./assessmentPublishValidation";

function assessment(
  questions: InstituteAssessment["questions"],
): InstituteAssessment {
  return {
    id: "assessment-1",
    assessmentSeriesId: "series-1",
    version: 1,
    trainingId: "training-1",
    title: "Assessment",
    instructions: "",
    passingScore: 70,
    weekNumber: null,
    durationMinutes: 60,
    attemptLimit: 1,
    availableFrom: null,
    availableUntil: null,
    randomizeQuestions: false,
    randomizeOptions: false,
    status: "DRAFT",
    questions,
  };
}

describe("assessment publication validation", () => {
  it("requires at least one question", () => {
    expect(getAssessmentPublishIssues(assessment([]))).toEqual([
      "Add at least one question before publishing.",
    ]);
  });

  it("requires MCQs to have two options and exactly one correct answer", () => {
    const issues = getAssessmentPublishIssues(assessment([{
      id: "q1",
      prompt: "Choose one",
      questionType: "MULTIPLE_CHOICE",
      displayOrder: 1,
      points: 1,
      gradingRubric: null,
      options: [{ id: "o1", text: "A", displayOrder: 1, correct: false }],
    }]));

    expect(issues).toEqual([
      "Question 1: Add at least two answer options.",
      "Question 1: Mark exactly one answer as correct.",
    ]);
  });

  it("allows a valid MCQ and written question with its rubric", () => {
    const issues = getAssessmentPublishIssues(assessment([
      {
        id: "q1",
        prompt: "Choose one",
        questionType: "MULTIPLE_CHOICE",
        displayOrder: 1,
        points: 1,
        gradingRubric: null,
        options: [
          { id: "o1", text: "A", displayOrder: 1, correct: true },
          { id: "o2", text: "B", displayOrder: 2, correct: false },
        ],
      },
      {
        id: "q2",
        prompt: "Explain",
        questionType: "WRITTEN_RESPONSE",
        displayOrder: 2,
        points: 5,
        gradingRubric: "Award marks for a complete explanation.",
        options: [],
      },
    ]));

    expect(issues).toEqual([]);
  });
});
