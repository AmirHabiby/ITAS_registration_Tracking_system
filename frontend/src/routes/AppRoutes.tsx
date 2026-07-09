import { Navigate, Route, Routes } from "react-router-dom";
import { ProtectedRoute } from "./ProtectedRoute";
import { RoleGate } from "./RoleGate";
import { RoleLayout } from "../layouts/RoleLayout";
import { LoginPage } from "../pages/LoginPage";
import { RoleHomeRedirect } from "../pages/RoleHomeRedirect";
import { AdminDashboardPage } from "../pages/admin/AdminDashboardPage";
import { AdminUsersPage } from "../pages/admin/AdminUsersPage";
import { AdminRepresentativesPage } from "../pages/admin/AdminRepresentativesPage";
import { AdminDelegatorsPage } from "../pages/admin/AdminDelegatorsPage";
import { AdminTrainingInstitutesPage } from "../pages/admin/AdminTrainingInstitutesPage";
import { RepresentativeDashboardPage } from "../pages/representative/RepresentativeDashboardPage";
import { RepresentativeTrainingsPage } from "../pages/representative/RepresentativeTrainingsPage";
import { RepresentativeRequestsPage } from "../pages/representative/RepresentativeRequestsPage";
import { RepresentativeResultsPage } from "../pages/representative/RepresentativeResultsPage";
import { DelegatorDashboardPage } from "../pages/delegator/DelegatorDashboardPage";
import { DelegatorTrainingRequestsPage } from "../pages/delegator/DelegatorTrainingRequestsPage";
import { DelegatorTrainedRepresentativesPage } from "../pages/delegator/DelegatorTrainedRepresentativesPage";
import { DelegatorAgentsPage } from "../pages/delegator/DelegatorAgentsPage";
import { InstituteDashboardPage } from "../pages/institute/InstituteDashboardPage";
import { InstituteTrainingsPage } from "../pages/institute/InstituteTrainingsPage";
import { InstituteEnrollmentsPage } from "../pages/institute/InstituteEnrollmentsPage";
import { InstituteAssessmentsPage } from "../pages/institute/InstituteAssessmentsPage";

export function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <RoleHomeRedirect />
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin"
        element={
          <ProtectedRoute>
            <RoleGate roles={["SYSTEM_ADMIN"]}>
              <RoleLayout>
                <Navigate to="/admin/dashboard" replace />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/dashboard"
        element={
          <ProtectedRoute>
            <RoleGate roles={["SYSTEM_ADMIN"]}>
              <RoleLayout>
                <AdminDashboardPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/users"
        element={
          <ProtectedRoute>
            <RoleGate roles={["SYSTEM_ADMIN"]}>
              <RoleLayout>
                <AdminUsersPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/representatives"
        element={
          <ProtectedRoute>
            <RoleGate roles={["SYSTEM_ADMIN"]}>
              <RoleLayout>
                <AdminRepresentativesPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/delegators"
        element={
          <ProtectedRoute>
            <RoleGate roles={["SYSTEM_ADMIN"]}>
              <RoleLayout>
                <AdminDelegatorsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/admin/training-institutes"
        element={
          <ProtectedRoute>
            <RoleGate roles={["SYSTEM_ADMIN"]}>
              <RoleLayout>
                <AdminTrainingInstitutesPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />

      <Route
        path="/representative/dashboard"
        element={
          <ProtectedRoute>
            <RoleGate roles={["REPRESENTATIVE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <RepresentativeDashboardPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/representative/trainings"
        element={
          <ProtectedRoute>
            <RoleGate roles={["REPRESENTATIVE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <RepresentativeTrainingsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/representative/my-requests"
        element={
          <ProtectedRoute>
            <RoleGate roles={["REPRESENTATIVE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <RepresentativeRequestsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/representative/my-results"
        element={
          <ProtectedRoute>
            <RoleGate roles={["REPRESENTATIVE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <RepresentativeResultsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />

      <Route
        path="/delegator/dashboard"
        element={
          <ProtectedRoute>
            <RoleGate roles={["DELEGATOR", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <DelegatorDashboardPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/delegator/training-requests"
        element={
          <ProtectedRoute>
            <RoleGate roles={["DELEGATOR", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <DelegatorTrainingRequestsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/delegator/trained-representatives"
        element={
          <ProtectedRoute>
            <RoleGate roles={["DELEGATOR", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <DelegatorTrainedRepresentativesPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/delegator/agents"
        element={
          <ProtectedRoute>
            <RoleGate roles={["DELEGATOR", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <DelegatorAgentsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />

      <Route
        path="/institute/dashboard"
        element={
          <ProtectedRoute>
            <RoleGate roles={["TRAINING_INSTITUTE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <InstituteDashboardPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/institute/trainings"
        element={
          <ProtectedRoute>
            <RoleGate roles={["TRAINING_INSTITUTE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <InstituteTrainingsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/institute/enrollments"
        element={
          <ProtectedRoute>
            <RoleGate roles={["TRAINING_INSTITUTE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <InstituteEnrollmentsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />
      <Route
        path="/institute/assessments"
        element={
          <ProtectedRoute>
            <RoleGate roles={["TRAINING_INSTITUTE", "SYSTEM_ADMIN"]}>
              <RoleLayout>
                <InstituteAssessmentsPage />
              </RoleLayout>
            </RoleGate>
          </ProtectedRoute>
        }
      />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
