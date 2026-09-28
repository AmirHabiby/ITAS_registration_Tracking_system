import { apiClient } from "./apiClient";

export type AssessmentStatus = "DRAFT" | "PUBLISHED" | "COMPLETED" | string;
export type QuestionType = "MULTIPLE_CHOICE" | "WRITTEN_RESPONSE";

export type InstituteAssessmentOption = {
  id: string;
  text: string;
  displayOrder: number;
  correct: boolean;
};

export type InstituteAssessmentQuestion = {
  id: string;
  prompt: string;
  questionType: QuestionType;
  displayOrder: number;
  points: number;
  gradingRubric: string | null;
  options: InstituteAssessmentOption[];
};

export type InstituteAssessment = {
  id: string;
  assessmentSeriesId: string;
  version: number;
  trainingId: string;
  title: string;
  instructions: string;
  passingScore: number;
  durationMinutes: number;
  attemptLimit: number;
  availableFrom: string | null;
  availableUntil: string | null;
  randomizeQuestions: boolean;
  randomizeOptions: boolean;
  status: AssessmentStatus;
  questions: InstituteAssessmentQuestion[];
};

export type AssessmentDraft = {
  title: string;
  instructions: string;
  passingScore: number;
  durationMinutes: number;
  attemptLimit: number;
  availableFrom: string | null;
  availableUntil: string | null;
  randomizeQuestions: boolean;
  randomizeOptions: boolean;
};

export type AssessmentQuestionDraft = {
  prompt: string;
  questionType: QuestionType;
  displayOrder: number;
  points: number;
  gradingRubric: string | null;
};

export type AssessmentOptionDraft = {
  text: string;
  displayOrder: number;
  correct: boolean;
};

export type CandidateAssessment = {
  id: string;
  trainingId: string;
  title: string;
  instructions: string;
  version: number;
  durationMinutes: number;
  attemptLimit: number;
  attemptsUsed: number;
  activeAttemptId: string | null;
  availableFrom: string | null;
  availableUntil: string | null;
};

export type CandidateAttempt = {
  id: string;
  assessmentId: string;
  assessmentTitle: string;
  assessmentVersion: number;
  status: "IN_PROGRESS" | "SUBMITTED" | "EXPIRED" | string;
  startedAt: string;
  deadlineAt: string;
  submittedAt: string | null;
  expiredAt: string | null;
  remainingSeconds: number;
  questions: CandidateAttemptQuestion[];
};

export type CandidateAttemptQuestion = {
  id: string;
  prompt: string;
  questionType: QuestionType;
  points: number;
  displayOrder: number;
  options: { id: string; text: string; displayOrder: number }[];
  selectedOptionId: string | null;
  responseText: string | null;
};

export type CandidateAttemptHistory = {
  attemptId: string;
  assessmentId: string;
  trainingId: string;
  trainingTitle: string;
  assessmentTitle: string;
  assessmentVersion: number;
  attemptNumber: number;
  status: string;
  startedAt: string;
  submittedAt: string | null;
  expiredAt: string | null;
  releasedResult: AssessmentResult | null;
};

export type AssessmentResult = {
  attemptId: string;
  attemptNumber: number;
  awardedMarks: number;
  maximumMarks: number;
  percentage: number;
  passed: boolean;
  evaluatedAt: string;
  releasedAt: string | null;
  writtenFeedback: {
    questionId: string;
    prompt: string;
    awardedMarks: number;
    maximumMarks: number;
    feedback: string | null;
  }[];
};

export type GradingQueueItem = {
  attemptId: string;
  assessmentId: string;
  assessmentTitle: string;
  representativeName: string;
  attemptNumber: number;
  attemptStatus: string;
  submittedAt: string | null;
  expiredAt: string | null;
  writtenQuestionCount: number;
  gradedWrittenQuestionCount: number;
};

export type CompletedAssessmentAttempt = {
  attemptId: string;
  assessmentId: string;
  assessmentTitle: string;
  representativeName: string;
  attemptNumber: number;
  attemptStatus: string;
  submittedAt: string | null;
  expiredAt: string | null;
  writtenQuestionCount: number;
  gradedWrittenQuestionCount: number;
  finalized: boolean;
  releasedAt: string | null;
  awardedMarks: number | null;
  maximumMarks: number | null;
  percentage: number | null;
  passed: boolean | null;
};

export type WrittenGrading = {
  attemptId: string;
  attemptStatus: string;
  answers: {
    questionId: string;
    prompt: string;
    responseText: string | null;
    gradingRubric: string | null;
    maximumMarks: number;
    awardedMarks: number | null;
    feedback: string | null;
    graderUserId: string | null;
    gradedAt: string | null;
    history: {
      awardedMarks: number;
      feedback: string | null;
      graderUserId: string;
      gradedAt: string;
    }[];
  }[];
};

export type DelegatorAssessmentOutcome = {
  attemptId: string;
  representativeId: string;
  representativeName: string;
  trainingId: string;
  trainingTitle: string;
  assessmentId: string;
  assessmentTitle: string;
  attemptNumber: number;
  awardedMarks: number;
  maximumMarks: number;
  percentage: number;
  passed: boolean;
  releasedAt: string;
};

const instituteBase = "/api/institutes/me";
const representativeBase = "/api/representative";

export const assessmentService = {
  listInstituteAssessments: (trainingId: string) =>
    apiClient.get<InstituteAssessment[]>(`${instituteBase}/trainings/${trainingId}/online-assessments`),
  createAssessment: (trainingId: string, body: AssessmentDraft) =>
    apiClient.post<InstituteAssessment>(`${instituteBase}/trainings/${trainingId}/online-assessments`, body),
  updateAssessment: (assessmentId: string, body: AssessmentDraft) =>
    apiClient.put<InstituteAssessment>(`${instituteBase}/online-assessments/${assessmentId}`, body),
  previewAssessment: (assessmentId: string) =>
    apiClient.get<InstituteAssessment>(`${instituteBase}/online-assessments/${assessmentId}/preview`),
  createRevision: (assessmentId: string) =>
    apiClient.post<InstituteAssessment>(`${instituteBase}/online-assessments/${assessmentId}/revisions`),
  publishAssessment: (assessmentId: string) =>
    apiClient.post<InstituteAssessment>(`${instituteBase}/online-assessments/${assessmentId}/publish`),
  addQuestion: (assessmentId: string, body: AssessmentQuestionDraft) =>
    apiClient.post<InstituteAssessment>(`${instituteBase}/online-assessments/${assessmentId}/questions`, body),
  updateQuestion: (assessmentId: string, questionId: string, body: AssessmentQuestionDraft) =>
    apiClient.put<InstituteAssessment>(
      `${instituteBase}/online-assessments/${assessmentId}/questions/${questionId}`,
      body,
    ),
  deleteQuestion: (assessmentId: string, questionId: string) =>
    apiClient.delete(`${instituteBase}/online-assessments/${assessmentId}/questions/${questionId}`),
  reorderQuestions: (assessmentId: string, questionIdsInOrder: string[]) =>
    apiClient.put<InstituteAssessment>(`${instituteBase}/online-assessments/${assessmentId}/questions/order`, {
      questionIdsInOrder,
    }),
  addOption: (assessmentId: string, questionId: string, body: AssessmentOptionDraft) =>
    apiClient.post<InstituteAssessment>(
      `${instituteBase}/online-assessments/${assessmentId}/questions/${questionId}/options`,
      body,
    ),
  updateOption: (assessmentId: string, questionId: string, optionId: string, body: AssessmentOptionDraft) =>
    apiClient.put<InstituteAssessment>(
      `${instituteBase}/online-assessments/${assessmentId}/questions/${questionId}/options/${optionId}`,
      body,
    ),
  deleteOption: (assessmentId: string, questionId: string, optionId: string) =>
    apiClient.delete(
      `${instituteBase}/online-assessments/${assessmentId}/questions/${questionId}/options/${optionId}`,
    ),
  listGradingQueue: () =>
    apiClient.get<GradingQueueItem[]>(`${instituteBase}/online-assessments/grading-queue`),
  listCompletedAttempts: () =>
    apiClient.get<CompletedAssessmentAttempt[]>(`${instituteBase}/online-assessments/completed-attempts`),
  getWrittenGrading: (attemptId: string) =>
    apiClient.get<WrittenGrading>(`${instituteBase}/online-assessment-attempts/${attemptId}/written-grading`),
  gradeWrittenQuestion: (
    attemptId: string,
    questionId: string,
    body: { awardedMarks: number; feedback: string },
  ) =>
    apiClient.post<WrittenGrading>(
      `${instituteBase}/online-assessment-attempts/${attemptId}/written-answers/${questionId}/grades`,
      body,
    ),
  finalizeGrading: (attemptId: string) =>
    apiClient.post<AssessmentResult>(
      `${instituteBase}/online-assessment-attempts/${attemptId}/finalize-grading`,
    ),
  releaseResult: (attemptId: string) =>
    apiClient.post<AssessmentResult>(
      `${instituteBase}/online-assessment-attempts/${attemptId}/release-result`,
    ),
  getInstituteResult: (attemptId: string) =>
    apiClient.get<AssessmentResult>(
      `${instituteBase}/online-assessment-attempts/${attemptId}/result`,
    ),
  listEligibleAssessments: () =>
    apiClient.get<CandidateAssessment[]>(`${representativeBase}/online-assessments`),
  listMyAttempts: () =>
    apiClient.get<CandidateAttemptHistory[]>(`${representativeBase}/online-assessment-attempts`),
  startAttempt: (assessmentId: string) =>
    apiClient.post<CandidateAttempt>(
      `${representativeBase}/online-assessments/${assessmentId}/attempts`,
    ),
  resumeAttempt: (attemptId: string) =>
    apiClient.get<CandidateAttempt>(`${representativeBase}/online-assessment-attempts/${attemptId}`),
  saveAnswer: (
    attemptId: string,
    questionId: string,
    body: { selectedOptionId: string | null; responseText: string | null },
  ) =>
    apiClient.put<CandidateAttempt>(
      `${representativeBase}/online-assessment-attempts/${attemptId}/answers/${questionId}`,
      body,
    ),
  submitAttempt: (attemptId: string) =>
    apiClient.post<CandidateAttempt>(
      `${representativeBase}/online-assessment-attempts/${attemptId}/submit`,
    ),
  getReleasedResult: (attemptId: string) =>
    apiClient.get<AssessmentResult>(
      `${representativeBase}/online-assessment-attempts/${attemptId}/result`,
    ),
  listDelegatorOutcomes: () =>
    apiClient.get<DelegatorAssessmentOutcome[]>("/api/delegators/me/assessment-outcomes"),
};
