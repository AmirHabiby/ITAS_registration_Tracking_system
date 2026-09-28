import { apiClient } from './apiClient';

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
  accessType: 'PRIVATE' | 'PUBLIC' | 'STAFF';
  status: string;
  active: boolean;
};

export type Institute = {
  id: string;
  name: string;
  contactEmail: string;
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
  firmId?: string | null;
};

export type DelegatorTrainedOption = {
  id: string;
  name: string;
  email: string;
  type: 'REPRESENTATIVE' | 'FIRM';
  status: string;
  firmId: string | null;
  trainedStaffCount: number;
  delegated: boolean;
};

export type FirmStaff = Representative & { firmId: string };
export type Firm = {
  id: string;
  name: string;
  email: string;
  description: string | null;
  active: boolean;
};
export type FirmDelegation = {
  id: string;
  firmId: string;
  delegatorProfileId: string;
  delegatedAt: string;
  revokedAt: string | null;
  reason: string | null;
};
export type FirmDashboard = {
  totalStaff: number;
  trainedStaff: number;
  staffInTraining: number;
  assignedStaff: number;
  availableTrainedStaff: number;
  delegated: boolean;
};

export type Agent = {
  delegationId: string;
  representativeId: string;
  representativeName?: string;
  delegatorId: string;
  status: string;
  delegatedAt: string;
  revokedAt: string | null;
  reason: string | null;
};

export type Enrollment = {
  id: string;
  representativeId: string;
  representativeName?: string;
  trainingId: string;
  trainingTitle?: string;
  assessmentScore: number | null;
  passed: boolean | null;
  assessmentNote: string | null;
  assessedAt: string | null;
  status: string;
};

export type Assessment = {
  id: string;
  trainingEnrollmentId: string;
  representativeName: string;
  submittedByUserId: string;
  score: number;
  passed: boolean;
  remarks: string;
  assessmentDate: string;
};

export type PublicTrainingSummary = {
  totalTrainings: number;
  enrolledTrainings: number;
  remainingTrainings: number;
};

export type PublicEnrollment = {
  id: string;
  trainingId: string;
  enrolledAt: string;
};

export type TrainingMaterial = {
  id: string;
  trainingId: string;
  title: string;
  description: string | null;
  materialType: string;
  fileUrl: string;
  fileSizeBytes: number | null;
  weekNumber: number;
};

const publicTraineeStorageKey = 'rtts.publicTraineeId';

function getPublicTraineeId() {
  const storedId = localStorage.getItem(publicTraineeStorageKey);
  if (storedId) return storedId;
  const traineeId = crypto.randomUUID();
  localStorage.setItem(publicTraineeStorageKey, traineeId);
  return traineeId;
}

export const portalService = {
  listAvailableTrainings: () => apiClient.get<Training[]>('/api/representatives/me/trainings'),
  requestTraining: (trainingId: string, representativeId: string) =>
    apiClient.post<TrainingRequest>('/api/representatives/me/training-requests', {
      trainingId,
      representativeId,
    }),
  listMyRequests: () => apiClient.get<TrainingRequest[]>('/api/representatives/me/training-requests'),
  listMyResults: () => apiClient.get<RepresentativeResult[]>('/api/representatives/me/results'),

  listDelegatorRequests: (pending = false) =>
    apiClient.get<TrainingRequest[]>(
      pending ? '/api/delegators/me/training-requests/pending' : '/api/delegators/me/training-requests',
    ),
  decideDelegatorRequest: (id: string, action: 'approve' | 'reject', note: string) =>
    apiClient.patch<TrainingRequest>(`/api/delegators/me/training-requests/${id}/${action}`, { note }),
  listTrainedRepresentatives: () => apiClient.get<DelegatorTrainedOption[]>('/api/delegators/me/trained-representatives'),
  markRepresentativeAsAgent: (id: string, note: string) =>
    apiClient.patch<Agent>(`/api/delegators/me/representatives/${id}/mark-as-agent`, {
      note,
    }),
  listAgents: () => apiClient.get<Agent[]>('/api/delegators/me/agents'),
  listFirms: () => apiClient.get<{ id: string; name: string; email: string; description: string; active: boolean }[]>('/api/delegators/me/firms'),
  delegateFirm: (id: string, note: string) => apiClient.patch<FirmDelegation>(`/api/delegators/me/firms/${id}/delegate`, { note }),
  revokeFirm: (id: string) => apiClient.patch<FirmDelegation>(`/api/delegators/me/firms/${id}/revoke`),

  listUsers: () => apiClient.get<User[]>('/api/admin/users'),
  setUserEnabled: (id: string, enabled: boolean) =>
    apiClient.patch<User>(`/api/admin/users/${id}/${enabled ? 'activate' : 'deactivate'}`),
  createRepresentative: (body: Record<string, unknown>) => apiClient.post<User>('/api/admin/representatives', body),
  createDelegator: (body: Record<string, unknown>) => apiClient.post<User>('/api/admin/delegators', body),
  createInstitute: (body: Record<string, unknown>) => apiClient.post<User>('/api/admin/training-institutes', body),
  createFirm: (body: Record<string, unknown>) => apiClient.post<User>('/api/admin/firms', body),
  listAdminFirms: () => apiClient.get<Firm[]>('/api/admin/firms'),
  listInstitutes: () => apiClient.get<Institute[]>('/api/institutes'),
  createAdminTraining: (body: {
    instituteId: string;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    capacity: number;
    accessType: 'PUBLIC' | 'STAFF';
    staffAccessPassword?: string;
  }) => apiClient.post<Training>('/api/trainings', body),
  uploadTrainingMaterial: (trainingId: string, weekNumber: number, file: File, title?: string) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('trainingId', trainingId);
    formData.append('weekNumber', String(weekNumber));
    if (title) formData.append('title', title);
    return apiClient.post('/api/media/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  firmDashboard: () => apiClient.get<FirmDashboard>('/api/dashboard/me'),
  listFirmStaff: () => apiClient.get<FirmStaff[]>('/api/firm-admin/staff'),
  createFirmStaff: (body: Record<string, unknown>) => apiClient.post<User>('/api/firm-admin/staff', body),
  getFirmDelegation: () => apiClient.get<FirmDelegation | null>('/api/firm-admin/delegation'),
  assignFirmAgent: (id: string, reason: string) => apiClient.post(`/api/firm-admin/staff/${id}/assign-agent`, { reason }),
  revokeFirmAssignment: (id: string) => apiClient.patch(`/api/firm-admin/assignments/${id}/revoke`),

  listInstituteEnrollments: () => apiClient.get<Enrollment[]>('/api/institutes/me/enrollments'),
  listInstituteTrainings: () => apiClient.get<Training[]>('/api/institutes/me/trainings'),
  createInstituteTraining: (body: {
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    capacity: number;
    accessType: 'PRIVATE' | 'PUBLIC' | 'STAFF';
    staffAccessPassword?: string;
  }) => apiClient.post<Training>('/api/institutes/me/trainings', body),
  listAssessments: () => apiClient.get<Assessment[]>('/api/assessments'),
  submitAssessment: (body: { representativeUsername: string; score: number; remarks: string; assessmentDate: string }) =>
    apiClient.post<Assessment>('/api/assessments', body),

  listPublicTrainings: () => apiClient.get<Training[]>('/api/public/trainings'),
  getPublicTrainingSummary: () =>
    apiClient.get<PublicTrainingSummary>('/api/public/trainings/summary', {
      headers: { 'X-Public-Trainee-Id': getPublicTraineeId() },
    }),
  enrollPublicTrainee: (trainingId: string) =>
    apiClient.post(`/api/public/trainings/${trainingId}/enroll`, null, {
      headers: { 'X-Public-Trainee-Id': getPublicTraineeId() },
    }),
  listPublicEnrollments: () =>
    apiClient.get<PublicEnrollment[]>('/api/public/trainings/enrolled', {
      headers: { 'X-Public-Trainee-Id': getPublicTraineeId() },
    }),
  listPublicTrainingMaterials: (trainingId: string) =>
    apiClient.get<TrainingMaterial[]>(`/api/public/trainings/${trainingId}/materials`, {
      headers: { 'X-Public-Trainee-Id': getPublicTraineeId() },
    }),
  listStaffTrainings: () => apiClient.get<Training[]>('/api/staff/trainings'),
  accessStaffTraining: (body: { name: string; department: string; trainingPassword: string }) =>
    apiClient.post<Training>('/api/staff/trainings/access', body),
};
