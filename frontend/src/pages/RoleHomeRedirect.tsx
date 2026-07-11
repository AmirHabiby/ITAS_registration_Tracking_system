import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const roleHomePath: Record<string, string> = {
  SYSTEM_ADMIN: "/admin/dashboard",
  REPRESENTATIVE: "/representative/dashboard",
  DELEGATOR: "/delegator/dashboard",
  TRAINING_INSTITUTE: "/institute/dashboard",
};

export function RoleHomeRedirect() {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Navigate to={roleHomePath[user.role] ?? "/login"} replace />;
}