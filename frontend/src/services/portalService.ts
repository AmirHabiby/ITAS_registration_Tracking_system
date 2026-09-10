import { apiClient } from "./apiClient";

export type Training = {
  id: string;
  trainingInstituteProfileId: string;
  title: string;
  description: string;
  startDate: string;
  endDate: string;
  capacity: number;
  passingScore: number | null;
  allowedRetakeAttempts: number;
  accessType: "PRIVATE" | "PUBLIC" | "STAFF";
  status: string;
  active: boolean;
};

export type TrainingRequest = {
  id: string;
  representativeId: string;
  representativeName?: string;
  trainingId: string;
  trainingTitle?: string;
  status: string;
  reviewerUsername: string | null;
  reviewerNote: string | null;
  requestedAt: string;
  reviewedAt: string | null;
};

export type RepresentativeResult = {
  enrollmentId: string;
  trainingId: string;
  assessmentScore: number | null;
  passed: boolean | null;
  assessmentNote: string | null;
  assessedAt: string | null;
  status: string;
};

export type User = {
  id: string;
  username: string;
  role: string;
  enabled: boolean;
  displayName: string;
};

export type Representative = {
  id: string;
  fullName: string;
  email: string;
  status: string;
};

export type Agent = {
  delegationId: string;
  representativeId: string;
  delegatorId: string;
  status: string;
  delegatedAt: string;
  revokedAt: string | null;
  reason: string | null;
};

export type Enrollment = {
  id: string;
  representativeId: string;
  trainingId: string;
  assessmentScore: number | null;
  passed: boolean | null;
  assessmentNote: string | null;
  assessedAt: string | null;
  status: string;
};

export type Assessment = {
  id: string;
  trainingEnrollmentId: string;
  submittedByUserId: string;
  score: number;
  passed: boolean;
  remarks: string;
  assessmentDate: string;
};

export const portalService = {
  listAvailableTrainings: () =>
    apiClient.get<Training[]>("/api/representatives/me/trainings"),
  requestTraining: (trainingId: string, representativeId: string) =>
    apiClient.post<TrainingRequest>("/api/representatives/me/training-requests", {
      trainingId,
      representativeId,
    }),
  listMyRequests: () =>
    apiClient.get<TrainingRequest[]>("/api/representatives/me/training-requests"),
  listMyResults: () =>
    apiClient.get<RepresentativeResult[]>("/api/representatives/me/results"),

  listDelegatorRequests: (pending = false) =>
    apiClient.get<TrainingRequest[]>(
      pending
        ? "/api/delegators/me/training-requests/pending"
        : "/api/delegators/me/training-requests",
    ),
  decideDelegatorRequest: (id: string, action: "approve" | "reject", note: string) =>
    apiClient.patch<TrainingRequest>(
      `/api/delegators/me/training-requests/${id}/${action}`,
      { note },
    ),
  listTrainedRepresentatives: () =>
    apiClient.get<Representative[]>("/api/delegators/me/trained-representatives"),
  markRepresentativeAsAgent: (id: string, note: string) =>
    apiClient.patch<Agent>(`/api/delegators/me/representatives/${id}/mark-as-agent`, {
      note,
    }),
  listAgents: () => apiClient.get<Agent[]>("/api/delegators/me/agents"),

  listUsers: () => apiClient.get<User[]>("/api/admin/users"),
  setUserEnabled: (id: string, enabled: boolean) =>
    apiClient.patch<User>(`/api/admin/users/${id}/${enabled ? "activate" : "deactivate"}`),
  createRepresentative: (body: Record<string, unknown>) =>
    apiClient.post<User>("/api/admin/representatives", body),
  createDelegator: (body: Record<string, unknown>) =>
    apiClient.post<User>("/api/admin/delegators", body),
  createInstitute: (body: Record<string, unknown>) =>
    apiClient.post<User>("/api/admin/training-institutes", body),

  listInstituteEnrollments: () =>
    apiClient.get<Enrollment[]>("/api/institutes/me/enrollments"),
  listInstituteTrainings: () =>
    apiClient.get<Training[]>("/api/institutes/me/trainings"),
  createInstituteTraining: (body: {
    instituteId: string;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    capacity: number;
    accessType: "PRIVATE" | "PUBLIC" | "STAFF";
    staffAccessPassword?: string;
  }) => apiClient.post<Training>("/api/institutes/me/trainings", body),
  listAssessments: () => apiClient.get<Assessment[]>("/api/assessments"),
  submitAssessment: (body: {
    enrollmentId: string;
    score: number;
    remarks: string;
    assessmentDate: string;
  }) => apiClient.post<Assessment>("/api/assessments", body),
};