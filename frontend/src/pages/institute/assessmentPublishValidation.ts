import type { InstituteAssessment, InstituteAssessmentQuestion } from "../../services/assessmentService";

export function getQuestionPublishIssues(question: InstituteAssessmentQuestion): string[] {
  const issues: string[] = [];
  if (!question.prompt.trim()) issues.push("Add question text.");
  if (!Number.isFinite(question.points) || question.points <= 0) issues.push("Marks must be greater than zero.");

  if (question.questionType === "MULTIPLE_CHOICE") {
    if (question.options.length < 2) issues.push("Add at least two answer options.");
    const correctCount = question.options.filter((option) => option.correct).length;
    if (correctCount !== 1) issues.push("Mark exactly one answer as correct.");
    if (question.gradingRubric?.trim()) issues.push("Remove the rubric from this MCQ.");
  } else if (question.questionType === "WRITTEN_RESPONSE") {
    if (!question.gradingRubric?.trim()) issues.push("Add a grading rubric.");
    if (question.options.length > 0) issues.push("Remove MCQ options from this written question.");
  } else {
    issues.push("This question type is not supported.");
  }

  return issues;
}

export function getAssessmentPublishIssues(assessment: InstituteAssessment): string[] {
  if (assessment.questions.length === 0) return ["Add at least one question before publishing."];

  return assessment.questions.flatMap((question, index) => {
    const questionIssues = getQuestionPublishIssues(question);
    return questionIssues.map((issue) => `Question ${index + 1}: ${issue}`);
  });
}
