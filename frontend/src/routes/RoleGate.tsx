import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const roleHomePath: Record<string, string> = {
  SYSTEM_ADMIN: "/admin/dashboard",
  REPRESENTATIVE: "/representative/dashboard",
  DELEGATOR: "/delegator/dashboard",
  TRAINING_INSTITUTE: "/institute/dashboard",
};

export function RoleGate({
  children,
  roles,
}: {
  children: React.ReactNode;
  roles: string[];
}) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (!roles.includes(user.role)) {
    return <Navigate to={roleHomePath[user.role] ?? "/login"} replace />;
  }

  return <>{children}</>;
}